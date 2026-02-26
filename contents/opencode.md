# opencode

오픈 소스 AI 코딩 에이전트

## Key Features

✅ 모델 자유도:
- Claude 뿐만 아니라 ChatGPT, OpenAI, GitHub Copilot, Google Gemini, 로컬 모델까지 75개 이상의 LLM 프로바이더를 지원

✅ LSP(Language Server Protocol) 기본 내장:
- LSP 덕분에 에이전트가 IDE 수준으로 코드를 이해
- 프로젝트를 열면 OpenCode가 알아서 적절한 Language Server를 찾아 실행
- 타입 정보, 함수 시그니처, 참조 관계까지 정확하게 파악

✅ 클라이언트/서버 아키텍처:
- PC에서 OpenCode 서버를 돌리고, 모바일에서 원격으로 조작 가능

✅ 멀티 세션과 에이전트 시스템:
- 같은 프로젝트에서 여러 에이전트를 동시에 돌릴 수 있음
- Build 모드와 Plan 모드
  - Build는 전체 접근 권한을 가진 실행 에이전트
  - Plan은 읽기 전용으로 분석과 탐색을 담당
  - 복잡한 작업을 할 때 Plan 모드에서 먼저 전략을 세우고, Build 모드로 넘어가 실행하는 워크플로우가 유용

✅ 기능:
- `/share` 명령으로 대화를 링크로 만들어 공유
- 에이전트가 만든 변경 사항을 `/undo`로 되돌리고, 마음이 바뀌면 `/redo`로 다시 적용 가능

✅ 프라이버시:
- 사용자 코드나 컨텍스트를 저장하지 않음

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