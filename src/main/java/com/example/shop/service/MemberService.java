package com.example.shop.service;

import com.example.shop.common.ResponseEntity;
import com.example.shop.member.Member;
import com.example.shop.member.MemberRepository;
import com.example.shop.member.MemberRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;

    public ResponseEntity<List<Member>> findAllMembers() {
        List<Member> members = memberRepository.findAll();
        long count = members.size();
        return new ResponseEntity<>(HttpStatus.OK.value(), members, count);
    }

    public ResponseEntity<Member> createMember(MemberRequest request) {
        Member member = new Member(
                UUID.randomUUID(),
                request.email(),
                request.name(),
                request.password(),
                request.phone(),
                request.saltKey(),
                request.flag()
        );
        Member member1 = memberRepository.save(member);
        int cnt = 0;
        if(member1 instanceof List){
            cnt = ((List<?>) member1).size();
        }else{
            cnt=1;
        }
        return new ResponseEntity<>(HttpStatus.OK.value(), member1, cnt);
    }

    public ResponseEntity<Member> updateMember(MemberRequest request, String id) {
        Member member = memberRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.setEmail(request.email());
        member.setName(request.name());
        member.setPassword(request.password());
        member.setPhone(request.phone());
        member.setSaltKey(request.saltKey());
        member.setFlag(request.flag());

        Member member1 = memberRepository.save(member);
        int cnt = 0;
        if(member1 instanceof List){
            cnt = ((List<?>) member1).size();
        }else{
            cnt=1;
        }
        return new ResponseEntity<>(HttpStatus.OK.value(), member1, cnt);
    }

    public ResponseEntity<String> deleteMember(String id) {
        memberRepository.deleteById(UUID.fromString(id));
        int cnt = 1;
        return new ResponseEntity<>(HttpStatus.OK.value(), id, cnt);
    }
}
