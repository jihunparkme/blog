# MongoDB Bucket Pattern

MongoDB의 `Group Data with the Bucket Pattern`에 대한 빠른 이해를 위해 [공식 문서](https://www.mongodb.com/docs/manual/data-modeling/design-patterns/group-data/bucket-pattern/)를 간략하게 정리하고자 합니다.

## Bucket Pattern

> 버킷 패턴은 긴 데이터 시리즈를 별개의 객체로 분리
>
> 큰 데이터 시리즈를 작은 그룹으로 분리하면 쿼리 액세스 패턴을 개선하고 애플리케이션 로직을 간소화 가능

**주식 거래를 추적하는 스키마에 대한 예시**

```mongodb
db.trades.insertMany(
  [
    {
      "ticker" : "MDB",
      "customerId": 123,
      "type" : "buy",
      "quantity" : 419,
      "date" : ISODate("2023-10-26T15:47:03.434Z")
    },
    {
      "ticker" : "MDB",
      "customerId": 123,
      "type" : "sell",
      "quantity" : 29,
      "date" : ISODate("2023-10-30T09:32:57.765Z")
    },
    {
      "ticker" : "GOOG",
      "customerId": 456,
      "type" : "buy",
      "quantity" : 50,
      "date" : ISODate("2023-10-31T11:16:02.120Z")
    }
  ]
)
```

위 예제는 한 고객이 한 번에 수행한 주식 거래를 표시하며 페이지당 10건의 거래를 표시

버킷 패턴을 사용하여 거래를 customerId 기준으로 10개의 그룹으로 그룹화해보겠습니다.

### 1단계. customerId를 기준으로 데이터를 그룹화

각 customerId에 대해 단일 문서를 갖도록 스키마를 재구성

```mongodb
{
  "customerId": 123,
  "history": [
    {
      "type": "buy",
      "ticker": "MDB",
      "qty": 419,
      "date": ISODate("2023-10-26T15:47:03.434Z")
    },
    {
      "type": "sell",
      "ticker": "MDB",
      "qty": 29,
      "date": ISODate("2023-10-30T09:32:57.765Z")
    }
  ]
},
{
  "customerId": 456,
  "history": [
    {
      "type" : "buy",
      "ticker" : "GOOG",
      "quantity" : 50,
      "date" : ISODate("2023-10-31T11:16:02.120Z")
    }
  ]
}
```

- 일반적인 customerId 값을 가진 문서는 단일 문서로 압축되며 customerId는 최상위 필드
- 해당 고객에 대한 거래는 history라는 내장된 배열 필드로 그룹화

### 2단계. 각 버킷의 식별자와 개수를 추가

```mongodb
db.trades.insertMany(
  [
    {
      "_id": "123_1698349623",
      "customerId": 123,
      "count": 2,
      "history": [
        {
          "type": "buy",
          "ticker": "MDB",
          "qty": 419,
          "date": ISODate("2023-10-26T15:47:03.434Z")
        },
        {
          "type": "sell",
          "ticker": "MDB",
          "qty": 29,
          "date": ISODate("2023-10-30T09:32:57.765Z")
        }
      ]
    },
    {
      "_id": "456_1698765362",
      "customerId": 456,
      "count": 1,
      "history": [
        {
          "type" : "buy",
          "ticker" : "GOOG",
          "quantity" : 50,
          "date" : ISODate("2023-10-31T11:16:02.120Z")
        }
      ]
    },
  ]
)
```

- `_id` 필드 값은 `customerId`와 `history` 필드의 첫 번째 거래 시간(유닉스 시간 이후)을 초 단위로 연결한 것
- `count` 필드 는 해당 문서의 history 배열 에 있는 요소의 수
  - 페이징 로직에 사용하기 위한 필드

