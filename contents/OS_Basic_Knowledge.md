# OS(Operating System, 운영체제)🌞

-   [backend-loadmap](https://github.com/kamranahmedse/developer-roadmap/blob/master/translations/korean/img/backend.png){:target="\_blank"} Part 03. OS

---

## 터미널 사용법 및 기본 명령🌟

-   grep, awk, sed, lsof, curl, wget, tail, head, less, find, ssh, kill

**Reference**

> [16 Terminal commands every user should know](https://www.techrepublic.com/article/16-terminal-commands-every-user-should-know/)
>
> [35 Linux Basic Commands Every User Should Know](https://www.hostinger.com/tutorials/linux-commands)
>
> [Linux 명령어 모음 Best 50](https://dora-guide.com/linux-commands/)

---

## OS의 일반적인 작동 원리🌟

### Operating System

-   Computer User와 Computer Hardware(CPU, I/O) 사이의 인터페이스 역할
-   자원이 필요한 프로그램에 자원을 할당해주고, 자원을 할당받은 프로그램들의 실행을 제어
-   운영체제가 하는 일
    -   Process management
    -   Memory management
    -   Storage management
    -   Protection and Security

### Works

-   기본적인 동작 원리로 OS는 사용자의 요청(Event 혹은 Interrupt, H/W interrupt or S/W interrupt)이 발생하면 적절하게 자원(CPU, I/O, 메모리 등)을 분배하여 그 요청을 처리
-   OS Loading
    1.  컴퓨터 전원이 켜지면 CPU는 ROM(Read Only Memory)에 저장된 내용을 읽는다.
        -   ROM에는 POST(Power ON Self-Test)와 부트로더(Boot Loader)가 저장
        -   POST : 컴퓨터 전원이 켜지면 가장 먼저 실행되는 프로그램으로 컴퓨터에 이상이 있는 지 체크
        -   Boot Loader : 하드디스크에 저장되어 있는 OS 프로그램을 가져와서 RAM에 넘겨주는 역할
    2.  CPU는 초기화를 위해 Boot Loader에 모인 명령들을 읽고, Dist(SSD, HDD)에 있는 프로그램들(OS..)을 RAM(Random Access Memory)에 올린다.
    3.  프로그램들이 메모리에 올라오면 CPU가 해당 프로그램을 작동시킨다.

**Reference**

> [OS의 구조 및 원리](https://velog.io/@gimseonjin/OS%EC%9D%98-%EA%B5%AC%EC%A1%B0-%EB%B0%8F-%EC%9B%90%EB%A6%AC)

---

## 프로세스 관리🌟

-   `프로세스`: 메인 메모리에 할당되어 실행중인 상태인 프로그램
    -   `PCB(Process Control Block)`: 프로세스에 대한 모든 정보가 모여있는 곳
    -   프로세스의 상태, 프로세스 번호(PID), 해당 프로세스의 program counter(pc), register값, MMU정보, CPU점유 시간 등이 포함
    -   CPU는 한 프로세스가 종료될 때까지 수행하는 것이 아니라 여러 프로세스를 중간 중간에 바꿔가면서 수행
        -   그러므로 CPU는 수행중인 프로세스를 나갈 때, 이 프로세스의 정보를 어딘가에 저장하고 있어야 다음에 이 프로세스를 수행할 때 이전에 수행한 그 다음부터 이어서 작업할 수 있다.
        -   이러한 정보를 저장하는 곳이 PCB

**Reference**

> [프로세스 관리](https://velog.io/@codemcd/%EC%9A%B4%EC%98%81%EC%B2%B4%EC%A0%9COS-5.-%ED%94%84%EB%A1%9C%EC%84%B8%EC%8A%A4-%EA%B4%80%EB%A6%AC/)

---

## 스레드와 동시성🌟

-   `Thread` : 실행되는 흐름의 단위
    -   일반적으로 한 프로그램은 하나의 실행되는 흐름이 존재
    -   이 흐름이 여러개 있다면 **Multi-Thread-Programming**
-   `Process` : 컴퓨터에서 연속적으로 실행되고 있는 컴퓨터 프로그램
    -   여러개의 Processor(Process의 인칭화)를 사용하는 걸 **Multi-processing**
-   `Parallelism`(병렬성) : 애플리케이션에서 작업을 여러 CPU에서 동시에 병렬로 작업할 수 있는 Process 단위로 분할
    -   여러 개의 쓰레드가 있으면 데이터 및 리소스 측면에서 서로 독립적으로 유사한 작업을 처리
-   `Concurrency`(동시성): 서로 독립적인 작업을 작은 단위의 연산으로 나누고 시간 분할 형태로 연산하여 논리적으로 동시에 실행되는 것처럼 보여주는 것 (논리적인 개념이기 때문에 단일 쓰레드에서도 사용이 가능한 개념)

**Reference**

> [Concurrency and Thread](https://velog.io/@ssseungzz7/OS-concurrency-and-thread)

---

## 메모리 관리🌟

-   가장 단순한 형태의 메모리 관리 방법은 프로그램의 요청이 있을 때, 메모리의 일부를 해당 프로그램에 할당하고, 더 이상 필요하지 않을 때 나중에 다시 사용할 수 있도록 할당을 해제하는 것

**Reference**

> [메모리 관리](https://velog.io/@byunji_jump/%EC%9A%B4%EC%98%81%EC%B2%B4%EC%A0%9C-%EB%A9%94%EB%AA%A8%EB%A6%AC-%EA%B4%80%EB%A6%AC)

---

## IPC

-   프로세스 간 통신(Inter Process Communication)🌟
    -   프로세스는 본래 독립적이나, 상황에 따라 프로세스끼리 협력해야 하는 경우가 있다. 이 경우, 프로세스 간 자원/데이터 공유가 필요한데, 이때 필요한 것이 `IPC`
-   IPC 기법의 종류
    -   메시지 전달
    -   공유 메모리

**Reference**

> [프로세스 간 통신](https://rebas.kr/854)

---

## 입출력 관리🌟

-   여러 입출력 장치를 사용하기 위해 존재
-   입출력 장치의 성능 향상을 위해 Buffering, Caching, Spooling 사용
-   `Buffering` : CPU의 데이터 처리 속도와 입출력 장치의 데이터 전송 속도의 차이로 인한 문제 해결
    -   입출력 데이터 전송 시 일시적으로 Buffer에 데이터를 저장
    -   단일 버퍼링 : 저장과 처리가 동시에 X
    -   이중 버퍼링 : 저장과 처리가 동시에 O
    -   순환 버퍼링 : 이중 버퍼링 확장
-   `Spooling`
    -   Simultaneous Peripheral Operation On Lien([SPOOL](https://ko.wikipedia.org/wiki/%EC%8A%A4%ED%92%80%EB%A7%81))
    -   컴퓨터 시스템에서 중앙처리장치와 입출력장치가 독립적으로 동작하도록 함으로써 중앙처리장치에 비해 주변장치의 처리속도가 느려서 발생하는 대기시간을 줄이기 위해 고안된 기법
    -   입출력 프로세스와 저속 입출력장치 사이의 데이터 전송을 자기 디스크와 같은 고속 장치를 통하도록 함

**Reference**

> [운영체제 서비스](https://velog.io/@codemcd/%EC%9A%B4%EC%98%81%EC%B2%B4%EC%A0%9COS-4.-%EC%9A%B4%EC%98%81%EC%B2%B4%EC%A0%9C-%EC%84%9C%EB%B9%84%EC%8A%A4)
>
> [입출력 관리](https://3catpapa.tistory.com/114)

---

## POSIX 기초🌟

-   stdin, stdout, stderr, pipes ..
-   이식 가능 운영 체제 인터페이스(portable operating system interface)의 약자
-   서로 다른 UNIX OS의 공통 API를 정리하여 이식성이 높은 유닉스 응용 프로그램을 개발하기 위한 목적으로 IEEE가 책정한 애플리케이션 인터페이스 규격
-   [POSIX](https://ko.wikipedia.org/wiki/POSIX)

**Reference**

> [POSIX 기초](https://velog.io/@goban/POSIX-%EA%B8%B0%EC%B4%88)

---

## 네트워크 기본 개념🌟

**Reference**

> [네트워크 기본 개념(1)](https://you4-bimi.tistory.com/5)
>
> [네트워크의 기본 개념](https://securityspecialist.tistory.com/10)
>
> [Computer Network 기본 개념](https://solt.tistory.com/69)

[thumbnail](https://binarymove.com/2017/04/24/evolution-operating-systems/)