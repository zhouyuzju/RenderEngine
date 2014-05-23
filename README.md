<pre>RenderEngine
============

A multi-thread and real-time rendering engine for rendering local web pages.
Based on swt ie browser and log4j and it's a windows based plateform, 
if you want to move it to the linux or mac plateform,
 <a href="http://www.eclipse.org/swt/faq.php#howusewebkit">click here</a>.

Usage:
  RenderController controller = new RenderController();
  controller.Configure("http://www.bdpf.org.cn/");
	controller.start();

Precondition:
  Local web pages located in {basePath} + {domainName}, for example: D:\accessibility-resources\snapshot\www.cdpsn.org.cn
  Index file located in {indexPath} + {domainName}, form example: D:\accessibility-resources\index\www.cdpsn.org.cn
  IN one of index files, every line lies in a format: {local_web_page_path}\t{url}

Configure:
  basePath =  Local web pages absolute path, for example: D:/accessibility-resources/snapshot/
  indexPath = Index file path, for example: D:/accessibility-resources/index/
  hostUrl = Website home page url, for example: http://www.cjr.org.cn/
  num_of_thread = Number of rendering threads, for example: 6

</pre>
