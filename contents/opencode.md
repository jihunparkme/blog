# opencode

오픈 소스 AI 코딩 에이전트

## Claude Code vs OpenCode

✅ 모델 자유도: Claude 뿐만 아니라 ChatGPT, OpenAI, GitHub Copilot, Google Gemini, 로컬 모델까지 75개 이상의 LLM 프로바이더를 지원

✅ LSP(Language Server Protocol) 기본 내장:  
- LSP 덕분에 에이전트가 IDE 수준으로 코드를 이해

✅ 클라이언트/서버 아키텍처
- PC에서 OpenCode 서버를 돌리고, 모바일에서 원격으로 조작 가능

## Key Features









OpenCode의 강점은 유연함과 정밀함에 있습니다.
모델 선택의 자유가 압도적입니다. Models.dev를 통해 75개 이상의 LLM 프로바이더를 지원하는데, 직접 API 키를 연결할 수도 있고, OpenCode 팀이 운영하는 OpenCode Zen 서비스를 쓸 수도 있습니다. Zen은 코딩 에이전트용으로 테스트하고 검증한 모델들을 큐레이션해서 제공하는 서비스입니다. 귀찮게 모델을 고르지 않아도 검증된 선택지를 쓸 수 있죠.
LSP 자동 로딩도 개발 경험을 크게 바꿉니다. 프로젝트를 열면 OpenCode가 알아서 적절한 Language Server를 찾아 실행합니다. 덕분에 에이전트가 타입 정보, 함수 시그니처, 참조 관계까지 정확하게 파악합니다. 그냥 코드를 텍스트로 읽는 것과 차원이 다릅니다.
멀티 세션과 에이전트 시스템도 인상적입니다. 같은 프로젝트에서 여러 에이전트를 동시에 돌릴 수 있고, Build 모드와 Plan 모드를 Tab 키로 전환하며 쓸 수 있습니다. Build는 전체 접근 권한을 가진 실행 에이전트, Plan은 읽기 전용으로 분석과 탐색을 담당합니다. 복잡한 작업을 할 때 Plan 모드에서 먼저 전략을 세우고, Build 모드로 넘어가 실행하는 워크플로우가 정말 유용합니다.
세션 공유도 가능합니다. /share 명령으로 대화를 링크로 만들어 팀원에게 보낼 수 있습니다. "여기까지 했는데 이렇게 하면 어때?"를 공유하는 게 쉬워지죠.
Undo/Redo는 당연해 보이지만 없으면 불편한 기능입니다. 에이전트가 만든 변경 사항을 /undo로 되돌리고, 마음이 바뀌면 /redo로 다시 적용할 수 있습니다.
프라이버시도 강점입니다. OpenCode는 사용자 코드나 컨텍스트를 저장하지 않습니다. 기업 환경이나 민감한 프로젝트에서도 안심하고 쓸 수 있죠.
플러그인 시스템은 확장성을 보장합니다. 커스터마이징이 필요하면 플러그인을 만들면 됩니다. oh-my-opencode 같은 커뮤니티 플러그인도 이 생태계에서 자라나고 있습니다.
데스크톱 앱도 베타로 나왔습니다. macOS, Windows, Linux를 모두 지원하고, TUI가 부담스러운 사람은 GUI로 쓸 수 있습니다. MCP 서버 연동도 지원해서 Model Context Protocol로 외부 도구와 통합할 수 있고, 테마, 키바인드, 권한 설정까지 세세하게 커스터마이징할 수 있습니다.
시작하는 법
OpenCode를 시작하는 건 간단합니다.
설치는 한 줄이면 됩니다.
curl -fsSL https://opencode.ai/install | bash
npm, brew, scoop 같은 패키지 매니저로도 설치할 수 있습니다.
설정은 /connect 명령으로 합니다. OpenCode Zen을 추천하지만, 직접 API 키를 넣어도 됩니다. ChatGPT Plus나 GitHub Copilot 계정이 있다면 그걸로도 시작할 수 있어요.
초기화는 /init으로 합니다. 프로젝트를 분석해서 AGENTS.md 파일을 만들어줍니다. 이 파일은 에이전트가 프로젝트 구조와 컨텍스트를 이해하는 데 쓰입니다.
사용은 직관적입니다. Tab 키로 Build와 Plan 모드를 전환하며 작업하면 됩니다. @를 입력하면 파일을 퍼지 검색해서 컨텍스트에 추가할 수 있고, 이미지를 드래그 앤 드롭하면 디자인을 참고할 수도 있습니다. /undo, /redo로 변경을 관리하고, /share로 세션을 공유하세요.
마무리
OpenCode는 오픈소스 AI 코딩 에이전트의 새로운 기준을 만들고 있습니다. 모델 자유도, LSP 통합, 터미널 경험, 프라이버시, 플러그인 생태계까지 갖춘 도구는 흔치 않습니다. 특정 회사의 플랫폼에 종속되지 않으면서도, 검증된 경험을 제공하는 선택지가 필요했다면 OpenCode를 한번 써보세요.
더 자세한 정보는 opencode.ai (https://opencode.ai)에서, 코드는 GitHub (https://github.com/anomalyco/opencode)에서 확인할 수 있습니다. 플러그인 생태계도 성장하고 있으니, oh-my-opencode 같은 커뮤니티 프로젝트도 살펴보시면 좋습니다.
터미널에서, 혹은 데스크톱에서. 원하는 환경에서 원하는 모델로. OpenCode와 함께 코딩하세요.























## Install

설치 스크립트를 사용한 설치 > 최신 릴리스는 OpenCode tap 사용을 권장

```shell
curl -fsSL https://opencode.ai/install | bash


...

Installing opencode version: 1.2.10
■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 100%
Successfully added opencode to $PATH in /Users/aaron/.zshrc

                                 ▄
█▀▀█ █▀▀█ █▀▀█ █▀▀▄ █▀▀▀ █▀▀█ █▀▀█ █▀▀█
█░░█ █░░█ █▀▀▀ █░░█ █░░░ █░░█ █░░█ █▀▀▀
▀▀▀▀ █▀▀▀ ▀▀▀▀ ▀  ▀ ▀▀▀▀ ▀▀▀▀ ▀▀▀▀ ▀▀▀▀


OpenCode includes free models, to start:

cd <project>  # Open directory
opencode      # Run command

For more information visit https://opencode.ai/docs
```

다운로드가 완료되었다면 `~/.zshrc` 파일 적용을 해주어야 opencode 설치 경로가 정상적으로 등록됩니다.

```shell
source ~/.zshrc

...
export PATH=/Users/{id}/.opencode/bin:$PATH
```

## Config

opencode TUI 모드 실행

```shell
opencode
```

API 키를 설정하면 원하는 LLM 제공자를 사용 가능
- TUI에서 `/connect` 명령을 실행한 뒤 구독하고 있는 LLM Provider 선택 및 API 적용

## Init

프로젝트 경로로 이동해서 OpenCode 초기화를 통해 프로젝트를 분석하고 프로젝트 루트에 `AGENTS.md` 파일을 생성
- 이렇게 하면 OpenCode가 프로젝트 구조와 사용 중인 코딩 패턴을 더 잘 이해

```shell
cd /path/to/project
opencode
```

TUI에서 `/init` 명령을 실행하여 초기화

## Using

✅ **기능 추가**

TUI에서 `<TAB>` 키를 눌러서 plan 모드와 build 모드를 선택

✅ **변경 되돌리기**
- 여러 번 연속으로 실행 가능

```shell
/undo
```

✅ **변경 다시 적용하기**

```shell
/redo
```

✅ **대화 공유하기**
- 현재 대화 링크를 생성하고 클립보드에 복사

```shell
/share
```

# oh-my-opencode

프롬프트로 LLM 에이전트에서 설치 시키기

```shell
Install and configure oh-my-opencode by following the instructions here:
https://raw.githubusercontent.com/code-yeongyu/oh-my-opencode/refs/heads/master/docs/guide/installation.md
```

## Key Features

oh-my-opencode(OmO)는 OpenCode의 플러그인으로, AI 코딩 에이전트를 오케스트레이션하는 도구

### Discipline Agents

모델의 강점을 극대화하는 역할 기반 시스템

AI 코딩 에이전트를 쓰다 보면 이런 경험이 있을 것이다. 간단한 작업인데 에이전트가 쓸데없이 코드베이스를 뒤지거나, 반대로 복잡한 작업인데 계획 없이 덤벼들거나. 문제는 모든 작업을 하나의 모델이 처리하려 한다는 데 있다.

OmO는 작업 종류에 따라 세 가지 핵심 에이전트를 자동으로 배치한다.

**Sisyphus**(claude-opus-4-6 / kimi-k2.5 / glm-5)는 `메인 오케스트레이터`
- 계획을 세우고, 하위 에이전트에 위임하고, 병렬로 실행하고, 무엇보다 중간에 멈추지 않음
- 사용자가 ultrawork 명령을 내리면 Sisyphus는 작업이 완료될 때까지 스스로를 재호출

**Hephaestus**(gpt-5.3-codex)는 `자율적 딥 워커`
- "이 버그 고쳐줘" 같은 목표만 던져주면 스스로 코드베이스를 탐색하고, 연구하고, 실행
- GPT-5.3-codex의 코드 이해 능력이 특히 강력해서 복잡한 버그 추적, 리팩토링 같은 작업에 적합

**Prometheus**(claude-opus-4-6 / kimi-k2.5 / glm-5)는 `전략 플래너`
- 복잡한 작업을 받으면 인터뷰 모드로 전환해서 사용자에게 질문
- "이 기능 어디까지 구현할까요?", "기존 코드 중 건드리지 말아야 할 부분 있나요?" 같은 식
- 범위를 파악하고, 모호성을 제거하고, 검증된 계획을 수립한 뒤 Sisyphus에게 넘김

핵심은 이 세 에이전트가 각자의 모델 강점에 맞게 튜닝되어 있다는 점이다. 사용자는 모델을 수동으로 선택할 필요가 없다. 작업 카테고리만 지정하면(visual-engineering, deep, quick, ultrabrain 등) 하네스가 적절한 모델을 자동으로 선택한다. Claude는 대화와 계획에 강하고, GPT-5.3-codex는 코드 이해에 강하고, Kimi나 GLM은 긴 컨텍스트 처리에 강하다. 이걸 시스템이 알아서 매칭한다.











## Reference

**opencode**
- [opencode](https://opencode.ai/)
- [opencode repo.](https://github.com/anomalyco/opencode)
- [opencode docs.](https://opencode.ai/docs/ko)

**oh my opencode**
- [oh-my-opencode repo.](https://github.com/code-yeongyu/oh-my-opencode)