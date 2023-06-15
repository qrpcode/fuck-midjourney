package com.example.midjourney.service;

import com.example.midjourney.bean.pojo.MidjourneyLog;
import com.example.midjourney.bean.pojo.RoomInfo;

import java.util.List;

public interface MidjourneyLogService {

    MidjourneyLog findById(Long id);

    MidjourneyLog findLastNormalLog(Integer memberId);

    MidjourneyLog findLastNormalOrIterative(Integer memberId);

    void updateRoom(MidjourneyLog midjourneyLog, RoomInfo roomInfo);

    List<MidjourneyLog> selectAllDoing();

    void updateFail(MidjourneyLog midjourneyLog);

    void updateSensitive(MidjourneyLog midjourneyLog);

    void updatePrompt(MidjourneyLog midjourneyLog, String prompt);

    void updateFinish(MidjourneyLog midjourey);

    int selectRoomOnUse(Integer id);

}
