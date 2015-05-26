package com.genesys.gms.mobile.callback.demo.legacy.data.api;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.ChatBasicResponse;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.ChatResponse;
import retrofit.Callback;
import retrofit.http.*;

import java.util.Map;

/**
 * Created by stau on 2/6/2015.
 */
public interface ChatApi {
  @FormUrlEncoded
  @POST("/service/{service_id}/ixn/chat")
  public ChatResponse startChat(@Path("service_id") String serviceId,
                                @Field("_verbose") boolean verbose,
                                @Field("notify_by") String notifyBy, // Could be enumerated
                                @Field("firstName") String firstName,
                                @Field("lastName") String lastName,
                                @Field("email") String email,
                                @Field("subject") String subject,
                                @Field("subscriptionID") String subscriptionId,
                                @Field("userDisplayName") String userDisplayName);

  @FormUrlEncoded
  @POST("/service/{service_id}/ixn/chat")
  public void startChat(@Path("service_id") String serviceId,
                        @Field("_verbose") boolean verbose,
                        @Field("notify_by") String notifyBy,
                        @Field("firstName") String firstName,
                        @Field("lastName") String lastName,
                        @Field("email") String email,
                        @Field("subject") String subject,
                        @Field("subscriptionID") String subscriptionId,
                        @Field("userDisplayName") String userDisplayName,
                        @Field("push_notification_deviceid") String pushNotificationDeviceId,
                        @Field("push_notification_type") String pushNotificationType,
                        @Field("push_notification_language") String pushNotificationLanguage,
                        @Field("push_notification_debug") boolean pushNotificationDebug,
                        Callback<ChatResponse> callback);

  @FormUrlEncoded
  @POST("/service/{service_id}/ixn/chat/send")
  public ChatResponse send(@Path("service_id") String serviceId,
                           @Field("message") String message,
                           @Field("_verbose") boolean verbose);

  @FormUrlEncoded
  @POST("/service/{service_id}/ixn/chat/send")
  public void send(@Path("service_id") String serviceId,
                   @Field("message") String message,
                   @Field("_verbose") boolean verbose,
                   Callback<ChatResponse> callback);

  @FormUrlEncoded
  @POST("/service/{service_id}/ixn/chat/refresh")
  public ChatResponse refresh(@Path("service_id") String serviceId,
                              @Field("transcriptPosition") int transcriptPosition,
                              @Field("message") String message,
                              @Field("_verbose") boolean verbose);

  @FormUrlEncoded
  @POST("/service/{service_id}/ixn/chat/refresh")
  public void refresh(@Path("service_id") String serviceId,
                      @Field("transcriptPosition") int transcriptPosition,
                      @Field("message") String message,
                      @Field("_verbose") boolean verbose,
                      Callback<ChatResponse> callback);

  @FormUrlEncoded
  @POST("/service/{service_id}/ixn/chat/startTyping")
  public ChatResponse startTyping(@Path("service_id") String serviceId,
                                  @Field("_verbose") boolean verbose);

  @FormUrlEncoded
  @POST("/service/{service_id}/ixn/chat/startTyping")
  public void startTyping(@Path("service_id") String serviceId,
                          @Field("_verbose") boolean verbose,
                          Callback<ChatResponse> callback);

  @FormUrlEncoded
  @POST("/service/{service_id}/ixn/chat/stopTyping")
  public ChatResponse stopTyping(@Path("service_id") String serviceId,
                                 @Field("_verbose") boolean verbose);

  @FormUrlEncoded
  @POST("/service/{service_id}/ixn/chat/stopTyping")
  public void stopTyping(@Path("service_id") String serviceId,
                         @Field("_verbose") boolean verbose,
                         Callback<ChatResponse> callback);

  @FormUrlEncoded
  @POST("/service/{service_id}/ixn/chat/disconnect")
  public ChatResponse disconnect(@Path("service_id") String serviceId,
                                 @Field("_verbose") boolean verbose);

  @FormUrlEncoded
  @POST("/service/{service_id}/ixn/chat/disconnect")
  public void disconnect(@Path("service_id") String serviceId,
                         @Field("_verbose") boolean verbose,
                         Callback<ChatResponse> callback);

  @FormUrlEncoded
  @POST("/service/request-chat")
  public ChatBasicResponse basicChat(@Field("_verbose") boolean verbose,
                                     @FieldMap Map<String, String> params);

  @FormUrlEncoded
  @POST("/service/request-chat")
  public void basicChat(@Field("_verbose") boolean verbose,
                        @FieldMap Map<String, String> params,
                        Callback<ChatBasicResponse> callback);
}
