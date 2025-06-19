# MongoDB MCP Server

지난 시간에 다룬 [5분만에 GitHub MCP Server 구축해서 GitHub 작업 시키기](https://data-make.tistory.com/799)에 이어 이번에는 `MongoDB MCP Server` 구축 방법을 소개하려고 합니다.

## MongoDB MCP Server

[mongodb-mcp-server](https://github.com/mongodb-js/mongodb-mcp-server) 역시 지나번에 다룬 GitHub MCP Server와 유사합니다.  
MongoDB와 연동하려면 `mcp.json` 파일만 수정하면 준비가 완료됩니다.

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

추가가 완료되었다면 `mcp.json` 파일에서 Start 버튼을 누르거나,  
Command Palette(cmd + shift + p) > MCP: List Servers > mongodb  
를 선택해 MongoDB MCP Server를 실행합니다.

## Agent mode

이제 본격적으로 MongoDB MCP Server로 요청을 보내보겠습니다.

시작은 간단한 질문으로 요청해 보았습니다.

---

**시작 질문**

```text
데이터베이스에 있는 컬렉션 목록을 보여줘
```

응답

```text
먼저 데이터베이스 목록을 조회하겠습니다:
> mongodb (MCP Server)...

다음으로 각 데이터베이스의 컬렉션을 조회하겠습니다. 먼저 'aaa' 데이터베이스의 컬렉션을 확인해보겠습니다:
> mongodb (MCP Server)...

'bbb' 데이터베이스의 컬렉션도 확인해보겠습니다:
> mongodb (MCP Server)...

데이터베이스별 컬렉션 목록은 다음과 같습니다:

1. aaa 데이터베이스:
- abcd123
2. bbb 데이터베이스:
- apple 
- banana
- cherry

나머지 admin, config, local은 MongoDB의 시스템 데이터베이스이고, ccc는 비어있는 테스트용 데이터베이스로 보입니다.

특정 데이터베이스나 컬렉션에 대해 더 자세한 정보가 필요하시다면 말씀해 주세요.
```

개인 PC의 로컬 MongoDB에 연동 후 테스트한 결과인데 데이터베이스 목록과 컬렉션 목록을 정확하게 조회하였습니다.

다음 질문부터는 데이터가 많은 데이터베이스에 연동해서 테스트해보기 위해 회사 테스트 데이터베이스에 연동하여 테스트 해보았습니다.

---

**첫 번째 질문**

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

이제 요청하신 조건에 맞는 데이터를 추출해보겠습니다. merchant_id가 "ZXC1234"이고 created_date이 2025-06-01 이후인 데이터에서 total_amount와 merchant_id를 가져와서 total_amount 기준으로 상위 10개를 조회하겠습니다.

검색 결과를 확인해보겠습니다. 현재 2건의 데이터가 조회되었습니다. 표로 정리해드리겠습니다:

(표 생략)

조회 결과 요약:
- 조건: merchant_id = "ZXC1234", created_date >= 2025-06-01
- 조회된 레코드 수: 2건
- 정렬: total_amount 내림차순
- 상위 10개 요청했지만 조건에 맞는 데이터가 2건만 존재합니다.

두 레코드 모두 total_amount가 3,000원으로 동일하며, 2025년 6월 12일에 생성된 데이터입니다.
```

이정도 간단한 조회 요청은 그래도 당연히 정확하게 조회할 수 있을 것이라고 생각은 했다.  
표 정리를 포함하여 조회 결과도 요약해 주는 것을 볼 수 있다.

