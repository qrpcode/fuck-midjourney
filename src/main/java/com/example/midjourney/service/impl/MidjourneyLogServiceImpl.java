package com.example.midjourney.service.impl;

import com.example.midjourney.bean.pojo.MidjourneyLog;
import com.example.midjourney.bean.pojo.RoomInfo;
import com.example.midjourney.mapper.MidjourneyLogMapper;
import com.example.midjourney.service.MidjourneyLogService;
import com.example.midjourney.util.Safes;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MidjourneyLogServiceImpl implements MidjourneyLogService {

    @Resource
    private MidjourneyLogMapper midjourneyLogMapper;

    @Override
    public MidjourneyLog findById(Long id) {
        return midjourneyLogMapper.selectByPrimaryKey(id);
    }

    @Override
    public MidjourneyLog findLastNormalLog(Integer memberId) {
        return midjourneyLogMapper.findLastNormalLog(memberId);
    }

    @Override
    public MidjourneyLog findLastNormalOrIterative(Integer memberId) {
        return midjourneyLogMapper.findLastNormalOrIterative(memberId);
    }

    @Override
    public void updateRoom(MidjourneyLog midjourneyLog, RoomInfo roomInfo) {
        midjourneyLog.setRoomId(roomInfo.getId());
        midjourneyLogMapper.updateByPrimaryKeySelective(midjourneyLog);
    }

    @Override
    public List<MidjourneyLog> selectAllDoing() {
        MidjourneyLog midjourneyLog = new MidjourneyLog();
        midjourneyLog.setStatus(0);
        return Safes.of(midjourneyLogMapper.select(midjourneyLog));
    }

    @Override
    public void updateFail(MidjourneyLog midjourneyLog) {
        midjourneyLog.setStatus(500);
        midjourneyLogMapper.updateByPrimaryKeySelective(midjourneyLog);
    }

    @Override
    public void updateSensitive(MidjourneyLog midjourneyLog) {
        midjourneyLog.setStatus(-1);
        midjourneyLogMapper.updateByPrimaryKeySelective(midjourneyLog);
    }

    @Override
    public void updatePrompt(MidjourneyLog midjourneyLog, String prompt) {
        midjourneyLog.setPrompt(prompt);
        midjourneyLogMapper.updateByPrimaryKeySelective(midjourneyLog);
    }

    @Override
    public void updateFinish(MidjourneyLog midjourey) {
        midjourey.setStatus(1);
        midjourneyLogMapper.updateByPrimaryKeySelective(midjourey);
    }

    @Override
    public int selectRoomOnUse(Integer id) {
        return midjourneyLogMapper.selectRoomOnUse(id);
    }
}
