package com.nj;

import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;

import com.baidu.aip.face.AutoTexturePreviewView;
import com.blankj.utilcode.util.BarUtils;
import com.nj.Alerts.Alarm;
import com.nj.Function.Fun_FingerPrint.mvp.presenter.FingerPrintPresenter;
import com.nj.Function.Fun_FingerPrint.mvp.view.IFingerPrintView;
import com.nj.Function.Func_Face.mvp.presenter.FacePresenter;
import com.nj.Function.Func_Face.mvp.view.IFaceView;
import com.nj.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import com.nj.Function.Func_IDCard.mvp.view.IIDCardView;
import com.nj.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import com.nj.UI.AddPersonWindow;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.RxActivity;
import java.util.concurrent.TimeUnit;
import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

import static com.nj.Function.Func_Face.mvp.Module.FaceImpl2.FEATURE_DATAS_UNREADY;


public abstract class TheFunctionActivity extends RxActivity implements IFaceView, IFingerPrintView,IIDCardView ,AddPersonWindow.OptionTypeListener {

    private String TAG = IndexActivity.class.getSimpleName();

    @BindView(R.id.preview_view)
    AutoTexturePreviewView previewView;

    @BindView(R.id.texture_view)
    TextureView textureView;

    SwitchPresenter sp = SwitchPresenter.getInstance();

    FingerPrintPresenter fpp = FingerPrintPresenter.getInstance();

    FacePresenter fp = FacePresenter.getInstance();

    IDCardPresenter idp = IDCardPresenter.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        Log.e(TAG,"onCreate");
        idp.idCardOpen();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG,"onResume");
        fp.FacePresenterSetView(this);
        fp.FaceIdentifyReady();
        fp.FaceIdentify();
        fpp.FingerPrintPresenterSetView(this);
        Observable.timer(3, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
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
        Log.e(TAG,"onPause");
        fpp.fpCancel(true);
        fpp.FingerPrintPresenterSetView(null);

        fp.FaceSetNoAction();
        fp.setIdentifyStatus(FEATURE_DATAS_UNREADY);
        fp.PreviewCease();
        fp.FacePresenterSetView(null);

        idp.IDCardPresenterSetView(null);
        idp.stopReadCard();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        fpp.fpClose();
        idp.idCardClose();
        Alarm.getInstance(this).release();

    }

    @Override
    public void onBackPressed() {

    }
}
