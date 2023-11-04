# AWS EC2 & RDS Free Tier 구축

System Architecture

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-ec2/system-architecture.png 'Result')

.

## AWS EC2 & RDS 구축

AWS EC2 & RDS 구축 방법은 [향로님의 블로그](https://jojoldu.tistory.com/259) 가 참고하기 좋은 것 같다. 
- 2023년 10월 기준 UI 가 블로그 이미지와 약간 다르긴 하지만 기본적인 설정은 동일하다.

.

그 밖에도 자세한 구축 방법은 많은 블로그에서 다루고 있어서 구축 과정에서 참고하면 좋을 내용들을 다뤄보려고 한다.

.

## AWS 프리티어 무료 사용량

`EC2`(Amazon Elastic Compute Cloud) : 클라우드에서 제공되는 크기 조정 가능한 컴퓨팅
- 월별 750시간 무료 (EC2 인스턴스 하나를 풀로 돌려도 남는 시간)
  - EC2 인스턴스 하나당 750시간 가동이 기준이므로, EC2 인스턴스 두 개를 돌린다면 375시간만(15일) 무료. 세 개일 경우 250시간(10일) 무료 적용.
  - 프리티어에서 다수의 인스턴스를 풀로 돌릴경우 월별 무료 사용량이 금방 제한되어 과금이 되니 학습이 끝나면 항상 인스턴스를 종료 혹은 중지해 주자.
- 리전에 따라 Linux/Windows 운영체제의 t2.micro 또는 t3.micro 인스턴스 타입만 무료

.

`RDS`(Amazon Relational Database Service) : MySQL, PostgreSQL, MariaDB 또는 SQL Server 등을 위한 관계형 데이터베이스 서비스
- RDS 인스턴스 한 개 무료 사용 가능
- 월별 750시간까지 무료
  - 단, db.t2.micro, db.t3.micro, db.t4g.micro 타입만 사용 가능
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

## EC2 인스턴스 생성

- EC2 OS : Amazon Linux([Amazon Linux 2023](https://aws.amazon.com/ko/linux/amazon-linux-2023/) AMI).
- 인스턴스 유형: t2.micro.

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-ec2/1.png 'Result')

.

- 키 페어는 ec2 인스턴스에 ssh 로 접근하기 위해 필요하므로 안전한 보관이 필요하다.
  - 개인키 파일은 .pem 타입으로 다운을 받아서 잘 저장해 두자.
  - 공개키는 ec2 인스턴스의 /home/ec2-user/.ssh/authorized_keys 파일에서 확인할 수 있다.

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-ec2/4.png 'Result')

.

- VPC, 서브넷: default.
- 퍼블릭 IP 자동 할당: 활성화.
- 보안 그룹 구성.
  - AWS EC2 터미널 접속을 위한 ssh 22 포트는 외부 접근 차단을 위해 내 IP 를 선택하자.
    - 다른 장소에서 접속 시 해당 장소의 IP 를 다시 SSH 규칙에 추가하는 것이 안전하다.
    - 나중에는 jenkins docker container 에서 ec2 ssh 접근을 위해 오픈하였지만..
    - 처음에는 안전하게 내 IP 로 설정해 두자.
  - HTTPS(443), HTTP(80) 는 외부에서 웹서비스 접근을 위해 사용하므로 포트를 열어두자.

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-ec2/2.png 'Result')

.

- 볼륨 크기 : 30GB (30GB 까지 프리티어로 사용 가능).
- [범용 SSD 스토리지](https://docs.aws.amazon.com/ko_kr/AmazonRDS/latest/UserGuide/CHAP_Storage.html#Concepts.Storage.GeneralSSD).
  - [아마존 EBS 볼륨 유형 (gp2 및 gp3) 비교](https://docs.aws.amazon.com/ko_kr/emr/latest/ManagementGuide/emr-plan-storage-compare-volume-types.html).

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-ec2/3.png 'Result')

.

나머지 
- `AWS EC2 고정 IP(Elastic IP) 등록`
- `EC2 터미널 접속`

은 [향로님의 블로그](https://jojoldu.tistory.com/259)를 참고해 보자.

.

## RDS 생성

- 표준 생성.
- MariaDB.
- 추가 비용 없이 쓰기 처리량을 최대 2배로 늘려준다고 하니 MariaDB 10.6.10 버전을 사용해 보자.
- 프리 티어로 선택.

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-rds/1.png 'Result')

.

- 마스터 사용자 이름과 암호는 까먹지 않도록 잘 기록해 두자.
- 인스턴스는 db.t2.micro.

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-rds/2.png 'Result')

.

- 프리 티어에서는 20GB 까지 사용 가능.
- 과금 방지를 위해 스토리지 자동 조정 활성화 OFF.

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-rds/3.png 'Result')

.

- EC2 컴퓨팅 리소스는 나중에도 설정이 가능하니 편한 방식을 선택.
- 기본 VPC.
- 기본 서브넷 그룹.
- 퍼블릭 액세스 허용.
- VPC 보안 그룹 새로 생성.
- 가용 영역 기본 설정 없음.
- 인증 기관 기본값

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-rds/4.png 'Result')

- 초기 데이터베이스 이름을 설정해 두는 게 편하다.
- 과금 방지를 위해 자동 백업 활성화 OFF.
- 과금 방지를 위해 마이너 버전 자동 업그레이드 사용 OFF.

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

## EC2 Amazon Linux 2023 MySQL 설치

AMI(Amazon Machine Image)로 EC2 Amazon Linux 2023 AMI 를 선택했었는데,

커맨드 라인 사용을 위해 mysql 을 설치하는 과정에서 `sudo yum install mysql` 명령어를 사용해도 설치가 제대로 되지 않고

`mysql: command not found` 라는 문구만 나올 뿐이었다..

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

## Set Timezone

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

  - @@time_zone : Asia/Seoul 확인

.

## EC2에 배포하기

EC2 배포 방법도 [향로님의 블로그](https://jojoldu.tistory.com/263)에 상세하게 설명이 되어 있다.

향로님의 블로그를 참고하며 추가로 필요한 내용들을 정리해 보자.

.

## Install Java 17

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

## Install Git And Clone

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

## Personal access tokens 로 로그인

git clone 명령을 수행했다면 github 로그인이 필요한데 비밀번호로는 인증이 실패할 것이다.

[Git password authentication is shutting down](https://github.blog/changelog/2021-08-12-git-password-authentication-is-shutting-down/)

```
2021년 8월 13일부터 Git 작업을 인증할 때 비밀번호를 입력하는 방식은 만료되었고, 토큰 기반 인증(ex. personal access, OAuth, SSH Key, or GitHub App installation token)이 필요하게 되었다.
Personal Access Token 을 활용하여 로그인해 보자.
```

[Github Token 방식으로 로그인하기](https://velog.io/@shin6949/Github-Token-%EB%B0%A9%EC%8B%9D%EC%9C%BC%EB%A1%9C-%EB%A1%9C%EA%B7%B8%EC%9D%B8%ED%95%98%EA%B8%B0-ch3ra7vc) 를 참고하여 Personal Access Token 을 생성하고 비밀번호대신 해당 토큰을 사용하여 다시 로그인해 보자.

.

## Git 자동 로그인

git 자동 로그인 설정을 해두지 않으면 매번 git 작업 때마다 로그인이 필요할 것이다.

```shell
$ git config credential.helper store

# 자동 로그인이 필요한 저장소 주소 입력
$ git push https://github.com/jihunparkme/jihunparkme.github.io.git 
```

> [Github 자동 로그인 설정](https://daechu.tistory.com/33)

.

## EC2 프리티어 메모리 부족현상 해결

`./gradlew test` 명령을 수행했다면 EC2 가 먹통이 되는 현상을 마주하였을 것이다..

t2.micro RAM 이 1GB 뿐이라서 그렇다..

다행히도 AWS 에서는 HDD 의 일정 공간을 마치 RAM 처럼 사용할 수 있도록 SWAP 메모리를 지정할 수 있게 해 준다.

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

## 배포 스크립트 생성

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
- Spring Boot 2.5.0 부터 jar 파일 생성 시 `xxx-plain.jar` 파일을 같이 생성해 주고 있다.
- `Plain archive`(xxx-plain.jar) 는 애플리케이션 실행에 필요한 모든 의존성을 포함하지 않고, 작성된 소스코드의 클래스 파일과 리소스 파일만 포함하여 실행 불가능한 상태
- 반면, `Executable archive`(.jar) 는 모든 의존성을 포함하여 실행 가능한 상태
- [Spring Boot 2.5.0 generates plain.jar file. Can I remove it?](https://stackoverflow.com/questions/67663728/spring-boot-2-5-0-generates-plain-jar-file-can-i-remove-it)
- build.gradle 에 아래 설정을 통해 `xxx-plain.jar` 파일 생성을 제한할 수 있다.
  
  ```groovy
  jar {
      enabled = false
  }
  ```

.

## 외부에서 서비스 접속

EC2 에 배포된 서비스의 포트 번호가 외부에서 접근 가능하도록 설정이 필요하다.

EC2 생성 시 설정한 인바운드 보안 그룹 규칙과 동일하다.

.

AWS EC2 인스턴스 페이지 -> 보안그룹 -> 현재 인스턴스의 보안 그룹 선택 -> 인바운드 탭
- 인바운드 규칙 편집 버튼을 클릭해서 사용자지정(TCP), 8080 포트를 추가
- `[퍼블릭 IPv4 DNS]:8080` 주소로 접속 확인

> [EC2에 배포하기](https://jojoldu.tistory.com/263)