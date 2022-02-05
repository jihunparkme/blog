# SpringBoot & Mybatis & MS-SQL

오랜만에 Spring Mybatis 설정을 할 일이 생겼는데, 기억이 가물가물해서.. 정리를 해보았다.

하지만, 가급적 메서드 기반으로 쿼리를 생성해주는 Querydsl 을 사용하는 것으로..

## Dependency

먼저 Mybatis 와 mssql-jdbc 관련 종속성을 추가해 주자.

mssql-jdbc 버전은 java 버전과

mybatis-spring-boot 버전은 spring-boot 버전과 맞춰주자.

아무 버전을 선택했다가.. 종속성 문제로 복잡해 지니 잘 확인해 보자! 😢

**pom.xml**

```xml
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>9.4.1.jre8</version>
</dependency>

<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.1.3</version>
</dependency>
```

## DataSource

다음으로 DataSource 정보를 yml 혹은 properties 파일에 작성해 주자.

- driver-class-name : MySQL 사용 시 `com.mysql.cj.jdbc.Driver`
- mapper-locations : mapper xml 파일을 관리할 경로. `src/main/resources` 이후 경로를 작성하자.

**application.yml**

```yml
datasource:
    url: jdbc:sqlserver://{IP}:{PORT};databaseName={DATABASENAME}
    username: username
    password: password
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver

mybatis:
	mapper-locations: classpath:/mybatis/mapper/*.xml
```

## Mybatis Config

Mybatis 관련 설정이 필요하다.

- typeAliases : 사용하려는 DTO에 대한 aliase 등록
- mappers : 사용할 mapper xml 등록

**conf\mybatis-config.xml**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <typeAliases>
        <typeAlias type="com.aaron.example.query.dto.Result" alias="result" />
    </typeAliases>

    <mappers>
        <mapper resource="QueryResult.xml"/>
    </mappers>
</configuration>
```

## Mapper Xml

mybatis-config.xml 에 등록한 mapper xml 파일을 만들자.

parameter 로 넘어온 값은 #{parameterName} 로 처리해주면 된다.

- mapper : Mapper Interface 위치를 작성해 주어야 한다.

**src\main\resources\mybatis\mapper\QueryResult.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.aaron.example.mapper.QueryResultMapper">
    <select id="findResultList" parameterType="String" resultType="map">
        SELECT *
        FROM TABLE
        WHERE DATE between #{beginDate} and #{toDate}
    </select>
</mapper>
```

## Mapper

mapper xml 파일에 등록한 Mapper Interface 를 만들자.

- `@Mapper(org.apache.ibatis.annotations.Mapper)` 를 명시해 주어야 Mybatis 가 인식할 수 있다.
- `@Param` 은 반드시 `org.apache.ibatis.annotations.Param` 를 사용하자.
  - `org.springframework.data.repository.query.Param` 를 사용하면 Mybatis 가 인식하지 못 한다.

**..\mapper\QueryResultMapper.java**

```java
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QueryResultMapper {
    public List<Map<String, Object>> findResultList(@Param("beginDate") String beginDate, @Param("toDate") String toDate) throws Exception;
}
```

## Controller

mapper xml 파일에서 `resultType="map"` 으로 설정했기 때문에 결과는 Map 타입으로 반환된다.

mybatis-config 에서 등록한 typeAlias 로 반환할 경우 `resultType="result"` 로 설정하면 된다.

**BasicResponse.java**

```java
@Builder
@Data
public class BasicResponse {

	private Integer code;
	private HttpStatus httpStatus;
	private String message;
	private Integer count;
	private List<Object> result;
}
```

**..\controller\QueryResultController.java**

```java
@RestController
@RequestMapping(value = "/api")
public class QueryResultController {

    @Autowired
    private QueryResultImpl queryResultService;

    @GetMapping(value = "/list")
    public ResponseEntity<BasicResponse> list(
        @RequestParam(value = "beginDate", required = true) String beginDate,
        @RequestParam(value = "toDate", required = true) String toDate) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> mapperList = queryResultService.getQueryResultMapper().findResultList(beginDate, toDate);
        List<Result> result = mapperList.stream().map(o ->
                                                      mapper.convertValue(o, Result.class))
            									.collect(Collectors.toList());

        BasicResponse basicResponse = BasicResponse.builder()
                                                    .code(HttpStatus.OK.value())
                                                    .httpStatus(HttpStatus.OK)
                                                    .message("조회 성공")
                                                    .result(new ArrayList<>(result))
                                                    .count(result.size()).build();

        return new ResponseEntity<>(basicResponse, HttpStatus.OK);
    }
}
```

## Service

**..\service\QueryResultImpl.java**

```java
@Getter
@Service
public class QueryResultImpl {
	@Autowired
	private QueryResultMapper queryResultMapper;
}
```
