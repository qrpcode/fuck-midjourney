package com.example.midjourney.biz;

import com.aliyun.alimt20181012.Client;
import com.aliyun.alimt20181012.models.TranslateGeneralRequest;
import com.aliyun.alimt20181012.models.TranslateGeneralResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.Common;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
public class TranslateBiz {

    public static Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
         Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        config.endpoint = "mt.cn-hangzhou.aliyuncs.com";
        return new com.aliyun.alimt20181012.Client(config);
    }

    @SneakyThrows
    public String translate(String str) {
        Client client = createClient("***", "***");
        TranslateGeneralRequest translateGeneralRequest = new TranslateGeneralRequest()
                .setFormatType("text")
                .setSourceText(str)
                .setScene("general")
                .setSourceLanguage("zh")
                .setTargetLanguage("en");
        RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        try {
            TranslateGeneralResponse response = client.translateGeneralWithOptions(translateGeneralRequest, runtime);
            return response.getBody().getData().getTranslated();
        } catch (TeaException error) {
            Common.assertAsString(error.message);
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            Common.assertAsString(error.message);
        }
        return "";
    }

}
