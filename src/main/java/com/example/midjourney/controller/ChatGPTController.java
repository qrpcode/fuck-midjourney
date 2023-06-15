package com.example.midjourney.controller;

import com.example.midjourney.biz.ChatGPTBiz;
import com.example.midjourney.contant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * ChatGPT
 * 接口不要包含chatGPT否则内网穿透平台可能封禁
 */
@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/capi")
public class ChatGPTController {

    @Resource
    private ChatGPTBiz chatGPTBiz;

    @GetMapping("/wx")
    public String chatGPT(@RequestParam("id") Long id) {
        log.info("[ChatGPT] id: {}", id);
        Constant.threadPool.execute(() -> chatGPTBiz.sendChatGPTMsg(id));
        return "ok";
    }

}
