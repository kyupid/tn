package com.kyu.tn.repository;

import com.kyu.tn.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<String> findEmail(String ci);
}
