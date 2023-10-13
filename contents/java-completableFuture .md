# Java CompletableFuture 

java 5에서 `Future` interface 는 비동기식 연산이 기능하도록 추가되었지만, 

계산을 결합하거나 발생 가능한 오류를 처리할 수 있는 방법은 없었다.

.

java 8은 `Future` interface, `CompleteStage` interface 를 구현한 `CompletableFuture` class 를 선보였다.

CompletableFuture 클래스는 다른 단계들과 결합할 수 있는 비동기 연산 단계에 대한 계약을 정의한다.

추가로, 비동기 연산 단계 및 오류 처리를 위한 약 50가지의 다양한 방법을 제공한다.

## CompletableFuture with Encapsulated Computation Logic

`public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)`
- 인자로 받은 Supplier 를 비동기적으로 호출 후 CompleteFuture 반환

```java
public class EncapsulatedComputationLogic {
    @Test
    void supplyAsync() throws Exception {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");
        assertEquals("Hello", future.get());
    }
}
```

## Processing Results of Asynchronous Computations

`public <U> CompletableFuture<U> thenApply(Function<? super T,? extends U> fn)`
- 인자로 받은 Function 을 사용하여 다음 연산 처리
- Function 이 반환하는 값을 보유하는 CompletableFuture 반환

`public CompletableFuture<Void> thenAccept(Consumer<? super T> action)`
- Consumer 를 인자로 받고, 결과를 CompletableFuture<Void> 로 반환
- CompletableFuture<Void>.get() 호출 시 Void 유형의 인스턴스를 반환

`public CompletableFuture<Void> thenRun(Runnable action)`
 - Runnable 를 인자로 받고, 결과를 CompletableFuture<Void> 로 반환
     
```java
public class ProcessingResultsOfAsynchronousComputations {

    @Test
    void thenApply() throws Exception {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<String> future = completableFuture.thenApply(s -> s + " World");

        assertEquals("Hello World", future.get());
    }

    @Test
    void thenAccept() throws Exception {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<Void> future = completableFuture.thenAccept(s -> System.out.println("Computation returned: " + s));

        future.get(); // 이 단계에서 출력 동작
    }

    @Test
    void thenRun() throws Exception {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<Void> future = completableFuture.thenRun(() -> System.out.println("Computation finished."));

        future.get(); // get() 호출이 없어도 출력 동작
    }
}
```

## Combining Futures

CompletableFuture API 의 가장 중요한 부분은 일련의 연산 단계에서 CompletableFuture 인스턴스를 결합하는 기능

- 이 연결의 결과는 그 자체로 추가 연결 및 결합을 허용하는 CompletableFuture
- 이 접근 방식은 함수형 언어 어디에서나 볼 수 있으며 [Monad Design Pattern](https://medium.com/thg-tech-blog/monad-design-pattern-in-java-3391d4095b3f) 으로 불림

.

`public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn)`
- thenCompose 메소드는 두 개의 Future 를 순차적으로 연결
- 이전 계산 단계의 결과를 인자로 받고, 이 값을 다음 CompletableFuture 안에서 사용

```java
CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello")
        .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + " World"));

assertEquals("Hello World", completableFuture.get());
```

.

`public <U,V> CompletableFuture<V> thenCombine(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn)`

- thenCompose 메소드는 thenApply 와 함께 Monad Design Pattern 기본 구성 요소를 구현
- Java 8 에서도 사용할 수 있는 Stream 및 Optional 클래스의 map 및 flatMap 메소드와 밀접하게 관련
- 두 메서드 모두 함수를 받아 계산 결과에 적용하지만, thenCompose(flatMap) 메서드는 같은 유형의 다른 객체를 반환하는 함수를 받음
- 이 기능적 구조를 통해 이러한 클래스의 인스턴스를 빌딩 블록으로 구성 가능

두 개의 독립적인 Future 를 실행하고 그 결과로 작업을 수행하려면
- Future 와 두 개의 인수가 있는 Function 을 받아들이는 thenCombine 메서드를 사용하여 두 결과를 모두 처리 가능

```java
CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello")
        .thenCombine(CompletableFuture.supplyAsync(() -> " World"), (s1, s2) -> s1 + s2);

assertEquals("Hello World", completableFuture.get());
```

.

`public <U> CompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action)`

- 더 간단한 경우는 두 개의 Future 결과로 무언가를 하고 싶지만 결과 값을 Future 체인으로 전달할 필요가 없는 경우

```java
CompletableFuture future = CompletableFuture.supplyAsync(() -> "Hello")
        .thenAcceptBoth(CompletableFuture.supplyAsync(() -> " World"), (s1, s2) -> System.out.println(s1 + s2));
```

## Difference Between thenApply() and thenCompose()

`thenApply()`
- 이전 호출의 결과를 처리
- 반환 유형이 모든 호출에 결합
- CompletableFuture 호출의 결과를 변환할 경우 유용

```java
CompletableFuture<Integer> finalResult = compute().thenApply(s-> s + 1);
```

`thenCompose()`

- 새로운 CompletionStage 반환에서 thenApply()와 유사
- 단, thenCompose() 는 이전 단계를 인수로 사용
- thenApply() 처럼 중첩된 future 가 아닌 결과가 포함된 Future 를 직접 평면화하고 반환

```java
CompletableFuture<Integer> computeAnother(Integer i){
    return CompletableFuture.supplyAsync(() -> 10 + i);
}
CompletableFuture<Integer> finalResult = compute().thenCompose(this::computeAnother);
```

따라서 CompletableFuture 메소드를 연결할 경우 thenCompose() 를 사용하는 것이 더 적합
- 이 두 메서드의 차이점은 map() 과 flatMap() 의 차이점과 유사

## Running Multiple Futures in Parallel

`public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs)`

여러 Future 를 병렬로 실행해야 하는 경우 일반적으로 모든 Future 가 실행될 때까지 기다린 다음 결합된 결과를 처리하려고 시도
- CompletableFuture.allOf() 정적 메소드를 사용하면 var-arg 로 제공되는 모든 Future 가 완료될 때까지 대기

CompletableFuture.allOf() 반환 유형은 CompletableFuture<Void>
- 이 방법의 한계는 모든 Future 의 결합된 결과를 반환하지 않는다는 점
- 대신, Futures 에서 수동으로 결과를 얻어야 함
- CompletableFuture.join() 메서드와 Java 8 Streams API 를 사용하면 간단

CompletableFuture.join() 메소드는 get 메소드와 유사하지만 Future 가 정상적으로 완료되지 않는 경우 확인되지 않은 예외 발생
- 이를 통해 Stream.map() 메서드에서 메서드 참조로 사용 가능

```java
CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "Beautiful");
CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> "World");

CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(future1, future2, future3);
combinedFuture.get();

assertTrue(future1.isDone());
assertTrue(future2.isDone());
assertTrue(future3.isDone());

String combined = Stream.of(future1, future2, future3)
        .map(CompletableFuture::join)
        .collect(Collectors.joining(" "));

assertEquals("Hello Beautiful World", combined);
```

## Handling Errors

비동기 처리 단계에서 오류를 처리하려면 비슷한 방식으로 throw/catch 관용구 적용
- 구문 블록에서 예외를 잡는 대신 CompletableFuture 클래스를 사용하면 특수 핸들 메서드에서 예외 처리 가능
- handle() 메서드는 계산 결과(성공적으로 완료된 경우)와 발생한 예외(일부 계산 단계가 정상적으로 완료되지 않은 경우) 두 가지 매개 변수를 받음

`public <U> CompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn)`

```java
String name = null;

//...

CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
    if (name == null) {
        throw new RuntimeException("Computation error!");
    }
    return "Hello, " + name;
}).handle((s, t) -> s != null ? s : "Hello, Stranger!");

// 핸들 메서드를 사용하여 기본값 제공
assertEquals("Hello, Stranger!", completableFuture.get());
```

completeExceptionally() 메서드는 예외를 사용하여 처리를 완료할 수 있는 기능

`public boolean completeExceptionally(Throwable ex)`

```java
CompletableFuture<String> completableFuture = new CompletableFuture<>();

//...

completableFuture.completeExceptionally(
        new RuntimeException("Calculation failed!"));
//...

completableFuture.get(); // ExecutionException
```

## Async Methods

CompletableFuture 클래스에 있는 Fluent API 대부분 메서드에는 Async 접미사가 있는 두 가지 추가 변형이 존재
- 이러한 메서드는 일반적으로 다른 스레드에서 해당 실행 단계를 실행하기 위한 것
- Async 접미사가 없는 메서드는 호출 스레드를 사용하여 다음 실행 단계를 실행
- 대조적으로 Executor 인수가 없는 Async 메서드
  - 병렬 처리가 1보다 큰 경우 ForkJoinPool.commonPool()로 액세스되는 Executor의 공통 포크/조인 풀 구현을 사용하여 단계를 실행
- Executor 인수가 있는 Async 메서드
  - 전달된 Executor를 사용하여 단계를 실행

Function 인스턴스를 사용하여 계산 결과 처리
- 눈에 보이는 유일한 차이점은 thenApplyAsync 메서드이지만 내부적으로는 함수 적용이 ForkJoinTask 인스턴스로 래핑
- 이를 통해 계산을 더욱 병렬화하고 시스템 리소스를 보다 효율적으로 사용 가능
- [Guide to the Fork/Join Framework in Java](https://www.baeldung.com/java-fork-join)

```java
CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello");

CompletableFuture<String> future = completableFuture
  .thenApplyAsync(s -> s + " World");

assertEquals("Hello World", future.get());
```

## Example

리스트를 순회하며 문자열 타입 결과를 리스트로

```java
@Test
void returnSingleTypeTest() throws Exception {
    ArrayList<String> list = new ArrayList<>();
    list.add("A");
    list.add("B");
    list.add("C");
    list.add("D");

    var stringList = list.stream()
            .map(hour -> CompletableFuture.supplyAsync(() -> doSomeThing(hour)))
            .collect(Collectors.toList());

    List<String> collect = stringList.stream().map(c -> {
                String result = "";
                try {
                    result = c.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println(e.getMessage());
                }
                return result;
            })
            .collect(Collectors.toList()); // 8초 뒤 결과 리턴

    collect.forEach(System.out::println);
}

private String doSomeThing(String input) {
    System.out.println("doSomeThing: " + input);
    try {
        if ("A".equals(input)) {
            Thread.sleep(30000);
        } else if ("B".equals(input)) {
            Thread.sleep(1000);
        } else if ("C".equals(input)) {
            Thread.sleep(8000);
        } else if ("D".equals(input)) {
            Thread.sleep(5000);
        }
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    return "End - " + input;
}

...

doSomeThing: B
doSomeThing: D
doSomeThing: A
doSomeThing: C
End - A
End - B
End - C
End - D
```

리스트를 순회하며 문자열 리스트 결과를 리스트로

```java
@Test
void returnListTypeTest() throws Exception {
    ArrayList<String> list = new ArrayList<>();
    list.add("A");
    list.add("B");
    list.add("C");
    list.add("D");

    var listOfStringList = list.stream()
            .map(s -> CompletableFuture.supplyAsync(() -> goSomeThingReturnList(s)))
            .collect(Collectors.toList());

    List<String> result = listOfStringList.stream().map(c -> {
                List<String> rst = Collections.emptyList();
                try {
                    rst = c.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println(e.getMessage());
                }
                return rst;
            })
            .flatMap(Collection::stream).collect(Collectors.toList());  // 5초 뒤 결과 리턴

    result.forEach(System.out::println);
}

private List<String> goSomeThingReturnList(String input) {
    System.out.println("doSomeThing: " + input);
    try {
        Thread.sleep(5000);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    ArrayList<String> result = new ArrayList<>();
    result.add(input + " - 1");
    result.add(input + " - 2");
    result.add(input + " - 3");

    return result;
}

...

doSomeThing: D
doSomeThing: C
doSomeThing: B
doSomeThing: A
A - 1
A - 2
A - 3
B - 1
B - 2
B - 3
C - 1
C - 2
C - 3
D - 1
D - 2
D - 3
```

---

**Reference**

- [Guide To CompletableFuture](https://www.baeldung.com/java-completablefuture)
- [Java 9 CompletableFuture API Improvements](https://www.baeldung.com/java-9-completablefuture)