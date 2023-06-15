package com.example.midjourney.controller;

import com.example.midjourney.biz.MidJourneyBiz;
import com.example.midjourney.contant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/mid")
public class MidJourneyController {

    @Resource
    private MidJourneyBiz midJourneyBiz;

    @GetMapping("/wx")
    public String chatGPT(@RequestParam("id") Long id) {

        log.info("[MidJourney] id: {}", id);
        Constant.threadPool.execute(() -> midJourneyBiz.buildImg(id));
        return "ok";
    }

}
