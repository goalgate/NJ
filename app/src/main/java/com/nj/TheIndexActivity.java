package com.nj;

import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.aip.entity.User;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.drv.card.ICardInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.nj.Alerts.Alarm;
import com.nj.Alerts.Alert_IP;
import com.nj.Alerts.Alert_Message;
import com.nj.Alerts.Alert_Password;
import com.nj.Alerts.Alert_Server;
import com.nj.Bean.ReUploadBean;
import com.nj.Bean.UserBean;
import com.nj.EventBus.AlarmEvent;
import com.nj.EventBus.LockUpEvent;
import com.nj.EventBus.NetworkEvent;
import com.nj.EventBus.OpenDoorEvent;
import com.nj.EventBus.PassEvent;
import com.nj.EventBus.TemHumEvent;
import com.nj.Function.Func_Face.mvp.presenter.FacePresenter;
import com.nj.Function.Func_Switch.mvp.module.SwitchImpl;
import com.nj.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import com.nj.Retrofit.RetrofitGenerator;
import com.nj.Service.SwitchService;
import com.nj.State.OperationState.Door_Open_OperateState;
import com.nj.State.OperationState.No_one_OperateState;
import com.nj.State.OperationState.One_man_OperateState;
import com.nj.State.OperationState.Operation;
import com.nj.State.OperationState.Two_man_OperateState;
import com.nj.Tools.DESX;
import com.nj.Tools.FileUtils;
import com.nj.Tools.MyObserver;
import com.nj.Tools.PersonType;
import com.nj.Tools.ServerConnectionUtil;
import com.nj.UI.AddPersonWindow;
import com.nj.greendao.DaoSession;
import com.trello.rxlifecycle2.android.ActivityEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static com.nj.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.AllView;
import static com.nj.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Identify;
import static com.nj.Function.Func_Face.mvp.presenter.FacePresenter.FaceResultType.Identify_non;


public class TheIndexActivity extends TheFunctionActivity {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Intent intent;

    enum identity_method {
        fingerprint, face, idcard
    }

    identity_method global_method;

    String TAG = TheIndexActivity.class.getSimpleName();

    SPUtils config = SPUtils.getInstance("config");

    UserBean cg_User1 = new UserBean();

    UserBean cg_User2 = new UserBean();

    UserBean unknownUser = new UserBean();

    Disposable disposableTips;

    private AddPersonWindow personWindow;

    No_one_OperateState no_one_operateState = new No_one_OperateState();

    DaoSession mdaosession = AppInit.getInstance().getDaoSession();

    @BindView(R.id.tv_info)
    TextView tv_info;

    @BindView(R.id.iv_network)
    ImageView iv_network;

    @BindView(R.id.iv_setting)
    ImageView iv_setting;

    @BindView(R.id.tv_time)
    TextView tv_time;

    @BindView(R.id.iv_lock)
    ImageView iv_lock;

    @BindView(R.id.tv_temperature)
    TextView tv_temperature;

    @BindView(R.id.tv_humidity)
    TextView tv_humidity;

    @BindView(R.id.gestures_overlay)
    GestureOverlayView gestures;

    Disposable checkChange;

    boolean network_state;

    GestureLibrary mGestureLib;

    Operation global_Operation;

    Alert_Message alert_message;

    Alert_Server alert_server;

    Alert_IP alert_ip;

    Alert_Password alert_password;

    @OnClick(R.id.iv_wifi)
    void option() {
        alert_password.show();
    }

    @OnClick(R.id.iv_lock)
    void showMessage() {
        alert_message.showMessage();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newindex);
        EventBus.getDefault().register(this);
        UIPrepare();
        global_Operation = new Operation(no_one_operateState);
        setGestures();
        autoUpdate();
        openService();
        syncTime();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            fp.CameraPreview(AppInit.getContext(), previewView, textureView);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void UIPrepare() {
        ButterKnife.bind(this);
        disposableTips = RxTextView.textChanges(tv_info)
                .debounce(60, TimeUnit.SECONDS)
                .switchMap(new Function<CharSequence, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull CharSequence charSequence) throws Exception {
                        return Observable.just("等待用户操作");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        tv_info.setText(s);
                    }
                });
        alert_ip = new Alert_IP(this);
        alert_ip.IpviewInit();
        alert_server = new Alert_Server(this);
        alert_server.serverInit(new Alert_Server.Server_Callback() {
            @Override
            public void setNetworkBmp() {
                iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_wifi));
            }
        });
        alert_password = new Alert_Password(this);
        alert_password.PasswordViewInit(new Alert_Password.Callback() {
            @Override
            public void normal_call() {
                personWindow = new AddPersonWindow(TheIndexActivity.this);
                personWindow.setOptionTypeListener(TheIndexActivity.this);
                personWindow.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
            }

            @Override
            public void super_call() {

            }
        });
        alert_message = new Alert_Message(this);
        alert_message.messageInit();
    }

    private void setGestures() {
        gestures.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE);
        gestures.setGestureVisible(false);
        gestures.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
            @Override
            public void onGesturePerformed(GestureOverlayView overlay,
                                           Gesture gesture) {
                ArrayList<Prediction> predictions = mGestureLib.recognize(gesture);
                if (predictions.size() > 0) {
                    Prediction prediction = (Prediction) predictions.get(0);
                    // 匹配的手势
                    if (prediction.score > 1.0) { // 越匹配score的值越大，最大为10
                        if (prediction.name.equals("setting")) {
                            NetworkUtils.openWirelessSettings();
                        }
                    }
                }
            }
        });
        if (mGestureLib == null) {
            mGestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
            mGestureLib.load();
        }
    }

    private void autoUpdate() {
        new ServerConnectionUtil().download("http://124.172.232.89:8050/daServer/updateADA.do?ver=" + AppUtils.getAppVersionName() + "&daid=" + config.getString("daid") + "&url=http://222.189.59.244/", new ServerConnectionUtil.Callback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    if (response.equals("true")) {
                        AppUtils.installApp(new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "Download" + File.separator + "app-release.apk"), "application/vnd.android.package-archive");
                    }
                }
            }
        });
    }

    private void syncTime() {
        RetrofitGenerator.getConnectApi().getTime(config.getString("key"))
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()).subscribe(new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull ResponseBody response) {
                try {
                    Map<String, String> infoMap = new Gson().fromJson(response.string(),
                            new TypeToken<HashMap<String, String>>() {
                            }.getType());
                    String datetime = infoMap.get("datetime");
                    if (!TextUtils.isEmpty(datetime)) {
                        AppInit.getMyManager().setTime(Integer.parseInt(datetime.substring(0, 4)),
                                Integer.parseInt(datetime.substring(5, 7)),
                                Integer.parseInt(datetime.substring(8, 10)),
                                Integer.parseInt(datetime.substring(11, 13)),
                                Integer.parseInt(datetime.substring(14, 16)),
                                Integer.parseInt(datetime.substring(17, 19)));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    void openService() {
        intent = new Intent(TheIndexActivity.this, SwitchService.class);
        startService(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetTemHumEvent(TemHumEvent event) {
        tv_temperature.setText(event.getTem() + "℃");
        tv_humidity.setText(event.getHum() + "%");
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetNetworkEvent(NetworkEvent event) {
        if (event.getNetwork_state()) {
            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_wifi));
            network_state = true;
        } else {
            iv_network.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_wifi1));
            network_state = false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetAlarmEvent(AlarmEvent event) {
        if (event.getType() == 1) {
            Alarm.getInstance(this).messageAlarm("门磁打开报警，请检查门磁情况");
        }
        if (event.getType() == 2) {
            tv_info.setText("入侵报警已被触发");
        } else if (event.getType() == 5) {
            tv_info.setText("泄露报警已被触发");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetOpenDoorEvent(OpenDoorEvent event) {
        OpenDoorRecord(event.getLegal());
        if (checkChange != null) {
            checkChange.dispose();
        }
        if (!getState(Door_Open_OperateState.class)) {
            global_Operation.setState(no_one_operateState);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetLockUpEvent(LockUpEvent event) {
        Alarm.getInstance(this).setKnown(false);
        tv_info.setText("仓库已重新上锁");
        cg_User1 = new UserBean();
        cg_User2 = new UserBean();
        iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_mj));
        global_Operation.setState(no_one_operateState);
    }

    @Override
    public void onResume() {
        super.onResume();
        network_state = false;
        cg_User1 = new UserBean();
        cg_User2 = new UserBean();
        global_Operation.setState(no_one_operateState);
        tv_info.setText("等待用户操作");
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        tv_time.setText(formatter.format(new Date(System.currentTimeMillis())));
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        //stopService(intent);
        AppInit.getMyManager().unBindAIDLService(AppInit.getContext());
        disposableTips.dispose();
    }

    @Override
    public void onOptionType(Button view, int type) {
        personWindow.dismiss();
        if (type == 1) {
            ActivityUtils.startActivity(getPackageName(), getPackageName() + ".TheAddPersonActivity");
        } else if (type == 2) {
            alert_server.show();
        } else if (type == 3) {
            ViewGroup extView2 = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.inputdevid_form, null);
            final EditText et_devid = (EditText) extView2.findViewById(R.id.devid_input);
            new AlertView("设备信息同步", null, "取消", new String[]{"确定"}, null, TheIndexActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, int position) {
                    if (position == 0) {
                        if (TextUtils.isEmpty(et_devid.getText().toString())) {
                            ToastUtils.showLong("您的输入为空请重试");
                        } else {
                            fpp.fpCancel(true);
                            equipment_sync(et_devid.getText().toString());
                        }
                    }
                }
            }).addExtView(extView2).show();
        } else if (type == 4) {
            alert_ip.show();
        } else if (type == 5) {
            ViewGroup deleteView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.delete_person_form, null);
            final EditText et_idcard = (EditText) deleteView.findViewById(R.id.idcard_input);
            final EditText et_finger = (EditText) deleteView.findViewById(R.id.et_finger);
            new AlertView("删除人员指纹信息", null, "取消", new String[]{"确定"}, null, TheIndexActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, int position) {
                    if (position == 0) {
                        if (TextUtils.isEmpty(et_idcard.getText().toString()) || TextUtils.isEmpty(et_finger.getText().toString())) {
                            ToastUtils.showLong("您的输入为空请重试");
                        } else {
                            deletePerson(et_idcard.getText().toString(), et_finger.getText().toString());
                        }
                    }
                }
            }).addExtView(deleteView).show();
        }
    }

    @Override
    public void onText(String msg) {
        if ("请确认指纹是否已登记".equals(msg)) {
            tv_info.setText("请确认指纹是否已登记,再重试");
        } else if ("松开手指".equals(msg)) {
            tv_info.setText(msg);
        }
    }

    @Override
    public void onSetImg(Bitmap bmp) {

    }

    @Override
    public void onFpSucc(final String msg) {
        Alarm.getInstance(this).doorAlarm(new Alarm.doorCallback() {
            @Override
            public void onTextBack(String msg) {
                tv_info.setText(msg);
            }

            @Override
            public void onSucc() {
                Alarm.getInstance(TheIndexActivity.this).networkAlarm(network_state, new Alarm.networkCallback() {
                    @Override
                    public void onIsKnown() {
                        try {
                            loadMessage(msg.substring(3, msg.length()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onTextBack(String msg) {
                        tv_info.setText(msg);
                    }
                });
            }
        });
    }

    private void loadMessage(String sp) {
        global_method = identity_method.fingerprint;
        UserBean the_user = mdaosession.queryRaw(UserBean.class, "where FINGERPRINT_ID = '" + sp + "'").get(0);
//        if (SPUtils.getInstance(sp).getString("courType").contains(PersonType.KuGuan)) {
        if (the_user.getCourType().contains(PersonType.KuGuan)) {
            if (getState(No_one_OperateState.class)) {
                global_Operation.setState(new One_man_OperateState());
                fp.FaceGetAllView();
                cg_User1 = the_user;
//                cg_User1.setCourIds(SPUtils.getInstance(sp).getString("courIds"));
//                cg_User1.setName(SPUtils.getInstance(sp).getString("name"));
//                cg_User1.setCardId(SPUtils.getInstance(sp).getString("cardId"));
//                cg_User1.setFingerprintId(sp);
//                cg_User1.setCourType(SPUtils.getInstance(sp).getString("courType"));
            } else if (getState(Two_man_OperateState.class)) {
                if (!the_user.getCardId().equals(cg_User1.getCardId())) {
//                if (!SPUtils.getInstance(sp).getString("cardId").equals(cg_User1.getCardId())) {
//                    cg_User2.setCourIds(SPUtils.getInstance(sp).getString("courIds"));
//                    cg_User2.setName(SPUtils.getInstance(sp).getString("name"));
//                    cg_User2.setCardId(SPUtils.getInstance(sp).getString("cardId"));
//                    cg_User2.setFingerprintId(sp);
                    cg_User2 = the_user;
                    fp.FaceGetAllView();
                    EventBus.getDefault().post(new PassEvent());
                    iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_mj1));
                } else {
                    tv_info.setText("请不要连续输入相同的管理员信息");
                }
            } else if (getState(Door_Open_OperateState.class)) {
                tv_info.setText("仓库门已打开");
            }
//        } else if (SPUtils.getInstance(sp).getString("courType").contains(PersonType.XunJian)) {
        } else if (the_user.getCourType().contains(PersonType.XunJian)) {
            if (checkChange != null) {
                checkChange.dispose();
            }
            cg_User1 = the_user;
//            cg_User1.setCourIds(SPUtils.getInstance(sp).getString("courIds"));
//            cg_User1.setName(SPUtils.getInstance(sp).getString("name"));
//            cg_User1.setCardId(SPUtils.getInstance(sp).getString("cardId"));
//            cg_User1.setFingerprintId(sp);
            checkRecord(PersonType.XunJian);
//        } else if (SPUtils.getInstance(sp).getString("courType").equals(PersonType.Gongan)) {
        } else if (the_user.getCourType().contains(PersonType.Gongan)) {

            if (checkChange != null) {
                checkChange.dispose();
            }
            cg_User1 = the_user;
//            cg_User1.setCourIds(SPUtils.getInstance(sp).getString("courIds"));
//            cg_User1.setName(SPUtils.getInstance(sp).getString("name"));
//            cg_User1.setCardId(SPUtils.getInstance(sp).getString("cardId"));
//            cg_User1.setFingerprintId(sp);
            checkRecord(PersonType.Gongan);
        } else {
            unknownUser = the_user;
//            unknownUser.setName(SPUtils.getInstance(sp).getString("name"));
//            unknownUser.setCardId(SPUtils.getInstance(sp).getString("cardId"));
//            unknownUser.setFingerprintId(sp);
            fp.FaceGetAllView();
        }
    }

    @Override
    public void onsetCardInfo(final ICardInfo cardInfo) {
        try {
            if (alert_message.Showing()) {
                alert_message.setICCardText("身份证功能正常");
            } else {
                Alarm.getInstance(TheIndexActivity.this).networkAlarm(network_state, new Alarm.networkCallback() {
                    @Override
                    public void onIsKnown() {
                        global_method = identity_method.idcard;
                        UserBean the_user = mdaosession.queryRaw(UserBean.class, "where CARD_ID = '" + cardInfo.cardId() + "'").get(0);
//                    if (courTypes.getString(cardInfo.cardId()).contains(PersonType.KuGuan)) {
                        if (the_user.getCourType().contains(PersonType.KuGuan)) {
                            if (getState(No_one_OperateState.class)) {
                                global_Operation.setState(new One_man_OperateState());
                                fp.FaceGetAllView();
                                cg_User1 = the_user;
//                            cg_User1.setCourIds(courIds.getString(cardInfo.cardId()));
//                            cg_User1.setName(cardInfo.name());
//                            cg_User1.setCardId(cardInfo.cardId());
                            } else if (getState(Two_man_OperateState.class)) {
                                if (!cardInfo.cardId().equals(cg_User1.getCardId())) {
                                    cg_User2 = the_user;
//                                cg_User2.setCourIds(courIds.getString(cardInfo.cardId()));
//                                cg_User2.setName(cardInfo.name());
//                                cg_User2.setCardId(cardInfo.cardId());
                                    fp.FaceGetAllView();
                                    EventBus.getDefault().post(new PassEvent());
                                    iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_mj1));
                                } else if (cardInfo.cardId().equals(cg_User1.getCardId())) {
                                    tv_info.setText("请不要连续输入相同的管理员信息");
                                }
                            } else if (getState(Door_Open_OperateState.class)) {
                                tv_info.setText("仓库门已打开");
                            }
                        } else if (the_user.getCourType().contains(PersonType.XunJian)) {
                            if (checkChange != null) {
                                checkChange.dispose();
                            }
                            cg_User1.setName(cardInfo.name());
                            cg_User1.setCardId(cardInfo.cardId());
                            checkRecord(PersonType.XunJian);
                        } else if (the_user.getCourType().contains(PersonType.Gongan)) {
                            if (checkChange != null) {
                                checkChange.dispose();
                            }
                            cg_User1.setName(cardInfo.name());
                            cg_User1.setCardId(cardInfo.cardId());
                            checkRecord(PersonType.Gongan);
                        } else {
                            unknownUser.setName(cardInfo.name());
                            unknownUser.setCardId(cardInfo.cardId());
                            fp.FaceGetAllView();
                        }
                    }

                    @Override
                    public void onTextBack(String msg) {
                        tv_info.setText(msg);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onsetCardImg(Bitmap bmp) {

    }

    @Override
    public void onSetText(String Msg) {

    }

    @Override
    public void onUser(FacePresenter.FaceResultType resultType, User user) {
        try {
            if (resultType.equals(Identify)) {
                global_method = identity_method.face;
                UserBean the_user = mdaosession.queryRaw(UserBean.class, "where CARD_ID = '" + user.getUserId() + "'").get(0);
//                    if (courTypes.getString(cardInfo.cardId()).contains(PersonType.KuGuan)) {
                if (the_user.getCourType().contains(PersonType.KuGuan)) {
                    if (getState(No_one_OperateState.class)) {
                        global_Operation.setState(new One_man_OperateState());
                        fp.FaceGetAllView();
                        cg_User1 = the_user;
//                            cg_User1.setCourIds(courIds.getString(cardInfo.cardId()));
//                            cg_User1.setName(cardInfo.name());
//                            cg_User1.setCardId(cardInfo.cardId());
                    } else if (getState(Two_man_OperateState.class)) {
                        if (!the_user.equals(cg_User1.getCardId())) {
                            cg_User2 = the_user;
//                                cg_User2.setCourIds(courIds.getString(cardInfo.cardId()));
//                                cg_User2.setName(cardInfo.name());
//                                cg_User2.setCardId(cardInfo.cardId());
                            fp.FaceGetAllView();
                            EventBus.getDefault().post(new PassEvent());
                            iv_lock.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newui_mj1));
                        } else {
                            tv_info.setText("请不要连续输入相同的管理员信息");
                        }
                    } else if (getState(Door_Open_OperateState.class)) {
                        tv_info.setText("仓库门已打开");
                    }
                } else if (the_user.getCourType().contains(PersonType.XunJian)) {
                    if (checkChange != null) {
                        checkChange.dispose();
                    }
                    cg_User1 = the_user;
                    checkRecord(PersonType.XunJian);
                } else if (the_user.getCourType().contains(PersonType.Gongan)) {
                    if (checkChange != null) {
                        checkChange.dispose();
                    }
                    cg_User1 = the_user;
                    checkRecord(PersonType.Gongan);
                } else {
                    unknownUser = the_user;
                    fp.FaceGetAllView();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onText(FacePresenter.FaceResultType resultType, String text) {
        if (resultType.equals(Identify_non)) {
            tv_info.setText(text);
            SwitchPresenter.getInstance().buzz(SwitchImpl.Hex.H2);
        }
    }

    Bitmap global_bmp;

    @Override
    public void onBitmap(FacePresenter.FaceResultType resultType, Bitmap bitmap) {
        if (resultType.equals(Identify)) {
            global_bmp = bitmap;
        } else if (resultType.equals(AllView)) {
            Matrix matrix = new Matrix();
            matrix.postScale(0.5f, 0.5f);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (unknownUser.getCardId() != null) {
                unknownPeople(bitmap);
            }
            if (getState(One_man_OperateState.class)) {
                cg_User1.setPhoto(FileUtils.bitmapToBase64(bitmap));
                switch (global_method) {
                    case face:
                        tv_info.setText("管理员" + cg_User1.getName() + "打卡,请继续管理员操作");
                        break;
                    case idcard:
                        tv_info.setText("管理员" + cg_User1.getName() + "打卡,请继续管理员操作");
                        break;
                    case fingerprint:
                        tv_info.setText("管理员" + cg_User1.getName() + "打卡,请继续管理员操作,指纹ID为" + cg_User1.getFingerprintId());
                        break;
                    default:
                        break;
                }
                global_Operation.doNext(new Operation.Callback_Operation() {
                    @Override
                    public void uploadCallback() {
                    }
                });
                Observable.timer(60, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                        .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Long>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                checkChange = d;
                            }

                            @Override
                            public void onNext(Long aLong) {
                                checkRecord(PersonType.KuGuan);
                                cg_User1 = new UserBean();
                                cg_User2 = new UserBean();
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            } else if (getState(Two_man_OperateState.class)) {
                if (checkChange != null) {
                    checkChange.dispose();
                }
                switch (global_method) {
                    case face:
                        tv_info.setText("管理员" + cg_User1.getName() + "打卡,请继续管理员操作");
                        break;
                    case idcard:
                        tv_info.setText("管理员" + cg_User1.getName() + "打卡,请继续管理员操作");

                        break;
                    case fingerprint:
                        tv_info.setText("管理员" + cg_User1.getName() + "打卡,请继续管理员操作,指纹ID为" + cg_User1.getFingerprintId());
                        break;
                    default:
                        break;
                }
                cg_User2.setPhoto(FileUtils.bitmapToBase64(bitmap));
                global_Operation.doNext(new Operation.Callback_Operation() {
                    @Override
                    public void uploadCallback() {
                        global_Operation.setState(new Door_Open_OperateState());
                    }
                });
            }
        }

    }

    private Boolean getState(Class stateClass) {
        if (global_Operation.getState().getClass().getName().equals(stateClass.getName())) {
            return true;
        } else {
            return false;
        }
    }

    private void OpenDoorRecord(boolean leagl) {
        final JSONObject OpenDoorJson = new JSONObject();
        if (leagl) {
            try {
                OpenDoorJson.put("courIds1", cg_User1.getCourIds());
                OpenDoorJson.put("courIds2", cg_User2.getCourIds());
                OpenDoorJson.put("id1", cg_User1.getCardId());
                OpenDoorJson.put("id2", cg_User2.getCardId());
                OpenDoorJson.put("name1", cg_User1.getName());
                OpenDoorJson.put("name2", cg_User2.getName());
                OpenDoorJson.put("photo1", cg_User1.getPhoto());
                OpenDoorJson.put("photo2", cg_User2.getPhoto());
                OpenDoorJson.put("datetime", TimeUtils.getNowString());
                OpenDoorJson.put("state", "y");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                OpenDoorJson.put("datetime", TimeUtils.getNowString());
                OpenDoorJson.put("state", "n");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        RetrofitGenerator.getConnectApi().openDoorRecord(config.getString("key"), OpenDoorJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<ResponseBody>(this) {
                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        try {
                            Map<String, String> infoMap = new Gson().fromJson(responseBody.string(),
                                    new TypeToken<HashMap<String, String>>() {
                                    }.getType());
                            if (infoMap.get("result").equals("true")) {
                                tv_info.setText("开门数据上传成功");
                            } else if (infoMap.get("result").equals("false")) {
                                tv_info.setText("开门数据上传失败");
                            } else if (infoMap.get("result").equals("dataErr")) {
                                tv_info.setText("上传的json数据有错");
                            } else if (infoMap.get("result").equals("dbErr")) {
                                tv_info.setText("数据库操作有错");
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        tv_info.setText("无法连接到服务器");
                        cg_User1 = new UserBean();
                        cg_User2 = new UserBean();
                        mdaosession.insert(new ReUploadBean(null, "openDoorRecord", OpenDoorJson.toString()));

                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        cg_User1 = new UserBean();
                        cg_User2 = new UserBean();
                    }
                });
    }

    private void equipment_sync(final String old_devid) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("oldDaid", old_devid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getConnectApi().searchFinger(config.getString("key"), jsonObject.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<ResponseBody>(this) {
                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        try {
                            Map<String, String> infoMap = new Gson().fromJson(responseBody.string(),
                                    new TypeToken<HashMap<String, String>>() {
                                    }.getType());

                            if (infoMap.get("result") == null) {
                                mdaosession.getUserBeanDao().deleteAll();
//                                courIds.clear();
//                                courTypes.clear();
                                JSONArray jsonArray = new JSONArray(infoMap.get("data"));
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject item = jsonArray.getJSONObject(i);
                                    UserBean saveUser = new UserBean();
                                    saveUser.setCourIds(item.getString("personIds"));
                                    saveUser.setName(item.getString("name"));
                                    saveUser.setCardId(item.getString("idcard"));
                                    saveUser.setCourType(item.getString("courType"));
                                    saveUser.setFingerprintId(item.getString("fingerprintId"));
                                    saveUser.setFingerprintKey(item.getString("fingerTemp"));
                                    mdaosession.insertOrReplace(saveUser);
//                                    SPUtils user_sp = SPUtils.getInstance(item.getString("fingerprintId"));
//                                    fpp.fpDownTemplate(item.getString("fingerprintId"), item.getString("fingerTemp"));
//                                    user_sp.put("courIds", item.getString("personIds"));
//                                    user_sp.put("name", item.getString("name"));
//                                    user_sp.put("cardId", item.getString("idcard"));
//                                    user_sp.put("courType", item.getString("courType"));
//                                    courIds.put(item.getString("idcard"), item.getString("personIds"));
//                                    courTypes.put(item.getString("idcard"), item.getString("courType"));
                                }
                                JSONObject jsonKey = new JSONObject();
                                try {
                                    jsonKey.put("daid", old_devid);
                                    jsonKey.put("check", DESX.encrypt(old_devid));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                config.put("daid", old_devid);
                                config.put("key", DESX.encrypt(jsonKey.toString()));
                                ToastUtils.showLong("设备数据更新成功");
                                fpp.fpIdentify();
                            } else {
                                ToastUtils.showLong("设备号有误");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        tv_info.setText("无法连接到服务器");
                    }
                });
    }

    private void checkRecord(String type) {
        SwitchPresenter.getInstance().OutD9(false);
        final JSONObject checkRecordJson = new JSONObject();
        try {
            checkRecordJson.put("id", cg_User1.getCardId());
            checkRecordJson.put("name", cg_User1.getName());
            checkRecordJson.put("checkType", type);
            checkRecordJson.put("datetime", TimeUtils.getNowString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getConnectApi().checkRecord(config.getString("key"), checkRecordJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<ResponseBody>(this) {
                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        if (!getState(Two_man_OperateState.class) || !getState(Door_Open_OperateState.class)) {
                            global_Operation.setState(new No_one_OperateState());
                        }
                        try {
                            Map<String, String> infoMap = new Gson().fromJson(responseBody.string(),
                                    new TypeToken<HashMap<String, String>>() {
                                    }.getType());
                            if (infoMap.get("result").equals("true")) {
                                tv_info.setText("巡检成功");
                            } else if (infoMap.get("result").equals("false")) {
                                tv_info.setText("巡检失败");
                            } else if (infoMap.get("result").equals("dataErr")) {
                                tv_info.setText("上传巡检数据失败");
                            } else if (infoMap.get("result").equals("dataErr")) {
                                tv_info.setText("数据库操作有错");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        tv_info.setText("无法连接到服务器");
                        mdaosession.insert(new ReUploadBean(null, "checkRecord", checkRecordJson.toString()));

                    }
                });
    }

    private void unknownPeople(Bitmap bmp) {
        final JSONObject unknownPeopleJson = new JSONObject();
        try {
            unknownPeopleJson.put("visitIdcard", unknownUser.getCardId());
            unknownPeopleJson.put("visitName", unknownUser.getName());
            unknownPeopleJson.put("photos", FileUtils.bitmapToBase64(bmp));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getConnectApi().saveVisit(config.getString("key"), unknownPeopleJson.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<ResponseBody>(this) {
                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                      /*  if (!getState(Two_man_OperateState.class) || !getState(Door_Open_OperateState.class)) {
                            global_Operation.setState(new No_one_OperateState());
                        }*/
                        try {
                            Map<String, String> infoMap = new Gson().fromJson(responseBody.string(),
                                    new TypeToken<HashMap<String, String>>() {
                                    }.getType());
                            if (!TextUtils.isEmpty(infoMap.get("result"))) {
                                if (infoMap.get("result").equals("true")) {
                                    if (unknownUser.getFingerprintId() != null) {
                                        tv_info.setText("访问人" + unknownUser.getName() + "数据上传成功,指纹号为" + unknownUser.getFingerprintId());
                                    } else {
                                        tv_info.setText("访问人" + unknownUser.getName() + "数据上传成功");
                                    }
                                } else if (infoMap.get("result").equals("false")) {
                                    tv_info.setText("访问人上传失败");
                                } else if (infoMap.get("result").equals("dataErr")) {
                                    tv_info.setText("上传访问人数据失败");
                                } else if (infoMap.get("result").equals("dbErr")) {
                                    tv_info.setText("数据库操作有错");
                                } else if (infoMap.get("result").equals("daidIsNull")) {
                                    tv_info.setText("请先注册设备");
                                }
                            }
                            unknownUser = new UserBean();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        tv_info.setText("无法连接到服务器");
                        unknownUser = new UserBean();
                        mdaosession.insert(new ReUploadBean(null, "saveVisit", unknownPeopleJson.toString()));

                    }
                });
    }

    private void deletePerson(final String idcard, final String fingerId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", idcard);
            jsonObject.put("fingerprintId", fingerId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getConnectApi().deleteFinger(config.getString("key"), jsonObject.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<ResponseBody>(this) {
                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        try {
                            Map<String, String> infoMap = new Gson().fromJson(responseBody.string(),
                                    new TypeToken<HashMap<String, String>>() {
                                    }.getType());
                            if (infoMap.get("result").equals("true")) {
                                fpp.fpCancel(true);
                                Observable.timer(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<Long>() {
                                            @Override
                                            public void accept(Long aLong) throws Exception {
                                                fpp.fpRemoveTmpl(fingerId);
                                                fpp.fpIdentify();
                                                UserBean deleteBean = mdaosession.queryRaw(UserBean.class, "where CARD_ID ='" + idcard + "' and FINGERPRINT_ID='" + fingerId + "'").get(0);
                                                mdaosession.delete(deleteBean);
                                            }
                                        });
                            } else if (infoMap.get("result").equals("dataUndefind")) {
                                ToastUtils.showLong("删除失败");
                            } else if (infoMap.get("result").equals("dataErr")) {
                                ToastUtils.showLong("服务出错");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        super.onError(e);
                        tv_info.setText("无法连接到服务器");
                    }
                });

    }


}

