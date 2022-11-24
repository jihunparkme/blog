# 동시성 문제와 스레드 로컬

## 동시성 문제

- 다수의 스레드가 동시에 같은 인스턴스 필드 값을 변경하면서 발생하는 문제
- 스프링 빈처럼 싱글톤 객체의 필드를 변경하며 사용할 때 주의

Sample Service

```java
@Slf4j
public class FieldService {

    private String nameStore; // 싱글톤 객체의 공용 필드

    public String logic(String name) {
        log.info("저장 name={} -> nameStore={}", name, nameStore);
        nameStore = name;

        sleep(1000);

        log.info("조회 nameStore={}", nameStore);
        return nameStore;
    }
}
```

Test

```java
@Slf4j
public class FieldServiceTest {

    private FieldService fieldService = new FieldService();

    @Test
    void field() {
        log.info("main start");

        Runnable userA = () -> {
            fieldService.logic("김치볶음밥");
        };
        Runnable userB = () -> {
            fieldService.logic("짜장면");
        };

        Thread threadA = new Thread(userA);
        threadA.setName("thread-A");
        Thread threadB = new Thread(userB);
        threadB.setName("thread-B");

        threadA.start();

         sleep(100); 

        threadB.start();

        sleep(3000);
        log.info("main exit");
    }
}
```

thread-A 로직의 끝나기 전에 thread-B 로직이 싱글톤 객체의 공용 필드를 수정하면서 thread-A의 필드 값을 덮어쓰게 되었다.
- 다수의 스레드가 동시에 같은 인스턴스/공용 필드 값을 변경하면서 발생하는 이슈가 바로 이러한 상황이다.
- thread-A는 김치볶음밥이지만 짜장면이 되어 버렸다.

```console
[Test worker] INFO ...FieldServiceTest - main start
[thread-A] INFO ...code.FieldService - 저장 name=김치볶음밥 -> nameStore=null
[thread-B] INFO ...code.FieldService - 저장 name=짜장면 -> nameStore=김치볶음밥
[thread-A] INFO ...code.FieldService - 조회 nameStore=짜장면
[thread-B] INFO ...code.FieldService - 조회 nameStore=짜장면
[Test worker] INFO ...FieldServiceTest - main exit
```

## ThreadLocal

- 동시성 문제를 해결할 수 있는 방법 중 하나
- **특정 스레드만 접근**할 수 있는 **특별한 저장소**
- 각 스레드마다 **별도의 내부 저장소 제공**
- 특정 스레드 로컬을 모두 사용면 메모리 누수 방지를 위해 `ThreadLocal.remove()` 호출로 저장된 값 제거

[Class ThreadLocal](https://docs.oracle.com/javase/8/docs/api/java/lang/ThreadLocal.html)


Sample Service

```java
@Slf4j
public class ThreadLocalService {

    // 싱글톤 객체의 공용 필드 대신 스레드 로컬 사용
    private ThreadLocal<String> nameStore = new ThreadLocal<>();

    public String logic(String name) {
        log.info("저장 name={} -> nameStore={}", name, nameStore.get());
        nameStore.set(name);

        sleep(1000);

        log.info("조회 nameStore={}", nameStore.get());
        return nameStore.get();
    }
}
```

Test

```java
@Slf4j
public class ThreadLocalServiceTest {

    private ThreadLocalService service = new ThreadLocalService();

    @Test
    void threadLocal() {
        log.info("threadLocal main start");

        Runnable userA = () -> {
            service.logic("김치볶음밥");
        };
        Runnable userB = () -> {
            service.logic("짜장면");
        };

        Thread threadA = new Thread(userA);
        threadA.setName("thread-A");
        Thread threadB = new Thread(userB);
        threadB.setName("thread-B");

        threadA.start();

        sleep(100);

        threadB.start();

        sleep(3000);
        log.info("threadLocal main exit");
    }
}
```

스레드 로컬을 통해 동시성 문제가 해결되었다.

```console
[Test worker] INFO ...ThreadLocalServiceTest - threadLocal main start
[thread-A] INFO ...code.ThreadLocalService - 저장 name=김치볶음밥 -> nameStore=null
[thread-B] INFO ...code.ThreadLocalService - 저장 name=짜장면 -> nameStore=null
[thread-A] INFO ...code.ThreadLocalService - 조회 nameStore=김치볶음밥
[thread-B] INFO ...code.ThreadLocalService - 조회 nameStore=짜장면
[Test worker] INFO ...ThreadLocalServiceTest - threadLocal main exit
```

## Reference

영한님의 [스프링 핵심 원리 - 고급편](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B3%A0%EA%B8%89%ED%8E%B8/dashboard) 강의 내용 中