# Nginx 무중단 배포

System Architecture

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/aws-ec2/system-architecture.png 'Result')

.

## Install Nginx

```shell
# 도커 이미지 가져오기
$ docker pull nginx

# nginx 서버 기동
$ docker run -itd -p 80:80 -v /home/ec2-user/app/nginx:/usr/share/nginx/conf --restart=always --name nginx -u root nginx

# 가동 서비스 확인
$ docker ps
```

-itd
- i: t 옵션과 같이 사용. 표준입력 활성화. 컨테이너와 연결되어있지 않더라도 표준입력 유지
- t: i 옵션과 같이 사용. TTY 모드로 사용하며 bash 사용을 위해 반드시 필요
- d: 컨테이너를 백그라운드로 실행. 실행시킨 뒤 docker ps 명령어로 컨테이너 실행 확인 가능

-p 80:80
- 컨테이너 포트를 호스트와 연결
- 컨테이너 외부와 통신할 80 포트와 컨테이너 내부적으로 사용할 80 포트 설정

-v /home/ec2-user/app/nginx:/usr/share/nginx/conf
- 컨테이너가 볼륨을 사용하기 위해서는 볼륨을 컨테이너에 마운트
- /home/ec2-user/app/nginx 볼륨을 컨테이너의 /usr/share/nginx/conf 경로에 마운트

--restart=always
- 도커 실행 시 재시작 정책 설정

--name
- 해당 컨테이너의 이름 설정
- 이름을 설정해 놓으면 컨테이너 id 외에도 해당 이름으로 컨테이너 설정 가능

Background Mode Docker Container
- 백그라운드 모드로 실행된 컨테이너에서 나오려면 백그라운드 종료 단축키를 사용해야 한다.
- Exit 단축키를 사용하여 컨테이너에서 나오면 컨테이너가 같이 종료되어 버린다.
  - 백그라운드 종료 : `ctrl + p + q`
  - Exit : `Ctrl + d`

.

[퍼블릭 IPv4 DNS] 로 접속을 하면 아래와 같이 nginx 가 우리를 반겨주고 있다.

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/nginx/1.png 'Result')

.

## Init Nginx

```shell
# nginx container 진입
$ docker exec -it --user root [Container ID] /bin/bash 

# 설정 파일인 nginx.conf 하단을 보면 /etc/nginx/conf.d 경로의 conf 파일들을 include 해주고 있다.
$ vi /etc/nginx/nginx.conf

...
include /etc/nginx/conf.d/*.conf;
...

# default.conf 파일 수정
$ vi /etc/nginx/conf.d/default.conf

# server 아래의 location / 부분을 찾아서 아래와 같이 추가
server {
    ...
    location / {
      proxy_pass http://[Elastic IP]:8080;
      proxy_set_header X-Real-IP $remote_addr;
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
      proxy_set_header Host $http_host;
    }
    ...

# 수정 후 docker 재시작
$ docker container restart [Container ID]
```

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/nginx/2.png 'Result')

- `proxy_pass` : `/` 요청이 오면 `http://[EC2_PUBLIC_DNS]:8080` 로 전달
- `proxy_set_header XXX $xxx` : 실제 요청 데이터를 header 각 항목에 할당
  - ex. proxy_set_header X-Real-IP $remote_addr : Request Header X-Real-IP 에 요청자 IP 저장

## 배포 스크립트 작성

무중단 배포를 위해 두 개의 서비스를 띄워야 한다.

먼저, 무중단 배포에 필요한 Profile 을 작성해 보자.

```yml
---
spring:
  profiles: set1
server:
  port: 8081

management:
  server:
    port: 1111

---
spring:
  profiles: set2

server:
  port: 8082

management:
  server:
    port: 2222
```

.


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

.

무중단 배포 관련 파일을 관리할 디렉토리 생성

```shell 
$ mkdir ~/app/nonstop
$ mkdir ~/app/nonstop/jar
```

.

`무중단 배포 스프립트`
- 스트립트 안에서 오류가 발생할 수도 있으니 전체를 실행하기 전에 커멘드 단위로 실행해 보자.

```shell
$ vi ~/app/nonstop/deploy.sh

#!/bin/bash

BASE_PATH=/home/ec2-user/app/nonstop
# jenkins 에서 자동 배포 후 전달해주는 jar 파일 경로
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

echo "> change application.jar Symbolic link"
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
    echo "--- Nginx 에 연결하지 않고 배포를 종료합니다."
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

## 동적 프록시 설정

배포가 완료되고 애플리케이션이 실행되면 Nginx 가 기존에 바라보던 Profile 의 반대를 보도록 변경해 주자.

```shell
# nginx container 진입
$ docker exec -it --user root [Container ID] /bin/bash 

# service-url 관리 파일 생성
# /home/ec2-user/app/nginx 볼륨에 마운팅된 경로에 생성
$ vi /usr/share/nginx/conf/service-url.inc

set $service_url http://[Elastic IP]:8080;

# proxy_pass 수정
$ vi /etc/nginx/conf.d/default.conf

include /usr/share/nginx/conf/service-url.inc;

server {
    ...
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

echo "> Nginx Container Restart"
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

## Jenkins 적용

기존에 Exec command 에 설정된 Jenkins 배포 자동화 shell 대신

Nginx 무중단 배포 shell 을 사용하도록 교체해주자.

.

구성 -> 빌드 후 조치 -> Exec command

```shell
sh /home/ec2-user/app/nonstop/deploy.sh  > /dev/null 2>&1
```

를 실행하도록 수정.

.

## Domain, HTTPS

[향로님의 블로그](https://jojoldu.tistory.com/259) 에서 아래 내용을 다루는 글이 있는데 아래 설정들도 추가해 보면 좋을 것 같다.

- `도메인 및 서비스 메일 생성`
- `EC2 와 도메인 연결`
- `Google 이메일 연결`
- `HTTPS 연결`

.

> [Nginx를 활용한 무중단 배포 구축](https://jojoldu.tistory.com/267?category=635883)