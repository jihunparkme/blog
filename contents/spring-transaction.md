# Spring Transaction

스프링 트랜잭션을 다시 공부하며 영한님의 [스프링 DB 2편 - 데이터 접근 활용 기술](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2) 강의 내용을 요약해 보았습니다.

# 추상화

`org.springframework.transaction.PlatformTransactionManager` 인터페이스를 통해 트랜잭션 추상화

```java
package org.springframework.transaction;

public interface PlatformTransactionManager extends TransactionManager {
    
    TransactionStatus getTransaction(@Nullable TransactionDefinition definition) throws TransactionException;

    void commit(TransactionStatus status) throws TransactionException;

    void rollback(TransactionStatus status) throws TransactionException;
}
```

**Spring.**

Spring 은 Transaction을 추상화해서 제공하고, 데이터 접근 기술에 대한 `TransactionManager`의 구현체도 제공
- 데이터 접근 기술마다 모두 다른 트랜잭션 처리 방식을 추상화
- 사용자는 필요한 구현체를 Spring Bean 으로 등록하고, 주입받아서 사용

**Spring Boot.**

Spring Boot 는 어떤 데이터 접근 기술을 사용하는지를 **자동으로 인식**해서 적절한 TransactionManager **선택 및 스프링 빈으로 등록**
- JdbcTemplate, MyBatis 사용 시 `DataSourceTransactionManager(JdbcTransactionManager)`를 스프링 빈으로 등록
- JPA 사용 시 `JpaTransactionManager`을 스프링 빈으로 등록

참고. [**Interface TransactionManager**](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/TransactionManager.html)

- All Known Subinterfaces:
  - CallbackPreferringPlatformTransactionManager
  - ConfigurableTransactionManager
  - PlatformTransactionManager
  - ReactiveTransactionManager
  - ResourceTransactionManager

- All Known Implementing Classes:
  - AbstractPlatformTransactionManager
  - AbstractReactiveTransactionManager
  - DataSourceTransactionManager
  - HibernateTransactionManager
  - JdbcTransactionManager
  - JmsTransactionManager
  - JpaTransactionManager
  - JtaTransactionManager
  - R2dbcTransactionManager

.

# 사용 방식

`선언적 트랜잭션 관리`(Declarative Transaction Management) vs. `프로그래밍 방식 트랜잭션 관리`(programmatic transaction management)

## 선언적 트랜잭션 관리

`@Transactional` 하나만 선언하여 편리하게 트랜잭션을 적용 (과거에는 XML에 설정)
- org.springframework.transaction.annotation.Transactional
- 이름 그대로 "해당 로직에 트랜잭션을 적용하겠다."라고 선언하면 트랜잭션이 적용되는 방식
- 기본적으로 프록시 방식의 `AOP` 적용
- 트랜잭션을 처리하는 객체와 비즈니스 로직을 처리하는 서비스 객체를 명확하게 분리
- 트랜잭션은 커넥션에 `setAutocommit(false)` 지정으로 시작
- 같은 데이터베이스 커넥션을 사용하여 같은 트랜잭션을 유지하기 위해 스프링 내부에서는 `트랜잭션 동기화 매니저`를 사용
  - JdbcTemplate을 포함한 대부분의 데이터 접근 기술들은 트랜잭션을 유지하기 위해 내부에서 트랜잭션 동기화 매니저를 통해 리소스(커넥션)를 동기화

  ![출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2](https://github.com/jihunparkme/jihunparkme.github.io/blob/master/post_img/spring/transaction-aop.png?raw=true)
  [출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2]

## 프로그래밍 방식의 트랜잭션 관리

[TransactionManager](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/TransactionManager.html) 또는 [TransactionTemplate](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/support/TransactionTemplate.html) 등을 사용해서 트랜잭션 관련 코드를 직접 작성 
- 프로그래밍 방식의 트랜잭션 관리를 사용하게 되면 **애플리케이션 코드가 트랜잭션 기술 코드와 강하게 결합**되는 단점
- 선언적 트랜잭션 관리가 훨씬 간편하고 실용적이기 때문에 **실무에서는 대부분 선언적 트랜잭션 관리를 사용**

.

# 적용

AOP 적용 방식에 따라서 인터페이스에 `@Transactional` 선언 시 AOP가 적용이 되지 않는 경우도 있으므로, **가급적 구체 클래스에 사용 권장**

**Transaction 적용 확인**

```java
package org.springframework.transaction.support;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Boolean> currentTransactionReadOnly = new NamedThreadLocal("Current transaction read-only status");
    private static final ThreadLocal<Boolean> actualTransactionActive = new NamedThreadLocal("Actual transaction active");

    ...

    public static boolean isCurrentTransactionReadOnly() {
        return currentTransactionReadOnly.get() != null;
    }

    public static boolean isActualTransactionActive() {
        return actualTransactionActive.get() != null;
    }
}
```

.

**트랜잭션 프록시가 호출하는 트랜잭션 로그 확인을 위한 설정**

```bash
logging.level.org.springframework.transaction.interceptor=TRACE

...

Getting transaction for [hello.springtx.apply...BasicService.tx]

# 실제 메서드 호출..

# 트랜젝션 로직 커밋 또는 롤백..

Completing transaction for [hello.springtx.apply...BasicService.tx]
```

.

@Transactional 이 특정 **클래스**나 **메서드**에 있다면, Transaction AOP는 프록시를 만들어서 스프링 컨테이너에 등록
- 실제 객체 대신 `프록시를 스프링 빈에 등록`하고 `프록시는 내부에 실제 객체를 참조`
- 프록시는 객체를 상속해서 만들어지기 때문에 다형성을 활용
- 클래스에 트랜잭션 적용 시 메서드는 자동 적용 (스프링에서 우선순위는 항상 더 구체적이고 자세한 것이 높은 우선순위)

![출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2](https://github.com/jihunparkme/jihunparkme.github.io/blob/8abd275c39be2090caf854ac3c82066fe8470b9d/post_img/spring/spring-container-proxy.png?raw=true)
[출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2]

.

# 주의사항

@Transactional 선언 시 `Spring Transaction AOP` 적용
- Transaction AOP 는 기본적으로 `프록시 방식의 AOP` 사용
- 스프링은 대상 객체 대신 프록시를 스프링 빈으로 등록하므로 **프록시 객체가 요청을 먼저 받고, 프록시 객체에서 트랜잭션 처리와 실제 객체 호출**
- 따라서, 트랜잭션을 적용하려면 **항상 프록시를 통해서 대상 객체를 호출**

> ⭐️ 
> 
> 만일 프록시를 거치지 않고 대상 객체를 직접 호출하게 되면 AOP가 적용되지 않고, 트랜잭션도 적용되지 않는다.
> 
> 대상 객체의 내부에서 메서드 호출이 발생하면 프록시를 거치지 않고 대상 객체를 직접 호출하는 문제가 발생

.

**프록시를 호출하는 경우**

```java
@Transactional
public void internal() {
    log.info("call internal");
}

```

![출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2](https://github.com/jihunparkme/jihunparkme.github.io/blob/master/post_img/spring/spring-transaction-internal.png?raw=true)
[출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2]

1. 클라이언트가 service.internal() 을 호출하면 service 의 `트랜잭션 프록시` 호출
2. internal() 메서드에 @Transactional 이 선언되어 있으므로 트랜잭션 프록시는 `트랜잭션을 적용`
3. 트랜잭션 적용 후 실제 service 객체 인스턴스의 `internal() 호출`
4. 실제 service 가 처리 완료되면 응답이 트랜잭션 프록시로 돌아오고, 트랜잭션 프록시는 트랜잭션을 완료

.

## 대상 객체를 직접 호출하는 경우

```java
public void external() {
    log.info("call external");
    internal();
}

@Transactional
public void internal() {
    log.info("call internal");
}

```

![출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2](https://github.com/jihunparkme/jihunparkme.github.io/blob/master/post_img/spring/spring-transaction-external.png?raw=true)
[출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2]

1. 클라이언트가 service.external() 을 호출하면 service 의 트랜잭션 프록시 호출
2. external() 메서드에는 @Transactional 이 없으므로 트랜잭션 프록시는 트랜잭션을 적용하지 않고, 실제 service 객체 인스턴스의 external() 호출
3. external() 은 내부에서 (this)internal() 직접 호출
4. 내부 호출은 프록시를 거치지 않으므로 트랜잭션 적용이 불가능

> ⭐️
> 
> @Transactional 을 사용하는 트랜잭션 AOP는 프록시를 사용하면서 메서드 내부 호출에 프록시를 적용할 수 없다.
> 
> 이를 해결할 수 있는 가장 단순한 방법으로 내부 호출을 외부 호출로 변경하기 위해 internal() 를 별도 클래스로 분리할 수 있다.

.

**대상 객체를 외부에서 호출하도록 수정한 경우**

![출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2](https://github.com/jihunparkme/jihunparkme.github.io/blob/master/post_img/spring/spring-transaction-external-class.png?raw=true)
[출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2]

1. 클라이언트가 service.external() 을 호출하면 실제 service 객체 인스턴스 호출
2. service 는 주입받은 internalService.internal() 호출
3. internalService 는 트랜잭션 프록시이므로 트랜잭션 적용
4. 트랜잭션 적용 후 실제 internalService 객체 인스턴스의 internal() 호출

> 참고.
> 
> 
> 스프링 트랜잭션 AOP 기능은 과도한 트랜잭션 적용을 막기 위해 `public 메서드`에만 트랜잭션이 적용되도록 기본 설정
> 
> public 이 아닌 곳에 @Transactional 이 붙으면 트랜잭션 적용을 무시한다.

.

## 초기화 시점

초기화 코드(ex.@PostConstruct)와 @Transactional 을 함께 사용하면 트랜잭션 적용 불가
- 초기화 코드가 먼저 호출되고 이후 트랜잭션 AOP가 적용되기 때문
  ```java
   @PostConstruct
   @Transactional
   public void initV1() {
       boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
       log.info("Hello init @PostConstruct tx active={}", isActive); // false
   }
   ```

대안으로 `@ApplicationReadyEvent` 사용
- [ApplicationReadyEvent](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/context/event/ApplicationReadyEvent.html) 는 트랜잭션 AOP를 포함한 스프링 컨테이너가 완전히 생성된 이후 이벤트가 선언된 메서드 호출

  ```java
  @EventListener(value = ApplicationReadyEvent.class)
  @Transactional
  public void init2() {
      boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
      log.info("Hello init ApplicationReadyEvent tx active={}", isActive); // true
  }
  ```

# 옵션

[Annotation Interface Transactional](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/annotation/Transactional.html)

```java
@AliasFor("transactionManager")
String value() default "";

/**
 * @Transactional 에서 트랜잭션 프록시가 사용할 트랜잭션 매니저 지정
 * 생략 시 기본으로 등록된 트랜잭션 매니저 사용
 * 사용 트랜잭션 매니저가 둘 이상이라면, 트랜잭션 매니저 이름을 지정해서 구분
 * 
 * @Transactional("memberTxManager")
 * @Transactional("orderTxManager")
 */
@AliasFor("value")
String transactionManager() default "";

/**
 * 트랜잭션 애노테이션에 있는 값을 읽어서 특정 동작을 할 경우 사용
 * 일반적으로 사용하지 않음
 */
String[] label() default {};

/**
 * 트랜잭션 전파 옵션
 */
Propagation propagation() default Propagation.REQUIRED;

/**
 * 트랜잭션 격리 수준 지정
 * 본값은 데이터베이스 설정 기준(DEFAULT)
 * 트랜잭션 격리 수준을 직접 지정하는 경우는 드물다.
 * - DEFAULT : 데이터베이스에서 설정한 격리 수준을 따른다.
 * - READ_UNCOMMITTED : 커밋되지 않은 읽기
 * - READ_COMMITTED : 커밋된 읽기
 * - REPEATABLE_READ : 반복 가능한 읽기
 * - SERIALIZABLE : 직렬화 가능
 */
Isolation isolation() default Isolation.DEFAULT;

/**
 * 트랜잭션 수행 시간에 대한 타임아웃을 초 단위로 지정
 * 기본 값은 트랜잭션 시스템의 타임아웃
 */
int timeout() default -1;

String timeoutString() default "";

/**
 * readOnly=true 옵션 사용 시 읽기 전용 트랜잭션 생성
 * - 드라이버나 데이터베이스에 따라 정상 동작하지 않는 경우도 있음.
 * 
 * 읽기에서 다양한 성능 최적화
 * 
 * 크게 세 곳에서 적용
 * (1). 프레임워크
 * - JdbcTemplate: 읽기 전용 트랜잭션 안에서 변경 기능을 실행하면 예외
 * - JPA: 읽기 전용 트랜잭션의 경우 커밋 시점에 플러시를 호출하지 않고, 변경이 불필요하니 변경 감지를 위한 스냅샷 객체도 생성하지 않음
 * 
 * (2). JDBC 드라이버
 * - 읽기 전용 트랜잭션에서 변경 쿼리가 발생하면 예외
 * - 읽기, 쓰기(master, slave) 데이터베이스를 구분해서 요청
 * - DB / 드라이버 버전에 따라 다르게 동작
 * 
 * (3). 데이터베이스
 * - 읽기 전용 트랜잭션의 경우 읽기만 하면 되므로, 내부에서 성능 최적화 발생
 */
boolean readOnly() default false;

/**
 * 특정 예외 발생 시 롤백을 하도록 지정
 * Exception(체크 예외)이 발생해도 롤백하도록 설정 가능
 * 
 * @Transactional(rollbackFor = Exception.class)
 */
Class<? extends Throwable>[] rollbackFor() default {};

String[] rollbackForClassName() default {};

/**
 * rollbackFor 와 반대로 특정 예외 발생 시 롤백을 하지 않도록 지정
 */
Class<? extends Throwable>[] noRollbackFor() default {};

String[] noRollbackForClassName() default {};
```

# 예외와 롤백

내부에서 예외를 처리하지 못하고 트랜잭션 범위 밖으로 예외를 던질 경우, 스프링 트랜잭션 AOP는 예외 종류에 따라 트랜잭션을 커밋하거나 롤백
- 언체크 예외(RuntimeException, Error, 그 하위 예외) 발생 시 트랜잭션 `롤백`
- 체크 예외(Exception, 그 하위 예외) 발생 시 트랜잭션 `커밋`
- 정상 응답(리턴) 시 트랜잭션을 `커밋`

.

**참고. 트랜잭션 커밋/롤백 로그 확인을 위한 설정**
- 트랜잭션 생성 로그(Creating new transaction with name)와 트랜잭션 커밋/롤백 로그(Committing JPA transaction on EntityManager) 확인

```
# 사용중인 TransactionManager
logging.level.org.springframework.jdbc.datasource.DataSourceTransactionManager=DEBUG
logging.level.org.springframework.orm.jpa.JpaTransactionManager=DEBUG #JPA log
logging.level.org.hibernate.resource.transaction=DEBUG
```

스프링은 기본적으로 예외를 아래 정책에 따름
- **Checked Exception / Commit** : 비즈니스 예외 (ex. 잔고 부족 ..)
    - 비즈니스 예외는 반드시 처리해야 하는 경우가 많으므로 중요하고, 체크 예외를 고려할 수 있음.
    - rollbackFor 옵션을 사용해서 비즈니스 상황에 따라 롤백 선택 가능
- **Unchecked Exception / Rollback** : 복구 불가능한 예외 (ex. DB 접근 오류, SQL 문법 오류 ..)