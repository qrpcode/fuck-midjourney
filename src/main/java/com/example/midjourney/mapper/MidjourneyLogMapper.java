package com.example.midjourney.mapper;

import com.example.midjourney.bean.pojo.MidjourneyLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface MidjourneyLogMapper extends Mapper<MidjourneyLog> {

    String COLUMN = " id, member_id AS memberId, prompt, channel, type, " +
            "status, img_file AS imgFile, room_id AS roomId ";

    @Select("SELECT " + COLUMN + " FROM midjourney_log WHERE member_id = #{memberId} AND type = 0 ORDER BY id DESC LIMIT 1")
    MidjourneyLog findLastNormalLog(@Param("memberId") Integer memberId);

    @Select("SELECT " + COLUMN + " FROM midjourney_log WHERE member_id = #{memberId} AND (type = 0 OR type = 2)" +
            " ORDER BY id DESC LIMIT 1")
    MidjourneyLog findLastNormalOrIterative(Integer memberId);

    @Select("SELECT COUNT(*) AS count FROM midjourney_log WHERE room_id = #{id} AND status = 0")
    Integer selectRoomOnUse(@Param("id") Integer id);

}
