package us.codecraft.webmagic.clawer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import us.codecraft.webmagic.fetcher.Destroyable;
import us.codecraft.webmagic.fetcher.Fetcher;
import us.codecraft.webmagic.fetcher.HttpClientFetcher;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.schedular.QueueScheduler;
import us.codecraft.webmagic.schedular.Scheduler;
import us.codecraft.webmagic.utils.ProcessHandler;
import us.codecraft.webmagic.utils.ThreadUtils;

/**
 * <pre>
 * webmagic爬虫的入口类。
 * 
 * 示例：
 * 定义一个最简单的爬虫：
 *      Spider.create(new SimplePageProcessor("http://my.oschina.net/", "http://my.oschina.net/*blog/*")).run();
 * 
 * 使用FilePipeline保存结果到文件:
 *      Spider.create(new SimplePageProcessor("http://my.oschina.net/", "http://my.oschina.net/*blog/*"))
 *          .pipeline(new FilePipeline("/data/temp/webmagic/")).run();
 * 
 * 使用FileCacheQueueScheduler缓存URL，关闭爬虫后下次自动从停止的页面继续抓取:
 *      Spider.create(new SimplePageProcessor("http://my.oschina.net/", "http://my.oschina.net/*blog/*"))
 *          .scheduler(new FileCacheQueueScheduler("/data/temp/webmagic/cache/")).run();
 * </pre>
 * 
 * @author code4crafter@gmail.com <br>
 *         Date: 13-4-21 Time: 上午6:53
 */
public class Spider implements Runnable, Task {

	private Fetcher downloader;

	private List<Pipeline> pipelines = new ArrayList<Pipeline>();

	private PageProcessor pageProcessor;

	private List<String> startUrls;

	private Site site;

	private String uuid;

	private Scheduler<Request> scheduler = new QueueScheduler();

	private Logger logger = Logger.getLogger(getClass());

	private ExecutorService executorService;

	private AtomicInteger stat = new AtomicInteger(STAT_INIT);

	private final static int STAT_INIT = 0;

	private final static int STAT_RUNNING = 1;

	private final static int STAT_STOPPED = 2;

	/**
	 * 使用已定义的抽取规则新建一个Spider。
	 * 
	 * @param pageProcessor
	 *            已定义的抽取规则
	 */
	public Spider(PageProcessor pageProcessor) {
		this.pageProcessor = pageProcessor;
		this.site = pageProcessor.getSite();
		this.startUrls = pageProcessor.getStartUrls();
	}

	/**
	 * 使用已定义的抽取规则新建一个Spider。
	 * 
	 * @param pageProcessor
	 *            已定义的抽取规则
	 * @return 新建的Spider
	 */
	public static Spider create(PageProcessor pageProcessor) {
		return new Spider(pageProcessor);
	}

	/**
	 * 重新设置startUrls，会覆盖Site本身的startUrls。
	 * 
	 * @param startUrls
	 * @return this
	 */
	public Spider startUrls(List<String> startUrls) {
		checkIfNotRunning();
		this.startUrls = startUrls;
		return this;
	}

	/**
	 * 为爬虫设置一个唯一ID，用于标志任务，默认情况下使用domain作为uuid，对于单domain多任务的情况，请为重复任务设置不同的ID。
	 * 
	 * @param uuid
	 *            唯一ID
	 * @return this
	 */
	public Spider setUUID(String uuid) {
		this.uuid = uuid;
		return this;
	}

	/**
	 * 设置调度器。调度器用于保存待抓取URL，并可以进行去重、同步、持久化等工作。默认情况下使用内存中的阻塞队列进行调度。
	 * 
	 * @param scheduler
	 *            调度器
	 * @return this
	 */
	public Spider scheduler(Scheduler<Request> scheduler) {
		checkIfNotRunning();
		this.scheduler = scheduler;
		return this;
	}

	/**
	 * 设置处理管道。处理管道用于最终抽取结果的后处理，例如：保存到文件、保存到数据库等。默认情况下会输出到控制台。
	 * 
	 * @param pipeline
	 *            处理管道
	 * @return this
	 */
	public Spider pipeline(Pipeline pipeline) {
		checkIfNotRunning();
		this.pipelines.add(pipeline);
		return this;
	}

	public Spider downloader(Fetcher downloader) {
		checkIfNotRunning();
		this.downloader = downloader;
		return this;
	}

	protected void checkComponent() {
		if (downloader == null) {
			this.downloader = new HttpClientFetcher();
		}
		if (pipelines.isEmpty()) {
			pipelines.add(new ConsolePipeline());
		}
	}

	@Override
	public void run() {
		if (!stat.compareAndSet(STAT_INIT, STAT_RUNNING)) {
			throw new IllegalStateException("Spider is already running!");
		}
		checkComponent();
		if (startUrls != null) {
			for (String startUrl : startUrls) {
				scheduler.push(new Request(startUrl,this.getSite()));
			}
		}
		ThreadUtils.process(scheduler, new ProcessHandler<Request>() {
			
			@Override
			public  void process(Request request) {
				// TODO Auto-generated method stub
				Page page = downloader.fetchPage(request);
				if (page == null) {
					sleep(site.getSleepTime());
					return;
				}
				pageProcessor.process(page);
				addRequest(page);
				for (Pipeline pipeline : pipelines) {
					pipeline.process(page.getResultItems());
				}
				sleep(site.getSleepTime());
			}
		});
		stat.compareAndSet(STAT_RUNNING, STAT_STOPPED);
		// release some resources
		destroy();
	}

	private void destroy() {
		destroyEach(downloader);
		destroyEach(pageProcessor);
		for (Pipeline pipeline : pipelines) {
			destroyEach(pipeline);
		}
	}

	private void destroyEach(Object object) {
		if (object instanceof Destroyable) {
			((Destroyable) object).destroy();
		}
	}

	// private void processRequest(Request request) {
	// Page page = downloader.downloadPage(request, this);
	// if (page == null) {
	// sleep(site.getSleepTime());
	// return;
	// }
	// pageProcessor.process(page);
	// addRequest(page);
	// for (Pipeline pipeline : pipelines) {
	// pipeline.process(page.getResultItems());
	// }
	// sleep(site.getSleepTime());
	// }

	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void addRequest(Page page) {
		if (CollectionUtils.isNotEmpty(page.getTargetRequests())) {
			for (Request request : page.getTargetRequests()) {
				scheduler.push(request);
			}
		}
	}

	private void checkIfNotRunning() {
		if (!stat.compareAndSet(STAT_INIT, STAT_INIT)) {
			throw new IllegalStateException("Spider is already running!");
		}
	}

	public void runAsync() {
		Thread thread = new Thread(this);
		thread.setDaemon(false);
		thread.start();
	}

	/**
	 * 建立多个线程下载
	 * 
	 * @param threadNum
	 *            线程数
	 * @return this
	 */
	public Spider thread(int threadNum) {
		checkIfNotRunning();
		if (threadNum <= 0) {
			throw new IllegalArgumentException(
					"threadNum should be more than one!");
		}
		if (threadNum == 1) {
			return this;
		}
		synchronized (this) {
			this.executorService = Executors.newFixedThreadPool(threadNum);
		}
		return this;
	}

	@Override
	public String getUUID() {
		if (uuid != null) {
			return uuid;
		}
		if (site != null) {
			return site.getDomain();
		}
		return null;
	}

	@Override
	public Site getSite() {
		return site;
	}

	class Aa implements ProcessHandler<Request> {

		@Override
		public  void process(Request request) {
			// TODO Auto-generated method stub

		}

	}
}
