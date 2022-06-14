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