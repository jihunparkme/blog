# Wikipedia 국/영문 데이터 수집/분석

자연어 처리를 위해 wikipedia 데이터를 활용해보자.

## Download Wiki dump file

- <https://dumps.wikimedia.org/kowiki/latest/>
  - <https://dumps.wikimedia.org/kowiki/latest/kowiki-latest-pages-articles.xml.bz2>
  - 2021/07 기준 데이터 : 1208126 건
- https://dumps.wikimedia.org/enwiki/latest/
  - https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2
  - 2021/07 기준 데이터 : 15839021 건
- pages-articles.xml.bz2 파일은 일반 문서의 최신 버전을 의미

> [Wiki database file information](https://ko.wikipedia.org/wiki/%EC%9C%84%ED%82%A4%EB%B0%B1%EA%B3%BC:%EB%8D%B0%EC%9D%B4%ED%84%B0%EB%B2%A0%EC%9D%B4%EC%8A%A4_%EB%8B%A4%EC%9A%B4%EB%A1%9C%EB%93%9C)

---

## Make Wikipedia Database Dump to Cleans Text

- [wikiextractor](https://github.com/attardi/wikiextractor)
- `python -m wikiextractor.WikiExtractor enwiki-latest-pages-articles.xml.bz2 -o result --json -b 50M`
  - -o [] : cleans text 로 변환된 파일을 저장할 경로
  - --json : 출력 파일 형태 (default - xml)
  - -b [] : 파일 한 개의 최대 크기 (출력이 []를 초과라면 새 파일 생성)
  - [README.md](https://github.com/attardi/wikiextractor#usage) 에 더 많은 옵션에 대한 설명이 있다.

**Before** (example)

```xml
<page>
	<title>상태 공간 탐색</title>
	<ns>0</ns>
	<id>1854759</id>
	<revision>
		<id>23755344</id>
		<parentid>20112292</parentid>
		<timestamp>2019-02-22T16:59:18Z</timestamp>
		<contributor>
			<username>Priviet</username>
			<id>211243</id>
		</contributor>
		<minor />
		<comment>/* top */{{독자 연구}} 틀 제거. 장기간 논의 없음. using [[Project:AWB|AWB]]
		</comment>
		<model>wikitext</model>
		<format>text/x-wiki</format>
		<text bytes="453" xml:space="preserve">{{출처 필요|날짜=2017-11-08}}
'''상태 공간 탐색'''은 목표 상태를 향해 찾아가는 모든 과정을 상태 공간, 즉 문제 해결 과정에서 나타나는 각 국면(局面)을 각각의 상태로 잡을 때 이것들의 집합으로 정의되는 공간으로 보고, 이 공간들의 최적의 집합을 찾아가는 방법이다.

{{토막글|컴퓨터 과학}}

[[분류:그래프 알고리즘]]
[[분류:검색 알고리즘]]</text>
		<sha1>kohoe66iq736q5886h0wsfa47t8wqrf</sha1>
	</revision>
</page>
```

**After** (example)

```xml
<doc id="1854759" url="?curid=1854759" title="상태 공간 탐색">
상태 공간 탐색

상태 공간 탐색은 목표 상태를 향해 찾아가는 모든 과정을 상태 공간, 즉 문제 해결 과정에서 나타나는 각 국면(局面)을 각각의 상태로 잡을 때 이것들의 집합으로 정의되는 공간으로 보고, 이 공간들의 최적의 집합을 찾아가는 방법이다.
</doc>
```

---

## PageId

wikipedia 의 각 데이터에는 pageId 로 불리우는 고유의 ID 가 존재한다.

이 PageId 로 다양한 시도를 해볼 수 있다.

## wikibase_item

wikibase_item 는 Q로 시작하는 고유 번호로 Item 의 ID를 의미한다.

언어가 다른 같은 Item 을 찾고자 할 떄는 wikibase_item 을 활용하면 된다. (ex. 사과 ​/ apple / 沙果)

---

## Wiki API

다른 언어는 PathVariable 값만 변경해주면 된다.

- Englist -> en
- Korean -> ko

**MediaWiki API** (pageid)

- https://en.wikipedia.org/w/api.php?action=query&prop=info&pageids=18630637&inprop=url

- pageId 로 해당 문서의 정보를 확인할 수 있는 API

```json
{
  "batchcomplete": "",
  "query": {
    "pages": {
      "18630637": {
        "pageid": 18630637,
        "ns": 0,
        "title": "Translation",
        "contentmodel": "wikitext",
        "pagelanguage": "en",
        "pagelanguagehtmlcode": "en",
        "pagelanguagedir": "ltr",
        "touched": "2021-08-08T17:25:36Z",
        "lastrevid": 1037779554,
        "length": 153771,
        "fullurl": "https://en.wikipedia.org/wiki/Translation",
        "editurl": "https://en.wikipedia.org/w/index.php?title=Translation&action=edit",
        "canonicalurl": "https://en.wikipedia.org/wiki/Translation"
      }
    }
  }
}
```

**MediaWiki API** (title)

- https://en.wikipedia.org/w/api.php?action=query&prop=pageprops&titles=Translation&format=xml

- 문서의 title 로 해당 문서의 정보를 확인할 수 있는 API

- json format 설정 가능

```xml
<api batchcomplete="">
    <query>
        <pages>
            <page _idx="18630637" pageid="18630637" ns="0" title="Translation">
                <pageprops page_image_free="Charles_V_ordonnant_la_traduction_d'Aristote_copy.jpg" wikibase-shortdesc="Transfer of the meaning of something in one language into another" wikibase_item="Q7553"/>
            </page>
        </pages>
    </query>
</api>
```

**Search by pageID**

- https://en.wikipedia.org/wiki?curid=18630637
- pageId 로 Wiki document 링크 접근

**Search by wikibase_item**

- https://www.wikidata.org/wiki/Q395
- wikibase_item 번호는 도구/위키데이터 항목(Tools/Wikidata item) 에서 확인하거나 상단 API 에서 얻을 수 있다.
