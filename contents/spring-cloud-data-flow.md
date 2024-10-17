👉🏻 실무에서 Batch로 `Task`, `Spring Cloud Data Flow`를 사용하게 되어, 간략하게 사용 방법을 살펴보려고 한다.

# Spring Cloud Data Flow

> Github: [spring-cloud/spring-cloud-dataflow](https://github.com/spring-cloud/spring-cloud-dataflow)
> 
> dataflow.spring.io: [Spring Cloud Data Flow](https://dataflow.spring.io/)

**Spring Cloud Data Flow (SCDF)** 는 MSA에서 스트리밍 및 배치 데이터 처리 파이프라인을 손쉽게 구축, 배포, 모니터링, 관리할 수 있는 오픈 소스 프로젝트
- 이를 통해 개발자는 데이터 처리 흐름을 관리하고 실시간 데이터 및 대규모 배치 데이터를 효율적으로 처리 가능
- `Spring Cloud Stream`, `Spring Batch`와 통합되어 있으며, 다양한 클라우드 플랫폼 및 컨테이너 오케스트레이션 플랫폼([Kubernetes](https://kubernetes.io/), [Cloud Foundry](https://www.cloudfoundry.org/) 등)과 호환

**Spring Cloud Data Flow (SCDF)** 의 간략한 주요 특징
- 스트리밍 및 배치 파이프라인을 쉽게 정의하고 관리
- 도메인 특화 언어(DSL)와 대시보드를 통한 직관적인 파이프라인 정의
- 사전 정의된 모듈과 MSA 기반의 유연한 파이프라인 구성
- Kubernetes 및 Cloud Foundry 같은 다양한 클라우드 플랫폼에서 유연하게 배포
- Spring Cloud Stream과 Spring Batch를 통한 강력한 스트리밍 및 배치 데이터 처리
- 모니터링 및 관리 도구와의 통합, 탄력적 스케일링 지원

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
- local (스케줄링 기능 불가)
- [Cloud Foundry](https://www.cloudfoundry.org/)
- [Kubernetes](https://kubernetes.io/)

### Execute Dataflow

여기서는 간단한 테스트를 위해 `spring-cloud-dataflow-server-x.x.x.jar` 파일을 직접 실행하려고 한다.
- [Manual Installation](https://dataflow.spring.io/docs/installation/local/manual/)

```sh
### Downloading Server Jars
# Download the Spring Cloud Data Flow Server
wget https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dataflow-server/2.11.5/spring-cloud-dataflow-server-2.11.5.jar

# Download Skipper
wget https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-skipper-server/2.11.5/spring-cloud-skipper-server-2.11.5.jar

#### Starting Server Jars
# Skipper
java -jar spring-cloud-skipper-server-2.11.5.jar
# Dataflow
java -jar spring-cloud-dataflow-server-2.11.5.jar

...

  ____                              ____ _                __
 / ___| _ __  _ __(_)_ __   __ _   / ___| | ___  _   _  __| |
 \___ \| '_ \| '__| | '_ \ / _` | | |   | |/ _ \| | | |/ _` |
  ___) | |_) | |  | | | | | (_| | | |___| | (_) | |_| | (_| |
 |____/| .__/|_|  |_|_| |_|\__, |  \____|_|\___/ \__,_|\__,_|
  ____ |_|    _          __|___/                 __________
 |  _ \  __ _| |_ __ _  |  ___| | _____      __  \ \ \ \ \ \
 | | | |/ _` | __/ _` | | |_  | |/ _ \ \ /\ / /   \ \ \ \ \ \
 | |_| | (_| | || (_| | |  _| | | (_) \ V  V /    / / / / / /
 |____/ \__,_|\__\__,_| |_|   |_|\___/ \_/\_/    /_/_/_/_/_/



2024-10-13 20:18:58.716  INFO 33132 --- [           main] s.c.d.c.p.t.DatabaseTypeAwareInitializer : checking database driver type:org.h2.Driver
2024-10-13 20:18:58.750  INFO 33132 --- [           main] c.c.c.
...
2024-10-13 20:19:01.271  INFO 33132 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 9393 (http)
```

### Build an image on a docker

> docker hub 에 task application 이미지 올리기

```sh
# >> project path

# docker login
$ docker login

# project build
$ ./gradlew build

# make docker directory and create Dockerfile
$ mkdir docker
$ cd docker
$ cp ../build/libs/billsetuptask-0.0.1-SNAPSHOT.jar .
$ vi Dockerfile

# 이미지를 생성할 때 사용할 기반 이미지
FROM openjdk:17-jdk-slim 
# JAR_FILE 변수
ARG JAR_FILE=billsetuptask-0.0.1-SNAPSHOT.jar 
# 실행할 jar 파일을 도커 컨테이너 내부에 billsetuptask.jar 이름으로 복사
COPY ${JAR_FILE} billsetuptask.jar 
 # 컨테이너가 시작될 때 실행할 스크립트 혹은 명령
ENTRYPOINT ["java","-jar","/billsetuptask.jar"]

...

# -t : 특정 이름으로 이미지 빌드
# . : Dockerfile 경로
$ docker build -t jihunparkme/billsetuptask:0.0.1-SNAPSHOT .   

# search created image
$ docker images

REPOSITORY                    TAG          IMAGE ID       CREATED         SIZE
billsetuptask                 latest       98dfb123a43a   2 minutes ago   432MB
...

# push docker image
$ docker push jihunparkme/billsetuptask:0.0.1-SNAPSHOT
```

### Date Flow Dashboard

> [Deploying a Spring Cloud Task application by Using Data Flow](https://godekdls.github.io/Spring%20Cloud%20Data%20Flow/batch-developer-guides.batch-development.data-flow-simple-task/)

http://localhost:9393/dashboard 접속

![dashboard](https://github.com/jihunparkme/blog/blob/main/img/scdf/dashboard.png?raw=true)

### Add Application

> Dashboard ➜ Add application ➜ Register one or more applications ➜ Import Application

- `Name`: 리소스 이름
- `Type`: Spring Cloud Task application은 항상 `Task` 타입으로 등록
- `URI`: docker:\<docker-image-path\>/\<imageName\>:\<Version\>

**Add Application(s)**

![dashboard](https://github.com/jihunparkme/blog/blob/main/img/scdf/add-application.png?raw=true)

**Application**

![dashboard](https://github.com/jihunparkme/blog/blob/main/img/scdf/application.png?raw=true)

### Create the Task Definition

> Tasks ➜ Create a task ➜ drag task ➜ Connect to a node ➜ CREATE TASK

**Add Task**

![dashboard](https://github.com/jihunparkme/blog/blob/main/img/scdf/add-task.png?raw=true)

**Task**

![dashboard](https://github.com/jihunparkme/blog/blob/main/img/scdf/task.png?raw=true)

### Launching the Task

> Tasks ➜ Task Menu ➜ Lanuch ➜ Launch task

Error creating bean with name 'org.springframework.cloud.task.configuration.TaskLifecycleConfiguration': Unsatisfied dependency expressed through constructor parameter 2: Error creating bean with name 'org.springframework.cloud.task.configuration.SimpleTaskAutoConfiguration': Invocation of init method failed

에러 확인

**Tasks**

![dashboard](https://github.com/jihunparkme/blog/blob/main/img/scdf/tasks.png?raw=true)

**Launch task bill-setup-task**

![dashboard](https://github.com/jihunparkme/blog/blob/main/img/scdf/launch-task.png?raw=true)