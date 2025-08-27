# genai로 지라 이슈 리포트 생성하기

사내에서 n8n을 활용해서 여러 AI Agent를 만들어 보고 싶었지만, 보안 이슈로 아쉽게 꿈을 펼치지 못 했습니다..ㅎㅎ
<br/>

그래서 다른 방법으로라도 해보리라는 마음으로 Python 기반으로 지라 이슈 리포트를 생성하는 방법을 선택하게 되었고, 그 방법을 공유해 보려고 합니다.

## Intro

👉🏼 **Gemini API용 Python SDK는 `google-generativeai` 패키지 설치**
<br/>

[google-generativeai](https://pypi.org/project/google-generativeai/)
<br/>

```bash
pip install -q -U google-generativeai
```
<br/>

👉🏼 **프로젝트 루트 디렉토리에 `.env` 파일을 만들고 API KEY 저장** 

```bash
GEMINI_API_KEY="..."
JIRA_API_KEY="..."
```

## Code

```python
###################
# 필요 라이브러리 임포트
import textwrap # 텍스트 포매팅
import google.generativeai as genai # Gemini API 사용
from dotenv import load_dotenv # 환경변수 관리
import os
import requests # HTTP 요청

load_dotenv()
gemini_api_key = os.getenv('GEMINI_API_KEY')
jira_api_key = os.getenv('JIRA_API_KEY')

# 사용 가능한 Gemini AI 모델 목록을 반환하는 함수
def get_genai_models():
  result = []
  for m in genai.list_models():
    if 'generateContent' in m.supported_generation_methods:
      result.append(m.name)
  return result

# Gemini AI API를 초기화하고 모델 인스턴스를 반환하는 함수
def init_genai():
  global gemini_api_key
  genai.configure(api_key=gemini_api_key)
  # print('\n'.join(get_genai_models()))
  return genai.GenerativeModel('gemini-2.5-pro')

# 최근 7일간 업데이트된 Jira 이슈들을 가져오는 함수
# - JQL을 사용하여 이슈 검색
# - 각 이슈의 key, summary, description 정보를 수집
# - 최대 100개의 이슈를 조회
def get_jira_issues():
  global jira_api_key
  jql = "project=SETTLEMENT+and+type!=SR+and+updatedDate>=-7d+order+by+updatedDate+desc"
  fields = "key,summary,updated,description"
  url = f"https://jira.company.com/rest/api/latest/search?jql={jql}&fields={fields}&maxResults=100"
  headers = {
    'Authorization': f'Bearer {jira_api_key}',
  }

  jira_issues_response = requests.request("GET", url, headers=headers, data={})
  data = jira_issues_response.json()

  result = []
  for issue in data['issues']:
    result.append({
      'key': issue['key'],
      'summary': issue['fields']['summary'],
      'description': issue['fields'].get('description', 'No description provided')
    })
  return result

############################################################

if __name__ == '__main__':
  model = init_genai()

  jira_issues = get_jira_issues()

  question = f'''
    최근 업데이트된 지라 이슈 목록을 바탕으로 리포트를 작성해줘.
    목록에 제공된 정보에 대한 설명을 해주자면, key는 이슈 번호, summary는 이슈 제목, description은 이슈 설명이야.
  
    지라 이슈 목록:
    {jira_issues}
    
    이 내용들을 바탕으로
    1) 핵심 주제를 분류해서 요약해줘,
    2) 가장 많이 언급된 버그 유형을 분석해줘,
    3) 요청사항의 주요 패턴을 알려줘.
    '''

  response = model.generate_content(question)
  print(response.text)
```

## Execution Results

👉🏼 **실행**

```bash
python3 weekly-report.py
```
<br/>

아래 결과는 실제 실행 결과입니다.
<br/>

---

### 최근 지라 이슈 기반 업무 현황 리포트

제공해주신 최근 업데이트된 지라 이슈 목록을 바탕으로 업무 현황을 분석하고 요약한 리포트입니다.

### 1. 핵심 주제별 업무 분류 및 요약

최근 진행된 이슈들은 크게 **5가지 주제**로 분류할 수 있습니다.

#### 가. 신규 기능 개발 및 시스템 고도화
새로운 비즈니스 요구사항을 반영하고 시스템의 근본적인 성능과 안정성을 개선하는 작업이 활발히 진행 중입니다.
- **주요 내용**:

...

#### 나. 외부 시스템 연동 및 정책 변경 대응
외부 서비스 연동, 제휴사 정책 변경, 그리고 정부 정책에 대응하기 위한 개발이 다수 진행되었습니다.
- **주요 내용**:

...

#### 다. 장애 대응 및 안정성 강화
정산 시스템의 안정적인 운영을 저해하는 장애에 신속하게 대응하고, 재발 방지를 위한 개선 작업을 진행했습니다.

...

#### 라. 데이터 보정 및 재처리
과거 데이터의 오류를 바로잡거나 누락된 데이터를 반영하기 위한 데이터 보정 및 배치 재수행 요청이 꾸준히 발생하고 있습니다.

...

#### 마. 운영 효율화 및 사용자 편의성 개선
운영 업무의 자동화 및 어드민 기능 개선을 통해 수기 작업을 줄이고 업무 효율성을 높이는 작업이 진행 중입니다.

...

---

#### 2. 가장 많이 언급된 버그/장애 유형 분석

지라 이슈 목록에서 '장애'로 명시된 티켓과 그 원인을 분석한 결과, 가장 빈번하게 발생한 장애 유형은 다음과 같습니다.

1.  **데이터 및 설정 누락 (Configuration & Data Integrity Issues)**

...

2.  **외부 시스템 연동 실패 (External Dependency Failures)**

...

---

### 3. 요청사항의 주요 패턴 분석

이슈들의 '요청사항' 섹션을 분석한 결과, 공통적으로 나타나는 주요 패턴은 다음과 같습니다.

1.  **필드 추가 및 데이터 스키마 변경**

...

2.  **데이터 재처리 및 수동 보정**

...

3.  **기존 로직 및 기준 변경**

...

4.  **신규 프로세스 및 기능 도입**

...

## Finish

실행 결과는 슬랙 알림이나 메일로도 전송시킬 수가 있습니다. 
<br/>

이렇게 다양한 방법으로 AI를 활용하여 업무를 효율적으로, 그리고 더 쉽게 할 수 있게 되었습니다.
<br/>

앞으로도 다양한 시도를 해볼 예정입니다.

## Reference

[Gemini API 시작하기](https://ai.google.dev/gemini-api/docs/get-started/tutorial?hl=ko&lang=python)
