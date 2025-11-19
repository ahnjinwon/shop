package com.example.shop.controller;

import com.example.shop.member.Member;
import com.example.shop.member.MemberRepository;
import com.example.shop.member.MemberRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.v1}/members")
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController {
    @Autowired
    private MemberRepository memberRepository;

    @GetMapping
    @Operation(summary = "회원 목록 조회", description = "전체 회원 목록을 조회합니다.")
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @PostMapping
    @Operation(summary = "회원 생성", description = "요청된 정보로 새로운 회원을 생성합니다.")
    public Member create(@RequestBody MemberRequest request){
        Member member = new Member(
                UUID.randomUUID(),
                request.email(),
                request.name(),
                request.password(),
                request.phone(),
                request.saltKey(),
                request.flag()
        );
        return memberRepository.save(member);
    }

    @PutMapping("{id}")
    @Operation(summary = "회원 정보 수정", description = "기존 회원의 정보를 수정합니다.")
    public Member update(@RequestBody MemberRequest request, @PathVariable String id){
        Member member = new Member(
                id,
                request.email(),
                request.name(),
                request.password(),
                request.phone(),
                request.saltKey(),
                request.flag()
        );
        return memberRepository.save(member);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable String id){
        memberRepository.deleteById(UUID.fromString(id));
    }

}
