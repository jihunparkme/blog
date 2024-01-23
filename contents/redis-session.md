# Redis로 Session 관리하기


## Install Redis in Docker

**Install Redis Image**

```bash
docker pull redis
```

.

**run Redis**

```bash
docker run -itd --name redis -p 6379:6379 --restart=always redis
```

`-itd`

- i: t 옵션과 같이 사용. 표준입력 활성화. 컨테이너와 연결되어있지 않더라도 표준입력 유지
- t: i 옵션과 같이 사용. TTY 모드로 사용하며 bash 사용을 위해 반드시 필요
- d: 컨테이너를 백그라운드로 실행. 실행시킨 뒤 docker ps 명령어로 컨테이너 실행 확인 가능

`-p 8000:8080`

- 컨테이너 포트를 호스트와 연결
- 컨테이너 외부와 통신할 6379 포트와 컨테이너 내부적으로 사용할 6379 포트 설정

`--name`

- 해당 컨테이너의 이름 설정
- 이름을 설정해 놓으면 컨테이너 id 외에도 해당 이름으로 컨테이너 설정 가능

.

**execute Redis**

```bash
docker exec -it --user root 'redis' /bin/bash
```

- redis 컨테이너 내부에 접속하여 redis-cli 접속


## Initial Settings

**dependecny**

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
implementation 'org.springframework.session:spring-session-data-redis'
```

```yml
spring:
  session:
    store-type: redis
  redis:
    host: localhost
    port: 6379
```

**RedisConfig**


**@EnabledRedisHttpSession**

