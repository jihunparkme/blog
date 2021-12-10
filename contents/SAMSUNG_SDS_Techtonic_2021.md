# SAMSUNG SDS Techtonic 2021

[SAMSUNG SDS Techtonic 2021](https://techtonic.samsungsds.com/techtonic/)

모든 세션을 듣고 싶었지만.. 일은 해야하기에 궁금했거나 호기심이 있는 일부 세션을 듣고 정리해 보았다.

## Total eXperience(MX=CX+EX) Management

**'성공적 Digital Transformation을 위한' Total eXperience(MX=CX+EX) Management**

**Total eXperience** (TX)

- [Gartner Top Strategic Technology Trends for 2022](https://www.gartner.com/en/information-technology/insights/top-technology-trends)
- create a better overall experience
  - MX : Multi eXperience
  - CX : Customer eXperience
  - EX : Employee eXperience
  - UX : User eXperience

**DT 실행 기업 중 70%가 실패**

- 주요 실패 원인은 DT를 `신기술 전환으로만` 인식

**DT 성공 기업의 공통적 특징**

- `일하는 사람들의 DNA 변화`가 있을 때, 진정한 DT의 완성이라 인식
- `통합적 고객경험에 집중`해서 DT를 추친
- 빠른 기업가치 상승

**사람주도 DT**

- **`고객 Pain point Zero화`에 필요한 적합한 기술 적용**
  - 고객이 원하는 가치 전달

**통합경험 기반의 사람주도 DT 핵심 전략**

```text
사람 주도(문제 인식) : Pain Point
기술 실현(기술 적용) : Solution
고객가치 제공 : CX Value
```

- `상황에 맞게` 연결된 매끄러운 채널 통합
  - 공장과 사무실을 오가는 직원을 위한 차세대 스마트 팩토리 시스템
- `초개인화`된 지능형 업무 시스템
  - 단순, 반복적이고 복잡한 업무를 클릭 하나로 해결
- `통합적 인지`를 통한 최적의 의사결정지원
  - 고객 이탈률 감소, 고객대응 자동화 서비스 제공, 데이터 기반의 구독서비스 컨설팅 지원
- `형태 데이터 기반`의 통합경험관리
  - 행태 데이터 기반의 통합경험관리로 개선된 고객경험 제공
  - 데이터 수집 - 분석/진단 - 개선/반영

**통합 디지털 경험 서비스 체계**

- 문제 식별 -> 서비스 인지/사용/지원 -> 지속확대의 모든 고객여정 영역에 대한 서비스 체계
- 각 단계별 목표 및 고객가치와 비즈니스 가치의 정의
- 통합경험 혁신을 위한 서비스와 자산
- 지속적인 경험측정과 관리체계로 임직원 니즈 및 시장의 변화에 대응

## MSA Reference Platform

**Monolithic Architecture VS Microservice Architecture**

- Monolithic Architecture
  - Application 전체가 하나의 서버에 존재
  - 모듈 : 런타임에 프로세스에 연결됨
  - 강한 결합성
- Microservice Architecture
  - 서비스 단위로 서버 분리
  - 서비스 : 독립적인 배포 가능
  - 느슨한 결합성

**Cloud Native Application**

- MSA 구조 Application은 클라우드를 최대한 활용
  - 민첩성 (Agility)
    - 서비스별로 서버가 분리되어 있으므로 빠른 배포 가능
  - 확장성 (Scalability)
    - 사용자 유입 급증 시 서비스 단위 메모리 확장
  - 회복력 (Resiliency)
    - 특정 서비스에 장애가 발생하여도, 다른 서비스는 정상적으로 동작

**Difficulties**

- 복잡도 증가
  - 서비스 구성
  - 분산 트랜잭션 처리
  - 운영 모니터링
- OSS 활용 증가
  - OSS 선정 어려움
  - OSS 간 정합성/통합의 어려움
  - 라이센스 이슈
- Infra 별 구성 변화
  - private Cloud 제약
  - public Cloud 플랫폼 종속성 / 비용 고민
  - Hybrid/Multi Cloud 운영 복잡도 증가

**Reference**

- [[Architecture\] Microservice Architecture(MSA) 빠르게 훑어보기](https://data-make.tistory.com/698)

## Low Code Development Platform

[LCDP](https://en.wikipedia.org/wiki/Low-code_development_platform)

- Hand coding 대신 GUI를 통해 애플리케이션 SW를 만들 수 있게 개발 환경을 제공
- 폭넓은 범위의 여러 사람들이 애플리케이션 개발에 참여 가능
- LCDP는 초기 setup, 학습, 개발 및 유지보수에 대한 비용을 낮출 수 있음

**Model Driven**

- 모델을 정의하고 모델에서 화면, API등 자동 생성 (높은 자동화율)
- 관계형 모델은 처리에 어려움

**Canvas Driven**

- 화면 저작으로 시작하므로 사용자 접근이 가장 편함
- 화면 개별 컴포넌트 변수 처리, Event 연결 등 추가 작업이 많음

**Process Driven**

- 업무 프로세스를 정의하고 각 프로세스에 대한 구현을 GUI로 수행
- 업무 흐름이 명확히 표현
- 중소규모의 어플리케이션의 경우 미적합

**API Driven**

- API를 먼저 정의하고 이를 기준으로 화면, 로직, 프로세스가 연결
- 협업에 유리
- API를 잘 만들기 위해 IT 지식 필요

**Simulation**

- Step 1.화면 저작
  - Low Code UX 화면에서 추가할 Component를 선택하여 Drag&Drop
- Step 2.로직 저작
  - 신규 Component의 내/외부 인터페이스 및 워크플로우 자동 연계
- Step 3. Cloud 배포
  - 배포할 Component를 자동 빌드 및 클라우드 환경에 배포

**기술적 고민**

- Floating Point Arithmetic
  - 2진수로는 실수의 정확한 값을 표현할 수 없는 한계
  - Apach Precision OR Big Decimal 등을 사용한 변형이 필요
- Date Type
  - UNIX time
  - Time Zone
  - Date format

**Global 제품**

- [Mendix](https://www.mendix.com/)
- [Outsystems](https://www.outsystems.com/)

---

## Job Scheduling

GPU 놀지마! 작업 시간 예측을 통한 스케줄링 방법(Job Scheduling) 

**클라우드에서의 스케줄러**

- ML/DL Job 특성을 파악하여 최적의 자원을 분배하는 기술
- 사용자의 요청 순서와 리소스 활용을 고려한 사용 만족 확보 필요

**개발 기술**

- Gang
  - 단일 ML/DL Job 수행에 필요한 resource(GPU, CPU, Memory 등)가 모두 확보되었을 때 Scheduling 수행
  - 분산 학습 Job을 구성하는 sub Job 들이 동시 실행 시작을 보장
- Bin-Packing
  - Job 에서 요청된 GPU 개수와 가장 근접한 가용 GPU를 확보하고 있는 node를 우선 배정
  - 유휴 자원 최소화, node 인접성 증가로 인한 분산학습 성능 개선
- FIFO
  - Job 요청 순서로 실행
  - GPU 효율성 저하
- Multi Queue 스케줄러
  - FIFO 병목 현상, GPU 효율성 개선
  - Queue를 추가로 만들어 요청 GPU가 많은 Job과 적은 Job을 따로 스케줄링 하는 방법

**Summary**

- Efficiency
  - Job 시간 예측을 통한 효율적인 스케줄링 방법 확보
  - GPU 사용율 극대화를 통한 가격 경쟁력 확보
- Optimization
  - ML/DL Job을 분석한 최적의 GPU 개수 배정
  - 네트워크 상황을 고려한 최적의 Job 배정
- Fairness
  - 대기 시간 최소화에 따른 사용자 만족도 상승
  - Job 순서 배정에 따른 공정한 확보

## Text Analysis

- Text Analysis 기술을 이용한 VoC 처리 지능화 적용 사례

**중요성**

- Text에 내재된 정보를 추출하여 정형화된 형태로 제공하는 기술

**5대 핵심 기능**

- `의도 분류`
  - Text에 내재된 작성자의 목적(or 의도)를 파악하여 분류
  - 고객 대응 시 질의 분류
- `감성 분석`
  - 사람의 언어에 담긴 감성을 이해
  - 고객 피드백, 메시지 분석
  - 뉴스 댓글 긍정/부정 판단
- `토픽 모델링`
  - Text에 내재된 토픽들을 도출하는 과정
  - 토픽들을 통한 주제 도출
- `키워드 추출`
  - 트렌드 분석을 통한 관심 키워드 도출 및 연관 키워드 분석
- `유사도 분석`
  - Text 간 의미적 유사성 도출

## OCR

- An approach in Vietnamese handwritten OCR for financial and manufacturing sectors

**활용**

- 문서 처리 시간 단축
  - 분류, 스캔, 추출, 데이터 입력 등을 기계 처리

**문제점**

- 스캐너 품질, 악필, 노이즈 등 불량 이미지 처리 문제
- 컴퓨터 비전과 언어 기능 지식 활용 필요
