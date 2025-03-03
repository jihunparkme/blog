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

⚠️ 정상적인 설정을 위해 RDS 생성을 제외한 아래 단계들은 반드시 적용이 필요합니다.
- Set Timezone
- EC2 프리티어 메모리 부족현상 해결
- 외부에서 서비스 접속

> Docker에 이미지를 빌드하는 방식을 적용하면서 서버에 자바 설치, 깃허브 연동과 같은 기본 세팅은 불필요하게 되었습니다.

⚠️ 고정 IP(Elastic IP) 등록
- [EC2]-[네트워크 및 보안]-[탄력적 IP]
- 탄력적 IP 주소 할당 ➜ 탄력적 IP 주소 연결 ➜ 생성한 EC2 인스턴스에 연결

### Docker

EC2의 기본적인 설정은 생각보다 간단(?)했습니다.

이제 빌드한 이미지로 서비스를 띄워볼 차례입니다.

👉🏻 **Install Docker**

```bash
# Install
$ sudo yum update -y # 인스턴스에 있는 패키지 업데이트
$ sudo yum install -y docker # docker 설치
$ docker -v # 버전 확인

# Setting
$ sudo systemctl enable docker.service # 재부팅 시 docker 자동 실행 설정

# Start
$ sudo systemctl start docker.service # docker 서비스 실행
$ systemctl status docker.service # docker 서비스 상태 확인

# Docker login
docker login -u ${username}
```

👉🏻 **Docker Login Using PAT(Personal Access Token)**
- 가급적 암호를 직접적으로 사용하는 것은 선호하지 않으므로 전용 PAT를 발급받아서 사용하려고 합니다.
- [Account Settings](https://app.docker.com/settings/account-information) 접속
  - Personal access tokens 메뉴 클릭
  - Create new token 클릭
  - 서버에서는 읽기 권한만 필요하므로 Access permissions는 Read-only 로 진행

> ⚠️ **permission denied while trying to connect to the Docker daemon socket at unix:///var/run/docker.sock**
>
> 만일 docker 명령어 사용 시 위 에러가 발생한다면, 
>
> `/var/run/docker.sock` 파일의 권한을 변경하여 그룹 내 다른 사용자도 접근 가능하도록 변경이 필요합니다.
>
> `sudo chmod 666 /var/run/docker.sock`

⚠️ **도커 명령어 참고**

```bash
# 컨테이너 중지
$ docker stop ${NAMES}

# 컨테이너 시작
$ docker start ${NAMES}

# 컨테이너 재시작
$ docker restart ${NAMES}

# 컨테이너 접속
$ docker exec -it ${NAMES} bash
```

### Mongodb(Docker)

해당 프로젝트에서는 도커에서 몽고디비를 실행시켜서 사용하려고 합니다.

```bash
## mongo 이미지 가져오기
$ docker pull mongo

## 이미지 목록 확인
$ docker images

## mongo 컨테이너 실행
$ docker run -itd -p 27017:27017 --restart=always --name mongodb -v ~/data:/data/db mongo

## mongo shell 접속
$ docker exec -it mongodb mongosh

user> use his_voice # database 생성
user> db.createCollection("sermons") # collection 셍상
```

### Docker run

이미지 실행은 [JIB설정-이미지 실행] 파트에서 다룬 것과 같이 이미지를 가져온 후 실행해 주면 됩니다.

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

이제 `http://[탄력적 IP]:8080`로 접속해 보면 성공적으로 서비스가 실행중인 것을 확인할 수 있습니다.

> ⚠️ 서비스와 데이터베이스가 모두 컨테이너로 실행될 경우
>
> 서비스에서 데이터베이스에 접근하기 위해 서비스에서는 EC2 내부 IP를 명시해야 하는데 
>
> EC2에서 아래 명령어를 통해 내부 IP 확인이 가능합니다.
>
> `ifconfig | grep "inet "`
















## 도메인 등록

https://jojoldu.tistory.com/270?category=635883

## SSL 인증서

https://data-make.tistory.com/783









## 무중단 배포

https://data-make.tistory.com/773

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