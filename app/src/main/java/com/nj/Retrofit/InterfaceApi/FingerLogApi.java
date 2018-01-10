package com.nj.Retrofit.InterfaceApi;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by zbsz on 2018/1/10.
 */

public interface FingerLogApi {
    @POST("fingerLog.json")
    Observable<ResponseBody> fingerLog(@Query("key") String key);
}
