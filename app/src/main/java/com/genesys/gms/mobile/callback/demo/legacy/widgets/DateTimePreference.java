package com.genesys.gms.mobile.callback.demo.legacy.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import com.genesys.gms.mobile.callback.demo.legacy.R;
import com.genesys.gms.mobile.callback.demo.legacy.util.TimeHelper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;

import java.util.Arrays;

/**
 * Created by stau on 10/07/2014.
 */
public class DateTimePreference extends DialogPreference
{
    // TODO: Externalize format of Date
    final private static String TAG = "DateTimePreference";

    private String currentDT;
    private NumberPicker datePicker;
    private NumberPicker hourPicker;
    private NumberPicker minutePicker;
    private NumberPicker periodPicker;
    private int maxDays = 28;
    final private static String[] defaultPeriods = {"AM", "PM"};
    /*
    final private static String DAY_OF_MONTH_FORMAT = "%a %b %d";
    final private static String ISO8601_FORMAT = "%FT%T.000Z";
    final private static String FRIENDLY_FORMAT = "%a %b %e %I:%M %p";
    */

    final private static String DEFAULT_VALUE = "0000-00-00T00:00:00.000Z";

    private class TwoDigitFormatter implements NumberPicker.Formatter{
		public String format(int value){
            return String.format("%02d", value);
        }
    }

    public DateTimePreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setDialogLayoutResource(R.layout.datetime_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DateTimePreference);
        final int N = a.getIndexCount();
        for(int i = 0; i < N; ++i)
        {
            int attr = a.getIndex(i);
            if(attr == R.styleable.DateTimePreference_maxDays)
            {
                maxDays = Math.max(1, a.getInt(attr, 28));
            }
        }
        a.recycle();
    }

    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);

        // Initialize Picker elements
        // Note that dates will not be initialized
        datePicker = (NumberPicker)view.findViewById(R.id.datePicker);
        populateDates();
        datePicker.setMaxValue(maxDays - 1);
        datePicker.setMinValue(0);
        datePicker.setWrapSelectorWheel(false);
        datePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        TwoDigitFormatter twoDigitFormatter = new TwoDigitFormatter();
        hourPicker = (NumberPicker)view.findViewById(R.id.hourPicker);
        hourPicker.setMaxValue(12);
        hourPicker.setMinValue(1);
        hourPicker.setFormatter(twoDigitFormatter);
        hourPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        minutePicker = (NumberPicker)view.findViewById(R.id.minutePicker);
        minutePicker.setMaxValue(59);
        minutePicker.setMinValue(0);
        minutePicker.setFormatter(twoDigitFormatter);
        minutePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        periodPicker = (NumberPicker)view.findViewById(R.id.periodPicker);
        periodPicker.setMaxValue(1);
        periodPicker.setMinValue(0);
        periodPicker.setWrapSelectorWheel(false);
        periodPicker.setDisplayedValues(defaultPeriods);
        periodPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        setPickers();
    }

    public void populateDates()
    {
        String[] displayedDates = new String[maxDays];
        DateTime time = new DateTime();

        // Populate datePicker values based on maxDays setting
        displayedDates[0] = "Today";

        for(int i=1;i<maxDays;++i)
        {
            time = time.plusDays(1);
            displayedDates[i] = time.toString(TimeHelper.DAY_OF_MONTH_FORMAT);
        }

        Log.d(TAG, "displayedDates: " + Arrays.toString(displayedDates));
        datePicker.setDisplayedValues(displayedDates);
    }

    private int getDayDifference(DateTime time1, DateTime time2)
    {
        return Days.daysBetween(time1.withTimeAtStartOfDay(), time2.withTimeAtStartOfDay()).getDays();
    }

    private void readPickers()
    {
        int day = datePicker.getValue(); // Days from Today
        int hour = hourPicker.getValue();
        int minute = minutePicker.getValue();
        int period = periodPicker.getValue();
        hour %= 12;
        if(period==1) // PM
        {
            hour += 12;
        }
        DateTime now = DateTime.now();
        DateTime time = now.plusDays(day).withTime(hour, minute, 0, 0);

        // If time before now is not allowed
        if (time.isBeforeNow())
        {
            time = now;
        }

        currentDT = TimeHelper.serializeUTCTime(time);
    }

    private void setPickers()
    {
        // Set to currentDT
        DateTime time;
        DateTime today = DateTime.now();
        try
        {
            time = TimeHelper.parseISO8601DateTime(currentDT);
            time = time.withZone(DateTimeZone.getDefault());
        }
        catch(NullPointerException ex)
        {
            Log.w(TAG, "currentDT is null");
            time = today;
        }
        catch(IllegalArgumentException ex)
        {
            Log.w(TAG, "currentDT is invalid: " + ex.getMessage());
            time = today;
        }

        int daysDiff = 0;

        Log.d(TAG, "currentDT: " + TimeHelper.serializeUTCTime(time) +
            " today: " + TimeHelper.serializeUTCTime(DateTime.now()));
        // Only if time before now is not allowed
        if(time.isBefore(today)) {
            time = today;
        }
        else
        {
            daysDiff = getDayDifference(today, time);
            Log.d(TAG, "Day difference between restored and today: " + daysDiff);
            // Previously selected time exceeds allowed range
            if(daysDiff>(maxDays - 1))
            {
                time = today;
                daysDiff = 0;
            }
        }

        datePicker.setValue(daysDiff);
        if(time.getHourOfDay() >= 12){
            hourPicker.setValue((time.getHourOfDay()-12)==0 ? 12 : (time.getHourOfDay()-12));
            periodPicker.setValue(1);
        }
        else
        {
            hourPicker.setValue((time.getHourOfDay()==0) ? 12 : time.getHourOfDay());
            periodPicker.setValue(0);
        }
        minutePicker.setValue(time.getMinuteOfHour());
    }

    private String formatDate()
    {
        return formatDate(null);
    }

    private String formatDate(DateTime time)
    {
        String result;
        if(time==null)
        {
            time = DateTime.now();
        }
        result = TimeHelper.serializeUTCTime(time);
        Log.d(TAG, "formatDate: " + result);
        return result;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
        super.onDialogClosed(positiveResult);
        if(positiveResult)
        {
            readPickers();
            if(callChangeListener(currentDT))
            {
                persistString(currentDT);
                
                setSummary(TimeHelper.toFriendlyString(currentDT));
            }
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue)
    {
        Log.d(TAG, "onSetInitialValue()" + (String)defaultValue);
        if(restorePersistedValue)
        {
            currentDT = this.getPersistedString(DEFAULT_VALUE);
            //setSummary(currentDT);
        }
        else
        {
            if (defaultValue==null)
            {
                currentDT = formatDate();
            }
            else
            {
                currentDT = (String) defaultValue;
            }
            persistString(currentDT);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        return a.getString(index);
    }
}
