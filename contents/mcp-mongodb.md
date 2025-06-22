# MongoDB MCP Server로 데이터 추출해보기

지난 시간에 다룬 [5분 만에 GitHub MCP Server 구축해서 GitHub 작업 시키기](https://data-make.tistory.com/799)에 이어, 이번에는 `MongoDB MCP Server` 구축 방법을 소개하려고 합니다.

## MongoDB MCP Server

[mongodb-mcp-server](https://github.com/mongodb-js/mongodb-mcp-server) 설정 역시 지난번에 다룬 GitHub MCP Server와 유사합니다.<br/>
다른 MCP 서버와 동일하게 `mcp.json` 파일에서 MongoDB MCP Server 설정만 추가하면 준비가 완료됩니다.

**실제 MongoDB 클러스터에 연동**

```json
{
    "mcp": { // MCP 서버의 전체 설정을 담는 객체
        "inputs": [], // 입력 소스(예: 파일, 폴더 등)를 지정하는 배열
        "servers": { // 사용할 MCP 서버들을 정의하는 객체
            "mongodb": { // 'mongodb'라는 이름의 MCP 서버 설정
                "command": "npx", // 서버 실행에 사용할 명령어
                "args": [ // npx에 전달할 인자(Arguments) 목록
                    "-y", // npx 실행 시 자동으로 yes를 선택
                    "mongodb-mcp-server", // 실행할 패키지 이름
                    "--connectionString", // MongoDB 연결 문자열 옵션
                    "mongodb+srv://<yourcluster>", // 실제 MongoDB 클러스터의 연결 정보
                    "--readOnly" // 서버를 읽기 전용 모드로 실행
                ],
            }
        }
    },
}
```

**MongoDB MCP Server가 사용할 포트를 명시할 수도 있습니다.**

```json
"mongodb": {
    "command": "npx",
    "args": [
        "-y", 
        "mongodb-mcp-server", 
        "--connectionString", 
        "mongodb://localhost:27017",
        "--port", // MCP 서버 포트             
        "27018", // MCP 서버가 사용할 포트
        "--readOnly"
    ],
}
```

설정이 완료되면 `mcp.json` 파일에서 Start 버튼을 누르거나,<br/>
`Command Palette(cmd + shift + p) > MCP: List Servers > mongodb`를 선택하여 MongoDB MCP Server를 실행할 수 있습니다.

## Agent mode

이제 본격적으로 MongoDB MCP Server로 요청을 보내보겠습니다.
- Claude 3.5 사용. ~~(Claude 4는 데이터를 조회한다고 해놓고 도망가는 행동을 자주 보였습니다.)~~
- Claude 3.5를 계속 사용하다 보니 결과는 주지 않고 조회만 반복하는 현상이 나타나, 다시 Claude 4를 사용하게 되었는데, Claude 3.5와의 대화 내용이 학습되었는지 이후에는 대답을 잘 해주었습니다.

<br/>
간단한 질문으로 시작해 보겠습니다.
<br/>

---

🚗 **시작 질문**

```text
데이터베이스에 있는 컬렉션 목록을 보여줘
```

결과

```text
먼저 데이터베이스 목록을 조회하겠습니다:
> mongodb (MCP Server)...

다음으로 각 데이터베이스의 컬렉션을 조회하겠습니다. 먼저 'aaa' 데이터베이스의 컬렉션을 확인해보겠습니다:
> mongodb (MCP Server)...

'bbb' 데이터베이스의 컬렉션도 확인해보겠습니다:
> mongodb (MCP Server)...

데이터베이스 별 컬렉션 목록은 다음과 같습니다:

1. aaa 데이터베이스:
- abcd123
1. bbb 데이터베이스:
- apple 
- banana
- cherry

나머지 admin, config, local은 MongoDB의 시스템 데이터베이스이고, ccc는 비어 있는 테스트용 데이터베이스로 보입니다.

특정 데이터베이스나 컬렉션에 대해 더 자세한 정보가 필요하시다면 말씀해 주세요.
```

로컬 MongoDB에 연동 후 테스트한 결과, **데이터베이스 목록**과 **컬렉션 목록**을 정확하게 조회하였습니다.<br/>

테스트 데이터베이스와 연동하여 요청해 보았을 때에도 **컬렉션의 성격에 따라 정확하게 분류하고 설명**하며, **존재하지 않는 컬렉션을 표시**하거나 **존재하는 컬렉션을 누락**하는 경우는 없었습니다.<br/>

다음 질문부터는 데이터가 어느 정도 쌓여 있는 테스트 데이터베이스에 연동해서 요청을 보내보려고 합니다.<br/>

---

1️⃣ **첫 번째 질문** (간단한 조회 1)

```text
xxx_collection 데이터를 추출해 주세요.
xxx_field 기준으로 상위 10개만 보여주세요.

조건은 아래와 같습니다.
- merchant_id : ZXC1234
- created_date : 2025-06-01 이후
- 추출 데이터 : merchant_id, total_amount
```

결과

```text
MongoDB에서 xxx_collection 컬렉션에서 데이터를 추출해드리겠습니다. 먼저 어떤 데이터베이스와 컬렉션이 있는지 확인해보겠습니다.
> mongodb (MCP Server)...

이제 aaa 데이터베이스의 컬렉션을 확인해보겠습니다.
> mongodb (MCP Server)...

이제 xxx_collection 컬렉션의 스키마를 확인해보겠습니다.
> mongodb (MCP Server)...

이제 요청하신 조건에 맞는 데이터를 추출해보겠습니다. merchant_id가 "ZXC1234"이고 created_date가 2025-06-01 이후인 데이터에서 total_amount와 merchant_id를 가져와서 total_amount 기준으로 상위 10개를 조회하겠습니다.

검색 결과를 확인해보겠습니다. 현재 2건의 데이터가 조회되었습니다. 표로 정리해드리겠습니다:

(표 생략)

조회 결과 요약:
- 조건: merchant_id = "ZXC1234", created_date >= 2025-06-01
- 조회된 레코드 수: 2건
- 정렬: total_amount 내림차순
- 상위 10개를 요청했지만 조건에 맞는 데이터가 2건만 존재합니다.

두 레코드 모두 total_amount가 3,000원으로 동일하며, 2025년 6월 12일에 생성된 데이터입니다.
```

간단한 조회 요청은 MCP가 충분히 처리할 수 있을 것이라고 생각했습니다.<br/>
표 정리를 포함하여 조회 결과도 요약해 주는 것을 볼 수 있습니다.

---

2️⃣ 두 번째 질문 (간단한 조회 2)

```text
aaa 데이터베이스의 log 컬렉션에서 아래 조건에 맞게 모든 결과를 조회해서 보여줘
 
조회 조건
- channel: OFFLINE
- created_date: {$gte: ISODate('2025-06-19T15:00:00'), $lt: ISODate('2025-06-20T15:00:00')} 조건을 그대로 사용
- source: PAYMENT
- status: FAILED
 
조회 필드
- action
- group_id
- serial
- status
- body
```

**조회 요청 시 표현 방식**

'조회해줘'라는 문구로 요청했을 때는 쿼리를 반복하기만 하고 **실제 결과를 보여주지 않는 현상**이 있었습니다.<br/>
반면 '보여줘'라고 요청하자 **정상적으로 조회 후 결과를 출력**했습니다.<br/>
따라서 결과를 직접 확인하고 싶다면 **'보여줘'와 같은 명확한 요청 동사를 사용**하는 것이 좋을 것 같습니다.<br/>

**날짜 조건 설정**

MongoDB는 특성상 **한국 시간(KST)** 이 아닌 **UTC(협정 세계시)** 를 사용합니다.<br/>
이 때문에 KST와 UTC를 모두 고려하여 조회 조건을 요청하면, MCP 서버가 **임의로 타임존을 변경하여 날짜를 잘못 변환하는 현상**이 나타났습니다.<br/>
이 문제를 해결하기 위해서는 **날짜 조건을 쿼리 그대로 명확하게 전달**하는 것이 가장 정확했습니다.<br/>

**조회 필드 명시**

조회할 필드를 요청에 포함시키지 않으면 MCP 서버가 **임의의 필드만 추출하여 결과를 제공**하는 경우가 있었습니다.<br/>
원하는 필드만 정확하게 조회하기 위해서는 **요청 시 조회할 필드명을 명확하게 지정**해주는 것이 좋습니다.

**결과**

```text
MongoDB에서 지정하신 조건에 맞는 데이터를 조회해드리겠습니다.
> mongodb (MCP Server)...

조회 결과 설명
...
조회 조건
...
조회된 주요 필드
...
주요 특징
...
```

이어서 조회 결과를 csv 파일로 추출할 수도 있었습니다.

결과적으로, 조회 필드를 명시적으로 요청했음에도 불구하고 MCP 서버가 json 타입의 body 필드의 경우 하위 필드의 일부만 임의로 추출해서 보여주는 현상이 있었습니다. 하위 필드를 가지고 있다면 하위 필드에 대한 명확한 지정을 해주는 것이 필요해 보입니다.

---

3️⃣ 세 번째 질문 (조인이 포함된 복잡한 조회)

이제 간단한 조회가 아닌 조인이 포함된 요청을 해보겠습니다.

**요청**

```text
apple 컬렉션과 banana 컬렉션을 조인해서 데이터를 추출해 주는데 표 형태로 보여주세요
 
---
 
apple 컬렉션 기준 조회 조건 (and 조건)
- p.p_date: {$gte: ISODate('2024-06-02T15:00:00')},
- e.origin_init_p_date: {$gte: ISODate('2024-06-02T15:00:00')},
 
apple 컬렉션 기준 그룹 조건
- p.p_rule_id
- channel
- cid
- payment_method
 
조인 조건
왼쪽은 banana 컬렉션 기준, 오른쪽은 apple 컬렉션 기준이고, apple 컬렉션의 left join으로 banana 컬렉션에 조인되는 데이터가 없다면 공백으로 치환해 주세요.
- p_rule.$id : p.p_rule_id
- channel : channel
- cid : cid
- payment_method : payment_method
 
결과 필드
- "01_채널타입": "apple.channel",
- "02_상호명": "banana.description"
- "03_CID": "apple.cid",
- "04_정대상일": "apple.target_date",
- "05_지급일(As is)": "apple.e.origin_init_p_date",
- "06_지급일(To be)": "apple.p.p_date",
- "07_정산주기": banana.cycle_type,
- "08_지급그룹ID": "p.p_rule_id",
- "09_YID": "apple.yid",
- "10_건별내역건수": "count", // 그룹으로 묶인 수량
- "11_지급예정금액": "apple.p.p_amount",
```

**결과**

```text
MongoDB에서 apple과 banana 컬렉션을 LEFT JOIN하여 데이터를 추출해드리겠습니다. LEFT JOIN으로 설정하여 banana에 매칭되는 데이터가 없어도 apple 데이터는 표시되도록 하겠습니다.
> mongodb (MCP Server)...

LEFT JOIN 결과가 조회되었습니다. 총 3개의 그룹이 조회되었으며, banana와 매칭되지 않는 데이터는 공백으로 표시되었습니다. 결과를 표로 정리해드리겠습니다:

조회 결과
...

조회 결과 요약
...

주요 특징
...
```

이번 요청에서는 조회 결과, 조회 결과 요약, 주요 특징이 포함되었지만, 다른 요청에서는 적용된 조회 조건, 그룹핑 기준, 조인 조건, 결과 분석이 포함되었는데, 매번 요청 때마다 템플릿이 달라지는 것으로 보입니다.<br/>
템플릿에 맞는 결과를 받길 원한다면 템플릿을 명시하여 요청하면 좋을 것 같습니다.<br/>

생각보다 복잡한 쿼리가 필요한 조회였는데, 놀랍게도 결과가 정확하게 나왔습니다.<br/>
그렇다면 의도한 쿼리대로 조회가 잘 되었는지, 조회에 사용된 쿼리도 확인해 보겠습니다.<br/>

---

**질문**

```text
위 추출 결과에 사용된 몽고디비 쿼리를 작성해 주세요
```

**결과**

```text
위에서 사용된 MongoDB 쿼리를 작성해드리겠습니다. 이 쿼리는 apple과 banana 컬렉션을 LEFT JOIN하여 데이터를 추출하는 aggregation pipeline입니다.
> mongodb (MCP Server)...

MongoDB 쿼리를 mongodb_query.js 파일로 작성했습니다. 이 쿼리는 다음과 같은 특징을 가지고 있습니다:

쿼리 구조
...

실행 방법
...
```

## 마치며

직접 작성한 쿼리로 먼저 결과를 확인한 후, MCP 서버가 원하는 결과를 도출할 때까지 프롬프트를 계속 수정하며 테스트를 진행했습니다.<br/>
결과적으로는 원하던 값을 얻을 수 있었지만, 정확한 결과를 모르는 상태에서 프롬프트만으로 얻어낸 데이터를 과연 신뢰할 수 있을지는 의문입니다.<br/><br/>

높은 확률(사실상 100%)로 정확한 결과를 도출하는 템플릿이 존재하고, 그 템플릿에서 크게 벗어나지 않는 반복적인 요청이 있다면 운영 환경에서 유용하게 활용할 수 있을 것 같습니다.<br/>
하지만, 매번 요구사항이 달라지는 현 상황에서는 대응하기 어려울 수도 있다는 생각이 듭니다.

## 참고

- https://github.com/mongodb-js/mongodb-mcp-server
- https://www.mongodb.com/blog/post/announcing-mongodb-mcp-server
- https://www.mongodb.com/ko-kr/docs/manual/mcp/

