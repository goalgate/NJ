package com.nj.Retrofit.InterfaceApi;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by zbsz on 2018/1/10.
 */

public interface CheckRecordApi {
    @FormUrlEncoded
    @POST("checkRecord.json")
    Observable<ResponseBody> checkRecord (@Field("key") String key, @Field("jsonData") String jsonData);

}
