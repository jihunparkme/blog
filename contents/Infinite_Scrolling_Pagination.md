# Infinite Scrolling & Pagination

Infinite Scrolling 구현에 참고할 수 있는 정말 좋은 자료가 있다.

- [jQuery Infinite Scrolling Demos](https://www.sitepoint.com/jquery-infinite-scrolling-demos/)

속도 개선을 위해 VanillaJs로 구현된 Demo를 활용하여 구현해보자.

# Demo

**scrollingTest.html**

- 아래 코드를 적용해보면 바로 동작을 확인해볼 수 있다.

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
  <head>
    <meta charset="UTF-8" />
    <title>Insert title here</title>
    <style>
      /* refer to jQuery Infinite Scrolling Demos Site 6th "Infinite Scrolling + Pagination" */
    </style>
  </head>
  <body>
    <div class="main">
      <h1>Infinite Scroll + Pagination Experiment</h1>
      <p>TEST</p>

      <div class="article-list" id="article-list"></div>
      <!-- Pagination List -->
      <ul
        class="article-list__pagination article-list__pagination--inactive"
        id="article-list-pagination"
      ></ul>
    </div>

    <script type="text/javascript" th:inline="javascript">
      /*<![CDATA[*/
      function getPageId(n) {
        return 'article-page-' + n;
      }

      function getDocumentHeight() {
        const body = document.body;
        const html = document.documentElement;

        return Math.max(
          body.scrollHeight,
          body.offsetHeight,
          html.clientHeight,
          html.scrollHeight,
          html.offsetHeight
        );
      }

      function getScrollTop() {
        return window.pageYOffset !== undefined
          ? window.pageYOffset
          : (
              document.documentElement ||
              document.body.parentNode ||
              document.body
            ).scrollTop;
      }

      function getArticleImage() {
        const hash = Math.floor(Math.random() * Number.MAX_SAFE_INTEGER);
        const image = new Image();
        image.className =
          'article-list__item__image article-list__item__image--loading';
        image.src = 'http://api.adorable.io/avatars/250/' + hash;

        image.onload = function () {
          image.classList.remove('article-list__item__image--loading');
        };

        return image;
      }

      function getArticle() {
        const articleImage = getArticleImage();
        const article = document.createElement('article');
        article.className = 'article-list__item';
        article.appendChild(articleImage);

        return article;
      }

      function getArticlePage(page, articlesPerPage = 10) {
        const pageElement = document.createElement('div');
        pageElement.id = getPageId(page);
        pageElement.className = 'article-list__page';

        while (articlesPerPage--) {
          pageElement.appendChild(getArticle());
        }

        return pageElement;
      }

      function addPaginationPage(page) {
        const pageLink = document.createElement('a');
        pageLink.href = '#' + getPageId(page);
        pageLink.innerHTML = page;

        const listItem = document.createElement('li');
        listItem.className = 'article-list__pagination__item';
        listItem.appendChild(pageLink);

        articleListPagination.appendChild(listItem);

        if (page === 2) {
          articleListPagination.classList.remove(
            'article-list__pagination--inactive'
          );
        }
      }

      function fetchPage(page) {
        articleList.appendChild(getArticlePage(page));
      }

      function addPage(page) {
        fetchPage(page); // add articleList data
        addPaginationPage(page); // add articleListPagination data
      }

      /*
       * Main
       */

      const articleList = document.getElementById('article-list');
      const articleListPagination = document.getElementById(
        'article-list-pagination'
      );
      let page = 0;

      // 초기 페이지 로드
      addPage(++page);

      window.onscroll = function () {
        if (getScrollTop() < getDocumentHeight() - window.innerHeight) return;
        // 스크롤이 페이지 하단에 도달할 경우 새 페이지 로드
        addPage(++page);
      };
      /*]]>*/
    </script>
  </body>
</html>
```

# Apply

Demo 코드를 활용해서 적용해보자 !

## View

**infiniteScrolling.html**

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
  <head>
    <meta charset="UTF-8" />
    <title>Insert title here</title>
    <style>
      /* refer to jQuery Infinite Scrolling Demos Site 6th "Infinite Scrolling + Pagination" */
    </style>
  </head>
  <body>
    <div class="main">
      <h1>Infinite Scroll + Pagination Experiment</h1>
      <p>TEST</p>

      <div class="article-list" id="article-list"></div>
      <!-- Pagination List -->
      <ul
        class="article-list__pagination article-list__pagination--inactive"
        id="article-list-pagination"
      ></ul>
    </div>

    <script src="/js/jquery-3.6.0.min.js"></script>
    <script type="text/javascript" th:inline="javascript">
      /*<![CDATA[*/
      const articlesPerPageSize = 10;

      function getPageId(n) {
        return 'article-page-' + n;
      }

      function getDocumentHeight() {
        const body = document.body;
        const html = document.documentElement;

        return Math.max(
          body.scrollHeight,
          body.offsetHeight,
          html.clientHeight,
          html.scrollHeight,
          html.offsetHeight
        );
      }

      function getScrollTop() {
        return window.pageYOffset !== undefined
          ? window.pageYOffset
          : (
              document.documentElement ||
              document.body.parentNode ||
              document.body
            ).scrollTop;
      }

      // result data 로 image tag 생성
      function getArticle(data) {
        const image = new Image();
        image.className =
          'article-list__item__image article-list__item__image--loading';
        image.src = '/gallery/display?id=' + data.mainImageId;
        image.onclick = function () {
          location.href = '/gallery/' + data.id;
        };

        image.onload = function () {
          image.classList.remove('article-list__item__image--loading');
        };

        const article = document.createElement('article');
        article.className = 'article-list__item';
        article.appendChild(image);

        return article;
      }

      // 해당 page 정보를 pagination 리스트에 추가
      function addPaginationPage(page) {
        const pageLink = document.createElement('a');
        pageLink.href = '#' + getPageId(page);
        pageLink.innerHTML = page;

        const listItem = document.createElement('li');
        listItem.className = 'article-list__pagination__item';
        listItem.appendChild(pageLink);

        articleListPagination.appendChild(listItem);

        if (page === 2) {
          articleListPagination.classList.remove(
            'article-list__pagination--inactive'
          );
        }
      }

      // ajax 로 해당 page 데이터 가져와서 뿌려주기
      function addPage(page) {
        $.ajax({
          type: 'GET',
          url: '/gallery/scroll/list',
          data: {
            page: page, // current Page
            size: articlesPerPageSize, // max page size
          },
          dataType: 'json',
        }).done(function (result) {
          if (result.length == 0) {
            return;
          }

          // add articleList data
          const pageElement = document.createElement('div');
          pageElement.id = getPageId(page);
          pageElement.className = 'article-list__page';

          for (var i = 0; i < result.length; i++) {
            pageElement.appendChild(getArticle(result[i]));
          }

          articleList.appendChild(pageElement);

          // add articleListPagination data
          addPaginationPage(page);
        });
      }

      const articleList = document.getElementById('article-list');
      const articleListPagination = document.getElementById(
        'article-list-pagination'
      );
      let page = 0;

      // 초기 페이지
      addPage(++page);

      window.onscroll = function () {
        if (getScrollTop() < getDocumentHeight() - window.innerHeight) return;
        // 스크롤이 페이지 하단에 도달할 경우 새 페이지 로드
        addPage(++page);
      };
      /*]]>*/
    </script>
  </body>
</html>
```

## Controller

**GalleryApiController.java**

```java
@RequiredArgsConstructor
@RestController
public class GalleryApiController {
    //...

	@GetMapping("/gallery/scroll/list")
	public ResponseEntity<List<Gallery>> scrollList(
			@PageableDefault(page = 0, size = 10) Pageable pageable, Model model) {

		// Repository 에 Paging 정보를 요청하기 위해 Pageable 객체 생성 (page, size, 정렬 정보)
		Pageable sortedByIdDesc = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").descending());
		Page<Gallery> galleryListPage = galleryService.getGalleryRepository().findAllByOrderByIdDesc(sortedByIdDesc);

		// List<Entity> 정보를 넘겨주기 위해 ResponseEntity 사용
		return new ResponseEntity<>(galleryListPage.getContent(), HttpStatus.OK);
	}

    //...
}
```

**GalleryController.java**

```java
@Controller
@RequestMapping("/gallery")
public class GalleryController {
	//...

    @GetMapping("/display")
    public ResponseEntity<byte[]> displayImgFile(@RequestParam("id") Long id)throws Exception{

        InputStream in = null;
        ResponseEntity<byte[]> entity = null;
        Optional<Attachments> optAttach = galleryService.getAttachmentsRepository().findById(id);
        if(!optAttach.isPresent()) {
            new RuntimeException("이미지 정보를 찾을 수 없습니다.");
        }

        Attachments attach = optAttach.get();

        try {
            HttpHeaders headers = new HttpHeaders();
            in = new FileInputStream(attach.getFilePath());
            headers.setContentType(FileUtilities.getMediaType(attach.getOrigFileName()));
            headers.add("Content-Disposition", "attachment; filename=\"" + new String(attach.getOrigFileName().getBytes("UTF-8"), "ISO-8859-1")+"\"");

            entity = new ResponseEntity<byte[]>(IOUtils.toByteArray(in), headers, HttpStatus.CREATED);

        } catch(Exception e) {
            e.printStackTrace();
            entity = new ResponseEntity<byte[]>(HttpStatus.BAD_REQUEST);
        } finally {
            in.close();
        }

        return entity;
    }

    //...
}
```

## Repository

**GalleryRepository.java**

```java
public interface GalleryRepository extends JpaRepository<Gallery, Long>, GalleryRepositoryCustom {

	Page<Gallery> findAllByOrderByIdDesc(Pageable pageable);

}
```

# Result

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/scroll_pagination.png 'Result')

# Reference

> <https://wbluke.tistory.com/18>
>
> <https://www.sitepoint.com/jquery-infinite-scrolling-demos/>
>
> [thumbnail](https://wpnewsify.com/tutorials/add-infinite-scrolling/)

# Project Code

[Github](https://github.com/jihunparkme/blog/tree/main/projects/Infinite_Scrolling_Pagination)
