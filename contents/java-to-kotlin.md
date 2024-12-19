# Java To Kotlin

## Kotlin version

먼저 코틀린 설정을 위해 아무 `.java` 파일 우클릭 후 `Convert Java File to Kotlin File` 기능을 사용하면 `OK, Configure Kotlin In the Project`로 코틀린 설정이 가능합니다.

`JDK 21`을 사용할 예정이라서 Kotlin 버전은 `2.1.0`으로 설정하였습니다.

※ [Which versions of Kotlin are compatible with which versions of Java?](https://stackoverflow.com/questions/63989767/which-versions-of-kotlin-are-compatible-with-which-versions-of-java)

![Result](https://github.com/jihunparkme/blog/blob/main/img/java-tio-kotlin/kotlin-version.png?raw=true 'Result')

IDE의 도움으로 kotlin 설정을 마치면 `build.gradle`, `settings.gradle` 파일에 코틀린 설정이 추가됩니다.

🔗 [commit](https://github.com/jihunparkme/tech-news/commit/efa999f9fe8da9750454e0fc18fcb868c553e057)

## Minimum Gradle version

코틀린 설정을 완료했다면 Gradle도 버전에 맞게 올려줍시다!

Kotlin version `2.1.0`으로 설정했으니 Gradle version `8.11`로 사용할 계획입니다.

[Compatibility Matrix](https://docs.gradle.org/current/userguide/compatibility.html#kotlin)

![Result](https://github.com/jihunparkme/blog/blob/main/img/java-tio-kotlin/embedded-kotlin-version.png?raw=true 'Result')

gradle version 설정은 `gradle > wrapper > gradle-wrapper.properties`에서 수정할 수 있습니다.