# Python MS-SQL 연동 (pymssql)

[Documentation](https://pythonhosted.org/pymssql/ref/pymssql.html)

## pymssql 설치

```shell
pip install pymssql
```

## MS SQL 연동

- Auto commit 을 사용할 경우 `conn.autocommit(True)` 을 설정해주면 된다.

```python
conn = pymssql.connect(server, username, password, database) # MSSQL 접속
cursor = conn.cursor() # 쿼리 생성과 결과 조회를 위해 사용
```

## SELECT

- 한글 깨짐을 해결하기 위해 `.encode('ISO-8859-1').decode('euc-kr')` 방법을 많은 분들이 사용하는 듯 하다.

```PYTHON
cursor.execute('SELECT * FROM POST;')
row = cursor.fetchone() # 쿼리 결과의 다음 행을 가져와 리턴
while row:
    print(row[0], row[1].encode('ISO-8859-1').decode('euc-kr'))
    row = cursor.fetchone()
```

## INSERT

- VALUES 안에 내용은 홑따옴표로 감싸주어야 한다.
  - `VALUES ("' + str(data) + '")` 쌍따옴표로 감싸주면 syntax error 발생

```python
data = 'hello World !!'
query = "INSERT INTO POST (CONTENTS) VALUES ('" + str(data) + "')"
cursor.execute(query)
conn.commit()
```

## UPDATE

```python
data = '헬로우 월드 !!'
query = "UPDATE POST set CONTENTS = '" + str(data) + "'  where POST_NO = 11"
cursor.execute(query)
conn.commit()
```

## DELETE

- INSERT 와 마찬가지로 VALUES 안에 내용은 홑따옴표로 감싸주어야 한다.

```python
data = '헬로우 월드 !!'
query = "DELETE FROM POST WHERE CONTENTS = '" + str(data) + "'" 
cursor.execute(query)
conn.commit()
```

# Example Code

```python
import pymssql

server = 'server'
database = 'database'
username = 'username'
password = 'password'

#############################################################################
# MSSQL 접속
conn = pymssql.connect(server, username, password, database) 
# auto commit 을 사용할 경우 : conn.autocommit(True)
cursor = conn.cursor()

#############################################################################
# SELECT
cursor.execute('SELECT * FROM POST;')
row = cursor.fetchone()
while row:
    print(row[0], row[1].encode('ISO-8859-1').decode('euc-kr'))
    row = cursor.fetchone()

#############################################################################
# INSERT
data = 'hello World !!'
query = "INSERT INTO POST (CONTENTS) VALUES ('" + str(data) + "')"  # 문자열은 무조건 홑따옴표
cursor.execute(query)
conn.commit()

#############################################################################
# UPDATE
data = '헬로우 월드 !!'
query = "UPDATE POST set CONTENTS = '" + str(data) + "'  where POST_NO = 11"
cursor.execute(query)
conn.commit()

#############################################################################
# DELETE
data = '헬로우 월드 !!'
query = "DELETE FROM POST WHERE CONTENTS = '" + str(data) + "'" 
cursor.execute(query)
conn.commit()

# 연결 끊기
conn.close()
```

