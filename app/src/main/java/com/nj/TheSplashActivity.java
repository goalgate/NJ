package com.nj;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import com.baidu.aip.manager.FaceSDKManager;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.SPUtils;
import com.nj.Function.Fun_FingerPrint.mvp.presenter.FingerPrintPresenter;
import com.nj.Function.Func_Face.mvp.presenter.FacePresenter;
import com.nj.Tools.AssetsUtils;
import com.nj.Tools.DESX;
import com.nj.Tools.NetInfo;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.RxActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class TheSplashActivity extends RxActivity {
    String TAG = SplashActivity.class.getSimpleName();

    public FingerPrintPresenter fpp = FingerPrintPresenter.getInstance();
    private SPUtils config = SPUtils.getInstance("config");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_splash);
        try {
            File key = new File(Environment.getExternalStorageDirectory() + File.separator + "key.txt");
            copyToClipboard(AppInit.getContext(), FileIOUtils.readFile2String(key));
        } catch (Exception e) {
            e.printStackTrace();
        }

        FacePresenter.getInstance().FaceInit(this, new FaceSDKManager.SdkInitListener() {
            @Override
            public void initStart() {
                Log.e(TAG, "sdk init start");
            }

            @Override
            public void initSuccess() {
                Log.e(TAG, "sdk init success");
                Observable.timer(3, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(TheSplashActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) throws Exception {
                                fpp.fpInit();
                                fpp.fpOpen();
                                if (config.getBoolean("firstStart", true)) {
                                    JSONObject jsonKey = new JSONObject();
                                    try {
                                        jsonKey.put("daid", new NetInfo().getMacId());
                                        jsonKey.put("check", DESX.encrypt(new NetInfo().getMacId()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    AssetsUtils.getInstance(AppInit.getContext()).copyAssetsToSD("wltlib","wltlib");
                                    config.put("firstStart", false);
                                    config.put("daid", new NetInfo().getMacId());
                                    config.put("key", DESX.encrypt(jsonKey.toString()));
                                    config.put("ServerId", "https://124.114.153.91:8447/wiscrisrest/deviceDocking/");
                                }
                                Observable.timer(3, TimeUnit.SECONDS)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .compose(TheSplashActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                                        .subscribe(new Consumer<Long>() {
                                            @Override
                                            public void accept(Long aLong) throws Exception {
                                                ActivityUtils.startActivity(getPackageName(), getPackageName() + ".IndexActivity");
                                            }
                                        });
                            }
                        });
            }

            @Override
            public void initFail(int errorCode, String msg) {
                Log.e(TAG, "sdk init fail:" + msg);
            }
        });
    }

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager systemService = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        systemService.setPrimaryClip(ClipData.newPlainText("text", text));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
