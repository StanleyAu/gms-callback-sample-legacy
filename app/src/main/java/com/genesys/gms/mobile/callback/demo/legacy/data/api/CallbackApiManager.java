package com.genesys.gms.mobile.callback.demo.legacy.data.api;

import android.content.SharedPreferences;
import android.net.Uri;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.*;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.UnknownErrorEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.callback.*;
import com.genesys.gms.mobile.callback.demo.legacy.data.retrofit.GmsEndpoint;
import com.genesys.gms.mobile.callback.demo.legacy.util.Globals;
import com.genesys.gms.mobile.callback.demo.legacy.util.TimeHelper;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import org.joda.time.DateTime;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stau on 2/3/2015.
 */
@Singleton
public class CallbackApiManager {
  // TODO: Convert events to use Bundles internally
  // TODO: Create a BundleTypeAdapter or use one from funf-open-sensing-framework
  private static final String GMS_USER = "gms_user";
  private static final String SERVICE_PATH = "service";
  private static final String CHECK_QUEUE_POSITION_SERVICE_NAME = "check-queue-position";

  private final CallbackApi callbackApi;
  private final GmsEndpoint gmsEndpoint;
  private final OkHttpClient httpClient;
  private final Gson gson;
  private final EventBus bus;
  private final SharedPreferences sharedPreferences;

  @Inject
  @DebugLog
  public CallbackApiManager(CallbackApi callbackApi,
                            GmsEndpoint gmsEndpoint,
                            OkHttpClient httpClient,
                            Gson gson,
                            SharedPreferences sharedPreferences) {
    this.callbackApi = callbackApi;
    this.gmsEndpoint = gmsEndpoint;
    this.httpClient = httpClient;
    this.gson = gson;
    this.bus = EventBus.getDefault();
    this.sharedPreferences = sharedPreferences;
  }

  public void onEvent(CallbackStartEvent event) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("_customer_number", event._customer_number);
    if (event._desired_time != null) {
      params.put("_desired_time", event._desired_time);
    }
    params.put("_callback_state", event._callback_state);
    params.put("_urs_virtual_queue", event._urs_virtual_queue);
    params.put("_request_queue_time_stat", event._request_queue_time_stat);
    if (event.properties != null) {
      params.putAll(event.properties);
    }

    callbackApi.startCallback(event.serviceName, params, new Callback<CallbackDialog>() {
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
        } catch (Exception e) {
          ;
        }
        bus.post(new UnknownErrorEvent(error));
      }
    });
  }

  public void onEvent(CallbackCancelEvent event) {
    callbackApi.cancelCallback(event.serviceName, event.serviceID, new Callback<Response>() {
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
        } catch (Exception e) {
          ;
        }
        bus.post(new UnknownErrorEvent(error));
      }
    });
  }

  public void onEvent(CallbackUpdateEvent event) {
    Map<String, String> params = new HashMap<String, String>();
    if (event._new_desired_time != null) {
      params.put("_new_desired_time", TimeHelper.serializeUTCTime(event._new_desired_time));
    }
    params.put("_callback_state", event._callback_state);
    if (event.properties != null) {
      params.putAll(event.properties);
    }
    callbackApi.updateCallback(event.serviceName, event.serviceID, params, new Callback<Response>() {
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
        } catch (Exception e) {
          ;
        }
        bus.post(new UnknownErrorEvent(error));
      }
    });
  }

  public void onEvent(CallbackQueryEvent event) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("operand", event.operand.name());
    if (event.properties != null) {
      params.putAll(event.properties);
    }
    callbackApi.queryCallback(event.serviceName, params, new Callback<List<CallbackRequest>>() {
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
        } catch (Exception e) {
          ;
        }
        bus.post(new UnknownErrorEvent(error));
      }
    });
  }

  public void onEvent(CallbackAvailabilityEvent event) {
    callbackApi.queryAvailability(
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

          @Override
          @DebugLog
          public void failure(RetrofitError error) {
            try {
              if (error.getResponse() != null) {
                CallbackException body = (CallbackException) error.getBodyAs(CallbackException.class);
                bus.post(new CallbackErrorEvent(body));
                return;
              }
            } catch (Exception e) {
              ;
            }
            bus.post(new UnknownErrorEvent(error));
          }
        }
    );
  }

  public void onEvent(CallbackAdminEvent event) {
    String strEndTime = null;

    if (event.end_time != null) {
      strEndTime = TimeHelper.serializeUTCTime(event.end_time);
    }
    callbackApi.queryCallbackAdmin(event.target, strEndTime, event.max, new Callback<Map<String, List<CallbackAdminRequest>>>() {
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
        } catch (Exception e) {
          ;
        }
        bus.post(new UnknownErrorEvent(error));
      }
    });
  }

  public void onEvent(CallbackDialogEvent event) {
    String strSyncUri;
    if (event.isFragment) {
      String strBaseUri = gmsEndpoint.getUrl();
      strSyncUri = new Uri.Builder()
          .encodedPath(strBaseUri)
          .appendPath(SERVICE_PATH)
          .appendEncodedPath(event.url)
          .toString();
    } else {
      strSyncUri = event.url;
    }

    String strGmsUser = sharedPreferences.getString(Globals.PROPERTY_GMS_USER, null);
    // TODO: Handle RuntimeExceptions resulting from Malformed URI
    Request.Builder builder = new Request.Builder()
        .url(strSyncUri);
    if (strGmsUser != null && !strGmsUser.isEmpty()) {
      builder.addHeader(GMS_USER, strGmsUser);
    }
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
        if (response.isSuccessful()) {
          try {
            callbackDialog = gson.fromJson(response.body().charStream(), CallbackDialog.class);
            success = true;
          } catch (Exception e) {
            Timber.e(e, "Exception while parsing Dialog response.");
          }
        } else {
          Timber.e("Negative response for Dialog request: %s", response);
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
    if (strGmsUser != null && !strGmsUser.isEmpty()) {
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
        if (response.isSuccessful()) {
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

            Log.d("CallbackApiManager", "CheckQueue Response: " + body);
            callbackQueuePosition = gson.fromJson(body, CallbackQueuePosition.class);
            */
            callbackQueuePosition = gson.fromJson(response.body().charStream(), CallbackQueuePosition.class);
            success = true;
          } catch (Exception e) {
            Timber.e(e, "Exception while parsing CheckQueue response.");
          }
        } else {
          Timber.e("Negative response for CheckQueue request: %s", response);
        }
        bus.post(new CallbackCheckQueueDoneEvent(success, callbackQueuePosition));
      }
    });
  }
}
