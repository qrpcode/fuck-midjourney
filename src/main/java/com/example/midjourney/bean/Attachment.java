package com.example.midjourney.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class Attachment {

    private String id;

    private String filename;

    private String size;

    private String url;

    @JSONField(name = "proxy_url")
    private String proxyUrl;

    private String width;

    private String height;

    @JSONField(name = "content_type")
    private String contentType;

}
