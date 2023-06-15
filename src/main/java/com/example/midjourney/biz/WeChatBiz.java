package com.example.midjourney.biz;

import com.alibaba.fastjson.JSON;
import com.example.midjourney.contant.Constant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import static com.example.midjourney.contant.Constant.WX_TOKEN_MAP;

@Slf4j
@Service
public class WeChatBiz {

    @SneakyThrows
    @Scheduled(cron ="0 */1 * * * ?")
    public void flushToken() {
        queryToken();
    }

    @SneakyThrows
    public static void queryToken() {
        for (Map.Entry<String, String> entry : Constant.WX_TOKEN_CHANNEL.entrySet()) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(entry.getValue())
                    .get()
                    .build();
            Response response = client.newCall(request).execute();
            String thisToken = Objects.requireNonNull(response.body()).string();
            WX_TOKEN_MAP.put(entry.getKey(), thisToken);
        }
    }

    @SneakyThrows
    public void sendTextMsg(String user, String msg, String channel) {
        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "{\r\n  " +
                "  \"touser\": \""  + user + "\",\r\n    \"msgtype\": \"text\",\r\n   " +
                " \"text\": {\r\n        \"content\": \"" + msg.trim() + "\"\r\n    }\r\n}");
        Request request = new Request.Builder()
                .url("https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + channelToken(channel))
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        log.info(Objects.requireNonNull(response.body()).string());
    }

    private String channelToken(String channel) {
        String token = WX_TOKEN_MAP.get(channel);
        log.info("[channel token] channel:{} token:{}", channel, token);
        return token;
    }

    @SneakyThrows
    public void sendImgMsg(String user, String mediaId, String channel) {
        log.info("[sendImgMsg] user:{}  mediaId:{}", user, mediaId);
        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "{\r\n  " +
                "  \"touser\": \""  + user + "\",\r\n    \"msgtype\": \"image\",\r\n   " +
                " \"image\": {\"media_id\": \"" + mediaId + "\"\r\n    }\r\n}");
        Request request = new Request.Builder()
                .url("https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + channelToken(channel))
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        log.info(Objects.requireNonNull(response.body()).string());
    }

    @SneakyThrows
    public String sendImg(String localPath, String channel) {
        log.info("[localPath] {}", localPath);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("media","/" + localPath,
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File("/" + localPath)))
                .build();
        Request request = new Request.Builder()
                .url("https://api.weixin.qq.com/cgi-bin/media/upload?type=image&access_token=" + channelToken(channel))
                .method("POST", body)
                .build();
        Response response = client.newCall(request).execute();
        String json = Objects.requireNonNull(response.body()).string();
        log.info("[wechat img update] {}", json);
        return JSON.parseObject(json).get("media_id").toString();
    }
}
