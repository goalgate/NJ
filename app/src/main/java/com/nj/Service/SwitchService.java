package com.nj.Service;

import android.app.Service;
import android.content.Intent;
import android.media.session.PlaybackState;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nj.AppInit;
import com.nj.Bean.ReUploadBean;
import com.nj.EventBus.AlarmEvent;
import com.nj.EventBus.LockUpEvent;
import com.nj.EventBus.NetworkEvent;
import com.nj.EventBus.PassEvent;
import com.nj.EventBus.TemHumEvent;
import com.nj.Function.Func_Switch.mvp.module.SwitchImpl;
import com.nj.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import com.nj.Function.Func_Switch.mvp.view.ISwitchView;
import com.nj.Retrofit.RetrofitGenerator;
import com.nj.State.DoorState.Door;
import com.nj.State.DoorState.State_Close;
import com.nj.State.DoorState.State_Open;
import com.nj.State.LockState.Lock;
import com.nj.State.LockState.State_Lockup;
import com.nj.State.LockState.State_Unlock;
import com.nj.greendao.DaoSession;
import com.nj.greendao.ReUploadBeanDao;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;


/**
 * Created by zbsz on 2017/8/28.
 */

public class SwitchService extends Service implements ISwitchView {

    SPUtils config = SPUtils.getInstance("config");

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();


    SwitchPresenter sp = SwitchPresenter.getInstance();

    String Last_Value;

    int last_mTemperature = 0;

    int last_mHumidity = 0;

    String THSwitchValue;

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
        Log.e("Message", "ServiceStart");
        lock = Lock.getInstance(new State_Lockup(sp));
        door = Door.getInstance(new State_Close(lock));
        reboot();
        reUpload();
        Observable.interval(0, 5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(@NonNull Long aLong) throws Exception {
                sp.readHum();
            }
        });
        Observable.interval(0, 30, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        testNet();
                    }
                });
        Observable.interval(10, 30, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        StateRecord();
                    }
                });
    }

    private void reUpload() {
        final ReUploadBeanDao reUploadBeanDao = mdaoSession.getReUploadBeanDao();
        List<ReUploadBean> list = reUploadBeanDao.queryBuilder().list();
        for (final ReUploadBean bean : list) {
            RetrofitGenerator.getConnectApi().universal(bean.getMethod()+".Json", config.getString("key"), bean.getContent())
                    .subscribeOn(Schedulers.single())
                    .unsubscribeOn(Schedulers.single())
                    .observeOn(Schedulers.single())
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            Log.e("信息提示", bean.getMethod());
                            reUploadBeanDao.delete(bean);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("信息提示error", bean.getMethod());

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetPassEvent(PassEvent event) {
        lock.setLockState(new State_Unlock(sp));
        lock.doNext();
        Observable.timer(120, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        unlock_noOpen = d;
                    }

                    @Override
                    public void onNext(Long aLong) {
                        lock.setLockState(new State_Lockup(sp));
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
                    alarmRecord(String.valueOf(1));
                }
            }
        } else {
            if (value.startsWith("AAAAAA")) {
                if (!value.equals(Last_Value)) {
                    Last_Value = value;
                    if (Last_Value.substring(6, 8).equals("01")) {//泄露报警
                        if (getLockState(State_Lockup.class)) {
                            alarmRecord(String.valueOf(5));
                            EventBus.getDefault().post(new AlarmEvent(5));
                        }
                    }
                    if (Last_Value.substring(8, 10).equals("01")) {//入侵报警
                        if (getLockState(State_Lockup.class)) {
                            alarmRecord(String.valueOf(2));
                            EventBus.getDefault().post(new AlarmEvent(2));
                        }
                    }
                    if (Last_Value.equals("AAAAAA000000000000")) {
                        if (getDoorState(State_Close.class)) {
                            door.setDoorState(new State_Open(lock));
                            door.doNext();
                            if (getLockState(State_Lockup.class)) {
                                alarmRecord(String.valueOf(1));
                            }
                        }
                        if (unlock_noOpen != null) {
                            unlock_noOpen.dispose();
                        }
                        if (rx_delay != null) {
                            rx_delay.dispose();
                        }
                    } else if (Last_Value.equals("AAAAAA000001000000")) {
                        door.setDoorState(new State_Close(lock));
                        if (getLockState(State_Unlock.class)) {
                            final String closeDoorTime = TimeUtils.getNowString();
                            Observable.timer(20, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                                    .subscribe(new Observer<Long>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {
                                            rx_delay = d;
                                        }

                                        @Override
                                        public void onNext(Long aLong) {
                                            lock.setLockState(new State_Lockup(sp));
                                            //door.setDoorState(new State_Close(lock));
                                            sp.buzz(SwitchImpl.Hex.H2);
                                            if (unlock_noOpen != null) {
                                                unlock_noOpen.dispose();
                                            }
                                            CloseDoorRecord(closeDoorTime);
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
            } else {
                if (value.startsWith("BBBBBB") && value.endsWith("C1EF")) {
                    THSwitchValue = value;
                }
            }
        }
    }

    @Override
    public void onTemHum(int temperature, int humidity) {
        EventBus.getDefault().post(new TemHumEvent(temperature, humidity));
        if ((Math.abs(temperature - last_mTemperature) > 5 || Math.abs(temperature - last_mTemperature) > 10)) {
            StateRecord();
        }
        last_mTemperature = temperature;
        last_mHumidity = humidity;
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

    private void testNet() {
        RetrofitGenerator.getConnectApi().testNet(config.getString("key")).
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
                            EventBus.getDefault().post(new NetworkEvent(true));
                        } else {
                            EventBus.getDefault().post(new NetworkEvent(false));
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

    private void StateRecord() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("datetime", TimeUtils.getNowString());
            jsonObject.put("switching", THSwitchValue);
            jsonObject.put("temperature", last_mTemperature);
            jsonObject.put("humidity", last_mHumidity);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getConnectApi().stateRecord(config.getString("key"), jsonObject.toString()).
                subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull ResponseBody responseBody) {

            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void alarmRecord(String type) {
        EventBus.getDefault().post(new AlarmEvent(1));
        final JSONObject alarmRecordJson = new JSONObject();
        try {
            alarmRecordJson.put("datetime", TimeUtils.getNowString());
            alarmRecordJson.put("alarmType", type);
            alarmRecordJson.put("alarmValue", String.valueOf(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RetrofitGenerator.getConnectApi().alarmRecord(config.getString("key"), alarmRecordJson.toString())
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull ResponseBody responseBody) {

            }

            @Override
            public void onError(@NonNull Throwable e) {
                mdaoSession.insert(new ReUploadBean(null, "alarmRecord", alarmRecordJson.toString()));

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void CloseDoorRecord(String time) {
        final JSONObject CloseDoorRecordJson = new JSONObject();
        try {
            CloseDoorRecordJson.put("datetime", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RetrofitGenerator.getConnectApi().closeDoorRecord(config.getString("key"), CloseDoorRecordJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mdaoSession.insert(new ReUploadBean(null, "closeDoorRecord", CloseDoorRecordJson.toString()));

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void reboot() {
        long daySpan = 24 * 60 * 60 * 1000 * 2;
        // 规定的每天时间，某时刻运行
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd '3:00:00'");
        // 首次运行时间
        try {
            Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sdf.format(new Date()));
            if (System.currentTimeMillis() > startTime.getTime())
                startTime = new Date(startTime.getTime() + daySpan);
            Timer t = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // 要执行的代码
                    AppInit.getMyManager().reboot();
                }
            };
            t.scheduleAtFixedRate(task, startTime, daySpan);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
