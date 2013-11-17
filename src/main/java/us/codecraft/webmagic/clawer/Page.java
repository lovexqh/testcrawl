package us.codecraft.webmagic.clawer;

import org.apache.commons.lang3.StringUtils;

import us.codecraft.webmagic.selector.Selectable;
import us.codecraft.webmagic.utils.UrlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <pre>
 * Page保存了上一次抓取的结果，并可定义待抓取的链接内容。
 * 
 *      主要方法：
 *      {@link #getUrl()} 获取页面的Url
 *      {@link #getHtml()}  获取页面的html内容
 *      {@link #putField(String, us.codecraft.webmagic.selector.Selectable)} 保存抽取的结果
 *      {@link #getFields()} 获取抽取的结果，在 {@link us.codecraft.webmagic.pipeline.Pipeline} 中调用
 *      {@link #addTargetRequests(java.util.List)} {@link #addTargetRequest(String)} 添加待抓取的链接
 * 
 * </pre>
 * 
 * @author code4crafter@gmail.com <br>
 */
public class Page {

	private Request request;

	private ResultItems resultItems = new ResultItems();

	private Selectable html;

	private Selectable url;

	private LinkedBlockingQueue<Request> targetRequests = new LinkedBlockingQueue<Request>();

	public Page() {
	}

	/**
	 * 保存抽取的结果
	 * 
	 * @param key
	 *            结果的key
	 * @param field
	 *            结果的value
	 */
	public void putField(String key, Object field) {
		resultItems.put(key, field);
	}

	/**
	 * 获取页面的html内容
	 * 
	 * @return html 页面的html内容
	 */
	public Selectable getHtml() {
		return html;
	}

	public void setHtml(Selectable html) {
		this.html = html;
	}

	public LinkedBlockingQueue<Request> getTargetRequests() {
		return targetRequests;
	}

	/**
	 * 添加待抓取的链接
	 * 
	 * @param requests
	 *            待抓取的链接
	 */
	public void addTargetRequests(List<String> requests,Site site) {
		for (String s : requests) {
			if (StringUtils.isBlank(s) || s.equals("#")
					||s.startsWith("javascript:")) {
				break;
			}
			
			String request = UrlUtils.canonicalizeUrl(s, url.toString());
			targetRequests.add(new Request(request, site));
		}
	}

	/**
	 * 添加待抓取的链接
	 * 
	 * @param requestString
	 *            待抓取的链接
	 */
	public void addTargetRequest(String requestString,Site site) {
		if (StringUtils.isBlank(requestString) || requestString.equals("#")) {
			return;
		}
		requestString = UrlUtils.canonicalizeUrl(requestString, url.toString());
		targetRequests.add(new Request(requestString,site));
	}

	/**
	 * 添加待抓取的页面，在需要传递附加信息时使用
	 * 
	 * @param request
	 *            待抓取的页面
	 */
	public void addTargetRequest(Request request) {
		targetRequests.add(request);
	}

	/**
	 * 获取页面的Url
	 * 
	 * @return url 当前页面的url，可用于抽取
	 */
	public Selectable getUrl() {
		return url;
	}

	/**
	 * 设置url
	 * 
	 * @param url
	 */
	public void setUrl(Selectable url) {
		this.url = url;
	}

	/**
	 * 获取抓取请求
	 * 
	 * @return request 抓取请求
	 */
	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
		this.resultItems.setRequest(request);
	}

	public ResultItems getResultItems() {
		return resultItems;
	}
}
