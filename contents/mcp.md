# MCP Start

MCP(Model Context Protocol).  
들어만 보았던 MCP. 이제 직접 사용해 보려고 합니다. 처음에는 진입장벽이 높을 것이라고 생각했지만, 막상 딸깍딸깍 하다보니 생각보다 너무 간단하게 AI를 활용할 수 있다는 것을 느끼게 되었습니다.

자! 그럼 가장 쉽고 빠르게 MCP를 사용해 볼 수 있는 `GitHub MCP`를 사용해 봅시다~!

## MCP

MCP(Model Context Protocol) ⁉️  
MCP는 다양한 인공지능 모델과 도구, 그리고 개발 환경 간의 상호 운용성을 높이기 위해 고안된 표준 프로토콜입니다. MCP는 개발자가 AI 모델, 플러그인, 외부 서비스, 그리고 IDE(ex. VS Code) 등 다양한 컴포넌트들을 손쉽게 연결하고 통합할 수 있도록 설계되었습니다.

주요 특징
- 표준화된 인터페이스: 다양한 AI 모델과 도구가 동일한 방식으로 통신할 수 있도록 표준화된 API와 메시지 포맷을 제공합니다.
- 확장성: 새로운 모델이나 도구를 손쉽게 추가할 수 있으며, 다양한 언어와 플랫폼을 지원합니다.
- 보안: 인증 및 권한 관리 기능을 내장하여 안전하게 외부 서비스와 연동할 수 있습니다.
- 유연한 배포: 로컬 환경, 클라우드, 컨테이너(Docker) 등 다양한 환경에서 MCP 서버를 실행할 수 있습니다.

활용 예시
- VS Code에서 다양한 AI 모델(ex. Copilot, GPT, Claude 등)을 플러그인 형태로 연동
- 외부 API(ex. GitHub, Slack, Jira 등)와 개발 환경의 통합 자동화

## GitHub MCP Server

[GitHub MCP Server](https://github.com/github/github-mcp-server?tab=readme-ov-file#installation)를 사용하는 방법은 README 파일에 작성된 설명만으로도 손쉽게 따라할 수 있습니다.

이 글에서는 다른 MCP Hosts를 사용하지 않고 로컬에 GitHub MCP 서버를 띄워보려고 합니다.  
로컬 컨테이너에서 GitHub MCP 서버를 실행시키기 위해 `Docker` 설치가 필요합니다.  
추가로 VS Code를 사용하여 진행할 것이므로 `VS Code`도  설치가 되어있지 않다면 설치가 필요합니다.

준비물
- Docker
- VS Code + Copilot MCP Extensions 설치
- GitHub Personal Access Token

1️⃣ 이제 VS code를 실행시키고 작업 디렉토리의 루트 경로에 `.vscode` 디렉토리를 생성한 후 `mcp.json` 파일을 작성합니다.

**.vscode/mcp.json**


```json
{
  "inputs": [
    {
      "type": "promptString",
      "id": "github_token",
      "description": "GitHub Personal Access Token",
      "password": true
    }
  ],
  "servers": {
    "github": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e",
        "GITHUB_PERSONAL_ACCESS_TOKEN",
        "ghcr.io/github/github-mcp-server"
      ],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "${input:github_token}"
      }
    }
  }
}
```

2️⃣ `Copilot Chat`에서 `Agent` 모드로 세팅해 줍니다.
- Command Palette(cmd + shift + p) > `Chat: Open Chat`을 통해 `Copilot Chat` 모드에 진입할 수 있습니다.

<img src="../img/mcp/chat-tool.png" width="60%">

3️⃣ 이제 다시 `mcp.json` 파일을 보면 "servers" 하단에 `Start` 라는 실행 버튼이 보이게 됩니다. Click!

<img src="../img/mcp/mcp-md.png" width="60%">

4️⃣ GitHub MCP 서버가 정상적으로 실행되었다면 Start 버튼은 `Running | Stop | Restart`으로 대체되고 

<img src="../img/mcp/mcp-running.png" width="40%">

도커를 확인해 보면 `trusting_ramanujan`라는 이름의 컨테이너가 실행된 것을 확인할 수 있습니다.

<img src="../img/mcp/trusting_ramanujan.png" width="30%">






[Use MCP servers in VS Code (Preview)](https://code.visualstudio.com/docs/copilot/chat/mcp-servers)






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





