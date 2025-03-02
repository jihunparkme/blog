# 하루만에 서비스 배포하기(jib, EC2, Docker)

## JIB를 활용한 컨테이너 이미지 빌드/푸시

일반적으로 도커 허브에 이미지를 빌드하기 위해 `Docker`, `Dockerfile`이 필요한데

Gradle, Maven에서 `Jib plugin`을 활용해 이미지를 빌드하고 푸시하는 방법을 알아보자.

### JIB 설정

> spring boot: 3.4.1
>
> Java: JDK 21
>
> Kotlin: 1.9.25
>
> Gradle: 8.11.1

👉🏻 `build.gradle.kts`에 jib plugins 추가하기

```kts
plugins {
    // ...
	id ("com.google.cloud.tools.jib") version "3.4.4"
}

// ...

jib {
	from {
		image = "eclipse-temurin:21.0.6_7-jre-alpine"
	}
	to {
		image = "jihunparkme/my-project"
		tags = setOf("latest", "1.0.0")
	}
	container {
		jvmFlags = listOf("-Xms128m", "-Xmx128m")
	}
}
```
- `jdk21`, `gradle-8.11.1` 버전을 사용하고 있는데 jib `3.2.0` 버전을 추가하니 아래와 같은 에러가 발생했다.
  - 비슷한 경우 `jib` 버전업이 필요하다. 
  - 최신 버전(`3.4.4`) 또는 gradle 버전에 맞는 사용해 보자.
    ```bash
    The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0.

    The org.gradle.api.plugins.JavaPluginConvention type has been deprecated. This is scheduled to be removed in Gradle 9.0.
    ```
- 베이스 이미지는 `jdk21`을 사용중이므로 그에 맞는 jdk 이미지를 설정
- 그밖에 이미지 이름, 태그, 컨테이너 설정 가능

### 이미지 빌드 및 푸시

👉🏻 프로젝트의 홈 디렉토리에서 아래 명령어를 통해 이미지를 빌드하면 자동으로 이미지를 레파지토리에 푸시

```bash
$ ./gradlew jib

...

Built and pushed image as jihunparkme/my-project, jihunparkme/my-project, jihunparkme/my-project:1.0.0
Executing tasks:
[===========================   ] 91.7% complete
> launching layer pushers


BUILD SUCCESSFUL in 16s
6 actionable tasks: 6 executed
```

> 자신의 도커 허브 레파지토리를 확인해 보면 이미지가 푸시된 것을 확인할 수 있다.
> 
> https://hub.docker.com/repositories/{DOCKER-HUB-USERNAME}

### 이미지 실행

```bash
# pull image
$ docker pull jihunparkme/my-project

# docker run
$ docker run -itd -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod --name my-project jihunparkme/my-project

# CONTAINER ID 확인
$ docker ps 

# 로그 확인
$ docker logs -f ${CONTAINER ID} or ${NAMES}
```

## EC2

AWS EC2 Free Tier 구축은 아래 글(이전 포스팅)에서 RDS 부분만 제외하고 참고하기
- [AWS EC2 & RDS Free Tier 구축](https://data-make.tistory.com/771)

## 서비스 실행

EC2의 기본적인 설정은 생각보다 간단했습니다.


















## 무중단 배포

https://data-make.tistory.com/773

## 도메인 등록

https://jojoldu.tistory.com/270?category=635883

## SSL 인증서

https://data-make.tistory.com/783


## 몽고디비 백업

`mongodb`를 사용할 예정인데 데이터베이스의 백업과 복구도 빠질 수 없는 내용이라고 생각한다.

참고차 백업/복구하는 방법을 알아보자.

👉🏻 **백업하기**

```bash
mongodump --out ~/mongo_backup --host 127.0.0.1 --port 27017
```
- 인증이 필요한 경우: `-u <username> -p <password>`
- 특정 데이터베이스: `--db <dbname>`
- 특정 컬렉션: `--collection <collectionName>`

👉🏻 **복구하기**

```bash
mongorestore --host 127.0.0.1 --port 27017 <dump data가 있는 디렉토리>
```
- 인증이 필요한 경우: `-u <username> -p <password>`
- 복구 전 드랍시킬 데이터베이스: `--drop <drop db name>`
- 특정 데이터베이스: `--db <dbname>`
- 특정 컬렉션: `--collection <collectionName>`

👉🏻 **원격지 서버로 파일 전송하기**

```bash
sudo apt-get update
sudo apt-get install openssh-server
```