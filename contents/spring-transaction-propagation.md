# Transaction Propagation

스프링 트랜잭션을 다시 공부하며 영한님의 [스프링 DB 2편 - 데이터 접근 활용 기술](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2) 강의 내용을 요약해 보았습니다.

# 트랜잭션 전파

Spring Transaction Propagation Use transaction twice

![출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2](https://github.com/jihunparkme/jihunparkme.github.io/blob/master/post_img/spring/spring-transaction-connection.png?raw=true)<br/>[출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2]

로그를 보면 트랜잭션1, 2가 같은 conn0 커넥션을 사용 중인데, 이것은 커넥션 풀 때문
- 트랜잭션1은 conn0을 모두 사용 후 커넥션 풀에 반납하고, 이후 트랜잭션2가 conn0을 커넥션 풀에서 획득

히카리(HikariCP) 커넥션 풀에서 커넥션을 획득하면 실제 커넥션을 그대로 반환하는 것이 아니라 내부 관리를 위해
`히카리 프록시 커넥션 객체`를 생성해서 반환
  - 이 객체의 주소를 확인하면 커넥션 풀에서 획득한 커넥션 구분이 가능
  - `HikariProxyConnection@2120431435 wrapping conn0: ...`
  - `HikariProxyConnection@1567077043 wrapping conn0: ...`
  - 트랜잭션2에서 커넥션이 재사용되었지만, 각각 커넥션 풀에서 커넥션을 조회

# 물리/논리 트랜잭션

![출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2](https://github.com/jihunparkme/jihunparkme.github.io/blob/master/post_img/spring/spring-transaction-propagation.png?raw=true)<br/>[출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2]

트랜잭션이 진행 중인 상태에서 **내부에 추가로 트랜잭션을 사용**하는 경우, 스프링은 **외부/내부 트랜잭션을 묶어서 하나의 트랜잭션**을 생성
- 트랜잭션 전파의 기본 옵션인 `REQUIRED` 기준 → 내부 트랜잭션은 외부 트랜잭션에 참여(외/내부 트랜잭션이 하나의 물리 트랜잭션으로 묶임)
- 옵션을 통해 다른 동작방식 선택 가능

**논리 트랜잭션**들은 하나의 **물리 트랜잭션**으로 묶임
- `물리 트랜잭션`: 실제 데이터베이스에 적용되는 트랜잭션(시작, 커밋, 롤백 단위)
- `논리 트랜잭션`: 트랜잭션 매니저를 통해 트랜잭션을 사용하는 단위
- 모든 트랜잭션 매니저가 커밋되어야 **물리 트랜잭션이 커밋**
- 하나의 트랜잭션 매니저라도 롤백되면 **물리 트랜잭션은 롤백**
- 외부 트랜잭션(처음 시작 트랜잭션)만 실제 물리 트랜잭션 관리(begin/commit)

**원칙**

- 모든 `논리 트랜잭션`이 커밋되어야 `물리 트랜잭션`이 커밋
- 하나의 `논리 트랜잭션`이라도 롤백되면 `물리 트랜잭션`은 롤백

# 흐름

## 요청 흐름

![출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2](https://github.com/jihunparkme/jihunparkme.github.io/blob/master/post_img/spring/spring-transaction-request.png?raw=true)<br/>[출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2]

**외부 트랜잭션**

1. txManager.getTransaction() 호출로 **외부 트랜잭션 시작**
2. 트랜잭션 매니저는 데이터소스를 통해 **커넥션 생성**
3. 생성 커넥션을 **수동 커밋 모드**로 설정(물리 트랜잭션 시작)
4. 트랜잭션 매니저는 트랜잭션 동기화 매니저에 **커넥션을 보관**
5. 트랜잭션 매니저는 트랜잭션을 생성한 결과를 `TransactionStatus`에 담아서 반환
   - `isNewTransaction=true`으로 신규 트랜잭션 여부 확인
6. 로직1이 실행되고 커넥션이 필요한 경우 트랜잭션 동기화 매니저를 통해 **트랜잭션이 적용된 커넥션 획득 후 사용**

**내부 트랜잭션**

7. txManager.getTransaction() 호출로 **내부 트랜잭션 시작**
8. 트랜잭션 매니저는 트랜잭션 동기화 매니저를 통해 **기존 트랜잭션 존재 확인**
9. 기존 트랜잭션이 존재하므로 **기존 트랜잭션에 참여**
   - 물리적으로 아무 행동을 하지 않고, 트랜잭션 동기화 매니저에 **보관된 기존 커넥션 사용**
10. `isNewTransaction = false`
11. 로직2가 실행되고, 커넥션이 필요한 경우 트랜잭션 동기화 매니저를 통해 외부 트랜잭션이 보관한 **기존 커넥션 획득 후 사용**

## 응답 흐름

![출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2](https://github.com/jihunparkme/jihunparkme.github.io/blob/master/post_img/spring/spring-transaction-response.png?raw=true)<br/>[출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2]

**내부 트랜잭션**

12. 로직2가 끝나고 트랜잭션 매니저를 통해 **내부 트랜잭션 커밋**
13. 트랜잭션 매니저는 커밋 시점에 신규 트랜잭션 여부에 따라 다르게 동작
    - 여기서는 신규 트랜잭션이 아니므로 아직 물리 트랜잭션이 끝나지 않았고, **실제 물리 커밋 호출을 하지 않음**

**외부 트랜잭션**

14.  로직1이 끝나고 트랜잭션 매니저를 통해 **외부 트랜잭션 커밋**
15.  트랜잭션 매니저는 커밋 시점에 신규 트랜잭션 여부에 따라 다르게 동작
     - 외부 트랜잭션은 신규 트랜잭션이므로 **데이터베이스 커넥션에 실제 커밋 호출**
16. 실제 데이터베이스에 커밋이 반영되고, 물리 트랜잭션도 끝.
    - `논리적인 커밋`: 트랜잭션 매니저에 커밋
    - `물리 커밋`: 실제 커넥션에 커밋

## 흐름 핵심

트랜잭션 매니저에 커밋을 한다고 항상 실제 커넥션에 물리 커밋이 발생하지 않음
- **신규 트랜잭션**인 경우에만 실제 커넥션을 사용해서 물리 커밋/롤백 수행

## REQUIRED 트랜잭션 롤백

**외부 롤백**

![출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2](https://github.com/jihunparkme/jihunparkme.github.io/blob/master/post_img/spring/spring-transaction-outer-rollback.png?raw=true)<br/>[출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2]

외부 트랜잭션에서 시작한 물리 트랜잭션의 범위가 내부 트랜잭션까지 사용
- 이후 외부 트랜잭션이 롤백되면서 전체 내용은 모두 롤백

**내부 롤백**

![출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2](https://github.com/jihunparkme/jihunparkme.github.io/blob/master/post_img/spring/spring-transaction-inner-rollback.png?raw=true)<br/>[출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2]

내부 트랜잭션을 롤백하면 실제 물리 트랜잭션이 롤백되지는 않고, 기존 트랜잭션에 롤백
전용 마크 표시
    
```java
Participating transaction failed - marking existing transaction as rollbackonly
```
    
이후 외부 트랜잭션이 커밋을 호출했지만, 전체 트랜잭션이 롤백 전용으로 표시되어 물리 트랜잭션 롤백
- `UnexpectedRollbackException`
    
```java
Global transaction is marked as rollback-only
```
    

## REQUIRES_NEW 트랜잭션 롤백

`REQUIRES_NEW` 옵션 사용 시
- 외부/내부 트랜잭션이 각각 별도의 물리 트랜잭션(별도의 DB connection 사용)을 가짐
- 각 트랜잭션의 롤백이 서로에게 영향을 주지 않음

**요청 흐름**

![출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2](https://github.com/jihunparkme/jihunparkme.github.io/blob/master/post_img/spring/spring-transaction-requires-new-request.png?raw=true)<br/>[출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2]

**응답 흐름**

![출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2](https://github.com/jihunparkme/jihunparkme.github.io/blob/master/post_img/spring/spring-transaction-requires-new-response.png?raw=true)<br/>[출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2]

# 전파 옵션

실무에서 대부분 `REQUIRED` 옵션을 사용하고, 아주 가끔 `REQUIRES_NEW` 사용

**REQUIRED**

- 가장 많이 사용하는 기본 설정(트랜잭션 필수)
- 기존 트랜잭션 X: 새로운 트랜잭션 생성
- 기존 트랜잭션 O: 기존 트랜잭션에 참여

**REQUIRES_NEW**

- 항상 새로운 트랜잭션 생성
- 기존 트랜잭션 X: 새로운 트랜잭션 생성
- 기존 트랜잭션 O: 새로운 트랜잭션 생성

.

나머지는 거의 사용하지 않으니 참고만..

**SUPPORT**

- 트랜잭션 지원
- 기존 트랜잭션 X: 트랜잭션 없이 진행
- 기존 트랜잭션 O: 기존 트랜잭션 참여

**NOT_SUPPORT**

- 트랜잭션 지원을 하지 않음
- 기존 트랜잭션 X: 트랜잭션 없이 진행
- 기존 트랜잭션 O: 트랜잭션 없이 진행(기존 트랜잭션은 보류)

**MANDATORY**

- 트랜잭션이 반드시 있어야 함
- 기존 트랜잭션 X: IllegalTransactionStateException 예외 발생
- 기존 트랜잭션 O: 기존 트랜잭션 참여

**NEVER**

- 트랜잭션을 사용하지 않음
- 기존 트랜잭션 X: 트랜잭션 없이 진행
- 기존 트랜잭션 O: IllegalTransactionStateException 예외 발생

**NESTED**

- 기존 트랜잭션 X: 새로운 트랜잭션 생성
- 기존 트랜잭션 O: 중첩 트랜잭션 생성
- 중첩 트랜잭션은 외부 트랜잭션의 영향을 받지만, 중첩 트랜잭션은 외부에 영향을 주지 않음

.

`isolation`, `timeout`, `readOnly` 는 트랜잭션 처음 시작 시에만 적용(참여하는
경우에는 적용되지 않음)

# 활용

⭐️ 복구

**`REQUIRED`와 `UnexpectedRollbackException`**

![출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2](https://github.com/jihunparkme/jihunparkme.github.io/blob/master/post_img/spring/spring-transaction-requires-recover-fail.png?raw=true)<br/>[출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2]

- LogRepository 에서 예외 발생
- 예외를 토스하면 LogRepository 의 트랜잭션 AOP가 해당 예외 전달받음
- 신규 트랜잭션이 아니므로 물리 트랜잭션을 롤백하지 않고, 트랜잭션 동기화 매니저에 rollbackOnly=true 표시
- 이후 트랜잭션 AOP는 전달 받은 예외를 밖으로 토스
- 예외가 MemberService 에 토스되고, MemberService 는 해당 예외 복구 및 정상 리턴
- 정상 흐름이 되었으므로 MemberService 의 트랜잭션 AOP는 커밋 호출
- 커밋 호출 시 신규 트랜잭션이므로 실제 물리 트랜잭션 커밋. 이때!! rollbackOnly 체크
- **rollbackOnly=true 상태이므로 물리 트랜잭션 롤백**
- 트랜잭션 매니저는 UnexpectedRollbackException 예외 토스
- 트랜잭션 AOP도 전달 받은 UnexpectedRollbackException 을 클라이언트에게 토스

.

**`REQUIRES_NEW`**

![출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2](https://github.com/jihunparkme/jihunparkme.github.io/blob/master/post_img/spring/spring-transaction-requires-recover-success.png?raw=true)<br/>
[출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2]

- LogRepository 에서 예외 발생
- 예외를 토스하면 LogRepository 의 트랜잭션 AOP가 해당 예외 전달받음
- REQUIRES_NEW 를 사용한 신규 트랜잭션이므로 물리 트랜잭션 롤백 및 반환(rollbackOnly 표시 X)
- 이후 트랜잭션 AOP는 전달 받은 예외를 밖으로 토스

- 예외가 MemberService 에 토스되고, MemberService 는 해당 예외 복구 및 정상 리턴
- 정상 흐름이 되었으므로 MemberService 의 트랜잭션 AOP는 커밋 호출
- 커밋 호출 시 신규 트랜잭션이므로 실제 물리 트랜잭션 커밋. rollbackOnly가 체크되어 있지 않으므로 물리 트랜잭션 커밋 및 정상 흐름 반환

.

`REQUIRED` 적용 시 논리 트랜잭션이 하나라도 롤백되면 관련 물리 트랜잭션 모두 롤백

- `REQUIRES_NEW`를 통한 트랜잭션 분리로 해결
    - 단, 하나의 HTTP 요청에 2개의 데이터베이스 커넥션을 사용하는 단점 존재
- 성능이 중요하다면 트랜잭션을 순차적으로 사용하는 구조를 선택하는 것이 유리