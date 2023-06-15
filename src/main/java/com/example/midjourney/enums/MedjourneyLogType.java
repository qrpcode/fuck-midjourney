package com.example.midjourney.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MedjourneyLogType {
    NORMAL(0, "常规请求"),
    BIG_IMG(1, "扩图请求"),
    ITERATIVE(2, "迭代请求"),
    ;
    private final int code;
    private final String name;
}
