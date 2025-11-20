package com.example.shop.member.application.dto;

import com.example.shop.member.domain.Member;

import java.time.LocalDateTime;
import java.util.UUID;

public record MemberInfo(
        UUID id,
        String email,
        String name,
        String phone,
        String flag,
        LocalDateTime regDt,
        LocalDateTime modifyDt
){
    public static MemberInfo from(Member member){
        return new MemberInfo(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhone(),
                member.getFlag(),
                member.getRegDt(),
                member.getModifyDt()
        );
    }
}
