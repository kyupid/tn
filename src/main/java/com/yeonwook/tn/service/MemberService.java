package com.yeonwook.tn.service;

import com.yeonwook.tn.dto.AuthMember;
import com.yeonwook.tn.library.DecryptModuleExample;
import com.yeonwook.tn.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public String getEmailIfPresent(AuthMember authMember) {

        // 휴대폰인증 테스트 불가로 encryptedInfo 가 임의의 값이 들어가야함
        String encryptedInfo = authMember.getEncryptedInfo();

        // 임의의 값이 들어가기 때문에
        // 인증 모듈 회사의 알고리즘으로 이루어진 DecryptModuleExample 도 사용할 수 없는 상태
        final String CI = DecryptModuleExample.decrypt(encryptedInfo).getCi();
        String email = memberRepository.findEmail(CI)
                .orElse(null);

        // ExistingMember 의 값을 체크해서 그 값을 "이미 존재하는 회원 페이지" 로 넘겨주거나 "회원가입 페이지" 로 리다이렉트
        return email;
    }
}
