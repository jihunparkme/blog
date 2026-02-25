# opencode

오픈 소스 AI 코딩 에이전트

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














## Reference

**opencode**
- [opencode](https://opencode.ai/)
- [opencode repo.](https://github.com/anomalyco/opencode)
- [opencode docs.](https://opencode.ai/docs/ko)

**oh my opencode**
- [oh-my-opencode repo.](https://github.com/code-yeongyu/oh-my-opencode)