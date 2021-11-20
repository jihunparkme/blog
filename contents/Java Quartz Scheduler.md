# Java Quartz Scheduler

Java의 Scheduling Library를 활용해서 일정 시간마다 코드를 실행시켜보자.

## Quartz 

- Quartz는 다중 Thread Architecture기반
- Thread 환경 관리를 위해 ThreadPool 에 의존
- 주요 Interface
  - `Scheduler` – scheduler 와 상호작용하는 기본 API
  - `Job` – 실제 작업을 수행하는 개체
  - `JobDetail` – Job instances 정의에 사용
  - `Trigger` – 주어진 작업(Job)이 수행될 조건 결정 (특정시간, 횟수, 반복주기 등)
  - `JobBuilder` – Job의 instances 정의하는 JobDetail instances 빌드에 사용
  - `TriggerBuilder` – Trigger instances 빌드에 사용

## Dependency

[Maven Repository](https://search.maven.org/classic/#search%7Cgav%7C1%7Cg%3A%22org.quartz-scheduler%22%20AND%20a%3A%22quartz%22)

```xml
<dependency>
    <groupId>org.quartz-scheduler</groupId>
    <artifactId>quartz</artifactId>
</dependency>
```

## Code

코드를 따로 분리해서 설명을 작성하는 것보다 코드와 같이 주석으로 설명을 작성하는게 더 이해하기 쉬워서(본인 기준이지만..) 

코드 설명은 주석을 참고하고 불필요하면 주석을 삭제해주세요 :)

**TestJob.java**

```java
import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class TestJob implements Job {

    private static final SimpleDateFormat TIMESTAMP_FMT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSS"); 
	
	/**
	 * Job interface 구현체
	 * Job의 trigger 실행 시 execute() Method는 scheduler의 스레드 중 하나에 의해 호출
	 * 
	 * @param JobExecutionContext
	 * 런타임 환경에 대한 정보, 이 환경을 실행한 Scheduler에 대한 핸들, 실행을 트리거한 트리거에 대한 핸들, 작업의 JobDetail 개체 및 기타 몇 가지 항목을 작업 인스턴스에 제공 
	 */
	@Override
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        System.out.println("Job Executed [" + new Date(System.currentTimeMillis()) + "]"); 
        
        /**
         * JobData에 접근
         */
        JobDataMap dataMap = ctx.getJobDetail().getJobDataMap();

        String currentDate = TIMESTAMP_FMT.format(new Date());
        String triggerKey = ctx.getTrigger().getKey().toString(); // group1.trggerName
        
        String jobSays = dataMap.getString("jobSays"); // Hello World!
        float myFloatValue = dataMap.getFloat("myFloatValue"); // 3.141
        
        System.out.println(String.format("[%s][%s] %s %s", currentDate, triggerKey, jobSays, myFloatValue));
    }
}
```

**QuartzTest.java**

```java
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzTest {

	 public static void main(String[] args) {
	        
        try {
        	// Scheduler 사용을 위한 인스턴스화
        	SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();
            
            // JOB Data 객체
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("jobSays", "Say Hello World!");
            jobDataMap.put("myFloatValue", 3.1415f);
            
            /**
             * JobDetail 은 Job이 스케줄러에 추가될 때 Quartz Client에 의해 작성 (작업 인스턴스 정의)
             * 
             * 또한 Job에 대한 다양한 속성 설정과 JobDataMap을 포함할 수 있으며,
             * JobDataMap은 Job 클래스의 특정 인스턴스에 대한 상태 정보를 저장하는 데 사용
             * 	- 작업 인스턴스가 실행될 때 사용하고자 하는 데이터 개체를 원하는 만큼 보유 
             * 	- Java Map interface를 구현한 것으로 원시 유형의 데이터를 저장하고 검색하기 위한 몇 가지 편의 방법이 추가
             */
            JobDetail jobDetail = JobBuilder.newJob(TestJob.class)
					        		.withIdentity("myJob", "group1")
					        		.setJobData(jobDataMap)
					        		.build();
            
            /**
             * Job의 실행을 trigger
             * 
             * 작업을 예약하려면 트리거를 인스턴스화하고 해당 속성을 조정하여 예약 요구 사항을 구성
             * 
             * - 특정시간 또는 특정 횟수 반복: SimpleTrigger
             * - 주기적 반복: CronTrigger (초 분 시 일 월 요일 연도)
             */
            
            // SimpleTrigger
            @SuppressWarnings("deprecation")
			SimpleTrigger simpleTrigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                                            .withIdentity("simple_trigger", "simple_trigger_group")
                                            .startAt(new Date(2021 - 1900, 10, 14, 13, 0)) // 2021.11.14 오후 1시
                                            .withSchedule(SimpleScheduleBuilder.repeatSecondlyForTotalCount(5, 10)) // 10초마다 반복하며, 최대 5회 실행
                                            .forJob(jobDetail)
                                            .build();
            
            // CronTrigger
            CronTrigger cronTrigger = (CronTrigger) TriggerBuilder.newTrigger()
					                .withIdentity("trggerName", "cron_trigger_group")
					                .withSchedule(CronScheduleBuilder.cronSchedule("5 * * * * ?")) // 매 5초마다 실행
				//	                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/2 8-17 * * ?")) // 매일 오전 8시에서 오후 5시 사이에 격분마다 실행
					                .forJob(jobDetail)
					                .build();
            
            Set<Trigger> triggerSet = new HashSet<Trigger>();
            triggerSet.add(simpleTrigger);
            triggerSet.add(cronTrigger);
            
            scheduler.scheduleJob(jobDetail, triggerSet, false);
            scheduler.start();
            
        } catch(Exception e) {
            e.printStackTrace();
        }        
    }
}
```

## Result

```json
# simple_trigger_group.simple_trigger : 2021.11.14 오후 1시 부터 매 5초마다 반복
# cron_trigger_group.trggerName : 매 5초마다 반복

[2021-11-14 13:00:00.0015][simple_trigger_group.simple_trigger] Say Hello World! 3.1415

[2021-11-14 13:00:05.0001][cron_trigger_group.trggerName] Say Hello World! 3.1415

[2021-11-14 13:00:10.0004][simple_trigger_group.simple_trigger] Say Hello World! 3.1415
[2021-11-14 13:00:20.0000][simple_trigger_group.simple_trigger] Say Hello World! 3.1415
[2021-11-14 13:00:30.0015][simple_trigger_group.simple_trigger] Say Hello World! 3.1415
[2021-11-14 13:00:40.0009][simple_trigger_group.simple_trigger] Say Hello World! 3.1415

[2021-11-14 13:01:05.0007][cron_trigger_group.trggerName] Say Hello World! 3.1415
[2021-11-14 13:02:05.0011][cron_trigger_group.trggerName] Say Hello World! 3.1415
```

## Reference

> [Documentation](http://www.quartz-scheduler.org/documentation/)
>
> [Introduction to Quartz (baeldung)](https://www.baeldung.com/quartz)
>
> [Scheduling in Spring with Quartz (baeldung)](https://www.baeldung.com/spring-quartz-schedule)
>
> <https://heodolf.tistory.com/134>
