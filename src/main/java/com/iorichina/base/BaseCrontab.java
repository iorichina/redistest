package com.iorichina.base;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseCrontab {

	final Logger log = LoggerFactory.getLogger(BaseCrontab.class);

	/**************** 线程池 **************/
	private ScheduledExecutorService executorBuildRank;

	private int interval = 60;

	public abstract void oneExecute();

	/**
	 * 初始化数据，并在设定时间内定时循环调用
	 */
	public void init() {
		if (executorBuildRank == null) {
			executorBuildRank = Executors.newSingleThreadScheduledExecutor();
		}

		logger().info("init gogogo");

		// 首次0秒后执行，之后每间隔*秒执行一次
		executorBuildRank.scheduleWithFixedDelay(new Runnable() {

			public void run() {
				try {
					BaseCrontab.this.logger().info(
							"crontab_" + BaseCrontab.this.getClass().getName()
									+ " -- [start]");
					try {
						oneExecute();
					} catch (Exception e) {
						logger().error(
								"[crontab_"
										+ BaseCrontab.this.getClass()
												.getSimpleName()
										+ "] exception => ", e);
					}
					logger().info(
							"crontab_" + BaseCrontab.this.getClass().getName()
									+ " -- [end]");
				} catch (Exception e) {
					logger().error(
							"[crontab_"
									+ BaseCrontab.this.getClass()
											.getSimpleName()
									+ "] exception => ", e);
				}

			}

		}, 3, getInterval(), TimeUnit.SECONDS);
	}

	/**
	 * 服务关闭
	 * 
	 * @throws InterruptedException
	 */
	public void close() throws InterruptedException {
		if (this.logger().isInfoEnabled()) {
			this.logger().info(
					"------ close" + this.getClass().getSimpleName()
							+ " ------");
		}
		if (!executorBuildRank.isShutdown()) {
			executorBuildRank.shutdown();
		}
	}

	/**
	 * 单独调用入口
	 */
	public void execute() {
		this.logger().info("start build rank={}", this.getClass().getName());
		long start = System.currentTimeMillis();
		try {
			oneExecute();
		} catch (Exception e) {
			this.logger()
					.error("build rank=" + this.getClass().getName()
							+ " error occur.", e);
			throw new RuntimeException();
		} finally {
			long end = System.currentTimeMillis();
			this.logger().info("finish build rank={},cost={}ms",
					this.getClass().getName(), (end - start));
		}
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public int getInterval() {
		return interval;
	}

	public Logger logger() {
		return log;
	}
}
