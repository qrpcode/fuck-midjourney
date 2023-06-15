package com.example.midjourney.service;

import com.example.midjourney.bean.pojo.OpenAIKey;

public interface OpenAIKeyService {

    OpenAIKey findToken();

    void markInsufficient(OpenAIKey openAIKey);

}
