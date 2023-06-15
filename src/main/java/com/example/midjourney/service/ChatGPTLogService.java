package com.example.midjourney.service;

import com.example.midjourney.bean.pojo.ChatGPTLog;

public interface ChatGPTLogService {

    ChatGPTLog findById(Long id);

    void updateFail(ChatGPTLog chatGPTLog);

    void updateSensitive(ChatGPTLog chatGPTLog);

    void updateFinish(ChatGPTLog chatGPTLog);

}
