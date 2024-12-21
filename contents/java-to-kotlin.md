# Java to Kotlin

## Kotlin version

먼저 코틀린 설정을 위해 아무 `.java` 파일 우클릭 후 `Convert Java File to Kotlin File` 기능을 사용하면 `OK, Configure Kotlin In the Project`로 코틀린 설정이 가능합니다.

`JDK 21`을 사용할 예정이라서 Kotlin 버전은 `2.1.0`으로 설정하였습니다.

※ [Which versions of Kotlin are compatible with which versions of Java?](https://stackoverflow.com/questions/63989767/which-versions-of-kotlin-are-compatible-with-which-versions-of-java)

![Result](https://github.com/jihunparkme/blog/blob/main/img/java-tio-kotlin/kotlin-version.png?raw=true 'Result')

IDE의 도움으로 kotlin 설정을 마치면 `build.gradle`, `settings.gradle` 파일에 코틀린 설정이 추가됩니다.

**Kotlin 설정 및 gradle.kts로 변환**

䷿AS-IS) build.gradle

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.5'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '21'
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'
    implementation 'org.springframework.boot:spring-boot-starter-mail'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // firebase Cloud Firestore
    implementation 'com.google.firebase:firebase-admin:9.2.0'

    // util
    implementation 'org.jsoup:jsoup:1.17.1'
    implementation 'org.apache.commons:commons-lang3'

    // lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // tool
    compileOnly 'org.springframework.boot:spring-boot-devtools'

    // test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testAnnotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.projectlombok:lombok'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

䷾ TO-BE) build.gradle.kts

```kts
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    val kotlinVersion = "2.1.0"
    kotlin("jvm") version kotlinVersion // Kotlin JVM을 사용하는 프로젝트를 위한 플러그인
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" // Kotlin 코드 스타일을 자동으로 검사하고 포맷팅하는 도구
}

group = "com"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // data
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // Utility libraries
    implementation("org.jsoup:jsoup:1.17.1")
    implementation("org.apache.commons:commons-lang3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // Spring Boot Devtools
    compileOnly("org.springframework.boot:spring-boot-devtools")

    // Testing libraries
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.mockito")
    }
    testImplementation("com.ninja-squad:springmockk:2.0.3")
    testImplementation("io.kotest:kotest-runner-junit5:5.4.2")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

## Minimum Gradle version

코틀린 설정을 완료했다면 Gradle도 버전에 맞게 올려줍시다!

Kotlin version `2.1.0`으로 설정했으니 Gradle version `8.11`로 사용할 계획입니다.

[Compatibility Matrix](https://docs.gradle.org/current/userguide/compatibility.html#kotlin)

![Result](https://github.com/jihunparkme/blog/blob/main/img/java-tio-kotlin/embedded-kotlin-version.png?raw=true 'Result')

gradle version 설정은 `gradle > wrapper > gradle-wrapper.properties`에서 수정할 수 있습니다.

## Java to Kotlin

먼저 `java`에서 적용되던 `lombok`과의 이별을 해야 할 때입니다.

### lombok 대신 data class

- 컴파일러가 `equals()`, `hashCode()`, `toString()`, `copy()`, `componentN()` 메서드를 자동 생성
- 데이터 클래스에 property 를 선언하는 순간 해당 property 는 `field`, `Getter`, Setter, 생성자 파라미터 역할

🔗 [commit](https://github.com/jihunparkme/tech-news/commit/4b9eb953ab1742e186b348a70783ff025b085dc0)

### warning

- `Unnecessary non-null assertion (!!) ...` 불필요한 non-null assertion 제거
- Kotlin은 Java로부터 변환될 때 null 허용을 기본으로 하고 있다보니 `?` 키워드를 가급적 모두 제거하고 필요 시 추가하기

## Reference

> [자바 프로젝트 3개 코틀린 점진적 전환기(feat. lombok 됩니다.)](https://tech.kakaopay.com/post/kotlin-migration/)