package com.example.shop.member.infrastructure;

import com.example.shop.member.domain.Member;
import com.example.shop.member.domain.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class MemberRepositoryAdapter implements MemberRepository {

    private final MemberJPARepository memberJPARepository;

    public MemberRepositoryAdapter(MemberJPARepository memberJPARepository) {
        this.memberJPARepository = memberJPARepository;
    }

    @Override
    public Page<Member> findAll(Pageable pageable) {
        return memberJPARepository.findAll(pageable);
    }

    @Override
    public Optional<Member> findById(UUID id) {
        return memberJPARepository.findById(id);
    }

    @Override
    public Member save(Member member) {
        return memberJPARepository.save(member);
    }

    @Override
    public void deleteById(UUID id) {
        memberJPARepository.deleteById(id);
    }
}
