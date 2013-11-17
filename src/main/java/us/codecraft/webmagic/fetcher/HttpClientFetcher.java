package us.codecraft.webmagic.fetcher;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import us.codecraft.webmagic.clawer.Page;
import us.codecraft.webmagic.clawer.Request;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.HttpClientUtil;
import us.codecraft.webmagic.utils.UrlUtils;

/**
 * 封装了HttpClient的下载器。已实现指定次数重试、处理gzip、自定义UA/cookie等功能。<br>
 * 
 * @author code4crafter@gmail.com <br>
 *         Date: 13-4-21 Time: 下午12:15
 */
public class HttpClientFetcher implements Fetcher {

	private static Logger logger = Logger.getLogger(HttpClientFetcher.class);

	public Page fetchPage(Request request) {
		logger.info("downloading page " + request.getUrl());

		HttpResponse httpResponse = HttpClientUtil.getInstance().execute(
				request);
		if (httpResponse == null)
			return null;
		if (isHtml(httpResponse)) {
			return parseHtml(request, httpResponse);
		} else {
			downLoadFile(request, httpResponse);
			return null;
		}

	}

	private Page parseHtml(Request request, HttpResponse httpResponse) {
		Page page = new Page();
		String charset = null;
		if (charset == null
				&& httpResponse.getEntity().getContentType() != null) {
			String value = httpResponse.getEntity().getContentType().getValue();
			charset = UrlUtils.getCharset(value);
		}
		if (charset == null) {
			charset = request.getSite().getCharset();
		}
		if (charset == null) {
			charset = "utf-8";
		}
		String content = "";
		try {
			content = EntityUtils.toString(httpResponse.getEntity(), charset);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		page.setHtml(new Html(UrlUtils.fixAllRelativeHrefs(content,
				request.getUrl())));
		page.setUrl(new PlainText(request.getUrl()));
		page.setRequest(request);
		return page;
	}

	private void downLoadFile(Request request, HttpResponse httpResponse) {
		String fileName = "";
		HeaderElement[] elements = httpResponse.getLastHeader(
				"Content-Disposition").getElements();
		for (HeaderElement headerElement : elements) {
			NameValuePair nameValueheader = headerElement
					.getParameterByName("filename");
			if (nameValueheader != null) {
				fileName = nameValueheader.getValue();
			}
		}
		String url = UrlUtils
				.canonicalizeUrl("./" + fileName, request.getUrl());
		System.out.println(url);
		File file = UrlUtils.Url2Path(url);
		try {
			FileUtils.writeByteArrayToFile(file,
					EntityUtils.toByteArray(httpResponse.getEntity()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isHtml(HttpResponse httpResponse) {
		Header header = httpResponse.getEntity().getContentType();
		return header == null || header.getValue().contains("text/html");
	}

}
