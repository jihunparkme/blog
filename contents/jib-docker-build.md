# JIB를 활용해 도커에 이미지 빌드하고 실행하기

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
		image = "jihunparkme/the-voice-of-jesus"
		tags = setOf("latest", "1.0.0")
	}
	container {
		jvmFlags = listOf("-Xms128m", "-Xmx128m")
	}
}
```


```
./gradlew jib   
```

```
docker run -it -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod --name tvoj jihunparkme/the-voice-of-jesus
```









운영환경처럼 도커에 리눅스 띄우고 리눅스 안에서 도커 파일 실행해보기

```bash
nohup java -jar -Dspring.profiles.active=prod  $REPOSITORY/deploy/$JAR_NAME > $REPOSITORY/deploy/deploy.log 2>&1 &
```


도커 이미지로 도커 띄우기