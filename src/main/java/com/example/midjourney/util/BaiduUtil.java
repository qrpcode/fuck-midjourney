package com.example.midjourney.util;

import com.alibaba.fastjson.JSON;
import com.example.midjourney.bean.BaiduTextCensor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.logging.log4j.util.Strings;

import java.util.Objects;

import static com.example.midjourney.contant.Constant.BAIDU_TOKEN;

@Slf4j
public class BaiduUtil {

    @SneakyThrows
    public static String queryBaiduToken() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=" +
                        "***&client_secret=***")
                .get()
                .build();
        Response response = client.newCall(request).execute();
        String token = JSON.parseObject(Objects.requireNonNull(response.body()).string()).get("access_token").toString();
        if (Strings.isEmpty(token)) {
            throw new RuntimeException("百度Token获取失败");
        }
        log.info("[baidu token] {}", token);
        return token;
    }

    @SneakyThrows
    public static BaiduTextCensor textCensor(String text) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "text=" + text);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rest/2.0/solution/v1/text_censor/v2/user_defined?access_token=" + BAIDU_TOKEN)
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = client.newCall(request).execute();
        return JSON.parseObject(Objects.requireNonNull(response.body()).string(), BaiduTextCensor.class);
    }

}
