<!-- filepath: /Users/aaron/gitRepository/blog/contents/mcp.md -->
# MCP Start

MCP(Model Context Protocol).  
들어만 보았던 MCP를 이제 직접 사용해 보려고 합니다. 처음에는 진입장벽이 높을 것이라고 생각했지만, 실제로 사용해보니 생각보다 매우 쉽게 AI를 활용할 수 있다는 것을 알게 되었습니다.

그럼 가장 쉽고 빠르게 MCP를 사용해볼 수 있는 `GitHub MCP`를 함께 살펴보겠습니다!

## MCP

MCP(Model Context Protocol) ⁉️  
MCP는 다양한 인공지능 모델과 도구, 그리고 개발 환경 간의 상호 운용성을 높이기 위해 고안된 표준 프로토콜입니다. MCP를 통해 개발자는 AI 모델, 플러그인, 외부 서비스, 그리고 IDE(예: VS Code) 등 다양한 컴포넌트들을 손쉽게 연결하고 통합할 수 있습니다.

주요 특징
- 표준화된 인터페이스: 다양한 AI 모델과 도구가 동일한 방식으로 통신할 수 있도록 표준화된 API와 메시지 형식을 제공합니다.
- 확장성: 새로운 모델이나 도구를 손쉽게 추가할 수 있으며, 다양한 언어와 플랫폼을 지원합니다.
- 보안: 인증 및 권한 관리 기능이 내장되어 있어 안전하게 외부 서비스와 연동할 수 있습니다.
- 유연한 배포: 로컬 환경, 클라우드, 컨테이너(Docker) 등 다양한 환경에서 MCP 서버를 실행할 수 있습니다.

활용 예시
- VS Code에서 다양한 AI 모델(예: Copilot, GPT, Claude 등)을 플러그인 형태로 연동
- 외부 API(예: GitHub, Slack, Jira 등)와 개발 환경의 통합 자동화

## GitHub MCP Server

[GitHub MCP Server](https://github.com/github/github-mcp-server?tab=readme-ov-file#installation)는 README 파일의 설명만으로도 손쉽게 설치하고 사용할 수 있습니다.

이 글에서는 다른 MCP Hosts를 사용하지 않고 로컬에 GitHub MCP 서버를 직접 구축해보겠습니다.  
로컬 컨테이너에서 GitHub MCP 서버를 실행하기 위해서는 `Docker`가 필요합니다.  
또한 VS Code를 사용하여 진행할 것이므로 `VS Code`가 설치되어 있지 않다면 먼저 설치해주세요.

준비물
- Docker
- VS Code + Copilot MCP Extensions 설치
- GitHub Personal Access Token

1️⃣ VS Code를 실행하고 작업 디렉토리의 루트 경로에 `.vscode` 디렉토리를 생성한 후 `mcp.json` 파일을 작성합니다.

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

2️⃣ `Copilot Chat`에서 `Agent` 모드로 설정합니다.
- Command Palette(cmd + shift + p) > `Chat: Open Chat`을 통해 `Copilot Chat` 모드를 실행할 수 있습니다.

<img src="https://github.com/jihunparkme/blog/blob/main/img/mcp/chat-tool.png?raw=true" width="60%">

3️⃣ 이제 `mcp.json` 파일을 보면 "servers" 하단에 `Start` 버튼이 표시됩니다. 이 버튼을 클릭합니다.

<img src="https://github.com/jihunparkme/blog/blob/main/img/mcp/mcp-md.png?raw=true" width="60%">

4️⃣ GitHub MCP 서버가 정상적으로 실행되면 Start 버튼이 `Running | Stop | Restart`로 변경되고 

<img src="https://github.com/jihunparkme/blog/blob/main/img/mcp/mcp-running.png?raw=true" width="40%">

도커에서 `trusting_ramanujan`이라는 이름의 컨테이너가 실행된 것을 확인할 수 있습니다.
- 컨테이너 이름은 재미있는 이름으로 무작위 생성됩니다. 이것도 AI가 만들어주는 거겠죠? 🧐
- 예시: angry_beaver, quizzical_merkle, kind_chaplygin...

5️⃣ 이제 설정이 완료되었습니다!

뭐라구욧 ⁉️ 그렇습니다. 이제 `Copilot Chat`에 명령만 하면 원하는 작업을 모두 수행해줍니다.  
간단한 예시로 "내가 지금 어떤 레포지토리들을 가지고 있는지 찾아줘"라고 요청해보겠습니다.

그러면 친절한 Copilot이 "GitHub 토큰을 사용하여 귀하의 GitHub 레포지토리 목록을 확인해보겠습니다."라고 답변하며, 로컬에서 실행 중인 GitHub MCP 서버를 통해 GitHub API 요청을 처리하여 보유한 레포지토리 목록을 보여줍니다.

GitHub MCP 서버를 통해 다양한 요청을 처리할 수 있으며, 자세한 내용은 [Tools](https://github.com/github/github-mcp-server?tab=readme-ov-file#tools) 문서에서 확인할 수 있습니다.

마지막으로, 제가 시도해본 몇 가지 요청을 소개하며 글을 마무리하겠습니다:
- "xxx 레포지토리의 main 브랜치 최근 커밋 내역을 알려줘"
- "이 경로의 파일을 'xxx'라는 커밋 메시지로 커밋해줘"
- 아래 경로에 있는 파일의 맞춤법을 검사해 주고, 각 문장들을 자연스럽게 다듬어 줘

## 마치며

기존에는 `GitHub REST API`사용을 위해 문서를 읽어보며 테스트하고 API 요청 코드를 작성하는 등 다양한 작업이 필요했지만 이제 `GitHub MCP Server`를 통해 단 몇 분만에 모든 작업이 가능해졌습니다. Github 말고도 MCP 서버를 제공하는 곳이라면 유사한 방식으로 API 요청을 쉽게 할 수 있게 되었습니다.

기술이 빠르게 발전하는 만큼 저도 발빠르게 사용해 보며 글을 작성하게 되었는데요.  
확실히 들어만 보았을 때와 다르게 직접 사용해 보니 신기하기도 하고 재미있었답니다.  
앞으로도 어떤 AI 기술들이 소개될지.. 걱정반 기대반이랄까요..

## 참고

[Use MCP servers in VS Code (Preview)](https://code.visualstudio.com/docs/copilot/chat/mcp-servers)


