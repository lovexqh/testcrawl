package us.codecraft.webmagic.fetcher;

/**
 * 比较占用资源的服务可以实现该接口，Spider会在结束时调用destroy()释放资源。<br>
 * @author yihua.huang@dianping.com <br>
 * @date: 13-7-26 <br>
 * Time: 下午3:10 <br>
 */
public interface Destroyable {

    public void destroy();

}
