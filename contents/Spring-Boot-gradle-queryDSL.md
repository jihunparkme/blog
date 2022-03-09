# Spring Boot, Gradle 환경에 Querydsl 설정 방법

Spring Boot, Querydsl 최신 버전을 사용하면서 QueryDSL 적용 방법에 변동이 생긴 듯하다.

참고로, 현재 프로젝트는 `spring-boot-2.6.3`, `gradle-7.3.2` 버전을 사용 중이다.

.

기존 방식대로 적용을 했을 때, 아래와 같은 에러가 발생하게 되었다.

`Unable to load class 'com.mysema.codegen.model.Type'.`

.

그럼.. 이제 어떤 방법으로 QueryDSL 설정을 해주어야 할지 확인해 보자!

## Gradle

먼저 Gradle 설정 방법이다.

```java
// 1. queryDsl version 정보 추가
buildscript {
	ext {
		queryDslVersion = "5.0.0"
	}
}

plugins {
	id 'org.springframework.boot' version '2.6.3'
	id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    // 2. querydsl plugins 추가
	id "com.ewerk.gradle.plugins.querydsl" version "1.0.10"
	id 'java'
}

//...

dependencies {
    // 3. querydsl dependencies 추가
	implementation "com.querydsl:querydsl-jpa:${queryDslVersion}"
	implementation "com.querydsl:querydsl-apt:${queryDslVersion}"
    //...
}

test {
	useJUnitPlatform()
}

/*
 * queryDSL 설정 추가
 */
// querydsl에서 사용할 경로 설정
def querydslDir = "$buildDir/generated/querydsl"
// JPA 사용 여부와 사용할 경로를 설정
querydsl {
	jpa = true
	querydslSourcesDir = querydslDir
}
// build 시 사용할 sourceSet 추가
sourceSets {
	main.java.srcDir querydslDir
}
// querydsl 컴파일시 사용할 옵션 설정
compileQuerydsl{
	options.annotationProcessorPath = configurations.querydsl
}
// querydsl 이 compileClassPath 를 상속하도록 설정
configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
	querydsl.extendsFrom compileClasspath
}
```

## compileQuerydsl 실행

Gradle 설정이 완료되었다면, `Reload Gradle Projdct` 를 실행해준 후,

`Gradle Tasks -> compileQuerydsl` 을 실행해주자.

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/leather-project-issue/그림2.png" width="50%"></center>
 
.

BUILD SUCCESSFUL 을 확인하였다면, `build/generated/querydsl` 경로에 Project Entity 들의 QClass 가 생성된 것을 확인할 수 있다.

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/leather-project-issue/그림3.png" width="50%"></center>

## Reference

[[Querydsl] 프로젝트 설정 및 테스트](https://jaime-note.tistory.com/67)

[compileQuerydsl 오류](https://www.inflearn.com/questions/355723)