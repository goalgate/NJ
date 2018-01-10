package com.nj.Retrofit.InterfaceApi;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by zbsz on 2018/1/10.
 */

public interface QueryPersonInfoApi {
    @POST("queryPersonInfo.json")
    Observable<ResponseBody> queryPersonInfo (@Query("key") String key, @Query("id") String id);
}
