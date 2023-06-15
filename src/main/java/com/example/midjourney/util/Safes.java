package com.example.midjourney.util;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

public class Safes {

    public static <T> List<T> of(List<T> list) {
        return Objects.isNull(list) ? Lists.newArrayList() : list;
    }

}
