# Spring Cloud Data Flow

실무에서 Spring Batch를 Spring Cloud Data Flow를 활용하여 사용하게 되어<br/>
`Spring Cloud Task application`과 `Spring Batch application`을 만들기 위한 방법을 알아보려고 한다.

이 application들은 독립형으로 배포하거나 Spring Cloud Data Flow를 이용해 Cloud Foundry, Kubernetes, local instance에 배포가 가능하다.

## Make Simple Task

> [Batch Processing with Spring Cloud Task](https://dataflow.spring.io/docs/batch-developer-guides/batch/spring-task/)

Spring Cloud Task를 이용하여 간단한 Spring Boot Application 만들기

**commit**

- [Building the Application](https://github.com/jihunparkme/Study-project-spring-java/commit/284befb7419863d648d6b3556b356027aa7fec11)

**application.yml**

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/task
    username: root
    password: 1234
  cloud:
    task:
      name: bill-setup-test
```

CustomTaskListener

```kotlin
@Component
class CustomTaskListener : TaskExecutionListener {

    override fun onTaskStartup(taskExecution: TaskExecution) {
        // 작업 시작 시 실행할 코드
    }

    override fun onTaskEnd(taskExecution: TaskExecution) {
        // 작업 종료 시 EXIT_CODE와 EXIT_MESSAGE 설정
        taskExecution.exitCode = 200
        taskExecution.exitMessage = "Custom Exit Message"
    }

    override fun onTaskFailed(taskExecution: TaskExecution, throwable: Throwable) {
        // 작업이 실패했을 때 실행할 코드
        taskExecution.exitCode = -1
        taskExecution.exitMessage = "Task failed due to: ${throwable.message}"
    }
}
```

...

Spring Cloud Task는 모든 Task 실행 내역을 `TASK_EXECUTION` 테이블에 기록하고,<br/>
Spring Cloud Task가 기록하는 정보들은 아래와 같다.
- START_TIME: Task 실행 시작 시간
- END_TIME: Task 실행 완료 시간
- TASK_NAME: Task 실행 관련 이름
- EXIT_CODE: Task 실행 후 반환한 종료 코드
- EXIT_MESSAGE: Task 실행 후 반환한 종료 메세지
- ERROR_MESSAGE: Task 실행 후 반환한 에러 메시지(존재 시)
- EXTERNAL_EXECUTION_ID: Task 실행과 관련한 ID

```bash
mysql> select * from TASK_EXECUTION;
+-------------------+----------------------------+----------------------------+-------------+-----------+--------------+---------------+---------------------+-----------------------+---------------------+
| TASK_EXECUTION_ID | START_TIME                 | END_TIME                   | TASK_NAME   | EXIT_CODE | EXIT_MESSAGE | ERROR_MESSAGE | LAST_UPDATED        | EXTERNAL_EXECUTION_ID | PARENT_EXECUTION_ID |
+-------------------+----------------------------+----------------------------+-------------+-----------+--------------+---------------+---------------------+-----------------------+---------------------+
|                 1 | 2024-10-11 23:03:10.222315 | 2024-10-11 23:03:10.324128 | bill-setup-test |         0 | NULL         | NULL          | 2024-10-11 23:03:10 | NULL                  |                NULL |
|                 2 | 2024-10-11 23:03:49.579194 | 2024-10-11 23:03:49.651225 | bill-setup-test |         0 | NULL         | NULL          | 2024-10-11 23:03:50 | NULL                  |                NULL |
...
|                 4 | 2024-10-11 23:16:30.345811 | 2024-10-11 23:16:30.412893 | bill-setup-test |         0 | Custom Exit Message | NULL          | 2024-10-11 23:16:30 | NULL                  |                NULL |
...
|                 9 | 2024-10-11 23:23:21.111182 | 2024-10-11 23:23:21.171615 | bill-setup-test |         1 | Custom Exit Message | java.lang.RuntimeException: error message | 2024-10-11 23:23:21 | NULL                  |                NULL |
+-------------------+----------------------------+----------------------------+-------------+-----------+--------------+---------------+---------------------+-----------------------+---------------------+
```

## Register and Launch a Spring Cloud Task application using Data Flow

> Data Flow를 이용해 Spring Cloud Task Application 등록 및 가동하기








## Spring Batch Jobs

> 간단한 Spring Batch Job

## Register and launch a Spring Batch application using Data Flow

> Data Flow를 이용해 Spring Batch Application 등록 및 가동하기