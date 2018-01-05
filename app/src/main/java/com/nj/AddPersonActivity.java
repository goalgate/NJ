package com.nj;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;

import com.jakewharton.rxbinding2.view.RxView;
import com.nj.EventBus.OpenDoorEvent;
import com.nj.Function.Fun_FingerPrint.mvp.presenter.FingerPrintPresenter;
import com.nj.Function.Fun_FingerPrint.mvp.view.IFingerPrintView;
import com.nj.Tools.FileUtils;
import com.nj.Tools.User;
import java.util.concurrent.TimeUnit;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;


/**
 * Created by zbsz on 2017/7/31.
 */

public class AddPersonActivity extends Activity implements IFingerPrintView {

    FingerPrintPresenter fpp = FingerPrintPresenter.getInstance();

    @BindView(R.id.iv_finger)
    ImageView img_finger;

    @BindView(R.id.et_finger)
    TextView tv_finger;

    @BindView(R.id.btn_commit)
    Button btn_commit;




    @OnClick(R.id.btn_commit)
    void commit() {
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_add_person);
        ButterKnife.bind(this);
        btn_commit.setClickable(false);
        RxView.clicks(img_finger).throttleFirst(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        if (!btn_commit.isClickable()) {
                            fpp.fpEnroll(String.valueOf(fpp.fpGetEmptyID()));
                        }
                    }
                });
    }

    @Override
    public void onSetImg(Bitmap bmp) {
        img_finger.setImageBitmap(bmp);
    }

    @Override
    public void onText(String msg) {
        if (!msg.equals("Canceled")) {
            tv_finger.setText(msg);
        }
        if (msg.endsWith("录入成功")) {
            btn_commit.setClickable(true);
        }
        if (msg.equals("指纹模板已存在")) {
            fpp.fpEnroll(String.valueOf(fpp.fpGetEmptyID()));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        fpp.FingerPrintPresenterSetView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fpp.fpCancel(true);
        //fpp.FingerPrintPresenterSetView(null);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onFpSucc(String msg) {

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}


