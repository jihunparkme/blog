# JPA

## N+1 문제

```text
JpaRepository에 정의한 인터페이스 메서드를 실행하면 JPA는 메서드 이름을 분석해서 JPQL을 생성하여 실행하게 된다. JPQL은 SQL을 추상화한 객체지향 쿼리 언어로서 특정 SQL에 종속되지 않고 엔티티 객체와 필드 이름을 가지고 쿼리를 한다. 그렇기 때문에 JPQL은 findAll()이란 메소드를 수행하였을 때 해당 엔티티를 조회하는 select * from Owner 쿼리만 실행하게 되는것이다. JPQL 입장에서는 연관관계 데이터를 무시하고 해당 엔티티 기준으로 쿼리를 조회하기 때문이다. 그렇기 때문에 연관된 엔티티 데이터가 필요한 경우, FetchType으로 지정한 시점에 조회를 별도로 호출하게 된다.
```

`Fetch join` 으로 해결해보자.

- INNER JOIN

```java
@Query("SELECT DISTINCT o FROM Owner o JOIN FETCH o.cats")
List<Owner> findAllJoinFetch();
```

장점


- 연관관계의 연관관계가 있을 경우에도 `하나의 쿼리 문으로 표현`

단점
- Fetch Join을 사용하게 되면 데이터 `호출 시점에 모든 연관 관계의 데이터를 가져오기 때문에` FetchType을 Lazy로 해놓는것이 무의미
  
- 하나의 쿼리문으로 데이터를 가져오다 보니 `페이징 쿼리를 사용할 수 없음`

`EntityGraph` 로 해결해보자.
- OUTER JOIN

```java
@EntityGraph(attributePaths = "cats")
@Query("select o from Owner o")
List<Owner> findAllEntityGraph();
```

- attributePaths에 쿼리 수행시 바로 가져올 필드명을 지정하면 Lazy가 아닌 Eager 조회

`중복 데이터 처리`

- Fetch Join과 EntityGraph 사용 시 JOIN 문이 호출되면서 카테시안 곱이 발생한다. 즉, 중복 데이터가 존재할 수 있다.
  - distinct를 사용하여 중복을 제거
  - 일대다 필드의 타입을 LinkedHashSet으로 선언 (Set은 순서 비보장이므로 LinkedHashSet을 사용하여 순서를 보장)

[N+1 문제](https://incheol-jung.gitbook.io/docs/q-and-a/spring/n+1)

[JPA N+1 문제 및 해결방안](https://jojoldu.tistory.com/165)

## JPA 순환 참조

[[JPA] JSON 직렬화 순환 참조 해결하기](https://data-make.tistory.com/727)

## JPA 양방향 관계 Entity 저장하기

[[JPA] 양방향 관계 Entity 저장하기](https://data-make.tistory.com/730)

## Spring Boot queryDSL 적용



# Spring

## 외부 경로에 있는 리소스 접근하기

보통 이미지 등과 같은 리소스 파일은 프로젝트 외부 공간에 저장하게 된다.

리소스 파일을 로컬 경로에 저장하기 위해 고민이 되었던 부분은, 

- addResourceHandlers 를 사용해서 리소스 요청 시 설정한 로컬 경로에서 리소스를 찾는 방법을 사용할지
- view rendering 전에 파일명만 저장되어 있던 필드를 전체 경로명으로 수정시켜준 후 rendering 을 진행할지..

결론은 addResourceHandlers 를 선택하였는데, 그 이유는 `현재 프로젝트에서 리소스는 이미지만 사용할 예정`이었고, `Product list + file list 까지 이중으로 계속 돌아주면 경로를 수정해 주는 것은 비효율적`이라고 생각을 하였다. 물론 람다, 스트림을 사용하면 개선될 수도 있겠지만, 무엇보다 리소스는 이미지 파일만 있다는 이유가 컸다.

**addResourceHandlers 등록**

WebMvcConfigurer Interface 의 addResourceHandlers method 를 오버라이딩하여 리소스의 프로젝트 경로를 로컬 디렉토리 경로로 매칭시키는 설정이 필요하다.

ResourceHandlerRegistry를 통해 리소스 요청 시 설정한 로컬 경로에서 리소스를 찾도록 설정할 수 있다.

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private String connectPath = "/img/**";

    @Value("${file.directory}")
    private String resourcePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(connectPath)
                .addResourceLocations("file:///" + resourcePath);
    }
}
```

- addResourceHandler : 클라이언트 요청 리소스 경로 `localhost:8080/img/test.jpg`

- addResourceLocations: 실제 리소스가 존재하는 외부 경로 `/Users/aaron/files/test.jpg`
  - 경로의 마지막은 반드시 "/"로 끝나야 하고, 로컬 디스크 경로일 경우 file:/// 접두어를 꼭 붙이자.

- 이 방법의 단점은 sub directory 
 

[reference 1](https://wildeveloperetrain.tistory.com/41)

[reference 2](https://www.baeldung.com/spring-mvc-static-resources#2-serving-a-resource-stored-in-the-file-system)