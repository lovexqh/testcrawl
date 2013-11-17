package us.codecraft.webmagic.downloader;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import us.codecraft.webmagic.clawer.Page;
import us.codecraft.webmagic.clawer.Request;
import us.codecraft.webmagic.clawer.Site;
import us.codecraft.webmagic.fetcher.HttpClientFetcher;

/**
 * Author: code4crafer@gmail.com
 * Date: 13-6-18
 * Time: 上午8:22
 */
public class HttpClientDownloaderTest {

    @Ignore
    @Test
    public void testCookie() {
        Site site = Site.me().setDomain("www.diandian.com").addCookie("t", "yct7q7e6v319wpg4cpxqduu5m77lcgix");
        HttpClientFetcher httpClientDownloader = new HttpClientFetcher();
        Page download = httpClientDownloader.fetchPage(new Request("http://www.diandian.com",site));
        Assert.assertTrue(download.getHtml().toString().contains("flashsword30"));
    }
}
