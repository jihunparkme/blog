# MongoDB Sharding

`MongoDB Sharding`에 대한 내용을 간략하게 정리하고자 합니다.

## Sharding?

> 데이터를 여러 머신에 분산하는 방법

MongoDB는 샤딩을 통한 수평적 확장을 지원
- **수평적 확장**: 시스템 데이터 세트와 로드를 여러 서버로 나누고 필요에 따라 서버를 추가하여 용량을 늘리는 것

## Sharded Cluster

> 컬렉션 수준에서 데이터를 샤딩하여 클러스터의 샤드 전체에 컬렉션 데이터를 분산

구성 컴포넌트
- `shard`: 각 샤드에는 샤드 데이터의 하위 집합이 포함.
  - 각 샤드는 복제본 세트로 배포되어야 함
- `Routing with mongos`: mongos는 클라이언트 애플리케이션과 샤딩된 클러스터 간의 인터페이스를 제공하는 쿼리 라우터 역할
- `config servers`: config 서버는 클러스터에 대한 메타데이터와 구성 설정을 저장
  - config 서버는 복제본 세트(CSRS)로 배포되어야 함



## Reference

- [https://www.mongodb.com/docs/manual/sharding/]