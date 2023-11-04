# Docker & Jenkins 배포 자동화 구축

System Architecture

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-ec2/system-architecture.png 'Result')

.

## Install Docker

```shell
# Install
$ sudo yum update -y # 인스턴스에 있는 패키지 업데이트
$ sudo yum install -y docker # docker 설치
$ docker -v # 버전 확인

# Setting
$ sudo systemctl enable docker.service # 재부팅 시 docker 자동 실행 설정

# Start
$ sudo systemctl start docker.service # docker 서비스 실행
$ systemctl status docker.service # docker 서비스 상태 확인
```

.

## Install Jenkins

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
$ docker exec -it --user root 'Container ID or Container name' /bin/bash # jenkins container shell 접속

# etc docker command
$ docker stop [Container ID] # Stop container
$ docker container restart [Container ID] # Restart container
$ docker rm [Container ID]
```

-itd
- i: t 옵션과 같이 사용. 표준입력 활성화. 컨테이너와 연결되어있지 않더라도 표준입력 유지
- t: i 옵션과 같이 사용. TTY 모드로 사용하며 bash 사용을 위해 반드시 필요
-d: 컨테이너를 백그라운드로 실행. 실행시킨 뒤 docker ps 명령어로 컨테이너 실행 확인 가능

-p 8000:8080
- 컨테이너 포트를 호스트와 연결
- 컨테이너 외부와 통신할 8000 포트와 컨테이너 내부적으로 사용할 8080 포트 설정
- 8080 포트는 webservice 에 이미 사용 중이므로 8000 포트 사용

--name
- 해당 컨테이너의 이름 설정
- 이름을 설정해 놓으면 컨테이너 id 외에도 해당 이름으로 컨테이너 설정 가능

Background Mode Docker Container
- 백그라운드 모드로 실행된 컨테이너에서 나오려면 백그라운드 종료 단축키를 사용해야 한다.
- Exit 단축키를 사용하여 컨테이너에서 나오면 컨테이너가 같이 종료되어 버린다.
  - 백그라운드 종료 : `ctrl + p + q`
  - Exit : `Ctrl + d`


.

AWS EC2 인스턴스에 8000 포트가 외부에서 접근 가능하도록 설정 필요
- AWS EC2 인스턴스 페이지 -> 보안그룹 -> 현재 인스턴스의 보안 그룹 선택 -> 인바운드 탭
- 인바운드 그룹 편집 버튼을 클릭해서 사용자지정(TCP), 8000 포트를 추가

> [docker run 커맨드 사용법](https://www.daleseo.com/docker-run/)

.

## Init Jenkins

Jenkins 초기 패스워드 확인

```shell
$ docker exec -it --user root 'Container ID or Container name' /bin/bash

cd /var/jenkins_home/secrets/
cat initialAdminPassword
```

.

`[Elastic IP]:8000` 주소로 접속하여 Jenkins 설정 시작

- 초기 화면에서 `initialAdminPassword` 에 저장된 초기 패스워드 입력

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/1.png 'Result')

- Install Suggested plugins 선택

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/2.png 'Result')

- Getting Started..

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/3.png 'Result')

- Create First Admin User 설정

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/4.png 'Result')

.

## Jenkins 자동 배포 설정

**`GitHub Repository 설정`**

Personal access tokens 생성
- Settings -> Developer Settings -> Personal access tokens (classic)
- EC2 github 인증 단계에서 만들어둔 토큰을 사용하자.

.

Webhook 설정
- Repository -> Settings -> Webhooks -> Add webhook
- Payload URL: `http://[Elastic IP]:8000/github-webhook/`

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/5.png 'Result')

.

**`Jenkins 설정`**

Install Publish Over SSH
- Jenkins Main 관리 -> Jenkins 관리 -> Plugins -> Available plugins
- Publish Over SSH 검색 후 Install

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/10.png 'Result')

.

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

.

Publish over SSH
- Key: ec2 접속 시 사용했던 .pem 공개키 입력

.

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

## Add Jenkins Item

Item 추가
- 새로운 Item
- Freestyle project 선택

.

소스 코드 관리 / Git
- Repository URL: `ec2 에 배포된 Repository URL`
- Credentials: `Add Credentials 단계에서 추가한 credentials 선택`
- Branches to build: `빌드할 브랜치`(*/main, */master, */dev ..)

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/8.png 'Result')

.

빌드 유발
- GitHub hook trigger for GITScm polling

.

Build Steps
- Execute shell
- `./gradlew clean build` 입력

.

빌드 후 조치
- Send build artifacts over SSH
- Name: SSH Server Name
- Source files: `build/libs/*.jar`
  - ec2 서버에 전달할 jenkins 서버에서의 jar 파일 경로
  - jenkins Source files 절대 경로: /var/jenkins_home/workspace/REPOSITORY_NAME/build/libs
  - Jenkins workspace 기준인 REPOSITORY_NAME 이후부터 작성
- Remove prefix: `build/libs`
  - Source files 경로에서 .jar 을 제외한 경로
- Remote directory: `/app/git/deploy`
  - jenkins 서버에서 빌드된 jar 파일을 전달받을 ec2 서버 경로
  - SSH Servers Remote Directory(/home/ec2-user) 이후 경로 작성
- Exec command: `sh /home/ec2-user/app/git/jenkins-deploy.sh > /dev/null 2>&1`
  - jenkins -> ec2 로 jar 파일을 전달한 이후 ec2 에서 실행할 명령어

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/jenkins/9.png 'Result')

.

Create Jenkins Deploy Shell in EC2
- REPOSITORY 에 git push 동작이 발생하거나, 젠킨스에서 만든 Item 에서 지금 빌드 동작이 발생할 경우,
- REPOSITORY 의 코드를 빌드하고, jar 파일을 ssh 로 연결된 ec2 서버에 전달한 뒤 해당 shell 파일 실행

```shell
# Repository 위치
REPOSITORY=/home/ec2-user/app/git

# 현재 구동중인 애플리케이션의 pid 확인
echo "> Check the currently running application PID"
CURRENT_PID=$(pgrep -f my-webservice)
echo "$CURRENT_PID"

if [ -z $CURRENT_PID ]; then
    echo "--- 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    # 기존에 기동중이던 애플리케이션를 종료
    echo "--- kill -15 $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

# jenkins 서버로부터 전달받은 jar 파일 이름 확인
echo "> 새 어플리케이션 배포"
JAR_NAME=$(ls $REPOSITORY/deploy/ | grep 'my-webservice' | tail -n 1)
echo "--- JAR Name: $JAR_NAME"

# jenkins 서버로부터 전달받은 jar 파일로 애플리케이션 구동
nohup java -jar $REPOSITORY/deploy/$JAR_NAME > $REPOSITORY/deploy/deploy.log 2>&1 &
```

jenkins 에서 ec2 인스턴스에 ssh 접속을 위해 22 번 포트를 외부에서 접근 가능하도록 설정 필요
- jenkins 에서 설정한 Send build artifacts over SSH 를 위한 설정
- AWS EC2 인스턴스 페이지 -> 보안그룹 -> 현재 인스턴스의 보안 그룹 선택 -> 인바운드 탭
- 인바운드 그룹 편집 버튼을 클릭해서 SSH 22 포트 오픈

.

production yml 파일
- jenkins 빌드 이후에 gitignore 에 등록된 파일 적용이 제대로 되지 않을 것이다.
- Repository 에 공개되지 않도록 gitignore 에 등록된 파일은 git clone 명령으로 받아올 수 없으므로 jenkins 서버에 따로 추가가 필요
- jenkins Docker 에서 `vi : command not found` 오류 해결

```shell
$ apt-get update
$ apt-get install vim

$ cd /var/jenkins_home/workspace/REPOSITORY_NAME/src/main/resources
$ vi application-real.yml
$ chmod 755 application-real.yml
```

.

다시 빌드해 보면 gitignore 에 등록된 파일이 적용된 상태로 빌드가 된다.
- Send build artifacts over SSH 에 작성한 jenkins 서버의 Source files(`build/libs/*.jar`) 경로에 있는 jar 파일들이
- EC2 서버의 Remote directory(`/app/git/deploy`) 경로로 잘 전달된 것을 확인할 수 있다.

.

참고..

```shell
Auth fail for methods 'publickey,gssapi-keyex,gssapi-with-mic'
```
- 22 번 포트를 오픈했음에도 Jenkins 에서 EC2 로 ssh 접근이 실패할 경우 아래 링크를 참고해보자.
- [How to Fix SSH Failed Permission Denied (publickey,gssapi-keyex,gssapi-with-mic)](https://phoenixnap.com/kb/ssh-permission-denied-publickey)
- [Failed to connect and initialize SSH connection Message [Auth fail]](https://stackoverflow.com/questions/65015826/jenkins-plugins-publish-over-bappublisherexception-failed-to-connect-and-initia/74567611#74567611)

> [Docker + Jenkins 자동 배포](https://velog.io/@wijoonwu/AWS-Jenkins-%EC%9E%90%EB%8F%99-%EB%B0%B0%ED%8F%AC)