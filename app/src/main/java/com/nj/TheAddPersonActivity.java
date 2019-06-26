package com.nj;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.aip.api.FaceApi;
import com.baidu.aip.entity.User;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.drv.card.ICardInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.rxbinding2.view.RxView;
import com.nj.Alerts.Alarm;
import com.nj.Bean.UserBean;
import com.nj.EventBus.FaceDetectEvent;
import com.nj.EventBus.OpenDoorEvent;
import com.nj.Function.Fun_FingerPrint.mvp.presenter.FingerPrintPresenter;
import com.nj.Function.Fun_FingerPrint.mvp.view.IFingerPrintView;
import com.nj.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import com.nj.Function.Func_IDCard.mvp.view.IIDCardView;
import com.nj.Retrofit.RetrofitGenerator;
import com.nj.Tools.FileUtils;
import com.nj.Tools.MyObserver;
import com.nj.greendao.DaoSession;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class TheAddPersonActivity extends Activity implements IFingerPrintView, IIDCardView {
    FingerPrintPresenter fpp = FingerPrintPresenter.getInstance();

    SPUtils config = SPUtils.getInstance("config");

    boolean commitable;

    UserBean user;

    String fp_id = "0";

    int count = 3;

    String userId = null;

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    String alertTitle = "请选择接下来的操作";

    IDCardPresenter idp = IDCardPresenter.getInstance();

    @BindView(R.id.iv_finger)
    ImageView img_finger;

    @BindView(R.id.et_finger)
    TextView tv_finger;

    @BindView(R.id.btn_commit)
    Button btn_commit;

    @BindView(R.id.tv_person_name)
    TextView person_name;

    @BindView(R.id.tv_id_card)
    TextView person_id;

    @BindView(R.id.iv_head_photo)
    ImageView headphoto;

    @OnClick(R.id.btn_commit)
    void commit() {
        if (commitable) {
            if (user.getFingerprintId() != null) {
                user.setFingerprintKey(fpp.fpUpTemlate(user.getFingerprintId()));
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", user.getCardId());
                    jsonObject.put("dataType", "1");
                    jsonObject.put("fingerprintPhoto", user.getFingerprintPhoto());
                    jsonObject.put("fingerprintId", user.getFingerprintId());
                    jsonObject.put("fingerprintKey", user.getFingerprintKey());
                    jsonObject.put("datetime", TimeUtils.getNowString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RetrofitGenerator.getConnectApi().fingerLog(config.getString("key"), jsonObject.toString())
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
                                        mdaoSession.getUserBeanDao().insert(user);
                                        alertTitle = "人员插入成功,请选择接下来的操作";
                                        fp_id = "0";
                                        userId = null;
//                                        SPUtils user_sp = SPUtils.getInstance(user.getFingerprintId());
//                                        user_sp.put("courIds", user.getCourIds());
//                                        user_sp.put("name", user.getName());
//                                        user_sp.put("cardId", user.getCardId());
//                                        user_sp.put("courType", user.getCourType());
//                                        courIds.put(user.getCardId(), user.getCourIds());
//                                        courTypes.put(user.getCardId(), user.getCourType());
                                        ToastUtils.showLong("人员插入成功");
                                        cancel();
                                    } else if (infoMap.get("result").equals("false")) {
                                        Alarm.getInstance(TheAddPersonActivity.this).messageAlarm("数据插入有错");
                                    } else {
                                        Alarm.getInstance(TheAddPersonActivity.this).messageAlarm(infoMap.get("result"));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        });

            } else {
                Alarm.getInstance(this).messageAlarm("您的操作有误，请重试");
            }
        } else {
            Alarm.getInstance(this).messageAlarm("您还有信息未登记，如需退出请按取消");
        }
    }

    @OnClick(R.id.btn_cancel)
    void cancel() {
        new AlertView(alertTitle, null, null, new String[]{"重置并继续录入信息", "退出至主桌面"}, null, TheAddPersonActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    alertTitle = "请选择接下来的操作";
                    commitable = false;
                    person_id.setText(null);
                    person_name.setText(null);
                    user = new UserBean();
                    img_finger.setClickable(false);
                    fpp.fpCancel(true);
                    fpp.fpRemoveTmpl(fp_id);
                    idp.readCard();
                    if (userId != null) {
                        FaceApi.getInstance().userDelete(userId, "1");
                    }
                    headphoto.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.user_icon));
                    tv_finger.setText("先验证人员身份获得指纹编号");
                    img_finger.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.zw_icon));
                } else {
                    if (userId != null) {
                        FaceApi.getInstance().userDelete(userId, "1");
                    }
                    fpp.fpCancel(true);
                    fpp.fpRemoveTmpl(fp_id);
                    finish();
                }
            }
        }).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_add_person);
        ButterKnife.bind(this);
        RxView.clicks(img_finger).throttleFirst(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        fpp.fpEnroll(fp_id);
                        img_finger.setClickable(false);
                    }
                });
        img_finger.setClickable(false);
    }

    @Override
    public void onSetImg(Bitmap bmp) {
        img_finger.setImageBitmap(bmp);
        user.setFingerprintPhoto(FileUtils.bitmapToBase64(bmp));
    }

    @Override
    public void onText(String msg) {
        if (!msg.equals("Canceled")) {
            tv_finger.setText(msg);
        }
        if (msg.endsWith("录入成功")) {
            Observable.interval(0, 1, TimeUnit.SECONDS)
                    .take(count + 1)
                    .map(new Function<Long, Long>() {
                        @Override
                        public Long apply(@NonNull Long aLong) throws Exception {
                            return count - aLong;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NonNull Long aLong) {
                            ToastUtils.showLong(aLong + "秒后进入人脸识别界面");
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            FaceDetect(user);
                        }
                    });
        }
        if (msg.endsWith("点我重试")) {
            img_finger.setClickable(true);
        }
    }

    void FaceDetect(UserBean detectUser) {
        Bundle bundle = new Bundle();
        bundle.putString("id", detectUser.getCardId());
        bundle.putString("info",detectUser.getName());
        ActivityUtils.startActivity(bundle,getPackageName(),getPackageName()+".TheFaceDetectActivity");
    }

    @Override
    protected void onResume() {
        super.onResume();
        idp.readCard();
        idp.IDCardPresenterSetView(this);
        fpp.FingerPrintPresenterSetView(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        fpp.fpCancel(true);
        idp.stopReadCard();
        fpp.FingerPrintPresenterSetView(null);
        idp.IDCardPresenterSetView(null);
    }

    @Override
    public void onSetText(String Msg) {

    }

    @Override
    public void onsetCardInfo(final ICardInfo cardInfo) {
        person_name.setText(person_name.getHint() + cardInfo.name());
        person_id.setText(person_id.getHint() + cardInfo.cardId());
        RetrofitGenerator.getConnectApi().queryPersonInfo(config.getString("key"), cardInfo.cardId()/*"632700197011090582"*/)
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
                                if (infoMap.get("status").equals(String.valueOf(1))) {
                                    fp_id = infoMap.get("data");
                                    idp.stopReadCard();
                                    img_finger.setClickable(false);
                                    fpp.fpEnroll(fp_id);
                                    user = new UserBean();
                                    user.setCardId(cardInfo.cardId());
                                    user.setName(cardInfo.name());
                               /*     user.setCardId("632700197011090582");
                                    user.setName(infoMap.get("name"));*/
                                    user.setFingerprintId(fp_id);
                                    user.setCourIds(infoMap.get("courIds"));
                                    user.setCourType(infoMap.get("courType"));
                                } else {
                                    Alarm.getInstance(TheAddPersonActivity.this).messageAlarm("您的身份有误，如有疑问请联系客服处理");
                                }
                            } else if (infoMap.get("result").equals("matchErr")) {
                                Alarm.getInstance(TheAddPersonActivity.this).messageAlarm("系统未能查询到该人员信息，如有疑问请联系客服处理");
                            } else {
                                Alarm.getInstance(TheAddPersonActivity.this).messageAlarm(infoMap.get("result"));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetOpenDoorEvent(OpenDoorEvent event) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("datetime", TimeUtils.getNowString());
            jsonObject.put("state", "n");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RetrofitGenerator.getConnectApi().openDoorRecord(config.getString("key"), jsonObject.toString())
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

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void onsetCardImg(Bitmap bmp) {
        headphoto.setImageBitmap(bmp);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onFpSucc(String msg) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFaceDetectEvent(FaceDetectEvent event) {
        headphoto.setImageBitmap(event.getBitmap());
        userId = event.getUserId();
        commitable = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Alarm.getInstance(this).release();
    }

}
