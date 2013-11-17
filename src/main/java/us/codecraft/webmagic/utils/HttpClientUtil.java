package us.codecraft.webmagic.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.DefaultClientConnection;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;
import org.apache.http.impl.conn.DefaultResponseParser;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.util.CharArrayBuffer;
import org.apache.log4j.Logger;

import us.codecraft.webmagic.clawer.Request;
import us.codecraft.webmagic.clawer.Site;

/**
 * @author code4crafter@gmail.com <br>
 *         Date: 13-4-21 Time: 下午12:29
 */
public class HttpClientUtil {

	public static volatile HttpClientUtil INSTANCE;
	private int poolSize = 20;
	private Logger logger = Logger.getLogger(getClass());
	PoolingClientConnectionManager connectionManager;

	public static HttpClientUtil getInstance() {
		if (INSTANCE == null) {
			synchronized (HttpClientUtil.class) {
				if (INSTANCE == null) {
					INSTANCE = new HttpClientUtil();
				}
			}
		}
		return INSTANCE;
	}

	private HttpClientUtil() {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
				.getSocketFactory()));

		connectionManager = new MyPoolingClientConnectionManager(schemeRegistry);
		connectionManager.setMaxTotal(poolSize);
		connectionManager.setDefaultMaxPerRoute(100);
	}

	public HttpResponse execute(Request request) {
		// HttpClient httpClient = generateClient(request);
		HttpClient httpClient = generateClient(request.getSite());
		HttpResponse httpResponse = null;
		int tried = 0;
		boolean retry;
		do {
			try {
				httpResponse = httpClient.execute(generate(request));
				if (httpResponse != null) {
					int statusCode = httpResponse.getStatusLine()
							.getStatusCode();
					if (request.getSite().getAcceptStatCode()
							.contains(statusCode)) {
						handleGzip(httpResponse);
					} else {
						logger.warn("code error " + statusCode + "\t"
								+ request.getUrl());
					}
				}
				retry = false;
			} catch (Exception e) {
				tried++;
				if (tried > request.getSite().getRetryTimes()) {
					logger.warn("download page " + request.getUrl() + " error",
							e);
					return null;
				}
				logger.info("download page " + request.getUrl()
						+ " error, retry the " + tried + " time!");
				retry = true;
			}
		} while (retry);
		return httpResponse;
	}

	private synchronized HttpClient generateClient(Site site) {
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreProtocolPNames.USER_AGENT, site.getUserAgent());
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5 * 1000);
		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
				10 * 1000);

		HttpProtocolParamBean paramsBean = new HttpProtocolParamBean(params);
		paramsBean.setVersion(HttpVersion.HTTP_1_1);
		paramsBean.setContentCharset(site.getCharset());
		paramsBean.setUseExpectContinue(false);

		DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager,
				params);
		generateCookie(httpClient, site);
		httpClient.getParams().setIntParameter("http.socket.timeout", 60000);
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.BEST_MATCH);
		return httpClient;
	}

	private void generateCookie(DefaultHttpClient httpClient, Site site) {
		CookieStore cookieStore = new BasicCookieStore();
		for (Map.Entry<String, String> cookieEntry : site.getCookies()
				.entrySet()) {
			BasicClientCookie cookie = new BasicClientCookie(
					cookieEntry.getKey(), cookieEntry.getValue());
			cookie.setDomain(site.getDomain());
			cookieStore.addCookie(cookie);
		}
		httpClient.setCookieStore(cookieStore);
	}

	private HttpUriRequest generate(Request request) {
		if (request.getType().equals(Constant.REQUEST_GET)) {

			return new HttpGet(request.getUrl());
		} else {
			HttpPost httpPost = new HttpPost(request.getUrl());
			UrlEncodedFormEntity uefEntity = null;
			try {
				uefEntity = new UrlEncodedFormEntity(request.getParams());
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			httpPost.setEntity(uefEntity);
			httpPost.setHeaders(request.getHeaders().toArray(new Header[0]));
			return httpPost;
		}
	}

	private void handleGzip(HttpResponse httpResponse) {
		// Header ceheader = httpResponse.getEntity().getContentEncoding();
		// if (ceheader != null) {
		// HeaderElement[] codecs = ceheader.getElements();
		// for (HeaderElement codec : codecs) {
		// if (codec.getName().equalsIgnoreCase("gzip")) {
		// httpResponse.setEntity(new GzipDecompressingEntity(
		// httpResponse.getEntity()));
		// }
		// }
		// }
	}

	/*
	 * * 以下处理httpclient bug 压制没有head的response结果
	 */
	class MyPoolingClientConnectionManager extends
			PoolingClientConnectionManager {
		public MyPoolingClientConnectionManager(SchemeRegistry schemeRegistry) {
			super(schemeRegistry);
		}

		@Override
		protected ClientConnectionOperator createConnectionOperator(
				SchemeRegistry schreg) {

			return new MyClientConnectionOperator(schreg);
		}
	}

	class MyClientConnectionOperator extends DefaultClientConnectionOperator {
		public MyClientConnectionOperator(final SchemeRegistry sr) {
			super(sr);
		}

		@Override
		public OperatedClientConnection createConnection() {
			return new MyClientConnection();
		}
	}

	class MyClientConnection extends DefaultClientConnection {
		@Override
		protected HttpMessageParser createResponseParser(
				final SessionInputBuffer buffer,
				final HttpResponseFactory responseFactory,
				final HttpParams params) {
			return new DefaultResponseParser(buffer, new MyLineParser(),
					responseFactory, params);
		}
	}

	class MyLineParser extends BasicLineParser {
		boolean hasInBody = false;

		@Override
		public Header parseHeader(final CharArrayBuffer buffer)
				throws ParseException {
			try {
				return super.parseHeader(buffer);
			} catch (ParseException ex) {
				// 压制ParseException异常
				return new BasicHeader("invalid", buffer.toString());
			}
		}

		@Override
		public boolean hasProtocolVersion(CharArrayBuffer buffer,
				ParserCursor cursor) {
			System.out.println(buffer.toString());
			if (buffer.toString().contains("<head>")) {
				buffer.clear();
				return true;
			} else {
				return super.hasProtocolVersion(buffer, cursor);
			}

		}

		public StatusLine parseStatusLine(CharArrayBuffer buffer,
				ParserCursor cursor) {
			if (buffer.isEmpty()) {
				buffer = new CharArrayBuffer(15);
				buffer.append("HTTP/1.1 200 OK");
				cursor = new ParserCursor(0, 15);
			}
			return super.parseStatusLine(buffer, cursor);
		}
	}
}
