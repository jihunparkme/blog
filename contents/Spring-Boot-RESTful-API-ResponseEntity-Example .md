# [Spring Boot] RESTful API ResponseEntity Example

제대로된 API Response 형태를 전달한 경험이 주로 없다보니..

문득 REST API Response Body 전달 형식이 궁금해졌다. :0

간단한 Response 구조로 테스트해보자.

## Class

`package com.example.responseApi.api.*`

### DTO

- Response 결과로 넘겨줄 객체

```java
@Data
public class Member {

	private Long id;
	private String name;
	private String dept;

	public Member(String name, String dept) {
		this.name = name;
		this.dept = dept;
	}
}

```

### Repository

- Database 연동 대체

```java
@Slf4j
@Repository
public class MemberRepository {

    private static Map<Long, Member> store = new HashMap<>(); //static 사용

    private static long sequence = 0L; //static 사용

    public Member save(Member member) {
        member.setId(++sequence);
        log.info("save: member={}", member);
        store.put(member.getId(), member);
        return member;
    }

    public Optional<Member> findById(Long id) {
    	return Optional.ofNullable(store.get(id));
    }

    public Optional<Member> findByLoginId(String loginId) {
        return findAll().stream()
                .filter(m -> m.getId().equals(loginId))
                .findFirst();
    }

    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    public void clearStore() {
        store.clear();
    }
}
```

### Response

- Response 시 사용할 class

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BasicResponse {

	private Integer code;
	private HttpStatus httpStatus;
	private String message;
	private Integer count;
	private List<Object> result;
}
```

### Controller

- API Controller

```java
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class ApiController {

	private final MemberRepository memberRepository;

	@GetMapping("/{id}")
	public ResponseEntity<BasicResponse> find(@PathVariable Long id) {

		BasicResponse basicResponse = new BasicResponse();
		Optional<Member> member = memberRepository.findById(id);

		if (member.isPresent()) {
			basicResponse = BasicResponse.builder()
										.code(HttpStatus.OK.value())
										.httpStatus(HttpStatus.OK)
										.message("사용자 조회 성공")
										.result(Arrays.asList(member.get()))
										.count(1).build();

		} else {
			basicResponse = BasicResponse.builder()
										.code(HttpStatus.NOT_FOUND.value())
										.httpStatus(HttpStatus.NOT_FOUND)
										.message("사용자를 찾을 수 없습니다.")
										.result(Collections.emptyList())
										.count(0).build();

		}

		return new ResponseEntity<>(basicResponse, basicResponse.getHttpStatus());
	}

	@GetMapping("/all")
	public ResponseEntity<BasicResponse> list() {
		List<Member> memberList = memberRepository.findAll();

		BasicResponse basicResponse = BasicResponse.builder()
												.code(HttpStatus.OK.value())
												.httpStatus(HttpStatus.OK)
												.message("전체 사용자 조회 성공")
												.result(new ArrayList<>(memberList))
												.count(memberList.size()).build();

		return new ResponseEntity<>(basicResponse, HttpStatus.OK);
	}
}
```

### Test Data Init

`package com.example.responseApi;`

- API 테스트를 위한 테스트 데이터 생성

```java
@Component
@RequiredArgsConstructor
public class TestDataInit {

	private final MemberRepository memberRepository;

	@PostConstruct
    public void init() {
		memberRepository.save(new Member("Cristoval", "back-end"));
		memberRepository.save(new Member("Aaron", "front-end"));
    }
}

```

## Request Test

- 사용자를 찾을 수 없을 경우

```http
http://localhost:8080/api/0
```

```json
{
  "code": 404,
  "httpStatus": "NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다.",
  "count": 0,
  "result": []
}
```

- 사용자 조회 성공

```http
http://localhost:8080/api/1
```

```json
{
  "code": 200,
  "httpStatus": "OK",
  "message": "사용자 조회 성공",
  "count": 1,
  "result": [
    {
      "id": 1,
      "name": "Cristoval",
      "dept": "back-end"
    }
  ]
}
```

- 전체 사용자 조회

```http
http://localhost:8080/api/all
```

```json
{
  "code": 200,
  "httpStatus": "OK",
  "message": "전체 사용자 조회 성공",
  "count": 2,
  "result": [
    {
      "id": 1,
      "name": "Cristoval",
      "dept": "back-end"
    },
    {
      "id": 2,
      "name": "Aaron",
      "dept": "front-end"
    }
  ]
}
```

## HTTP 상태코드

## `2xx`

Successful: 요청 정상 처리

**Code**

- 200 OK
- 201 Created (POST)
- 202 Accepted (batch)
- 204 No Content

## `3xx`

Redirection: 요청을 완료를 위해 추가 행동 필요

**Redirect**

- 웹 브라우저는 3xx 응답의 결과에 Location 헤더가 있으면, Location 위치로 자동 이동
- 영구 리다이렉션 : 특정 리소스의 URI가 영구적으로 이동 (301, 308)
- 일시 리다이렉션 : 일시적인 변경 (302, 303, 307)
  - PRG(Post/Redirect/Get)에 사용 / 새로고침 중복 주문 방지
- 특수 리다이렉션 : 결과 대신 캐시 사용

**Code**

- 300 Multiple Choices (X)
- 301 Moved Permanently
  - 리다이렉트 시 <u>Get</u>으로 변하고, 본문 손실
- 302 Found
  - 리다이렉트 시 <u>GET</u>으로 변하고, 본문 제거
- 303 See Other
  - 리다이렉트 시 <u>GET</u>으로 변경
- 304 Not Modified
  - 클라이언트에게 리소스가 수정되지 않았음을 알려줌 (캐시 재사용)
- 307 Temporary Redirect
  - 리다이렉트 시 메서드와 본문 유지
- 308 Permanent Redirect
  - 리다이렌트 시 <u>POST</u>, 본문 유지

## `4xx`

Client Error

- 오류의 원인은 클라이언트

**Code**

- 400 Bad Request
  - 클라이언트가 잘못된 요청을 해서 서버가 요청을 처리할 수 없음
- 401 Unauthorized
  - 클라이언트가 해당 리소스에 대한 인증이 필요
  - 인증(Authentication): 로그인
  - 인가(Authorization): 권한
- 403 Forbidden
  - 서버가 요청을 이해했지만 승인을 거부 (접근 권한 제한)
- 404 Not Found
  - 요청 리소스를 찾을 수 없음

## `5xx`

Server Error

- 서버 문제로 오류 발생

**Code**

- 500 Internal Server Error
  - 서버 내부 문제로 오류 발생 (애매하면 500)
- 503 Service Unavailable
  - 서비스 이용 불가

## Project

[PROJECT](https://github.com/jihunparkme/blog/tree/main/projects/responseApi)

## Reference

- [RESTful API 설계 가이드](https://sanghaklee.tistory.com/57)
- [REST API Response Body 형식에 대한 경험적 구조](http://blog.storyg.co/rest-api-response-body-best-pratics)
