# AWS EC2 & RDS Free Tier

## AWS EC2 & RDS 구축

AWS EC2 & RDS 구축 방법은 [향로님의 블로그](https://jojoldu.tistory.com/259) 가 참고하기 좋은 것 같다. (2023년 10월 기준 UI 가 약간 다르긴 하지만 기본적인 설정은 동일)

그밖에도 자세한 구축 방법은 많은 블로그에서 설명을 해주고 있으니 생략하는 것으로..

.

다만, 구축하는 과정에서 필요했던 참고 내용들을 정리해 보려고 한다.

.

### AWS 프리티어 무료 사용량

`EC2`(Amazon Elastic Compute Cloud) : 클라우드에서 제공되는 크기 조정 가능한 컴퓨팅 파워
- 월별 750시간 무료 (EC2 인스턴스 하나를 풀로 돌려도 남는 시간)
  - EC2 하나당 750시간 가동이 기준으로, 만일 EC2 2개를 돌린다면 375시간만(15일) 무료. 3개일 경우 250시간(10일) 무료.
  - 프리티어에서 다수의 인스턴스를 풀로 돌릴경우 월별 무료 사용량이 금방 제한되어 과금이 되니 학습이 끝나면 항상 인스턴스를 종료 혹은 중지 해주자.
- 리전에 따라 Linux/Windows 운영체제의 t2.micro 또는 t3.micro 인스턴스 타입만 무료


`RDS`(Amazon Relational Database Service) : MySQL, PostgreSQL, MariaDB 또는 SQL Server를 위한 관리형 관계형 데이터베이스 서비스
- RDS 인스턴스 1개 무료 사용 가능
- 월별 750시간까지 무료(단, db.t2.micro, db.t3.micro, db.t4g.micro 타입만 사용 가능)
- 범용(SSD) 데이터베이스 스토리지 20GB 제한
  - 10GB를 사용하는 RDS 인스턴스 3개를 생성하면 과금 발생(30GB)
- 데이터베이스 백업 및 DB 스냅샷용 스토리지 20GB
- 과금 방지 옵션 설정
  - 자동 백업 활성화 옵션 OFF
  - 스토리지 자동 조정 활성화 옵션 OFF
  - 마이너 버전 자동 업그레이드 사용 옵션 OFF
  - Multi-AZ와 고성능 I/O인 Provisioned IOPS Storate를 사용하지 않도록 설정

> [AWS 프리티어 요금 폭탄 방지 무료 사용량 정리](https://inpa.tistory.com/entry/AWS-%F0%9F%92%B0-%ED%94%84%EB%A6%AC%ED%8B%B0%EC%96%B4-%EC%9A%94%EA%B8%88-%ED%8F%AD%ED%83%84-%EB%B0%A9%EC%A7%80-%F0%9F%92%B8-%EB%AC%B4%EB%A3%8C-%EC%82%AC%EC%9A%A9%EB%9F%89-%EC%A0%95%EB%A6%AC)
>
> [AWS Free Tier](https://aws.amazon.com/ko/free/?all-free-tier.sort-by=item.additionalFields.SortRank&all-free-tier.sort-order=asc&awsf.Free%20Tier%20Types=*all&awsf.Free%20Tier%20Categories=*all)

.

### EC2 인스턴스 생성

- EC2 OS : Amazon Linux(Amazon Linux 2023 AMI)
- 인스턴스 유형: t2.micro

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-ec2/1.png 'Result')

.

- 키 페어는 인스턴스에 접근하기 위해 필요하므로 조심한 보관이 필요하다.

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-ec2/4.png 'Result')

.

- VPC, 서브넷: default
- 퍼블릭 IP 자동 할당: 활성화
- 보안 그룹 구성
  - AWS EC2 터미널 접속을 위한 ssg 22 포트는 외부 접근 차단을 위해 내 IP 를 선택하자.
  - 다른 장소에서 접속 시 해당 장소의 IP 를 다시 SSH 규칙에 추가하는 것이 안전
  - HTTPS(443), HTTP(80) 는 외부에서 웹서비스 접근을 위해 사용하므로 포트를 열어두자.

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-ec2/2.png 'Result')

.

- 불륨 크기 : 30GB (30GB 까지 프리티어로 사용 가능)

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-ec2/2.png 'Result')

.

나머지 
- `AWS EC2 고정 IP(Elastic IP) 등록`
- `EC2 터미널 접속`

은 [향로님의 블로그](https://jojoldu.tistory.com/259)를 참고해 보자.

.

### RDS 생성

- 표준 생성.
- MariaDB.
- 추가 비용 없이 쓰기 처리량을 최대 2배로 늘려준다고 하니 10.6.10 버전을 사용해 보자.
- 프리 티어로 선택.

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-rds/1.png 'Result')

.

- 마스터 사용자 이름과 암호는 까먹지 않도록 잘 기록해 두자.
- 인스턴스는 db.t2.micro.

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-rds/2.png 'Result')

.

- 프리 티어에서는 20GB 까지 사용 가능.
- 과금 방지를 위해 스토리지 자동 조정 활성화 OFF

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-rds/3.png 'Result')

.

- EC2 컴퓨팅 리소스는 나중에 설정이 가능하니 편한 방식을 선택.
- 기본 VPC
- 기본 서브넷 그룹
- 퍼블릭 액세스 허용
- VPC 보안 그룹 새로 생성

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-rds/4.png 'Result')

- 초기 데이터베이스 이름을 설정해 두는게 편하다.
- 과금 방지를 위해 자동 백업 활성화 OFF
- 과금 방지를 위해 마이너 버전 자동 업그레이드 사용 OFF

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-rds/5.png 'Result')

.

RDS 생성은 생각보다 간단하지만 과금 방지를 위한 몇 가지 설정들만 잘 체크해 주면 될 것 같다.

나머지 
- `보안 그룹 생성`
- `RDS 접근 확인`
- `파라미터 그룹 생성`
- `EC2 에서 RDS 접근` 
  
은 [향로님의 블로그](https://jojoldu.tistory.com/259)를 참고해 보자.

.

### EC2 Amazon Linux 2023 MySQL 설치

AMI(Amazon Machine Image)로 EC2 Amazon Linux AMI 2023 를 선택했었는데,

커맨드 라인 사용을 위해 mysql 을 설치하는 과정에서 `sudo yum install mysql` 명령어를 사용해도 설치가 제대로 되지 않고

`mysql: command not found` 라는 말만 할 뿐이었다..

여러 서칭을 하면서 *Amazon Linux 2023의 경우 EL9 버전의 레파지토리와 mysql-community-sever를 설치해야 한다.* 는 글을 보게 되었고,

아래 명령어로 MySQL 설치 문제를 해결할 수 있었다.

```shell
$ sudo dnf install https://dev.mysql.com/get/mysql80-community-release-el9-1.noarch.rpm
$ sudo dnf install mysql-community-server
```

> [EC2 mysql 패키지 설치 오류](https://hyunki99.tistory.com/102)
>
> [EC2 mysql: command not found](https://velog.io/@miracle-21/EC2mysql-command-not-found)

.

### Set Timezone

**`EC2`**

```shell
# check timezone
$ date

# change timezone
$ sudo rm /etc/localtime
$ sudo ln -s /usr/share/zoneinfo/Asia/Seoul /etc/localtime
```

최종 반영을 위해 재부팅을 해주자.

.

**`RDS`**

(1) RDS -> 파라미터 그룹 -> RDS 파라미터 그룹 선택 후 편집

(2) 파라미터 필터에 `time_zone` 검색

(3) time_zone 값을 `Asia/Seoul` 로 설정

(4) 변경 사항 저장

(5) DB 툴을 통해 아래 쿼리 수행

  ```sql
  select @@time_zone, now();
  ```

  - @@time_zone : Asia/Seoul

.

## EC2에 배포하기

EC2 배포 방법도 [향로님의 블로그](https://jojoldu.tistory.com/263)에 상세하게 설명이 되어 있다.

향로님의 블로그를 참고하며 추가로 필요한 내용들을 확인해 보자.

.

### Install Java 17

```shell
# Install java
$ yum list java*  # 설치 가능한 java 조회
$ sudo yum install java-17-amazon-corretto
$ java -version

# java 설치 위치 확인
$ echo $JAVA_HOME # 현재 JAVA_HOME 확인
$ which java # java 설치 위치 확인
$ ls -l /usr/bin/java # which java 경로
$ readlink -f /usr/bin/java # 심볼릭 링크의 java 원본 위치

# 환경변수 설정
$ sudo vi /etc/profile

export JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto.x86_64

$ source /etc/profile

# 설정 확인
$ echo $JAVA_HOME
$ $JAVA_HOME -version
```

- `/etc/profile` : 모든 사용자에 적용
- `~/.bashrc` : 해당 사용자에게만 적용

> [EC2 Java 설치 및 JAVA_HOME 설정](https://happy-jjang-a.tistory.com/57)

.

### Install Git And Clone

```shell
# Install git
$ sudo yum install git
$ git --version

# git project directory
$ mkdir app
$ mkdir app/git
$ cd ~/app/git

# clone git repository
$ git clone https://github.com/프로젝트주소.git
$ git branch -a # 현재 브랜치 확인
$ ./gradlew test # gradle 테스트 수행
```

.

### Personal access tokens 로 로그인

git clone 명령을 수행했다면 github 로그인이 필요한데 비밀번호로는 인증이 실패할 것이다.

[Git password authentication is shutting down](https://github.blog/changelog/2021-08-12-git-password-authentication-is-shutting-down/)

```
2021년 8월 13일부터 Git 작업을 인증할 때 비밀번호를 입력하는 방식은 만료되었고, 토큰 기반 인증(ex. personal access, OAuth, SSH Key, or GitHub App installation token)이 필요하게 되었다.
Personal Access Token 을 활용하여 로그인해보자.
```

> [Github Token 방식으로 로그인하기](https://velog.io/@shin6949/Github-Token-%EB%B0%A9%EC%8B%9D%EC%9C%BC%EB%A1%9C-%EB%A1%9C%EA%B7%B8%EC%9D%B8%ED%95%98%EA%B8%B0-ch3ra7vc)

.

### Git 자동 로그인

git 자동 로그인 설정을 해두지 않으면 매번 git 작업 때마다 로그인이 필요할 것이다.

```shell
$ git config credential.helper store
$ git push https://github.com/jihunparkme/jihunparkme.github.io.git # 자동 로그인이 필요한 저장소 주소
```

> [Github 자동 로그인 설정](https://daechu.tistory.com/33)

.

### EC2 프리티어 메모리 부족현상 해결

`./gradlew test` 명령을 수행했다면 EC2 가 먹통이 되는 현상을 마주하였을 것이다..

t2.micro RAM 이 1GB 뿐이라서 그렇다..

다행히도 AWS 에서는 HDD 의 일정 공간을 마치 RAM 처럼 사용할 수 있도록 SWAP 메모리를 지정할 수 있게 해준다.

[How do I allocate memory to work as swap space in an Amazon EC2 instance by using a swap file?](https://repost.aws/ko/knowledge-center/ec2-memory-swap-file)

```shell
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

> [AWS EC2 프리티어에서 메모리 부족현상 해결방법](https://sundries-in-myidea.tistory.com/102)

.

### 배포 스크립트

```shell
$ cd ~/app/git/
$ vi deploy.sh
```

**deploy.sh**

```shell
#!/bin/bash

REPOSITORY=/home/ec2-user/app/git

cd $REPOSITORY/my-webservice-site/

echo "> Git Pull"
git pull

echo "> start Build Project"
./gradlew build

echo "> copy build jar File"
cp ./build/libs/*.jar $REPOSITORY/deploy/

echo "> Check the currently running application PID"
CURRENT_PID=$(pgrep -f my-webservice)

echo "$CURRENT_PID"

if [ -z $CURRENT_PID ]; then
    echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

echo "> Deploy a new application"
JAR_NAME=$(ls $REPOSITORY/deploy/ | grep 'my-webservice' | tail -n 1)

echo "> JAR Name: $JAR_NAME"
nohup java -jar $REPOSITORY/deploy/$JAR_NAME > $REPOSITORY/deploy/deploy.log 2>&1 &
```

**스크립트 실행**

```shell
# 스크립트 실행 권한 추가
$ chmod 755 ./deploy.sh

# 스크립트 실행
$ ./deploy.sh

# 실행중인 프로세스 확인
$ ps -ef | grep my-webservice
```
.

💡 Plain archive
- Spring Boot 2.5.0 부터 jar 파일 생성 시 `xxx-plain.jar` 파일을 같이 생성
- `Plain archive`(xxx-plain.jar) 는 애플리케이션 실행에 필요한 모든 의존성을 포함하지 않고, 작성된 소스코드의 클래스 파일과 리소스 파일만 포함하여 실행 불가능한 상태
- 반면, `Executable archive`(.jar) 는 모든 의존성을 포함하여 실행 가능한 상태
- [Spring Boot 2.5.0 generates plain.jar file. Can I remove it?](https://stackoverflow.com/questions/67663728/spring-boot-2-5-0-generates-plain-jar-file-can-i-remove-it)
- build.gradle
  
  ```groovy
  jar {
      enabled = false
  }
  ```

.

### 외부에서 서비스 접속

EC2 에 배포된 서비스의 포트 번호가 외부에서 접근 가능하도록 설정이 필요

AWS EC2 인스턴스 페이지 -> 보안그룹 -> 현재 프로젝트의 인스턴스 -> 인바운드 탭
- 인바운드 편집 버튼을 클릭해서 사용자지정(TCP), 8080 포트를 추가
- `[퍼블릭 IPv4 DNS]:8080` 주소로 접속

> [EC2에 배포하기](https://jojoldu.tistory.com/263)

.

## Docker & Jenkins 배포 자동화 구축

### Install Docker

```shell
# Install
$ sudo yum update -y # 인스턴스에 있는 패키지 업데이트
$ sudo yum install -y docker # docker 설치
$ docker -v # 버전 확인

# Setting
$ sudo systemctl enable docker.service # 재부팅 시 docker 자동 실행 설정
$ docker update --restart=always [Container ID]

# Start
$ sudo systemctl start docker.service # docker 서비스 실행
$ systemctl status docker.service # docker 서비스 상태 확인
```

.

### Install Jenkins

**docker search, pull 권한 에러 발생 시 권한 설정**

`permission denied while trying to connect to the Docker daemon socket at ...`

[Got permission denied while trying to connect to the Docker daemon socket](https://technote.kr/369)

```shell
# 접근을 위해 root:docker 권한 필요
$ ls -al /var/run/docker.sock 

# 현재 로그인한 사용자를 docker group 에 포함
$ sudo usermod -a -G docker $USER 

# EC2 인스턴스 재구동 후 해당 ID 에 docker 
# group 권한 부여 확인
$ id

# 다시 docker pull 시도
$ docker pull jenkins/jenkins:lts
```

.

Install jenkins image in docker

```shell
$ docker search jenkins # search image
$ docker pull jenkins/jenkins:lts # docker image 가져오기
$ docker images # 설치된 jenkins image 확인 
```

.

Create jenkins Container

```shell
$ docker run -itd -p 8000:8080 --restart=always --name jenkins -u root jenkins/jenkins:lts

$ docker ps # 실행중인 docker 확인
$ docker exec -it --user root 'Container ID' /bin/bash # jenkins container 진입

# etc docker command
$ docker stop [Container ID] # Stop container
$ docker container restart [Container ID] # Restart container
$ docker rm [Container ID]
```

-p 8000:8080
- 8080 포트는 webservice 에 이미 사용중이므로 8000 포트 사용
- 컨테이너 외부와 통신할 8000 포트와 내부적으로 사용할 8080 포트 설정

8000 포트 번호도 외부에서 접근 가능하도록 설정 필요
- AWS EC2 인스턴스 페이지 -> 보안그룹 -> 현재 프로젝트의 인스턴스 -> 인바운드 탭
- 인바운드 편집 버튼을 클릭해서 사용자지정(TCP), 8000 포트를 추가

exit Shell
- exit : `Ctrl + d`
- 백그라운드 종료 : `ctrl + p + q`

> [docker run 커맨드 사용법](https://www.daleseo.com/docker-run/)

.

### Init Jenkins

Jenkins 초기 패스워드 확인

```shell
cd /var/jenkins_home/secrets/
cat initialAdminPassword
```

`[퍼블릭 IPv4 DNS]:8000` 주소로 접속하여 Jenkins 설정 시작

- 초기 화면에서 initialAdminPassword 에 저장된 초기 패스워드 입력

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/1.png 'Result')

- Install Suggested plugins 선택

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/2.png 'Result')

- Getting Started..

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/3.png 'Result')

- Create First Admin User 설정

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/4.png 'Result')

.

### Jenkins 자동 배포 설정

**`GitHub Repository 설정`**

Personal access tokens 생성
- Settings / Developer Settings / Personal access tokens (classic)
- github 인증 단계에서 만들어둔 토큰을 사용하자.

Webhook 설정
- Repository -> Settings -> Webhooks -> Add webhook
- Payload URL: http://[퍼블릭 IPv4 DNS]:8000/github-webhook/

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/5.png 'Result')

.

**`Jenkins 설정`**

Install Publish Over SSH
- Jenkins Main 관리 -> Jenkins 관리 -> Plugins -> Available plugins
- Publish Over SSH 검색 후 Install

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/10.png 'Result')

Add Credentials
- Jenkins 관리 -> System -> GitHub -> GitHub Servers -> Add GitHub Serves -> Add Credentials

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/6.png 'Result')

- Credentials 생성
  - Domain: Global credentials
  - Kind: Username with password
  - Scope: Global
  - Username: Github Login ID
  - Password: Personal access token
  - ID: credential 설정 ID

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/7.png 'Result')

Publish over SSH
- Key: ec2 접속 시 사용했던 .pem 파일 내용

SSH Servers
- Name: `SSH Server Name`
- Hostname: `ec2 서버 IP`
  - 접속할 원격 서버 IP
- Username : `ec2-user`
  - 원격 서버의 user-name
- Remote Directory: `/home/ec2-user`
  - 원격서버 작업 디렉토리

.

**`Tomezone 설정`**

- 우측 상단 로그아웃 좌측 계정명을 클릭하여 사용자 설정으로 이동
- 설정 -> User Defined Time Zone -> `Asia/Seoul`

.

### Add Jenkins Item

Item 추가
- 새로운 Item
- Freestyle project 선택

소스 코드 관리 / Git
- Repository URL: ec2 에 배포된 Repository URL
- Credentials: Add Credentials 단계에서 추가한 credentials 선택
- Branches to build: 빌드할 브랜치(*/main, */master, */dev ..)

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/8.png 'Result')

빌드 유발
- GitHub hook trigger for GITScm polling

Build Steps
- Execute shell
- `./gradlew clean build` 입력

빌드 후 조치
- Send build artifacts over SSH
- Name: SSH Server Name
- Source files: `build/libs/*.jar`
  - ec2 서버에 전달할 jar 파일 경로
  - jenkins 서버에서 절대 경로는 /var/jenkins_home/workspace/REPOSITORY_NAME/build/libs
  - Jenkins workspace 기준인 REPOSITORY_NAME 이후부터 작성
- Remove prefix: `build/libs`
  - Source files 경로에서 .jar 파일을 제외한 prefix 를 제거한 경로
- Remote directory: `/app/git/deploy`
  - jenkins 서버에서 빌드된 jar 파일을 받을 ec2 경로
  - SSH Servers Remote Directory 경로 이후 경로 작성
- Exec command: sh /home/ec2-user/app/git/jenkins-deploy.sh > /dev/null 2>&1
  - jenkins -> ec2 로 jar 파일을 전달한 이후 ec2 에서 실행할 명령어

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/9.png 'Result')

Create Jenkins Deploy Shell in ec2
- REPOSITORY 에 push 동작이 발생하거나, 젠킨스에서 만든 Item 에서 지금 빌드 동작이 발생할 경우,
- REPOSITORY 의 코드를 빌드하고, jar 파일을 ssh 연결된 ec2 서버에 전달한 뒤 해당 shell 파일 실행

```shell
REPOSITORY=/home/ec2-user/app/git

echo "> 현재 구동중인 애플리케이션 pid 확인"
CURRENT_PID=$(pgrep -f my-webservice)

echo "$CURRENT_PID"

if [ -z $CURRENT_PID ]; then
    echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

echo "> 새 어플리케이션 배포"

JAR_NAME=$(ls $REPOSITORY/deploy/ | grep 'my-webservice' | tail -n 1)

echo "> JAR Name: $JAR_NAME"

nohup java -jar $REPOSITORY/deploy/$JAR_NAME > $REPOSITORY/deploy/deploy.log 2>&1 &
```

22 번 포트를 외부에서 접근 가능하도록 설정 필요
- AWS EC2 인스턴스 페이지 -> 보안그룹 -> 현재 프로젝트의 인스턴스 -> 인바운드 탭
- 인바운드 편집 버튼을 클릭해서 SSH 22 포트 오픈

production yml 파일
- jenkins 빌드 이후에 gitignore 에 등록된 파일 적용이 제대로 되지 않을 것이다.
- 외부에 공개되지 않도록 gitignore 에 포함된 파일은 jsnkins 서버에 따로 추가가 필요
- jenkins Docker 에서 `vi : command not found` 오류 해결

```shell
$ apt-get update
$ apt-get install vim

$ cd /var/jenkins_home/workspace/REPOSITORY_NAME/src/main/resources
$ vi application-real.yml
$ chmod 755 application-real.yml
```

다시 빌드해 보면 gitignore 에 등록된 파일이 적용된 상태로 빌드가 된다.
- Send build artifacts over SSH 에 작성한 jenkins 서버의 `Source files` 경로에 있는 jar 파일들이
- ec2 서버의 `Remote directory` 경로로 잘 전달된 것을 볼 수 있다.

```shell
Auth fail for methods 'publickey,gssapi-keyex,gssapi-with-mic'
```
- 만일 Jenkins 에서 ec2 로 ssh 접근이 실패할 경우 아래 링크를 참고해보자.
- [How to Fix SSH Failed Permission Denied (publickey,gssapi-keyex,gssapi-with-mic)](https://phoenixnap.com/kb/ssh-permission-denied-publickey)
- [Failed to connect and initialize SSH connection Message [Auth fail]](https://stackoverflow.com/questions/65015826/jenkins-plugins-publish-over-bappublisherexception-failed-to-connect-and-initia/74567611#74567611)

> [Docker + Jenkins 자동 배포](https://velog.io/@wijoonwu/AWS-Jenkins-%EC%9E%90%EB%8F%99-%EB%B0%B0%ED%8F%AC)

.

## Nginx 무중단 배포

### Install Nginx
```shell
# 도커 이미지 가져오기
$ docker pull nginx

# nginx 서버 기동
$ docker run -itd -p 80:80 -v /home/ec2-user/app/nginx:/usr/share/nginx/conf --restart=always --name nginx -u root nginx

# 가동 서비스 확인
$ docker ps
```

[퍼블릭 IPv4 DNS] 로 접속을 하면 아래와 같이 nginx 가 우리를 반겨주고 있다.

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/nginx/1.png 'Result')

```shell
# nginx container 진입
$ docker exec -it --user root [Container ID] /bin/bash 

# 설정 파일인 nginx.conf 하단을 보면 아래 경로의 conf 파일들을 include 해주고 있다.
# include /etc/nginx/conf.d/*.conf;
$ vi /etc/nginx/nginx.conf

# default.conf 파일 수정
$ vi /etc/nginx/conf.d/default.conf

# server 아래의 location / 부분을 찾아서 아래와 같이 추가
proxy_pass http://[Elastic IP]:8080;
proxy_set_header X-Real-IP $remote_addr;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
proxy_set_header Host $http_host;

# 수정 후 docker 재시작
$ docker container restart [Container ID]
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/nginx/2.png 'Result')

- proxy_pass: `/` 요청이 오면 `http://[EC2_PUBLIC_DNS]:8080` 로 전달
- proxy_set_header XXX $xxx: 실제 요청 데이터를 header 각 항목에 할당
  - proxy_set_header X-Real-IP $remote_addr: Request Header X-Real-IP 에 요청자 IP 저장

### 배포 스크립트 작성

무중단 배포를 위해 두 개의 서비스를 띄워야 한다.

먼저, 무중단 배포에 필요한 Prifile 을 작성해 보자.

```yml
---
spring:
  profiles: set1
server:
  port: 8081

---
spring:
  profiles: set2

server:
  port: 8082
```

.

다음으로 무중단 배포 스크립트 동작 확인을 위해 기존 jar 파일을 복사해보자.

```shell
# 무중단 배포 관련 파일을 관리할 디렉토리
$ mkdir ~/app/nonstop
```

.

`무중단 배포 스프립트`
- 스트립트 안에서 오류가 발생할 수도 있으니 전체를 실행하기 전에 커멘드 단위로 실행해 보자.

💡 spring-boot-starter-actuator
- 스크립트에서 Health check(http://localhost:$IDLE_PORT/health) 를 하는 부분이 있는데 해당 기능을 사용하기 위해 의존성이 필요하다.
- 추가로 actuator 는 스프링부트 프로젝트의 여러 상태를 확인할 수 있다보니 안전하게 사용하는 것도 중요하다.
- [Actuator 안전하게 사용하기](https://techblog.woowahan.com/9232/)
- Actuator 보안 대책이 반영된 actuator 설정 예시
  - ec2 보안 그룹에서 actuator 포트를 열어주어야 한다.

  ```yml
  management:
    server:
      port: 1234
    endpoints:
      info:
        enabled: true
      health:
        enabled: true
      jmx:
        exposure:
          exclude: "*"
      web:
        exposure:
          include: info, health
        base-path: /abcdefg/actuator
  ```

```shell
$ vi ~/app/nonstop/deploy.sh

#!/bin/bash

BASE_PATH=/home/ec2-user/app/nonstop
BUILD_PATH=$(ls /home/ec2-user/app/git/deploy/*.jar) 
JAR_NAME=$(basename $BUILD_PATH)
echo "> build file name: $JAR_NAME"

echo "--- Copy build file"
DEPLOY_PATH=$BASE_PATH/jar/
cp $BUILD_PATH $DEPLOY_PATH

echo "================================"

echo "> Check the currently running Set"
CURRENT_PROFILE=$(curl -s http://localhost/profile)
echo "--- $CURRENT_PROFILE"

# Find a resting set
if [ $CURRENT_PROFILE == set1 ]
then
  IDLE_PROFILE=set2
  IDLE_PORT=8082
  IDLE_ACTUATOR=2222
elif [ $CURRENT_PROFILE == set2 ]
then
  IDLE_PROFILE=set1
  IDLE_PORT=8081
  IDLE_ACTUATOR=1111
else
  echo "> Not found Profile. Profile: $CURRENT_PROFILE"
  echo "> assign set1. IDLE_PROFILE: set1"
  IDLE_PROFILE=set1
  IDLE_PORT=8081
  IDLE_ACTUATOR=1111
fi

echo "================================"

echo "> change application.jar"
IDLE_APPLICATION=$IDLE_PROFILE-my-webservice.jar
IDLE_APPLICATION_PATH=$DEPLOY_PATH$IDLE_APPLICATION

ln -Tfs $DEPLOY_PATH$JAR_NAME $IDLE_APPLICATION_PATH

echo "================================"

echo "> Check the application PID running in $IDLE_PROFILE"
IDLE_PID=$(pgrep -f $IDLE_APPLICATION)

if [ -z $IDLE_PID ]
then
  echo "--- 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
  echo "--- kill -15 $IDLE_PID"
  kill -15 $IDLE_PID
  sleep 5
fi

echo "================================"

echo "> Deploy $IDLE_PROFILE"
nohup java -jar -Dspring.profiles.active=$IDLE_PROFILE $IDLE_APPLICATION_PATH > $BASE_PATH/deploy-$IDLE_PROFILE.log 2>&1 &

echo "> $IDLE_PROFILE 10초 후 Health check 시작"
echo "--- curl -s http://localhost:$IDLE_ACTUATOR/abcdefg/actuator/health"
sleep 10

for retry_count in {1..10}
do
  response=$(curl -s http://localhost:$IDLE_ACTUATOR/abcdefg/actuator/health)
  up_count=$(echo $response | grep 'UP' | wc -l)

  if [ $up_count -ge 1 ]
  then # $up_count >= 1 ("UP" 문자열이 있는지 검증)
      echo "> Health check 성공"
      break
  else
      echo "--- Health check 의 응답을 알 수 없거나 혹은 status 가 UP이 아닙니다."
      echo "--- Health check: ${response}"
  fi

  if [ $retry_count -eq 10 ]
  then
    echo "> Health check 실패. "
    echo "> Nginx 에 연결하지 않고 배포를 종료합니다."
    exit 1
  fi

  echo "> Health check 연결 실패. 재시도..."
  sleep 10
done
```

.

`무중단 배포 스크립트 실행 확인`
- Health check 성공까지 잘 동작하는지 확인해보자.

```shell
# 스크립트 실행 권한 추가
$ chmod 755 ./deploy.sh

$ ~/app/nonstop/deploy.sh
```

.

### 동적 프록시 설정

배포가 완료되고 애플리케이션이 실행되면 Nginx 가 기존에 바라보던 Profile 의 반대를 보도록 변경해주자.

```shell
# nginx container 진입
$ docker exec -it --user root [Container ID] /bin/bash 

# service-url 관리 파일 생성
$ vi /usr/share/nginx/conf/service-url.inc

set $service_url http://[Elastic IP]:8080;

# proxy_pass 수정
$ vi /etc/nginx/conf.d/default.conf

include /usr/share/nginx/conf/service-url.inc;

location / {
        proxy_pass $service_url;
...

# 수정 후 docker 재시작
$ docker container restart [Container ID]

# Nginx 로 요청해서 현재 Profile 확인
$ curl -s localhost/profile
```

.

`Nginx 스크립트 작성`

```shell
$ vi ~/app/nonstop/switch.sh

#!/bin/bash

echo "> Check the currently running Port"
CURRENT_PROFILE=$(curl -s http://localhost/profile)

if [ $CURRENT_PROFILE == set1 ]
then
  IDLE_PORT=8082
elif [ $CURRENT_PROFILE == set2 ]
then
  IDLE_PORT=8081
else
  echo "--- 일치하는 Profile이 없습니다. Profile: $CURRENT_PROFILE"
  echo "--- 8081을 할당합니다."
  IDLE_PORT=8081
fi

echo "================================"

echo "> 전환할 Port: $IDLE_PORT"
echo "--- Port 전환"
echo "set \$service_url http://[Elastic IP]:${IDLE_PORT};" | sudo tee /home/ec2-user/app/nginx/service-url.inc

echo "================================"

PROXY_PORT=$(curl -s http://localhost/profile)
echo "> Nginx Current Proxy Port: $PROXY_PORT"

echo "> Nginx Container Reload"
NGINX_CONTAINER_ID=$(docker container ls --all --quiet --filter "name=nginx")
docker container restart $NGINX_CONTAINER_ID
```

.

`Nginx 스크립트 적용`

```shell
# 실행 권한 추가
$ chmod 755 ~/app/nonstop/switch.sh

# 무중단 배포 스크립트 하단에 nginx switch 스크립트 실행 명령 추가
$ vi ~/app/nonstop/deploy.sh

echo "> 스위칭"
sleep 10
/home/ec2-user/app/nonstop/switch.sh
```

.

### Jenkins 에 적용

구성 -> 빌드 후 조치 -> Exec command
~/app/nonstop/deploy.sh  > /dev/null 2>&1 를 실행하도록.

sh /home/ec2-user/app/nonstop/deploy.sh  > /dev/null 2>&1

.

## Domain, HTTPS

[향로님의 블로그](https://jojoldu.tistory.com/259) 에서 아래 내용을 다루는 글이 있는데 아래 설정들도 추가해 보면 좋을 것 같다.

- 도메인 및 서비스 메일 생성
- EC2 와 도메인 연결
- Google 이메일 연결
- HTTPS 연결

.

> [Nginx를 활용한 무중단 배포 구축](https://jojoldu.tistory.com/267?category=635883)

---

Gitlab + Jenkins + Nginx + Docker + AWS EC2 - 무중단 배포

- https://gksdudrb922.tistory.com/236

운영 환경 설정

- https://jojoldu.tistory.com/269?category=635883

도메인구매, HTTPS 연결, 타임존 수정

- https://jojoldu.tistory.com/270?category=635883