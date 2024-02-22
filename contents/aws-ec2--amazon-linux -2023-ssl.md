# AWS EC2 Amazon Linux 2023 SSL

사이드 프로젝트에 SSL 인증서를 적용하게 되었습니다.
<br/>

SSL 인증서 발급을 위해 여러 방법을 적용해 보면서 많이 사용되는 두 가지 방법을 적용하는 과정을 기록해 보려고 합니다.

## 도메인

도메인 구매, 인증, 네임서버 등록은 간략하게 넘어가도록 하겠습니다.

자세한 내용은 [EC2 HTTPS로 연결하기 (1) - 도메인 구매하고 ACM 인증서 발급하기](https://woojin.tistory.com/93) 글을 참고하였습니다.

.
<br/>

**도메인 구매**

먼저 도메인 구매는 `gabia`를 이용하였습니다.

.
<br/>

**도메인 인증**

서버로 `AWS EC2`를 이용하고 있기 때문에 도메인 인증은 `AWS Route 53`에서 하게 되었습니다.

.
<br/>

**도메인 네임 서버**

가비아의 도메인 네임서버가 있지만, AWS Route 53에서 도메인을 인증하였으니 도메인 네임 서버는 AWS의 
네임서버를 사용게 되었습니다.

# AWS ACM & ELB

첫 번째 방법으로 `ACM`(AWS Certificate Manager)을 통해 SSL 인증서를 발급받고, `ELB`(Elastic Load Balancer)를 구성하게 되었습니다.

`AWS Certificate Manager` 요금은 AWS 리소스에 대한 비용만 지불하면 **무료로 안내**하고 있습니다.
<br/>

```text
AWS Certificate Manager 요금

AWS Certificate Manager를 통해 프로비저닝된 공인 SSL/TLS 인증서는 무료입니다. 애플리케이션을 실행하기 위해 생성한 AWS 리소스에 대한 비용만 지불하면 됩니다.

ACM을 통해 AWS Private Certificate Authority(CA)를 관리하는 경우 자세한 내용과 예제는 AWS Private CA 요금 페이지를 참조하세요.
```

[AWS Certificate Manager Pricing](https://aws.amazon.com/certificate-manager/pricing/?nc1=h_ls)

.
<br/>

하지만.. ACM을 통해 SSL 인증서를 발급 받고, HTTPS/HTTP 요청에 대한 처리를 위해 보통 `AWS ELB`(Elastic Load Balancer)를 사용하고 있었습니다.
<br/>

요청을 서버로 분산하는 용도로 ELB를 사용하는 것이 아닌 단순히 HTTPS/HTTP 요청 처리를 위해 ELB를 사용한다는 것에 대한 필요성을 느끼지 못하였습니다. NGINX로 충분히 처리가 가능한 단순한 작업에 비용이 발생하는 이유도 있었습니다.
<br/>

[Elastic Load Balancing pricing](https://aws.amazon.com/elasticloadbalancing/pricing/?nc1=h_ls)

...
<br/>

Reference

- [EC2 HTTPS로 연결하기 (2) - 로드밸런서로 리다이렉트 설정하고 Health check 통과하기](https://woojin.tistory.com/94)
- [ELB(Elastic Load Balancer) 구성 & 사용법 가이드](https://inpa.tistory.com/entry/AWS-%F0%9F%93%9A-ELB-Elastic-Load-Balancer-%EA%B0%9C%EB%85%90-%EC%9B%90%EB%A6%AC-%EA%B5%AC%EC%B6%95-%EC%84%B8%ED%8C%85-CLB-ALB-NLB-GLB)

# Certbot & Let’s encrypt

두 번째 방법으로 `Certbot`으로 `Let’s encrypt` 인증서를 발급받게 되었습니다.

## Problem

[AWS linux2에서 certbot nginx 인증하기](https://flamingotiger.github.io/backend/devOps/aws-linux2-certbot-nginx/) 글을 참고하며 진행하게 되었는데 
<br/>

Certbot에 필요한 종속성을 위해 EPEL 패키지를 설치하는 과정에서 계속 redhat-release 버전 관련 오류가 발생하였는데 원인을 계속 찾다보니 [Amazon Linux 2023 에서 EPEL 리포지토리를 사용할 수 없다는 글]((https://repost.aws/questions/QUIMw8IWHSQz6W5tGv3WO1qA/problem-conflicting-requests-nothing-provides-redhat-release-7-needed-by-epel-release-7-14-noarch)
)을 발견하게 되었습니다.
<br/>

```text
If you are using Amazon Linux 2023, you cannot use the EPEL repository.

... 
Amazon Linux 2 features a high level of compatibility with CentOS 7. As a result, many EPEL7 packages work on Amazon Linux 2. However, AL2023 doesn't support EPEL or EPEL-like repositories.
```

## Resolve

결국 `Amazon Linux 2023`을 키워드로 인증서를 발급받는 방법을 서칭하게 되었고, 

[Amazon Linux 2023 AMI 설정법](https://www.inflearn.com/news/903768)글이 많은 도움이 되었습니다.
<br/>

```shell
# Amazon Linux 2023
$ sudo yum update -y # 모든 기존 패키지를 최신 버전으로 업데이트
$ sudo yum install nginx -y # Nginx 패키지 설치
$ sudo systemctl enable nginx # Nginx 서비스가 시스템 부팅 시 자동으로 시작되도록 설정

$ sudo su - # root 계정으로 전환
$ dnf install python3 augeas-libs # python3, augeas-libs 라이브러리 설치

        ...
        Complete!

$ dnf remove certbot # 기존 설치된 certbot 패키지 제거

        ...
        Complete!

$ python3 -m venv /opt/certbot/ # python3 가상 환경 생성(pip로 certbot 설치 목적)
$ /opt/certbot/bin/pip install --upgrade pip # pip 패키지를 최신 버전으로 업그레이드
$ /opt/certbot/bin/pip install certbot certbot-nginx # pip 명령어로 certbot certbot-nginx 패키지 설치

        ...
        Installing collected packages: pyparsing, certbot-nginx
        Successfully installed certbot-nginx-2.9.0 pyparsing-3.1.1

$ ln -s /opt/certbot/bin/certbot /usr/bin/certbot # python3 가상 환경에 생성된 certbot 디렉토리로 링크 생성

$ certbot --nginx

        Please enter the domain name(s) you would like on your certificate (comma and/or
        space separated) (Enter 'c' to cancel):

        ...

        Successfully received certificate.
        Certificate is saved at: /etc/letsencrypt/live/www.mydomain.shop/fullchain.pem
        Key is saved at:         /etc/letsencrypt/live/www.mydomain.shop/privkey.pem
        This certificate expires on 2024-00-00.
        These files will be updated when the certificate renews.

        ...
```

`certbot --nginx`

- certbot 도구를 사용하여 nginx 웹 서버에 대한 Let's Encrypt SSL/TLS 인증서를 자동으로 발급하고 적용
- 명령어 실행을 통해 아래 과정이 진행
  - 도메인 검증
  - 인증서 발급
  - 인증서 설치 및 Nginx 설정 변경
  - 자동 갱신 설정

...
<br/>

[참고] Amazon Linux 2 (AL2)는 아래 명령어로 설정할 수 있습니다.
<br/>

```shell
# Amazon Linux 2
$ sudo su -
$ yum -y install yum-utils
$ yum-config-manager --enable rhui-REGION-rhel-server-extras rhui-REGION-rhel-server-optional
$ yum install https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
$ yum install certbot python2-certbot-nginx

$ certbot --nginx
$ systemctl restart nginx
```

...
<br/>

Reference.

- [certbot](https://certbot.eff.org/)
- [AWS EC2 certbot ssl 인증](https://hagenti0612.github.io/aws/aws-certbot/)
- [Certbot으로 무료 HTTPS 인증서 발급받기](https://www.vompressor.com/tls1/)
- [Amazon Linux 2023에서 Let’s Encrypt를 설치하기](https://www.zinnunkebi.com/amazon-linux-2023-lets-encrypt/)
- [Amazon Linux2에서 Certbot을 통해 HTTPS 적용하기 (With. Nginx)](https://dev-jwblog.tistory.com/57)

## Nginx

nginx 설치 후 생성된 `/etc/nginx` 경로에 있는 `nginx.conf` 루트 설정 파일을 보면
<br/>

```shell
$ sudo vi /etc/nginx/nginx.conf
```

아래와 같이 `/etc/nginx/default.d/` 경로의 .conf 하위 설정 파일들을 include 하는 부분을 볼 수 있습니다.
<br/>

```shell
...

include /etc/nginx/default.d/*.conf;
...
```

`/etc/nginx/default.d/` 경로에 `default.conf` 파일을 생성하고 HTTPS/HTTP 요청을 처리하도록 설정해 보겠습니다.
<br/>

```shell
$ sudo vi /etc/nginx/conf.d/default.conf
```

`default.conf` 설정 파일 작성
<br/>

```shell
server {
        listen 443 ssl;
        server_name  www.mydomain.shop mydomain.shop;

        include /home/ec2-user/app/nginx/service-url.inc;

        location / {
                proxy_pass $service_url;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                proxy_set_header Host $http_host;
        }
        
        ssl_certificate /etc/letsencrypt/live/www.mydomain.shop/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/www.mydomain.shop/privkey.pem;
        include /etc/letsencrypt/options-ssl-nginx.conf;
        ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;
}

server {
        listen 80;
        server_name  localhost;

        location / {
                return 301 https://www.mydomain.shop$request_uri;
        }
}
```

SSL 인증을 위한 파일들은 `certbot --nginx` 명령을 통해 생성됩니다.
- /etc/letsencrypt/live/www.mydomain.shop/fullchain.pem
- /etc/letsencrypt/live/www.mydomain.shop/privkey.pem
- /etc/letsencrypt/options-ssl-nginx.conf
- /etc/letsencrypt/ssl-dhparams.pem

.
<br/>

참고로 `options-ssl-nginx.conf` 파일의 내용은 아래와 같습니다.
<br/>

```shell
ssl_session_cache shared:le_nginx_SSL:10m;
ssl_session_timeout 1440m;
ssl_session_tickets off;

ssl_protocols TLSv1.2 TLSv1.3;
ssl_prefer_server_ciphers off;

ssl_ciphers "ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-CHACHA20-POLY1305:ECDHE-RSA-CHACHA20-POLY1305:DHE-RSA-AES128-GCM-SHA256:DHE-RSA-AES256-GCM-SHA384";
```

.
<br/>

Nginx 서버 실행/리로드
<br/>

```shell
$ sudo service nginx start # nginx 서버 실행

$ sudo service nginx reload # 설정 파일 수정 후 설정파일 로딩

$ sudo nginx -t # nginx 실행 오류 시 오류 확인
```

## 갱신 자동화

Let’s Encrypt 인증서는 90일마다 갱신이 필요합니다.
매번 만료 전에 직접 갱신을 하기 번거로우므로 리눅스의 crontab 스케줄러를 활용해서 자동화할 수 있습니다.

참고로, Amazon Linux 2023은 crontab 사용을 위해 별도 설치가 필요합니다.
<br/>

```shell
$ sudo yum -y install cronie
```

crontab에 스케줄러 등록
<br/>

```shell
$ crontab -l # crontab 조회
$ crontab -e # crontab 수정

30 1,13 * * * root /usr/bin/certbot renew --no-self-upgrade --renew-hook "sudo service nginx reload" >> /home/ec2-user/app/log/ssl-renewal.log 2>&1

$ sudo systemctl restart crond # cron 재실행
```

- `30 1,13 * * *`: 매일 1시 30분, 13시 30분에 실행 (분 시 일 월 요일)
- `root`: root 사용자 권한으로 실행
- `/usr/bin/certbot renew`: 만료 예정인 모든 Let's Encrypt SSL 인증서를 갱신
- `--no-self-upgrade`: certbot 자동 업그레이드 방지
- `--renew-hook`: 인증서 갱신 후 실행할 명령를 지정
- `sudo service nginx reload`: 갱신된 인증서를 웹 서버에 적용하기 위해 인증서 갱신이 성공적으로 완료된 후 nginx 서비스 리로드
- `>>`: 실행 결과를 파일로 저장

crontab 등록 후 실제 갱신을 테스트해 볼 수도 있습니다.
<br/>

```shell
$ sudo certbot renew --dry-run # 갱신 테스트

$ sudo certbot renew # 실제 갱신

$ sudo certbot certificates # 만료일 확인
```