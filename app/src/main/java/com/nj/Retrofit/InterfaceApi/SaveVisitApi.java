package com.nj.Retrofit.InterfaceApi;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by zbsz on 2018/1/12.
 */

public interface SaveVisitApi {

    @POST("saveVisit.json")
    Observable<ResponseBody> saveVisit(@Query("key") String key, @Query("jsonData") String jsonData);

}