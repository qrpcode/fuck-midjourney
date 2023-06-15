package com.example.midjourney.service.impl;

import com.example.midjourney.bean.pojo.RoomInfo;
import com.example.midjourney.mapper.RoomInfoMapper;
import com.example.midjourney.service.RoomInfoService;
import com.example.midjourney.util.Safes;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RoomInfoServiceImpl implements RoomInfoService {

    @Resource
    private RoomInfoMapper roomInfoMapper;

    @Override
    public RoomInfo findIdleRoom() {
        return Safes.of(roomInfoMapper.selectAll()).stream()
                .filter(room -> room.getNowNumber() < 45)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addOnce(RoomInfo roomInfo) {
        RoomInfo info = roomInfoMapper.selectByPrimaryKey(roomInfo.getId());
        info.setNowNumber(info.getNowNumber() + 1);
        roomInfoMapper.updateByPrimaryKeySelective(info);
    }

    @Override
    public RoomInfo findById(Integer roomId) {
        return roomInfoMapper.selectByPrimaryKey(roomId);
    }

    @Override
    public List<RoomInfo> findAll() {
        return roomInfoMapper.selectAll();
    }

    @Override
    public void update(RoomInfo roomInfo) {
        roomInfoMapper.updateByPrimaryKeySelective(roomInfo);
    }
}
