package com.example.midjourney.service.impl;

import com.example.midjourney.bean.pojo.OpenAIKey;
import com.example.midjourney.mapper.OpenAIKeyMapper;
import com.example.midjourney.service.OpenAIKeyService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OpenAIKeyServiceImpl implements OpenAIKeyService {

    @Resource
    private OpenAIKeyMapper openAIKeyMapper;

    @Override
    public OpenAIKey findToken() {
        return openAIKeyMapper.selectOnceNotUse();
    }

    @Override
    public void markInsufficient(OpenAIKey openAIKey) {
        openAIKey.setStatus(1);
        openAIKeyMapper.updateByPrimaryKeySelective(openAIKey);
    }
}
