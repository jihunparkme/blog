# n8n News crawler

https://docs.n8n.io/hosting/installation/docker/#prerequisites



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

기사 페이지 가져오기

```http
GET https://www.aitimes.com/news/articleList.html
```

✅ Code in Javascript

기사 내용만 추출하기

```javascript
// 입력 데이터를 'items' 변수에서 가져오는 것은 동일합니다.
const results = [];

// 추출하려는 영역의 시작 태그와 닫는 태그를 정의합니다.
const startTag = '<article id="section-list"';
const endTag = '</article>';

for (const item of $input.all()) {
    // 1. 입력 HTML 문자열을 가져옵니다. (필드 이름에 맞게 수정하세요)
    const htmlString = item.json.data; 
    let extractedContent = null; 

    if (htmlString) {
        // 2. 시작 태그의 위치를 찾습니다. (클래스 등 다른 속성 포함)
        const startIndex = htmlString.indexOf(startTag);

        // 3. 시작 태그를 찾았다면, 실제 내용이 시작되는 위치를 계산합니다.
        if (startIndex !== -1) {
            
            // 3-1. 시작 태그를 찾은 후, 태그가 닫히는 '>' 문자의 위치를 찾습니다.
            // 검색 시작 지점은 startIndex 바로 다음입니다.
            const tagEndIndex = htmlString.indexOf('>', startIndex);

            if (tagEndIndex !== -1) {
                // 실제 내용의 시작 위치: 태그 닫는 괄호 '>' 바로 다음 위치
                const contentStartIndex = tagEndIndex + 1;

                // 3-2. 내용이 끝나는 닫는 태그(</article>)의 위치를 찾습니다.
                // 검색 시작 지점은 태그 닫는 괄호 '>' 바로 다음입니다.
                const contentEndIndex = htmlString.indexOf(endTag, contentStartIndex);

                if (contentEndIndex !== -1) {
                    // 4. 내용 추출: contentStartIndex부터 contentEndIndex 바로 앞까지 자릅니다.
                    extractedContent = htmlString.substring(contentStartIndex, contentEndIndex);
                    
                    // 추출된 내용의 앞뒤 공백과 줄바꿈을 제거합니다.
                    extractedContent = extractedContent.trim();
                }
            }
        }
    }

    // 5. 결과를 새 JSON 객체에 담아 results 배열에 추가합니다.
    results.push({
        json: {
            sectionListContent: extractedContent.replace(/\r\n/g, '\n').replace(/\t/g, ' ')
        }
    });
}

return results;
```



```
다음은 AI 기사 사이트의 HTML 구조입니다.

**태그와 ID 정보:**
최신 기사 목록은 HTML 내에서 다음 선택자에 해당하는 영역에 있습니다.
Selector: 'article#section-list'

**추출할 데이터 필드 및 세부 선택자:**
'article#section-list' 영역 내부에서, 각 기사에 대해 다음 세 가지 정보를 추출해 주세요.

1.  **제목 (Title):** 기사 제목 텍스트.
    * 선택자: '.altlist-subject'
2.  **날짜 (Date):** 기사가 작성된 날짜 정보 텍스트.
    * 선택자: '.altlist-info-item'
3.  **링크 (URL):** 기사 본문으로 연결되는 URL.
    * 선택자: '.altlist-subject' 요소의 'href' 속성 값.

**요청 형식:**
추출된 데이터는 JSON 형식으로 반환해 주세요.

--- 

{{ $json.data }}
```





