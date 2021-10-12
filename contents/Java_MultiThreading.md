# Java MultiThreading

## AtomicInteger

- Integer 자료형을 가지고 있는 wrapping 클래스
- 멀티쓰레드 환경에서 동시성을 보장 (synchronized 보다 적은 비용으로 동시성을 보장)

**Reference**

> https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/AtomicInteger.html
>
> https://codechacha.com/ko/java-atomic-integer/

---

## LinkedBlockingQueue

- 한정된 메모리 사용을 위해 초기 Queue Size 세팅
- Queue Data(empty/full) 상태에 따라 Thread가 Blocking 할 수 있는 기능 제공
- Queue에서 아이템을 가져올 때 비어있으면 null을 리턴하지 않고 아이템이 추가될 때까지 대기
- 반대로, 아이템을 추가할 때 Queue가 가득차 있으면 공간이 생길 때까지 기다
- 내부적으로 동시성에 안전하기 때문에 synchronized 구문 없이 사용 가능

**Reference**

> https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/LinkedBlockingQueue.html
>
> https://codechacha.com/ko/java-arrayblockingqueue/

---

## ExecutorService

- 비동기 작업의 진행 상황을 추적하기 위해 생성할 수 있는 메서드 제공
- 스레드 풀의 개수 및 종류를 정하여 생성하고 사용할 수 있도록 제공

**Reference**

> https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html
>
> https://codechacha.com/ko/java-executors/

---

## CountDownLatch

- 하나 이상의 스레드가 다른 스레드에서 수행 중인 일련의 작업이 완료될 때까지 대기할 수 있도록 하는 동기화 지원

**Reference**

> https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/CountDownLatch.html
>
> https://codechacha.com/ko/java-countdownlatch/

---

## Sample Code

```java
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ttt {

	public static void main(String[] args) throws IOException {

		long start = System.currentTimeMillis();
		int threadCount = 10;

		AtomicInteger alreadyIndexed = new AtomicInteger();
		LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>(threadCount);
		ExecutorService excutor = Executors.newFixedThreadPool(threadCount + 1);
		CountDownLatch await = new CountDownLatch(threadCount + 1);

		excutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					for (int i = 1; i <= 10; i++) {
						queue.put("work " + i); // Queue에 요소 삽입 (Queue가 꽉 찼을 경우 대기)
						System.out.println("Producer >> " + Thread.currentThread().getName() + " take: " + i + ", remain: " + queue.size() +  "work ");

						// do something
						Thread.sleep(1000);

					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					try {
						while (await.getCount() > 1) {
							queue.put("empty"); // 각 Thread에 모든 작업이 끝났다는 표식을 남기자. (인터럽트 여부 혹은 종료 메시지를 보내는 방법도 존재)
							Thread.sleep(1000);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					await.countDown();
				}
			}
		});

		for (int i = 0; i < threadCount; i++) {
			excutor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						while (true) {
							String buffer = queue.take(); // Queue head 요소를 꺼낸다. (Queue가 비어있다면 대기)
							if ("empty".equals(buffer)) { // 모든 작업이 끝났다는 표식이 있다면 해당 Thread 종료
								break;
							}
							System.out.println("Consumer >> " + Thread.currentThread().getName() + " take: " + buffer + ", remain: " + queue.size());
							// do something
							Thread.sleep(10000);
							System.out.println("Consumer >> " + Thread.currentThread().getName() + "  take: " + buffer + " EEEEEEEnd");
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						System.out.println("thread end...");
						await.countDown(); // Letch Count를 감소시키고 Count가 0에 도달하면 대기 중인 모든 스레드를 해제
					}
				}
			});
		}

		try {
			await.await(); //현재 스레드 Latch가 0으로 카운트다운 될 때까지 기다린다.
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("already indexed: " + alreadyIndexed.get());

		long end = System.currentTimeMillis();
		System.out.println("전체 실행 시간 : " + ( end - start )/1000.0/60.0 + " min (" + ( end - start )/1000.0  + " sec)");
	}
}
```
