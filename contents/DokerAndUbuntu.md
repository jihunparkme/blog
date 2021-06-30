# Windows Setting

- Windows 기능 켜기/끄기 접속 후 `가상 머신 플랫폼`, `Hyper-V` 체크

# Download Docker Desktop

- [Docker Desktop for Window](https://www.docker.com/products/docker-desktop)
- Download Guide : https://luckygg.tistory.com/165
- `Cannot enable Hyper-V service` Error

  - [Link](https://docs.microsoft.com/ko-kr/virtualization/hyper-v-on-windows/quick-start/enable-hyper-v)
  - `Windows PowerShell` 이 실행되지 않을 경우 파일 위치를 열고 `Windows PowerShell ISE` 실행
  - `bcdedit /set hypervisorlaunchtype auto`

- `WSL 2 installation is incomplete` Error
  - [Link](https://blog.nachal.com/1691)

# Docker Command

(example ubuntu)

- search image : `# docker search ubuntu`

- download latest image : `# docker pull ubuntu:latest`

- print image list : `# docker images`

- create container : `# docker run -it --name=[Container NAME] [Image Repository]`

  - ex. `# docker run -it --name=ubuntu_server ubuntu`
  - ex. `# docker run -itd d27b9ffc5667 /bin/bash`

- exie Shell

  - exit : Ctrl + d
  - 백그라운드 종료 : ctrl + p + q

- Docker list : `# docker ps`

  - `# docker ps -a` 는 모든 container 확인

- attach container : `# docker attach [Container NAME]`

- stop conatiner : `# docker stop [Container ID]`

- remove conatiner : `# docker rm [Container ID]`

## Reference

> [Link](https://augustines.tistory.com/136)
>
> [명령어](https://captcha.tistory.com/49)

# ubuntu & SSH

- Update ubuntu : `# apt-get update`

- Install SSH : `# apt-get install net-tools vim ssh`

- Edit SSH config : `# vim /etc/ssh/sshd_config`

  - PermitRootLogin 주석을 제거한 후, prohibit-password 를 yes 로 수정

- edit root password : `# passwd root`

- start ssh service : `# service ssh start`

- restart ssh service : `# service ssh restart`

- 원격 접속 : `# ssh -p [PORT] root@[IP]`

  - ex. `# ssh -p 22 root@127.17.0.2`

- ubuntu Etc.
  - 열린 PORT 확인 : `netstat -nap`
  - LISTEN 중인 PORT : `netstat -nap | grep LISTEN`
  - 특정 PORT : `netstat -nap | grep 22`

## Reference

> [Link](https://chanhy63.tistory.com/11)
