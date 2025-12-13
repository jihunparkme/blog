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





