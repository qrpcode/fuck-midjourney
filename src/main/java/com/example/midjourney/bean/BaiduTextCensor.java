package com.example.midjourney.bean;

import lombok.Data;

import java.util.List;

@Data
public class BaiduTextCensor {

    private String conclusion;

    private Integer conclusionType;

    private List<BaiduTextCensorData> data;

}
