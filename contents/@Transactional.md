# @Transactional 잘 사용해보기

@Transactional 을 사용하고는 있지만.. 

잘 모르고 저스트 그냥 사용하고 있다 보니 간단하게라도 탐구를 해볼 필요성을 느끼게 되었다.

그래서.. 

오늘도 어김없이 탐정놀이를 해보려고 한다. 🔍🤠🔎

.

시작하기에 앞서,

트랜잭션 구성은 *@Configuration*, *XML* 기반 설정 말고도 애노테이션 기반으로 설정할 수 있다.

주로 애노테이션 기반으로 많이 사용되는 것 같다.

애노테이션 방식은 선언적 트랜잭션이라고 불리고, 선언 시 트랜잭션 기능이 적용된 프록시 객체가 생성된다.

.

`@Transactional`으로 생성된 프록시 객체는 `@Transactional`이 적용된 메소드가 호출될 경우,

`PlatformTransactionManager`를 사용하여 트랜잭션을 시작하고, 정상 여부에 따라 Commit/Rollback 동작을 수행한다. 

.

간단하게, 트랜잭션 처리를 JDK Dynamic Proxy 객체에게 대신 위임하여 AOP로 동작하게 된다.

# Transaction ACID

- **원자성(Atomicity)**
  - 한 트랜잭션 내에서 실행한 작업들은 `하나의 단위`로 처리
  - 모두 성공하거나 모두 실패
- **일관성(Consistency)**
  - 트랜잭션은 `일관성` 있는 데이터베이스 상태를 유지
- **격리성(Isolation)**
  - 동시에 실행되는 트랜잭션들이 `서로 영향을 미치지 않도록` 격리
- **영속성(Durability)**
  - 트랜잭션을 성공적으로 마치면 `결과가 항상 저장`

# Configure Transactions

## Configuration class

- @Configuration 클래스에 @EnableTransactionManagement 선언
- Spring Boot 프로젝트를 사용하고, `spring-data-*` or `spring-tx` dependencies 설정이 되어 있다면 기본적으로 활성화

```java
@Configuration
@EnableTransactionManagement
public class PersistenceJPAConfig{

   @Bean
   public LocalContainerEntityManagerFactoryBean
     entityManagerFactoryBean(){
      //...
   }

   @Bean
   public PlatformTransactionManager transactionManager(){
      JpaTransactionManager transactionManager
        = new JpaTransactionManager();
      transactionManager.setEntityManagerFactory(
        entityManagerFactoryBean().getObject() );
      return transactionManager;
   }
}
```

## XML

- 3.1 이전 버전 또는 Java가 옵션이 아닌 경우

```xml
<bean id="txManager" class="org.springframework.orm.jpa.JpaTransactionManager">
   <property name="entityManagerFactory" ref="myEmf" />
</bean>
<tx:annotation-driven transaction-manager="txManager" />
```

## Annotation

- Annotation으로 트랜잭션을 구성할 경우 추가 구성 지원
  - *Propagation* Type
  - *Isolation* Level
  - Timeout
  - *readOnly* flag
  - *Rollback* rules
- 단, private/protected 메서드는 @Transactional 무시

```java
@Service
@Transactional
public class TransferServiceImpl implements TransferService {
    @Override
    public void transfer(String user1, String user2, double val) {
        // ...
    }
}

@Transactional
public void transfer(String user1, String user2, double val) {
    // ...
}
```

- Spring은 @Transactional이 적용된 모든 클래스/메서드에 대한 프록시 생성

  - 프록시는 프레임워크가 트랜잭션을 시작/커밋하기 위해 실행 중인 메서드의 전후로 트랜잭션 로직을 주입

    ```java
    // @Transactional 메서드 주위에 일부 트랜잭션 관리 코드를 래핑하는 예시
    createTransactionIfNecessary();
    try {
        callMethod();
        commitTransactionAfterReturning();
    } catch (exception) {
        completeTransactionAfterThrowing();
        throw exception;
    }
    ```

> [Transactions with Spring and JPA](https://www.baeldung.com/transaction-configuration-with-jpa-and-spring)
>
> [@Transactional 동작 원리](https://hwannny.tistory.com/98)

# Transaction Options

- **isolation**
  - 트랜잭션에서 일관성없는 데이터 허용 수준을 설정 (`격리 수준`)
- **propagation**
  - 동작 도중 다른 트랜잭션을 호출할 때, 어떻게 할 것인지 지정하는 옵션 (`전파 옵션`)
- **noRollbackFor**
  - 특정 예외 발생 시 rollback이 동작하지 않도록 설정
- **rollbackFor**
  - 특정 예외 발생 시 rollback이 동작하도록 설정
- **timeout**
  - 지정한 시간 내에 메소드 수행이 완료되지 않으면 rollback이 동작하도록 설정
- **readOnly**
  - 트랜잭션을 읽기 전용으로 설정

## isolation

**concurrency side effects on a transaction**

- **Dirty read:** 동시 트랜잭션의 커밋되지 않은 변경 내용을 조회하는 상황 *(데이터 불일치)*
- **Nonrepeatable read**: 동시 트랜잭션이 동일한 행을 업데이트하고 커밋하는 경우, 행을 다시 조회할 때 다른 값을 얻 는 상황
- **Phantom read:** 다른 트랜잭션이 특정 범위의 행을 추가/제거할 경우, 커밋 전/후 조회 결과가 다른 상황

```java
@Transactional(isolation = Isolation.XXX)
public void example(String message) {
    // ...
}
```

- **DEFAULT**
  - DBMS의 기본 격리 수준 적용
- **READ_UNCOMMITED** *(level 0)*
  - 트랜잭션의 동시 액세스 허용
  - 세 가지 동시성 부작용이 모두 발생 (Dirty read, Nonrepeatable read, Phantom read)
  - Postgres는 미지원(대신 READ_COMMITED 로 폴백), Oracle은 지원하거나 허용하지 않음
- **READ_COMMITED** *(level 1)* 
  - Dirty read 방지
  - 나머지 부작용은 여전히 발생할 수 있음 (Nonrepeatable read, Phantom read)
  - Postgres, SQL Server 및 Oracle의 기본 수준
- **REPEATEABLE_READ** *(level 2)* 
  - Dirty read, Nonrepeatable read 방지
  - 업데이트 손실을 방지하기 위해 필요한 가장 낮은 수준 (동시 액세스를 허용하지 않음)
  - Phantom read 부작용은 여전히 발생
  - MySQL의 기본 수준, Oracle은 미지원
- **SERIALIZABLE** *(level 3)*
  - 가장 높은 격리 수준이지만, 동시 호출을 순차적으로 실행하므로 성능 저하의 우려
  - 모든 부작용을 방지

> [Transaction Propagation and Isolation in Spring @Transactional](https://www.baeldung.com/spring-transactional-propagation-isolation)

## **propagation**

```java
@Transactional(propagation = Propagation.XXX)
public void example(String user) { 
    // ... 
}
```

- **REQUIRED** *(default)*
  - 활성 트랜잭션이 있는지 확인하고, 아무것도 없으면 새 트랜잭션을 생성
- **SUPPORTS**
  - 활성 트랜잭션이 있는지 확인하고, 있으면 기존 트랜잭션 사용. 없으면 트랜잭션 없이 실행
- **MANDATORY**
  - 활성 트랜잭션이 있으면 사용하고, 없으면 예외 발생
  - 독립적으로 트랜잭션을 진행하면 안 되는 경우 사용
- **NEVER**
  - 활성 트랜잭션이 있으면 예외 발생
  - 트랜잭션을 사용하지 않도록 제어할 경우
- **NOT_SUPPORTED**
  - 현재 트랜잭션이 존재하면 트랜잭션을 일시 중단한 다음 트랜잭션 없이 비즈니스 로직 실행
- **REQUIRES_NEW**
  - 현재 트랜잭션이 존재하는 경우, 현재 트랜잭션을 일시 중단하고 새 트랜잭션을 생성
- **NESTED**
  -  트랜잭션이 존재하는지 확인하고 존재하는 경우 저장점을 표시
  - 비즈니스 로직 실행에서 예외가 발생하면 트랜잭션이 이 저장 지점으로 롤백
  - 활성 트랜잭션이 없으면 *REQUIRED* 처럼 작동

> [Transaction Propagation and Isolation in Spring @Transactional](https://www.baeldung.com/spring-transactional-propagation-isolation)

## rollbackFor, noRollbackFor

- 선언적 트랜잭션에서는 런타임 예외가 발생하면 롤백 수행
  - 예외가 발생하지 않거나 체크 예외 발생 시 커밋
  - 스프링에서는 데이터 액세스 기술 예외는 런타임 예외로 던져지므로 런타임 예외만 롤백 대상으로 삼음
  - rollbackFor 옵션으로 기본 동작방식 변경 가능

**rollbackFor**

- 특정 예외가 발생 시 강제로 Rollback

```java
@Transactional(rollbackFor=Exception.class)
```

**noRollbackFor**

- 특정 예외의 발생 시 Rollback 제외

```java
 @Transactional(noRollbackFor=Exception.class)
```

## timeout

- 지정한 시간 내에 해당 메소드 수행이 완료되이 않은 경우 rollback 수행 (단위: second)
- -1일 경우 no timeout (default : -1)

```java
@Transactional(timeout=10)
```

## readOnly

- 트랜잭션을 읽기 전용으로 설정
- 성능을 최적화하기 위해 사용하거나, 특정 트랜잭션 작업 안에서 쓰기 작업이 일어나는 것을 의도적으로 방지하기 위해 사용
- readOnly = true인 경우 INSERT, UPDATE, DELETE 작업 진행 시 실행 시 예외 발생(default : false)

```java
@Transactional(readOnly = true)
```

# Reference

[Transactional 정리 및 예제](https://goddaehee.tistory.com/167)

[Using @Transactional](https://docs.spring.io/spring-framework/docs/4.2.x/spring-framework-reference/html/transaction.html#transaction-declarative-annotations)

[응? 이게 왜 롤백되는거지?](https://techblog.woowahan.com/2606/)