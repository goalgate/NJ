package com.nj;

import android.app.Activity;
import android.app.ProgressDialog;
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

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.BarUtils;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;

import com.drv.card.CardInfo;
import com.drv.card.CardInfoRk123x;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.rxbinding2.view.RxView;
import com.nj.EventBus.OpenDoorEvent;
import com.nj.Function.Fun_FingerPrint.mvp.presenter.FingerPrintPresenter;
import com.nj.Function.Fun_FingerPrint.mvp.view.IFingerPrintView;
import com.nj.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import com.nj.Function.Func_IDCard.mvp.view.IIDCardView;
import com.nj.Retrofit.RetrofitGenerator;
import com.nj.Tools.FileUtils;
import com.nj.Tools.User;

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
import retrofit2.Retrofit;
import retrofit2.http.Path;


/**
 * Created by zbsz on 2017/7/31.
 */

public class AddPersonActivity extends Activity implements IFingerPrintView,IIDCardView {
    SPUtils config = SPUtils.getInstance("config");

    SPUtils courIds = SPUtils.getInstance("courId");

    SPUtils courTypes = SPUtils.getInstance("courType");

    FingerPrintPresenter fpp = FingerPrintPresenter.getInstance();

    boolean commitable;

    User user ;

    int fp_id = 0;

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


/*
    final ProgressDialog progressDialog = new ProgressDialog(AddPersonActivity.this);
*/
    @OnClick(R.id.btn_commit)
    void commit() {
        if(commitable){
            if(user.getFingerprintId()!= null){
                SPUtils user_sp = SPUtils.getInstance(user.getFingerprintId());
                user_sp.put("courIds",user.getCourIds());
                user_sp.put("name",user.getName());
                user_sp.put("cardId",user.getCardId());
                user_sp.put("courType",user.getCourType());
                courIds.put(user.getCardId(),user.getCourIds());
                courTypes.put(user.getCardId(),user.getCourType());
                ToastUtils.showLong("人员插入成功");
                finish();
                /*JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", user.getCardId());
                    jsonObject.put("fingerprintPhoto",user.getFingerprintPhoto());
                    jsonObject.put("fingerprintId",user.getFingerprintId());
                    jsonObject.put("fingerprintKey",fpp.fpUpTemlate(user.getFingerprintId()));
                    jsonObject.put("datetime",TimeUtils.getNowString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RetrofitGenerator.getFingerLogApi().fingerLog(config.getString("key"),jsonObject.toString())
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<ResponseBody>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                progressDialog.setMessage("数据上传中，请稍候");
                                progressDialog.show();
                            }

                            @Override
                            public void onNext(@NonNull ResponseBody responseBody) {
                                try{
                                    Map<String, String> infoMap = new Gson().fromJson(responseBody.string(),
                                            new TypeToken<HashMap<String, String>>() {
                                            }.getType());
                                    if(infoMap.get("result").equals("true")){
                                        SPUtils user_sp = SPUtils.getInstance(user.getFingerprintId());
                                        user_sp.put("courIds",user.getCourIds());
                                        user_sp.put("name",user.getName());
                                        user_sp.put("cardId",user.getCardId());
                                        user_sp.put("courType",user.getCourType());
                                        courIds.put(user.getCardId(),user.getCourIds());
                                        courTypes.put(user.getCardId(),user.getCourType());
                                        ToastUtils.showLong("人员插入成功");
                                        finish();
                                   }else if(infoMap.get("result").equals("dataErr")){
                                        new AlertView("验证失败", null, null, new String[]{"确定"}, null, AddPersonActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                                            @Override
                                            public void onItemClick(Object o, int position) {

                                            }
                                        }).show();
                                   }else if(infoMap.get("result").equals("dbErr")){
                                        new AlertView("数据库操作有错", null, null, new String[]{"确定"}, null, AddPersonActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                                            @Override
                                            public void onItemClick(Object o, int position) {

                                            }
                                        }).show();
                                    }
                                }catch (IOException e){
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                progressDialog.dismiss();
                                new AlertView("无法连接数据库", null, null, new String[]{"确定"}, null, AddPersonActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(Object o, int position) {

                                    }
                                }).show();
                            }

                            @Override
                            public void onComplete() {
                                progressDialog.dismiss();
                            }
                        });*/

            }else{
                new AlertView("您的操作有误，请重试", null, null, new String[]{"确定"}, null, AddPersonActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, int position) {

                    }
                }).show();
            }
        }else{
            new AlertView("您还有信息未登记，如需退出请按取消", null, null, new String[]{"确定"}, null, AddPersonActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, int position) {

                }
            }).show();
        }



    }

    @OnClick(R.id.btn_cancel)
    void cancel(){
        fpp.fpRemoveTmpl(String.valueOf(fp_id));
        finish();
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
                        if (commitable) {
                            fpp.fpEnroll(String.valueOf(fp_id));
                            img_finger.setClickable(false);
                        }
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
            commitable =true;

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
    public void onsetCardInfo(final CardInfoRk123x cardInfo) {
        person_name.setText("人员姓名："+cardInfo.name());
        person_id.setText("人员身份证："+cardInfo.cardId());
        RetrofitGenerator.getQueryPersonInfoApi().queryPersonInfo(config.getString("key"),/*cardInfo.cardId()*/"632700197011090582")
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        try {
                            Map<String, String> infoMap = new Gson().fromJson(responseBody.string(),
                                    new TypeToken<HashMap<String, String>>() {
                                    }.getType());
                            if(infoMap.get("result").equals("true")){
                                if(infoMap.get("status").equals(String.valueOf(1))){
                                    fp_id = fpp.fpGetEmptyID();
                                    idp.stopReadCard();
                                    img_finger.setClickable(false);
                                    fpp.fpEnroll(String.valueOf(fp_id));
                                    user = new User();
                                  /*  user.setCardId(cardInfo.cardId());
                                    user.setName(cardInfo.name());*/
                                    user.setCardId("632700197011090582");
                                    user.setName(infoMap.get("name"));
                                    user.setFingerprintId(String.valueOf(fp_id));
                                    user.setCourIds(infoMap.get("courIds"));
                                    user.setCourType(infoMap.get("courType"));
                                }else{
                                    new AlertView("您的身份有误，如有疑问请联系客服处理", null, null, new String[]{"确定"}, null, AddPersonActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                                        @Override
                                        public void onItemClick(Object o, int position) {

                                        }
                                    }).show();
                                }
                            }else{
                                new AlertView(infoMap.get("result"), null, null, new String[]{"确定"}, null, AddPersonActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(Object o, int position) {

                                    }
                                }).show();
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        new AlertView("无法连接服务器", null, null, new String[]{"确定"}, null, AddPersonActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                            @Override
                            public void onItemClick(Object o, int position) {

                            }
                        }).show();
                    }

                    @Override
                    public void onComplete() {

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
        RetrofitGenerator.getOpenDoorRecordApi().openDoorRecord(config.getString("key"),jsonObject.toString())
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
        commitable =false;
        idp.readCard();
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


