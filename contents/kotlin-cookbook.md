# Kotlin Cookbook

[코틀린 쿡북](https://www.yes24.com/Product/Goods/90452827) 책을 요약한 내용입니다.

![BOOK](https://github.com/jihunparkme/blog/blob/main/img/kotlin/kotlin-cookbook.jpeg?raw=true)

# 코틀린 기초

## Null 허용 타입

👉🏻 **val 변수의 영리한 타입 변환(smart cast)**

- 널 할당이 불가능한 문자열 타입으로 영리한 변환 가능

```kotlin
val p = Person(first = "North", middle = null, last = "West")
if (p.middle != null) {
    val middleNameLength = p.middle.length
}
``` 

👉🏻 **var 변수가 널 값이 아님을 단언하기 `!!`**

- var 변수는 String 타입으로 영리한 타입 변환이 불가능
- 널 아님 단언 연산자(`!!`, not-null assertion operator)로 널 아님을 단언할 수 있지만, 코드 스멜이다
  - 변수가 널이 아닌 값으로 다뤄지도록 강제하고 해당 변수가 널이라면 예외를 던진다
  - 널 값에 이 연산자를 사용하는 것은 코틀린에서 NPE를 만날 수 있는 몇 가지 상황 중 하나
  - 가능하면 사용하지 않도록 노력하자.

```kotlin
var p = Person(first = "North", middle = null, last = "West")
if (p.middle != null) {
    val middleNameLength = p.middle!!.length
}
``` 

👉🏻 **var 변수에 안전 호출 연산자 사용하기 `?.`**

- 이 상황에서 안전 호출(`?.`, safe call)를 사용하는 것이 좋다.
- 결과 타입은 Type? 형태이고, null 이면 null을 반환

```kotlin
var p = Person(first = "North", middle = null, last = "West")
val middleNameLength = p.middle?.length
``` 

👉🏻 **var 변수에 안전 호출 연산자와 엘비스 연산자 사용하기 `?:`**

- `?:` : 왼쪽 식의 값을 확인해서 해당 값이 널이 아니면 그 값을 리턴, 널이라면 오른쪽 값을 리턴

```kotlin
var p = Person(first = "North", middle = null, last = "West")
val middleNameLength = p.middle?.length ?: 0
``` 

👉🏻 **안전 타입 변환 연산자 `as?`**

- 타입 변환이 올바르게 동작하지 않은 경우 ClassCastException이 발생하는 상황을 방지

```kotlin
val p1 = p as? Person
```