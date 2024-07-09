# Transaction Propagation

스프링 트랜잭션을 다시 공부하며 영한님의 [스프링 DB 2편 - 데이터 접근 활용 기술](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2) 강의 내용을 요약해 보았습니다.

# 트랜잭션 전파

Spring Transaction Propagation Use transaction twice

![출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2](https://github.com/jihunparkme/jihunparkme.github.io/blob/master/post_img/spring/spring-transaction-connection.png?raw=true)<br/>[출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-db-2]

로그를 보면 트랜잭션1, 2가 같은 conn0 커넥션을 사용중인데, 이것은 커넥션 풀 때문
- 트랜잭션1은 conn0을 모두 사용 후 커넥션 풀에 반납하고, 이후 트랜잭션2가 conn0을 커넥션 풀에서 획득

히카리(HikariCP) 커넥션 풀에서 커넥션을 획득하면 실제 커넥션을 그대로 반환하는 것이 아니라 내부 관리를 위해
`히카리 프록시 커넥션 객체`를 생성해서 반환
  - 이 객체의 주소를 확인하면 커넥션 풀에서 획득한 커넥션 구분이 가능
  - `HikariProxyConnection@2120431435 wrapping conn0: ...`
  - `HikariProxyConnection@1567077043 wrapping conn0: ...`
  - 트랜잭션2에서 커넥션이 재사용 되었지만, 각각 커넥션 풀에서 커넥션을 조회
