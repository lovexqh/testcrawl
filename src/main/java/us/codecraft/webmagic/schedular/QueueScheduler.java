package us.codecraft.webmagic.schedular;

import org.apache.log4j.Logger;
import us.codecraft.webmagic.clawer.Request;
import us.codecraft.webmagic.clawer.Task;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 内存队列实现的线程安全Scheduler。<br>
 * 
 * @author code4crafter@gmail.com <br>
 *         Date: 13-4-21 Time: 下午1:13
 */
public class QueueScheduler implements Scheduler<Request> {

	private Logger logger = Logger.getLogger(getClass());

	private BlockingQueue<Request> queue = new LinkedBlockingQueue<Request>();

	private Set<String> urls = new HashSet<String>();

	@Override
	public void push(Request request) {
		if (urls.add(request.getUrl())) {
			queue.add(request);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("push to queue " + request.getUrl());
		}

	}

	@Override
	public Request poll() {
		try {
//			return queue.take();
			return queue.poll(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
}
