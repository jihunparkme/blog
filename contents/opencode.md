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

### 🧠 Background Agents

> 실제 팀처럼 분업하는 시스템

하나의 에이전트가 모든 작업을 순차적으로 처리하면 느리다.
- OmO는 **`5개 이상의 전문 에이전트를 병렬로 동시 실행`**
- 메인 컨텍스트 윈도우는 가볍게 유지하고, 백그라운드 에이전트들이 각자 작업을 처리
- 결과는 준비되면 수집

예를 들어, 프론트엔드 기능을 추가하는 작업이라면,
- 에이전트 A는 API 문서를 검색
- 에이전트 B는 기존 컴포넌트를 분석
- 에이전트 C는 테스트 코드를 작성
- 메인 에이전트는 이 결과들을 종합해서 구현

ultrawork와 결합되면, 백그라운드 에이전트들이 병렬로 작업하고, 메인 에이전트는 완료될 때까지 루프를 돌린다. 
- 실제 개발팀이 스프린트를 돌리는 것과 비슷한 느낌

### 📚 Built-in MCPs

> 항상 켜져 있는 정보 파이프라인

보통 AI 에이전트가 인터넷에서 검색하거나 공식 문서를 참조하려면 별도 도구를 설정해야 한다. 그렇다보니 대부분의 사용자는 귀찮아서 안 하게 되는데, 이로 인해 에이전트는 학습 데이터에만 의존하게 되고, 오래된 정보로 코드를 작성한다.

OmO는 **`세 가지 MCP`(Model Context Protocol) `서버를 기본 탑재`**
- `Exa`: 웹 검색. 최신 정보, 블로그 포스트, 기술 문서를 실시간으로 조회
- `Context7`: 공식 문서. Next.js, React, MongoDB 같은 라이브러리의 최신 공식 문서를 참조
- `Grep.app`: GitHub 코드 검색. 100만 개 이상의 오픈소스 프로젝트에서 실제 사용 예시 조회

이 세 개는 에이전트가 바로 쓸 수 있도록 항상 켜져 있다. 

예를 들어, "Next.js 15에서 Server Actions 어떻게 써?"라고 물으면, 에이전트는 수동 설정 없이도 `Context7`에서 공식 문서를 가져오고, `Grep.app`에서 실제 코드 예시를 찾고, `Exa`로 최신 블로그 포스트를 참조

### Skill-Embedded MCPs

> 컨텍스트 윈도우를 절약하는 설계

MCP 서버는 강력하지만 서버가 제공하는 모든 도구와 스키마가 프롬프트에 포함되기 때문에 컨텍스트 윈도우를 잡아먹는다. 10개 MCP 서버를 켜두면 컨텍스트의 절반이 도구 설명으로 채워진다.

`Skill-Embedded MCP`는 각 스킬(예: Playwright, Git Master)이 자체 MCP 서버를 탑재하여 **`필요할 때만 로드되고, 작업이 끝나면 해제`**된다. 따라서, **`컨텍스트 윈도우는 실제 작업에만`** 쓰인다.

예를 들어, 브라우저 자동화 작업이 필요하면 Playwright 스킬이 로드되고, Playwright MCP 서버가 활성화된다. 작업이 끝나면 서버는 해제되고, 컨텍스트에서 사라진다. 다음 작업이 Git 관련이면 Git Master 스킬이 로드되고, Git MCP 서버가 활성화된다.

이 방식은 토큰 효율성과 에이전트 성능을 동시에 높인다. 필요한 도구만 로드되니 컨텍스트가 깨끗하고, 에이전트는 혼란스러워하지 않는다.

### Claude Code 호환성

기존에 Claude Code에서 설정해둔 훅, 커맨드, 스킬, MCP, 플러그인을 모두 그대로 쓸 수 있다. 설정 파일도 그대로다. OmO는 **`Claude Code의 확장이지, 대체가 아니다`**.

이 덕분에 마이그레이션 비용이 없다. 오늘 OmO를 설치하고 ultrawork 명령어만 익히면, 내일부터 바로 생산성이 올라간다.

### 📋 Prometheus Planner

> "프롬프트 앤 프레이"를 넘어서

복잡한 작업을 AI 에이전트에게 던지는 건 도박이다. 사용자가 모든 걸 명확히 설명했다고 생각해도, 에이전트는 예상 밖의 방향으로 간다. 범위를 오해하거나, 제약 사항을 무시하거나, 아예 다른 걸 구현한다.

Prometheus Planner(/start-work)는 **`복잡한 작업을 받으면 인터뷰 모드로 전환`**
- "이 기능 어디까지 구현할까요?"  
- "기존 코드 중 건드리지 말아야 할 부분 있나요?"  
- "테스트는 어느 수준까지 작성할까요?"

사용자가 답변하면, Prometheus는 범위를 파악하고 모호성을 제거하고 상세 계획을 수립한다. 이 계획은 검증되고, Sisyphus에게 넘어가서 실행된다.
"프롬프트 앤 프레이" 방식은 이제 옛날 얘기다. Prometheus는 작업 전에 정렬(alignment)을 먼저 한다.















## Reference

**oh my opencode**
- [oh-my-opencode repo.](https://github.com/code-yeongyu/oh-my-opencode)