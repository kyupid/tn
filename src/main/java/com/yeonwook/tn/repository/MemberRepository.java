package com.yeonwook.tn.repository;

import com.yeonwook.tn.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
