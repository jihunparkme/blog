package com.example.scheduler.quartz;

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
                                            .startAt(new Date(2021 - 1900, 10, 15, 9, 49)) // 2021.11.14 오후 1시
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