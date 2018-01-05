package com.nj.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.nj.EventBus.LockUpEvent;
import com.nj.EventBus.PassEvent;
import com.nj.EventBus.TemHumEvent;
import com.nj.Function.Func_Switch.mvp.module.SwitchImpl;
import com.nj.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import com.nj.Function.Func_Switch.mvp.view.ISwitchView;
import com.nj.State.DoorState.Door;
import com.nj.State.DoorState.State_Close;
import com.nj.State.DoorState.State_Open;
import com.nj.State.LockState.Lock;
import com.nj.State.LockState.State_Lockup;
import com.nj.State.LockState.State_Unlock;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;



/**
 * Created by zbsz on 2017/8/28.
 */

public class SwitchService extends Service implements ISwitchView {

    SwitchPresenter sp = SwitchPresenter.getInstance();

    String Last_Value;

    Disposable rx_delay;

    Disposable unlock_noOpen;

    Door door;

    Lock lock;
    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        sp.SwitchPresenterSetView(this);
        sp.switch_Open();
        Log.e("Message","ServiceStart");
        lock = new Lock(new State_Lockup(sp));
        door = new Door(new State_Close(lock));

        Observable.interval(0, 5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(@NonNull Long aLong) throws Exception {
                sp.readHum();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetPassEvent(PassEvent event) {
        lock.setLockState(new State_Unlock(sp));
        lock.doNext();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSwitchingText(String value) {
        if ((Last_Value == null || Last_Value.equals(""))) {
            if (value.startsWith("AAAAAA")) {
                Last_Value = value;
                if (value.equals("AAAAAA000000000000")) {
                    door.setDoorState(new State_Open(lock));
                    door.doNext();
                }
            }
        } else {
            if (value.startsWith("AAAAAA")) {
                if (!value.equals(Last_Value)) {
                    Last_Value = value;
                    if (Last_Value.equals("AAAAAA000000000000")) {
                        if(getDoorState(State_Close.class)){
                            door.setDoorState(new State_Open(lock));
                            door.doNext();
                        }
                        if (unlock_noOpen != null) {
                            unlock_noOpen.dispose();
                        }
                        if (rx_delay != null) {
                            rx_delay.dispose();
                        }
                    } else if (Last_Value.equals("AAAAAA000001000000")) {
                        Observable.timer(20, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                                .subscribe(new Observer<Long>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        rx_delay = d;
                                    }

                                    @Override
                                    public void onNext(Long aLong) {
                                        lock.setLockState(new State_Lockup(sp));
                                        door.setDoorState(new State_Close(lock));
                                        sp.buzz(SwitchImpl.Hex.H2);
                                        EventBus.getDefault().post(new LockUpEvent());
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                });
                    }
                }
                if (getLockState(State_Unlock.class)&& value.equals("AAAAAA000001000000")) {
                    Observable.timer(120, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                            .subscribe(new Observer<Long>() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                    unlock_noOpen = d;
                                }

                                @Override
                                public void onNext(Long aLong) {
                                    lock.setLockState(new State_Lockup(sp));
                                    EventBus.getDefault().post(new LockUpEvent());
                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onComplete() {
                                }
                            });
                }
            }
        }
    }

    @Override
    public void onTemHum(int temperature, int humidity) {
        EventBus.getDefault().post(new TemHumEvent(temperature, humidity));
    }



    private Boolean getDoorState(Class stateClass) {
        if (door.getDoorState().getClass().getName().equals(stateClass.getName())) {
            return true;
        } else {
            return false;
        }
    }
    private Boolean getLockState(Class stateClass) {
        if (lock.getLockState().getClass().getName().equals(stateClass.getName())) {
            return true;
        } else {
            return false;
        }
    }
}
