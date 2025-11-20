package com.example.shop.member.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository {
    Page<Member> findAll(Pageable pageable);
    Optional<Member> findById(UUID id);
    Member save(Member member);
    void deleteById(UUID id);
}
