# JPA 양방향 관계 Entity 저장하기

인간은 습관의 동물이다.

습관에는 대부분 좋은 습관이 많지만 그중에 나쁜 습관도 있다.

그것은 바로.. 원리를 모르고 개발하는 습관이다. 😯

.

'요로케할 때 이렇게 했었으니까, 이렇게 해야지~!' 는 더 이상 통하지 않는다..

사이드 프로젝트를 하며 깨닫는 부분이 많다.

.

습관처럼 몸에 익어버린 방법들이 갑자기 뇌 정지가 오며 멈춰버렸을 때, 머릿속이 하얘지고 말았다.

적어도 왜 이렇게 개발했었는지 원리를 알았더라면, 이럴 일은 없었을텐데..

정말 기본적인 것들 앞에서 갑자기 띠용🙄 할 때가 있다.

(최근에 습관성 타이핑으로 노트북 비밀번호 까먹은 거는 진짜 비밀..🤫)

.

본론으로, JPA 양방향 관계 Entity를 저장해보자.

## Situation

상품 등록 시 여러 이미지를 첨부할 수 있다.

상품은 첨부파일과 1:N 관계(`@OneToMany`)를 가지고 있다.

```java
@Getter
@NoArgsConstructor
@Entity
public class Product extends BaseTimeEntity {

    //...

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<ProductUploadFile> productUploadFiles = new LinkedHashSet<>();

    //...
}
```

반대로 첨부파일은 상품과 N:1 관계(`@ManyToOne`)를 가지고 있다.

```java
@Getter
@NoArgsConstructor
@Entity
public class ProductUploadFile extends BaseTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    //...
}

```

## Problem

상품 저장 시 첨부파일도 함께 저장되어야 한다.

그런데.. 아래 코드는 뭔가 비효율적이게 보인다.

어떤 부분이 비효율적일까?

.

참고로, 요청에는 DTO 가 사용되었다.

```java
@Transactional
public Long save(ProductDto.Request form, SessionUser user) throws IOException {

    ProductCategory category = categoryRepository.findById(form.getProductCategory()).get();

    /** 
     * 1. 상품 엔티티 생성 및 저장
     */
    Product product = Product.builder()
            .productCategory(category)
            .name(form.getName())
            .contents(form.getContents())
            .hits(0L)
            .deleteYn(BooleanFormatType.N)
            .userId(user.getId())
            .build();

    Product entityProudct = productRepository.save(product);

    /** 
     * 2. 썸네일 첨부파일 엔티티 생성 및 저장
     */
    MultipartFile formThumbnailFile = form.getThumbnailFile();
    UploadFile uploadFile = fileUtilities.storeFile(formThumbnailFile, PathConst.PRODUCT);
    ProductUploadFile productThumbnailFile = ProductUploadFile.builder()
            .product(entityProudct)
            .uploadFileName(uploadFile.getUploadFileName())
            .storeFileName(uploadFile.getStoreFileName())
            .thumbnailYn(BooleanFormatType.Y)
            .build();
    uploadFileRepository.save(productThumbnailFile);

    /** 
     * 3. 기타 첨부파일 엔티티 생성 및 저장
     */
    List<MultipartFile> formUploadFiles = form.getProductUploadFiles();
    if (formUploadFiles != null && !formUploadFiles.isEmpty()) {
        List<UploadFile> uploadFiles = fileUtilities.storeFiles(formUploadFiles, PathConst.PRODUCT);
        uploadFiles.stream()
                .map(up -> {
                    ProductUploadFile file = ProductUploadFile.builder()
                            .product(entityProudct)
                            .uploadFileName(up.getUploadFileName())
                            .storeFileName(up.getStoreFileName())
                            .thumbnailYn(BooleanFormatType.N)
                            .build();

                    uploadFileRepository.save(file);
                    return null;
                });
    }

    return entityProudct.getId();
}
```

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/leather-project-issue/그림4.png" width="100%"></center>

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/leather-project-issue/그림5.png" width="100%"></center>

결과를 보니 상품과 첨부 파일이 문제없이 저장된다.

그럼 무엇이 문제란 것인가!?

.

문제는 없다.

그럼 뭐냐!??!🤯

.

무언가.. JPA를 비효율적으로 사용하는 것 같다.

Repository의 save() 호출이 너무 많다.

아마 첨부파일의 개수만큼 호출될 것이다...

여기서, "상품만 저장하면 나머지 연관된 엔티티들이 자동으로 저장되게 할 순 없을까?"가 핵심이다.

## Solution

Product.java

- CascadeType 은 ALL(`PERSIST, REMOVE, MERGE, REFRESH, DETACH`)로 설정

```java
public class Product extends BaseTimeEntity {
    //..
    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<ProductUploadFile> productUploadFiles = new LinkedHashSet<>();

    public void addProductUploadFiles(ProductUploadFile productUploadFile) {
        productUploadFiles.add(productUploadFile);
        productUploadFile.setProduct(this);
    }
}
```

ProductService.java

```java
@Transactional
public Long save(ProductDto.SaveRequest form, SessionUser user) throws IOException {

    ProductCategory category = categoryRepository.findById(form.getProductCategory()).get();
    
    /**
    * 1. 상품 엔티티 생성
    */
    Product product = Product.builder()
            .productCategory(category)
            .name(form.getName())
            .contents(form.getContents())
            .hits(0L)
            .deleteYn(BooleanFormatType.N)
            .userId(user.getId())
            .build();

    /** 
     * 2. 썸네일 첨부파일 엔티티 생성
     */
    MultipartFile formThumbnailFile = form.getThumbnailFile();
    UploadFile uploadFile = fileUtilities.storeFile(formThumbnailFile, PathConst.PRODUCT);
    ProductUploadFile productThumbnailFile = ProductUploadFile.builder()
            .uploadFileName(uploadFile.getUploadFileName())
            .storeFileName(uploadFile.getStoreFileName())
            .thumbnailYn(BooleanFormatType.Y)
            .build();
    product.addProductUploadFiles(productThumbnailFile);

    /** 
     * 3. 기타 첨부파일 엔티티 생성
     */
    List<MultipartFile> formUploadFiles = form.getProductUploadFiles();
    if (formUploadFiles != null && !formUploadFiles.isEmpty()) {
        List<UploadFile> uploadFiles = fileUtilities.storeFiles(formUploadFiles, PathConst.PRODUCT);
        uploadFiles.stream()
                .map(up -> {
                    ProductUploadFile puf = ProductUploadFile.builder()
                            .uploadFileName(up.getUploadFileName())
                            .storeFileName(up.getStoreFileName())
                            .thumbnailYn(BooleanFormatType.N)
                            .build();

                    product.addProductUploadFiles(productThumbnailFile);
                    return null;
                });
    }

    /**
     * 4. 상품 엔티티 저장
     */
    return productRepository.save(product).getId();
}
```

기존 방법과 차이점이 느껴진다면 👏🏻👏🏻👏🏻~!!

기존에는 첨부파일 엔티티를 생성하자마자 바로 save() 호출을 했었다.

하지만, 우리는 JPA를 사용 중이지 않은가!!

Repository의 save() 호출이 많았었지만 이제 단 한 번만 호출하면 된다.

.

(0) @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)

- OneToMany 필드(productUploadFiles)의 cascade 타입을 `PERSIST` 상태로 설정
- 영속성 전이를 통해 연관된 엔티티도 함께 영속 상태로 만들기

(1) `상품 엔티티` 생성

(2, 3) `첨부파일 엔티티` 생성

- 연관관계 편의 메서드 addProductUploadFiles()를 통해 첨부파일, 상품 엔티티 양쪽에 모두 연관 엔티티 세팅

(4) 상품 엔티티만 레파지토리에 저장해주면 외래키로 매핑된 첨부파일 엔티티는 자동으로 저장

.

쿼리는 이전과 동일하게 동작한다.

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/leather-project-issue/그림6.png" width="100%"></center>

<center><img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/leather-project-issue/그림7.png" width="100%"></center>

## Result

저장되는 쿼리는 동일하지만 조금 더 JPA 답게 코드를 작성할 수 있게 되었다.

어쩌면 정말 기본적인 부분일 수 있지만, 순간 머릿속이 하얘지면서 비효율적인 방향으로 코드를 작성하게 되었었다.

나의 작은 머리에 과부하가 온 것인가😢

.

다시 정신을 차리고 머리에 노크를 하면서 원리를 찾다 보니 다시 방법이 떠올라서 작성해 보았다.

노트북 비밀번호든.. 뭐든.. 잘 적어놓자..🤣

