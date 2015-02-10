package com.genesys.gms.mobile.callback.demo.legacy.data.api;

import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.*;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.*;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.callback.*;
import com.genesys.gms.mobile.callback.demo.legacy.data.retrofit.GmsEndpoint;
import com.genesys.gms.mobile.callback.demo.legacy.util.Globals;
import com.genesys.gms.mobile.callback.demo.legacy.util.TimeHelper;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import org.joda.time.DateTime;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stau on 2/3/2015.
 */
@Singleton
public class CallbackServiceManager {
    public static final String GMS_USER = "gms_user";
    public static final String CHECK_QUEUE_POSITION_SERVICE_NAME = "check-queue-position";
    public static final MediaType FORM_ENCODED = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private final CallbackService callbackService;
    private final GmsEndpoint gmsEndpoint;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final EventBus bus;
    private final SharedPreferences sharedPreferences;

    @DebugLog @Inject
    public CallbackServiceManager(CallbackService callbackService,
                                  GmsEndpoint gmsEndpoint,
                                  OkHttpClient httpClient,
                                  Gson gson,
                                  SharedPreferences sharedPreferences) {
        this.callbackService = callbackService;
        this.gmsEndpoint = gmsEndpoint;
        this.httpClient = httpClient;
        this.gson = gson;
        this.bus = EventBus.getDefault();
        this.sharedPreferences = sharedPreferences;
    }

    public void onEvent(CallbackStartEvent event) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("_customer_number", event._customer_number);
        if(event._desired_time != null) {
            params.put("_desired_time", event._desired_time);
        }
        params.put("_callback_state", event._callback_state);
        params.put("_urs_virtual_queue", event._urs_virtual_queue);
        params.put("_request_queue_time_stat", event._request_queue_time_stat);
        if(event.properties != null) {
            params.putAll(event.properties);
        }

        callbackService.startCallback(event.serviceName, params, new Callback<CallbackDialog>() {
            @Override
            public void success(CallbackDialog callbackDialog, Response response) {
                bus.post(new CallbackStartDoneEvent(callbackDialog));
            }

            @Override
            public void failure(RetrofitError error) {
                try {
                    if (error.getResponse() != null) {
                        CallbackException body = (CallbackException) error.getBodyAs(CallbackException.class);
                        bus.post(new CallbackErrorEvent(body));
                        return;
                    }
                } catch (Exception e) {;}
                bus.post(new UnknownErrorEvent(error));
            }
        });
    }

    public void onEvent(CallbackCancelEvent event) {
        callbackService.cancelCallback(event.serviceName, event.serviceID, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                bus.post(new CallbackCancelDoneEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                try {
                    if (error.getResponse() != null) {
                        CallbackException body = (CallbackException) error.getBodyAs(CallbackException.class);
                        bus.post(new CallbackErrorEvent(body));
                        return;
                    }
                } catch (Exception e) {;}
                bus.post(new UnknownErrorEvent(error));
            }
        });
    }

    public void onEvent(CallbackUpdateEvent event) {
        Map<String, String> params = new HashMap<String, String>();
        if(event._new_desired_time != null) {
            params.put("_new_desired_time", TimeHelper.serializeUTCTime(event._new_desired_time));
        }
        params.put("_callback_state", event._callback_state);
        if(event.properties != null) {
            params.putAll(event.properties);
        }
        callbackService.updateCallback(event.serviceName, event.serviceID, params, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                bus.post(new CallbackUpdateDoneEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                try {
                    if (error.getResponse() != null) {
                        CallbackRescheduleException body = (CallbackRescheduleException) error.getBodyAs(CallbackRescheduleException.class);
                        bus.post(new CallbackRescheduleErrorEvent(body));
                        return;
                    }
                } catch (Exception e) {;}
                bus.post(new UnknownErrorEvent(error));
            }
        });
    }

    public void onEvent(CallbackQueryEvent event) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("operand", event.operand.name());
        if(event.properties != null) {
            params.putAll(event.properties);
        }
        callbackService.queryCallback(event.serviceName, params, new Callback<List<CallbackRequest>>() {
            @Override
            public void success(List<CallbackRequest> callbackRequests, Response response) {
                bus.post(new CallbackQueryDoneEvent(callbackRequests));
            }

            @Override
            public void failure(RetrofitError error) {
                try {
                    if (error.getResponse() != null) {
                        CallbackException body = (CallbackException) error.getBodyAs(CallbackException.class);
                        bus.post(new CallbackErrorEvent(body));
                        return;
                    }
                } catch (Exception e) {;}
                bus.post(new UnknownErrorEvent(error));
            }
        });
    }

    @DebugLog
    public void onEvent(CallbackAvailabilityEvent event) {
        callbackService.queryAvailability(
            event.serviceName,
            event.start,
            event.numberOfDays,
            event.end,
            event.maxTimeSlots,
            new Callback<Map<DateTime, Integer>>() {
                @Override
                public void success(Map<DateTime, Integer> dateTimeIntegerMap, Response response) {
                    bus.post(new CallbackAvailabilityDoneEvent(dateTimeIntegerMap));
                }

                @Override @DebugLog
                public void failure(RetrofitError error) {
                    try {
                        if (error.getResponse() != null) {
                            CallbackException body = (CallbackException) error.getBodyAs(CallbackException.class);
                            bus.post(new CallbackErrorEvent(body));
                            return;
                        }
                    } catch (Exception e) {;}
                    bus.post(new UnknownErrorEvent(error));
                }
            }
        );
    }

    public void onEvent(CallbackAdminEvent event) {
        String strEndTime = null;

        if(event.end_time != null) {
            strEndTime = TimeHelper.serializeUTCTime(event.end_time);
        }
        callbackService.queryCallbackAdmin(event.target, strEndTime, event.max, new Callback<Map<String, List<CallbackAdminRequest>>>() {
            @Override
            public void success(Map<String, List<CallbackAdminRequest>> stringListMap, Response response) {
                bus.post(new CallbackAdminDoneEvent(stringListMap));
            }

            @Override
            public void failure(RetrofitError error) {
                try {
                    if (error.getResponse() != null) {
                        CallbackException body = (CallbackException) error.getBodyAs(CallbackException.class);
                        bus.post(new CallbackErrorEvent(body));
                        return;
                    }
                } catch (Exception e) {;}
                bus.post(new UnknownErrorEvent(error));
            }
        });
    }

    public void onEvent(CallbackDialogEvent event) {
        String strBaseUri = gmsEndpoint.getUrl();
        String strSyncUri = new Uri.Builder()
            .encodedPath(strBaseUri)
            .appendEncodedPath(event.url)
            .toString();

        String strGmsUser = sharedPreferences.getString(Globals.PROPERTY_GMS_USER, null);
        // TODO: Handle RuntimeExceptions resulting from Malformed URI
        Request.Builder builder = new Request.Builder()
                .url(strSyncUri);
        if(strGmsUser != null && !strGmsUser.isEmpty()) {
            builder.addHeader(GMS_USER, strGmsUser);
        }
        // builder.post(RequestBody.create(FORM_ENCODED, ""));
        Request request = builder.build();

        httpClient.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                bus.post(new CallbackDialogDoneEvent(false, null));
            }

            @Override
            public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                boolean success = false;
                CallbackDialog callbackDialog = null;
                if(response.isSuccessful()) {
                    try {
                        callbackDialog = gson.fromJson(response.body().charStream(), CallbackDialog.class);
                        success = true;
                    } catch (Exception e) {
                        Log.e("CallbackServiceManager", "Exception while parsing Dialog response: " + e);
                    }
                } else {
                    Log.e("CallbackServiceManager", "Negative response for Dialog request: " + response);
                }
                bus.post(new CallbackDialogDoneEvent(success, callbackDialog));
            }
        });
    }

    public void onEvent(CallbackCheckQueueEvent event) {
        String strBaseUri = gmsEndpoint.getUrl();
        // TODO: Check how using the Callback interface can affect this
        String strServiceUri = new Uri.Builder()
                .encodedPath(strBaseUri)
                .appendPath("service")
                .appendPath(event.sessionId)
                .appendPath(CHECK_QUEUE_POSITION_SERVICE_NAME)
                .toString();

        String strGmsUser = sharedPreferences.getString(Globals.PROPERTY_GMS_USER, null);
        Request.Builder builder = new Request.Builder()
                .url(strServiceUri);
        if(strGmsUser != null && !strGmsUser.isEmpty()) {
            builder.addHeader(GMS_USER, strGmsUser);
        }
        Request request = builder.build();

        httpClient.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                bus.post(new CallbackCheckQueueDoneEvent(false, null));
            }

            @Override
            public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                boolean success = false;
                CallbackQueuePosition callbackQueuePosition = null;
                if(response.isSuccessful()) {
                    try {
                        /*
                        Reader reader = response.body().charStream();
                        StringBuilder builder = new StringBuilder();
                        int charsRead = -1;
                        char[] chars = new char[100];
                        do{
                            charsRead = reader.read(chars,0,chars.length);
                            //if we have valid chars, append them to end of string.
                            if(charsRead>0)
                                builder.append(chars,0,charsRead);
                        }while(charsRead>0);
                        String body = builder.toString();

                        Log.d("CallbackServiceManager", "CheckQueue Response: " + body);
                        callbackQueuePosition = gson.fromJson(body, CallbackQueuePosition.class);
                        */
                        callbackQueuePosition = gson.fromJson(response.body().charStream(), CallbackQueuePosition.class);
                        success = true;
                    } catch (Exception e) {
                        Log.e("CallbackServiceManager", "Exception while parsing CheckQueue response: " + e);
                    }
                } else {
                    Log.e("CallbackServiceManager", "Negative response for CheckQueue request: " + response);
                }
                bus.post(new CallbackCheckQueueDoneEvent(success, callbackQueuePosition));
            }
        });
    }
}
