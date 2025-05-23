# Kafka Streams 에 대한 고찰

# Kafka Streams❓

> 토픽에 적재된 데이터를 기반으로 **상태기반** 또는 **비상태기반**으로 실시간 변환하여 다른 토픽에 적재하는 라이브러리

💡 상태 기반❓
- 상태 저장소(State Store):
  - RocksDB 같은 임베디드 데이터베이스를 사용하여 처리 과정에서 발생하는 상태 정보를 저장
- KTable:
  - 키-값 형태의 데이터를 테이블처럼 관리하는 추상화
  - 입력 스트림의 각 키에 대한 최신 상태를 저장하고 관리하며, 상태 저장소에 저장
- 변경 로그 토픽(Changelog Topic):
  - 상태 저장소에 저장된 상태 변경 사항을 기록(내부적으로 토픽 생성)
  - 애플리케이션이 재시작되거나 장애가 발생했을 때 상태를 복원하는 데 사용
- 윈도우 기반 처리(Windowing):
  - 특정 시간 범위 또는 이벤트 범위 내에서 상태를 관리하여, 특정 기간 동안의 데이터 집계, 추이 분석 등을 수행 가능

💡 상태 기반 처리에 활용될 수 있는 사례❓
- **실시간 데이터 집계 및 분석**
  - 특정 기간 동안의 데이터 집계: 시간별, 분별, 일별 사용자 활동, 매출 현황 등
  - 실시간 추이 분석: 주식 가격 변동, 웹사이트 트래픽 변화 등을 실시간으로 감지하고 분석
  - 이상 감지: 사용자 행동 패턴, 시스템 로그 등을 분석하여 비정상적인 활동을 실시간으로 감지
- **실시간 데이터 변환 및 처리**
  - 데이터 스트림 조인: 여러 데이터 스트림을 결합하여 새로운 스트림을 생성
  - 데이터 필터링 및 변환: 특정 조건에 맞는 데이터만 필터링하거나, 데이터를 다른 형식으로 변환
  - 세션 기반 처리: 사용자 세션, 네트워크 세션 등 특정 세션 동안의 데이터를 처리
- **실시간 추천 시스템**
  - 사용자 행동 패턴 분석: 사용자의 과거 구매 내역, 검색 기록 등을 분석하여 실시간으로 상품을 추천
  - 실시간 광고 타겟팅: 사용자 위치, 관심사 등을 분석하여 실시간으로 광고를 노출
- **실시간 모니터링 및 알림**
  - 시스템 모니터링: CPU 사용량, 메모리 사용량, 네트워크 트래픽 등을 실시간으로 모니터링하고 이상 발생 시 알림을 전송
  - 금융 거래 모니터링: 실시간으로 금융 거래를 모니터링하고 이상 거래 감지 시 알림을 전송

## 토폴로지

> 데이터 스트림을 처리하는 과정, 즉 데이터의 흐름과 변환 과정을 정의하는 구조

- `processor`: 카프카 스트림즈에서 토폴로지를 이루는 노드
  - 프로세서에서 소스 프로세서, 스트림 프로세서, 싱크 프로세서 세 가지가 존재
- `stream`: 노드와 노드를 이은 선
  - 스트림은 토픽의 데이터를 뜻하는데 프로듀서와 컨슈머에서의 레코드와 동일

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/refs/heads/main/img/kafka-streams/topology.png" width="100%"></center>

**소스 프로세스**
- 데이터를 처리하기 위해 최초로 선언해야 하는 노드
- 하나 이상의 토픽에서 데이터를 가져오는 역할

**스트림 프로세스**
- 다른 프로세서가 반환한 데이터를 처리하는 역할
  - 변환, 분기처리와 같은 로직이 데이터 처리의 일종

**싱크 프로세서**
- 데이터를 특정 카프카 토픽으로 저장하는 역할
- 스트림즈로 처리된 데이터의 최종 종착지

## Streams DSL

kafka Streams 개발 방법은 두 가지를 제공
- Streams DSL(Domain Specific Language)
  - 일반적인 스트림 처리 작업을 위한 고수준의 추상화를 제공
  - 필터링, 매핑, 집계, 조인 등과 같은 일반적인 스트림 처리 작업을 간단하고 선언적인 방식으로 수행
- processor API
  - 스트림 처리 로직을 직접 정의하고 제어할 수 있는 낮은 수준의 추상화를 제공
  - 스트림 프로세서, 상태 저장소, 토폴로지 등을 직접 정의하고 관리

`Streams DSL`의 `KStream`을 활용하여 구현

**KStream**
- 레코드의 흐름을 표현한 것으로 메시지 키와 메시지 값으로 구성
- KStream으로 데이터를 조회하면 토픽에 존재하는(또는 KStream에 존재하는) 모든 레코드가 출력

## vs Batch

|Spring Batch|Kafka Streams|
|---|---|
|청크 기반 처리로 하나의 아이템이라도 실패하면 해당 청크 전체가 실패<br/>- 청크 내 한 건이라도 예외 발생 시 전체 롤백|각 아이템이 각자 처리되므로 실패한다면 해당 아이템만 재처리 가능|
|청크 기반 처리는 커서 기반으로 조회하게 되는데 CursorItemReader는 트랜잭션과 커넥션을 오래 잡고 있어서 대량 처리 시 메모리 사용량 증가, DB 커넥션 고갈 위험<br/>- 모든 데이터 조회 후 처리 시작<br/>- 병렬 처리 및 청크 구성으로 빠른 처리|reactive mongo를 활용해 비동기로 조회하여 조회되는 즉시 스트림으로 처리 가능<br/>-토픽으로 데이터가 들어오는 즉시 처리 시작<br/>-카프카 파티션 수 및 스레드 수 만큼 병렬화 가능|
|실행 후 종료되어 리소스 해제|지속 실행 상태 유지로 리소스 점유|

## vs Producer/Consumer

|Producer/Consumer|Kafka Streams|
|---|---|
|데이터 처리는 애플리케이션 서버에서 수행<br/>- 데이터 대량 처리 시 서버에 부하|카프카 클러스터 내부에서 수행<br/>- 서버 부하를 신경쓸 필요가 없음|
|각 단계를 독립적인 애플리케이션으로 구현|각 단계를 스트림 처리 토폴로지로 연결하여 하나의 애플리케이션으로 구현|
|각 단계를 독립적으로 개발하고 배포할 수 있어 높은 유연성|토폴로지를 통해 데이터 흐름을 효율적으로 관리|
||스트림 데이터 처리에 최적화<br/>- 일반적으로 카프카 프로듀서/컨슈머 구성보다 빠른 처리 속도|