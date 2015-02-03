package com.genesys.gms.mobile.callback.demo.legacy.data.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by stau on 02/11/2014.
 */
public class DateTimeTypeAdapter extends TypeAdapter<DateTime> {
    @Inject
    DateTimeFormatter dateTimeFormatter;

    @Override
    public void write(JsonWriter out, DateTime value) throws IOException {
        if(value == null){
            out.nullValue();
            return;
        }
        out.value(dateTimeFormatter.print(value));
    }

    @Override
    public DateTime read(JsonReader in) throws IOException {
        if(in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return dateTimeFormatter.parseDateTime(in.nextString());
    }
}