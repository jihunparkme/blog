[Java] 자바 플레이그라운드 with TDD, CleanCode 후기 (2)

# Java PlayGround


NEXTSTEP [자바 플레이그라운드 with TDD, 클린 코드](https://edu.nextstep.camp/c/9WPRB0ys/)에서 새롭게 배우고 깨닫게 된 내용들을 기록한 글입니다.

## Inheritance

**상속을 통한 중복 코드 제거**

- 중복 코드를 `별도의 클래스`로 분리해보자.

**extends**

- 부모 클래스의 모든 `필드`와 `메소드`를 자식 클래스가 상속하도록 지원하는 keyword
- 상속을 할 경우 `멤버 필드`와 `메소드`를 하위 클래스에서 그대로 상속

## Abstract

**추상화를 통한 중복 제거**

- 역할이 비슷한 메서드를 `추상화` 시켜 중복을 제거해보자.

**abstract**

- 클래스를 추상 클래스로 지정할 때 사용
- 클래스를 abstract로 지정하면 new keyword를 통해 객체를 직접 생성할 수 없음
- 메소드에 abstract를 사용할 경우 interface의 메소드와 같이 구현 부분은 없음
- abstract로 선언한 메소드는 자식 클래스에서 반드시 구현

### Results

**Before**

```java
// Coffee.java
public class Coffee {
    void prepareRecipe() {
        boilWater();
        brewCoffeeGrinds();
        pourInCup();
        addSugarAndMilk();
    }

    public void boilWater() {  // 1. 중복 코드
        System.out.println("물을 끓인다.");
    }

    public void brewCoffeeGrinds() { // 3. brew() 로 추상화
        System.out.println("필터를 활용해 커피를 내린다.");
    }

    public void pourInCup() { // 2. 중복 코드
        System.out.println("컵에 붓는다.");
    }

    public void addSugarAndMilk() { // 4. addCondiments() 로 추상화
        System.out.println("설탕과 우유를 추가한다.");
    }
}

// Tea.java
public class Tea {
    void prepareRecipe() {
        boilWater();
        steepTeaBag();
        pourInCup();
        addLemon();
    }

    public void boilWater() { // 1. 중복 코드
        System.out.println("물을 끓인다.");
    }

    public void steepTeaBag() { // 3. brew() 로 추상화
        System.out.println("티백을 담근다.");
    }

    public void pourInCup() { // 2. 중복 코드
        System.out.println("컵에 붓는다.");
    }

    public void addLemon() { // 4. addCondiments() 로 추상화
        System.out.println("레몬을 추가한다.");
    }
}
```

**After**

```java
// CaffeineBeverage.java
public abstract class CaffeineBeverage {
    abstract void brew();
    
    abstract void addCondiments();
    
    void prepareRecipe() {
        boilWater();
        brew();
        pourInCup();
        addCondiments();
    }
    
    protected void boilWater() {
        System.out.println("물을 끓인다.");
    }

    protected void pourInCup() {
        System.out.println("컵에 붓는다.");
    }
}

// Coffee.java
public class Coffee extends CaffeineBeverage {
    public void brew() {
        System.out.println("필터를 활용해 커피를 내린다.");
    }

    public void addCondiments() {
        System.out.println("설탕과 우유를 추가한다.");
    }
}

// Tea.java
public class Tea extends CaffeineBeverage {
    public void brew() {
        System.out.println("티백을 담근다.");
    }

    public void addCondiments() {
        System.out.println("레몬을 추가한다.");
    }
}
```

### 업캐스팅(upcasting)

- 하위 클래스를 상위 클래스로 타입을 변환

```java
CaffeineBeverage beverage = new Coffee();
CaffeineBeverage beverage = new Tea();
```

### 다운캐스트(downcasting)

- 상위 클래스를 하위 클래스의 타입으로 변환

```java
CaffeineBeverage beverage = new Coffee();

if (beverage  instanceof Coffee) {
    Coffee coffee = (Coffee)beverage;
}
```

## Interface

- 자바에서 한 단계 `더 높은 추상화`를 하기 위해 사용
- 구현 로직은 존재하지 않으며 메소드에 대한 `입력`, `출력`만 정의
- 소프트웨어에 변경이 발생할 경우 소스 코드에 변경을 최소화함으로써 유지보수 비용을 줄이고, 변화에 빠르게 대응하기 위해 사용
  - 추상화를 통해 변화에 빠르게 대응할 수 있지만 추상화에 따른 개발 비용이 발생

## Coordinate Example

### Interface

- 모든 도형에서 `비슷한 동작`을 수행하는 공통 메서드를 추상화

```java
public interface Figure {
    boolean hasPoint(int x, int y);

    double area();

    String getAreaInfo();
}
```

### Abstract Class

- 모든 도형에서 `동일한 동작`을 수행하는 공통 메서드를 추상화
- 공통적으로 가지고 있는 데이터도 포함

```java
@Getter
@EqualsAndHashCode
public abstract class AbstractFigure implements Figure {
    static final String ERROR_FIGURE_NULL = "올바른 Point 값이 아닙니다.";
    private final List<Point> points;

    AbstractFigure(List<Point> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException(ERROR_FIGURE_NULL);
        }
        this.points = points;
    }

    @Override
    public boolean hasPoint(int x, int y) {
        return getPoints().stream()
                .anyMatch(point -> point.isSame(x, y));
    }
}
```

### FigureFactory

- Map을 활용하면 if문을 사용하지 않고 객체를    구분하여 클래스를 만들 수 있다.
- 아주.. 좋은 방법인 것 같다.

```java
public class FigureFactory {
    private static final String ERROR_INVALID_FIGURE_CREATION = "입력된 Point 개수가 유효하지 않습니다.";
    private static final int NUM_OF_VERTICES_OF_LINE = 2;
    private static final int NUM_OF_VERTICES_OF_TRIANGLE = 3;
    private static final int NUM_OF_VERTICES_OF_RECTANGLE = 4;
    private static final Map<Integer, Function<List<Point>, Figure>> classifier = new HashMap<>();

    static {
        classifier.put(NUM_OF_VERTICES_OF_LINE, Line::new);
        classifier.put(NUM_OF_VERTICES_OF_TRIANGLE, Triangle::new);
        classifier.put(NUM_OF_VERTICES_OF_RECTANGLE, Rectangle::new);
    }

    public static Figure create(List<Point> points) {
        if (points == null) {
            throw new IllegalArgumentException(AbstractFigure.ERROR_FIGURE_NULL);
        }
        if (isInvalidNumberOf(points)) {
            throw new IllegalArgumentException(ERROR_INVALID_FIGURE_CREATION);
        }
        return classifyFigure(points);
    }

    private static boolean isInvalidNumberOf(List<Point> points) {
        int numOfPoints = points.size();
        return numOfPoints < NUM_OF_VERTICES_OF_LINE || numOfPoints > NUM_OF_VERTICES_OF_RECTANGLE;
    }

    private static Figure classifyFigure(List<Point> points) {
        return classifier.get(points.size()).apply(points);
    }
}
```

### Class

- AbstractFigure의 implements인 Figure의 메서드를 구현

```java
public class Line extends AbstractFigure {
    private static final String OUTPUT_AREA_OF_LINE = "두 점 사이의 거리는 ";

    Line(List<Point> points) {
        super(points);
    }

    @Override
    public double area() {
        return getPoints().get(0).calculateDistance(getPoints().get(1));
    }

    @Override
    public String getAreaInfo() {
        return OUTPUT_AREA_OF_LINE + area();
    }
}
```

> [repository](https://github.com/jihunparkme/Study-project-spring-java/tree/main/java-coordinate-playground-practice)

## 함수형 프로그래밍

**동시성**

- 멀티 CPU의 동시성 이슈를 해결하면서 안정적인 소프트웨어를 개발하는 것이 중요
- 데이터의 상태를 변경하는 과정에서 객체 지향 프로그래밍 방식으로 동시성 문제를 해결하는데는 한계가 존재
  - 대용량 데이터를 처리할 때 데이터를 객체로 변환하는데 따른 부담

**모듈화**

- 더 유용하고, 재사용이 편리하고, 구성이 용이하고, 테스트하기 더 간편한 추상화를 제공
- 함수형 프로그래밍을 통해 문제 접근 방법, 문제를 작은 단위로 쪼개는 방법, 설계 과정, 프로그래밍 순서에서 새로운 시각을 배울 수 있음
- 객체지향은 객체에 대한 모델링에 많은 시간 투자가 필요

**함수형 프로그래밍**

- 변경 불가능한 값(immutable value) 활용하여 동시성 이슈 발생 가능성을 줄임
- 클래스 없이 독립적으로 함수가 존재
- 람다와 클로저 사용
- 고계함수(higher-order function)
  - 다른 함수를 인수로 받아들이거나 함수를 리턴하는 함수
- 함수가 불변(immutable)인 특성을 가지므로 부수효과(side effect)가 없음
  - 멀티 스레드 환경에서 안정적
  
### 람다(lambda)

람다는 익명 함수의 다른 표현

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);

numbers.forEach((Integer value) -> System.out.println(value));
numbers.forEach(value -> System.out.println(value)); // Type 추론이 가능해 Type 생략 가능
numbers.forEach(System.out::println); // :: 연산자 활용 가능
numbers.forEach(x -> System.out.println(x));
```

### 스트림(stream)

**filter**

```java
List<String> words = Arrays.asList(contents.split("[\\P{L}]+"));

// using for loop
long count = 0;
for (String w : words) {
  if (w.length() > 12) {
    count++;  
  }
}

// using stream
long count = words.stream()
                    .filter(w -> w.length() > 12)
                    .count();
```

**map**

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);
List<Integer> dobuleNumbers = numbers.stream()
                                    .map(x -> 2 * x)
                                    .collect(Collectors.toList());
```

**reduce**

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);

public int sumAll(List<Integer> numbers) {
    return numbers.stream()
                    .reduce(0, (x, y) -> x + y);
}
```