# [IntelliJ] Spring, Maven, Tomcat  Setting in Intranet (인트라넷 환경에서 설정)

**인터넷이 연결되지 않은 or 인트라넷 PC에서 Spring + Maven + Tomcat Server Project 설정을 해보잣!** 😎😆😁

## 필요 파일

- 크게 네 가지 정도의 파일이 필요하다.
  - java
  - Maven
  - Maven Repository
  - tomcat server

## 프로젝트 준비

**기존 프로젝트 사용**

- 기존 Eclipse 에서 사용하던 프로젝트를 그래도 사용할 경우, IntellJ 빌드 과정에서 Eclipse 에서 생성된 클래스 파일과 충돌이 발생할 수 있다.
- /target 디렉토리를 삭제한 후 진행해도 되지만 부수적인 오류를 만나기 싫다면.. 가급적 깔끔하게 새로 클론을 받자.

**Cloning a Repository**

- `Git Bash`, `Intellij - VCS - Get from Version Control..` 등 편한 방법으로 프로젝트 클론하기

## Open Project

- 기존 Eclipse 환경에서 진행하던 프로젝트가 있다면, 아래와 같이 Eclipse project 로 열지 Maven project 로 열지 선택하는 팝업이 뜬다. Maven project 를 선택해주면 되고, 
  
<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img1.png" width="50%"></center>

- 혹여나 Eclipse project 로 열었다 하더라도 아래와 같이 IntelliJ 가 Maven build scripts 를 인식하고 Maven project 로 Load 할꺼냐고 물어보면 OK! 해주면 된다.
  
<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img2.png" width="80%"></center>

## Maven 설정

인터넷이 가능한 PC 라면 해당 설정은 불필요할 수 있다.

`File - Setting - Build, Execution, Deployment -Build Tools - Maven`

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img3.png" width="100%"></center>

- `Maven home path` : Maven home path
- `User Setting file` : Maven Repository 설정 파일 경로 (Nexus와 같은 Repository 관리 서버 설정)
- `Local repository` : Local Maven Repository 경로
  - `User Setting file`, `Local repository` 는 Override 체크를 해주어야 선택이 가능하다.

## SDK 설정

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img4.png" width="100%"></center>

- Java Path 환경 변수 설정이 되어 있다면 SDK 목록에 설정된 java version 이 나타난다.
- 나타나지 않는다면 Java Path 환경 변수 설정이 제대로 되어 있는지 확인해 보자.

## java.lang.OutOfMemoryError

프로젝트 규모가 커서 Java heap space 가 부족할 경우, `java.lang.OutOfMemoryError: Java heap space` 에러가 발생할 수 있다.
.
이 경우 `File - Setting - Build,Execution,Deployment - Compiler` 에 가서 `Shared build process heap size` 를 2000~3000으로 넉넉하게 설정해주자.
.
참고로, Java heap size 설정 후 SSL 관련 에러 발생 시 `Java\jre\lib\security\java.security` 파일에서 SSL 세팅이 필요하다. (이 부분은 별도로 검색해 보자!)

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img5.png" width="100%"></center>

## Lombok

Lombok 사용 시 `Enable annotation processing` 설정이 필요하다.
.
빌드를 할 경우 IntellJ 에서 해당 프로젝트가 Lombok 을 사용하는 것을 인식해서 아래와 같은 팝업이 뜨면서, "Enable annotation processing 할래?" 라고 물어볼 때 설정을 해도 되지만,

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img6.png" width="60%"></center>

설정 `File - Setting - Build,Execution,Deployment - Compiler` 에서 할 수도 있다.

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img7.png" width="100%"></center>
 
## Tomcat Server 생성

IntelliJ IDE 우측 상단 Interface 를 활용하거나 `Run - Edit Configurations` 로 서버 설정을 할 수 있다.
  
<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img8.png" width="100%"></center>

- `Add New Configurations - Tomcat Server - Local` 

**Deployment**

Deployment 설정을 먼저 해보자.

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img9.png" width="100%"></center>

- `Deployment - + 버튼 - Artifacts - project:war exploded`

- Application context : 루트 경로는 "/"
    
**`Server`**

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img10.png" width="100%"></center>

- `Name` : Server Name

- `Application Server` : tomcat server directory path

- `URL` : URL path

- `VM options` : VM option 이 있을 경우 작성해주자.

- `On 'Update'action - update classes and resources`  : 코드 수정 후 자동 리로드 목적

- `On frame deactivation - update classes and resources` :  코드 수정 후 자동 리로드 목적

- `JRE` : jre 목록이 나오지 않는다면, java path 설정이 필요하다.

## Cannot resolve symbol QClass

Qclass 인식을 못 하거나 그밖에 소스 폴더가 자동으로 포함되지 않을 경우

- `프로젝트 우클릭 -> Maven -> Generate Sources and Update Folders`

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/그림5.png" width="100%"></center>

## Run

로그에 `Artifact ProjectName:war exploded: Artifact is deployed successfully` 를 발견했다면, 성공이다 ! 😎

