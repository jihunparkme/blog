# Java To Kotlin

## Kotlin version

먼저 코틀린 설정을 위해 아무 `.java` 파일 우클릭 후 `Convert Java File to Kotlin File` 기능을 사용하면 `OK, Configure Kotlin In the Project`로 코틀린 설정이 가능하다.

`JDK 21`을 사용할 예정이라서 Kotlin 버전은 `2.1.0`으로 설정하였다.

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/java-to-kotlin/kotlin-version.png?raw=true 'Result')

ref. [Which versions of Kotlin are compatible with which versions of Java?](https://stackoverflow.com/questions/63989767/which-versions-of-kotlin-are-compatible-with-which-versions-of-java)

IDE의 도움으로 kotlin 설정을 마치면 `build.gradle`, `settings.gradle` 파일에 코틀린 설정이 추가된다

## Minimum Gradle version

[Compatibility Matrix](https://docs.gradle.org/current/userguide/compatibility.html#kotlin)
