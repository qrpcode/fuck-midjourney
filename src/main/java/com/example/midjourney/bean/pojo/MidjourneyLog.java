package com.example.midjourney.bean.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.NameStyle;
import tk.mybatis.mapper.code.Style;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@NameStyle(Style.camelhump)
@Table(name = "midjourney_log")
public class MidjourneyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer memberId;

    private String channel;

    private String prompt;

    private Integer type;

    private Integer status;

    private String imgFile;

    private Integer roomId;

    private String uuid;

    private Date createTime;

}
