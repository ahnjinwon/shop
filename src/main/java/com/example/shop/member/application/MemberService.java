package com.example.shop.member.application;

import com.example.shop.common.ResponseEntity;
import com.example.shop.member.application.dto.MemberCommand;
import com.example.shop.member.application.dto.MemberInfo;
import com.example.shop.member.domain.Member;
import com.example.shop.member.domain.MemberRepository;
import com.example.shop.member.infrastructure.MemberJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;

    public ResponseEntity<List<MemberInfo>> findAllMembers(Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        List<MemberInfo> members = page.stream().map(MemberInfo::from).toList();
        long count = members.size();
        return new ResponseEntity<>(HttpStatus.OK.value(), members, count);
    }

    public ResponseEntity<MemberInfo> createMember(MemberCommand command) {
        Member member = Member.create(
                command.email(),
                command.name(),
                command.password(),
                command.phone(),
                command.saltKey(),
                command.flag()
        );
        Member member1 = memberRepository.save(member);
        int cnt = 0;
        if(member1 instanceof List){
            cnt = ((List<?>) member1).size();
        }else{
            cnt=1;
        }
        return new ResponseEntity<>(HttpStatus.OK.value(), MemberInfo.from(member1), cnt);
    }

    public ResponseEntity<MemberInfo> updateMember(MemberCommand command, String id) {
        Member member = memberRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.updateInformation(
                command.email(),
                command.name(),
                command.password(),
                command.phone(),
                command.saltKey(),
                command.flag()
                );

        Member member1 = memberRepository.save(member);
        int cnt = 0;
        if(member1 instanceof List){
            cnt = ((List<?>) member1).size();
        }else{
            cnt=1;
        }
        return new ResponseEntity<>(HttpStatus.OK.value(), MemberInfo.from(member1), cnt);
    }

    public ResponseEntity<String> deleteMember(String id) {
        memberRepository.deleteById(UUID.fromString(id));
        int cnt = 1;
        return new ResponseEntity<>(HttpStatus.OK.value(), id, cnt);
    }
}
