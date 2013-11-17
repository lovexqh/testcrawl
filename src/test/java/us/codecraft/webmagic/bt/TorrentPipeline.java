package us.codecraft.webmagic.bt;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import us.codecraft.webmagic.clawer.Page;
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
import us.codecraft.webmagic.utils.UrlUtils;

public class TorrentPipeline implements Pipeline {
	private Logger logger = Logger.getLogger(getClass());
	private Scheduler<Request> scheduler = new QueueScheduler();
	Fetcher downloader = new HttpClientFetcher();
	private Site torrentSite;

	@Override
	public void process(ResultItems resultItems) {
		checkSite();
		if (resultItems.isSkip()) {
			return;
		}
		List<String> torrent = resultItems.get(Constant.TORRENT);
		if (torrent == null || torrent.size() == 0)
			return;
		for (String url : torrent) {
			scheduler.push(new Request(url, torrentSite));
		}
		ThreadUtils.process(scheduler, new torrentHandler());
		// executorPool.shutdown();
	}


	class torrentHandler implements ProcessHandler<Request> {

		@Override
		public void process(Request request) {
			logger.info("downloading page " + request.getUrl());
			Page page = downloader.fetchPage(request);
			// page;
			if (page != null && page.getHtml() != null) {

				String title = page.getHtml().xpath("//title").toString();
				String torrentUrl = page.getHtml().xpath("//form/@action")
						.toString();
				torrentUrl = UrlUtils.canonicalizeUrl(torrentUrl,
						request.getUrl());
				Request torrentReq = new Request(torrentUrl, torrentSite);
				torrentReq.setParam("type", "torrent");
				torrentReq.setParam("id", title);
				torrentReq.setParam("name", title);
				torrentReq.setHeader("Referer", request.getUrl());
				torrentReq.setType(Constant.REQUEST_POST);
				downloader.fetchPage(torrentReq);
			}
		}
	}

	private void checkSite() {
		if (this.torrentSite == null) {
			torrentSite = Site.me();
		}
	}
}
