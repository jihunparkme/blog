# The Scheduled Annotation in Spring

`@Scheduler`를 사용해서 일정한 시간 간격으로, 혹은 특정 일정에 코드가 실행되도록 해보자.

# Spring Scheduler

## Dependency

Spring Boot starter 에 기본적으로 의존 `org.springframework.scheduling`

## Enable Scheduling

- Project Application Class에 `@EnableScheduling` 추가

```java
@EnableScheduling // 추가
@SpringBootApplication
public class SchedulerApplication {
	public static void main(String[] args) {
		SpringApplication.run(SchedulerApplication.class, args);
	}
}
```

- scheduler를 사용할 Class에 `@Component`, Method에 `@Scheduled` 추가
- `@Scheduled` 규칙

  - Method는 void 타입으로
  - Method는 매개변수 사용 불가

- https://docs.oracle.com/cd/B13866_04/webconf.904/b10877/timezone.htm)

# Example

## fixedDelay

- 해당 메서드가 끝나는 시간 기준, milliseconds 간격으로 실행
- 하나의 인스턴스만 항상 실행되도록 해야 할 상황에서 유용

```java
@Scheduled(fixedDelay = 1000)
// @Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds}") // 문자열 milliseconds 사용 시
public void scheduleFixedDelayTask() throws InterruptedException {
    log.info("Fixed delay task - {}", System.currentTimeMillis() / 1000);
    Thread.sleep(5000);
}
```

## fixedRate

- 해당 메서드가 시작하는 시간 기준, milliseconds 간격으로 실행
- 병렬로 Scheduler 를 사용할 경우, Class에 `@EnableAsync`, Method에 `@Async` 추가
- 모든 실행이 독립적인 경우에 유용

```java
@Async
@Scheduled(fixedRate = 1000)
// @Scheduled(fixedRateString = "${fixedRate.in.milliseconds}")  // 문자열 milliseconds 사용 시
public void scheduleFixedRateTask() throws InterruptedException {
    log.info("Fixed rate task - {}", System.currentTimeMillis() / 1000);
    Thread.sleep(5000);
}
```

## fixedDelay + fixedRate

- `initialDelay` 값 이후 처음 실행 되고, `fixedDelay` 값에 따라 계속 실행

```java
@Scheduled(fixedDelay = 1000, initialDelay = 5000)
public void scheduleFixedRateWithInitialDelayTask() {
    long now = System.currentTimeMillis() / 1000;
    log.info("Fixed rate task with one second initial delay - {}", now);
}
```

## Cron

- 작업 예약으로 실행

```java
@Scheduled(cron = "0 15 10 15 * ?") // 매월 15일 오전 10시 15분에 실행
// @Scheduled(cron = "0 15 10 15 11 ?") // 11월 15일 오전 10시 15분에 실행
// @Scheduled(cron = "${cron.expression}")
// @Scheduled(cron = "0 15 10 15 * ?", zone = "Europe/Paris") // timezone 설정
public void scheduleTaskUsingCronExpression() {
    long now = System.currentTimeMillis() / 1000;
    log.info("schedule tasks using cron jobs - {}", now);
}
```

# Setting information

**fixedDelay**

- 이전 작업이 종료된 후 설정시간(milliseconds) 이후에 다시 시작
- 이전 작업이 완료될 때까지 대기

**fixedDelayString**

- fixedDelay와 동일 하고 설정시간(milliseconds)을 문자로 입력하는 경우 사용

**fixedRate**

- 고정 시간 간격으로 시작
- 이전 작업이 완료될 때까지 다음 작업이 진행되지 않음
- 병렬 동작을 사용할 경우 `@Async` 추가

**fixedRateString**

- fixedRate와 동일 하고 설정시간(milliseconds)을 문자로 입력

**initialDelay**

- 설정된 `initialDelay` 시간 후부터 `fixedDelay` 시간 간격으로 실행

**initialDelayString**

- initialDelay와 동일 하고 설정시간(milliseconds)을 문자로 입력

**cron**

- Cron 표현식을 사용한 작업 예약
- `cron = "* * * * * *"`
- 첫번째 부터 초(0-59) 분(0-59) 시간(0-23) 일(1-31) 월(1-12) 요일(0-7)

**zone**

- `zone = "Asia/Seoul"`
- 미설정시 Local 시간대 사용
- [A Time Zones](https://docs.oracle.com/cd/B13866_04/webconf.904/b10877/timezone.htm)

# Test Code

```java
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component // 추가
@EnableAsync // 추가
public class ScheduleTest {

	/**
	 * 해당 메서드 로직이 끝나는 시간 기준, milliseconds 간격으로 실행
	 * 이전 작업이 완료될 때까지 대기
	 */
	@Scheduled(fixedDelay = 1000)
	public void scheduleFixedDelayTask() throws InterruptedException {
		log.info("Fixed delay task - {}", System.currentTimeMillis() / 1000);
		Thread.sleep(5000);
	}

	/**
	 * 해당 메서드 로직이 시작하는 시간 기준, milliseconds 간격으로 실행
	 * 이전 작업이 완료될 때까지 다음 작업이 진행되지 않음
	 */
	@Async // 병렬로 Scheduler 를 사용할 경우 @Async 추가
	@Scheduled(fixedRate = 1000)
	public void scheduleFixedRateTask() throws InterruptedException {
		log.info("Fixed rate task - {}", System.currentTimeMillis() / 1000);
		Thread.sleep(5000);
	}

	/**
	 * 설정된 initialDelay 시간(milliseconds) 후부터 fixedDelay 시간(milliseconds) 간격으로 실행
	 */
	@Scheduled(fixedDelay = 1000, initialDelay = 5000)
	public void scheduleFixedRateWithInitialDelayTask() {
	    long now = System.currentTimeMillis() / 1000;
	    log.info("Fixed rate task with one second initial delay -{}", now);
	}

	/**
	 * Cron 표현식을 사용한 작업 예약
	 * 초(0-59) 분(0-59) 시간(0-23) 일(1-31) 월(1-12) 요일(0-7)
	 */
	@Scheduled(cron = "0 15 10 15 * ?")
	public void scheduleTaskUsingCronExpression() {
		long now = System.currentTimeMillis() / 1000;
		log.info("schedule tasks using cron jobs - {}", now);
	}
}
```

## Project

[Github](https://github.com/jihunparkme/blog/tree/main/projects/spring_scheduler)

## Reference

> <https://www.baeldung.com/spring-scheduled-tasks>
>
> <https://copycoding.tistory.com/305>
