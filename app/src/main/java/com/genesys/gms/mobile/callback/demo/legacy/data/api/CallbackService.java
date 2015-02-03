package com.genesys.gms.mobile.callback.demo.legacy.data.api;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.CallbackAdminRequest;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.CallbackConfirmationDialog;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.CallbackRequest;
import org.joda.time.DateTime;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.*;

import java.util.List;
import java.util.Map;

/**
 * Created by stau on 06/10/2014.
 */
public interface CallbackService {
    @POST("/service/callback/{service_name}")
    public CallbackConfirmationDialog startCallback(@Path("service_name") String serviceName,
                                                    @Body Map<String, String> params);

    @POST("/service/callback/{service_name}")
    public void startCallback(@Path("service_name") String serviceName,
                                                    @Body Map<String, String> params,
                                                    Callback<CallbackConfirmationDialog> callback);

    @DELETE("/service/callback/{service_name}/{service_id}")
    public Response cancelCallback(@Path("service_name") String serviceName,
                                   @Path("service_id") String serviceID,
                                   @Body Map<String, String> params);

    @DELETE("/service/callback/{service_name}/{service_id}")
    public void cancelCallback(@Path("service_name") String serviceName,
                                   @Path("service_id") String serviceID,
                                   @Body Map<String, String> params,
                                   Callback<Response> callback);

    @PUT("/service/callback/{service_name}/{service_id}")
    public Response updateCallback(@Path("service_name") String serviceName,
                                   @Path("service_id") String serviceID,
                                   @Body Map<String, String> params);

    @PUT("/service/callback/{service_name}/{service_id}")
    public void updateCallback(@Path("service_name") String serviceName,
                                   @Path("service_id") String serviceID,
                                   @Body Map<String, String> params,
                                   Callback<Response> callback);

    @GET("/service/callback/{service_name}")
    public List<CallbackRequest> queryCallback(@Path("service_name") String serviceName,
                                               @QueryMap Map<String, String> params);

    @GET("/service/callback/{service_name}")
    public void queryCallback(@Path("service_name") String serviceName,
                               @QueryMap Map<String, String> params,
                               Callback<List<CallbackRequest>> callback);

    @GET("/service/callback/{service_name}/availability")
    public Map<DateTime, Integer> queryAvailability(@Path("service_name") String serviceName,
                                                    @Query("timestamp") String timestamp);

    @GET("/service/callback/{service_name}/availability")
    public void queryAvailability(@Path("service_name") String serviceName,
                                    @Query("timestamp") String timestamp,
                                    Callback<Map<DateTime, Integer>> callback);

    @GET("/admin/callback/queues")
    public Map<String, List<CallbackAdminRequest>> queryCallbackAdmin(@Query("target") String targetName,
                                                                      @Query("end_time") String endTime,
                                                                      @Query("max") int max);

    @GET("/admin/callback/queues")
    public void queryCallbackAdmin(@Query("target") String targetName,
                                      @Query("end_time") String endTime,
                                      @Query("max") int max,
                                      Callback<Map<String, List<CallbackAdminRequest>>> callback);
}