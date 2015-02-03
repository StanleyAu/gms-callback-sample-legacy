package com.genesys.gms.mobile.callback.demo.legacy.util;

import android.util.Log;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by stau on 1/9/2015.
 * NTS: Error handling is deferred to consumer
 */
public final class TimeHelper {
    public final static String ISO8601_FORMAT = "yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'";
    public final static String DAY_OF_MONTH_FORMAT = "EEE MMM d";
    public final static String FRIENDLY_FORMAT = "EEE MMM d hh':'mm a";

    public final static DateTimeFormatter ISO8601_FORMATTER = DateTimeFormat.forPattern(ISO8601_FORMAT).withZone(DateTimeZone.UTC);

    public static DateTime parseISO8601DateTime(String ISO8601_datestring) {
        return ISO8601_FORMATTER.parseDateTime(ISO8601_datestring);
    }

    public static DateTime toLocalTime(DateTime dateTime) {
        if(dateTime.getZone() != DateTimeZone.getDefault()) {
            return dateTime.withZone(DateTimeZone.getDefault());
        } else {
            return dateTime;
        }
    }

    public static DateTime toUTCTime(DateTime dateTime) {
        if(dateTime.getZone() != DateTimeZone.UTC) {
            return dateTime.withZone(DateTimeZone.UTC);
        } else {
            return dateTime;
        }
    }

    public static String serializeUTCTime(DateTime dateTime) {
        if(dateTime.getZone() != DateTimeZone.UTC) {
            return dateTime.withZone(DateTimeZone.UTC).toString(ISO8601_FORMAT);
        } else {
            return dateTime.toString(ISO8601_FORMAT);
        }
    }

    public static String toFriendlyString(String ISO8601_datestring)
    {
        DateTime time;
        time = ISO8601_FORMATTER.parseDateTime(ISO8601_datestring);
        time = time.withZone(DateTimeZone.getDefault());
        return time.toString(FRIENDLY_FORMAT);
    }
    public static String toFriendlyString(DateTime dateTime)
    {
        if(dateTime.getZone() != DateTimeZone.getDefault()) {
            return dateTime.withZone(DateTimeZone.getDefault()).toString(FRIENDLY_FORMAT);
        } else {
            return dateTime.toString(FRIENDLY_FORMAT);
        }
    }
}
