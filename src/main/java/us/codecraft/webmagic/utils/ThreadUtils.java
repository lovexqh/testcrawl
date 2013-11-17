package us.codecraft.webmagic.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import us.codecraft.webmagic.schedular.Scheduler;

/**
 * 线程工具类。<br>
 * 
 * @author code4crafer@gmail.com Date: 13-6-23 Time: 下午7:11
 */
public class ThreadUtils {
	static int threadnum = 4;

	public static <T> void process(Scheduler<T> scheduler,
			final ProcessHandler processHandler) {
		T t = scheduler.poll();
		// multi thread
		final AtomicInteger threadAlive = new AtomicInteger(0);
		ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(threadnum);
		while (true) {
			if (t == null) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			} else {
				final T t1 = t;
				threadAlive.incrementAndGet();
				executorService.execute(new Runnable() {
					@Override
					public void run() {
						processHandler.process(t1);
						threadAlive.decrementAndGet();
					}
				});
			}
			if (threadAlive.get() == 0) {
				t = scheduler.poll();
				if (t == null) {
					break;
				} else {
					continue;
				}
			}
			t = scheduler.poll();
		}
		executorService.shutdown();
	}

}
