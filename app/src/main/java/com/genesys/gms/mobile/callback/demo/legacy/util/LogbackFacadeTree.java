package com.genesys.gms.mobile.callback.demo.legacy.util;

import android.content.Context;
import android.util.Log;
import timber.log.Timber;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by stau on 2/12/2015.
 */
public class LogbackFacadeTree implements Timber.TaggedTree {
    private static final int MAX_LOG_LENGTH = 4000;
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("\\$\\d+$");
    private static final ThreadLocal<String> NEXT_TAG = new ThreadLocal<String>();

    private final String logFilePath;
    private final Timber.Tree rootTree;

    private static String createTag() {
        String tag = NEXT_TAG.get();
        if (tag != null) {
            NEXT_TAG.remove();
            return tag;
        }

        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if (stackTrace.length < 6) {
            throw new IllegalStateException(
                "Synthetic stacktrace didn't have enough elements: are you using proguard?");
        }
        tag = stackTrace[5].getClassName();
        Matcher m = ANONYMOUS_CLASS.matcher(tag);
        if (m.find()) {
            tag = m.replaceAll("");
        }
        return tag.substring(tag.lastIndexOf('.') + 1);
    }

    static String formatString(String message, Object... args) {
        // If no varargs are supplied, treat it as a request to log the string without formatting.
        return args.length == 0 ? message : String.format(message, args);
    }

    public LogbackFacadeTree(Timber.Tree tree, Context context) {
        super();
        rootTree = tree;
        logFilePath = context.getCacheDir().getAbsolutePath() + File.separator + "log";
    }

    private static void appendToFile(String filePath, int priority, String tag, String msg) {
        StringBuilder builder = new StringBuilder();
        switch(priority) {
            case Log.VERBOSE:
                builder.append("V/");
                break;
            case Log.DEBUG:
                builder.append("D/");
                break;
            case Log.INFO:
                builder.append("I/");
                break;
            case Log.WARN:
                builder.append("W/");
                break;
            case Log.ERROR:
                builder.append("E/");
                break;
        }
        builder.append(tag)
            .append(":  ")
            .append(msg);

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
            out.println(builder.toString());
            out.close();
        } catch(IOException e) {
            // Failed to append
        }
    }

    @Override
    public void tag(String tag) {
        try {
            ((Timber.TaggedTree)rootTree).tag(tag);
        } catch(Exception e) {
            ;
        }
    }

    @Override
    public void v(String message, Object... args) {
        rootTree.v(message, args);
        throwShade(Log.VERBOSE, formatString(message, args), null);
    }

    @Override
    public void v(Throwable t, String message, Object... args) {
        rootTree.v(t, message, args);
        throwShade(Log.VERBOSE, formatString(message, args), t);
    }

    @Override
    public void d(String message, Object... args) {
        rootTree.d(message, args);
        throwShade(Log.DEBUG, formatString(message, args), null);
    }

    @Override
    public void d(Throwable t, String message, Object... args) {
        rootTree.d(t, message, args);
        throwShade(Log.DEBUG, formatString(message, args), t);
    }

    @Override
    public void i(String message, Object... args) {
        rootTree.i(message, args);
        throwShade(Log.INFO, formatString(message, args), null);
    }

    @Override
    public void i(Throwable t, String message, Object... args) {
        rootTree.i(t, message, args);
        throwShade(Log.INFO, formatString(message, args), t);
    }

    @Override
    public void w(String message, Object... args) {
        rootTree.w(message, args);
        throwShade(Log.WARN, formatString(message, args), null);
    }

    @Override
    public void w(Throwable t, String message, Object... args) {
        rootTree.w(t, message, args);
        throwShade(Log.WARN, formatString(message, args), t);
    }

    @Override
    public void e(String message, Object... args) {
        rootTree.e(message, args);
        throwShade(Log.ERROR, formatString(message, args), null);
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
        rootTree.e(t, message, args);
        throwShade(Log.ERROR, formatString(message, args), t);
    }

    private void throwShade(int priority, String message, Throwable t) {
        if (message == null || message.length() == 0) {
            if (t != null) {
                message = Log.getStackTraceString(t);
            } else {
                // Swallow message if it's null and there's no throwable.
                return;
            }
        } else if (t != null) {
            message += "\n" + Log.getStackTraceString(t);
        }

        String tag = createTag();

        if (message.length() < MAX_LOG_LENGTH) {
            appendToFile(logFilePath, priority, tag, message);
            return;
        }

        // Split by line, then ensure each line can fit into Log's maximum length.
        for (int i = 0, length = message.length(); i < length; i++) {
            int newline = message.indexOf('\n', i);
            newline = newline != -1 ? newline : length;
            do {
                int end = Math.min(newline, i + MAX_LOG_LENGTH);
                appendToFile(logFilePath, priority, tag, message.substring(i, end));
                i = end;
            } while (i < newline);
        }
    }
}
