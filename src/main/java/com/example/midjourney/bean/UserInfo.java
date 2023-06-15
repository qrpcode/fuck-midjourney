package com.example.midjourney.bean;

import com.example.midjourney.bean.pojo.MidjourneyLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private MidjourneyLog midjourneyLog;

    private long createTime;

}
