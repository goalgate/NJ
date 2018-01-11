package com.nj;

import android.os.Bundle;
import android.view.SurfaceView;

import com.blankj.utilcode.util.BarUtils;


import com.nj.Function.Fun_FingerPrint.mvp.presenter.FingerPrintPresenter;
import com.nj.Function.Fun_FingerPrint.mvp.view.IFingerPrintView;
import com.nj.Function.Func_Camera.mvp.presenter.PhotoPresenter;
import com.nj.Function.Func_Camera.mvp.view.IPhotoView;
import com.nj.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import com.nj.Function.Func_IDCard.mvp.view.IIDCardView;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.RxActivity;
import com.ys.myapi.MyManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by zbsz on 2017/11/27.
 */

public abstract class FunctionActivity extends RxActivity implements IPhotoView,IFingerPrintView,IIDCardView {
    public IDCardPresenter idp = IDCardPresenter.getInstance();

    public FingerPrintPresenter fpp = FingerPrintPresenter.getInstance();

    public PhotoPresenter pp = PhotoPresenter.getInstance();

    public SurfaceView surfaceView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        pp.initCamera();
        idp.idCardOpen();
        fpp.fpInit();
        fpp.fpOpen();
    }

    @Override
    public void onStart() {
        super.onStart();
        pp.setParameter(surfaceView.getHolder());
    }



    @Override
    public void onRestart() {
        super.onRestart();
        pp.initCamera();

    }

    @Override
    public void onResume() {
        super.onResume();
        pp.PhotoPresenterSetView(this);
        pp.setDisplay(surfaceView.getHolder());
        fpp.FingerPrintPresenterSetView(this);
        Observable.timer(8, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        fpp.fpIdentify();
                    }
                });
        idp.IDCardPresenterSetView(this);
        idp.readCard();
    }

    @Override
    public void onPause() {
        super.onPause();
        fpp.fpCancel(true);
        fpp.FingerPrintPresenterSetView(null);
        pp.PhotoPresenterSetView(null);
        idp.stopReadCard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fpp.fpClose();
        idp.idCardClose();
    }
}
