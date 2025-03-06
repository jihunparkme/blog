# 하루만에 서비스 배포하기(JIB, EC2, Docker ..)

배포를 위한 **JIB**, **AWS EC2**, **Docker**, **Mongodb**, **Grafana**, **도메인 등록**, **SSL 인증서**에 대한 내용을 다룹니다.

⚠️ 본문에서는 각 개념에 대한 자세한 내용을 다루지 않고, 간략한 진행 과정을 다루고 있습니다.<br/>
따라서 자세한 내용은 각 내용에 첨부된 글을 참고해 주세요.🙇🏻‍♂️

# JIB를 활용한 컨테이너 이미지 빌드/푸시

일반적으로 도커 허브에 이미지를 빌드하기 위해 `Docker`, `Dockerfile`이 필요한데

Gradle, Maven에서 `Jib plugin`을 활용해 간편하게 이미지를 빌드하고 푸시하는 방법을 알이보려고 합니다.

## JIB 설정

> - spring boot: 3.4.1
> - Java: JDK 21
> - Kotlin: 1.9.25
> - Gradle: 8.11.1

👉🏻 `build.gradle.kts`에 `jib plugins` 추가하기

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
`jdk21`, `gradle-8.11.1` 버전을 사용하고 있는데 jib `3.2.0` 버전을 적용하니 아래와 같은 에러가 발생했다.
  ```bash
  The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0.

  The org.gradle.api.plugins.JavaPluginConvention type has been deprecated. This is scheduled to be removed in Gradle 9.0.
  ```
  - 비슷한 경우 `jib` 버전업이 필요하다. 
    - 최신 버전(`3.4.4`) 또는 gradle 버전에 맞는 사용해 보자.

베이스 이미지는 `jdk21`을 사용중이므로 그에 맞는 jdk 이미지를 설정
- 그밖에 이미지 이름, 태그, 컨테이너 설정 가능

## 이미지 빌드 & 푸시

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
> https://hub.docker.com/repositories

## 컨테이너 실행

로컬에서 도커 허브에 업로드한 이미지를 실행해 봅시다

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

# AWS EC2

AWS EC2 Free Tier 구축은 아래 글(이전 포스팅)에서 RDS 부분만 제외하고 참고해 봅시다.
- [AWS EC2 & RDS Free Tier 구축](https://data-make.tistory.com/771)

⚠️ 정상적인 설정을 위해 RDS 생성을 제외한 아래 단계들은 반드시 적용이 필요합니다. 자세한 내용은 글을 참고하시고 간략하게 명령어만 남겨두겠습니다.

> Docker에 이미지를 빌드하는 방식을 적용하면서 서버에 자바 설치, 깃허브 연동과 같은 기본 세팅은 불필요하게 되었습니다.

✅ Set Timezone

```bash
# check timezone
$ date

# change timezone
$ sudo rm /etc/localtime
$ sudo ln -s /usr/share/zoneinfo/Asia/Seoul /etc/localtime
```

✅ EC2 프리티어 메모리 부족현상 해결

```bash
# dd 명령어로 swap 메모리 할당 (128M 씩 16개의 공간, 약 2GB)
$ sudo dd if=/dev/zero of=/swapfile bs=128M count=16

# swap 파일에 대한 읽기 및 쓰기 권한 업데이트
$ sudo chmod 600 /swapfile

# Linux 스왑 영역 설정
$ sudo mkswap /swapfile

# swap 공간에 swap 파일을 추가하여 swap 파일을 즉시 사용할 수 있도록 설정
$ sudo swapon /swapfile

# 절차가 성공했는지 확인
$ sudo swapon -s

# /etc/fstab 파일을 수정하여 부팅 시 swap 파일 활성화
$ sudo vi /etc/fstab

/swapfile swap swap defaults 0 0 # 파일 끝에 추가 후 저장

# 메모리 상태 확인
$ free -h
```

✅ 외부에서 서비스 접속
- 외부에서 EC2 인스턴스의 특정 포트로 접속을 하기 위해 인바운드 탭에서 해당 포트를 열어주어야 합니다.
- 다른 포트도 서비스에 사용한다면 인바운드 탭에서 오픈시켜주면 됩니다.
- AWS EC2 인스턴스 페이지 -> 보안그룹 -> 현재 인스턴스의 보안 그룹 선택 -> 인바운드 탭
  
  ```text 
  인바운드 규칙 편집 버튼을 클릭
  - 유형: 사용자지정(TCP)
  - 포트 범위: 8080 (애플리케이션 포트)
  - 소스: Anywhere(0.0.0.0/0)
  ```

- ⚠️ ssh(22) 포트나 애플리케이션 관리에 사용되는 포트는 로컬 PC IP로 설정해 두는게 보안상 필수입니다.

✅ 고정 IP(Elastic IP) 등록
- 기본적으로 인스턴스를 재가동할 때마다 IP가 변경되는데 고정 IP를 사용해서 IP가 변경되지 않도록 할 수 있습니다.
- [EC2]-[네트워크 및 보안]-[탄력적 IP]
- 탄력적 IP 주소 할당 ➜ 탄력적 IP 주소 연결 ➜ 생성한 EC2 인스턴스에 연결

> 📚 Reference.
>
> [[AWS] AWS EC2 & RDS Free Tier 구축](https://data-make.tistory.com/771)
>
> [스프링부트로 웹 서비스 출시하기 - 4. AWS EC2 & RDS 구축하기](https://jojoldu.tistory.com/259)

## Docker In EC2

EC2의 기본적인 설정은 생각보다 간단(?)했습니다.

이제 도커 허브에 업로드한 이미지로 EC2 인스턴스에 서비스를 실행시켜볼 차례입니다.

먼저 도커 설치부터 시작합니다.

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
- 가급적 암호를 직접적으로 사용하는 것은 권장하지 않으므로 전용 PAT를 발급받아서 사용하려고 합니다.
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

📖 **도커 명령어 참고**

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

## Docker Mongodb

해당 프로젝트에서는 몽고디비를 도커로 실행시켜서 사용하려고 합니다.

다른 RDB 혹은 NoSQL DB 사용한다면 해당 이미지를 가져온 후 실행시키면 됩니다.

```bash
## mongo 이미지 가져오기
$ docker pull mongo

## 이미지 목록 확인
$ docker images

## mongo 컨테이너 실행
$ docker run -itd -p 27017:27017 --restart=always --name mongodb -v ~/data:/data/db mongo

## mongo shell 접속
$ docker exec -it mongodb mongosh

user> use database_name # database 생성
user> db.createCollection("collection_name") # collection 셍상
```

## 컨테이너 실행

컨테이너 실행은 [JIB설정-컨테이너 실행] 파트에서 다룬 것과 같이 이미지를 가져온 후 실행해 주면 됩니다.

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
- 인바운드 탭에서 8080 포트를 Anywhere로 열어두었기 때문에 접속이 가능한 것입니다.

> ⚠️ 서비스와 데이터베이스가 모두 도커 컨테이너로 실행될 경우
>
> 서비스에서 데이터베이스에 엑세스하기 위해 서비스에서는 EC2 인스턴스 내부 IP를 명시해야 하는데 
>
> EC2에서 아래 명령어를 통해 내부 IP 확인이 가능합니다.
>
> `ifconfig | grep "inet "`
>
> 예시. `data.mongodb.uri: mongodb://${EC2_INSTANCE_INTERNAL_IP}:${PORT}...`

# 무중단 배포

## Nginx 설정

```bash
# nginx 설치
$ sudo yum install -y nginx
```

👉🏻 **nginx 명령어 참고**

```bash
# 버전 확인
$ nginx -version

# 기본 명령어
$ sudo systemctl start nginx   # nginx 시작
$ sudo systemctl enable nginx  # 부팅시 자동실행
$ sudo systemctl status nginx  # 상태 확인
$ sudo systemctl stop nginx    # nginx 중지

# 실행 중 에러확인.
$ journalctl -xe
```

👉🏻 **nginx 설정**

- ✅ 동적 프록시 설정을 위해 service-url 관리 파일 생성

```bash
$ sudo mkdir /etc/nginx/conf
$ sudo vi /etc/nginx/conf/service-url.inc

set $service_url http://[Elastic IP]:8080;
```

- 기본 설정 파일인 `/etc/nginx/nginx.conf` 하단을 보면 `/etc/nginx/conf.d` 경로의 conf 파일들을 include 해주고 있다.

```bash
$ vi /etc/nginx/nginx.conf

...
include /etc/nginx/conf.d/*.conf;
...
```

- ✅ default.conf 파일 생성

```bash
# default.conf 파일 생성
$ sudo vi /etc/nginx/conf.d/default.conf

server {
	include /etc/nginx/conf/service-url.inc;

	location / {
		proxy_pass $service_url;
	}
}

# nginx 재시작
$ sudo systemctl restart nginx
```

nginx 재시작 이후 포트를 제외한 `http://[탄력적 IP]`로 접속이 잘 된다면 nginx 설정이 정상적으로 되었습니다.

## 배포 스크립트

무중단 배포는 Blue/Green 방식으로 적용해 보려고 합니다.

인바운드 8081 8082 오픈

이제 본격적으로 무중단 배포 스크립트를 작성해 보겠습니다.

```bash
# 배포 스크립트 생성
$ vi ~/app/deploy/nonstop-deploy.sh

#!/bin/bash

IS_BLUE=$(docker ps | grep blue) # 실행중인 컨테이너가 blue인지 확인

if [ -z $IS_BLUE  ];then # green 이라면
  echo "### GREEN => BLUE ###"

  echo "1 >>> get latest image"
  docker pull jihunparkme/my-project

  echo "2 >>> run blue(8081) container"
  docker run -itd -p 8081:8080 -p 4041:4040 -e SPRING_PROFILES_ACTIVE=prod1 --name blue jihunparkme/my-project

  while [ 1 = 1 ]; do
    echo "3 >>> blue(8081) health check..."
    sleep 3

    response=$(curl -s http://localhost:4041/management/actuator/health)
    up_count=$(echo $response | grep 'UP' | wc -l)

    if [ $up_count -ge 1 ]; then # 서비스가 정상적으로 실행되었다면 health check 중지
      echo "blue(8081) health check success."
      break ;
    fi
  done;

  echo "4 >>> reload nginx"
  echo "set \$service_url http://$[ELASTIC IP]:8081;" | sudo tee /etc/nginx/conf/service-url.inc
  sudo nginx -s reload

  echo "5 >>> green(8082) container down"
  docker rm -f green

else
  echo "### BLUE => GREEN ###"

  echo "1 >>> get latest image"
  docker pull jihunparkme/my-project

  echo "2 >>> run green(8082) container"
  docker run -itd -p 8082:8080 -p 4042:4040 -e SPRING_PROFILES_ACTIVE=prod2 --name green jihunparkme/my-project

  while [ 1 = 1 ]; do
    echo "3 >>> green(8082) health check..."
    sleep 3

    response=$(curl -s http://localhost:4042/management/actuator/health)
    up_count=$(echo $response | grep 'UP' | wc -l)

    if [ $up_count -ge 1 ]; then # 서비스 가능하면 health check 중지
        echo "green(8082) health check success."
        break ;
    fi
  done;

  echo "4 >>> reload nginx"
  echo "set \$service_url http://$[ELASTIC IP]:8082;" | sudo tee /etc/nginx/conf/service-url.inc
  sudo nginx -s reload

  echo "5 >>> blue(8081) container down"
  docker rm -f blue
fi

# 스크립트 저장 후 실행 권한 적용
$ chmod 755 ~/app/deploy/nonstop-deploy.sh

# 스크립트 실행
$ ~/app/deploy/nonstop-deploy.sh
```

👉🏻 스크립트를 실행하면 아래와 같이 무중단 배포가 수행됩니다.

```bash
### GREEN => BLUE ###
1 >>> get latest image
Using default tag: latest
latest: Pulling from jihunparkme/my-project
Digest: sha256:xxx
Status: Image is up to date for jihunparkme/my-project:latest
docker.io/jihunparkme/my-project:latest
2 >>> run blue(8081) container
xxx
3 >>> blue(8081) health check...
3 >>> blue(8081) health check...
3 >>> blue(8081) health check...
3 >>> blue(8081) health check...
3 >>> blue(8081) health check...
blue(8081) health check success.
4 >>> reload nginx
set $service_url http://$[ELASTIC IP]:8081;
5 >>> green(8082) container down
green
```

👉🏻 alias 등록

```bash
$ vi ~/.bashrc

# 하단에 alias 추가
alias deploy="~/app/deploy/nonstop-deploy.sh"

$ source ~/.bashrc

# deploy alias로 무중단 배포 진행
$ deploy
```

## 그라파나



```bash
# 도커 컴포즈 설치
$ sudo curl -L "https://github.com/docker/compose/releases/download/v2.5.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
# 권한 부
$ sudo chmod +x /usr/local/bin/docker-compose
# 버전 확인
$ docker-compose --version

$ mkdir ~/app/monitoring
$ vi ~/app/monitoring/prometheus.yml
$ vi ~/app/monitoring/monitoring.yml
```

✅ **prometheus.yml**

```sh
scrape_configs:
 - job_name: "prometheus"
   static_configs:
     - targets: ["$[ELASTIC IP]:9090"]
 - job_name: "spring-actuator" # 수집하는 임의 이름
   # 수집 경로 지정(1초에 한 번씩 호출해서 메트릭 수집)
   metrics_path: '/management/actuator/prometheus'
   # 수집 주기 (10s~1m 권장)
   scrape_interval: 1s
   # 수집할 서버 정보(IP, PORT)
   static_configs:
     - targets: ['$[ELASTIC IP]:4041', '$[ELASTIC IP]:4042']
```

✅ **monitoring.yml**

```sh
version: '3'

services:
  prometheus:
    image: prom/prometheus:latest
    restart: always
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
       - ./prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana:latest
    restart: always
    container_name: grafana
    user: "$UID:$GID"
    ports:
      - "3000:3000"
    volumes:
      - ./grafana-data:/var/lib/grafana
    depends_on:
      - prometheus
```

```bash
docker-compose -f ~/app/monitoring/monitoring.yml up -d
```


인바운드 4041 4042  $[ELASTIC IP] 추가



참고. [[Monitoring] Prometheus & Grafana](https://data-make.tistory.com/795)




















# 도메인 등록

https://jojoldu.tistory.com/270?category=635883

# SSL 인증서

https://data-make.tistory.com/783

# 비고

# 몽고디비 백업

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