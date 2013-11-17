package us.codecraft.webmagic.clawer;


import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import us.codecraft.webmagic.utils.Constant;


/**
 * Request对象封装了待抓取的url信息。<br/>
 * 在PageProcessor中，Request对象可以通过{@link us.codecraft.webmagic.Page#getRequest()}
 * 获取。<br/>
 * <br/>
 * Request对象包含一个extra属性，可以写入一些必须的上下文，这个特性在某些场合会有用。<br/>
 * 
 * <pre>
 *      Example:
 *          抓取<a href="${link}">${linktext}</a>时，希望提取链接link，并保存linktext的信息。
 *      在上一个页面：
 *      public void process(Page page){
 *          Request request = new Request(link,linktext);
 *          page.addTargetRequest(request)
 *      }
 *      在下一个页面：
 *      public void process(Page page){
 *          String linktext =  (String)page.getRequest().getExtra()[0];
 *      }
 * </pre>
 * 
 * @author code4crafter@gmail.com <br>
 *         Date: 13-4-21 Time: 上午11:37
 */
public class Request {
	private String type = Constant.REQUEST_GET;
	private String url;
	private Site site;

	private List<NameValuePair> params = new ArrayList<NameValuePair>();
	private List<Header> headers = new ArrayList<Header>();

	/**
	 * 构建一个request对象
	 * 
	 * @param url
	 *            必须参数，待抓取的url
	 * @param extra
	 *            额外参数，可以保存一些需要的上下文信息
	 */
	private Request(String url) {
		this.url = url;
	}
	public Request(String url,Site site) {
		this.url = url;
		this.site=site;
	}
	public Request(String url, List<NameValuePair> params) {
		this.url = url;
		this.params = params;
	}

	/**
	 * 获取预存的对象
	 * 
	 * @return object[] 预存的对象数组
	 */
	public List<NameValuePair> getParams() {
		return params;
	}

	public void setParam(String key, String value) {
		getParams().add(new BasicNameValuePair(key, value));
	}

	/**
	 * 获取待抓取的url
	 * 
	 * @return url 待抓取的url
	 */
	public String getUrl() {
		return url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Header> getHeaders() {
		return headers;
	}

	public void setHeader(String name, String value) {
		headers.add(new BasicHeader(name, value));
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}
	public void setUrl(String url) {
		this.url = url;
	}

}
