package com.nj.Retrofit.InterfaceApi;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ConnectApi {
    @FormUrlEncoded
    @POST("alarmRecord.json")
    Observable<ResponseBody> alarmRecord (@Field("key") String key, @Field("jsonData") String jsonData);

    @FormUrlEncoded
    @POST("checkRecord.json")
    Observable<ResponseBody> checkRecord (@Field("key") String key, @Field("jsonData") String jsonData);

    @FormUrlEncoded
    @POST("closeDoorRecord.json")
    Observable<ResponseBody> closeDoorRecord (@Field("key") String key, @Field("jsonData") String jsonData);

    @FormUrlEncoded
    @POST("deleteFinger.json")
    Observable<ResponseBody> deleteFinger(@Field("key") String key, @Field("jsonData") String jsonData);

    @FormUrlEncoded
    @POST("fingerLog.json")
    Observable<ResponseBody> fingerLog(@Field("key") String key, @Field("jsonData") String jsonData);

    @FormUrlEncoded
    @POST("getTime.json")
    Observable<ResponseBody> getTime(@Field("key") String key);

    @FormUrlEncoded
    @POST("openDoorRecord.json")
    Observable<ResponseBody> openDoorRecord (@Field("key") String key, @Field("jsonData") String jsonData);

    @FormUrlEncoded
    @POST("queryPersonInfo.json")
    Observable<ResponseBody> queryPersonInfo (@Field("key") String key, @Field("id") String id);

    @FormUrlEncoded
    @POST("saveVisit.json")
    Observable<ResponseBody> saveVisit(@Field("key") String key, @Field("jsonData") String jsonData);

    @FormUrlEncoded
    @POST("searchFinger.json")
    Observable<ResponseBody> searchFinger(@Field("key") String key, @Field("jsonData") String jsonData);

    @FormUrlEncoded
    @POST("stateRecord.json")
    Observable<ResponseBody> stateRecord(@Field("key") String key, @Field("jsonData") String jsonData);

    @FormUrlEncoded
    @POST("testNet.json")
    Observable<ResponseBody> testNet(@Field("key") String key);

    @FormUrlEncoded
    @POST("{method}")
    Observable<ResponseBody> universal(@Path("method") String method, @Field("key") String key,@Field("jsonData") String jsonData);
}

