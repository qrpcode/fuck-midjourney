package com.example.midjourney.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class MidInnerComponent {

    private Integer type;

    @JSONField(name = "custom_id")
    private String customId;

}
