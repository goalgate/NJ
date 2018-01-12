package com.nj.Retrofit;

import android.text.TextUtils;

import com.blankj.utilcode.util.SPUtils;
import com.nj.AppInit;

import com.nj.Retrofit.InterfaceApi.AlarmRecordApi;
import com.nj.Retrofit.InterfaceApi.CheckRecordApi;
import com.nj.Retrofit.InterfaceApi.CloseDoorRecordApi;
import com.nj.Retrofit.InterfaceApi.FingerLogApi;
import com.nj.Retrofit.InterfaceApi.GetTimeApi;
import com.nj.Retrofit.InterfaceApi.OpenDoorRecordApi;
import com.nj.Retrofit.InterfaceApi.QueryPersonInfoApi;
import com.nj.Retrofit.InterfaceApi.SaveVisitApi;
import com.nj.Retrofit.InterfaceApi.SearchFingerApi;
import com.nj.Retrofit.InterfaceApi.StateRecordApi;
import com.nj.Retrofit.InterfaceApi.TestNetApi;
import com.nj.Tools.SSLParser;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit变量初始化
 * Created by SmileXie on 16/7/16.
 */
public class RetrofitGenerator {
    private static String TAG = "RetrofitGenerator";
    private static final String PREFS_NAME = "config";
    private static final String Uri = "https://113.140.1.137:8446/wiscrisrest/deviceDocking/";
    private static OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
    private static GetTimeApi getTimeApi;
    private static AlarmRecordApi alarmRecordApi;
    private static CheckRecordApi checkRecordApi;
    private static CloseDoorRecordApi closeDoorRecordApi;
    private static FingerLogApi fingerLogApi;
    private static OpenDoorRecordApi openDoorRecordApi;
    private static QueryPersonInfoApi queryPersonInfoApi;
    private static SearchFingerApi searchFingerApi;
    private static StateRecordApi stateRecordApi;
    private static TestNetApi testNetApi;
    private static SaveVisitApi saveVisitApi;
    private static <S> S createService(Class<S> serviceClass) {
        SSLParser.SSLParams sslParams = null;
        try {
            sslParams = SSLParser.getSSLParams(AppInit.getContext());
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        OkHttpClient client = okHttpClient.connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .sslSocketFactory(sslParams.getSslSocketFactory(), sslParams.trustManager)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(SPUtils.getInstance("config").getString("ServerId")).client(client).build();
        return retrofit.create(serviceClass);
    }


    public static GetTimeApi getTimeApi() {
        if (getTimeApi == null) {
            getTimeApi = createService(GetTimeApi.class);
        }
        return getTimeApi;
    }

    public static AlarmRecordApi getAlarmRecordApi() {
        if (alarmRecordApi == null) {
            alarmRecordApi = createService(AlarmRecordApi.class);
        }
        return alarmRecordApi;
    }

    public static CheckRecordApi getCheckRecordApi() {
        if (checkRecordApi == null) {
            checkRecordApi = createService(CheckRecordApi.class);
        }
        return checkRecordApi;
    }
    public static CloseDoorRecordApi getCloseDoorRecordApi() {
        if (closeDoorRecordApi == null) {
            closeDoorRecordApi = createService(CloseDoorRecordApi.class);
        }
        return closeDoorRecordApi;
    }

    public static FingerLogApi getFingerLogApi() {
        if (fingerLogApi == null) {
            fingerLogApi = createService(FingerLogApi.class);
        }
        return fingerLogApi;
    }

    public static OpenDoorRecordApi getOpenDoorRecordApi() {
        if (openDoorRecordApi == null) {
            openDoorRecordApi = createService(OpenDoorRecordApi.class);
        }
        return openDoorRecordApi;
    }

    public static QueryPersonInfoApi getQueryPersonInfoApi() {
        if (queryPersonInfoApi == null) {
            queryPersonInfoApi = createService(QueryPersonInfoApi.class);
        }
        return queryPersonInfoApi;
    }

    public static SearchFingerApi getSearchFingerApi() {
        if (searchFingerApi == null) {
            searchFingerApi = createService(SearchFingerApi.class);
        }
        return searchFingerApi;
    }
    public static StateRecordApi stateRecordApi() {
        if (stateRecordApi == null) {
            stateRecordApi = createService(StateRecordApi.class);
        }
        return stateRecordApi;
    }

    public static TestNetApi getTestNetApi() {
        if (testNetApi == null) {
            testNetApi = createService(TestNetApi.class);
        }
        return testNetApi;
    }

    public static SaveVisitApi getSaveVisitApi() {
        if (saveVisitApi == null) {
            saveVisitApi = createService(SaveVisitApi.class);
        }
        return saveVisitApi;
    }

    public static String getUri() {
        return Uri;
    }


}
