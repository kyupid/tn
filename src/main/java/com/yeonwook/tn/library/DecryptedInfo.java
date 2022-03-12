package com.yeonwook.tn.library;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DecryptedInfo {

    // 아래 속성들은 복호화 이후 받는 정보들의 예시입니다
    private String name;
    private String birth;
    private String phoneNumber;
    private String gender;
    // ...
    // ...

    // 우리가 사용할 정보
    private String ci;
}
