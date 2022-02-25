# [IntelliJ] Spring, Maven, Tomcat  Setting in Intranet (인트라넷 환경에서 설정)

**인터넷이 연결되지 않은 or 인트라넷 PC 에서 Spring + Maven + Tomcat Server Project 설정을 해보잣!** 😎😆😁

## 프로젝트 준비

Cloning a Repository

- Git Bash, Intellij - VCS - Get from Version Control.. 등 편리한 방법으로 프로젝트 클론하기

## Open Project

- 기존 Eclipse 환경에서 진행하던 프로젝트가 있다면, Eclipse project 로 열어주자.
- 기존 Eclipse 에서 사용하던 프로젝트로 진행했을 때, /target 경로의 클래스 파일 쪽에서 충돌이 일어나는 것인지... 
  빌드 과정에서 오류가 계속 발생해서 /target 디렉토리를 지우고 진행을 하거나 깔끔하게 새로 클론 받는 것을 추천!

## Maven 설정

인터넷이 가능한 PC 라면 해당 설정은 불필요할 수 있다.

`File - Setting - Build, Execution, Deployment -Build Tools - Maven`

- `Maven home path` : Maven 설치 경로
- `User Setting file` : Maven Repository 설정 파일 경로
- `Local repository` : Local Maven Repository 경로

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/그림1.png" width="100%"></center>

## Tomcat Server 생성

**`Run - Edit Configurations`**

- `Add New Configurations - Tomcat Server - Local`
  
<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/그림2.png" width="50%"></center>
  
**`Deployment - + 버튼 - Artifacts - project:war exploded`**

- Application context : 루트 경로는 "/"

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/그림3.png" width="100%"></center>
    
**`Server`**

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/그림4.png" width="100%"></center>

- `Name` : Server Name

- `Application Server` : tomcat server directory path

- `URL` : URL path

- `VM options` : VM option 이 있을 경우 작성해주자.

- `On 'Update'action - update classes and resources`  : 코드 수정 후 자동 리로드 목적

- `On frame deactivation - update classes and resources` :  코드 수정 후 자동 리로드 목적

- `JRE` : jre 목록이 나오지 않는다면, java path 설정이 필요하다.

## Lombok 사용 시

`File - Setting - Build,Execution,Deployment - Compiler - Annotation Processors` : Enable annotation processing ✔

## Cannot resolve symbol QClass

Qclass 인식을 못 하는 경우

- `프로젝트 우클릭 -> Maven -> Generate Sources and Update Folders`

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/그림5.png" width="100%"></center>

## java.lang.OutOfMemoryError: Java heap space 발생 시

`File - Setting - Build,Execution,Deployment - Compiler - Shared build process heap size` : 2000 

