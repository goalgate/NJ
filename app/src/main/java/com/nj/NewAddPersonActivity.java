package com.nj;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.drv.card.ICardInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.rxbinding2.view.RxView;
import com.nj.Alerts.Alarm;
import com.nj.EventBus.OpenDoorEvent;
import com.nj.Function.Fun_FingerPrint.mvp.presenter.FingerPrintPresenter;
import com.nj.Function.Fun_FingerPrint.mvp.view.IFingerPrintView;
import com.nj.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import com.nj.Function.Func_IDCard.mvp.view.IIDCardView;
import com.nj.Retrofit.RetrofitGenerator;
import com.nj.Tools.FileUtils;
import com.nj.Tools.MyObserver;
import com.nj.Bean.UserBean;

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
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class NewAddPersonActivity extends Activity implements IFingerPrintView, IIDCardView {
    SPUtils config = SPUtils.getInstance("config");

    SPUtils courIds = SPUtils.getInstance("courId");

    /*    SPUtils pfpIds = SPUtils.getInstance("pfpIds");*/

    SPUtils courTypes = SPUtils.getInstance("courType");

    FingerPrintPresenter fpp = FingerPrintPresenter.getInstance();

    boolean commitable;

    UserBean user;

    String fp_id = "0";

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
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", user.getCardId());
                    jsonObject.put("dataType","1");
                    jsonObject.put("fingerprintPhoto", user.getFingerprintPhoto());
                    jsonObject.put("fingerprintId", user.getFingerprintId());
                    jsonObject.put("fingerprintKey", fpp.fpUpTemlate(user.getFingerprintId()));
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
                                        SPUtils user_sp = SPUtils.getInstance(user.getFingerprintId());
                                        user_sp.put("courIds", user.getCourIds());
                                        user_sp.put("name", user.getName());
                                        user_sp.put("cardId", user.getCardId());
                                        user_sp.put("courType", user.getCourType());
                                        courIds.put(user.getCardId(), user.getCourIds());
                                        courTypes.put(user.getCardId(), user.getCourType());
                                        fp_id = "0";
                                        ToastUtils.showLong("人员插入成功");
                                        cancel();
                                    } else if (infoMap.get("result").equals("false")) {
                                        Alarm.getInstance(NewAddPersonActivity.this).messageAlarm("数据插入有错");
//                                        new AlertView("数据插入有错", null, null, new String[]{"确定"}, null, NewAddPersonActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
//                                            @Override
//                                            public void onItemClick(Object o, int position) {
//
//                                            }
//                                        }).show();
                                    } else {
                                        Alarm.getInstance(NewAddPersonActivity.this).messageAlarm(infoMap.get("result"));
//                                        new AlertView(infoMap.get("result"), null, null, new String[]{"确定"}, null, NewAddPersonActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
//                                            @Override
//                                            public void onItemClick(Object o, int position) {
//
//                                            }
//                                        }).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
            } else {
                Alarm.getInstance(this).messageAlarm("您的操作有误，请重试");
//                new AlertView("您的操作有误，请重试", null, null, new String[]{"确定"}, null, NewAddPersonActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
//                    @Override
//                    public void onItemClick(Object o, int position) {
//
//                    }
//                }).show();
            }
        } else {
            Alarm.getInstance(this).messageAlarm("您还有信息未登记，如需退出请按取消");
//            new AlertView("您还有信息未登记，如需退出请按取消", null, null, new String[]{"确定"}, null, NewAddPersonActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
//                @Override
//                public void onItemClick(Object o, int position) {
//
//                }
//            }).show();
        }


    }

    @OnClick(R.id.btn_cancel)
    void cancel() {
        new AlertView("请选择接下来的操作", null, null, new String[]{"重置", "退出"}, null, this, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    fp_id = "0";
                    commitable = false;
                    user = new UserBean();
                    idp.readCard();
                    person_id.setHint("人员姓名：");
                    person_id.setText(null);
                    person_name.setHint("人员身份证：");
                    person_name.setText(null);
                    headphoto.setImageBitmap(null);
                    img_finger.setClickable(false);
                    fpp.fpCancel(true);
                    fpp.fpRemoveTmpl(fp_id);
                    tv_finger.setText("请刷身份证以获得指纹编号");
                    img_finger.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.zw_icon));
                    //fpp.fpRemoveAll();
                } else {
                    fpp.fpCancel(true);
                    fpp.fpRemoveTmpl(fp_id);
                    finish();
                }
            }
        }).show();
//        fpp.fpRemoveTmpl(String.valueOf(fp_id));
//        finish();
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
            commitable = true;
        }
        if (msg.endsWith("点我重试")) {
            img_finger.setClickable(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        fpp.FingerPrintPresenterSetView(this);
        idp.IDCardPresenterSetView(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        fpp.fpCancel(true);
        idp.stopReadCard();
        //fpp.FingerPrintPresenterSetView(null);
    }


    @Override
    public void onSetText(String Msg) {

    }

    @Override
    public void onsetCardInfo(final ICardInfo cardInfo) {
        person_name.setText("人员姓名：" + cardInfo.name());
        person_id.setText("人员身份证：" + cardInfo.cardId());
        RetrofitGenerator.getConnectApi().queryPersonInfo(config.getString("key"),cardInfo.cardId()/*"632700197011090582"*/)
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
                                    Alarm.getInstance(NewAddPersonActivity.this).messageAlarm("您的身份有误，如有疑问请联系客服处理");
//                                    new AlertView("您的身份有误，如有疑问请联系客服处理", null, null, new String[]{"确定"}, null, NewAddPersonActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
//                                        @Override
//                                        public void onItemClick(Object o, int position) {
//                                            infoClear();
//                                        }
//                                    }).show();
                                }
                            }else if(infoMap.get("result").equals("matchErr")){
                                Alarm.getInstance(NewAddPersonActivity.this).messageAlarm("系统未能查询到该人员信息，如有疑问请联系客服处理");

//                                new AlertView("系统未能查询到该人员信息，如有疑问请联系客服处理", null, null, new String[]{"确定"}, null, NewAddPersonActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
//                                    @Override
//                                    public void onItemClick(Object o, int position) {
//                                        infoClear();
//                                    }
//                                }).show();
                            }else {
                                Alarm.getInstance(NewAddPersonActivity.this).messageAlarm(infoMap.get("result"));

//                                new AlertView(infoMap.get("result"), null, null, new String[]{"确定"}, null, NewAddPersonActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
//                                    @Override
//                                    public void onItemClick(Object o, int position) {
//                                        infoClear();
//                                    }
//                                }).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }catch (Exception e){
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

    @Override
    protected void onResume() {
        super.onResume();
        commitable = false;
        idp.readCard();
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        Alarm.getInstance(this).release();
    }
}
