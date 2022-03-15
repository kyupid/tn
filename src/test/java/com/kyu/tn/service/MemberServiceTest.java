package com.kyu.tn.service;

import com.kyu.tn.dto.AuthMember;
import com.kyu.tn.library.DecryptModuleExample;
import com.kyu.tn.library.DecryptedInfo;
import com.kyu.tn.repository.MemberRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthMember authMember;

    @Mock
    private DecryptedInfo decryptedInfo;

    @InjectMocks
    private MemberService memberService;

    private static MockedStatic<DecryptModuleExample> mModuleExample;

    @BeforeAll
    public static void before() {
        mModuleExample = mockStatic(DecryptModuleExample.class);
    }

    @AfterAll
    public static void after() {
        mModuleExample.close();
    }

    @Test
    @DisplayName("CI를 가지고있는 멤버가 있는 경우")
    void verifyMemberWithCI() {
        String email = "test@test.com";

        //when
        when(authMember.getEncryptedInfo()).thenReturn("");
        when(DecryptModuleExample.decrypt(anyString())).thenReturn(decryptedInfo);
        when(decryptedInfo.getCi()).thenReturn("");
        when(memberRepository.findEmail(anyString())).thenReturn(Optional.of(email));

        //given
        String resultEmail = memberService.getEmailIfPresent(authMember);

        //then
        verify(memberRepository, times(1)).findEmail(anyString());
        assertThat(resultEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("CI가 가지고 있는 멤버가 없는 경우")
    void verifyMemberWithoutCI() {
        //when
        when(authMember.getEncryptedInfo()).thenReturn("");
        when(DecryptModuleExample.decrypt(anyString())).thenReturn(decryptedInfo);
        when(decryptedInfo.getCi()).thenReturn("");
        when(memberRepository.findEmail(anyString())).thenReturn(Optional.empty());

        //given
        String email = memberService.getEmailIfPresent(authMember);

        //then
        verify(memberRepository, times(1)).findEmail(anyString());
        assertThat(email).isEqualTo(null);
    }
}
