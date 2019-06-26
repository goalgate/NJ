package com.nj.Alerts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.nj.Function.Func_Switch.mvp.module.SwitchImpl;
import com.nj.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import com.nj.R;
import com.nj.State.DoorState.Door;
import com.nj.State.DoorState.State_Open;
import com.nj.State.LockState.Lock;
import com.nj.State.LockState.State_Unlock;


public class Alarm {

    private TextView alarmText;

    private AlertView alert;

    private ViewGroup alarmView;

    private Context context;

    private boolean networkIsKnown = false;

    private static Alarm instance = null;

    public static Alarm getInstance(Context context) {
        if (instance == null) {
            instance = new Alarm(context);
        }
        return instance;
    }

    private Alarm(Context context) {
        this.context = context;
        alarmView = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.alarm_text, null);
        alarmText = (TextView) alarmView.findViewById(R.id.alarmText);
        alert = new AlertView("", null, null, new String[]{"确定"}, null, context, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                networkIsKnown = true;
            }
        });
        alert.addExtView(alarmView);
    }


    public void networkAlarm(boolean networkState, networkCallback callback) {
        if (!networkState) {
            if (!networkIsKnown) {
                SwitchPresenter.getInstance().buzz(SwitchImpl.Hex.H6);
                alarmText.setText("设备服务器连接失败,请检查网络,点击确定可继续使用");
                callback.onTextBack("设备服务器连接失败,请检查网络,点击确定可继续使用");
                alert.show();
            } else {
                callback.onIsKnown();
            }
        }else{
            callback.onIsKnown();
        }
    }


    public void messageAlarm(String msg) {
        alarmText.setText(msg);
        alert.show();
    }
    public void setKnown( boolean known) {
        networkIsKnown = known;
    }

    public interface doorCallback {
        void onTextBack(String msg);

        void onSucc();
    }

    public interface networkCallback {
        void onIsKnown();
        void onTextBack(String msg);
    }

    public void doorAlarm(doorCallback callback) {
        if (Door.getInstance().getDoorState().getClass().getName().equals(State_Open.class.getName())) {
            if (Lock.getInstance().getLockState().getClass().getName().equals(State_Unlock.class.getName())) {
                alarmText.setText("门磁已打开,如需撤防请先闭合门磁");
                callback.onTextBack("门磁已打开,如需撤防请先闭合门磁");
            } else {
                alarmText.setText("仓库已设防,但门磁未闭合,请检查门磁状态");
                callback.onTextBack("仓库已设防,但门磁未闭合,请检查门磁状态");
            }
            SwitchPresenter.getInstance().buzz(SwitchImpl.Hex.H6);
            alert.show();
        } else {
            callback.onSucc();
        }

    }
    public void release(){
        instance = null;
    }
}
