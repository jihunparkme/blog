# Basic Refactoring

[참고1](https://github.com/wickedwukong/martin-fowler-refactoring-2nd/tree/master/src/chp6)

[참고2](https://github.com/devJang/refactoring-2nd-edition/tree/master/chapter-06/src)

[참고](https://github.com/yujeongJeon/yujeongJeon.github.io)

## 리팩터링 전에 테스트 구축하기

`모든 테스트를 완전히 자동화하고 그 결과까지 스스로 검사하게 만들자.`

`실패해야 할 상황에서는 반드시 실패하게 만들자.`

`문제가 생길 가능성이 있는 경계 조건을 생각해보고 그 부분을 집중적으로 테스트하자.`

`어차피 모든 버그를 잡아낼 수는 없다고 생각하여 테스트를 작성하지 않는다면 대다수의 버그를 잡을 수 있는 기회를 날리는 셈이다.`

`너무 많은 테스트는 의욕을 떨어지게 만들 수 있다. 따라서 위험한 부분에 집중하자. 처리 과정이 복잡하거나 함수에서 오류가 생길만한 부분을 찾아보자.`

`버그 리포트를 받으면 가장 먼저 그 버그를 드러내는 단위 테스트부터 작성하자.`

`누군가 결함을 심으면 테스트가 발견할 수 있다는 믿음을 기준으로 테스트 코드를 작성하자.`

# Chapter 6. 기본적인 리팩터링

## 함수 추출하기

`목적과 구현을 분리하는 방식`

반대 리팩터링 : 함수 인라인하기

**개요**

Before

```javascript
function printOwing(invoice) {
    printBanner();

    let outstanding = calculatorOutstanding();

    // 세부 사항 출력
    console.log(`고객명: ${invoice.customer}`);
    console.log(`채무액: ${outstanding}`);
}
```

After

```javascript
function printOwing(invoice) {
    printBanner();
    let outstanding = calculatorOutstanding();
    printDetails(outstanding);

    function printDetails(outstanding) {
        console.log(`고객명: ${invoice.customer}`);
        console.log(`채무액: ${outstanding}`);
    }
}
```

**절차**

1. 함수를 새로 만들고 목적(무엇을 하는지)을 잘 드러내는 이름 붙이기.
2. 추출할 코드를 새 함수에 복사
3. 참조하는 변수는 매개변수로 전달
   - 원본 함수의 지역 변수
   - 추출한 함수의 유효범위를 벗어나는 변수
4. 추출한 코드 부분을 새로 만든 함수를 호출하는 문으로 수정
5. 테스트
6. 유사한 코드 확인

## 함수 인라인하기

`함수 본문이 이름만큼 명확한 경우, 함수 코드를 이름만큼 깔끔하게 리팩터링할 경우`

반대 리팩터링 : 함수 추출하기

**개요**

Before

```javascript
function raiting(aDriver) { return moreThanFiveLateDeliveries(aDriver) ? 2 : 1; }
function moreThanFiveLateDeliveries(aDriver) { return aDriver.numberOfLaterDeliveries > 5; }
```

After

```javascript
function raiting(aDriver) {  return aDriver.numberOfLaterDeliveries > 5 ? 2 : 1; }
```

**절차**

1. 다형 메서드인지 확인
   - 서브클래스에서 오버라이드하는 메서드는 인라인 금지
2. 인라인할 함수를 호출하는 곳을 모두 찾기
3. 각 호출문을 함수 본문으로 교체
4. 하나씩 교체할 때마다 테스트
5. 함수 정의 삭제

## 변수 추출하기

`표현식이 복잡하여 이해하기 어려울 경우 지역 변수를 활용하여 표현식을 관리`

- 디버거 breakpoint를 지정하거나 상태 출력 문장을 추가하면 디버깅에 도움이 된다.
- 문맥을 고려하여 현재 선언된 함수보다 더 넓은 문맥에서까지의 의미가 된다면 함수로 추출 권장

반대 리팩터링 : 변수 인라인하기

**개요**

Before

```javascript
function price(order) {
    // 가격(price) = 기본 가격 - 수량 할인 + 배송비
    return (
        order.quantity * order.itemPrice -
        Math.max(0, order.quantity - 500) * order.itemPrice * 0.05 +
        Math.min(order.quantity * order.itemPrice * 0.1, 0.05)
    );
}
```

After

```javascript
function price(order) {
    const basePrice = order.quantity * order.itemPrice;
    const quantityDiscount = Math.max(0, order.quantity - 500) * order.itemPrice * 0.05;
    const shipping = Math.min(basePrice * 0.1, 100);

    return basePrice - quantityDiscount + shipping;
}
```

**절차**

1. 추출할 표현식에 사이드이펙트가 없는지 확인
2. 불변 변수를 하나 선언 후 이름을 붙일 표현식의 복제본 대입해보기
   - 함수가 클래스 전체에 영향을 줄 떄는 변수가 아닌 메서드로 추출
3. 원본 표현식을 새로 만든 변수로 교체
4. 테스트
5. 표현식을 여러 곳에서 사용할 경우 각각을 새로 만든 변수로 교체

## 변수 인라인하기

`변수명이 원래 표현식과 다를 바 없을 때`

반대 리팩터링 : 변수 추출하기

**개요**

Before

```javascript
((anOrder) => {
    let basePrice = anOrder.basePrice;
    return basePrice > 1000;
})(anOrder);
```

After

```javascript
((anOrder) => {
    return anOrder.basePrice > 1000;
})(anOrder);
```

**절차**

1. 인라인할 표현식에 사이드 이펙트가 없는지 확인
2. 상수인지 확인하고 상수로 수정 후 테스트
   - 이 경우 변수에 값이 단 한 번만 대입되는지 확인 가능
3. 변수를 표현식으로 교체
4. 테스트
5. 변수를 사용하는 부분 모두 교체 시까지 이 과정 반복
6. 변수 선언문과 대입문 지우기
7. 테스트

## 함수 선언 바꾸기

`호출문만 보고도 무슨 일을 하는지 파악할 수 있도록 함수 이름을 좋게 만들자.`

- 주석을 이용해 함수의 목적을 설명해보면 좋은 이름이 떠오를 것이다.

다른 이름 : 함수 이름 바꾸기, 매개변수 바꾸기

**개요**

간단한 절차

```javascript
// Before
function circum(radius) {}

// After
function circumference(radius) {}
```

마이그레이션 절차

```javascript
// Before
function circum(radius) {
    return 2 * Math.PI * radius;
}

// After
function circum(radius) {
    return circumference(radius);
}

function circumference(radius) {
    return 2 * Math.PI * radius;
}
```

**절차**

함수 이름 바꾸기, 매개변수 바꾸기에 모두 적용

마이그레이션 절차의 복잡도에 따라 **간단한 절차**와 **마이그레이션 절차**로 구분지어 따름

*간단한 절차*

1. 함수 본문에 제거 대상 매개변수를 참조하는 곳이 없는지 확인
2. 메서드 선언 형태 변경
3. 기존 메서드 선언을 참조하는 부분을 바뀐 형태로 수정
4. 테스트

*마이그레이션 절차* 

(간단한 절차 적용 문제 발생 시)

1. 함수 본문 리팩터링
   - 함수/변수 추출 등
2. 함수 본문에서 새로운 함수로 추출
3. 추출한 함수에 매개변수 추가 시 간단한 절차를 따라 추가
4. 어서션을 추가하여 새로 추가한 매개변수를 실제 사용하는지 확인 (javascript)
5. 테스트
6. 기존 함수 인라인 처리
7. 임시 이름을 붙인 새 함수를 원래 이름으로 수정
8. 테스트

## 변수 캡슐화하기

`자주 사용하는 가변 데이터에 대한 결합도가 높아지는 막기 위해, 데이터의 유효범위가 넓을수록 캡슐화하자.`

- 데이터 변경 전 검증이나 변경 후 추가 로직을 쉽게 끼워넣을 수 있다.

**개요**

Before

```javascript
let defaultOwner = { firstName: '마틴', lastName: '파울러'};
```

After

```javascript
let defaultOwnerData = { firstName: '마틴', lastName: '파울러' };
export function defaultOwner() { return defaultOwnerData; }
export function setDefaultOwner(arg) { defaultOwnerData = arg; }
```

**절차**

1. 변수 접근/갱신을 전담하는 Getter/Setter 만들기
2. 정적 검사 수행
3. 변수를 직접 참조하던 부분을 적절한 캡슐화 함수 호출로 변경 (테스트 병행)
4. 변수의 접근 범위 제한
5. 테스트
6. 변수 값이 레코드일 경우 레코드 캡슐화 적용 고려

`캡슐화할 데이터를 사용하는 방식과 어떻게 변경할지에 따라 캡슐화의 구체적인 대상과 방법을 정하자.`

```javascript
let defaultOwnerData = { firstName: '마틴', lastName: '파울러' };
export function defaultOwner() { return new Person(defaultOwnerData); }
export function setDefaultOwner(arg) { defaultOwnerData = arg; }

class Person {
    constructor(data) {
        this._lastName = data.lastName;
        this._firstName = data.firstName;
    }

    get lastName() { return this._lastName; }

    get firstName() { return this._firstName; }
}
```



## 변수 이름 바꾸기

`변수는 프로그래머가 하려는 일에 관해 많은 것을 설명해준다.`

**개요**

Before

```javascript
let a = height * width;
```

After

```javascript
let area = height * width;
```

**절차**

1. 폭넓게 쓰이는 변수라면 변수 캡슐화 고려하기
2. 변경할 변수를 참조하는 곳을 하나씩 변경하기
   - 해당 함수에서만 유효한 변수
3. 테스트

## 매개변수 객체 만들기

`데이터 뭉치를 데이터 구조로 묶으면 데이터 사이의 관계가 명확해진다.`

- +, 매개변수 수 감소, 원소 참조의 일관성
- 데이터 구조를 클래스로 만들어두면 관련 동작들을 해당 클래스로 옮길 수 있다.

**개요**

Before

```javascript
function amountInvoiced(startDate, endDate) {}
function amountReceived(startDate, endDate) {}
function amountOverdue(startDate, endDate) {}
```

After

```javascript
function amountInvoiced(aDateRange) {}
function amountReceived(aDateRange) {}
function amountOverdue(aDateRange) {}
```

**절차**

1. 데이터 구조 만들기
2. 함수 선언 바꾸기로 매개변수에 새 데이터 구조 추가
3. 함수 호출 시 인스턴스 수정
4. 기존 매개변수를 사용하던 코드를 새 데이터 구조를 사용하도록 수정
5. 기존 매개변수 제거
6. 각 단계별 테스트

## 여러 함수를 클래스로 묶기

`클래스로 묶으면 함수들이 공유하는 공통 환경을 더 명확하게 표현`

`각 함수에 전달되는 인수를 줄여 객체 안에서의 함수 호출을 간결하게`

**개요**

Before

```javascript
function base(aReading) {}
function taxableCharge(aReading) {}
function calculateBaseChange(aReading) {}
```

After

```javascript
class Reading {
    base() {}
    taxableCharge() {}
    calculateBaseChange() {}
}
```

**절차**

1. 함수들이 공유하는 공통 데이터 레코드 캡슐화
2. 공통 레코드를 사용하는 함수 각각을 새 클래스로 옮기기
   - 멤버 변수는 함수 호출문의 인수에서 제거
3. 데이터를 조작하는 로직들은 함수로 추출하여 새 클래스로 옮기기

## 여러 함수를 변환 함수로 묶기

`변환 함수는 원본 데이터를 입력받아서 필요 정보를 모두 두출한 후, 각 출력 데이터 필드에 넣어 반환한다.`

`검색과 갱신을 일관된 장소에서 처리할 수 있고, 로직 중복도 막을 수 있다.`

`원본 데이터가 코드 안에서 갱신될 때는 클래스로 묶기를 적용하자.`

- 데이터 일관성을 지켜야 한다면(원본 데이터 변경을 막기 위한 목적이 있다면) 클래스 묶기를 적용하자.

**개요**

Before

```javascript
function base(aReading) {}
function taxableCharge(aReading) {}
```

After

```javascript
function enrichReading(argReading) {
    const aReading = _.cloneDeep(argReading);
    aReading.baseCharge = base(aReading);
    aReading.taxableCharge = taxableCharge(aReading);
    // 원본 데이터는 변경면 안됨. (원본 데이터 확인 테스트 작성 필요)
    return aReading;
}
```

**절차**

1. 입력된 값을 그대로 반환하는 변환 함수 생성
   - 깊은 복사 처리
2. 묶을 함수의 본문 코드를 변환 함수로 옮기고, 처리 결과를 새 필드로 기록
   - 로직이 복잡하면 함수 추출 먼저 적용
3. 테스트
4. 나머지 관련 함수도 적용

## 단계 쪼개기

`서로 다른 두 대상을 한꺼번에 다루는 경우 각각을 별개의 모듈로 나누자.`

`동작을 두 단계로 쪼개는 방법`

**개요**

Before

```javascript
const orderData = orderString.split(/\s+/);
const productPrice = priceList[orderData[0].split('-')[1]];
const orderPrice = parseInt(orderData[1]) * productPrice;
```

After

```javascript
const orderRecord = parseOrder(order)
const orderPrice = parseInt(orderData[1]) * productPrice;

function parseOrder(aString) {
    const values = aString.split(/\s+/);
    return ({
        productID: values[0].split('-')[1]
        quantity: parseInt(values[1])
	})
}
function price(order, priceList) {
    return order.quantity * priceList[order.productID]
}
```

**절차**

1. 두 번째 단계 처리 코드를 독립 함수로 추출
2. 테스트
3. 중간 데이터 구조(ex. Class)를 만들어 전 단계에서 추출한 함수의 인수로 추가
4. 테스트
5. 추출한 두 번째 단계 함수의 매개변수를 검토
   - 첫 번째 단계에 사용된 매개변수는 중간 데이터 구조(ex. Class)로 옮기기
6. 첫 번째 단계 코드를 함수로 추출하면서 중간 데이터 구조 반환

**Example**

```javascript
function priceOrder(product, quantity, shippingMethod) {
    const priceData = calculatePricingData(product, quantity);
    return applyShipping(priceData, shippingMethod, discount);
}

function calculatePricingData(product, quantity) {
    const basePrice = product.basePrice * quantity;
    const discount = Math.max(quantity - product.discountThreshold, 0) 
		    * product.basePrice * product.discountRate;
    return { basePrice: basePrice, quantity: quantity, discount: discount };
}

function applyShipping(priceData, shippingMethod) {
    const shippingPerCase = (priceData.basePrice > shippingMethod.discountThreshold) ? 
          	 shippingMethod.discountedFee : shippingMethod.feePerCase;
    const shippingCost = priceData.quantity * shippingPerCase;
    return priceData.basePrice - priceData.discount + shippingCost;
}
```

# Chapter 7. 캡슐화

## 레코드 캡슐화하기

`변수 조작 방식 통제를 위함`

`레코드는 연관된 여러 데이터를 직관적인 방식으로 묶을 수 있어서, 따로 취급할 때보다 의미 있는 단위로 전달할 수 있게 해준다.`

`캡슐화에서는 값을 수정하는 부분을 명확하게 드러내고 한 곳에 모아두는 일이 중요`

`가변 데이터를 저장할 경우 레코드보다 객체를 선호`

- 어떻게 저장했는지 숨기고, 각 값을 메서드로 제공

**개요**

?. Map type -> Class type

Before

```javascript
const organization = { name: '애크미 구스베리', country: 'GB' };
```

After

```javascript
class Organization {
    constructor(data) {
        this._name = data.name;
        this._country = data.country;
    }

    get name() { return this._name; }
    set name(arg) { this._name = arg; }
    get country() { return this._country; }
    set country(arg) { this._country = arg; }
}
```

**절차**

1. 레코드를 담은 변수 캡슐화 (변수를 반환하는 함수)
2. 레코드를 새로운 클래스로 정의 (Getter/Setter 생성)
3. 테스트
4. 새로 정의한 클래스 객체를 반환하는 함수 만들기
5. 기존 레코드 반환 코드를 새 함수로 변경
6. 클래스에서 원본 데이터 반환 접근자와 원본 레코드 반환 함수 제거
7. 테스트
8. 레코드 필드도 중첩 구조라면 레코드 캡슐화하기와 컬렉션 캡슐화하기를 재귀적으로 적용

## 컬렉션 캡슐화하기

`Collection 변수 접근을 캡슐화하면서 Getter가 Collection 자체를 반환한다면, 원본이 변경될 수 있다.`

- add(), remove() 라는 Collection 변경자 메서드를 만들자.

`Collection Getter 를 제공하되 내부 Collection의 복제본을 반환하자.`

`Collection 을 다루는 클래스는 불필요한 복제본을 만드는 편이 예상치 못한 수정으로 인한 오류를 디버깅하는 것보다 낫다.`

- 컬렉션 관리를 책임지는 클래스라면 항상 복제본을 제공하자.

**개요**

?. 컬렉션 관리는 복제본으로

Before

```javascript
class Person {
    constructor(name) {
        this._name = name;
        this._courses = [];
    }

    get name() { return this._name; }
    get courses() { return this.courses; }
    set courses(aList) { this._courses = aList; }
}
```

After

```javascript
class Person {
    constructor(name) {
        this._name = name;
        this._courses = [];
    }

    get name() { return this._name; }
    get courses() { return this.courses.slice(); }
    addCourse(aCourse) { this._courses.push(aCourse); }
    removeCourse(aCourse, fbIfAbsent = () => { throw new RangeError();} {
		const index = this._courses.indexOf(aCourse);
		if (index === -1) fbIfAbsent(); 
		else this._courses.splice(index, 1);
	}
	set courses(aList) { this._courses = aList.slice(); }
}
```

**절차**

1. 컬렉션 캡슐화가 되어있지 않다면 '변수 캡슐화하기' 진행
2. 컬렉션에 원소를 추가/제거하는 함수 추가
   - 컬렉션 자체를 통째로 바꾸는 Setter 는 제거하자.
3. 정적 검사
4. 기존 컬렉션 참조 코드를 추가/제거 함수 호출로 변경 및 테스트 
5. 컬렉션 Getter는 원본 내용을 수정할 수 없는 읽기 전용 프락시나 복제본 반환
6. 테스트

## 기본형을 객체로 바꾸기

`단순 출력 이상의 기능이 필요해지면 데이터를 표현하는 전용 클래스를 정의하자.`

**개요**

Before

```javascript
orders.filter((o) => 'high' === o.priority 
              	|| 'rush' === o.priority);
```

After

```javascript
orders.filter((o) => o.priority.higherThan(new Priority('normal')));
```

**절차**

1. 변수 캡슐화하기
2. 단순한 값 클래스 만들기
   - 기본 생성자와 Getter/Setter 추가
3. 정적 검사 수행
4. Getter/Setter 수정
5. 테스트
6. 함수 이름 검토

**Example**

```javascript
class Order {
    constructor(data) { 
        this._priority = data.priority;
    }
    // 1, 2
    get priority() { return this._priority; }
    get priorityString() { return this._priority.toString(); } // 6
    set priority(aString) { this._priority = new Priority(aString); } // 4
}
```

```javascript
class Priority {
    constructor(value) {
        if (value instanceof Priority) { return value; }
        // 우선순위 값 검증 및 비교 로직
        if (Priority.legalValues().includes(value)) {
            this._value = value;
        } else {
            throw new Error(`<${value}}> is invalid for Priority`);
        }
    }

    static legalValues() { return ['low', 'normal', 'high', 'rush']; }
    get _index() { return Priority.legalValues().findIndex((s) => s === this._value); }
    toString() { return this._value; }
    equals(other) { return this._index === other._index; }
    higherThan(other) { return this._index > other._index; }
    lowerThan(other) { return this._index < other._index; }
}
```

## 임시 변수를 질의 함수로 바꾸기

`다른 함수에서도 사용할 수 있어 코드 중복을 줄일 수 있다.`

`여러 곳에서 똑같은 방식으로 계산되는 변수를 발견하면 질의 함수로 바꿀 수 있을지 살펴보자.`

`값이 대입된 변수가 있는데, 복잡한 로직에서 여러 차례 다시 대입되는 경우 모두 질의 함수로 추출하자.`

**개요**

Before

```javascript
class Order {
    constructor(quantity, item) {
        this._quantity = quantity;
        this._item = item;
    }

    get price() { //
        var basePrice = this._quantity * this._itemPrice;
        var discountFactor = 0.98;
        if (basePrice > 1000) {
            discountFactor -= 0.03;
        }
        return basePrice * discountFactor;
    }
}
```

After

```javascript
class Order {
    constructor(quantity, item) {
        this._quantity = quantity;
        this._item = item;
    }

    get basePrice() { return this._quantity * this._itemPrice; }
    get discountFactor() {
        var discountFactor = 0.98;
        if (basePrice > 1000) {
            discountFactor -= 0.03;
        }
        return discountFactor;
    }
    get price() { return this.basePrice * this.discountFactor; } //
}
```

**절차**

1. 변수를 사용할 때마다 매번 다른 결과를 갖는지 확인
2. 변수를 읽기 전용으로 만들 수 있다면 읽기 전용으로 만들기
3. 테스트
   - 재대입 코드가 있는지 발견할 수 있다.
4. 변수 대입문을 함수로 추출
   - 사이드 이펙트가 있다면 질의 함수와 변경 함수로 분리하기로 대처
5. 테스트
6. 변수 인라인하기로 임시 변수 제거

## 클래스 추출하기

반대 리팩터링 : 클래스 인라인하기

`클래스는 명확하게 추상화하고 소수의 주어진 역할만 처리하자.`

`따로 묶을 수 있는 데이터와 메서드가 보인다면 어서 분리하자.`

**개요**

Before

```javascript
class Person {
    get officeAreaCode() { return this._officeAreaCode; }
    get officeNumber() { return this._officeNumber; }
}
```

After

```javascript
class Person {
    get officeAreaCode() { return this._telephoneNumber.areaCode; }
    get officeNumber() { return this._telephoneNumber.number; }
}

class TelephoneNumber {
    get areaCode() { return this._areaCode; }
    get number() { return this._number; }
}
```

**절차**

1. 클래스 역할 분리 방법 정하기
2. 분리될 역할을 담당할 클래스 만들기
3. 원래 클래스 생성자에서 새로운 클래스의 인스턴스 생성
4. 분리된 역할에 필요한 필드들을 새로운 클래스로 옮기기
5. 메서드들도 새로운 클래스로 옮기기(함수 옮기기)
   - 호출을 당하는 일이 많은 메서드부터 옮기자
6. 양쪽 클래스의 인터페이스를 살피며 불필요 메서드 제거 및 환경에 맞게 이름 수정(함수 선언 바꾸기)
7. 새로운 클래스를 외부로 노출할지 결정
   - 외부로 노출할 경우 "새로운 클래스에 참조를 값으로 바꾸기" 적용 고민

**Example**

```javascript
class Person {
    constructor() {
        this._telephoneNumber = new TelephoneNumber();
    }
    get officeAreaCode() { return this._telephoneNumber.areaCode; }
    set officeAreaCode(arg) { this._telephoneNumber.areaCode = arg; }
    get officeNumber() { return this._telephoneNumber.number; }
    set officeNumber(arg) { this._telephoneNumber.number = arg; }
    get telephoneNumber() { return this._telephoneNumber.toString(); }
}
```

```javascript
class TelephoneNumber {
    get areaCode() { return this.areaCode; }
    set areaCode(arg) { this.areaCode = arg; }
    get number() { return this.number; }
    set number(arg) { this.number = arg; }
    get toString() { return `(${this.areaCode}) ${this.number}`; }
}
```

## 클래스 인라인하기

`역할 옮기기 리팩터링 후 더 이상 제 역할을 못 하는 클래스는, 자신을 가장 많이 사용하는 클래스로 흡수시키자.`

반대 리팩터링 : 클래스 추출하기

**개요**

Before

```javascript
class Person {
    get officeAreaCode() { return this._telephoneNumber.areaCode; }
    get officeNumber() { return this._telephoneNumber.number; }
}

class TelephoneNumber { 
    get areaCode() { return this._areaCode; }
    get number() { return this._number; }
}
```

After

```javascript
class Person {
  get officeAreaCode() { return this._officeAreaCode; }
  get officeNumber() { return this._officeNumber; }
}
```

**절차**

1. 소스 클래스의 각 public 메서드에 대응하는 메서드들을 타깃 클래스에 생성
2. 소스 클래스의 메서드를 사용하는 코드를 모두 타깃 클래스의 위임 메서드를 사용하도록 수정 (수정마다 테스트)
3. 소스 클래스의 메서드와 필드를 모두 타깃 클래스로 옮기기 (이동마다 테스트)
4. 소스 클래스를 삭제

## 위임 숨기기

`캡슐화는 모듈들이 시스템의 다른 부분에 대해 알아야 할 내용을 줄여준다.`

`인터페이스와의 의존성을 없애려면 서버 자체에 위임 메서드를 만들어서 위임 객체의 존재를 숨기자.`

반대 리팩터링 : 중개자 제거하기

**개요**

Before

```javascript
manager = aPerson.department.manager;
```

After

```javascript
manager = aPerson.manager;

class Person {
  get manager() { return this.department.manager; }
}
```

**절차**

1. 위임 객체의 각 메서드에 해당하는 위임 메서드를 서버에 생성
2. 클라이언트가 서버를 호출하도록 수정
3. 서버로부터 위임 객체를 얻는 접근자 제거
4. 테스트

**example**

```javascript
manager = aPerson.manager;
```

```javascript
class Person {
    constructor(name) { 
        this._name = name;
    }
    get name() { return this._name; }
    get manager() { return this._department.manager; } // 부서 클래스를 숨기고 위임 메서드 생성
    set department(arg) { this._department = arg; }
}
```

```javascript
class Department {
    get chargeCode() { return this._chargeCode; }
    set chargeCode(arg) { this._chargeCode = arg; }
    get manager() { return this._manager; }
    set manager(arg) { this._manager = arg; }
}
```



## 69L
