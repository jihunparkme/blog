# Oracle Select vs OR vs IN 비교

약 2만 건 정도의 데이터를 조회하는 API를 개발해야 하는데, 

하나씩 조회, OR 연산자 사용, IN 연산자 사용 중 어떤 방법이 성능상 유리할지 궁금했다.

당연히 모두가 아는 결과를 테스트하는 것일 수도 있지만.. 내 눈으로 직접 확인해 보아야 믿을 수 있을 것 같다..

.

실제 서비스 환경과 동일하게 SpringBoot, MyBatis, Oracle 환경으로 테스트하였고, 

오라클에서 IN 절에 허용되는 최대 식은 1,000개인 이유로 1,000건 단위로 분할조회 하도록 하였다.

1,000건이 초과하면 `ORA-01795 maximum number of expressions in a list is 1000` 예외를 던진다고 한다.

(그런데 테스트 당시에는 IN 절에서 1,000건이 넘어도 예외 없이 조회가 잘 되던데.. 🤔)

...

결론은 1,000개의 데이터를 

`루프를 돌리면서 하나씩 조회하는 것`과 `OR 연산자를 사용해서 조회하는 것`과 `IN 연산자를 사용해서 조회하는 것` 을 비교해 보자.

## 루프 단일 조회

1,000번의 jdbc connection / disconnect 작업이 있다 보니 생각만큼 느린 결과를 보였다.

5회 시도 평균 11.695 초가 나왔다.

```sql
SELECT ...
  FROM ...
 WHERE op.ORD_NO = #{ordNo}
   AND op.ORD_PRD_SEQ = #{ordPrdSeq}
```

```java
@Test
void getTabTypeCode() throws Exception {
    List<OrderProductParam> requests = getOrderProductParam();

    ArrayList<OrderTabTypeCodeResponseV1> result = new ArrayList<>();
    for (OrderProductParam request : requests) {
        result.add(repository.getTabTypeCode(request));
    }

    Assertions.assertEquals(1000, result.size());
}
```

## IN Condition

[IN Condition](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/IN-Condition.html#GUID-C7961CB3-8F60-47E0-96EB-BDCF5DB1317C)

IN 연산자는 아주 작은 차이지만 OR 연산자보다 성능이 좋았다.

5회 시도 평균 0.144 초가 나왔다.

```sql
SELECT ...
  FROM ...
WHERE
    <foreach collection="list" item="item" separator="," close=")" open="(op.ORD_NO, op.ORD_PRD_SEQ) IN (">
        (#{item.ordNo}, #{item.ordPrdSeq})
    </foreach>
```

```java
@Test
void getTabTypeCodesUsingIn() throws Exception {
    List<OrderProductParam> requests = getOrderProductParam();

    List<OrderTabTypeCodeResponseV1> result = repository.getTabTypeCodesUsingIn(requests);

    Assertions.assertEquals(1000, result.size());
}
```

## OR Condition

[Compound Conditions](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Compound-Conditions.html#GUID-D2A245F5-8071-4DF7-886E-A46F3D13AC80)

IN 연산자와 성능은 거의 차이가 없었다.

5회 시도 평균 0.419 초가 나왔다.

```sql
SELECT ...
  FROM ...
WHERE
    <foreach collection="list" item="item" open="(" separator="OR" close=")">
        (op.ORD_NO = #{item.ordNo} AND op.ORD_PRD_SEQ = #{item.ordPrdSeq})
    </foreach>
```

```java
@Test
void getTabTypeCodesUsingOr() throws Exception {
    List<OrderProductParam> requests = getOrderProductParam();

    List<OrderTabTypeCodeResponseV1> result = repository.getTabTypeCodesUsingOr(requests);

    Assertions.assertEquals(1000, result.size());
}
```

## Conclusion

특정 케이스마다 성능 차이가 다를 수도 있을 것이다.

어떤 케이스에서는 단일 조회 성능이 더 좋고, 어떤 케이스에서는 IN, OR 연산자를 사용하는 것이 더 좋고..

현재 케이스에서는 IN 연산자가 가장 좋은 성능을 보였다.

|연산자|1|2|3|4|5|평균
|---|---|---|---|---|---|---
|select|11.319|12.841|11.3|12.081|10.937|11.695
|or|0.93|0.104|0,124|0.111|0.83|0.419
|in|0.131|0.167|0.1235|0.155|0.148|0.144


> 놓친 부분이 있거나, 잘 못 알고 있는 부분이 있을 수도 있습니다..
>
> 바로잡힌 정보를 공유주시면 정말 감사드리겠습니다.!🙏🏻