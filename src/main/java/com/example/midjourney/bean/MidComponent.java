package com.example.midjourney.bean;

import lombok.Data;

import java.util.List;

@Data
public class MidComponent {

    private Integer type;

    private List<MidInnerComponent> components;

}
