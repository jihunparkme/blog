# Spring pagination

3분 Pagination

빠르고 간단하고 쉽게 Spring Pagination 구현하기 🎉🎊

3분만에 Spring Pagination 을 구현하고 싶다면 `jQuery Pagination plugin`을 활용해보자.

홈페이지를 참고하면 Demo Code, Options explanation 등을 참고할 수 있다.

---

## Download plugin

Official Homepage : [jQuery Pagination plugin](https://esimakin.github.io/twbs-pagination/#page-11)

Javascript Code : [jquery.twbsPagination.js](https://github.com/josecebe/twbs-pagination/blob/master/jquery.twbsPagination.js)

## Import File

- jQuery, Bootstrap 기반 plugin 으로 해당 Library 도 필요

```html
<script src="/js/jquery.twbsPagination.js"></script>

<!-- jQuery -->
<script src="/js/jquery-3.6.0.min.js"></script>

<!-- Bootstrap -->
<script src="/js/bootstrap.min.js"></script>
<link rel="stylesheet" href="/css/bootstrap.min.css" />
```

## HTML Code

- 스타일을 적용하기 위해 div로 감싸주었지만 사실 ul 태그만 있으면 된다. WoW !

```html
<div class="paging-div">
  <ul class="pagination" id="pagination"></ul>
</div>
```

## CSS Code

- pagination 가운데 정렬을 위해 스타일 추가

```css
.paging-div {
  padding: 15px 0 5px 10px;
  display: table;
  margin-left: auto;
  margin-right: auto;
  text-align: center;
}
```

## JavaScript Code

Pagination plugin을 적용하는 코드이다.

```javascript
    window.pagObj = $('#pagination').twbsPagination({
        totalPages: [[${dataListPage.totalPages}]], // 전체 페이지
        startPage: parseInt([[${dataListPage.number}]] + 1), // 시작(현재) 페이지
        visiblePages: 10, // 최대로 보여줄 페이지
        prev: "‹", // Previous Button Label
        next: "›", // Next Button Label
        first: '«', // First Button Label
        last: '»', // Last Button Label
        onPageClick: function (event, page) { // Page Click event
            console.info("current page : " + page);
        }
    }).on('page', function (event, page) {
        searchDataList(page);
    });
```

- Pagination Options는 아래 링크를 참고하자.
  - [https://esimakin.github.io/twbs-pagination/#options-and-events](https://esimakin.github.io/twbs-pagination/#options-and-events)

---

## Result

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/pagination.jpg 'Result')

---

## Spring JPA QueryDSL & jQuery Pagination plugin

- Result Image 와 같이 `jQuery Pagination plugin`과 `Spring JPA QueryDSL`을 활용하여

- 검색, 페이징 기능이 있는 게시판 리스트 만들기는 아래 링크 참고해보자.

> [Spring QueryDSL과 Pagination을 활용하여 리스트 검색 기능 만들기](https://data-make.tistory.com/671)
