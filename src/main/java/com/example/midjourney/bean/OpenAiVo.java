package com.example.midjourney.bean;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OpenAiVo {

    private List<Choice> choices;

    private Map<String, String> error;

}
