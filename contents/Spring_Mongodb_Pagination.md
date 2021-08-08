# Spring MongoTemplate & Pagination

기존 SQL Server / MySQL이 아닌 MongoDB를 활용하여 게시판을 만들게 되었다.

관계형 Database Model과 다른 Document 지향 Database Model로 쿼리도 생소하고.. Spring에서의 사용도 유사하면서 다르다보니 정리해두면 언젠가 쓸모가 있을 듯 하다.

기본적인 내용은 지난번에 작성한 [QueryDSL과 Pagination을 활용하여 리스트 검색 기능](https://data-make.tistory.com/671)과 유사하다.

다만, 쿼리 생성 부분이 약간(?) 많이 달라서 정리를 한 번 해보자!

MongoDB Dependency 관련 설명은 생략하고, 쿼리 생성 핵심 부분만 훑어보자.

## Repository

```java
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<Post, Long> {

	List<Post> findAllByOrderByIdDesc();
}
```

## Service

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;

// ...
public class PostServiceImpl {

	private final MongoTemplate mongoTemplate;
	private final PostRepository postRepository;

	@Transactional
	public Long save(Post post) {

		LocalDateTime today = LocalDateTime.now();
		// 신규 등록
		if (post.getId() == null) {
             // MongoDB는 id auto increment가 생각보다 귀찮(?)아서 그냥 마지막 게시물 Id를 받아왔다..
             // 더 좋은 방법이 있다면 알려주세yo..
			List<Post> lastPost = postRepository.findAllByOrderByIdDesc();
			if (lastPost.isEmpty()) {
				post.setId(1L);
			} else {
				post.setId(postRepository.findAllByOrderByIdDesc().get(0).getId() + 1);
			}

			post.setCreateDate(today);
         // 수정
		} else {
			Post origPost = postRepository.findById(post.getId()).get();

			post.setHit(origPost.getHit());
			post.setCreateDate(origPost.getCreateDate());
			post.setEditDate(today);
		}

		return mongoTemplate.save(post).getId();
	}

	public Page<Post> findPost(String searchType, String searchString, String category, String period,
			LocalDate fromDate, LocalDate toDate, Pageable pageable) {

		Query query = new Query();

		// 기간 검색을 선택했을 경우
		if ("CUSTOM".equals(period)) {
			String startDate = fromDate.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			String endDate = toDate.plusDays(1L).atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

			LocalDateTime s = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			LocalDateTime e = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

			query.addCriteria(Criteria.where("create_date").gte(s).lt(e));
		}

		// 검색 타입을 선택했을 경우
		switch(searchType) {
		case "ALL":
			query.addCriteria(Criteria.where("title").regex(".*" + searchString + ".*").
					orOperator(Criteria.where("writer").regex(".*" + searchString + ".*")));
			break;
		case "TITLE":
			query.addCriteria(Criteria.where("title").regex(".*" + searchString + ".*"));
			break;
		case "WRITER":
			query.addCriteria(Criteria.where("writer").regex(".*" + searchString + ".*"));
			break;
		}

		// 카테고리를 선택했을 경우
		switch(category) {
		case "ALL":
			break;
		case "NOTICE":
			query.addCriteria(Criteria.where("category").is("공지사항"));
			break;
		case "QNA":
			query.addCriteria(Criteria.where("category").is("질문"));
			break;
		}

		// Sort
		query.with(Sort.by(Sort.Direction.DESC, "_id"));

		// pagination
		if (pageable != null) {
			query.with(pageable);
		}

		List<Post> postList = mongoTemplate.find(query, Post.class);

		return PageableExecutionUtils.getPage(
				postList,
                pageable,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Post.class));
	}
}

```

- 기간 검색(between) : `query.addCriteria(Criteria.where("create_date").gte(s).lt(e));`
- 검색 키워드(contains) : `query.addCriteria(Criteria.where("title").regex(".*" + searchString + ".*"));`
- radio box(equals) : `query.addCriteria(Criteria.where("category").is("공지사항"));`
- Sort : `query.with(Sort.by(Sort.Direction.DESC, "_id"));`
- pagination : `query.with(pageable);`

## Reference

- [A Guide to Queries in Spring Data MongoDB](https://www.baeldung.com/queries-in-spring-data-mongodb)
- [Pagination with mongoTemplate](https://stackoverflow.com/questions/29030542/pagination-with-mongotemplate)
- [Spring Data mongo case insensitive like query](https://stackoverflow.com/questions/41746370/spring-data-mongo-case-insensitive-like-query)
