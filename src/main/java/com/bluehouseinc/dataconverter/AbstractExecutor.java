package com.bluehouseinc.dataconverter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.util.StopWatch;

import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class AbstractExecutor {
	StopWatch sw = new StopWatch();
	protected ConfigurationProvider cfgProvider;

	public AbstractExecutor(ConfigurationProvider cfgProvider) {
		this.cfgProvider = cfgProvider;
	}

	public void execute() throws TidalException {

		String name = this.getClass().getSimpleName();

		sw.start();
		log.debug("Executing [" + name + "]");

		ExecutorService executor = null;

		try {

			if (getThreadCount() > 1) {

				BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();

				executor = new ThreadPoolExecutor(getThreadCount(), getThreadCount() * 2, 5, TimeUnit.SECONDS, blockingQueue, new ThreadPoolExecutor.AbortPolicy());

				((ThreadPoolExecutor) executor).prestartAllCoreThreads();

				doExecute(executor);

				executor.shutdown();

				if (!executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS)) {
					executor.shutdownNow();
				}

			} else {
				doExecute();

			}

		} catch (InterruptedException e) {
			executor.shutdownNow();
			throw new TidalException(e);
		}

		sw.stop();

		log.debug("Executing [" + name + "] Completed in [" + sw.getTotalTimeSeconds() + "] Seconds");
	}

	public abstract void doExecute(ExecutorService executor);

	public abstract void doExecute();

	protected int getThreadCount() {
		return 0;
	}

}
