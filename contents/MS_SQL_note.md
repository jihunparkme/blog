# MS SQL

자주 사용하고 자주 까먹는 쿼리 모음

a.k.a. 매번 찾기 귀찮은

## Insert

```sql
INSERT INTO 테이블 이름 (열1, 열2, ...)
VALUES (값1, 값2 , ….)
-- Example
INSERT INTO table_name (col1, col2, ...)
VALUES (3, 'value' , ….)
```

## Alter

- 컬럼 추가

```sql
ALTER TABLE 테이블명 ADD 컬럼명 데이터타입 속성 기본값
-- Example
ALTER TABLE table_name ADD column_name CHAR(1) NOT NULL DEFAULT 'N'
```

- 컬럼 삭제

```mysql
ALTER TABLE 테이블명 DROP COLUMN 컬럼명
-- Example
ALTER TABLE table_name DROP COLUMN column_name
```

- 컬럼 속성 변경

```sql
ALTER TABLE 테이블명 ALTER COLUMN 컬럼명 데이터타입 속성
--Example
ALTER TABLE table_name ALTER COLUMN column_name CHAR(4) NULL DEFAULT 'AB01'
```

- 컬럼명 변경

```sql
EXEC SP_RENAME '테이블명.[변경전컬럼이름]', '새로운컬럼이름', 'COLUMN'
--Example
EXEC SP_RENAME 'table_name.[before_colName]', 'after_colName', 'COLUMN'
```

---

## Update

- 안전하게 데이터 Update 하기

```sql
-- 1. Select로 변경 전 후 확인하기
--         item이 apple인 데이터의 col01 값을 update
SELECT col01, col01 + 'world'
FROM table_name
WHERE item='apple'

-- 2. Select 쿼리에서 update로 변경
--        select -> set,
--         set 에서 , -> =
--      from -> update
UPDATE table_name
SET col01 = col01 + 'world'
WHERE item='apple'
```

---

## Delete

- 안전하게 데이터 Delete 하기

```sql
-- 1. 삭제할 데이터 확인
--        item이 apple인 데이터 삭제
select *
from table_name
where item = 'apple'


-- 2. select 쿼리에서 delete 쿼리로 변경
--        select절은 지우고
--        from -> delete
delete table_name
where item = 'apple'
```

---

## CASE WHEN

- 단일 조건

```sql
-- CASE WHEN 조건절 THEN 참일때 값 ELSE 거짓일때 값 END 컬럼명
CASE WHEN gender='1' THEN '남' ELSE '여' END 성별
```

- 다중 조건

```sql
SELECT NAME,
   (CASE WHEN SCORE>= '90' THEN 'A'
        WHEN (SCORE>= '80' AND SCORE < '90') THEN 'B'
        WHEN (SCORE>= '70' AND SCORE < '80') THEN 'C'
        WHEN (SCORE>= '60' AND SCORE < '70') THEN 'D'
        ELSE 'F'
    END) AS '학점'
FROM table_name
```

---

## Sequence

- 조회 결과 순번 지정 : ROW_NUMBER

```mssql
SELECT ROW_NUMBER() OVER( ORDER BY (SELECT 1)) AS ROW
FROM TABLE_NAME
```

- 숫자 자릿수에 맞게 0 채우기

```mssql
SELECT REPLICATE('0', 3 - LEN('3')) + '1' AS ROWNUM
```

- 조회 결과 순번 지정 + 숫자 자릿수에 맞게 0 채우기

```sql
SELECT REPLICATE('0', 3 - LEN(CONVERT(NVARCHAR(3),ROW_NUMBER() OVER( ORDER BY (SELECT 1))))) + CONVERT(NVARCHAR(3),ROW_NUMBER() OVER( ORDER BY (SELECT 1))) AS ROWNUM
FROM TABLE_NAME
```

- 그룹별 순번(index) : PARTITION BY

```sql
SELECT *, ROW_NUMBER() OVER(PARTITION BY GROUP_NAME ORDER BY GROUP_PIVOT DESC) 'P_ROW'
FROM TABLE_NAME
```

---

## Transaction

- Transaction [reference]('https://coding-factory.tistory.com/82')
  - BEGIN TRAN : 트랜잭션 시작
  - ROLLBACK TRAN : 트랜잭션 이전상태로 ROLL BACK
  - COMMIT TRAN : 트랜잭션 완료

```sql
SELECT .. --Update할 데이터 확인
♠ BEGIN TRAN --트랜잭션 시작
UPDATE .. -- 업데이트 수행
SELECT .. -- 업데이트 된 데이터 확인
♠ COMMIT TRAN --트랜잭션 완료
```

---

**Reference**

> [SQL Tutorial](https://www.w3schools.com/sql/)
>
> [SQL Server Functions](https://www.w3schools.com/sql/sql_ref_sqlserver.asp)
>
> [SQL Server 기술 설명](https://docs.microsoft.com/ko-kr/sql/sql-server/?view=sql-server-ver15)
