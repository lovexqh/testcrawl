package us.codecraft.webmagic.bt;

import java.util.ArrayList;
import java.util.List;

import us.codecraft.webmagic.clawer.Page;
import us.codecraft.webmagic.clawer.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.Constant;

/**
 * 非常简单的抽取器。链接抽取使用定义的通配符，并保存抽取整个内容到content字段。<br>
 * 
 * @author code4crafter@gmail.com <br>
 *         Date: 13-4-22 Time: 下午9:15
 */
public class BtProcesser implements PageProcessor {

	// private String urlPattern;

	private static final String UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31";
	// private static String startUrl =
	// "http://img1.gtimg.com/nice_mb/3e/26/26893.jpg";
	// private static String startUrl =
	// "http://www3.17domn.com/bt9/file.php/MRMPZ8S.html";
	private static String startUrl = "http://97.13bt.info";
	private static String urlPattern = startUrl + "/*";
	// private static String torrentPattern = startUrl+"/*";
	static {
		urlPattern = convertPattern(urlPattern);

	}

	@Override
	public void process(Page page) {
		List<String> requests = page.getHtml().links().regex(urlPattern).all();
		// 调用page.addTargetRequests()方法添加待抓取链接
		page.addTargetRequests(requests, page.getRequest().getSite());
		// xpath方式抽取
		page.putField(Constant.TITLE, page.getHtml().xpath("//title")
				.toString());
		// sc表示使用Readability技术抽取正文
		page.putField(Constant.HTML, page.getHtml().toString());
		page.putField(Constant.TORRENT,
				page.getHtml().links().regex(convertPattern("*file.php*"))
						.all());
		page.putField(Constant.IMAGE, page.getHtml().xpath("//IMG/@src").all());
		page.putField(Constant.CONTENT, page.getHtml().smartContent()
				.toString());
	}

	@Override
	public Site getSite() {
		return Site.me().setCharset("gb2312").setUserAgent(UA);
	}

	private static String convertPattern(String pattern) {
		return "(" + pattern.replace(".", "\\.").replace("*", "[^\"'#]*") + ")";
	}

	@Override
	public List<String> getStartUrls() {
		// TODO Auto-generated method stub
		List<String> urls = new ArrayList<String>();
		urls.add(startUrl);
		return urls;
	}
}
