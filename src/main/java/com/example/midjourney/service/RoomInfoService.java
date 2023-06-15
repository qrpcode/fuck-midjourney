package com.example.midjourney.service;

import com.example.midjourney.bean.pojo.RoomInfo;

import java.util.List;

public interface RoomInfoService {

    RoomInfo findIdleRoom();

    void addOnce(RoomInfo roomInfo);

    RoomInfo findById(Integer roomId);

    List<RoomInfo> findAll();

    void update(RoomInfo roomInfo);

}
