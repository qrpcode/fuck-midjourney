package com.example.midjourney.bean.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.NameStyle;
import tk.mybatis.mapper.code.Style;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NameStyle(Style.camelhump)
@Table(name = "room_info")
public class RoomInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer discordId;

    /**
     * https://discord.com/channels/【这里】/1091697672269865033
     */
    private String discordGuildId;

    /**
     * https://discord.com/channels/1091697672269865030/【这里】
     */
    private String discordChannelId;

    private Integer nowNumber;
}
