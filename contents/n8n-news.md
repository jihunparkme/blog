# n8n News crawler

`n8n`을 활용해서 뉴스 크롤러를 만들어 보려고 해요.

기존에는 직접 코드를 작성해서 만들었는데 이제는 코드 없이도 만들 수 있는 범위가 넓어진 것 같네요.

기본적인 흐름은 `기사 페이지 가져오기` ➜ `기사 내용 추출하기` ➜ `메일 또는 메신저로 전송하기` 정도가 될 것 같아요.

## Run n8n

`n8n`은 도커를 통해 쉽게 호스팅이 가능합니다. 

[Docker Installation](https://docs.n8n.io/hosting/installation/docker/)

```bash
docker volume create n8n_data # Docker 볼륨 생성

docker run -itd \
 --name n8n \
 -p 5678:5678 \
 -e GENERIC_TIMEZONE="Asia/Seoul" \ # n8n 내부에서 사용하는 시간대
 -e TZ="Asia/Seoul" \ # 컨테이너 운영체제 수준의 시간대
 -e N8N_ENFORCE_SETTINGS_FILE_PERMISSIONS=true \
 -e N8N_RUNNERS_ENABLED=true \ # 크플로우 실행 기능을 활성화
 -v n8n_data:/Users/aaron/study/n8n \ # 볼륨 마운트 설정
 docker.n8n.io/n8nio/n8n
 ``` 

> 이제 본격적으로 n8n에서 노드를 하나씩 생성해 크롤러를 만들어 볼게요.

## HTTP Request

기사 페이지 가져오기

```http
GET https://www.aitimes.com/news/articleList.html
```

## Code in Javascript

기사 내용만 추출하기

```javascript
const results = [];

const startTag = '<article id="section-list"';
const endTag = '</article>';

for (const item of $input.all()) {
    const htmlString = item.json.data; 
    let extractedContent = null; 

    // 시작 태그 위치 찾기
    const startIndex = htmlString.indexOf(startTag);
    // 실제 내용이 시작되는 위치 계산
    if (startIndex !== -1) {
        // 태그가 닫히는 '>' 문자 위치 찾기
        const tagEndIndex = htmlString.indexOf('>', startIndex);
        if (tagEndIndex !== -1) {
            // 실제 내용의 시작 위치: 태그 닫는 괄호 '>' 바로 다음 위치
            const contentStartIndex = tagEndIndex + 1;
            // 내용이 끝나는 닫는 태그(</article>) 위치 찾기
            const contentEndIndex = htmlString.indexOf(endTag, contentStartIndex);
            if (contentEndIndex !== -1) {
                // 내용 추출: contentStartIndex부터 contentEndIndex 바로 앞까지
                extractedContent = htmlString.substring(contentStartIndex, contentEndIndex);
                // 추출된 내용의 앞뒤 공백과 줄바꿈을 제거
                extractedContent = extractedContent.trim();
            }
        }
    }

  results.push({
        json: {
            news_list: extractedContent.replace(/\r\n/g, ' ').replace(/\t/g, ' ')
        }
    });
}

return results;
```

## AI Agent

✅ Prompt

```text
**기사 목록이 포함된 태그와 ID 정보:**
Selector: 'article#section-list'

**추출할 데이터 필드 및 세부 선택자:**
'article#section-list' 영역 내부에서, 각 기사에 대해 다음 세 가지 정보를 추출해 주세요.

1.  **제목 (Title):** 기사 제목 텍스트.
    * 선택자: '.altlist-subject'
2.  **날짜 (Date):** 기사가 작성된 날짜 정보 텍스트.
    * 선택자: '.altlist-info-item'
3.  **링크 (URL):** 기사 본문으로 연결되는 URL.
    * 선택자: '.altlist-subject' 요소의 'href' 속성 값.

--- 

{{ $json.news_list }}
```

✅ System Message

```text
당신의 업무는 HTML 포맷의 데이터가 주어지면 기사 목록이 포함된 태그와 ID 정보를 통해 기사 목록 위치를 파악하고, 추출할 데이터 필드 및 세부 선택자를 참고해서 정보를 추출하는 것입니다. 

당신의 모든 답변은 오직 다음 JSON 형식으로만 반환되어야 합니다.
다른 어떠한 설명, 서론, 결론 문장 없이 JSON 배열만을 반환해야 합니다.

[
  {
    "title": "검색 결과의 제목",
    "date": "검색 결과 날짜,
    "url": "정보의 출처 URL"
  },
]
```

✅ Structured Output Parser

```text
[
  {
	"type": "object",
	"properties": {
		"title": {
			"type": "string"
		},
        "date": {
			"type": "string"
		},
        "url": {
			"type": "string"
		}
	}
  }
]
```








