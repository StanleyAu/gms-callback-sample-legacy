package com.genesys.gms.mobile.callback.demo.legacy.data.api;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.*;
import retrofit.mime.TypedFile;

import java.util.Map;

/**
 * Created by stau on 5/7/2015.
 */
public interface StorageService {
    @FormUrlEncoded
    @POST("/storage/{ttl}")
    public Response create(@Path("ttl") int ttl,
                           @FieldMap Map<String, String> payload);

    @FormUrlEncoded
    @POST("/storage/{ttl}")
    public void create(@Path("ttl") int ttl,
                       @FieldMap Map<String, String> payload,
                       Callback<Response> callback);

    @FormUrlEncoded
    @POST("/storage/{storage_id}/{ttl}")
    public Response update(@Path("storage_id") String storageId,
                           @Path("ttl") int ttl,
                           @FieldMap Map<String, String> payload);

    @FormUrlEncoded
    @POST("/storage/{storage_id}/{ttl}")
    public void update(@Path("storage_id") String storageId,
                       @Path("ttl") int ttl,
                       @FieldMap Map<String, String> payload,
                       Callback<Response> callback);

    @Multipart
    @POST("/storage/{storage_id}/{ttl}")
    public Response updateBinary(@Path("storage_id") String storageId,
                           @Path("ttl") int ttl,
                           @PartMap Map<String, TypedFile> params);

    @Multipart
    @POST("/storage/{storage_id}/{ttl}")
    public void updateBinary(@Path("storage_id") String storageId,
                       @Path("ttl") int ttl,
                       @PartMap Map<String, TypedFile> params,
                       Callback<Response> callback);

    @GET("/storage/{storage_id}")
    public Map<String, String> queryAllKeys(@Path("storage_id") String storageId);

    @GET("/storage/{storage_id}")
    public void queryAllKeys(@Path("storage_id") String storageId,
                             Callback<Map<String, String>> callback);

    @GET("/storage/{storage_id}/{key}")
    public Response queryOneKey(@Path("storage_id") String storageId,
                                @Path("key") String key);

    @GET("/storage/{storage_id}/{key}")
    public void queryOneKey(@Path("storage_id") String storageId,
                            @Path("key") String key,
                            Callback<Response> callback);

    @GET("/storage/binary/{storage_id}/{key}")
    public Response queryBinary(@Path("storage_id") String storageId,
                                @Path("key") String key);

    @GET("/storage/binary/{storage_id}/{key}")
    public void queryBinary(@Path("storage_id") String storageId,
                            @Path("key") String key,
                            Callback<Response> callback);

    @DELETE("/storage/{storage_id}")
    public Response delete(@Path("storage_id") String storageId);

    @DELETE("/storage/{storage_id}")
    public void delete(@Path("storage_id") String storageId,
                       Callback<Response> callback);
}
