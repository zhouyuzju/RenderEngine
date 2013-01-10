package cn.edu.zju.eagle.accessibility.render;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;

/**
 * 
 * @ClassName: TaskPool
 * @Description: Container of rendering tasks, construct by index files. Giving
 *               synchronized rendering tasks to rendering thread. task format:
 *               {local_html_file_path}\t{html_url}
 * @author zhouyu <jwjzy1020 at gmail dot com>
 * @date 2013-1-10 下午1:45:47
 * @version V0.1
 * 
 */
public class TaskPool {

	private static Logger logger = Logger.getLogger(TaskPool.class);

	/**
	 * Container to hold thousands of tasks
	 */
	private Stack<String> taskSet;

	/**
	 * Render thread fetch FRAMESIZE tasks every time when it finished its
	 * current tasks
	 */
	private static final int FRAMESIZE = 20;

	/**
	 * Index files containing all the web pages which needs to rendering.
	 * Creating by web crawlers of format:{local_html_file_path}\t{html_url} per
	 * line.
	 */
	private String[] indexFiles = { "htm.idx", "html.idx", "shtml.idx",
			"asp.idx", "aspx.idx", "php.idx", "jsp.idx", "do.idx" };

	public TaskPool() {
		taskSet = new Stack<String>();
	}

	/**
	 * 
	 * @Title: configure
	 * @Description: configure taskPool from render.properties
	 * @param indexPath
	 *            : the local file path of index files, for example:
	 *            D:\accessibility-resources\index\www.cdpsn.org.cn
	 * @param hostUrl
	 *            : the rendering target website home page url, for example:
	 *            http://www.cjr.org.cn/
	 * @return void
	 * @throws
	 */
	public void configure(String indexPath, String hostUrl) {
		for (String indexFile : indexFiles) {
			readIndex(indexPath + getDomain(hostUrl) + '/' + indexFile);
		}
	}

	/**
	 * 
	 * @Title: getNextFrame
	 * @Description: Fetch a list of rendering tasks to the requesting render
	 *               thread
	 * @param @return
	 * @return List<String>: a list of tasks fetch into one of the render thread
	 * @throws
	 */
	public synchronized List<String> getNextFrame() {
		while (true) {
			List<String> result = new ArrayList<String>();

			/*
			 * when task container has nothing left then return null
			 */
			if (taskSet.size() == 0)
				return result;

			/*
			 * add FRAMESIZE or the rest of task container of tasks into list
			 */
			for (int i = 0; i < FRAMESIZE && taskSet.size() > 0; i++)
				result.add(taskSet.pop());
			return result;
		}
	}

	/**
	 * 
	 * @Title: isFinished
	 * @Description: Return if the task container has no task left
	 * @param @return
	 * @return boolean: if the task container has no task left
	 * @throws
	 */
	public boolean isFinished() {
		return taskSet.size() == 0;
	}

	/**
	 * 
	 * @Title: readIndex
	 * @Description: Read index files into task container
	 * @param path
	 *            : the index file absolute path
	 * @return void
	 * @throws
	 */
	private void readIndex(String path) {
		try {
			File file = new File(path);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));
			String tmp = "";
			while ((tmp = br.readLine()) != null) {
				taskSet.add(tmp);
			}
			br.close();
			logger.info("Read File: " + path);
		} catch (Exception e) {
			logger.warn("File: " + path + " doesn't exist");
		}
	}

	/**
	 * 
	 * @Title: getDomain
	 * @Description: convert url into domain
	 * @param url
	 *            : url for example: https://github.com/zhouyuzju
	 * @return String: return the domain of the giving url, for example:
	 *         github.com
	 * @throws
	 */
	private String getDomain(String url) {
		return url.split("://")[1].split("/")[0];
	}

}
