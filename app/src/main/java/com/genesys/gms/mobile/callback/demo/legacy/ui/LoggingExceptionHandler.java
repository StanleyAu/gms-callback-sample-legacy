package com.genesys.gms.mobile.callback.demo.legacy.ui;

import org.slf4j.Logger;

public class LoggingExceptionHandler implements Thread.UncaughtExceptionHandler {
	private final Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();
	private final Logger logger;

	public LoggingExceptionHandler(Logger logger) {
		this.logger = logger;
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		logger.error("Uncaught exception: ", ex);
		
		if (originalHandler != null)
			originalHandler.uncaughtException(thread, ex);
		else
			System.exit(0);
	}
	
	public static void setDefaultUncaughtExceptionHandler(Logger logger) {
		Thread.setDefaultUncaughtExceptionHandler(new LoggingExceptionHandler(logger));
	}
}

