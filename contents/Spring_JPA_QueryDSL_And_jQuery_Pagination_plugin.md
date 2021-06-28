# Spring QueryDSL & Pagination을 활용

## jQuery Pagination plugin 적용

**참고**

> [[Spring] pagination, 3분만에 paging 만들기](https://data-make.tistory.com/670)

## Add Dependency

- `querydsl-jpa` / `querydsl-apt`
- QueryDSL에서 QClass를 사용하기 위해 plugin 등록이 필요하다.

**pom.xml**

```xml
<!-- dependency -->
<!-- https://mvnrepository.com/artifact/com.querydsl/querydsl-jpa -->
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-jpa</artifactId>
</dependency>
<!-- https://mvnrepository.com/artifact/com.querydsl/querydsl-apt -->
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-apt</artifactId>
</dependency>

<!-- plugin -->
<plugin>
    <groupId>com.mysema.maven</groupId>
    <artifactId>apt-maven-plugin</artifactId>
    <version>1.1.3</version>
    <executions>
        <execution>
            <goals>
                <goal>process</goal>
            </goals>
            <configuration>
                <outputDirectory>target/generated-sources/java</outputDirectory>
                <processor>com.querydsl.apt.jpa.JPAAnnotationProcessor</processor>
            </configuration>
        </execution>
    </executions>
</plugin>
<!-- ... -->
```

## Set properties

- Spring Boot Pageable의 start page index는 0부터 시작한다.
  - 이 경우, page parameter 사용에 다소 혼란(?)이 있어 1-based 로 설정하고자 한다.

**application.properties**

```properties
spring.data.web.pageable.one-indexed-parameters=true
```

- 그밖에 properties default 옵션도 참고해보자. ([reference](https://stackoverflow.com/a/49575492))

```properties
# Default page size.
spring.data.web.pageable.default-page-size=20

# Maximum page size to be accepted.
spring.data.web.pageable.max-page-size=2000

# Whether to expose and assume 1-based page number indexes.
spring.data.web.pageable.one-indexed-parameters=false

# Page index parameter name.
spring.data.web.pageable.page-parameter=page

# General prefix to be prepended to the page number and page size parameters.
spring.data.web.pageable.prefix=

# Delimiter to be used between the qualifier and the actual page number and size properties.
spring.data.web.pageable.qualifier-delimiter=_

# Page size parameter name.
spring.data.web.pageable.size-parameter=size

# Sort parameter name.
spring.data.web.sort.sort-parameter=sort
```

## View

`thymeleaf`

- View에서 핵심은 `page 버튼` or `검색 버튼` 클릭 시 서버에 page 정보를 함께 전달해 준다는 것이다.

**listGallery.html**

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
  <head>
    <meta charset="UTF-8" />
    <title>리스트</title>
    <link rel="stylesheet" href="/css/bootstrap.min.css" />
    <link rel="stylesheet" href="/css/common.css" />
    <style>
      body {
        padding-top: 70px;
        padding-bottom: 30px;
      }
    </style>
  </head>
  <body>
    <article>
      <div class="container">
        <div class="table-responsive">
          <div th:if="${galleryListPage.totalElements} != 0">
            Total : <span th:text="${galleryListPage.totalElements}"></span>
          </div>
          <form name="form" id="form" th:action="@{/gallery/list}" method="get">
            <input type="hidden" id="page" name="page" value="1" />
            <div class="form-group">
              <div style="float: left;">
                <select
                  style="height:30px"
                  name="searchType"
                  id="searchType"
                  class="select"
                >
                  <option value="ALL" selected>전체</option>
                  <option value="TITLE">제목</option>
                  <option value="WRITER">작성자</option>
                </select>
              </div>
              <input
                type="text"
                style="float: left;"
                name="searchValue"
                id="searchValue"
                class="text"
                value=""
                th:value="${param.searchValue}"
              />
              <!-- 서버에 Page 정보도 함께 전달 -->
              <button
                type="button"
                class="btn btn-sm btn-primary"
                id="btnSearch"
              >
                검색
              </button>
              <button
                type="button"
                class="btn btn-sm btn-secondary"
                id="btnReset"
              >
                초기화
              </button>
            </div>
            <div class="form-group">
              <strong>기간</strong>
              <label class="radio"
                ><input
                  type="radio"
                  name="period"
                  id="periodAll"
                  value="ALL"
                  checked
                /><span>전체</span></label
              >
              <label class="radio"
                ><input
                  type="radio"
                  name="period"
                  id="periodCustom"
                  value="CUSTOM"
                /><span>입력</span></label
              >
              <div class="input_period">
                <div>
                  <input
                    type="date"
                    style="float: left;"
                    id="fromDate"
                    name="fromDate"
                    class="datepicker"
                    value=""
                  />
                </div>
                <span style="float: left; padding: 0 5px 0 5px">~</span>
                <div>
                  <input
                    type="date"
                    style="float: left;"
                    id="toDate"
                    name="toDate"
                    class="datepicker"
                    value=""
                  />
                </div>
              </div>
            </div>
          </form>
          <table class="table table-striped table-sm">
            <colgroup>
              <col style="width:5%;" />
              <col style="width:auto;" />
              <col style="width:15%;" />
              <col style="width:10%;" />
              <col style="width:10%;" />
            </colgroup>
            <thead>
              <tr>
                <th>NO</th>
                <th>글제목</th>
                <th>작성자</th>
                <th>조회수</th>
                <th>작성일</th>
              </tr>
            </thead>
            <tbody>
              <tr th:if="${galleryListPage.totalElements} == 0">
                <td colspan="5" align="center">데이터가 없습니다.</td>
              </tr>
              <tr
                th:each="gallery, status : ${galleryListPage.content}"
                th:unless="${#lists.isEmpty(galleryListPage.content)}"
              >
                <td th:text="${gallery.id}"></td>
                <td>
                  <a
                    th:href="@{'/gallery/' + ${gallery.id}}"
                    th:text="${gallery.title}"
                  ></a>
                </td>
                <td th:text="${gallery.memberName}"></td>
                <td th:text="0"></td>
                <td
                  th:text="${#temporals.format(gallery.createdDateTime, 'yyyy-MM-dd HH:mm')}"
                ></td>
              </tr>
            </tbody>
          </table>
        </div>
        <div>
          <button type="button" class="btn btn-sm btn-primary" id="btnWrite">
            글쓰기
          </button>
        </div>
        <!-- pagination -->
        <div class="paging-div">
          <ul class="pagination" id="pagination"></ul>
        </div>
      </div>
    </article>

    <script src="/js/jquery-3.6.0.min.js"></script>
    <script src="/js/bootstrap.min.js"></script>
    <script src="/js/jquery.twbsPagination.js"></script>

    <script type="text/javascript" th:inline="javascript">
      /*<![CDATA[*/
      $(function() {

      	// 키워드 입력 후 Enter 처리
      	$("#searchValue").on("keyup", function(e) {
      		if (e.keyCode == 13) { searchGalleryList(1); }
      	});

      	// 기간 입력 클릭
      	$("input[name=period]").change(function() {
      		if (this.id == "periodCustom") {
      			$(".input_period").addClass("on");
      		} else {
      			$(".input_period").removeClass("on");
      			$("#fromDate").val("");
      			$("#toDate").val("");
      		}
      	});

      	// 글쓰기 버튼 클릭
      	$("#btnWrite").click(function(e) {
      		location.href = "/gallery/edit";
      	});

      	// 검색 버튼 클릭
      	$("#btnSearch").click(function() {
      		searchGalleryList(1);
      	});

      	// 검색 초기화 버튼 클릭
      	$("#btnReset").click(function() {

      		$("#searchType").val("ALL");
      		$("#searchValue").val("");

      		$("input[name=period][value=ALL]").prop('checked', 'checked');
      		$("#fromDate").val("");
      		$("#toDate").val("");

      		searchGalleryList(1);
      	});

      	/**
      	 * Set Pagination
      	 */
      	window.pagObj = $('#pagination').twbsPagination({
              totalPages: [[${galleryListPage.totalPages}]],
              startPage: parseInt([[${galleryListPage.number}]] + 1),
              visiblePages: 10,
              prev: "‹",
      		next: "›",
      		first: '«',
      		last: '»',
              onPageClick: function (event, page) {
                  console.info("current page : " + page);
              }
          }).on('page', function (event, page) {
              searchGalleryList(page);
          });

      	init();
      });

      function searchGalleryList(page) {

      	if (page == undefined) {
      		page = 1;
      	}

      	if ($("#searchType").val() !=  "ALL" && $("#searchString").val() == "") {
      		alert("'검색어'를 입력해주세요.");
      		$("#searchValue").focus();
      		return;
      	}

      	$("#page").val(page); // page 정보 저장 후
      	$("#form").submit(); // form data 와 함께 전송
      }

      function init() {

      	if ([[${param.searchType}]] == null) {
      		$("#searchType").val("ALL");
      	} else {
      		$("#searchType").val([[${param.searchType}]]);
      	}

      	$("input[name=period][value=" + [[${param.period}]] + "]").prop('checked', 'checked');
      	if($("input[name=period]:checked").val() == "CUSTOM") {
      		$(".input_period").addClass("on");
      		$("#fromDate").val([[${param.fromDate}]]);
      		$("#toDate").val([[${param.toDate}]]);
      	}
      }
      /*]]>*/
    </script>
  </body>
</html>
```

## Controller

- pageable 정보를 form data와 함께 전달 받는다.

**GalleryController.java**

```java
@GetMapping("/list")
public String list(
    @RequestParam(value = "searchType", required = false, defaultValue = "ALL") String searchType,
    @RequestParam(value = "searchValue", required = false, defaultValue = "") String searchValue,
    @RequestParam(value = "period", required = false, defaultValue = "ALL") String period,
    // String으로 전달된 날짜 데이터를 LocalDate Type으로 변환 시 발생하는 오류를 막기 위해 DateTimeFormat 사용
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDate", required = false) LocalDate toDate,
    @PageableDefault(page = 0, size = 10) Pageable pageable,
    Model model) {

    // 검색에 필요한 파라미터 세팅
    GalleryParam galleryParam = setGalleryParameter(searchType, searchValue, period, fromDate, toDate);

    /**
     * Pageable 정보가 담긴 List
     * content : 해당 페이지에 담긴 Data List
     * pageable
     *		page : 현재 페이지
     *		size : 한 페이지에 담기는 데이터 크기
     *		sort : 정렬 정보
     * total : 총 데이터 개수
     */
    Page<Gallery> galleryListPage = galleryService.getGalleryRepository().findAllGallery(galleryParam, pageable);

    model.addAttribute("galleryListPage", galleryListPage);

    return "gallery/listGallery";
}

private GalleryParam setGalleryParameter(String searchType, String searchValue,
			String period, LocalDate fromDate, LocalDate toDate) {

    GalleryParam galleryParam = new GalleryParam();

    galleryParam.setSearchType(searchType);
    galleryParam.setSearchValue(searchValue);
    galleryParam.setPeriod(period);
    if (fromDate != null && toDate != null) {
        galleryParam.setFromDate(fromDate.atStartOfDay());
        galleryParam.setToDate(toDate.plusDays(1L).atStartOfDay());
    }

    return galleryParam;
}
```

## Repository

**GalleryRepository.java**

```java
public interface GalleryRepository extends JpaRepository<Gallery, Long>, GalleryRepositoryCustom {
}
```

## RepositoryCustom

**GalleryRepositoryCustom.java**

```java
public interface GalleryRepositoryCustom {

	public Page<Gallery> findAllGallery(GalleryParam param, Pageable pageable);

	public List<Gallery> findAllGallery(GalleryParam param);
}
```

**GalleryRepositoryCustomImpl.java**

```java
public class GalleryRepositoryCustomImpl extends QuerydslRepositorySupport implements GalleryRepositoryCustom {

	public GalleryRepositoryCustomImpl() {
		super(Gallery.class);
	}

	@Data
	public static class GalleryParam {
		private String searchType;
		private String searchValue;
		private String period;
		private LocalDateTime fromDate;
		private LocalDateTime toDate;
	}

	@Override
	public Page<Gallery> findAllGallery(GalleryParam param, Pageable pageable) {

		JPQLQuery<Gallery> query = findAllGalleryQuery(param, pageable);

        /*
         * paging 처리를 위한 fetchResults
         * 조회 리스트 + 전체 개수를 포함한 QueryResults
     	 */
		QueryResults<Gallery> queryResults = query.fetchResults();

		return new PageImpl<Gallery>(queryResults.getResults(), pageable, queryResults.getTotal());
	}

	@Override
	public List<Gallery> findAllGallery(GalleryParam param) {

		JPQLQuery<Gallery> query = findAllGalleryQuery(param, null);

         /*
         * QueryResults에서 조회 리스트만 return
         */
		return query.fetchResults().getResults();
	}

    /**
     * JPQL Query 생성 함수
     */
	private JPQLQuery<Gallery> findAllGalleryQuery(GalleryParam param, Pageable pageable) {

		QGallery gallery = QGallery.gallery;

		/*
         * From Clause
         */
		JPQLQuery<Gallery> query = from(gallery);

		BooleanBuilder bb = new BooleanBuilder();

		/*
         * Where Clause
         */
		// period
        if (param.getFromDate() != null && param.getToDate() != null) {
            bb.and(gallery.createdDateTime.between(param.getFromDate(), param.getToDate()));
        }

		// searchType & searchValue
        switch (param.getSearchType()) {
            case "ALL" :
                bb.and(gallery.title.contains(param.getSearchValue()).
                       or(gallery.memberName.contains(param.getSearchValue())));
                break;
            case "TITLE" :
                bb.and(gallery.title.contains(param.getSearchValue()));
                break;
            case "WRITER" :
                bb.and(gallery.memberName.contains(param.getSearchValue()));
                break;
        }

		// paging
		if (pageable != null) {
			query.limit(pageable.getPageSize());
			query.offset(pageable.getOffset());
		}

		/*
         * OrderBy Clause
         */
		OrderSpecifier<Long> orderId = gallery.id.desc();

		return query.distinct().where(bb).orderBy(orderId);
	}
}
```

---

## Reference

> [Spring 블로그 만들기](https://freehoon.tistory.com/category/%EA%B0%9C%EB%B0%9C/Spring%20%20%EB%B8%94%EB%A1%9C%EA%B7%B8%20%EB%A7%8C%EB%93%A4%EA%B8%B0)
