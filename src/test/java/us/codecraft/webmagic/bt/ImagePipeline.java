package us.codecraft.webmagic.bt;

import java.util.List;

import org.apache.log4j.Logger;

import us.codecraft.webmagic.clawer.Request;
import us.codecraft.webmagic.clawer.ResultItems;
import us.codecraft.webmagic.clawer.Site;
import us.codecraft.webmagic.fetcher.Fetcher;
import us.codecraft.webmagic.fetcher.HttpClientFetcher;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.schedular.QueueScheduler;
import us.codecraft.webmagic.schedular.Scheduler;
import us.codecraft.webmagic.utils.Constant;
import us.codecraft.webmagic.utils.ProcessHandler;
import us.codecraft.webmagic.utils.ThreadUtils;

public class ImagePipeline implements Pipeline {
	private Logger logger = Logger.getLogger(getClass());
	private Scheduler<Request> scheduler = new QueueScheduler();
	private Site imageSite = new Site();

	@Override
	public void process(ResultItems resultItems) {
		if (resultItems.isSkip()) {
			return;
		}
		List<String> images = resultItems.get(Constant.IMAGE);
		if (images == null || images.size() == 0)
			return;
		for (String url : images) {
			scheduler.push(new Request(url, imageSite));
		}
		// multi thread
		final Fetcher downloader = new HttpClientFetcher();
		ThreadUtils.process(scheduler, new ProcessHandler<Request>() {
			@Override
			public void process(Request request) {
				downloader.fetchPage(request);
			}
		});
		// executorPool.shutdown();
	}
}
