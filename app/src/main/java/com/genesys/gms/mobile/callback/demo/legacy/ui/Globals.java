package com.genesys.gms.mobile.callback.demo.legacy.ui;

import java.io.File;

import org.slf4j.LoggerFactory;

import android.content.ContextWrapper;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.BasicLogcatConfigurator;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;


public class Globals {

	public static final int CONNECT_TIMEOUT = 15000;
	public static final int REQUEST_TIMEOUT = 15000;

	public static final String GENESYS_LOG_TAG = "GenesysService";
	
	public static final String ACTION_GENESYS_RESPONSE = "com.genesys.gms.mobile.callback.demo.legacy.action.GENESYS_RESPONSE";
	public static final String ACTION_GENESYS_CLOUD_MESSAGE = "com.genesys.gms.mobile.callback.demo.legacy.action.GENESYS_CLOUD_MESSAGE";
	public static final String ACTION_GENESYS_START_SESSION = "com.genesys.gms.mobile.callback.demo.legacy.action.GENESYS_START_SESSION";
	public static final String ACTION_GENESYS_START_CHAT = "com.genesys.gms.mobile.callback.demo.legacy.action.GENESYS_START_CHAT";
	public static final String ACTION_GENESYS_ERROR_MESSAGE = "com.genesys.gms.mobile.callback.demo.legacy.action.GENESYS_ERROR_MESSAGE";
	public static final String EXTRA_MESSAGE = "com.genesys.gms.mobile.callback.demo.legacy.extra.Message";
	public static final String EXTRA_CHAT_URL = "com.genesys.gms.mobile.callback.demo.legacy.extra.ChatUrl";
	public static final String EXTRA_COMET_URL = "com.genesys.gms.mobile.callback.demo.legacy.extra.CometUrl";
	public static final String EXTRA_SUBJECT = "com.genesys.gms.mobile.callback.demo.legacy.extra.Subject";
	public static final String EXTRA_REQUEST_TYPE = "com.genesys.gms.mobile.callback.demo.legacy.extra.RequestType";

    public static final String PROPERTY_HOST = "com.genesys.gms.mobile.callback.demo.legacy.property.Host";
    public static final String PROPERTY_PORT = "com.genesys.gms.mobile.callback.demo.legacy.property.Port";
    public static final String PROPERTY_API_VERSION = "com.genesys.gms.mobile.callback.demo.legacy.property.ApiVersion";
    public static final String PROPERTY_GMS_USER = "com.genesys.gms.mobile.callback.demo.legacy.property.GMSUser";

	public static void setupLogging(ContextWrapper context) {
		BasicLogcatConfigurator.configureDefaultContext();
		
		LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();		
	    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
	    encoder.setContext(lc);
	    encoder.setPattern("%d{HH:mm:ss.SSS} %-5level%n - %msg%n%n");
	    encoder.start();

	    FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
	    fileAppender.setContext(lc);
		String logFile = context.getCacheDir().getAbsolutePath() + File.separator + "log";
	    fileAppender.setFile(logFile);
	    fileAppender.setEncoder(encoder);
	    fileAppender.start();
	    
	    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Globals.GENESYS_LOG_TAG);
	    root.addAppender(fileAppender);
	}

	static final Class<?> RECEIVER_ACTIVITY_CLASS =  GenesysSampleActivity.class;
	
}
