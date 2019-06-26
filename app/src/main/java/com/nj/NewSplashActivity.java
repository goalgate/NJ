package com.nj;

import android.os.Bundle;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.nj.Function.Fun_FingerPrint.mvp.presenter.FingerPrintPresenter;
import com.nj.Tools.DESX;
import com.nj.Tools.NetInfo;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.RxActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class NewSplashActivity extends RxActivity {
    public FingerPrintPresenter fpp = FingerPrintPresenter.getInstance();

    private SPUtils config = SPUtils.getInstance("config");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Observable.timer(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(NewSplashActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
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
                            config.put("firstStart", false);
                            config.put("daid", new NetInfo().getMacId());
                            config.put("key",DESX.encrypt(jsonKey.toString()));
                            config.put("ServerId","https://124.114.153.91:8447/wiscrisrest/deviceDocking/");
                        }
                        Observable.timer(3, TimeUnit.SECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .compose(NewSplashActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                                .subscribe(new Consumer<Long>() {
                                    @Override
                                    public void accept(Long aLong) throws Exception {
                                        ActivityUtils.startActivity(getPackageName(), getPackageName() + ".IndexActivity");
                                        NewSplashActivity.this.finish();
                                    }
                                });

                    }
                });
    }
}
