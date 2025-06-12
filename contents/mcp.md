# MCP Start

## 














log 에 필요한 데이터
- action FINISH INSERT
- channelType ONLINE, OFFLINE
- group-id 로 묶음을 확인
- source_Type PAYMENT, CANCEL
- status 로 성공 실패 중복 미확인 .. 확인


금액은
NumberDecimal





### 상태 기반 ⁉️

상태 기반의 의미를 잠시 짚고 가자면, 네 가지 키워드로 설명할 수 있습니다.
- **상태 저장소**(State Store):
  - RocksDB 같은 임베디드 데이터베이스를 사용하여 처리 과정에서 발생하는 상태 정보를 저장
- **KTable**:
  - 키-값 형태의 데이터를 테이블처럼 관리하는 추상화
  - 입력 스트림의 각 키에 대한 최신 상태를 저장하고 관리하며, 상태 저장소에 저장
- **변경 로그 토픽(Changelog Topic)**:
  - 상태 저장소에 저장된 상태 변경 사항을 기록(내부적으로 토픽 생성)
  - 애플리케이션이 재시작되거나 장애가 발생했을 때 상태를 복원하는 데 사용
- **윈도우 기반 처리(Windowing)**:
  - 특정 시간 범위 또는 이벤트 범위 내에서 상태를 관리하여, 특정 기간 동안의 데이터 집계, 추이 분석 등을 수행 가능












  

2️⃣ 결제 데이터 생성

```http
http://localhost:8080/api/payment/send
```

- 비실시간 데이터이므로 메시지의 끝을 알기 위해 메시지 전송이 끝날 때 FINISH action type 의 메시지를 각 파티션에 전송

3️⃣ 지급 규칙 조회

지급 규칙 데이터는 `KTable`을 활용
- KTable은 업데이트 스트림이고, 데이터베이스에 데이터를 변경하는 것과 유사
- KTable 크기는 계속 증가하지 않으며, 기존 레코드는 새 레코드로 교체





