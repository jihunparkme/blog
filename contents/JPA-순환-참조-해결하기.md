# [JPA] JSON 직렬화 순환 참조 해결하기

**순환 참조.**

JPA에서 양방향으로 연결된 엔티티를 JSON 형태로 직렬화하는 과정에서, 서로의 정보를 계속 순환하며 참조하여 `StackOverflowError` 를 발생시키는 현상

.

**직렬화.**

객체/데이터를 바이트 형태로 변환하여 네트워크를 통해 송수할 수 있도록 만드는 것

## Situation

- `Product` 에서 `ProductCategory`를 조회할 수도 있고, `ProductCategory` 에서 `Product` 도 조회할 수 있어야 한다.

- Controller 에서 `Product` 목록을 JSON 형태 직렬화하여 반환해야 한다.

```java
@Getter
@NoArgsConstructor
@Entity
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_category_id", nullable = false)
    private ProductCategory productCategory;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 20000)
    private String contents;

    @Column(nullable = false, columnDefinition = "BIGINT default 0")
    private Long hits;

    @Enumerated(EnumType.STRING)
    @Column(length = 1, nullable = false, columnDefinition = "BIGINT default N")
    private BooleanFormatType deleteYn;

    @Column(nullable = false)
    private Long userId;

    private LocalDateTime deletedDateTime;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<ProductUploadFile> productUploadFiles = new LinkedHashSet<>();
}
```

```java
@ResponseBody
@GetMapping("/scroll")
public ResponseEntity<List<Product>> scrollList(
        @PageableDefault(page = 0, size = 10) Pageable pageable,
        Model model) {

    Page<Product> productListPage = categoryService.findAllSortByIdDescPaging(pageable.getPageNumber(), pageable.getPageSize());

    return new ResponseEntity<>(productListPage.getContent(), HttpStatus.OK);
}
```

## Problem

- 매핑된 데이터를 `FetchType.LAZY` 로 사용하고 있고,
- 두 Entity 가 `1:N`, `N:1` 양방향 관계를 가지고 있고, 
- Entity 자체를 JSON 으로 직렬화하여 반환할 경우 `순환 참조가 발생`
- 또는, Entity 에 @Data, @ToString, @EqualsAndHashCode 을 사용하면서 두 객체가 서로의 필드를 계속 참조하며 순환참조 발생

```console
Cannot call sendError() after the response has been committed

...
...

Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is org.springframework.http.converter.HttpMessageNotWritableException: Could not write JSON: Infinite recursion (StackOverflowError); nested exception is com.fasterxml.jackson.databind.JsonMappingException: Infinite recursion (StackOverflowError) (through reference chain: com.leather.workshop.domain.product.domain.Product["productCategory"]->com.leather.workshop.domain.product.domain.

ProductCategory$HibernateProxy$Q6UsphhP["products"]->org.hibernate.collection.internal.PersistentSet[0]->com.leather.workshop.domain.product.domain.Product["productCategory"]->com.leather.workshop.domain.product.domain.ProductCategory$HibernateProxy$Q6UsphhP["products"]->org.hibernate.collection.internal.PersistentSet[0]->com.leather.workshop.domain.product.domain.Product["productCategory"]->com.leather.workshop.domain.product.domain.

ProductCategory$HibernateProxy$Q6UsphhP["products"]->org.hibernate.collection.internal.PersistentSet[0]->com.leather.workshop.domain.product.domain.Product["productCategory"]->com.leather.workshop.domain.product.domain.ProductCategory$HibernateProxy$Q6UsphhP["products"]->org.hibernate.collection.internal.PersistentSet[0]->com.leather.workshop.domain.product.domain.Product["productCategory"]->com.leather.workshop.domain.product.domain
...
...
ProductCategory$HibernateProxy$Q6UsphhP["products"]->org.hibernate.collection.internal.PersistentSet[0]->com.leather.workshop.domain.product.domain.Product["createdDateTime"])] with root cause
```

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/leather-project-issue/%EA%B7%B8%EB%A6%BC1.png" width="90%"></center>

## Cause

- `Product` 엔티티를 JSON 형태로 직렬화하는 과정에서, `Product` 엔티티가 참조하고 있는 `ProductCategory` 엔티티를 조회하게 된다.
- `ProductCategory` 엔티티를 조회하는 과정에서, `ProductCategory` 엔티티가 참조하고 있는 `Product` 엔티티를 조회한다.
- 다시 `Product` 엔티티를 조회하는 과정에서, `Product` 엔티티가 참조하고 있는 `ProductCategory` 엔티티를 조회... 다시 `ProductCategory` 엔티티를 조회..
- 이렇게 위 과정이 끊임없이 반복되며 두 Entity는 계속 서로를 참조하다 결국 StackOverflowError 를 발생시키게 된다.

**@ResponseBody 와 객체 직렬화**

Spring Boot 는 Controller에 @ResponseBody 선언 시 Object 를 JSON 형태로 직렬화하기 위해 `HttpMessageConverters` 에서 `jackson Library` 활용

- [jackson의 직렬화 방식](https://www.baeldung.com/jackson-field-serializable-deserializable-or-not)
  - 기본적으로 public 필드만 직렬화를 시도
  - private 필드를 직렬화하기 위해 getter 선언

## Solution

### 1. Entity 대신 DTO 로 반환

- 가장 추천하는 방식이다.
- Entity 클래스는 데이터베이스와 맞닿는 핵심 클래스이다.
- Entity 클래스를 기준으로 수많은 클래스나 비즈니스 로직들이 동작하고 있다.
- Entity 클래스를 통해 여러 클래스들이 영향을 받을 수 있으므로 Entity 클래스를 Request/Response 클래스로 사용하는 것은 강력하게 추천하지 않는다.
- 컨트롤러에서 Response 값으로 여러 테이블을 조인해야하는 경우가 많으므로, DB Layer 와 View Layer 의 역할 분리를 철저하게 해주자.
- 역시나 객체지향 설계에 있어서 역할과 책임은 중요한 요소인 것 같다.

[스프링 부트와 AWS로 혼자 구현하는 웹 서비스 참고]

### 2. @JsonManagedReference & @JsonBackReference

양방향 관계에서 직렬화 방향을 설정하여 순환 참조를 해결할 수 있도록 설계된 애노테이션

**@JsonManagedReference**

- 연관관계 주인 반대 Entity 에 선언

- 정상적으로 직렬화 수행

  ```java
  @OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JsonManagedReference
  private Set<ProductUploadFile> productUploadFiles = new LinkedHashSet<>();
  ```

**@JsonBackReference**

- 연관관계의 주인 Entity 에 선언

- 직렬화가 되지 않도록 수행

  ```java
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_category_id", nullable = false)
  @JsonBackReference
  private ProductCategory productCategory;
  ```

### 3. @JsonIgnore

- 양방향 관계를 가지고 있는 두 엔티티 중 하나의 엔티티의 참조 필드에 직렬화를 제외시키는 방법
- JSON 직렬화 과정에서 해당 애노테이션이 선언된 필드는 직렬화 대상에서 제외
- 해당 필드가 직렬화에 필요할 경우에는 적합하지 않은 방법

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "product_category_id", nullable = false)
@JsonIgnore
private ProductCategory productCategory;
```

## Reference

<https://dev-coco.tistory.com/133>

<https://subji.github.io/posts/2020/08/06/infiniterecusionofjpa>