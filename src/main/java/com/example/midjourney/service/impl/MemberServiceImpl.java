package com.example.midjourney.service.impl;

import com.example.midjourney.bean.pojo.Member;
import com.example.midjourney.mapper.MemberMapper;
import com.example.midjourney.service.MemberService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class MemberServiceImpl implements MemberService {

    @Resource
    private MemberMapper memberMapper;

    @Override
    public String selectWxidById(Integer memberId) {
        Member member = memberMapper.selectByPrimaryKey(memberId);
        return Optional.ofNullable(member).map(Member::getWxid).orElse("");
    }
}
