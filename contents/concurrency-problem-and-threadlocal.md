# 동시성 문제와 스레드 로컬(ThreadLocal)

동시성 문제는
- 다수의 쓰레드가 동시에 같은 인스턴스 필드 값을 변경하면서 발생하는 문제
- 스프링 빈처럼 싱글톤 객체의 필드를 변경하며 사용할 때 주의

## Before

```java
@Slf4j
public class FieldService {

    private String nameStore;

    public String logic(String name) {
        log.info("저장 name={} -> nameStore={}", name, nameStore);
        nameStore = name;

        sleep(1000);

        log.info("조회 nameStore={}", nameStore);
        return nameStore;
    }
}
```

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


```console
[Test worker] INFO ...FieldServiceTest - main start
[thread-A] INFO ...code.FieldService - 저장 name=김치볶음밥 -> nameStore=null
[thread-B] INFO ...code.FieldService - 저장 name=짜장면 -> nameStore=김치볶음밥
[thread-A] INFO ...code.FieldService - 조회 nameStore=짜장면
[thread-B] INFO ...code.FieldService - 조회 nameStore=짜장면
[Test worker] INFO ...FieldServiceTest - main exit
```



## After

- **특정 스레드만 접근**할 수 있는 특별한 저장소
- 각 스레드마다 별도의 내부 저장소 제공
- 특정 스레드가 스레드 로컬을 모두 사용면 ThreadLocal.remove() 호출로 저장된 값 제거 필요
  - 메모리 누수 방지

[ThreadLocal](https://docs.oracle.com/javase/8/docs/api/java/lang/ThreadLocal.html)


```java
@Slf4j
public class ThreadLocalService {

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

        sleep(2000);

        log.info("threadLocal main exit");
    }
}

```

```console
[Test worker] INFO ...ThreadLocalServiceTest - threadLocal main start
[thread-A] INFO ...code.ThreadLocalService - 저장 name=김치볶음밥 -> nameStore=null
[thread-B] INFO ...code.ThreadLocalService - 저장 name=짜장면 -> nameStore=null
[thread-A] INFO ...code.ThreadLocalService - 조회 nameStore=김치볶음밥
[thread-B] INFO ...code.ThreadLocalService - 조회 nameStore=짜장면
[Test worker] INFO ...ThreadLocalServiceTest - threadLocal main exit
```