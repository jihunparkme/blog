# Windows Setting

- Windows 기능 켜기/끄기 접속 후 `가상 머신 플랫폼`, `Hyper-V` 체크

# Download Docker Desktop

- [Docker Desktop for Window](https://www.docker.com/products/docker-desktop)
- Download Guide : https://luckygg.tistory.com/165
- lDocker 설치 후 `Cannot enable Hyper-V service` 에러가 발생 시 아래 링크 참고
  - [Link](https://docs.microsoft.com/ko-kr/virtualization/hyper-v-on-windows/quick-start/enable-hyper-v)
  - `Windows PowerShell` 이 실행되지 않을 경우 파일 위치를 열고 `Windows PowerShell ISE` 실행
  - `bcdedit /set hypervisorlaunchtype auto`

# Docker Command

- search image : `# docker search ubuntu`

- download image : `# docker pull ubuntu:latest`

- print images : `# docker images`

- run container : `# docker run -it --name=ubuntu_server ubuntu`

  - exit : Ctrl + d
  - 백그라운드 종료 : ctrl + p + q

- Docker list : `# docker ps -a`

- attach container : `# docker attach ubuntu`

- stop conatiner : `# docker stop [Container ID]`

- remove conatiner : `# docker rm [Container ID]`

# Create Ubuntu

- [link](https://augustines.tistory.com/136)
- [명령어](https://captcha.tistory.com/49)
- `docker run -itd d27b9ffc5667 /bin/bash`

## SSH 접속

https://chanhy63.tistory.com/11

https://believecom.tistory.com/650

https://sleepyeyes.tistory.com/67

https://hwan-shell.tistory.com/178

https://docs.microsoft.com/ko-kr/virtualization/hyper-v-on-windows/quick-start/enable-hyper-v

https://blog.gaerae.com/2019/04/hyper-v-troubleshooting.html

우선 docker 로 간단한 ubuntu 서버 띄워보는 테스트

ubuntu ssh 세팅
