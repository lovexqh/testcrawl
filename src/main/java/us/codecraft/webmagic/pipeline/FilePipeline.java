package us.codecraft.webmagic.pipeline;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import us.codecraft.webmagic.clawer.ResultItems;
import us.codecraft.webmagic.clawer.Task;
import us.codecraft.webmagic.utils.UrlUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * 持久化到文件的接口。
 * 
 * @author code4crafter@gmail.com <br>
 *         Date: 13-4-21 Time: 下午6:28
 */
public class FilePipeline implements Pipeline {

	private String path = "/data/temp/webmagic/";

	private Logger logger = Logger.getLogger(getClass());

	/**
	 * 新建一个FilePipeline，使用默认保存路径"/data/temp/webmagic/"
	 */
	public FilePipeline() {

	}

	/**
	 * 新建一个FilePipeline
	 * 
	 * @param path
	 *            文件保存路径
	 */
	public FilePipeline(String path) {
		this.path = path;
	}

	@Override
	public void process(ResultItems resultItems) {
		if (resultItems.isSkip()) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("url:\t" + resultItems.getRequest().getUrl());
		for (Map.Entry<String, Object> entry : resultItems.getAll().entrySet()) {
			if (entry.getValue() instanceof Iterable) {
				Iterable value = (Iterable) entry.getValue();
				sb.append(entry.getKey() + ":");
				for (Object o : value) {
					sb.append(o.toString());
				}
			} else {
				sb.append(entry.getKey() + ":\t" + entry.getValue());
			}
		}
		try {
			FileUtils.writeStringToFile(
					UrlUtils.Url2Path(resultItems.getRequest().getUrl()),
					conert2localUrl(sb.toString()),"GBK");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.info("write file error");
		}
	}
	private String conert2localUrl(String content){
		return StringUtils.replace(content, "http://", "http://localhost/");
	}
}
