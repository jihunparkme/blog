# Microservice Architecture(MSA) 빠르게 훑어보기

## Monolithic Architecture

<img src="https://raw.githubusercontent.com/jihunparkme/blog/main/img/msa_img/1.png" height="400">

- 기존 SW 개발 방식
- 하나의 war 또는 ear에 모든 서비스 기능이 포함
  - 서비스 기능들을 하나의 Application에 담는 것
  - 특정 기능의 작은 이슈가 Application 전체에 영향을 미칠 수 있음
  - 모듈 간 `의존성이 강함`

## Microservices Architecture

![Result](https://raw.githubusercontent.com/jihunparkme/blog/main/img/msa_img/2.png)

```text
Microservice : 하나의 큰 Application을 여러 개의 다른 역할을 수행하는 Application으로 분리하였을 때 각 Application을 의미

Microservices Architecture : 이렇게 Microservice를 분리하여 여러 개의 작은 Application으로 쪼개어 변경과 조합이 가능하도록 만든 아키텍처
```

- SW 기능들을 `작고`, `독립적`이며, `느슨하게` 결합된 모듈(서비스)로 분해하는 아키텍쳐
  - 개별 모듈은 개별적인 작업을 담당하고, 엑세스할 수 있는 개방형 표준 API로 다른 모듈과 통신
- 기능적인 분해
  - 다양한 기능들을 `독립적인 서비스`로 바라보는 전략
  - `확장성` 및 `생상성` 향상
  - 단일 모듈의 장애가 전체 애플리케이션에 영향을 주지 않음
  - 모듈 간 `의존성이 적고 유연`
- MSA 기반의 애플리케이션은 Docker 컨테이너 가상화로 배포하는 것이 좋음
  - VM도 배포 가능하지만 리소스의 비효율적인 부분이 있음(모듈들의 단위가 작다보니)
  - 각 기능들은 컨테이너 단위로 관리되고, 각 컨테이너는 각자의 OS와 DB를 가짐

**example**

`Monolithic Architecture`의 경우 특정 기능 수정/추가 시 Application 전체가 구동되는 서버에서 배포/재가동이 필요하지만,

`Microservices Architecture`의 경우 특정 기능이 포함된 서비스에 해당하는 서버에서만 독립적으로 배포하여 기존 서비스는 지속될 수 있다.

## Reference

[마이크로서비스 아키텍처. 그것이 뭣이 중헌디?](http://guruble.com/%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4microservice-%EC%95%84%ED%82%A4%ED%85%8D%EC%B2%98-%EA%B7%B8%EA%B2%83%EC%9D%B4-%EB%AD%A3%EC%9D%B4-%EC%A4%91%ED%97%8C%EB%94%94/) 게시글을 보면 guruble님이 이해하기 쉽게 풀어서 설명을 해주셨다.

해당 포스팅을 보면 충분히 MSA(MicroService Architecture)를 이해할 수 있을 것이다.

> [마이크로 서비스 아키텍처 스타일](https://docs.microsoft.com/ko-kr/azure/architecture/guide/architecture-styles/microservices)
>
> [마이크로서비스 아키텍처(Microservices Architecture)의 장점과 단점](https://giljae.medium.com/%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4-%EC%95%84%ED%82%A4%ED%85%8D%EC%B2%98-microservices-architecture-%EC%9D%98-%EC%9E%A5%EC%A0%90%EA%B3%BC-%EB%8B%A8%EC%A0%90-7c45615cfe1a)
