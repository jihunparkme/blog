# Spring Batch & Jenkins 구동 방식 개선

현재 실무에서 스프링 배치의 구동 방식이 아래와 같다

Deploy Tool
- 배치 프로젝트 특정 브랜치의 jar 파일을 배치 서버에 배포

Jenkins
- Build periodically - Schedule 설정
- Execute shell 을 통한 배치 실행
  - 쉘에는 파라미터, 프로파일 설정과 jar 파일로 배치 잡을 실행하는 커멘드가 포함

## 현상

현재 구동 방식에서는 한 가지(한 가지가 아닐 수도 있지만..) 문제가 있었다.

.

jar 파일이 배포되는 사이 배치 잡이 실행되거나, 배치 잡이 실행되는 사이 jar 파일이 배포되면

Class 정보를 찾을 수 없다는 예외(`java.lang.NoClassDefFoundError`, `java.lang.ClassNotFoundException`) 가 터지면서 배치 실행이 실패하거나, 종료되고,

어떠한 경우에는 배치가 종료되지도 않고 정상적인 동작을 수행하지 않는 STARTED 상태의 좀비로 감염되는 상황이 발생했다.

.

위와 같은 현상은 jar 파일 실행도중 jar 파일이 변경(수정 혹은 삭제) 될 경우 발생할 수 있다고 한다.

.

좀비 상태가 되어버리면 살아 있지도 죽지도 않은 상태가 되는데, 배치 상태는 STARTED 로 남아있기 때문에

스케줄러에서 다음 배치 가동 시간이 되더라도 (Job 중복 실행 방지로 인해) 배치를 실행시키지 못하게 된다.

이렇게.. 아무도 모르게 며칠 뒤, 혹은 특이 케이스가 발견되어 배치 상태를 보면 좀비가 되어 있는 모습를 종종 보게 되었다..

## 고민

**방법 1. 스프링 기동 시 실행 중인 배치의 상태를 STARTED 에서 FAILED 로 변경**

처음에는 단순하게 스프링이 기동될 때 STARTED 상태의 좀비 배치를 FAILD 상태로 변경시켜 주기 위해

`ApplicationListener`, `ContextRefreshedEvent` 를 활용해서

스프링 기동 시 실행 중인 배치의 상태를 STARTED 에서 FAILED 로 변경하는 방법을 적용해 보려고 하였다.

(WAS 기동 시 배치 상태가 STARTED 인 것은, WAS 내릴 때 해당 배치의 상태를 고려하지 않고 내린 것이 되므로, 상태를 FAILED 처리할 필요성이 있음)

> reference: https://twofootdog.tistory.com/112

.

하지만.. 배포로 인해 jar 파일이 변경되면서 (1~2초 차이로) 스프링이 가동조차 못 하는 케이스도 발생하게 되었다.

이렇게 jar 파일 쪽으로 초점을 돌리게 되었다.

---

**방법 2. jar 파일 조회 후 배치 실행**

배치 실행 전에 jar 파일 여부를 체크한 뒤 스프링이 기동될 수 있도록 해보았다.

```bash
if [ -f /app/deploy/batch/batch-0.0.1-SNAPSHOT.jar ]; then
  /app/batch/shell/run_batch_job.sh sampleJob
fi
```

하지만.. jar 파일 여부 체크를 통과하고

스프링이 기동될 타이밍에 배포로 jar 파일이 변경되는 케이스도 있다 보니..

이 방법도 완벽한 해결책이 될 수 없었다.

## 개선

여러 고민과 탐색, 그리고 테스트 끝에 ..

`Batch 실행 시 원본 jar 를 실행하기 때문에 링크된 파일이 변경되어도 실행 중이던 Batch가 종료되지 않는다.` 는 글을 보게 되었고, 

([동욱님 블로그](https://jojoldu.tistory.com/)였던 것 같은데 글을 못 찾겟는..🥲)

심볼릭 링크를 스위치처럼 이용해 보면 어떨까.. 라는 생각과 함께 리눅스의 Symbolic link 를 활용해보게 되었다.

.

대략적인 아이디어는 아래와 같다.

|status|symbolic link|version1|version2|description|
|---|---|---|---|---|
||~/verseion1|0.0.1.jar|0.0.1.jar|심볼릭 링크는 1번 디렉토리의 jar 파일로 링크가 되어 있다.|
|배포||0.0.1.jar|0.0.2.jar|배포를 하게 되면 2번 디렉토리의 jar 파일이 변경되고|
||~/verseion2|0.0.1.jar|0.0.2.jar|링크는 2번 디렉토리의 jar 파일로 변경된다.|
|배포||0.0.3.jar|0.0.2.jar|마찬가지로, 배포를 하게 되면 1번 디렉토리의 jar 파일이 변경되고|
||~/verseion1|0.0.3.jar|0.0.2.jar|링크는 다시 1번 디렉토리의 jar 파일로 변경된다.|
|...|...|...|...|...|

.

**change-link.sh**

```bash
#!/bin/bash
 
ORIGIN_JAR=$(readlink /app/batch/shell/application.jar)
DEPLOY_PATH=/app/deploy/batch
 
if [[ ${ORIGIN_JAR} == *"version-1"* ]]; then
  VERSION=version-2
else
  VERSION=version-1
fi
 
echo $VERSION
 
/usr/bin/rm -f ${DEPLOY_PATH}/${VERSION}/batch-0.0.1-SNAPSHOT.jar
/usr/bin/cp -f ${DEPLOY_PATH}/batch-0.0.1-SNAPSHOT.jar ${DEPLOY_PATH}/${VERSION}/
 
unlink /app/batch/shell/application.jar
ln -s ${DEPLOY_PATH}/${VERSION}/batch-0.0.1-SNAPSHOT.jar  /app/batch/shell/application.jar
```

배치 잡 실행 기준 원본 jar 파일을 유지하면서 수정된 jar 파일을 배포하기 위함

- readlink 명령어로 심볼링 링크가 연결되어 있는 jar 파일 경로 가져오기
- 심볼링 링크가 version-1 디렉토리로 연결되어 있다면
  - version-2 디렉토리의 jar 파일 갱신, 링크를 version-2 의 jar 파일로 연결
- 반대로 심볼링 링크가 version-2 디렉토리로 연결되어 있다면
  - version-1 디렉토리의 jar 파일 갱신, 링크를 version-1 의 jar 파일로 연결

**run_batch_job.sh**

```bash
#!/bin/bash
 
jobName=$1
jobParameters=""
args=("$@")
for arg in "$@"; do
  if [[ $arg == $1 ]]; then
    continue
  fi
  jobParameters+=" $arg"
done
 
PROFILE="stg"
JAVA_OPTS="-Xms512m -Xmx1024m"
ORIGIN_JAR=$(readlink /app/batch/shell/application.jar)
 
echo "> ORIGIN_JAR_PATH: ${ORIGIN_JAR}"
 
$JAVA_HOME/bin/java -jar -Dspring.profiles.active=$PROFILE ${ORIGIN_JAR} $JAVA_OPTS --job.name=$jobName $jobParameters
```

**Jenkins Execute shell**

```bash
/app/batch/shell/run_batch_job.sh sampleJob
```

## 마무리

나름의 여러 고민과 탐색 끝에 적용한 방식이지만..

분명 더 좋은 개선 방법이 있을 것이라고 생각한다..

달콤한 피드백 부탁드립니다..🙏🏻