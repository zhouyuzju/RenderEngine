package cn.edu.zju.eagle.accessibility.render;
/**
 * 
 * @ClassName: RenderEngine
 * @Description: The example of configure and start a multi-thread and real time
 *               renderEngine
 * @author zhouyu
 * @date 2013-1-10 下午2:10:30
 * @version V0.1
 * 
 */
public class RenderEngine {
	public static void main(String args[]) {
		RenderController controller = new RenderController();
		controller.Configure("http://www.bdpf.org.cn/");
		controller.start();
	}
}
