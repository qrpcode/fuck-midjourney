package com.example.midjourney.mapper;

import com.example.midjourney.bean.pojo.OpenAIKey;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface OpenAIKeyMapper extends Mapper<OpenAIKey> {

    @Select("SELECT id, open_ai_key AS openAiKey, status FROM open_ai_key WHERE status = 0 LIMIT 1")
    OpenAIKey selectOnceNotUse();


}
