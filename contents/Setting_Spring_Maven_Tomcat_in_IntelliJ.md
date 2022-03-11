# [IntelliJ] Spring, Maven, Tomcat  Setting in Intranet (인트라넷 환경에서 설정)

**인터넷이 연결되지 않은 or 인트라넷 PC에서 Spring + Maven + Tomcat Server Project 설정을 해보잣!** 😎😆😁

## 필요 파일

- 크게 네 가지 정도의 파일이 필요하다.
  - java
  - Maven (Maven home)
  - Maven Repository (Local Repository & Maven setting)
  - tomcat server

## 프로젝트 준비

### 기존 프로젝트 사용

- 기존 Eclipse에서 사용하던 프로젝트를 그대로 사용할 경우, IntelliJ 빌드 과정에서 Eclipse에서 이미 생성된 클래스 파일과 충돌이 발생할 수 있다.

- `/target` 디렉터리를 삭제한 후 진행해도 되지만 부수적인 오류를 만나기 싫다면.. 가급적 깔끔하게 새로 클론을 받자.

### Cloning a Repository

- `Git Bash`를 활용하거나,  `Intellij` - `VCS` - `Get from Version Control..` 등 편한 방법으로 프로젝트 클론 하기

## Open Project

- 프로젝트에 Eclipse configuration 파일이 포함되어 있다면, 아래와 같이 Eclipse project 로 열지 Maven project 로 열지 선택하는 팝업이 나타난다. `Maven project` 를 선택해주면 되고, 
  
<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img1.png" width="50%"></center>

- 혹여나 Eclipse project 로 열었다 하더라도 아래와 같이 IntelliJ 가 Maven build scripts 를 인식하고 `Maven project` 로 Load 할 거냐고 물어보면 OK! 해주면 된다.
  
<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img2.png" width="80%"></center>

## Maven 설정

인터넷이 가능한 PC 라면 해당 설정은 불필요할 수 있다.

.

하지만, 인터넷이 막혀있거나 인트라넷 환경이라면 사내 Maven Repository 서버 설정이 필요하다.

.

`File` - `Setting` - `Build, Execution, Deployment` - `Build Tools` - `Maven`

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img3.png" width="100%"></center>

- `Maven home path` : Maven home path
- `User Setting file` : Maven Repository 설정 파일 (사내 Maven Repository 서버 관련 설정)
- `Local repository` : Local Maven Repository 경로
  - `User Setting file`, `Local repository` 는 Override 체크를 해주어야 경로 선택이 가능하다.

## SDK 설정

`File` - `Project Structure` - `Project`

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img4.png" width="100%"></center>

- Java Path 환경 변수 설정이 되어 있다면 SDK 목록에 설정된 java version 이 나타난다.

- 나타나지 않는다면 Java Path 환경 변수 설정이 제대로 되어 있는지 확인해 보자.

## java.lang.OutOfMemoryError

프로젝트 규모가 커서 Java heap space 가 부족할 경우, `java.lang.OutOfMemoryError: Java heap space` 에러가 발생할 수 있다.

.

이 경우 `File` - `Setting` - `Build,Execution,Deployment` - `Compiler` 에 가서 `Shared build process heap size` 를 2000~3000으로 넉넉하게 늘려주자.

.

참고로, Java heap size 설정 후 SSL 관련 에러 발생 시 `Java\jre\lib\security\java.security` 파일에서 SSL 설정 변경이 필요하다. (이 부분은 별도로 검색해 보자!)

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img5.png" width="100%"></center>

## Lombok

프로젝트에서 Lombok 사용 시 `Enable annotation processing` 설정이 필요하다.

.

Lombok을 사용하는 프로젝트가 빌드를 할 경우, IntelliJ는 해당 프로젝트가 Lombok을 사용하는 것을 인식하고 아래와 같은 안내를 해준다. `Enable annotation processing 할래?` 라고 물어볼 때 설정을 해도 되지만,

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img6.png" width="60%"></center>

.

`File` - `Setting` - `Build,Execution,Deployment` - `Compiler` 에서 미리 설정할 수도 있다.

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img7.png" width="100%"></center>
 
## Tomcat Server 생성

### Edit Configurations

(1) IntelliJ IDE 우측 상단 Interface 를 활용하거나 (2) `Run` - `Edit Configurations`에서 서버 관련 설정을 할 수 있다.

- `Add New Configurations` - `Tomcat Server` - `Local` 
  
<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img8.png" width="100%"></center>

### Deployment

Deployment 설정을 먼저 해보자.

- `Deployment` - `+ 버튼` - `Artifacts` - `project:war exploded`

- `Application context` : 루트 경로 설정

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img9.png" width="100%"></center>
    
### `Server`

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img10.png" width="100%"></center>

- `Name` : Server Name (Project명과 맞춰주자)

- `Application Server` : tomcat server path

- `URL` : URL path

- `VM options` : VM option 이 있을 경우 작성해주자.

- `On 'Update'action - update classes and resources`  : 코드 수정 후 자동 리로드 목적

- `On frame deactivation - update classes and resources` :  코드 수정 후 자동 리로드 목적

- `JRE` : jre 목록이 나오지 않는다면, java path 환경 변수를 확인해보자.

## Generate Sources and Update Folders

QClass를 사용하는 프로젝트를 처음 열었다면, QClass 생성이 필요하다.

.

`Cannot resolve symbol QClass` 에러 메시지가 출력되며 Qclass 인식을 못 하거나 그밖에 소스 폴더가 자동으로 포함되지 않을 경우 `Generate Sources and Update Folders` 를 실행하자.

- `프로젝트 우클릭` -> `Maven` -> `Generate Sources and Update Folders`

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/그림5.png" width="100%"></center>

## Run

서버가 가동되며 로그에서 `Artifact ProjectName:war exploded: Artifact is deployed successfully` 를 발견했다면, 성공이다 ! 😎

---

\+ 2022.3.12

.

개인적으로 intelliJ를 사용하면서 개발 생산성이 굉장히 증가하는 경험을 해보았기에 Eclipse와 intelliJ의 장단점을 비교하며 사내에 intelliJ 사용을 제안했었다. 다행히 긍정적인 반응으로 사내 개발 프로젝트에도 intelliJ를 사용하게 되었다. 

.

intelliJ 사용을 적극 제안했기에.. 회사에서 IntelliJ 프로젝트 설정 및 간단한 사용 방법을 개발팀 팀원들에게 발표하게 되었다. 이틀로 나누어 발표하였고, 첫날은 여덟 분, 둘째 날은 열한 분이 들어주셨다.

.

intelliJ를 제대로 사용해본 지는 얼마 되지 않다 보니 부족한 부분도 많았지만, 믿고 발표를 맡겨주신 분들, 모두 끝까지 들어주신 분들께 정말 감사하다. 발표자료는 회사 프로젝트 정보가 포함되어있어 공개는 할 수 없지만, 포스팅 내용에 자료 대부분의 내용을 다뤘다.

.

회사에서 무언가를 설명하는 발표는 처음인 만큼 서툴렀던 부분도 있었지만, 향후에도 발표할 기회가 생기면 적극적으로 참여해보고, 열심히 갈고 닦아서 기술 관련 발표도 해보고 싶다. (사실 사내 기술 컨퍼런스가 있다면 프로 발표러가 될 자신이 있다🤣)

.

많이 떨기도 하였고.. 부끄럽기도 했지만 정말 좋은 발표 경험이었다. 😆

.

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/IntelliJ-setting/img12.png" width="100%"></center>

.

추가로 IntelliJ KeyMap 링크와 공식 문서도 첨부하였다.

.

[IntelliJ KeyMap](https://resources.jetbrains.com/storage/products/intellij-idea/docs/IntelliJIDEA_ReferenceCard.pdf)

[IntelliJ Documentation](https://www.jetbrains.com/help/idea/getting-started.html)