package com.genesys.gms.mobile.callback.demo.legacy.data.api;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.CallbackAdminRequest;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.CallbackDialog;
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
    public CallbackDialog startCallback(@Path("service_name") String serviceName,
                                                    @Body Map<String, String> params);

    @POST("/service/callback/{service_name}")
    public void startCallback(@Path("service_name") String serviceName,
                                                    @Body Map<String, String> params,
                                                    Callback<CallbackDialog> callback);

    @DELETE("/service/callback/{service_name}/{service_id}")
    public Response cancelCallback(@Path("service_name") String serviceName,
                                   @Path("service_id") String serviceID);

    @DELETE("/service/callback/{service_name}/{service_id}")
    public void cancelCallback(@Path("service_name") String serviceName,
                                   @Path("service_id") String serviceID,
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
                                                    @Query("start") DateTime start,
                                                    @Query("number-of-days") Integer numberOfDays,
                                                    @Query("end") DateTime end,
                                                    @Query("max-time-slots") Integer maxTimeSlots);

    @GET("/service/callback/{service_name}/availability")
    public void queryAvailability(@Path("service_name") String serviceName,
                                  @Query("start") DateTime start,
                                  @Query("number-of-days") Integer numberOfDays,
                                  @Query("end") DateTime end,
                                  @Query("max-time-slots") Integer maxTimeSlots,
                                  Callback<Map<DateTime, Integer>> callback);

    @GET("/admin/callback/queues")
    public Map<String, List<CallbackAdminRequest>> queryCallbackAdmin(@Query("target") String targetName,
                                                                      @Query("end_time") String endTime,
                                                                      @Query("max") Integer max);

    @GET("/admin/callback/queues")
    public void queryCallbackAdmin(@Query("target") String targetName,
                                      @Query("end_time") String endTime,
                                      @Query("max") Integer max,
                                      Callback<Map<String, List<CallbackAdminRequest>>> callback);
}