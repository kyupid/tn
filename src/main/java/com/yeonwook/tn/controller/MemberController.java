package com.yeonwook.tn.controller;

import com.yeonwook.tn.dto.AuthMember;
import com.yeonwook.tn.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final HttpSession session;

    @PostMapping("/api/phoneAuth")
    public String divergingPointForExistingMember(@RequestBody AuthMember authMember, Model model) {
        String email = memberService.getEmailIfPresent(authMember);

        if (email != null) {
            model.addAttribute("email", email);
            return "/이미_존재하는_회원.html";
        }

        session.setAttribute("authMember", authMember);
        return "/회원가입_폼.html";
    }
}
