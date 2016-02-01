package com.iorichina.base;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class ServiceBooter {
	final Logger logger = LoggerFactory.getLogger(ServiceBooter.class);

	public static final String SERVER_XML = "classpath:spring_server.xml";
	protected FileSystemXmlApplicationContext context;

	public static void main(String[] args) {
		new ServiceBooter()._main(args);
	}

	private void _main(String[] args) {
		System.out.println(ServiceBooter.class.getClass().getSimpleName()
				+ " start");

		String serverXml = SERVER_XML;
		String xml = System.getProperty("spring_server_xml");
		if (xml != null && !xml.isEmpty()) {
			serverXml = xml;
		}

		try {
			// loop
			boot(serverXml);
		} catch (Throwable e) {
			System.out.println("xml=>" + serverXml + e.getMessage());
			if (logger.isWarnEnabled()) {
				logger.warn("xml=>" + serverXml, e);
			}
		} finally {
			System.out.println(ServiceBooter.class.getClass().getSimpleName()
					+ " exit");
		}

		// check threads
		try {
			ThreadMXBean tb = ManagementFactory.getThreadMXBean();
			long thisId = Thread.currentThread().getId();
			String[] names = new String[] { "Finalizer", "Reference Handler",
					"Signal Dispatcher" };
			long tids[] = tb.getAllThreadIds();
			for (long l : tids) {
				if (l == thisId)
					continue;
				ThreadInfo info = tb.getThreadInfo(l, 10);
				if (info == null)
					continue;
				boolean m = false;
				for (int i = 0; i < names.length; i++) {
					String n = names[i];
					if (n.equals(info.getThreadName())) {
						m = true;
						break;
					}
				}
				if (m)
					continue;

				System.out.println("LIVING THREAD!!\n" + info.toString());
			}
		} catch (Exception err) {
			System.out.println("check living thread fail");
			err.printStackTrace(System.out);
		}

		System.exit(1);
	}

	public void boot(String serverXml) throws Throwable {

		if (serverXml.isEmpty()) {
			throw new Exception("serverXml is miss!!");
		}
		System.out.println("loading spring application");
		// 预加载所有bean
		context = new FileSystemXmlApplicationContext(serverXml);

		// 监听关闭
		final Thread t = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (t.isAlive()) {
					t.interrupt();
				}
			}
		});

		// new MainThread() {
		// @Override
		// public boolean postShutdown() {
		// if (t.isAlive()) {
		// t.interrupt();
		// }
		// return true;
		// }
		// };
		// 启动
		try {
			startServer();

			System.out.println("mainLoop");
			logger.info("mainLoop");
			mainLoop();
			System.out.println("exit mainLoop");
		} catch (InterruptedException e) {
			logger.debug("Interrupted!");
		} finally {
			System.out.println("close spring application");
			context.close();
		}

	}

	protected void mainLoop() throws InterruptedException {
		while (true) {
			Thread.sleep(Long.MAX_VALUE);
		}
	}

	public void startServer() {
	}

}
