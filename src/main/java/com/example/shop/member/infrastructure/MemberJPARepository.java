package com.example.shop.member.infrastructure;

import com.example.shop.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MemberJPARepository extends JpaRepository<Member, UUID> {
}
