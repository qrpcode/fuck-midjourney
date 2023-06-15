package com.example.midjourney.biz;

import com.alibaba.fastjson.JSON;
import com.example.midjourney.bean.BaiduTextCensor;
import com.example.midjourney.bean.BaiduTextCensorData;
import com.example.midjourney.bean.OpenAiVo;
import com.example.midjourney.bean.pojo.ChatGPTLog;
import com.example.midjourney.bean.pojo.OpenAIKey;
import com.example.midjourney.service.ChatGPTLogService;
import com.example.midjourney.service.MemberService;
import com.example.midjourney.service.OpenAIKeyService;
import com.example.midjourney.util.BaiduUtil;
import com.example.midjourney.util.CallBackUtil;
import com.example.midjourney.util.Safes;
import com.google.common.base.Joiner;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.midjourney.contant.Constant.CHATGPT;
import static com.example.midjourney.contant.Constant.INSUFFICIENT;

@Slf4j
@Service
public class ChatGPTBiz {

    @Resource
    private WeChatBiz weChatBiz;

    @Resource
    private ChatGPTLogService chatGPTLogService;

    @Resource
    private MemberService memberService;

    @Resource
    private OpenAIKeyService openAIKeyService;

    @SneakyThrows
    public void sendChatGPTMsg(Long id) {
        ChatGPTLog chatGPTLog = chatGPTLogService.findById(id);
        if (Objects.isNull(chatGPTLog) || Strings.isEmpty(chatGPTLog.getAsk())) {
            sendFail(chatGPTLog);
            return;
        }
        if (chatGPTLog.getStatus() != 0) {
            return;
        }
        BaiduTextCensor censor = BaiduUtil.textCensor(chatGPTLog.getAsk());
        if (censor.getConclusionType() == 2 || censor.getConclusionType() == 3) {
            sendSensitive(chatGPTLog, censor);
            return;
        }
        String answer = askOpenAi(chatGPTLog.getAsk());
        if (Strings.isNotEmpty(answer)) {
            log.info("[openAI] {}", answer);
            BaiduTextCensor answerCensor = BaiduUtil.textCensor(answer);
            if (answerCensor.getConclusionType() == 2 || answerCensor.getConclusionType() == 3) {
                sendSensitive(chatGPTLog, answerCensor);
                return;
            }
            String wxid = memberService.selectWxidById(chatGPTLog.getMemberId());
            weChatBiz.sendTextMsg(wxid, answer, chatGPTLog.getChannel());
            chatGPTLogService.updateFinish(chatGPTLog);
        } else {
            sendFail(chatGPTLog);
        }
    }

    private String askOpenAi(String ask) {
        OpenAIKey openAIKey = openAIKeyService.findToken();
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(1200, TimeUnit.SECONDS)
                    .readTimeout(1200, TimeUnit.SECONDS)
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            okhttp3.RequestBody body = RequestBody.create(mediaType, "{\"model\": \"text-davinci-003\",\"prompt\": " +
                    JSON.toJSONString(ask) + ",\"temperature\": 0.4,\"max_tokens\": 1500,\"top_p\": 1," +
                    "\"frequency_penalty\":1.11,\"presence_penalty\":0}");
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/completions")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + openAIKey.getOpenAiKey())
                    .build();
            Response response = client.newCall(request).execute();
            String json = Objects.requireNonNull(response.body()).string();
            log.info("[ChatGPT 原生返回] {}", json);
            OpenAiVo openAiVo = JSON.parseObject(json, OpenAiVo.class);
            if (isMatchInsufficientQuotaError(openAiVo)) {
                openAIKeyService.markInsufficient(openAIKey);
                return askOpenAi(ask);
            }
            return openAiVo.getChoices().get(0).getText();
        } catch (Throwable t) {
            log.error("[查询OpenAI失败]", t);
            return "";
        }
    }

    private boolean isMatchInsufficientQuotaError(OpenAiVo openAiVo) {
        Map<String, String> error = openAiVo.getError();
        if (CollectionUtils.isEmpty(error)) {
            return false;
        }
        return INSUFFICIENT.equals(error.get("type"));
    }


    /**
     * 审核不通过通知用户
     */
    private void sendSensitive(ChatGPTLog chatGPTLog, BaiduTextCensor censor) {
        String reason = Joiner.on("、")
                .join(Safes.of(censor.getData()).stream()
                        .map(BaiduTextCensorData::getMsg)
                        .collect(Collectors.toList()));
        reason = Strings.isEmpty(reason) ? "包含敏感信息" : reason;
        String fullText = "【违规提示】输入或回答中因 " + reason + " ，依法进行屏蔽。（百度提供审核能力）";
        chatGPTLogService.updateSensitive(chatGPTLog);
        String wxid = memberService.selectWxidById(chatGPTLog.getMemberId());
        weChatBiz.sendTextMsg(wxid, fullText, chatGPTLog.getChannel());
    }

    /**
     * 调用失败通知用户
     */
    private void sendFail(ChatGPTLog chatGPTLog) {
        if (Objects.isNull(chatGPTLog) || Objects.isNull(chatGPTLog.getId())) {
            return;
        }
        String wxid = memberService.selectWxidById(chatGPTLog.getMemberId());
        if (Strings.isEmpty(wxid)) {
            return;
        }
        chatGPTLogService.updateFail(chatGPTLog);
        CallBackUtil.failCallBack(chatGPTLog.getChannel(), CHATGPT, chatGPTLog.getId());
        weChatBiz.sendTextMsg(wxid, "【提示】抱歉，系统处理失败，已为您退换对应电量", chatGPTLog.getChannel());
    }

}
