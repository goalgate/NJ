package com.nj.Alerts;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nj.AppInit;
import com.nj.EventBus.NetworkEvent;
import com.nj.Function.Func_Camera.mvp.presenter.PhotoPresenter;
import com.nj.R;
import com.nj.Retrofit.RetrofitGenerator;
import com.nj.Tools.DAInfo;


import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class Alert_Server {


    //    private Context context;
//
//    private SPUtils config = SPUtils.getInstance("config");
//
//    String url;
//    private AlertView inputServerView;
//    private EditText etName;
//    private ImageView QRview;
//
//    public Alert_Server(Context context) {
//        this.context = context;
//    }
//
//    public void serverInit(final Server_Callback callback) {
//        ViewGroup extView1 = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.inputserver_form, null);
//        etName = (EditText) extView1.findViewById(R.id.server_input);
//        QRview = (ImageView) extView1.findViewById(R.id.QRimage);
//        inputServerView = new AlertView("服务器设置", null, "取消", new String[]{"确定"}, null, this.context, AlertView.Style.Alert, new OnItemClickListener() {
//            @Override
//            public void onItemClick(Object o, int position) {
//                if (position == 0) {
//                    if (!etName.getText().toString().replaceAll(" ", "").endsWith("/")) {
//                        url = etName.getText().toString() + "/";
//                    } else {
//                        url = etName.getText().toString();
//                    }
//                    SSLParser.SSLParams sslParams = null;
//                    try {
//                        sslParams = SSLParser.getSSLParams(AppInit.getContext());
//                    } catch (CertificateException e) {
//                        e.printStackTrace();
//                    } catch (NoSuchAlgorithmException e) {
//                        e.printStackTrace();
//                    } catch (KeyStoreException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch (KeyManagementException e) {
//                        e.printStackTrace();
//                    }
//                    OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
//                            .writeTimeout(30, TimeUnit.SECONDS)
//                            .readTimeout(30, TimeUnit.SECONDS)
//                            .sslSocketFactory(sslParams.getSslSocketFactory(), sslParams.trustManager)
//                            .hostnameVerifier(new HostnameVerifier() {
//                                @Override
//                                public boolean verify(String hostname, SSLSession session) {
//                                    return true;
//                                }
//                            }).build();
//                    new Retrofit.Builder()
//                            .addConverterFactory(GsonConverterFactory.create())
//                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                            .baseUrl(url).client(client).build().create(TestNetApi.class)
//                            .testNet(config.getString("key"))
//                            .subscribeOn(Schedulers.io())
//                            .unsubscribeOn(Schedulers.io())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(new Observer<ResponseBody>() {
//                                @Override
//                                public void onSubscribe(@NonNull Disposable d) {
//
//                                }
//
//                                @Override
//                                public void onNext(@NonNull ResponseBody responseBody) {
//                                    try {
//                                        Map<String, String> infoMap = new Gson().fromJson(responseBody.string(),
//                                                new TypeToken<HashMap<String, String>>() {
//                                                }.getType());
//
//                                        if (infoMap.get("result").equals("true")) {
//                                            config.put("ServerId", url);
//                                            ToastUtils.showLong("连接服务器成功");
//                                            callback.setNetworkBmp();
//                                        } else {
//                                            ToastUtils.showLong("连接服务器失败");
//                                        }
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//
//                                @Override
//                                public void onError(@NonNull Throwable e) {
//                                    ToastUtils.showLong("服务器连接失败");
//                                }
//
//                                @Override
//                                public void onComplete() {
//
//                                }
//                            });
////                    OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
////                            .writeTimeout(30, TimeUnit.SECONDS)
////                            .readTimeout(30, TimeUnit.SECONDS)
////                            .build();
////                    new Retrofit.Builder()
////                            .addConverterFactory(GsonConverterFactory.create())
////                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
////                            .baseUrl(url).client(client).build().create(ConnectApi.class)
////                            .noData("testNet", config.getString("key"))
////                            .subscribeOn(Schedulers.io())
////                            .unsubscribeOn(Schedulers.io())
////                            .observeOn(AndroidSchedulers.mainThread())
////                            .subscribe(new Observer<String>() {
////                                @Override
////                                public void onSubscribe(@NonNull Disposable d) {
////
////                                }
////
////                                @Override
////                                public void onNext(String s) {
////                                    if (s.equals("true")) {
////                                        config.put("ServerId", url);
////                                        ToastUtils.showLong("连接服务器成功");
////                                        callback.setNetworkBmp();
////                                        //iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.wifi));
////                                    } else {
////                                        ToastUtils.showLong("连接服务器失败");
////                                    }
////                                }
////
////                                @Override
////                                public void onError(@NonNull Throwable e) {
////                                    ToastUtils.showLong("服务器连接失败");
////                                }
////
////                                @Override
////                                public void onComplete() {
////
////                                }
////                            });
//                }
//            }
//        });
//        inputServerView.addExtView(extView1);
//    }
//
//
//
//    public void show() {
//        Bitmap mBitmap = null;
//        etName.setText(config.getString("ServerId"));
//        DAInfo di = new DAInfo();
//        try {
//            di.setId(config.getString("daid"));
//            di.setName("数据采集器");
//            di.setModel("CBDI-P-ID");
//            di.setSoftwareVer(AppUtils.getAppVersionName());
//            di.setProject("NJGY");
//            mBitmap = di.daInfoBmp();
//        } catch (Exception ex) {
//
//        }
//        if (mBitmap != null) {
//            QRview.setImageBitmap(mBitmap);
//        }
//        inputServerView.show();
//    }
//
//    public interface Server_Callback {
//        void setNetworkBmp();
//    }
//
//    ;
    private Context context;

    int count = 5;

    private SPUtils config = SPUtils.getInstance("config");

    String url;
    private AlertView inputServerView;
    private EditText etName;
    private ImageView QRview;
    private Button connect;

    public Alert_Server(Context context) {
        this.context = context;
    }

    public void serverInit(final Server_Callback callback) {
        ViewGroup extView1 = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.inputserver_form, null);
        etName = (EditText) extView1.findViewById(R.id.server_input);
        QRview = (ImageView) extView1.findViewById(R.id.QRimage);
        connect = (Button) extView1.findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etName.getText().toString().replaceAll(" ", "").endsWith("/")) {
                    url = etName.getText().toString() + "/";
                } else {
                    url = etName.getText().toString();
                }
                RetrofitGenerator.getConnectApi(url).testNet(config.getString("key")).
                        subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        try {
                            Map<String, String> infoMap = new Gson().fromJson(responseBody.string(),
                                    new TypeToken<HashMap<String, String>>() {
                                    }.getType());
                            if (!TextUtils.isEmpty(infoMap.get("result"))) {
                                if (infoMap.get("result").equals("true")) {
                                    config.put("ServerId", url);
                                    ToastUtils.showLong("连接服务器成功");
                                    callback.setNetworkBmp();
                                } else {
                                    ToastUtils.showLong("连接服务器失败");
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        EventBus.getDefault().post(new NetworkEvent(false));
                    }

                    @Override
                    public void onComplete() {

                    }
                });

            }
        });
        inputServerView = new AlertView("服务器设置", null, "取消", new String[]{"确定"}, null, context, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    Observable.interval(0, 1, TimeUnit.SECONDS)
                            .take(count + 1)
                            .map(new Function<Long, Long>() {
                                @Override
                                public Long apply(@NonNull Long aLong) throws Exception {
                                    return count - aLong;
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Long>() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {

                                }

                                @Override
                                public void onNext(@NonNull Long aLong) {
                                    ToastUtils.showLong(aLong + "秒后重新开机保存设置");
                                }

                                @Override
                                public void onError(@NonNull Throwable e) {

                                }

                                @Override
                                public void onComplete() {
                                    PhotoPresenter.getInstance().close_Camera();
                                    AppInit.getMyManager().reboot();
                                }
                            });
                }
            }
        });
        inputServerView.addExtView(extView1);
    }


    public void show() {
        Bitmap mBitmap = null;
        etName.setText(config.getString("ServerId"));
        DAInfo di = new DAInfo();
        try {
            di.setId(config.getString("daid"));
            di.setName("数据采集器");
            di.setModel("CBDI-P-IC");
            di.setSoftwareVer(AppUtils.getAppVersionName());
            di.setProject("NJGY");
            mBitmap = di.daInfoBmp();
        } catch (Exception ex) {

        }
        if (mBitmap != null) {
            QRview.setImageBitmap(mBitmap);
        }
        inputServerView.show();
    }

    public interface Server_Callback {
        void setNetworkBmp();
    }

}
