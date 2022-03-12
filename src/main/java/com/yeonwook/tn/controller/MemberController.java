package com.yeonwook.tn.controller;

import com.yeonwook.tn.entity.Member;
import com.yeonwook.tn.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping
    public Member index() {
        System.out.println("MemberController.index");
        return memberRepository.getById(1L);
    }
}
