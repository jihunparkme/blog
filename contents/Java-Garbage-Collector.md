# Java Garbage Collection(GC)

[Effective Java 3/E](https://www.yes24.com/Product/Goods/65551284) 책을 보면서 GC에 대한 이야기가 많이 언급되었다.

한 번쯤 공부해 두면 도움이 될 것 같다는 생각에 관련 기사를 읽어보며 정리를 해보았다.

...

애플리케이션에서 성능의 가장 중요한 측면 중 하나는 올바른 GC를 선택하고 이를 최적화는 것이다.

최적의 GC 선택은 각 애플리케이션의 동작 및 요구사항에 따라 달라지므로 자바 개발자는 GC를 이해하는 것이 중요하다.

# Memory management and avoiding memory leaks

- 자바는 객체 사용 후 메모리에서 각 객체를 제거하는 자동 GC 실행
- 애플리케이션에서 생성된 자바 객체는 힙이라는 메모리 세그먼트에 저장
- 애플리케이션이 새 객체를 생성하고 힙이 가득 차면, JVM이 GC를 트리거

## Basic garbage collection

`Mark`
- GC는 힙 메모리를 검색하고 모든 활성 개체(참조를 유지하는 개체) 표시
- 참조가 없는 객체는 모두 제거 가능

`Sweep`
- GC는 힙 메모리에서 참조되지 않은 모든 객체를 재활용

`Compact`
- 힙 메모리에 빈 영역들로 메모리 조각이 발생하지 않도록 압축
  - 흩어져있는 메모리 공간을 모아서 큰 공간을 확보
- 힙의 시작 부분에서 연속된 블록으로 개체를 정렬

> 대부분의 개체가 수명이 짧으므로 힙의 모든 개체에 대한 Mark, Compact 단계가 자주 실행되는 것은 비효율적이고 많이 시간이 소요
>
> 애플리케이션의 특성에 맞는 최적의 GC 선택이 필요

## Generational garbage collection

수명이 짧은 객체를 효율적으로 처리하기 위해, 다른 시간 동안 존재했던 객체에 대해 다른 레벨을 추가함으로써 간단한 가비지 컬렉션 시퀀스를 확장하는 알고리즘
- 힙 메모리를 두 개의 주요 파티션인 young generation, old generation 로 분류
- garbage collection types
  - `minor collections`: young generation 을 위한 타입
  - `major collections`: old generation 을 위한 타입
  - `full collections`: minor, major collections 모두 수행

.

### Young objects

모든 새로운 개체는 처음에 young generation 에 할당되며, 이는 Eden 과 Survivor 두 파티션으로 세분화

**Eden partition**
- 모든 새로운 개체는 이 곳으로 배치
- 각 GC 사이클 이후 Eden 파티션에 남아 있는 모든 개체는 Survivor 파티션으로 이동

**Survivor partition**
- S0(FromSpace), S1(ToSpace) 라는 두 파티션으로 더 나뉨

.

**개체 할당의 일반적인 흐름**

- #1. 초기 모든 새로운 개체는 Eden 파티션에 할당.
  - 두 개의 Survivor 파티션(S0, S1)은 비어있는 상태
- #2. Eden 파티션이 가득 차서 새 개체 할당이 실패하면 JVM이 `Minor GC` 실행. 
   - 더 이상 필요하지 않은 개체들이 제거된 후 모든 활성 개체는 표시(`Mark`)되고, S0 파티션으로 이동
   - Eden 파티션은 초기화(S1은 여전히 비어 있는 상태)
- #3. 이후 Eden 이 다시 가득 차고 또 다른 `Minor GC` 가 실행되면, Eden, S0 파티션의 모든 살아있는 개체를 표시(`Mark`)
  - 그런 다음 Eden, S0 의 모든 살아있는 개체를 S1 로 이동
  - Eden, S0은 비어 있는 상태로 유지(Survivor 파티션 중 하나(S0 또는 S1)는 항상 비어있음)
- #4. 다음 `Minor GC`는 3단계와 동일한 프로세스를 수행하지만 S0에서 S1로 개체를 이동하는 대신 S1에서 S0으로 개체를 이동
  - 모든 활성 개체는 S0에 존재

위 순서 이후에도 3단계와 4단계 사이에 `Minor GC`가 번갈아 동작

여러 번의 `Minor GC` 동작 후, young generatio 에서 개체가 오랜 시간 활성 상태를 유지하게 되면, 해당 개체들은 old generation 으로 이동할 수 있는 자격이 생김

### Old objects

old generation 의 GC는 `Major GC` 라고 불리며 `Mark` 와 `Sweeps` 를 수행

- `Full GC` 가 young, old generations 을 모두 정리
- young, old generations 의 모든 살아있는 대상을 활성화하고 old generations 압축

`Full GC` 를 사용하면 애플리케이션이 중지되어 힙 메모리에 새 개체가 할당되지 않고, `Full GC` 가 수행되는 동안 기존 개체에 연결 불가능

자바에서 가비지 컬렉션이 자동으로 발생하지만, `System.gc()` 또는 `Runtime.gc()` 메서드로 JVM에 가비지 컬렉션을 수행하도록 명시적으로 요청 가능
- 이러한 메서드는 가비지 컬렉션을 보장하지 않고, JVM에 전적으로 의존하므로 권장하지 않는 방법

## Monitoring garbage collection activity and heap use

GC 로그는 메모리와 관련된 성능 문제를 해결하는 데 도움
- GC 로그는 서버에 큰 오버헤드를 주지 않으므로 디버깅을 위해 프로덕션 환경에서 로그를 활성화하는 것을 권장

### jstat

[jstat](https://docs.oracle.com/en/java/javase/11/tools/jstat.html#GUID-5F72A7F9-5D5A-4486-8201-E1D1BA8ACCB5) 커멘드라인을 사용하여 힙 메모리 사용, 메타스페이스 사용, 마이너, 메이저 및 전체 GC 이벤트 수, GC 시간 등 GC 작업 모니터링

```bash
jstat -gc $JAVA_PID
```

### jconsole

[jconsole](https://docs.oracle.com/en/java/javase/11/tools/jconsole.html#GUID-02BB4CB1-9C56-436A-9CEC-6D61984D48C1) 은 실행 중인 Java 애플리케이션의 상태를 보여주는 GUI 실행
- 메모리, 스레드, CPU 사용 모니터링

# How the JVM uses and allocates memory

## Tracking memory use in the JVM

GC가 부적절하게 조정되면 성능 문제와 예측 불가능한 영향 발생
- 예를 들어, 잦은 `Full GC`는 CPU 사용량이 증가하여 애플리케이션 요청 처리가 원활하지 않을 수 있다.
- GC가 너무 자주 발생하거나 CPU의 상당한 비율을 차지하는 경우 가장 먼저 불필요하게 메모리를 할당하고 있는지 확인 필요
  - 과도한 할당은 종종 메모리 누수로 인해 발생
- 이후 개발 환경에서 예상되는 운영 부하를 사용하여 테스트하고 최대 힙 메모리 사용량 확인
  - 운영 힙 크기는 테스트된 최대 용량보다 25%~30% 더 커야 오버헤드를 발생 가능

.

**Out-Of-Memory 탐지**

`-XX:HeapDumpOnOutOfMemoryError` 옵션
- Java 힙의 할당을 충족할 수 없고, 애플리케이션이 `OutOfMemoryError`로 실패할 때 힙 덤프 생성(힙 덤프는 원인 탐색에 도움)
- 메모리 부족 오류에 대한 중요한 정보 제공
- 성능에 영향을 주지 않으므로 운영 환경에서 활성화 가능
- 항상 이 옵션을 설정하는 것을 권장

Heap Dump
- 기본적으로 힙 덤프는 JVM의 작업 디렉터리 있는 `java_pidpid.hprof` 파일에 생성
- `-XX:HeapDumpPath` 옵션을 사용하여 파일 이름이나 디렉터리 지정 가능
- [Jconsole](https://docs.oracle.com/javase/8/docs/technotes/guides/management/jconsole.html)을 사용하여 최대 힙 메모리 사용량 확인 가능

**Heap Dump 뷴석**

힙 덤프는 큰 개체가 오랫동안 보존되었는지 여부를 보여준다. 
- 보존 원인을 찾으려면 코드와 메모리 부족 오류가 발생한 상황 이해 필요
- 코드의 메모리 누수 또는 애플리케이션의 최대 부하에 할당된 메모리가 부족하여 발생할 수 있음

큰 개체를 보유하는 이유
- JVM에 할당된 힙 메모리를 모두 흡수하는 단일 개체
- 메모리를 유지하는 다수의 작은 개체
- 단일 스레드에 의한 대용량 보존(OutOfMemoryError)과 관련된 스레드일 수 있음

대용량 보존의 원인을 확인한 후에는 GC 루트의 경로를 보고 무엇이 개체의 생존을 유지하고 있는지 확인
- GC 루트는 힙 외부의 개체이므로 절대 수집되지 않음
- GC 루트의 경로는 힙 위의 개체가 GC되는 것을 방지하는 참조 체인을 보여줌
- [10 Tips for using the Eclipse Memory Analyzer](https://eclipsesource.com/blogs/2013/01/21/10-tips-for-using-the-eclipse-memory-analyzer/)

## JVM options that affect memory use

JVM에서 사용 가능한 메모리에 영향을 주는 매개 변수 목록
- `-Xms`: 힙의 최소 크기와 초기 크기 설정
- `-Xmx`: 힙의 최대 크기 설정
- `-XX:PermSize`: Permanent Generation(perm) 메모리 영역의 초기 크기 설정(~JDK 8)
- `-XX:MaxPermSize`: perm 메모리 영역의 최대 크기 설정(~JDK 8)
- `-XX:MetaspaceSize`: Metaspace 초기 크기 설정(JDK 8~)
- `-XX:MaxMetaspaceSize`: Metaspace 최대 크기 설정(JDK 8~)

운영 환경에서는 보통 힙 크기가 고정되고 JVM에 미리 할당되도록 `-Xms`, `-Xmx` 옵션을 동일한 값으로 설정

[The java Command Options](https://docs.oracle.com/en/java/javase/15/docs/specs/man/java.html)

## Calculating JVM memory consumption

많은 프로그래머들이 JVM에 대한 최대 힙 값을 올바르게 계산하지만, JVM은 훨씬 더 많은 메모리를 사용
- `-Xmx` 매개 변수의 값은 Java 힙의 최대 크기를 의미하지만, JVM에 의해 소비되는 메모리는 그 밖에도 존재
  - Permanent Generation(JDK 8 이전 이름) 또는 Metaspace(JDK 8 이후 이름)
  - CodeCache
  - 다른 JVM 내부에서 사용하는 native C++ heap
  - 스레드 스택을 위한 공간
  - direct byte buffers
  - GC overhead 
  - ..등이 JVM의 메모리 사용량의 일부로 계산

JVM 프로세스에서 사용하는 메모리 계산법
- JVM 메모리 사용량은 최대 부하 하에서 `-Xmx` 값 이상이 될 수 있음

```java
JVM memory = Heap memory + Metaspace + CodeCache + (ThreadStackSize * Number of Threads) + DirectByteBuffers + Jvm-native
```

## Components of JVM memory consumption

JVM 메모리의 세 가지 중요한 구성 요소
- **`Metaspace`**`
  - 사용된 클래스 및 메서드에 대한 정보 저장
  - JDK 8 이전에는 [HotSpot JVM](https://openjdk.org/groups/hotspot/)에서 perm(Permanent Generation)이라고 불리었고, 이 영역은 Java 힙과 인접
    - JDK 8 이후부터는 perm이 Java 힙과 인접하지 않는 Metaspace로 대체
  - Metaspace는 native memory 에서 할당
  - `MaxMetaspaceSize` 매개 변수는 JVM의 Metaspace 사용을 제한
    - 기본적으로 매우 낮은 크기의 기본값으로 시작하여 필요에 따라 점차 커지는 Metaspace에는 제한이 없음
    - Metaspace에는 클래스 메타데이터만 포함되어 있고, 모든 활성 중인 개체들은 힙 메모리로 이동
      - 따라서 Metaspace의 크기는 perm보다 훨씬 낮음
    - 일반적으로 큰 Metaspace 누출이 발생하지 않는 한 최대 Metaspace 크기를 지정할 필요가 없음
- **`CodeCache`**`
  - JVM에 의해 생성된 native code를 포함
  - JVM은 동적으로 interpreter loop, JNI(Java Native Interface) stubs, JIT(Just-in-Time) 컴파일러에 의해 native code로 컴파일되는 자바 메소드 등 다양한 이유로 native code 생성
    - JIT 컴파일러는 CodeCache 영역의 주요 기여자
- **`ThreadStackSize`**`
  - `-XX:ThreadStackSize=<size>` 옵션을 사용하여 스레드 스택 크기를 바이트 단위로 설정
    - 킬로바이트를 나타내려면 k 또는 K, 메가바이트를 나타내면 m 또는 M, 기가바이트를 나타내면 g 또는 G 추가
    - `-Xss=<size>`로도 지정 가능
    - 기본값은 기본 운영 체제와 아키텍처에 의존

## How to check the thread stack size

thread stack size 확인

```bash
$ jinfo -flag ThreadStackSize JAVA_PID
```

default thread stack size 확인

```bash
$ java -XX:+PrintFlagsFinal -version |grep ThreadStackSize
```

# How to choose the best Java garbage collector

GC를 선택하고 조정하여 자바 성능을 향상시키는 방법

## Choosing a garbage collector 

변수를 정의하고 사용할 때 동적으로 메모리를 할당
- JVM은 새로운 변수에 대한 요청에 따라 운영 체제로부터 메모리를 할당하여 제공
- 백그라운드 스레드에서 실행되는 GC는 애플리케이션이 여전히 참조하는 메모리 부분을 결정하고, 애플리케이션 재사용을 위해 참조되지 않은 메모리를 회수

자바는 다양한 애플리케이션 요구를 충족시키기 위해 많은 GC를 제공
- 적합한 GC의 선택은 성능에 큰 영향
- GC의 선택의 필수적인 기준
  - `Throughput`: 메모리 할당 및 GC 대비 응용 프로그램 활동에 소요된 총 시간의 백분율
    - 처리량이 95%인 경우 애플리케이션 코드가 95% 실행되고 가비지 컬렉션이 5% 실행되고 있음을 의미
    - 고부하 비즈니스 애플리케이션에 대한 처리율이 높아지기를 원할 경우
  - `Latency`(Stop-The-World): GC의 영향을 받는 애플리케이션 응답 일시 중지
    - 사람이나 어떤 활성 프로세스와 상호 작용하는 모든 애플리케이션에서 가능한 가장 낮은 지연 시간을 원할 경우
  - `Footprint`: 페이지 및 캐시 라인 단위로 측정되는 프로세스의 작업 집합

.

사용자와 애플리케이션마다 요구 사항이 다르다.
- 어떤 사람들은 더 높은 처리량을 원하고 교환 시 더 긴 대기 시간을 견딜 수 있다.
- 반면, 어떤 사람들은 아주 짧은 일시 정지 시간이라도 사용자 경험에 부정적인 영향을 미칠 수 있기 때문에 낮은 대기 시간을 필요로 한다.
- 물리적 메모리가 제한적이거나 프로세스가 많은 시스템에서는 `footprint`이 확장성을 좌우할 수 있다.

요구 사항에 따른 GC 비교
- Serial collector
- Parallel collector
- Garbage-first (G1) collector
- Z collector
- Shenandoah collector
- Concurrent Mark Sweep (CMS) collector (deprecated)

### Serial collector

모든 작업을 하나의 스레드에서 수행하는 GC

- `하나의 스레드를 사용`하면 여러 스레드 간에 통신 오버헤드가 없기 때문에 효율성을 향상
- 대기 상태를 견딜 수 있고, 매우 작은 힙을 생성하는 단일 프로세서 시스템 응용 프로그램에 가장 적합
- 데이터 세트가 작은 응용 프로그램의 경우 멀티프로세서 시스템에서 Serial collector 사용 가능
- `generational GC`에 해당하고, 모든 개체들의 집합을 세대로 나누고, 하나 이상의 세대에 있는 모든 개체들을 한 번의 패스로 수집
- 특정 하드웨어 및 운영 체제 구성에서 기본적으로 선택
  - 컴파일러 옵션을 사용하여 명시적으로 실행도 가능
  - `-XX:+UseSerialGC`

### Parallel collector

- 지연 시간보다 `처리량이 더 중요`한 경우가 많기 때문에 처리량 수집기라고도 불림
  - 대량 데이터 처리, 배치 작업 등과 같이 긴 대기가 허용될 경우 사용
- Serial collector 와 마찬가지로 `generational GC`
  - 주요 차이점은 Parallel collector가 GC 수집 속도를 높이기 위해 `여러 개의 스레드`를 실행
- 애플리케이션 요구사항이 처리량을 최대로 하고 1초 이상의 대기가 허용되는 경우 Parallel collector가 적합
  - Parallel collector는 멀티프로세서 또는 멀티 스레드 시스템에서 실행되는 중간 크기에서 큰 크기의 데이터 세트를 가진 애플리케이션에 사용 가능

.

**Parallel collector 활성화**

- `-XX:+UseParallelGC`
- 추가 컴파일러 옵션을 통해 몇 가지 매개 변수 구성 가능
  - `-XX:ParallelGCThreads=n`은 GC 스레드 수 지정
  - `-XX:MaxGCPauseMillis=n`은 최대 대기 시간을 밀리초 단위로 지정
    - 기본적으로 대기 시간에는 제한이 없지만 옵션 사용 시 n 이하의 대시 시간 예상
  - `-XX:GCTimeRatio=n`은 응용 프로그램의 목표 처리량 설정
    - GC에 사용되는 시간을 1/(1+n) 비율로 설정
    - ex. `-XX:GCTimeRatio=24`는 1/25의 목표를 설정하므로 전체 시간의 4%가 GC에 사용
    - 기본값은 99, GC에 사용되는 시간은 1%

[The Parallel Collector](https://docs.oracle.com/en/java/javase/11/gctuning/parallel-collector1.html#GUID-DCDD6E46-0406-41D1-AB49-FB96A50EB9CE)

### Garbage-first (G1) collector

[G1](https://docs.oracle.com/en/java/javase/11/gctuning/introduction-garbage-collection-tuning.html#GUID-326EB4CF-8C8C-4267-8355-21AB04F0D304)은 메모리 양이 많은 멀티프로세서 머신을 위해 설계된 서버 스타일 수집기
- `짧은 대기 시간`과 함께 `높은 처리량` 달성 시도를 하며, 튜닝 작업은 거의 불필요
- 특정 하드웨어 및 운영 체제에서 기본적으로 선택되며 `-XX:+UseG1GC` 옵션을 통해 명시적으로 활성화 가능

G1은 애플리케이션과 동시에 비싼 작업을 수행하기 때문에 동시 수집기라고 불림
- G1은 egionalized and generational garbage collector 로, 힙이 동일한 크기의 여러 영역으로 분할
- 시작 시 JVM은 영역 크기를 설정하는데, 영역 크기는 힙 크기에 따라 1MB~32MB까지 달라질 수 있음(목표는 2048개 이하의 영역을 갖는 것)
- Eden, Survivor, Old generations 는 이들 영역의 논리 집합이며, 인접하지 않음

G1 수집기는 아래 기준 중 하나 이상을 충족하는 애플리케이션에 대해 높은 처리량과 낮은 대기 시간을 달성
- 대용량 힙 크기: 구체적으로 라이브 오브젝트가 50% 이상 차지하는 경우 6GB 이상일 경우
- 애플리케이션 실행 중에 크게 달라질 수 있는 GC 세대 간 할당 및 촉진 비율
- 힙에 대량의 조각이 들어있는 경우
- 대기를 수백 밀리초로 제한해야 할 경우

.

문자열 중복 제거
- `-XX:+UseStringDeduplication` 사용
- JDK 8부터 G1 수집기는 [문자열 중복 제거](https://openjdk.org/jeps/192)를 통해 또 다른 최적화 기능을 제공하여 응용 프로그램의 힙 사용량이 약 10% 감소
- `-XX:+UseStringDeduplication` 컴파일러 옵션을 사용하면 G1 수집기가 중복 문자열을 찾아서 하나의 문자열에 대한 단일 활성 참조를 유지하는 동시에 중복에 대한 GC 수행
- 현재 문자열 중복 제거를 지원하는 Java 가비지 컬렉션은 없음
- 테스트 환경에서 메모리 사용량을 줄일 수 있는지 확인한 후 운영 시 옵션을 활성화하는 것을 권장

.

그밖에 G1 컴파일러 옵션
- `-XX:+UseG1GC`: G1 GC 활성화
- `-XX:+UseStringDeduplication`: 문자열 중복 제거 활성화
- `-XX:+PrintStringDeduplicationStatistics`: 이전 옵션과 함께 실행되는 경우 세부적인 복제 통계 출력
- `-XX:StringDeduplicationAgeThreshold=n`: n개의 GC 주기에 도달하는 문자열 개체를 중복제거 대상으로 간주(default: 3)

> [Introduction to the G1 Garbage Collector](https://www.redhat.com/en/blog/part-1-introduction-g1-garbage-collector?extIdCarryOver=true&sc_cid=701f2000001OH6pAAG)
> 
> [Collecting and reading G1 garbage collector logs](https://www.redhat.com/en/blog/collecting-and-reading-g1-garbage-collector-logs-part-2?extIdCarryOver=true&sc_cid=701f2000001OH6pAAG)
> 
> [G1 Collector Tuning](https://access.redhat.com/solutions/2162391?extIdCarryOver=true&sc_cid=701f2000001OH6pAAG)

### Z Garbage Collector (ZGC)

[ZGC](https://docs.oracle.com/en/java/javase/11/gctuning/z-garbage-collector1.html#GUID-A5A42691-095E-47BA-B6DC-FB4E5FAA43D0)는 `대기 시간이 짧은 GC`로 `매우 큰(멀티 테라바이트) 힙에서` 잘 작동
- G1과 마찬가지로 애플리케이션과 동시에 작동
- concurrent, single-generation, region-based, NUMA-aware, and compacting 기능 보유
- 애플리케이션 스레드 실행을 10ms 이상의 중지하지 않음

매우 짧은 대기 시간을 필요로 하는 매우 큰 양의 메모리를 가진 응용 프로그램에 적합
- 실험 기능으로 사용할 수 있으며 `-XX:+UnlockExperimentalVMOptions -XX:+UseZGC` 명령어로 활성화

최대 힙 크기를 설정하는 것은 ZGC 사용 시 매우 중요
- 수집기의 동작이 할당 속도 분산과 데이터 세트의 실시간 양에 따라 달라지기 때문
- 더 큰 힙에서 더 잘 작동하지만, 불필요한 메모리를 낭비하는 것도 비효율적이므로 메모리 사용량과 GC에 사용할 수 있는 리소스 간의 균형 조정 필요

ZGC의 동시 GC 스레드
- 동시 GC 스레드의 수 역시 ZGC 튜닝에 중요한 값
- `XX:ConcGCThreads=n` 옵션을 통해 동시 GC 수 설정
  - GC에 제공되는 CPU 시간 설정
  - 기본적으로 ZGC는 실행할 스레드의 수를 자동으로 선택하며, 일부 응용 프로그램에서는 별도 조정 필요
  - 너무 많은 스레드 설정 시 CPU가 많이 사용되는 반면, 너무 적은 스레드 설정 시 가비지는 수집기보다 더 빨리 생성

### Shenandoah collector

[Shenandoah](https://developers.redhat.com/articles/2021/09/16/shenandoah-openjdk-17-sub-millisecond-gc-pauses)는 `대기 시간이 매우 짧은 또 다른 GC`
- 동시 압축을 포함하여 더 많은 GC 작업을 응용 프로그램과 동시에 수행하여 대기 시간 감축(대기 시간은 힙 크기와 무관)
- 2GB 힙 또는 200GB 힙을 수집하는 가비지는 비슷한 대기 동작을 가져야 함
- **힙 크기 요구 사항에 관계없이 응답성과 짧은 대기 시간이 필요한 애플리케이션에 가장 적합**
- `-XX:+UseShenandoahGC` 컴파일러 옵션을 통해 활성화

### Concurrent Mark Sweep (CMS) collector (deprecated)

JDK 9 시점에서 더 이상 사용되지 않으며 G1 수집기 사용 권장

- [CMS Collector](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/cms.html)는 GC 대기 시간이 짧고 애플리케이션이 실행되는 동안 GC와 프로세서 리소스를 공유할 수 있는 응용 프로그램에서 선호되었음
- long-lived tenured generation이 높고 응용 프로그램이 둘 이상의 프로세서를 사용하는 시스템에서 실행될 때 더 많은 이점을 제공
- `-XX:+UseConcMarkSweepGC` 컴파일러 옵션을 통해 활성화

generational GC로서, tenured generations 수집
- GC(특히 mark-and-sweep 작업)를 응용 프로그램 스레드와 동시에 수행함으로써 응용 프로그램에서 짧은 대기 시간 보장
- 그러나 구 세대가 채워지기 전에 CMS 수집기가 참조되지 않은 개체를 지울 수 없거나, 구 세대에서 사용 가능한 공간으로 개체 할당을 만족시킬 수 없는 경우 CMS는 GC를 수행하기 위해 모든 응용 프로그램 스레드를 중지
- CMS GC가 GC을 동시에 완료할 수 없는 상태를 동시 모드 실패라고 하며 CMS 수집기에 대한 매개 변수 조정이 중요

---

**Reference**

[Part 1. explained the basics of garbage collection](https://developers.redhat.com/articles/2021/08/20/stages-and-levels-java-garbage-collection)
[Part 2. delved into memory usage by the Java Virtual Machine](https://developers.redhat.com/articles/2021/09/09/how-jvm-uses-and-allocates-memory)
[Part 3. How to choose the best Java garbage collector](https://developers.redhat.com/articles/2021/11/02/how-choose-best-java-garbage-collector#)