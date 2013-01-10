package cn.edu.zju.eagle.accessibility.render;
import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @ClassName: RenderThread
 * @Description: A rendering thread performing a list of rendering jobs once a
 *               time
 * @author zhouyu <jwjzy1020 at gmail dot com>
 * @date 2013-1-10 下午3:13:38
 * @version V0.1
 * 
 */
public class RenderThread extends Thread {
	private static Logger logger = Logger.getLogger(RenderThread.class);

	/*
	 * render thread id
	 */
	private int taskId;

	/*
	 * if the thread finishes its tasks
	 */
	private boolean iswaiting;

	/*
	 * the file absolute path of web page which needs rendering
	 */
	private String path;

	/*
	 * the url corresponding to the rending web page
	 */
	private String url;

	/*
	 * global taskPool
	 */
	private TaskPool taskPool;

	/*
	 * SWT object Display
	 */
	private Display display;

	/*
	 * SWT object Shell
	 */
	private Shell shell;

	/*
	 * SWT object Browser
	 */
	private Browser browser;

	/*
	 * if the current web page rendering job finished
	 */
	private boolean renderFinished = false;

	/**
	 * 
	 * @param i
	 *            : taskId
	 * @param taskPool
	 *            : global taskPool
	 */
	public RenderThread(int i, TaskPool taskPool) {
		taskId = i;
		iswaiting = true;
		this.taskPool = taskPool;
	}

	@Override
	/**
	 * 
	 * @Title: run
	 * @Description: override from thread. 
	 * 1.Request rendering tasks to taskPool.
	 * 2.Get a response from taskPool, if the response is not null, rendering them.
	 * 3.Rendering these tasks one by one.
	 * 4.continue step1 until the response is null.
	 * 5.if the response is null, this thread died.
	 * @return void
	 * @throws
	 */
	public void run() {
		try {

			/*
			 * Construct the WEBKIT browser based on SWT and add rendering
			 * completed event. If current rendering task completed, set
			 * renderFinished true.
			 */
			display = new Display();
			shell = new Shell(display);
			browser = new Browser(shell, SWT.NONE);

			/*
			 * add rendering completed and changed event listener
			 */
			browser.addProgressListener(new ProgressListener() {
				public void changed(ProgressEvent event) {

				}

				public void completed(ProgressEvent event) {
					logger.info("Renderer " + taskId + ": render " + path + " "
							+ browser.getText().length());
					// logger.info(browser.getText());
					renderFinished = true;
				}
			});

			/*
			 * Continue request rendering tasks from taskPool
			 */
			while (true) {
				iswaiting = true;
				List<String> urlList = taskPool.getNextFrame();
				iswaiting = false;

				/*
				 * if the response is null, this thread died
				 */
				if (urlList.size() == 0) {
					if (taskPool.isFinished()) {
						logger.info("Renderer " + taskId
								+ " finish jobs and exit!");
						return;
					}
				}

				/*
				 * rendering a list of tasks one by one
				 */
				for (int i = 0; i < urlList.size(); i++) {
					path = urlList.get(i).split("\t")[0];
					// url = line.split("\t")[1];
					browser.setUrl(new File(path).getAbsolutePath());

					/*
					 * wait until the current rendering page completed, then go
					 * on rendering anther one
					 */
					while (!renderFinished) {
						if (!display.readAndDispatch())
							display.sleep();
					}
					renderFinished = false;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Title: getTaskId
	 * @Description: return the taskId of this rendering thread
	 * @return int: the taskId of this rendering thread
	 * @throws
	 */
	public int getTaskId() {
		return taskId;
	}

	/**
	 * 
	 * @Title: isNotWaiting
	 * @Description: return if this thread is waiting for the response to
	 *               taskPool
	 * @return boolean: if this thread is waiting for the response to taskPool
	 * @throws
	 */
	public boolean isNotWaiting() {
		return !iswaiting;
	}
}