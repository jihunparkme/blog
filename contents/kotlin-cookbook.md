# Kotlin Cookbook

[코틀린 쿡북](https://www.yes24.com/Product/Goods/90452827) 책을 요약한 내용입니다.

![BOOK](https://github.com/jihunparkme/blog/blob/main/img/kotlin/kotlin-cookbook.jpeg?raw=true)

[Kotlin Cookbook samples](https://github.com/kousen/kotlin-cookbook)

# 코틀린 기초

## Null 허용 타입

> 변수가 null 값을 갖지 못하게 하려면
>
> 안전 호출 연산자(?.)나 엘비스 연산자(?:)와 결합해서 사용하자.

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

## 명시적 타입 변환

> 코틀린은 자동으로 기본 타입을 더 넓은 타입으로 승격하지 않는다. Int -> Long (X)
>
> 더 작은 타입을 명시적으로 변환하려면 toInt, toLong 등 구체적인 변환 함수를 사용하자.

```kotlin
val intVar: Int = 3
// val longVar: Long = intVal // failed Compile
val longVar: Long = intVar.toLong()

...

// 단 연산자 중복 시 명시적 타입 변환 불필요
val longSum = 3L + intVar
```

사용 가능한 타입 변환 메소드
- toByte(): Byte
- toChar(): Char
- toShort(): Short
- toInt(): Int
- toLong(): Long
- toFloat(): Float
- toDouble(): Double

## 중위함수

> 코틀린에는 자바처럼 내장 거듭제곱 연산자가 없다.

👉🏻 **시그니처의 확장 함수를 정의**

```kotlin
fun Int.pow(x: Int) = toDouble().pow(x).toInt()
fun Long.pow(x: Int) = toDouble().pow(x).toLong()
```

👉🏻 **중위 연산자 infix 정의**

```kotlin
import kotlin.math.pow

infix fun Int.`**`(x: Int) = toDouble().pow(x).toInt()
infix fun Long.`**`(x: Int) = toDouble().pow(x).toLong()
infix fun Float.`**`(x: Int) = pow()
infix fun Double.`**`(x: Int) = pow()

fun Int.pow(x: Int) = `**`(x)
fun Long.pow(x: Int) = `**`(x)

...

@Test
fun `raise to pwoer`() {
    assertAll(
        { assertEquals(1, 2 `**` 0) },
        { assertEquals(2, 2 `**` 1) },
        { assertEquals(4, 2 `**` 2) },
        { assertEquals(8, 2 `**` 3) },

        { assertEquals(1L, 2L `**` 0) },
        { assertEquals(2L, 2L `**` 1) },
        { assertEquals(4L, 2L `**` 2) },
        { assertEquals(8L, 2L `**` 3) },

        { assertEquals(1F, 2F `**` 0) },
        { assertEquals(2F, 2F `**` 1) },
        { assertEquals(4F, 2F `**` 2) },
        { assertEquals(8F, 2F `**` 3) },

        { assertEquals(1.0, 2.0 `**` 0) },
        { assertEquals(2.0, 2.0 `**` 1) },
        { assertEquals(4.0, 2.0 `**` 2) },
        { assertEquals(8.0, 2.0 `**` 3) },

        { assertEquals(1, 2.pow(0)) },
        { assertEquals(2, 2.pow(1)) },
        { assertEquals(4, 2.pow(2)) },
        { assertEquals(8, 2.pow(3)) },

        { assertEquals(1L, 2L.pow(0)) },
        { assertEquals(2L, 2L.pow(1)) },
        { assertEquals(4L, 2L.pow(2)) },
        { assertEquals(8L, 2L.pow(3)) },
    )
}
```