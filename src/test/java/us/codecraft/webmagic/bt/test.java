package us.codecraft.webmagic.bt;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import us.codecraft.webmagic.clawer.Spider;

public class test {
	// private static String startUrl =
	// "http://www.MyServer.com/get_public_tbl.cgi?A=1";
	private static String startUrl = "http://www3.17domn.com/bt9/file.php/MRMPZ8S.html";

	public static void test1() {
		Spider.create(new BtProcesser())
				// .pipeline(new
				// FilePipeline(FilenameUtils.normalize(Constant.DOWNLOADPATH)))
		 .pipeline(new TorrentPipeline())
		 .pipeline(new ImagePipeline())
				.run();

	}

	public static void test2() {
		// String result = "";
		// String url = startUrl;
		// HttpGet httpGet = new HttpGet(url);
		// // 执行这行代码时出错
		// ClientConnectionManager cm = new BasicClientConnectionManager();
		// DefaultHttpClient hc = new DefaultHttpClient(cm);
		// HttpParams params = new BasicHttpParams();
		// params.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
		// hc.setParams(params);
		// // Header header = new
		// httpGet.addHeader(new BasicHeader("Accept-Encoding", "gzip"));
		// httpGet.addHeader(new BasicHeader("User-Agent",
		// "EliteTagger_v3.0.5-BETA"));
		// HttpResponse response;
		// try {
		// response = hc.execute(httpGet);
		// if (response.getStatusLine().getStatusCode() == 200) {
		// HttpEntity entity = response.getEntity();
		// result = EntityUtils.toString(entity, "utf-8");
		// }
		// System.out.println(result);
		// } catch (ClientProtocolException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } finally {
		// httpGet.abort();
		// }
	}

	public static void test3() {
		HttpClient httpclient = new DefaultHttpClient();
		try {
			HttpGet httpget = new HttpGet(startUrl);
			BasicResponseHandler responseHandler = new BasicResponseHandler();// Here
																				// is
																				// the
																				// change
			String responseBody = httpclient.execute(httpget, responseHandler);
			System.out.println(responseBody);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
	}

	public static void main(String[] args) {
		test1();
		// test2();
		// test3();
	}
}
