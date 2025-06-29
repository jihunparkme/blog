# MongoDB Bucket Aggregation

MongoDB의 `$bucket (aggregation)`에 대한 빠른 이해를 위해 [공식 문서](https://www.mongodb.com/docs/manual/reference/operator/aggregation/bucket/#mongodb-pipeline-pipe.-bucket)를 간략하게 정리하고자 합니다.

## $bucket

> 문서를 지정된 표현식과 버킷 경계에 따라 버킷이라고 하는 그룹으로 분류하고 각 버킷마다 문서를 출력

**$bucket 및 메모리 제한**

- `$bucket` 단계의 RAM 제한은 100MB
- 해당 한도를 초과하면 `$bucket`이 기본값으로 오류를 반환
- 처리 공간을 더 확보하려면 `allowDiskUse` 옵션을 사용하여 집계 파이프라인 단계가 임시 파일에 데이터를 쓸 수 있도록 설정 필요

## Syntax

```json
{
  $bucket: {
      groupBy: <expression>,
      boundaries: [ <lowerbound1>, <lowerbound2>, ... ],
      default: <literal>,
      output: {
         <output1>: { <$accumulator expression> },
         ...
         <outputN>: { <$accumulator expression> }
      }
   }
}
```

|Field|Type|Description|
|---|---|---|
|groupBy|expression|문서를 그룹화하는 표현식. (필드 경로를 지정하려면 필드 이름 앞에 `$`를 붙이고 따옴표로 묶음)|
|boundaries|array|각 버킷의 경계를 지정하는 groupBy 표현식을 기반으로 한 값 배열. 인접한 각 값 쌍은 버킷의 포괄적인 하한과 배타적인 상한으로 작용. (최소 두 개의 경계를 지정)|
|default|Iiteral||
|output|document||