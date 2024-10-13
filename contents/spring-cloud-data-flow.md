👉🏻 실무에서 Batch로 `Task`, `Spring Cloud Data Flow`를 사용하게 되어, 간략하게 사용 방법을 살펴보려고 한다.

# Spring Cloud Data Flow

> Github: [spring-cloud/spring-cloud-dataflow](https://github.com/spring-cloud/spring-cloud-dataflow)
> 
> dataflow.spring.io: [Spring Cloud Data Flow](https://dataflow.spring.io/)

**Spring Cloud Data Flow (SCDF)**는 마이크로서비스 아키텍처에서 스트리밍 및 배치 데이터 처리 파이프라인을 손쉽게 구축, 배포, 모니터링, 관리할 수 있는 오픈 소스 프로젝트이다.
- 이를 통해 개발자는 데이터 처리 흐름을 관리하고 실시간 데이터 및 대규모 배치 데이터를 효율적으로 처리할 수 있다.

ℹ️ 참고.

> Spring Batch를 실행시키기 위해 Spring Batch Job, Scheduler, Pipeline, Monitoring 등이 필요하다.
> 
> 기존에는 Spring Batch Admin과 Jenkins(scheduler)를 이용해 구현했다고 하는데, Spring Batch Admin은 2017년 12월 31일자로 서비스가 종료되었고, 
> 
> Spring은 Spring Batch Admin의 복제/확장판인 `Spring Cloud Data Flow` 사용을 권장하고 있다.
>
> [Spring Batch Admin](https://docs.spring.io/spring-batch-admin/2.x/)

Cloud Foundry 및 Kubernetes를 위한 마이크로서비스 기반 스트리밍 및 일괄 데이터 처리

## Make Simple Task

> Spring Cloud Task를 이용하여 간단한 Spring Boot Application 만들기
> 
> [Batch Processing with Spring Cloud Task](https://dataflow.spring.io/docs/batch-developer-guides/batch/spring-task/)

- 단순하게 Spring Cloud Task를 사용해 BILL_STATEMENTS 테이블을 생성하는 동작을 수행한다.
- `@EnableTask`는 Task 실행에 관한 정보(Task 시작/종료 시간과 종료 코드 등)를 저장하는 `TaskRepository`를 설정한다.
- commit: [Building the Application](https://github.com/jihunparkme/Study-project-spring-java/commit/284befb7419863d648d6b3556b356027aa7fec11)

```kotlin
@Configuration
@EnableTask
class TaskConfiguration {
    @Autowired
    private val dataSource: DataSource? = null
    @Bean
    fun commandLineRunner(): CommandLineRunner {
        return CommandLineRunner { args: Array<String?>? ->
            val jdbcTemplate = JdbcTemplate(dataSource!!)
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS " +
                        "BILL_STATEMENTS ( " +
                            "id int, " +
                            "first_name varchar(50)," +
                            "last_name varchar(50), " +
                            "minutes int," +
                            "data_usage int, " +
                            "bill_amount double" +
                        ")"
            )
        }
    }
}
```

**application.yml**
- `TASK_NAME`은 기본값으로 application이 설정되는데 `spring.cloud.task.name`으로 설정을 변경할 수 있다.

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

**CustomTaskListener**
- `TaskExecutionListener`를 구현하여 `EXIT_CODE`, `EXIT_MESSAGE`를 설정할 수 있다.

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
        taskExecution.exitCode = 500
        taskExecution.exitMessage = "Task failed due to: ${throwable.message}"
    }
}
```

...

Spring Cloud Task는 모든 Task 실행 내역을 `TASK_EXECUTION` 테이블에 기록하고,<br/>
Spring Cloud Task가 기록하는 정보들은 아래와 같다.
- `START_TIME`: Task 실행 시작 시간
- `END_TIME`: Task 실행 완료 시간
- `TASK_NAME`: Task 실행 관련 이름
- `EXIT_CODE`: Task 실행 후 반환한 종료 코드
- `EXIT_MESSAGE`: Task 실행 후 반환한 종료 메세지
- `ERROR_MESSAGE`: Task 실행 후 반환한 에러 메시지(존재 시)
- `EXTERNAL_EXECUTION_ID`: Task 실행과 관련한 ID

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
>
> [Deploying a Spring Cloud Task application by Using Data Flow](https://dataflow.spring.io/docs/batch-developer-guides/batch/data-flow-simple-task/)

Spring Cloud Data Flow를 사용하기 위해 서버 구성 요소를 설치해야 하는데, Data Flow는 기본적으로 아래 세 가지 플랫폼을 지원한다.
- local
- [Cloud Foundry](https://www.cloudfoundry.org/)
- [Kubernetes](https://kubernetes.io/)

여기서는 간단한 테스트를 위해 `spring-cloud-dataflow-server.jar` 파일을 직접 실행하려고 한다.
- [Manual Installation](https://dataflow.spring.io/docs/installation/local/manual/)

```sh
wget https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dataflow-server/2.11.5/spring-cloud-dataflow-server-2.11.5.jar

java -jar spring-cloud-dataflow-server-2.11.5.jar
```











## Spring Batch Jobs

> 간단한 Spring Batch Job

## Register and launch a Spring Batch application using Data Flow

> Data Flow를 이용해 Spring Batch Application 등록 및 가동하기