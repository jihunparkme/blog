import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreading {

	public static void main(String[] args) throws IOException {

		long start = System.currentTimeMillis();
		int threadCount = 5;

		AtomicInteger alreadyIndexed = new AtomicInteger();
		LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>(threadCount);
		ExecutorService excutor = Executors.newFixedThreadPool(threadCount + 1);
		CountDownLatch await = new CountDownLatch(threadCount + 1);

		excutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					for (int i = 1; i <= 10; i++) { // 작업 리스트
						queue.put("work " + i); // Queue에 작업 목록 삽입 (Queue가 꽉 찼을 경우 대기)
						
						// do something
						System.out.println("Producer >> " + Thread.currentThread().getName() + " take: " + i + ", remain: " + queue.size());
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally { // 모든 작업 리스트가 Queue에 삽입
					try {
						while (await.getCount() > 1) {
							queue.put("empty"); // 각 Thread에 모든 작업이 끝났다는 표식을 남기자. (인터럽트 여부 혹은 종료 메시지를 보내는 방법도 존재)
							Thread.sleep(1000);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					await.countDown(); // Letch Count를 감소시키고 Count가 0에 도달하면 대기 중인 모든 스레드를 해제
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
							System.out.println("Consumer Start >> " + Thread.currentThread().getName() + " take: " + buffer + ", remain: " + queue.size());

							// do something
							Thread.sleep(10000);

							System.out.println("Consumer End >> " + Thread.currentThread().getName() + "  take: " + buffer);
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