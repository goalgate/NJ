package com.nj;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.nj.Function.Fun_FingerPrint.mvp.presenter.FingerPrintPresenter;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by zbsz on 2017/12/8.
 */

public class SplashActivity extends Activity {
    public FingerPrintPresenter fpp = FingerPrintPresenter.getInstance();

    private static final String PREFS_NAME = "config";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        SPUtils SP_Config = SPUtils.getInstance(PREFS_NAME);
        fpp.fpInit();
        fpp.fpOpen();
        if (SP_Config.getBoolean("firstStart", true)) {
            ActivityUtils.startActivity(getPackageName(),getPackageName()+".StartActivity");
            this.finish();
        }else {
            Observable.timer(3, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NonNull Long aLong) {
                            ActivityUtils.startActivity(getPackageName(),getPackageName()+".IndexActivity");
                            SplashActivity.this.finish();
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {

                        }

                        @Override
                        public void onComplete() {


                        }
                    });

        }
    }
}
