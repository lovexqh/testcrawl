package us.codecraft.webmagic.clawer;

import java.util.*;


/**
 * Site定义一个待抓取的站点的各种信息。<br>
 * 这个类的所有getter方法，一般都只会被爬虫框架内部进行调用。<br>
 *
 * @author code4crafter@gmail.com <br>
 *         Date: 13-4-21
 *         Time: 下午12:13
 */
public class Site {

    private String domain;

    private String userAgent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31";

    private Map<String, String> cookies = new LinkedHashMap<String, String>();

    private String charset="utf-8";

//    private List<String> startUrls = new ArrayList<String>();

    private int sleepTime = 3000;

    private int retryTimes = 0;

    private static final Set<Integer> DEFAULT_STATUS_CODE_SET = new HashSet<Integer>();

    private Set<Integer> acceptStatCode = DEFAULT_STATUS_CODE_SET;

    static {
        DEFAULT_STATUS_CODE_SET.add(200);
    }

    /**
     * 创建一个Site对象，等价于new Site()
     *
     * @return 新建的对象
     */
    public static Site me() {
        return new Site();
    }

    /**
     * 为这个站点添加一个cookie，可用于抓取某些需要登录访问的站点。这个cookie的域名与{@link #getDomain()}是一致的
     *
     * @param name  cookie的名称
     * @param value cookie的值
     * @return this
     */
    public Site addCookie(String name, String value) {
        cookies.put(name, value);
        return this;
    }

    /**
     * 为这个站点设置user-agent，很多网站都对user-agent进行了限制，不设置此选项可能会得到期望之外的结果。
     *
     * @param userAgent userAgent
     * @return this
     */
    public Site setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * 获取已经设置的所有cookie
     *
     * @return 已经设置的所有cookie
     */
    public Map<String, String> getCookies() {
        return cookies;
    }

    /**
     * 获取已设置的user-agent
     *
     * @return 已设置的user-agent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * 获取已设置的domain
     *
     * @return 已设置的domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * 设置这个站点所在域名，必须项。<br>
     * 目前不支持多个域名的抓取。抓取多个域名请新建一个Spider。
     *
     * @param domain 爬虫会抓取的域名
     * @return this
     */
    public Site setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    /**
     * 设置页面编码，若不设置则自动根据Html meta信息获取。<br>
     * 一般无需设置encoding，如果发现下载的结果是乱码，则可以设置此项。<br>
     *
     * @param charset 编码格式，主要是"utf-8"、"gbk"两种
     * @return this
     */
    public Site setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 获取已设置的编码
     *
     * @return 已设置的domain
     */
    public String getCharset() {
        return charset;
    }

    /**
     * 设置可接受的http状态码，仅当状态码在这个集合中时，才会读取页面内容。<br>
     * 默认为200，正常情况下，无须设置此项。<br>
     * 某些站点会错误的返回状态码，此时可以对这个选项进行设置。<br>
     *
     * @param acceptStatCode 可接受的状态码
     * @return this
     */
    public Site setAcceptStatCode(Set<Integer> acceptStatCode) {
        this.acceptStatCode = acceptStatCode;
        return this;
    }

    /**
     * 获取可接受的状态码
     *
     * @return 可接受的状态码
     */
    public Set<Integer> getAcceptStatCode() {
        return acceptStatCode;
    }

    /**
     * 获取初始页面的地址列表
     * @return 初始页面的地址列表
//     */
//    public List<String> getStartUrls() {
//        return startUrls;
//    }

    /**
     * 增加初始页面的地址，可反复调用此方法增加多个初始地址。
     * @param startUrl 初始页面的地址
     * @return this
     */
//    public Site addStartUrl(String startUrl) {
//        this.startUrls.add(startUrl);
//        return this;
//    }

    /**
     * 设置两次抓取之间的间隔，避免对目标站点压力过大(或者避免被防火墙屏蔽...)。
     *
     * @param sleepTime 单位毫秒
     * @return this
     */
    public Site setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }

    /**
     * 获取两次抓取之间的间隔
     * @return 两次抓取之间的间隔，单位毫秒
     */
    public int getSleepTime() {
        return sleepTime;
    }

    /**
     * 获取重新下载的次数，默认为0
     * @return 重新下载的次数
     */
    public int getRetryTimes() {
        return retryTimes;
    }

    /**
     * 设置获取重新下载的次数，默认为0
     * @return this
     */
    public Site setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        Site site = (Site) o;
//
//        if (acceptStatCode != null ? !acceptStatCode.equals(site.acceptStatCode) : site.acceptStatCode != null)
//            return false;
//        if (!domain.equals(site.domain)) return false;
//        if (!startUrls.equals(site.startUrls)) return false;
//        if (charset != null ? !charset.equals(site.charset) : site.charset != null) return false;
//        if (userAgent != null ? !userAgent.equals(site.userAgent) : site.userAgent != null) return false;
//
//        return true;
//    }

//    public Task toTask(){
//        return new Task() {
//            @Override
//            public String getUUID() {
//                return Site.this.getDomain();
//            }
//
//            @Override
//            public Site getSite() {
//                return Site.this;
//            }
//        };
//    }

//    @Override
//    public int hashCode() {
//        int result = domain.hashCode();
//        result = 31 * result + (startUrls != null ? startUrls.hashCode() : 0);
//        result = 31 * result + (userAgent != null ? userAgent.hashCode() : 0);
//        result = 31 * result + (charset != null ? charset.hashCode() : 0);
//        result = 31 * result + (acceptStatCode != null ? acceptStatCode.hashCode() : 0);
//        return result;
//    }
}
