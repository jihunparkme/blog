# Gemini CLI + Atlassian MCP 조합으로 SR 생성하기

사내 배포 프로세스에서는 별도의 Service Request(SR) 지라 티켓이 필요합니다. 기존에는 작업 티켓의 내용을 수동으로 복사하여 SR 티켓을 생성하는 방식을 사용했는데, 이는 시간 소모적이고 실수가 발생할 수 있는 비효율적인 프로세스였습니다.
<br/>

이러한 문제를 해결하기 위해 기존 작업 티켓을 기반으로 SR 티켓을 자동으로 생성하는 방법을 고민하게 되었습니다. 여러 도구들을 서칭한 결과, Atlassian에서 제공하는 `MCP`와 `Gemini CLI`를 조합하여 SR을 생성하는 방법을 공유하려고 합니다.

## 필요한 도구

- [mcp-atlassian](https://github.com/sooperset/mcp-atlassian)
- [gemini-cli](https://github.com/google-gemini/gemini-cli)

## Gemini CLI 설정

Gemini CLI 설정 파일(`~/.gemini/settings.json`)에 Atlassian MCP 설정을 추가합니다.
- MCP-Atlassian 실행을 위해 `Docker`가 필요합니다.
<br/>

```bash
{
  ..
  mcpServers: {
      mcp-atlassian: {
        command: docker,
        args: [
          run,
          -i,
          --rm,
          -e, CONFLUENCE_URL,
          -e, CONFLUENCE_PERSONAL_TOKEN,
          -e, CONFLUENCE_SPACES_FILTER,
          -e, JIRA_URL,
          -e, JIRA_PERSONAL_TOKEN,
          -e, JIRA_PROJECTS_FILTER,
          ghcr.io/sooperset/mcp-atlassian:latest
        ],
        env: {
          CONFLUENCE_URL: https://wiki.company.com,
          CONFLUENCE_PERSONAL_TOKEN: "your_confluence_pat",
          CONFLUENCE_SPACES_FILTER: project,
          JIRA_URL: https://jira.company.com,
          JIRA_PERSONAL_TOKEN: "your_jira_pat",
          JIRA_PROJECTS_FILTER: PROJECT
        }
      }
  }
}
```

## 프로세스 정의

`~/.gemini/GEMINI.md` 파일에 SR 생성 프로세스를 정의합니다.  
특히 사용자 지정 필드를 사용하는 경우, 필드명과 데이터 타입을 명확히 지정하는 것이 중요합니다.
<br/>

```bash
 ---
 
 ## Custom Command: Create SR from Ticket
 
 아래 프로세스를 "티켓으로 SR 생성"이라는 이름으로 기억해 줘.
 
 내가 "PROJECT-XXXXX로 SR 생성해 줘"라고 요청하면, 다음 단계를 수행해야 해:
 
 1.  **데이터 가져오기**: `PROJECT-XXXXX`로 지정된 원본 Jira 티켓의 세부 정보를 가져온다.
 2.  **새 이슈 준비**: 다음 속성을 가진 새 Jira 이슈를 준비한다:
     *   **프로젝트**: `PROJECT`
     *   **이슈 타입**: `SR`
     *   **요약**: 원본 티켓의 요약을 그대로 사용한다.
     *   **설명**: 원본 티켓의 설명을 그대로 사용하되, 모든 '\r\n'을 '\n'으로 바꾼다.
     *   **담당자**: 원본 티켓의 담당자와 동일하게 지정한다.
     *   **마감일**: 현재 날짜로부터 정확히 한 달 뒤로 설정한다.
     *   **사용자 지정 필드 `customfield_12345` (SR 승인자)**: 문자열 `"hello.world"`으로 설정한다.
     *   **사용자 지정 필드 `customfield_54321` (서비스 분류)**: `["[플랫폼] 프로젝트플랫폼"]` 문자열을 포함하는 배열로 설정한다.
 3.  **이슈 생성**: 이전 단계에서 준비된 데이터로 새 SR 이슈를 생성한다.
 4.  **이슈 연결**: 새 이슈가 생성되면, 새 이슈와 원본 `PROJECT-XXXXX` 티켓 사이에 'Relates' 관계의 링크를 생성한다.
```

## 사용 방법

SR 티켓 생성이 필요할 때 다음과 같이 명령어를 실행합니다.

```bash
> PROJECT-12254 으로 SR 생성해 줘
```

**실행 결과 예시**

```bash
알겠습니다. PROJECT-12254 티켓을 기반으로 SR을 생성하겠습니다. 먼저 원본 티켓의 정보를 가져오겠습니다.

...

네, 원본 티켓 정보를 확인했습니다. 이제 해당 정보로 새로운 SR 이슈를 생성하겠습니다. 마감일은 한 달 뒤인 2025년 9월 25일로 설정됩니다.

...

SR 이슈(PROJECT-12267)가 성공적으로 생성되었습니다. 이제 원본 티켓과 'Relates' 관계로 연결하겠습니다.

...

SR 생성이 완료되었습니다. 새로운 SR 티켓 번호는 PROJECT-12267이며, 원본 티켓 PROJECT-12254와 연결되었습니다.
```
