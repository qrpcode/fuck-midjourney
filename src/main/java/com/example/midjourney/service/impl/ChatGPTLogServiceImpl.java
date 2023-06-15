package com.example.midjourney.service.impl;

import com.example.midjourney.bean.pojo.ChatGPTLog;
import com.example.midjourney.mapper.ChatGPTLogMapper;
import com.example.midjourney.service.ChatGPTLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ChatGPTLogServiceImpl implements ChatGPTLogService {

    @Resource
    private ChatGPTLogMapper chatGPTLogMapper;

    @Override
    public ChatGPTLog findById(Long id) {
        return chatGPTLogMapper.selectByPrimaryKey(id);
    }

    @Override
    public void updateFail(ChatGPTLog chatGPTLog) {
        chatGPTLog.setStatus(500);
        chatGPTLogMapper.updateByPrimaryKeySelective(chatGPTLog);
    }

    @Override
    public void updateSensitive(ChatGPTLog chatGPTLog) {
        chatGPTLog.setStatus(-1);
        chatGPTLogMapper.updateByPrimaryKeySelective(chatGPTLog);
    }

    @Override
    public void updateFinish(ChatGPTLog chatGPTLog) {
        chatGPTLog.setStatus(1);
        chatGPTLogMapper.updateByPrimaryKeySelective(chatGPTLog);
    }

}
