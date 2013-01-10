package cn.edu.zju.eagle.accessibility.render;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * 
 * @ClassName: RenderController
 * @Description: The controller manages a rendering session. This class create
 *               rendering threads and monitor these threads.
 * @author zhouyu <jwjzy1020 at gmail dot com>
 * @date 2013-1-10 下午2:12:35
 * @version V0.1
 * 
 */
public class RenderController {
	private static Logger logger = Logger.getLogger(RenderController.class);

	/*
	 * A list of rendering threads, each of them performing rendering jobs
	 */
	private List<RenderThread> threadPool;

	/*
	 * Number of rendering threads to finish rendering job, configure from
	 * render.properties
	 */
	private int threadNum;

	/*
	 * Synchronized object to synchronize monitoring thread
	 */
	private final Object waitingLock;

	/*
	 * If all the rendering thread finish its tasks
	 */
	private boolean finished;

	/*
	 * Configure from render.properties,not used now
	 */
	private String basePath;

	/*
	 * Configure from render.properties,not used now
	 */
	private String hostUrl;

	/*
	 * The rendering index file path, configure from render.properties
	 */
	private String indexPath;

	/*
	 * A task pool which contains all the rendering tasks
	 */
	private TaskPool taskPool;

	public RenderController() {
		threadPool = new ArrayList<RenderThread>();
		finished = false;
		waitingLock = new Object();
		taskPool = new TaskPool();
	}

	/**
	 * 
	 * @Title: Configure
	 * @Description: Configure rendering thread number and index file path
	 * @param hosturl
	 *            : configure from render.properties
	 * @return void
	 * @throws
	 */
	public void Configure(String hosturl) {
		InputStream is;
		try {
			is = RenderController.class.getClassLoader().getResourceAsStream(
					"render.properties");
			Properties prop = new Properties();
			prop.load(is);
			is.close();
			basePath = prop.getProperty("basePath");
			hostUrl = prop.getProperty("hostUrl").split(";")[0];
			indexPath = prop.getProperty("indexPath");
			threadNum = Integer.parseInt(prop.getProperty("num_of_thread"));
			hostUrl = hosturl;
			logger.info("configure render controller!");
			taskPool.configure(indexPath, hosturl);
		} catch (Exception e) {
			logger.warn("fail to load render.properties!");
		}
	}

	/**
	 * 
	 * @Title: start
	 * @Description: Start rendering jobs, create rendering threads and a
	 *               monitor thread manage these threads until all the rendering
	 *               threads finish its tasks
	 * @param
	 * @return void
	 * @throws
	 */
	public void start() {
		try {

			/*
			 * start rendering threads
			 */
			for (int i = 0; i < threadNum; i++) {
				RenderThread thread = new RenderThread(i, taskPool);
				threadPool.add(thread);
				thread.start();
				logger.info("Renderer " + i + " started!");
			}

			/*
			 * start a monitor thread
			 */
			Thread monitorThread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						synchronized (waitingLock) {
							while (true) {
								Thread.sleep(10 * 1000); // monitor the render
															// threads every 10
															// seconds
								boolean someoneIsWorking = false;

								/*
								 * if the rendering thread die when there's some
								 * tasks left in taskPool,recreate this thread
								 */
								for (int i = 0; i < threadPool.size(); i++) {
									RenderThread thread = threadPool.get(i);
									if (!thread.isAlive()) {
										if (!finished && !taskPool.isFinished()) {
											logger.info("Recreate render "
													+ thread.getTaskId());
											thread = new RenderThread(i,
													taskPool);
											threadPool.remove(i);
											threadPool.add(i, thread);
											thread.start();
										}
									} else if (thread.isNotWaiting()) {
										someoneIsWorking = true;
									}
								}

								/*
								 * If all the thread finished its tasks,and
								 * taskPool has no task left. monitor thread
								 * finished
								 */
								if (!someoneIsWorking && taskPool.isFinished()) {
									logger.info("All the render thread job finished, controller exit!");
									someoneIsWorking = false;

									finished = true;
									waitingLock.notifyAll();

									return;
								}
							}
						}
					} catch (Exception e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					}
				}
			});

			monitorThread.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
