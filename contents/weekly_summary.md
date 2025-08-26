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




