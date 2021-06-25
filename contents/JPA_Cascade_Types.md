# JPA Cascade Types

Spring JPA CascadeType 종류

`javax.persistence.CascadeType`

---

## JPA Cascade Type

- ALL
- PERSIST
- MERGE
- REMOVE
- REFRESH
- DETACH

---

## CascadeType.ALL

- 상위 엔터티에서 하위 엔터티로 모든 작업을 전파

```java
@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String name;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
    private List<Address> addresses;
}
```

```java
@Entity
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String street;
    private int houseNumber;
    private String city;
    private int zipCode;

    @ManyToOne(fetch = FetchType.LAZY)
    private Person person;
}
```

---

## CascadeType.PERSIST

- 하위 엔티티까지 영속성 전달
  - Person Entity를 저장하면 Address Entity도 저장

```java
@Test
public void whenParentSavedThenChildSaved() {
    Person person = new Person();
    Address address = new Address();
    address.setPerson(person);
    person.setAddresses(Arrays.asList(address));
    session.persist(person); // persist 동작 수행
    session.flush();
    session.clear();
}
```

```sql
Hibernate: insert into Person (name, id) values (?, ?)
Hibernate: insert into Address (
    city, houseNumber, person_id, street, zipCode, id) values (?, ?, ?, ?, ?, ?)
```

---

## CascadeType.MERGE

- 하위 엔티티까지 병합 작업을 지속
  - Address와 Person 엔티티를 조회한 후 업데이트

```java
@Test
public void whenParentSavedThenMerged() {
    int addressId;
    Person person = buildPerson("devender");
    Address address = buildAddress(person);
    person.setAddresses(Arrays.asList(address));
    session.persist(person);
    session.flush();
    addressId = address.getId();
    session.clear();

    Address savedAddressEntity = session.find(Address.class, addressId);
    Person savedPersonEntity = savedAddressEntity.getPerson();
    savedPersonEntity.setName("devender kumar");
    savedAddressEntity.setHouseNumber(24);
    session.merge(savedPersonEntity); // merge 동작 수행
    session.flush();
}
```

```sql
Hibernate: select address0_.id as id1_0_0_, address0_.city as city2_0_0_, address0_.houseNumber as houseNum3_0_0_, address0_.person_id as person_i6_0_0_, address0_.street as street4_0_0_, address0_.zipCode as zipCode5_0_0_ from Address address0_ where address0_.id=?

Hibernate: select person0_.id as id1_1_0_, person0_.name as name2_1_0_ from Person person0_ where person0_.id=?

Hibernate: update Address set city=?, houseNumber=?, person_id=?, street=?, zipCode=? where id=?

Hibernate: update Person set name=? where id=?
```

---

## CascadeType.REMOVE

- 하위 엔티티까지 제거 작업을 지속
  - 연결된 하위 엔티티까지 엔티티 제거

```java
@Test
public void whenParentRemovedThenChildRemoved() {
    int personId;
    Person person = buildPerson("devender");
    Address address = buildAddress(person);
    person.setAddresses(Arrays.asList(address));
    session.persist(person);
    session.flush();
    personId = person.getId();
    session.clear();

    Person savedPersonEntity = session.find(Person.class, personId);
    session.remove(savedPersonEntity); // remove 동작 수행
    session.flush();
}
```

```sql
Hibernate: delete from Address where id=?
Hibernate: delete from Person where id=?
```

---

## CascadeType.REFRESH

- 데이터베이스로부터 인스턴스 값을 다시 읽어 오기(새로고침)
  - 연결된 하위 엔티티까지 인스턴스 값 새로고침

```java
@Test
public void whenParentRefreshedThenChildRefreshed() {
    Person person = buildPerson("devender");
    Address address = buildAddress(person);
    person.setAddresses(Arrays.asList(address));
    session.persist(person);
    session.flush();
    person.setName("Devender Kumar");
    address.setHouseNumber(24);
    session.refresh(person); // refresh 동작 수행

    assertThat(person.getName()).isEqualTo("devender");
    assertThat(address.getHouseNumber()).isEqualTo(23);
}
```

---

## CascadeType.DETACH

- 영속성 컨텍스트에서 엔티티 제거
  - 연결된 하위 엔티티까지 영속성 제거

```java
@Test
public void whenParentDetachedThenChildDetached() {
    Person person = buildPerson("devender");
    Address address = buildAddress(person);
    person.setAddresses(Arrays.asList(address));
    session.persist(person);
    session.flush();

    assertThat(session.contains(person)).isTrue();
    assertThat(session.contains(address)).isTrue();

    session.detach(person); // detach 동작 수행 시 영속성 컨텍스트에 존재하지 않음.
    assertThat(session.contains(person)).isFalse();
    assertThat(session.contains(address)).isFalse();
}
```

## Reference

> [https://www.baeldung.com/jpa-cascade-types](https://www.baeldung.com/jpa-cascade-types)
