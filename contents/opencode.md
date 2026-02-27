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

이제, TUI에서 `/init` 명령을 실행하여 초기화

## Reference

- [opencode](https://opencode.ai/)
- [opencode repo.](https://github.com/anomalyco/opencode)
- [opencode docs.](https://opencode.ai/docs/ko)

# oh-my-opencode

프롬프트로 LLM 에이전트에서 설치 시키기

```shell
Install and configure oh-my-opencode by following the instructions here:
https://raw.githubusercontent.com/code-yeongyu/oh-my-opencode/refs/heads/master/docs/guide/installation.md
```

## Key Features

oh-my-opencode(OmO)는 OpenCode의 플러그인으로, **AI 코딩 에이전트를 오케스트레이션**하는 도구

### 🤖 Discipline Agents

> 모델의 강점을 극대화하는 역할 기반 시스템

AI 코딩 에이전트를 쓰다 보면 이런 경험이 있을 것이다. 간단한 작업인데 에이전트가 쓸데없이 코드베이스를 뒤지거나, 반대로 복잡한 작업인데 계획 없이 덤벼들거나. 문제는 모든 작업을 하나의 모델이 처리하려 한다는 데 있다.

OmO는 작업 종류에 따라 세 가지 핵심 에이전트를 자동으로 배치한다.

**`Sisyphus`**(claude-opus-4-6 / kimi-k2.5 / glm-5)는 **`메인 오케스트레이터`**
- **계획을 세우고, 하위 에이전트에 위임하고, 병렬로 실행**
- 사용자가 ultrawork 명령을 내리면 Sisyphus는 **작업이 완료될 때까지 스스로를 재호출**

**`Hephaestus`**(gpt-5.3-codex)는 **`자율적 딥 워커`**
- "이 버그 고쳐줘" 같은 **목표만 던져주면 스스로 코드베이스를 탐색하고, 연구하고, 실행**
- GPT-5.3-codex의 코드 이해 능력이 특히 강력해서 복잡한 버그 추적, 리팩토링 같은 작업에 적합

**`Prometheus`**(claude-opus-4-6 / kimi-k2.5 / glm-5)는 **`전략 플래너`**
- 복잡한 작업을 받으면 인터뷰 모드로 전환해서 **사용자에게 질문**
- "이 기능 어디까지 구현할까요?", "기존 코드 중 건드리지 말아야 할 부분 있나요?" 같은 식
- **범위를 파악하고, 모호성을 제거하고, 검증된 계획을 수립**한 뒤 Sisyphus에게 넘김

핵심은 이 세 에이전트가 각자의 모델 강점에 맞게 튜닝되어 있다는 점이다. 사용자는 모델을 수동으로 선택할 필요가 없다. 작업 카테고리만 지정하면(visual-engineering, deep, quick, ultrabrain 등) 하네스가 적절한 모델을 자동으로 선택한다. Claude는 대화와 계획에 강하고, GPT-5.3-codex는 코드 이해에 강하고, Kimi나 GLM은 긴 컨텍스트 처리에 강하다. 이걸 시스템이 알아서 매칭한다.

### ⚡ ultrawork

> 한 단어로 끝까지 가는 루프

AI 에이전트가 작업 중간에 멈추는 이유는 대부분 두 가지
- 컨텍스트 윈도우가 부족하거나
- 다음 단계를 판단하지 못해서

결국, 사용자는 "계속해"를 반복 입력하게 된다.

`ultrawork`(축약: ulw)는 이 문제를 근본적으로 해결한다. 한 단어를 입력하면 모든 에이전트가 활성화되고, 작업이 100% 완료될 때까지 멈추지 않는다.

내부적으로 Ralph Loop와 ulw-loop가 작동한다. **`에이전트가 스스로를 계속 호출하면서 todo 리스트를 소진`** 한다. Todo Enforcer는 에이전트가 유휴 상태에 빠지면 시스템이 개입해서 다시 작업하게 만든다. "이거 끝났으니 다음 뭐 하지?"라고 물어보는 대신, 시스템이 "다음은 이거야"라고 알려준다.

```
Tip. OmO 제작자는 ChatGPT($20) + Kimi($0.99) + GLM($10) 조합을 추천
- 월 31달러로 거의 모든 작업을 커버
- Claude API를 직접 쓰는 것보다 저렴하고, 모델 조합도 더 유연
```

### 🚪 IntentGate

> 리터럴한 오해석을 막는 의도 분석

AI 에이전트는 종종 사용자의 말을 너무 문자 그대로 받아들인다. 
- "이 코드 살펴봐"라고 하면 정말 코드만 읽고 끝낸다. 
- 사용자가 원했던 건 버그를 찾거나 개선점을 제안하는 것이었는데 말이다.

`IntentGate`는 **사용자의 진짜 의도를 먼저 분석한 뒤 분류하고 행동**한다. 
- "이거 살펴봐"가 단순 조사인지, 구현까지 필요한지, 아니면 설명만 원하는 건지 먼저 판단한다. 

이게 중요한 이유는, 잘못된 분류가 전체 작업 흐름을 망치기 때문이다. 
- 구현이 필요한데 조사만 하고 끝나면, 사용자는 다시 프롬프트를 작성해야 한다. 
- 컨텍스트는 낭비되고, 시간도 낭비된다. IntentGate는 이 첫 단계를 확실히 잡는다.

### 🔗 Hashline

> stale-line 에러를 제로로 만드는 편집 도구

AI 코딩 에이전트의 가장 치명적인 문제 중 하나는 편집 도구다. 
- 기존 에이전트들은 대부분 줄 번호 기반 편집을 쓴다. "12번째 줄을 이렇게 바꿔"라는 식. 
- 문제는 파일이 동시에 변경되면 줄 번호가 어긋난다는 것이다. 결과는 stale-line 에러, 코드 손상, 작업 실패다.

Hash-Anchored Edit Tool(`Hashline`)은 이 문제를 **`콘텐츠 해시로 해결`** 한다. (oh-my-pi에서 영감을 받아 구현)

동작 방식: **`에이전트가 파일을 읽으면 각 줄에 해시 태그`** 가 붙는다.

```javascript
11#VK| function hello() {
12#PM|   console.log("world");
13#QR| }
```

에이전트가 편집을 시도할 때, 시스템은 해시를 검증
- 11번째 줄의 해시가 여전히 VK인지 확인
- 만약 다른 에이전트가 이미 그 줄을 수정했다면 해시가 달라지고, 편집은 거부
- 에이전트는 파일을 다시 읽고, 최신 해시를 가져와서 재시도

Grok Code Fast 1 벤치마크에서 Hashline 적용 전 성공률 6.7% -> 적용 후 68.3% 10배 이상 향상

### 🛠️ LSP + AST-Grep

> IDE 수준 정밀도

대부분의 AI 에이전트는 코드를 텍스트로 취급
- "이 함수 이름 바꿔"라고 하면 정규식으로 검색해서 치환
- 문제는 같은 이름의 다른 함수까지 바뀐다는 것
- 주석 안의 함수 이름도 바뀐다. 재앙이다.

OmO는 `LSP`(Language Server Protocol)를 내장
- lsp_rename은 IDE처럼 **`심볼의 모든 참조를 추적해서 정확히`** 바꾼다. 
- lsp_goto_definition은 정의로 점프하고, lsp_find_references는 모든 사용처를 찾고, lsp_diagnostics는 에러와 경고를 미리 체크한다. 
- 빌드 전에 문제를 잡는다.

`AST-Grep`은 25개 언어를 지원하는 **패턴 인식 코드 검색/리라이팅 도구**
- "모든 console.log를 logger.info로 바꿔"라는 작업을 AST 수준에서 처리
- 문자열 안의 console.log는 건드리지 않는다. 주석 안의 것도 건드리지 않고 코드만 정확히 바꾼다.
- 이 두 도구의 조합은 에이전트가 코드를 텍스트가 아니라 구조로 이해하게 만드므로 리팩토링이 안전해진다.




















## Reference

**oh my opencode**
- [oh-my-opencode repo.](https://github.com/code-yeongyu/oh-my-opencode)