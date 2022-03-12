package com.yeonwook.tn.repository;

import com.yeonwook.tn.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<String> findEmail(String ci);
}
