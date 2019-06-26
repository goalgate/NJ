package com.nj.Tools;


import android.app.ProgressDialog;
import android.content.Context;
import android.view.Gravity;

import com.nj.Alerts.Alarm;

import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MyObserver<T> implements Observer<T>{
    private Context context;

    private ProgressDialog progressDialog;

    Boolean delay;

    public MyObserver(Context context) {
        this.context = context;
        this.progressDialog = new ProgressDialog(context);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        progressDialog.setMessage("数据上传中，请稍候");
        delay = false;
    }

    public MyObserver(Context context, Boolean delay) {
        this.context = context;
        this.progressDialog = new ProgressDialog(context);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        progressDialog.setMessage("数据上传中，请稍候");
        this.delay = delay;
    }

    @Override
    public void onSubscribe(Disposable d) {
        progressDialog.show();
    }

    @Override
    public void onNext(T t) {

    }

    @Override
    public void onError(Throwable e) {
        progressDialog.dismiss();
        if(delay){
            Observable.timer(1,TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            Alarm.getInstance(context).messageAlarm("无法连接服务器，请检查网络");
                        }
                    });
        }else {
            Alarm.getInstance(context).messageAlarm("无法连接服务器，请检查网络");
        }

    }

    @Override
    public void onComplete() {
        progressDialog.dismiss();
    }
}
