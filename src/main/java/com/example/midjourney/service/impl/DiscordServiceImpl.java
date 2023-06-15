package com.example.midjourney.service.impl;

import com.example.midjourney.bean.pojo.Discord;
import com.example.midjourney.mapper.DiscordMapper;
import com.example.midjourney.service.DiscordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DiscordServiceImpl implements DiscordService {

    @Resource
    private DiscordMapper discordMapper;

    @Override
    public Discord findById(Integer discordId) {
        return discordMapper.selectByPrimaryKey(discordId);
    }

}
