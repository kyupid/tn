
# 테스트 코드 작성이 필요한 이유

테스트 코드 작성해야한다, TDD 해야한다, 말은 많이 들었다.
인프런의 김영한님도 말씀하시더라. 테스트코드 없이 개발하는 것은 불가능하다고.

그 말을 처음 들었을 땐 별 감흥이 없었는데 이번에 자바/스프링으로 신규 프로젝트를 진행하며 온몸으로 테스트 코드 작성의 중요성을 깨달았다.

이 글에서는 내가 테스트가 번거로웠던 많은 로직들 중에 한가지를 샘플링하여 가져와 그 중요성을 설명할 것이다.

테스트 코드를 작성하지 않는다면 어떤식으로 개발해야할까?
아직 완성되지 않은 서비스를 테스트 할때 여러 변수들에 임의의 값을 넣어주고, 종속된 다른 클래스에도 임의의 값을 넣어줘야하고, Repository에도 임의의 객체를 넣어줘야한다.
때에 따라선 이보다 훨씬 부가적인 작업이 많을 것이다.
이를 수동으로 처리하면 어떻게 될까?

그 어려움을 직접 느껴보자.

설명을 위해 다음과 같은 회원가입 로직이 있다고 생각해보자.

> 1. 회원가입 버튼을 클릭한다.
> 2. 본인인증을 위한 휴대폰 인증 모듈 페이지가 호출된다.
> 3. 본인인증을 마치면 암호화된 사용자 정보를 얻는다.
> 4. 복호화하여 CI값(간단히 사용자 구별을위한 unique key 값)으로 기가입된 회원인지 체크한다.
> 5. 기가입된 회원/미가입된 회원 구분에 따른 후처리(View)

그러나 현재 [휴대폰 인증 모듈](https://www.niceid.co.kr/resources/images/prod/img_mobi_slider1-1.jpg) 사용이 불가한 상황이라고 가정하자.

코드를 보자.

```java
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final HttpSession session;

    @PostMapping("/api/phoneAuth")
    public String divergingPointForExistingMember(@RequestBody AuthMember authMember, Model model) {
        String email = memberService.getMemberEmailIfPresent(authMember);

        if (email != null) {
            // 이미 존재하는 회원 페이지에서 사용자의 아이디를 알려주기 위한 모델
            model.addAttribute("email", email);
            return "/이미_존재하는_회원_페이지";
        }

        // 회원가입 페이지로 넘기는 인증된 데이터 세션에 저장, 나중에 회원가입할때 사용
        session.setAttribute("authMember", authMember);
        return "/회원가입_페이지";
    }
}
```
```java
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public String getMemberEmailIfPresent(AuthMember authMember) {

        // 휴대폰인증 테스트 불가로 encryptedInfo 가 임의의 값으로 들어가야함
        String encryptedInfo = authMember.getEncryptedInfo();

        // 임의의 값이 들어가기 때문에
        // 인증 모듈 회사의 알고리즘으로 이루어진 DecryptModuleExample 도 사용할 수 없는 상태
        final String CI = DecryptModuleExample.decrypt(encryptedInfo).getCi();
        String email = memberRepository.findEmail(CI)
                .orElse(null);

        // ExistingMember 의 값을 체크해서 그 값을 "이미 존재하는 회원 페이지" 로 넘겨주거나 "회원가입 페이지" 로 넘김
        return email;
    }
}
```
```java
@Getter // 휴대폰 인증으로부터 받은 데이터
public class AuthMember {
    // 부가적인 데이터들
    // ...
    // ...

    // 필요한 정보
    String encryptedInfo;
}
```
```java
// 휴대폰 인증 모듈을 사용할 수 없기 때문에
// 이 모듈을 사용할 수 없는 상태
public class DecryptModuleExample {

    public static DecryptedInfo decrypt(String encryptedInfo) {

        // 복호화 로직
        // ...
        // 복호화 로직에 의해 생성된 객체
        DecryptedInfo decryptedInfo = new DecryptedInfo();
        
        return decryptedInfo;
    }
}

```
```java
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DecryptedInfo {

    // 아래 속성들은 사용자가 휴대폰 인증 후 받은 encryptedInfo 의 복호화한 정보들의 예시
    private String name;
    private String birth;
    private String phoneNumber;
    private String gender;
    // ...
    // ...

    // 우리가 사용해야하는 정보
    private String ci;
}
```

현재 이 로직을 검증하고 싶지만 여러 장애물이 있다.

1. 휴대폰 인증 모듈을 사용할 수 없는 상태이다.
2. 설령 모듈을 사용한다고 해도 매번 테스트할 때마다 휴대폰 인증을 할 수는 없는 노릇이다.
3. 모듈을 사용할 수 없기 때문에 사용자 인증 정보를 받을 수 없다.
4. 즉, `AuthMember` 를 받을 수 없다.
5. 때문에, `AuthMember` 에 **임의의 값**을 넣어야한다.
6. **임의의 값**을 넣기 때문에 사용자 정보를 복호화할 모듈 또한 사용할 수 없다.
7. 임의로 해당 복호화 코드를 뺄 수는 없기 때문에 **리턴값(CI)을 임의로** 지정해줘야한다.

여기서 포인트는 **임의의 값**이다.

이것을 테스트 코드없이 수동으로 검증하려면 아래와 같이 작성..하고 싶은 사람은 아무도 않겠지만, 아래와 같이 작성할 수 있다.

```java
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final HttpSession session;

    @PostMapping("/api/phoneAuth")
    public String divergingPointForExistingMember(@RequestBody AuthMember authMember, Model model) {
        // 테스트를 위해 postman 같은 곳에서 authMember 를 임의값으로 지정하여 호출

        // .. 기타 코드 생략 ..
    }
}
```
```java
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public String getEmailIfPresent(AuthMember authMember) {

        // 앞에서 임의의 값을 받음
        String encryptedInfo = authMember.getEncryptedInfo();

        // 임의의 값이 들어가서 인증 모듈 회사의 알고리즘으로 이루어진 DecryptModuleExample 도 사용할 수 없는 상태
        // 때문에, 임의의 값을 리턴하기 위해 해당 모듈을 직접 수정해야하는 상황 발생
        // 때문에, 어쩔수 없이 아래 코드를 지우는 수 밖에 없음
        final String CI = DecryptModuleExample.decrypt(encryptedInfo).getCi();

        // 위 코드를 지웠으므로 아래와 같이 임의의 테스트 값을 설정
        final String CI = "TEST_CI";

        // TEST_CI 에 대한 Member 가 존재해야하기 때문에 아래 코드가 추가
        memberRepository.save(new Member(1L, "kyu@github.kr", 23, "TEST_CI"));

        // null 일경우에도 테스트를 해야하기 때문에 그럴 경우는 memberRepository.save() 코드를 주석처리 
        String email = memberRepository.findEmail(CI)
                .orElse(null);

        return email;
    }
}
```

수동으로 테스트하기 위해서 얼마나 많은 위험한 포인트가 있는지 모르겠다.

1. postman 이라는 추가적인 툴을 사용하여 데이터를 넣어주었다.
2. `DecrypteModuleExample.decrypt()` 사용할 수 없어서 테스트를 위해 지울 수 밖에 없었다.
3. `final String CI = "TEST_CI"` 테스트용 코드가 프로덕션에 들어갔다.
4. `memberRepository.save()` 테스트용 코드가 프로덕션에 들어갔다.
5. null 일경우도 테스트해야하기 때문에 `memberRepository.save()` 를 지우고 만들고를 반복하며 테스트해야한다.

1번은 그렇다치자. 그런데 2번부터 5번은 모두 프로덕션에 있어야할 코드들이 아니거나 실제 프로덕션 코드가 지워졌다.
변경지점이 말도 안되게 많이 생겼다.

실제 해당 메소드를 테스트 할 수 있는 환경이 주어진다면, 이 변경지점들을 다시 원복시켜야 할것이다.
원복시킬때 종속되어 있는 위 코드들을 찾아서 지워줘야하고 그 과정에서 실수가 발생하기 마련일 것이다.
이런 불필요하고 위험한 오버헤드가 있어야할까?
해당 로직도 그리 복잡한게 아닌데 이보다 더 처리해야할 게 많다면 테스트 코드를 따로 작성하는 것은 필수가 될 수 밖에 없다.

프로덕션 코드에 변경을 없애기 위해 테스트 코드를 작성해보자.

```java
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

        // when
        // 여기서 임의의로 넣어줬던 모든 값들을 처리해준다.
        when(authMember.getEncryptedInfo()).thenReturn("");
        when(DecryptModuleExample.decrypt(anyString())).thenReturn(decryptedInfo);
        when(decryptedInfo.getCi()).thenReturn("");
        when(memberRepository.findEmail(anyString())).thenReturn(Optional.of(email));

        // given
        // 서비스 호출해준다.
        String resultEmail = memberService.getEmailIfPresent(authMember);

        // then
        // verify 해준다.
        verify(memberRepository, times(1)).findEmail(anyString());
        assertThat(resultEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("CI가 가지고 있는 멤버가 없는 경우")
    void verifyMemberWithoutCI() {

        // 위 테스트 메소드와 이하동문 

        // when
        when(authMember.getEncryptedInfo()).thenReturn("");
        when(DecryptModuleExample.decrypt(anyString())).thenReturn(decryptedInfo);
        when(decryptedInfo.getCi()).thenReturn("");
        when(memberRepository.findEmail(anyString())).thenReturn(Optional.empty());

        // given
        String email = memberService.getEmailIfPresent(authMember);

        // then
        verify(memberRepository, times(1)).findEmail(anyString());
        assertThat(email).isEqualTo(null);
    }
}
```

테스트 코드를 작성하니 위에서 수동 테스트를 위해서 더하거나 뺐던 모든 코드들이 불필요해진다. 뿐만아니라 이로인해 얻는 이점이 정말 많아졌다.

변경점이 생기면 이때까지 테스트 했던 기능에 어떤 영향을 끼칠지 모른다. 바로 코드에 대한 자신감 하락으로 이어지고 찝찝한 기분을 떨쳐낼 수 없다.

하지만 테스트 코드를 작성하면 코드에 대한 자신감이 향상되고 기능 변경에 바로 대처할수있다. 변경하고 전체 테스트를 돌려서 통과여부만 체크하면 되기때문이다.

실제로 코드에 변경이 일어났다고 가정해보자.

```java
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public String getMemberEmailIfPresent(AuthMember authMember) {

        // 휴대폰인증 테스트 불가로 encryptedInfo 가 임의의 값으로 들어가야함
        String encryptedInfo = authMember.getEncryptedInfo();

        // 임의의 값이 들어가기 때문에
        // 인증 모듈 회사의 알고리즘으로 이루어진 DecryptModuleExample 도 사용할 수 없는 상태
        final String CI = DecryptModuleExample.decrypt(encryptedInfo).getCi();
        String email = memberRepository.findEmail(CI)
                .orElse(null);

        // ExistingMember 의 값을 체크해서 그 값을 "이미 존재하는 회원 페이지" 로 넘겨주거나 "회원가입 페이지" 로 넘김
        return email;
    }
}
```

원래의 코드를 예를 들어서 Repository에서 이메일이 아니라 Member를 꺼내는 방향으로 변경했다고 생각해보자.

```java
    // ...생략
    // String email = memberRepository.findEmail(CI).orElse(null);
    Member member = memberRepository.findByCI(CI).orElse(Member.emptyMember());
return member;
}
```

해당 서비스 코드에서는 해당 부분과 연관된 컨트롤러만 변경하면 되지만
예를 들어, 다른 본인인증 이후의 회원가입 페이지에서는 해당 member 값으로 뷰를 처리하는데 개발자가 이것을 알아차리지 못했다면 회원가입 페이지 코드를 건들이는 날까지 모르고 넘어가는 것이다.

이런 크고 작은 것들이 쌓인다면 프로그램은 버그덩어리가 될것이다. 테스트 코드를 작성하며 이를 사전에 예방할 수 있는것이다.

테스트코드를 작성하며 들인 시간과 수고가 오히려 프로그램 품질을 올려주고 결과적으로 시간을 절약해준다.

또한 이러한 장점은 테스트 코드가 곧 기능들에 대한 문서역할도 할수있다. 아무리 메소드나 변수 네이밍을 잘해도 해당 기능에 대한 역할을 한번에 파악하기란 쉽지않다. 왜냐면 이름으로부터 단편적으로 기능에 대한 의미를 추론할 수 있을 뿐이지 구체적으로 어떤 역할을 하는지 파악하려면 결국 코드를 봐야하기 때문이다.

```java
public String getMemberEmailIfPresent(AuthMember authMember)
```
```java
@DisplayName("CI를 가지고있는 멤버가 있는 경우 테스트")
void test1(){}

@DisplayName("CI를 가지고있는 멤버가 없는 경우 테스트")
void test2(){}
```

또한 이렇게 테스트 코드를 작성해주며 여러 조건을 주며 의도하는 테스트를 하기 때문에 그에 따라 의도하는 기능에 대해 명명해줘야하기 때문에 메소드명만으로는 되지 않던 게 테스트코드 작성한것이 문서가 될 수 있는것이다.
