package com.genesys.gms.mobile.callback.demo.legacy.data.api;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.*;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.*;
import com.genesys.gms.mobile.callback.demo.legacy.util.TimeHelper;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import org.joda.time.DateTime;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stau on 2/3/2015.
 */
@Singleton
public class CallbackServiceManager {
    private final CallbackService callbackService;
    private final EventBus bus;

    @DebugLog @Inject
    public CallbackServiceManager(CallbackService callbackService) {
        this.callbackService = callbackService;
        this.bus = EventBus.getDefault();
    }

    public void onEvent(CallbackStartEvent event) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("_customer_number", event._customer_number);
        if(event._desired_time != null) {
            params.put("_desired_time", TimeHelper.serializeUTCTime(event._desired_time));
        }
        params.put("_callback_state", event._callback_state);
        params.put("_urs_virtual_queue", event._urs_virtual_queue);
        params.put("_request_queue_time_stat", event._request_queue_time_stat);
        if(event.properties != null) {
            params.putAll(event.properties);
        }

        callbackService.startCallback(event.serviceName, params, new Callback<CallbackConfirmationDialog>() {
            @Override
            public void success(CallbackConfirmationDialog callbackConfirmationDialog, Response response) {
                bus.post(new CallbackStartDoneEvent(callbackConfirmationDialog));
            }

            @Override
            public void failure(RetrofitError error) {
                try {
                    if (error.getResponse() != null) {
                        CallbackException body = (CallbackException) error.getBodyAs(CallbackException.class);
                        bus.post(new CallbackErrorEvent(body));
                    }
                } catch (RuntimeException ex) {
                    bus.post(new UnknownErrorEvent(error));
                }
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
                    }
                } catch (RuntimeException ex) {
                    bus.post(new UnknownErrorEvent(error));
                }
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
                    }
                } catch (RuntimeException ex) {
                    bus.post(new UnknownErrorEvent(error));
                }
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
                    }
                } catch (RuntimeException ex) {
                    bus.post(new UnknownErrorEvent(error));
                }
            }
        });
    }

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

                @Override
                public void failure(RetrofitError error) {
                    try {
                        if (error.getResponse() != null) {
                            CallbackException body = (CallbackException) error.getBodyAs(CallbackException.class);
                            bus.post(new CallbackErrorEvent(body));
                        }
                    } catch (RuntimeException ex) {
                        bus.post(new UnknownErrorEvent(error));
                    }
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
                    }
                } catch (RuntimeException ex) {
                    bus.post(new UnknownErrorEvent(error));
                }
            }
        });
    }
}
