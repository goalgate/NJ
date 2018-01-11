package com.nj;

import android.app.Activity;
import android.os.Bundle;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.nj.Function.Fun_FingerPrint.mvp.presenter.FingerPrintPresenter;

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

        if (SP_Config.getBoolean("firstStart", true)) {
            ActivityUtils.startActivity(getPackageName(),getPackageName()+".StartActivity");
            this.finish();
        }else {
            fpp.fpInit();
            fpp.fpOpen();
            ActivityUtils.startActivity(getPackageName(), getPackageName() + ".IndexActivity");
            this.finish();
        }
    }
}
