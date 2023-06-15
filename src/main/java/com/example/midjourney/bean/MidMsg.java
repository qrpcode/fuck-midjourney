package com.example.midjourney.bean;

import lombok.Data;

import java.util.List;

@Data
public class MidMsg {

    private String id;

    private Integer type;

    private String content;

    private List<Attachment> attachments;

    private List<MidComponent> components;
}
