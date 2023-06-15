package com.example.midjourney.util;

import org.apache.logging.log4j.util.Strings;

public class TextUtil {

    public static String cleanString(String str) {
        return str.replace("\"", "'")
                .replace("\"", "[")
                .replace("\"", "]")
                .replace("\"", "{")
                .replace("\"", "}");
    }

    public static boolean isHaveChinese(String chinese) {
        return Strings.isNotEmpty(getChinese(chinese));
    }


    public static String getChinese(String source) {
        return source.replaceAll("\\s*","")
                .replaceAll("[^(\u4e00-\u9fa5)]","");
    }

}
