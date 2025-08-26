# genai로 지라 이슈 리포트 생성하기

사내에서 n8n을 활용해서 여러 AI Agent를 만들어 보고 싶었지만, 보안 이슈로 아쉽게 꿈을 펼치지 못 했습니다..ㅎㅎ

그래서 다른 방법으로라도 해보리라는 마음으로 Python 기반으로 지라 이슈 리포트를 생성하는 방법을 선택하게 되었고, 그 방법을 공유해 보려고 합니다.

## Intro

👉🏼 **Gemini API용 Python SDK는 `google-generativeai` 패키지 설치**

[google-generativeai](https://pypi.org/project/google-generativeai/)

```bash
pip install -q -U google-generativeai
```

👉🏼 **프로젝트 루트 디렉토리에 `.env` 파일을 만들고 API KEY 저장** 

```bash
GEMINI_API_KEY="..."
JIRA_API_KEY="..."
```
