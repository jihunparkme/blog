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

