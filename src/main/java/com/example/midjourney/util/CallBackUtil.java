package com.example.midjourney.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.logging.log4j.util.Strings;

import java.util.Objects;

import static com.example.midjourney.contant.Constant.CHANNEL_CALL_BACK;

@Slf4j
public class CallBackUtil {

    @SneakyThrows
    public static void failCallBack(String channel, String type, Long id) {
        String url = CHANNEL_CALL_BACK.get(channel);
        if (Strings.isEmpty(url)) {
            throw new RuntimeException("[通道失败] 渠道 " + channel + " 没有对应回调地址");
        }
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(url + "?type=" + type + "&id=" + id + "&status=1")
                .get()
                .build();
        Response response = client.newCall(request).execute();
        if (Objects.nonNull(response.body())) {
            log.info("[失败回调返回内容] {}", response.body().string());
        }
    }

}
