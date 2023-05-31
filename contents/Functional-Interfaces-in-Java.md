# Functional Interfaces in Java

## Functional Interfaces

한 개의 추상 메서드를 가지는 인터페이스

- 인터페이스에 여러 개의 디폴트 메서드가 있더라도 추상 메서드가 하나라면 함수형 인터페이스
- 람다 표현식은 함수형 인터페이스로만 사용 가능
- 함수형 인터페이스를 직접 만들 경우 `@FunctionalInterface` 어노테이션을 사용하면, 해당 인터페이스가 함수형 인터페이스 조건에 충족하는지 검증
  - *Multiple non-overriding abstract methods found in interface com.practice.notepad.CustomFunctionalInterface*

## Functional Interfaces in Java

|함수형 인터페이스|Descripter|Method
|:---|:---|:---|
|Predicate|T -> boolean|boolean test(T t)
|Consumer|T -> void|void accept(T t)
|Supplier|() -> T|T get()
|Function<T, R>|T -> R|R apply(T t)
|Comparator|(T, T) -> int|int compare(T o1, T o2)
|Runnable|() -> void|void run()
|Callable|() -> T|V call()

### Predicate

인자 하나를 받아서 boolean 타입 리턴 (T -> boolean)

```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);
}
```

- IntPredicate
- LongPredicate
- DoublePredicate

`일반적으로 컬렉션의 필터에 적용`

**Predicate in filter()**

```java
Predicate<Integer> noGreaterThan5 =  x -> x > 5;
List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

List<Integer> collect = list.stream()
        .filter(noGreaterThan5)
        .collect(Collectors.toList());

System.out.println(collect); // [6, 7, 8, 9, 10]
```

**Predicate.and()**

```java
Predicate<Integer> noGreaterThan5 = x -> x > 5;
Predicate<Integer> noLessThan8 = x -> x < 8;
List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

List<Integer> collect = list.stream()
        .filter(noGreaterThan5.and(noLessThan8))
        .collect(Collectors.toList());

System.out.println(collect); // [6, 7]
```

**Predicate.or()**

```java
Predicate<String> lengthIs3 = x -> x.length() == 3;
Predicate<String> startWithA = x -> x.startsWith("A");

List<String> list = Arrays.asList("A", "AA", "AAA", "B", "BB", "BBB");

List<String> collect = list.stream()
        .filter(lengthIs3.or(startWithA))
        .collect(Collectors.toList());

System.out.println(collect); // [A, AA, AAA, BBB]
```

**Predicate.negate()**

```java
Predicate<String> startWithA = x -> x.startsWith("A");
List<String> list = Arrays.asList("A", "AA", "AAA", "B", "BB", "BBB");

List<String> collect = list.stream()
        .filter(startWithA.negate())
        .collect(Collectors.toList());

System.out.println(collect); // [B, BB, BBB]
```

**Predicate.test()**

```java
List<String> list = Arrays.asList("A", "AA", "AAA", "B", "BB", "BBB");

System.out.println(StringProcessor.filter(
    list, x -> x.startsWith("A"))); // [A, AA, AAA]

System.out.println(StringProcessor.filter(
    list, x -> x.startsWith("A") && x.length() == 3)); // [AAA]

...

class StringProcessor {
    static List<String> filter(List<String> list, Predicate<String> predicate) {
        return list.stream().filter(predicate::test).collect(Collectors.toList());
    }
}
```

- [Interface Predicate<T>](https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html)
- [Java 8 Predicate Examples](https://mkyong.com/java8/java-8-predicate-examples/)

### Consumer

인자 하나를 받고 아무것도 리턴하지 않음 (T -> void)

```java
@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);
}
```

- IntConsumer
- LongConsumer
- DoubleConsumer

`일반적으로 출력이나 리턴이 없는 void 메서드에 사용`

**Consumer.accept()**

```java
Consumer<String> print = x -> System.out.println(x);
print.accept("java");   // java
```

**example**

```java
List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
forEach(list, (Integer x) -> System.out.println(x));

...

static <T> void forEach(List<T> list, Consumer<T> consumer) {
    for (T t : list) {
        consumer.accept(t);
    }
}
```

- [Interface Consumer<T>](https://docs.oracle.com/javase/8/docs/api/java/util/function/Consumer.html)
- [Java 8 Consumer Examples](https://mkyong.com/java8/java-8-consumer-examples/)


### Supplier

아무런 인자를 받지 않고 T 타입 객체 리턴 (() -> T)

```java
@FunctionalInterface
public interface Supplier<T> {
    T get();
}
```

- BooleanSupplier
- IntSupplier
- LongSupplier
- DoubleSupplier

`일반적으로 객체 생성에 사용`

**Supplier.get()**

```java
private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

...

Supplier<LocalDateTime> s = () -> LocalDateTime.now();
LocalDateTime time = s.get();

System.out.println(time); // 2023-05-22T20:20:20.281223

Supplier<String> s1 = () -> dtf.format(LocalDateTime.now());
String time2 = s1.get();

System.out.println(time2); // 2023-05-22 20:20:49
```

**example**

```java
Developer obj = factory(Developer::new);
System.out.println(obj);

Developer obj2 = factory(() -> new Developer("mkyong"));
System.out.println(obj2);

...

public static Developer factory(Supplier<? extends Developer> s) {
    Developer developer = s.get();
    if (developer.getName() == null || "".equals(developer.getName())) {
        developer.setName("default");
    }
    developer.setSalary(BigDecimal.ONE);
    developer.setStart(LocalDate.of(2017, 8, 8));

    return developer;

}

```

- [Interface Supplier<T>](https://docs.oracle.com/javase/8/docs/api/java/util/function/Supplier.html)
- [Java 8 Supplier Examples](https://mkyong.com/java8/java-8-supplier-examples/)

### Function

T 타입을 인자로 받아서 R 타입 리턴 (T -> R)

```java
@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);
}
```

- IntToDoubleFunction
- IntToLongFunction
- LongToDoubleFunction
- LongToIntFunction
- DoubleToIntFunction
- DoubleToLongFunction
- IntFunction<R>
- LongFunction<R>
- DoubleFunction<R>
- ToIntFunction<T>
- ToDoubleFunction<T>
- ToLongFunction<T>

`일반적으로 인자와 리턴의 타입이 다른 경우 사용`

**Function.apply()**

```java
Function<String, Integer> func = x -> x.length();

Integer apply = func.apply("mkyong"); // chain 으로 사용할 경우 .andThen() 메서드 활용

System.out.println(apply); // 6
```

**example (List -> Map)**

```java
List<String> list = Arrays.asList("node", "c++", "java", "javascript");

Map<String, Integer> map = convertListToMap(list, x -> x.length());
System.out.println(map);    // {node=4, c++=3, java=4, javascript=10}

...

public <T, R> Map<T, R> convertListToMap(List<T> list, Function<T, R> func) {

    Map<T, R> result = new HashMap<>();
    for (T t : list) {
        result.put(t, func.apply(t));
    }
    return result;

}
```

- [Interface Function<T,R>](https://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html)
- [Java 8 Function Examples](https://mkyong.com/java8/java-8-function-examples/)

### Comparator

T 타입 인자 두 개를 받아서 int 타입 리턴 ((T, T) -> int)

```java
@FunctionalInterface
public interface Comparator<T> {
    int compare(T o1, T o2);
}
```

**Comparator.compare()**

```java
Collections.sort(listDevs, new Comparator<Developer>() {
    @Override
    public int compare(Developer o1, Developer o2) {
        return o1.getAge() - o2.getAge();
    }
});

...

// Using lambda
listDevs.sort((Developer o1, Developer o2)->o1.getAge()-o2.getAge());
listDevs.sort((Developer o1, Developer o2)->o1.getName().compareTo(o2.getName()));

// sorting
Comparator<Developer> salaryComparator = (o1, o2)->o1.getSalary().compareTo(o2.getSalary());
listDevs.sort(salaryComparator);
listDevs.sort(salaryComparator.reversed());
```

- [Interface Comparator<T>](https://docs.oracle.com/javase/8/docs/api/java/util/Comparator.html)
- [Java 8 Lambda : Comparator example](https://mkyong.com/java8/java-8-lambda-comparator-example/)

### Runnable

아무런 객체를 받지 않고 리턴도 하지 않음 (() -> void)
- 멀티스레드 작업을 표현하기 위해 제공되는 핵심 인터페이스

```java
@FunctionalInterface
public interface Runnable {
    public abstract void run();
}
```

**결과를 리턴하지 않는 경우(ex. 이벤트 로깅)에 사용**

**implements Runnable**

```java
public class EventLoggingTask implements  Runnable{
    private Logger logger
      = LoggerFactory.getLogger(EventLoggingTask.class);

    @Override
    public void run() {
        logger.info("Message");
    }
}

...

public void executeTask() {
    executorService = Executors.newSingleThreadExecutor();
    Future future = executorService.submit(new EventLoggingTask());
    executorService.shutdown();
}
```

### Callable

아무런 인자를 받지 않고 T 타입 객체 리턴 (() -> T)
- Supplier와 동일한 개념
- Runnable과 병렬 처리를 위해 함께 등장한 개념(Java 1.5에서 Runnable 개선 버전으로 Callable 제공)

```java
@FunctionalInterface
public interface Callable<V> {
    V call() throws Exception;
}
```

**implements Callable**

```java
public class FactorialTask implements Callable<Integer> {
    int number;

    // standard constructors

    public Integer call() throws InvalidParamaterException {
        int fact = 1;
        // ...

        if(number < 0) {
            throw new InvalidParamaterException("Number should be positive");
        }

        for(int count = number; count > 1; count--) {
            fact = fact * count;
        }

        return fact;
    }
}

...

@Test
public void whenTaskSubmitted_ThenFutureResultObtained(){
    FactorialTask task = new FactorialTask(5);
    Future<Integer> future = executorService.submit(task);
 
    assertEquals(120, future.get().intValue());
}

...

@Test(expected = ExecutionException.class)
public void whenException_ThenCallableThrowsIt() {
 
    FactorialCallableTask task = new FactorialCallableTask(-5);
    Future<Integer> future = executorService.submit(task);
    // .get() 메서드를 호출하지 않으면 예외가 발생하지 않음.
    Integer result = future.get().intValue();
}
```

- [Runnable vs. Callable in Java](https://www.baeldung.com/java-runnable-callable)

### BiPredicate

두 개의 인자를 받고 boolean 리턴

- 두 개의 인자를 받는 것을 제외하고, Predicate와 동일

```java
@FunctionalInterface
public interface BiPredicate<T, U> {
    boolean test(T t, U u);
}
```

**BiPredicate.test()**

```java
BiPredicate<String, Integer> filter = (x, y) -> {
    return x.length() == y;
};

boolean result = filter.test("aaron", 5);
System.out.println(result);  // true

boolean result2 = filter.test("java", 10);
System.out.println(result2); // false
```

- [Interface BiPredicate<T,U>](https://docs.oracle.com/javase/8/docs/api/java/util/function/BiPredicate.html)
- [Java 8 BiPredicate Examples](https://mkyong.com/java8/java-8-bipredicate-examples/)

### BiConsumer

두 개의 인자를 받고 void 리턴

```java
@FunctionalInterface
public interface BiConsumer<T, U> {
  void accept(T t, U u);
}
```

**BiConsumer.accept()**

```java
addTwo(1, 2, (x, y) -> System.out.println(x + y));          // 3
addTwo("Node", ".js", (x, y) -> System.out.println(x + y)); // Node.js

...

static <T> void addTwo(T a1, T a2, BiConsumer<T, T> c) {
    c.accept(a1, a2);
}
```

- [Interface BiConsumer<T,U>](https://docs.oracle.com/javase/8/docs/api/java/util/function/BiConsumer.html)
- [Java 8 BiConsumer Examples](https://mkyong.com/java8/java-8-biconsumer-examples/)

### BiFunction

두 개의 인자를 받고 R 타입 객체 리턴
- T: 함수에 대한 첫 번째 인수
- U: 함수의 두 번째 인수
- R: 함수의 결과

```java
@FunctionalInterface
public interface BiFunction<T, U, R> {
    R apply(T t, U u);
}
```

**BiFunction.apply()**

```java
BiFunction<Integer, Integer, Integer> func = (x1, x2) -> x1 + x2;
Integer result = func.apply(2, 3); // 5

BiFunction<Integer, Integer, Double> func2 = (x1, x2) -> Math.pow(x1, x2);
Double result2 = func2.apply(2, 4);  // 16.0

BiFunction<Integer, Integer, List<Integer>> func3 = (x1, x2) -> Arrays.asList(x1 + x2);
List<Integer> result3 = func3.apply(2, 3); // [5]
```

**BiFunction.andThen()**

```java
BiFunction<Integer, Integer, Double> func1 = (a1, a2) -> Math.pow(a1, a2);
Function<Double, String> func2 = (input) -> "Result : " + String.valueOf(input);
String result = func1.andThen(func2).apply(2, 4);

System.out.println(result); // Result : 16.0
```

**example**

```java
String result = powToString(2, 4,
        (a1, a2) -> Math.pow(a1, a2),
        (r) -> "Result : " + String.valueOf(r));

System.out.println(result); // Result : 16.0

...

public static <R> R powToString(Integer a1, Integer a2,
                                    BiFunction<Integer, Integer, Double> func,
                                    Function<Double, R> func2) {
    return func.andThen(func2).apply(a1, a2);
}
```

- [Interface BiFunction<T,U,R>](https://docs.oracle.com/javase/8/docs/api/java/util/function/BiFunction.html)
- [Java 8 BiFunction Examples](https://mkyong.com/java8/java-8-BiFunction-examples/)

## Reference

https://bcp0109.tistory.com/313

- [Functional Interfaces in Java 8](https://www.baeldung.com/java-8-functional-interfaces)
- [Package java.util.function](https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html)
- [Package java.util.concurrent](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html)