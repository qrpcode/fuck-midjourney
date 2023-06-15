package com.example.midjourney.contant;

import com.example.midjourney.bean.UserInfo;
import com.example.midjourney.biz.WeChatBiz;
import com.example.midjourney.util.BaiduUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Constant {

    public static ExecutorService threadPool = Executors.newFixedThreadPool(20);

    public static Map<String, UserInfo> imgWordUserMap = Maps.newConcurrentMap();

    public static String MID_JOURNEY_HEAD = "**";

    public static String MID_FIND_LEFT = "** - <";

    public static String FILE_PATH = "D:\\000img\\";

    public static String CHATGPT = "chatgpt";
    public static String MIDJOURNEY = "midjourney";

    public static final String INSUFFICIENT = "insufficient_quota";

    public static Map<String, String> WX_TOKEN_CHANNEL = Maps.newHashMap();

    public static Map<String, String> WX_TOKEN_MAP = Maps.newHashMap();

    public static String BAIDU_TOKEN = BaiduUtil.queryBaiduToken();

    public static Map<String, String> CHANNEL_CALL_BACK = Maps.newHashMap();

    /**
     * 最大等待时间15分钟
     */
    public static long MAX_WAIT_TIME = 1000 * 60 * 15;

    public static List<String> MidjourneyBlackWord = Lists.newArrayList("violence", "nipples", "nipple_slip",
            "nipple slip", "erect_nipples", "areola", "breast_grab", "breast grab", "breast_hold", "breast hold",
            "paizuri", "bukkake", "thigh_sex", "thigh sex", "footjob", "foot job", "masturbation", "handjob", "fingering",
            "cunnilingus", "pussy_juice", "pussy juice", "oshiri", "butt","ass_grab",  "ass grab", "buttjob",
            "butt job", "anal", "enema", "bestiality", "censored", "uncensored", "pussy", "vulva", "penis", "dildo",
            "sex", "fellatio", "bathing", "blood", "bondage", "extreme_content", "extreme content", "guro", "futanari",
            "gangbang", "lactation", "pubic hair", "pubic_hair", "pubic hair", "tentacles", "vibrator", "cameltoe",
            "***", "***", "trump", "arrested");

    static {
        WX_TOKEN_CHANNEL.put("mbxzl", "https://***/mbapi/queryToken.php?key=**");
        WX_TOKEN_CHANNEL.put("wlmp", "https://***/api/queryToken.php?key=***");

        CHANNEL_CALL_BACK.put("mbxzl", "https://***/mbapi/callback.php");
        CHANNEL_CALL_BACK.put("wlmp", "https://***/api/callback.php");

        WeChatBiz.queryToken();
    }


}
