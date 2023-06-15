package com.example.midjourney.biz;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.example.midjourney.bean.BaiduTextCensor;
import com.example.midjourney.bean.BaiduTextCensorData;
import com.example.midjourney.bean.MidMsg;
import com.example.midjourney.bean.pojo.Discord;
import com.example.midjourney.bean.pojo.MidjourneyLog;
import com.example.midjourney.bean.pojo.RoomInfo;
import com.example.midjourney.contant.Constant;
import com.example.midjourney.enums.MedjourneyLogType;
import com.example.midjourney.service.DiscordService;
import com.example.midjourney.service.MidjourneyLogService;
import com.example.midjourney.service.MemberService;
import com.example.midjourney.service.RoomInfoService;
import com.example.midjourney.util.*;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.logging.log4j.util.Strings;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.midjourney.contant.Constant.*;

/**
 * 启动就运行，不停刷新消息列表
 */
@Slf4j
@Service
public class MidJourneyBiz {

    private static BigDecimal bigDecimal = new BigDecimal("1099765990370980513")
            .add(new BigDecimal(System.currentTimeMillis()));

    @Resource
    private WeChatBiz weChatBiz;

    @Resource
    private TranslateBiz translateBiz;

    @Resource
    private MidjourneyLogService midjourneyLogService;

    @Resource
    private MemberService memberService;

    @Resource
    private RoomInfoService roomInfoService;

    @Resource
    private DiscordService discordService;

    public void buildImg(Long id) {
        MidjourneyLog midjourneyLog = midjourneyLogService.findById(id);
        try {
            if (midjourneyLog.getStatus() != 0) {
                return;
            }
            if (isFailMsg(midjourneyLog.getChannel(), midjourneyLog)) {
                log.info("[失败提示] 消息校验不通过 log:{}", midjourneyLog);
                sendBuildFail(midjourneyLog);
                return;
            }
            String prompt = midjourneyLog.getPrompt();
            if (midjourneyLog.getType() == MedjourneyLogType.BIG_IMG.getCode()) {
                downImg(midjourneyLog);
                return;
            }
            BaiduTextCensor censor = null;
            if (TextUtil.isHaveChinese(prompt)) {
                censor = BaiduUtil.textCensor(prompt);
            }
            prompt = cleanMsg(prompt);
            if (isBlackWord(prompt)) {
                sendSensitive(midjourneyLog);
                return;
            }
            if (Objects.isNull(censor)) {
                censor = BaiduUtil.textCensor(prompt);
            }
            midjourneyLogService.updatePrompt(midjourneyLog, prompt);
            if (censor.getConclusionType() == 2 || censor.getConclusionType() == 3) {
                sendSensitive(midjourneyLog, censor);
                return;
            }
            if (midjourneyLog.getType() == MedjourneyLogType.ITERATIVE.getCode()) {
                //暂不支持
                sendBuildFail(midjourneyLog);
                return;
            }
            RoomInfo roomInfo = roomInfoService.findIdleRoom();
            if (Objects.isNull(roomInfo)) {
                log.error("[并发超出警报] 当前并发次数已经无法满足！！！");
                sendBuildFail(midjourneyLog);
                return;
            }
            if (sendMsg(prompt, roomInfo)) {
                midjourneyLogService.updateRoom(midjourneyLog, roomInfo);
            } else {
                log.error("[发送失败] 发送信息失败，请检查");
                sendBuildFail(midjourneyLog);
            }
        } catch (Throwable t) {
            sendBuildFail(midjourneyLog);
        }
    }

    @SneakyThrows
    private void downImg(MidjourneyLog midjourneyLog) {
        MidjourneyLog lastLog = midjourneyLogService.findLastNormalLog(midjourneyLog.getMemberId());
        log.info("[下载图片] {}", lastLog);
        if (Objects.isNull(lastLog)) {
            log.info("[失败提示] 找不到上一次的图片 log:{}", midjourneyLog);
            sendBuildFail(midjourneyLog);
            return;
        }
        String imgFile = lastLog.getImgFile();
        File file = new File(imgFile);
        if (!file.exists()) {
            log.info("[失败提示] 上一次图片不存在 log:{}", lastLog);
            sendBuildFail(midjourneyLog);
            return;
        }
        cutAndSendImg(midjourneyLog, file);
    }

    private void cutAndSendImg(MidjourneyLog midjourneyLog, File oldFile) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new FileInputStream(oldFile));
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        String msg = midjourneyLog.getPrompt().trim();
        String newFileName;
        if (msg.equalsIgnoreCase("u1")) {
            newFileName = oldFile.getPath() + "_u1.png";
            ImgUtil.cut(oldFile, FileUtil.file(newFileName),
                    new Rectangle(0, 0, width / 2, height / 2)
            );
        } else if (msg.equalsIgnoreCase("u2")) {
            newFileName = oldFile.getPath() + "_u2.png";
            ImgUtil.cut(oldFile, FileUtil.file(newFileName),
                    new Rectangle(width / 2, 0, width / 2, height / 2)
            );
        } else if (msg.equalsIgnoreCase("u3")) {
            newFileName = oldFile.getPath() + "_u3.png";
            ImgUtil.cut(oldFile, FileUtil.file(newFileName),
                    new Rectangle(0, height / 2, width / 2, height / 2)
            );
        } else if (msg.equalsIgnoreCase("u4")) {
            newFileName = oldFile.getPath() + "_u4.png";
            ImgUtil.cut(oldFile, FileUtil.file(newFileName),
                    new Rectangle(width / 2, height / 2, width / 2, height / 2)
            );
        } else {
            sendBuildFail(midjourneyLog);
            return;
        }
        String mediaId = weChatBiz.sendImg(newFileName, midjourneyLog.getChannel());
        log.info("[mediaId] {}", mediaId);
        if (Strings.isNotEmpty(mediaId)) {
            String wxId = memberService.selectWxidById(midjourneyLog.getMemberId());
            if (Strings.isEmpty(wxId)) {
                sendBuildFail(midjourneyLog);
                return;
            }
            midjourneyLogService.updateFinish(midjourneyLog);
            weChatBiz.sendImgMsg(wxId, mediaId, midjourneyLog.getChannel());
        }
    }

    private boolean isFailMsg(String channel, MidjourneyLog midjourneyLog) {
        return Objects.isNull(midjourneyLog) || Strings.isEmpty(midjourneyLog.getPrompt()) || Strings.isEmpty(channel)
                || Objects.isNull(midjourneyLog.getMemberId()) || checkNotRunMsg(midjourneyLog);
    }

    public boolean checkNotRunMsg(MidjourneyLog midjourneyLog) {
        String prompt = midjourneyLog.getPrompt();
        if (midjourneyLog.getType() == MedjourneyLogType.BIG_IMG.getCode()) {
            return !(prompt.equalsIgnoreCase("u1") || prompt.equalsIgnoreCase("u2")
                    || prompt.equalsIgnoreCase("u3") || prompt.equalsIgnoreCase("u4"));
        } else if (midjourneyLog.getType() == MedjourneyLogType.ITERATIVE.getCode()) {
            return !(prompt.equalsIgnoreCase("v1") || prompt.equalsIgnoreCase("v2")
                    || prompt.equalsIgnoreCase("v3") || prompt.equalsIgnoreCase("v4"));
        } else {
            return false;
        }
    }

    private String cleanMsg(String msg) {
        msg = msg.replace("—", "--")
                .replace("-- ", "--")
                .replace("-- ", "--")
                .replace("-- ", "--")
                .replace("，", ",")
                .replace("/", "")
                .replace("--v", " --v ")
                .replace("--niji", " --niji ")
                .replace("--ar", " --ar ")
                .replace("--aspect", " --ar ")
                .replace("--chaos", " --chaos ")
                .replace("--c", " --c ")
                .replace("--no", " --no ")
                .replace("--quality", " --quality ")
                .replace("--q", " --q ")
                .replace("--repeat", " --repeat ")
                .replace("--s", " --s ")
                .replace("--upbeta", " --upbeta ")
                .trim();
        if (TextUtil.isHaveChinese(msg)) {
            msg = translateBiz.translate(msg);
        }
        msg = msg.replace("，", ",")
                .replace("/", "")
                .replace("--v5", " --v 5 ")
                .replace("--niji5", " --niji 5 ")
                .trim();
        if (!msg.contains("--niji") && !msg.contains("--v") && msg.length() > 3) {
            msg = msg + " --v 5";
        }
        return msg;
    }

    public void sendBuildFail(MidjourneyLog midjourneyLog) {
        String wxId = memberService.selectWxidById(midjourneyLog.getMemberId());
        CallBackUtil.failCallBack(midjourneyLog.getChannel(), MIDJOURNEY, midjourneyLog.getId());
        midjourneyLogService.updateFail(midjourneyLog);
        weChatBiz.sendTextMsg(wxId, "抱歉，您的消息【" + midjourneyLog.getPrompt() + "】处理失败，已为您退换对应电量",
                midjourneyLog.getChannel());
    }

    @Scheduled(cron = "34 * * * * ? ")
    public void checkImg() {
        //房间号清理
        cleanRoomNumber();
        //查询所有进行中的任务
        List<MidjourneyLog> logs = midjourneyLogService.selectAllDoing().stream()
                .filter(log -> Objects.nonNull(log.getRoomId())).collect(Collectors.toList());
        //找到超时任务进行关闭
        List<MidjourneyLog> failLogs = logs.stream()
                .filter(log -> System.currentTimeMillis() - log.getCreateTime().getTime() > MAX_WAIT_TIME)
                .collect(Collectors.toList());
        failLogs.forEach(this::sendBuildFail);
        //剩余任务整理出来房间号
        logs.removeAll(failLogs);
        Set<Integer> roomSet = logs.stream().map(MidjourneyLog::getRoomId).collect(Collectors.toSet());
        //轮询当前进度
        for (Integer roomId : roomSet) {
            RoomInfo roomInfo = roomInfoService.findById(roomId);
            if (Objects.isNull(roomInfo)) {
                log.error("[room没找到] roomId:{} 没找到对应房间，看一下是不是挂了", roomId);
                continue;
            }
            Discord discord = discordService.findById(roomInfo.getDiscordId());
            List<MidMsg> midMsgs = readNowList(roomInfo, discord);
            log.info("[消息列表] {}", midMsgs);
            checkAndSendMsg(midMsgs, logs, roomInfo);
        }
    }

    private void cleanRoomNumber() {
        List<RoomInfo> roomInfos = roomInfoService.findAll();
        for (RoomInfo roomInfo : roomInfos) {
            int count = midjourneyLogService.selectRoomOnUse(roomInfo.getId());
            roomInfo.setNowNumber(count);
            roomInfoService.update(roomInfo);
        }
    }

    private void checkAndSendMsg(List<MidMsg> midMsgs, List<MidjourneyLog> logs, RoomInfo roomInfo) {
        List<MidjourneyLog> roomLogs = logs.stream()
                .filter(log -> log.getRoomId().equals(roomInfo.getId()))
                .collect(Collectors.toList());
        midMsgs.stream()
                .filter(this::isPrintOk)
                .forEach(m -> roomLogs.forEach(midjourey -> {
                    if (m.getContent().startsWith(MID_JOURNEY_HEAD + filterHead(midjourey.getPrompt()))) {
                        log.info("[Midjourney 配对] msg:{}  key:{}", m, midjourey.getPrompt());
                        String wxid = memberService.selectWxidById(midjourey.getMemberId());
                        String url = m.getAttachments().get(0).getUrl();
                        String localPath = Constant.FILE_PATH + UUID.randomUUID() + "." + FileUtil.getSuffix(url);
                        ImgDownUtil.getImage(url.replace("https://", "http://"), localPath);
                        localPath = localPath.replace("\\", "/");
                        if ("webp".equals(FileUtil.getSuffix(localPath))) {
                            com.example.midjourney.util.ImgUtil.webpToPng(localPath, localPath + ".png");
                            localPath = localPath + ".png";
                        }
                        String mediaId = weChatBiz.sendImg(localPath, midjourey.getChannel());
                        log.info("[mediaId] {}", mediaId);
                        if (Strings.isNotEmpty(mediaId)) {
                            weChatBiz.sendImgMsg(wxid, mediaId, midjourey.getChannel());
                            weChatBiz.sendTextMsg(wxid, "下载高清大图口令：\n\n☆左上图回复：U1\n\n☆右上图回复：U2" +
                                    "\n\n☆左下图回复：U3\n\n☆右下图回复：U4\n\n下载大图也会扣电量哦~", midjourey.getChannel());
                            midjourey.setImgFile(localPath);
                            midjourneyLogService.updateFinish(midjourey);
                        }
                    }
                }));
    }

    private String filterHead(String prompt) {
        return Lists.newArrayList(Splitter.on("--").split(prompt)).stream().findFirst().orElse("").trim();
    }

    private boolean isPrintOk(MidMsg midMsg) {
        if (Objects.isNull(midMsg)
                || Strings.isEmpty(midMsg.getContent())
                || !midMsg.getContent().contains(MID_FIND_LEFT)) {
            return false;
        }
        String str = midMsg.getContent().substring(midMsg.getContent().indexOf(MID_FIND_LEFT));
        return !str.contains("%") && !str.contains("Waiting");
    }

    private boolean isBlackWord(String msg) {
        msg = msg.toLowerCase();
        for (String s : Constant.MidjourneyBlackWord) {
            if (msg.contains(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void sendSensitive(MidjourneyLog midjourneyLog) {
        String wxid = memberService.selectWxidById(midjourneyLog.getMemberId());
        midjourneyLogService.updateSensitive(midjourneyLog);
        weChatBiz.sendTextMsg(wxid, "【违规提示】输入内容包含违禁词，依法进行屏蔽。", midjourneyLog.getChannel());
    }

    /**
     * 审核不通过通知用户
     */
    private void sendSensitive(MidjourneyLog midjourneyLog, BaiduTextCensor censor) {
        String reason = Joiner.on("、")
                .join(Safes.of(censor.getData()).stream()
                        .map(BaiduTextCensorData::getMsg)
                        .collect(Collectors.toList()));
        reason = Strings.isEmpty(reason) ? "包含敏感信息" : reason;
        String fullText = "【违规提示】输入内容因 " + reason + " ，依法进行屏蔽。（百度提供审核能力）";
        String wxid = memberService.selectWxidById(midjourneyLog.getMemberId());
        midjourneyLogService.updateSensitive(midjourneyLog);
        weChatBiz.sendTextMsg(wxid, fullText, midjourneyLog.getChannel());
    }

    public List<MidMsg> readNowList(RoomInfo roomInfo, Discord discord) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url("https://discord.com/api/v9/channels/" + roomInfo.getDiscordChannelId() + "/messages?limit=50")
                .get()
                .addHeader("authority", "discord.com")
                .addHeader("accept", "*/*")
                .addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6")
                .addHeader("authorization", discord.getAuthorization())
                .addHeader("cache-control", "no-cache")
                .addHeader("cookie", discord.getCookie())
                .addHeader("pragma", "no-cache")
                .addHeader("referer", "https://discord.com/channels/" + roomInfo.getDiscordGuildId() +
                        "/" + roomInfo.getDiscordChannelId())
                .addHeader("sec-ch-ua", "\"Not_A Brand\";v=\"99\", \"Microsoft Edge\";v=\"109\", \"Chromium\";v=\"109\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.70")
                .addHeader("x-debug-options", "bugReporterEnabled")
                .addHeader("x-discord-locale", "zh-CN")
                .addHeader("x-super-properties", discord.getSuperProperties())
                .build();
        try {
            Response response = client.newCall(request).execute();
            String string = Objects.requireNonNull(response.body()).string();
            response.close();
            return JSON.parseArray(string, MidMsg.class);
        } catch (Throwable t) {
            log.error("[发生意外 读取消息失败] room: {}", roomInfo);
        }
        return Lists.newArrayList();
    }

    @SneakyThrows
    public boolean sendMsg(String msg, RoomInfo roomInfo) {
        Discord discord = discordService.findById(roomInfo.getDiscordId());
        bigDecimal = bigDecimal.add(new BigDecimal(1000));
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("payload_json", "{\"type\":2,\"application_id\":\"936929561302675456\"," +
                        "\"guild_id\":\"" + roomInfo.getDiscordGuildId() + "\"," +
                        "\"channel_id\":\"" + roomInfo.getDiscordChannelId() + "\"," +
                        "\"session_id\":\"" + discord.getSessionId() + "\"," +
                        "\"data\":{\"version\":\"1077969938624553050\"," +
                        "\"id\":\"938956540159881230\"," +
                        "\"name\":\"imagine\"," +
                        "\"type\":1,\"options\":[" +
                        "{\"type\":3,\"name\":\"prompt\"," +
                        "\"value\":\"" + TextUtil.cleanString(msg) + "\"}]," +
                        "\"application_command\":" +
                        "{\"id\":\"938956540159881230\",\"application_id\":\"936929561302675456\",\"version\":" +
                        "\"1077969938624553050\",\"default_member_permissions\":null,\"type\":1,\"nsfw\":false," +
                        "\"name\":\"imagine\",\"description\":\"Create images with Midjourney\",\"dm_permission\":true," +
                        "\"contexts\":null,\"options\":[{\"type\":3,\"name\":\"prompt\"," +
                        "\"description\":\"The prompt to imagine\",\"required\":true}]}," +
                        "\"attachments\":[]},\"nonce\":\"" + bigDecimal.toString() + "\"} ")
                .build();
        Request request = new Request.Builder()
                .url("https://discord.com/api/v9/interactions")
                .method("POST", body)
                .addHeader("authority", "discord.com")
                .addHeader("accept", "*/*")
                .addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6")
                .addHeader("authorization", discord.getAuthorization())
                .addHeader("cache-control", "no-cache")
                .addHeader("cookie", discord.getCookie())
                .addHeader("origin", "https://discord.com")
                .addHeader("pragma", "no-cache")
                .addHeader("referer", "https://discord.com/channels/" + roomInfo.getDiscordGuildId() +
                        "/" + roomInfo.getDiscordChannelId())
                .addHeader("sec-ch-ua", "\"Not_A Brand\";v=\"99\", \"Microsoft Edge\";v=\"109\", \"Chromium\";v=\"109\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.70")
                .addHeader("x-debug-options", "bugReporterEnabled")
                .addHeader("x-discord-locale", "zh-CN")
                .addHeader("x-super-properties", discord.getSuperProperties())
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() == 204) {
            roomInfoService.addOnce(roomInfo);
            response.close();
            return true;
        }
        response.close();
        log.info("[midjourney发消息失败] {}", response.code());
        return false;
    }
}
