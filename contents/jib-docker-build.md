# JIB를 활용해 도커에 이미지 빌드하고 실행하기

일반적으로 도커에 이미지를 빌드하기 위해 `Docker`, `Dockerfile`이 필요한데

Gradle, Maven에서 Jib 플러그인을 활용해 이미지를 빌드는 방법을 알아보자.

## Spring boot JIB 설정

👉🏻 `build.gradle.kts`에 jib plugins 추가하기

```kts
plugins {
    // ...
	id ("com.google.cloud.tools.jib") version "3.4.4"
}

// ...

jib {
	from {
		image = "eclipse-temurin:21.0.6_7-jre-alpine"
	}
	to {
		image = "jihunparkme/my-project"
		tags = setOf("latest", "1.0.0")
	}
	container {
		jvmFlags = listOf("-Xms128m", "-Xmx128m")
	}
}
```
- `jdk21`, `gradle-8.11.1` 버전을 사용하고 있는데 아래와 같은 에러가 발생한다면 `jib` 버전업이 필요하다. 최신 버전(`3.4.4`)을 사용해 보자.

    ```bash
    The org.gradle.api.plugins.Convention type has been deprecated. This is scheduled to be removed in Gradle 9.0.
    The org.gradle.api.plugins.JavaPluginConvention type has been deprecated. This is scheduled to be removed in Gradle 9.0.
    ```
- 베이스 이미지는 jdk21을 사용중이므로 그에 맞는 이미지를 설정
- 그밖에 이미지 이름, 태그, 컨테이너 설정 가능

👉🏻 프로젝트의 홈 디렉토리에서 아래 명령어를 통해 이미지를 빌드하면 자동으로 이미지 레파지토리에 푸시

```
./gradlew jib

...
Built and pushed image as jihunparkme/my-project, jihunparkme/my-project, jihunparkme/my-project:1.0.0
Executing tasks:
[===========================   ] 91.7% complete
> launching layer pushers


BUILD SUCCESSFUL in 16s
6 actionable tasks: 6 executed
```

자신의 도커 허브 레파지토리를 확인해 보면 이미지가 푸시된 것을 확인할 수 있다.
- https://hub.docker.com/repositories/{YOUR-NAME}







```
docker run -it -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod --name tvoj jihunparkme/my-project
```









운영환경처럼 도커에 리눅스 띄우고 리눅스 안에서 도커 파일 실행해보기

```bash
nohup java -jar -Dspring.profiles.active=prod  $REPOSITORY/deploy/$JAR_NAME > $REPOSITORY/deploy/deploy.log 2>&1 &
```


도커 이미지로 도커 띄우기