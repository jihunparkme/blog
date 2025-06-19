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
mongodb (MCP Server)...

다음으로 각 데이터베이스의 컬렉션을 조회하겠습니다. 먼저 'aaa' 데이터베이스의 컬렉션을 확인해보겠습니다:
mongodb (MCP Server)...

'bbb' 데이터베이스의 컬렉션도 확인해보겠습니다:
mongodb (MCP Server)...

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

---
