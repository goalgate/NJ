package com.nj;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.TextureView;

import com.baidu.aip.entity.User;
import com.baidu.aip.face.AutoTexturePreviewView;
import com.baidu.aip.face.PreviewView;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.drv.card.CardInfo;
import com.drv.card.CardInfoBean;
import com.drv.card.ICardInfo;
import com.nj.EventBus.FaceDetectEvent;
import com.nj.Function.Func_Face.mvp.presenter.FacePresenter;
import com.nj.Function.Func_Face.mvp.view.IFaceView;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TheFaceDetectActivity extends Activity implements IFaceView {

    FacePresenter fp = FacePresenter.getInstance();

    CardInfoBean cardInfo = new CardInfoBean();

    @BindView(R.id.preview_view)
    AutoTexturePreviewView previewView;

    @BindView(R.id.texture_view)
    TextureView textureView;

    User re_user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_facedetect);
        ButterKnife.bind(this);
        Bundle bundle = getIntent().getExtras();
        cardInfo.setCardID(bundle.getString("id"));
        cardInfo.setName(bundle.getString("info"));
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            fp.CameraPreview(AppInit.getContext(), previewView, textureView);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fp.FacePresenterSetView(this);
        fp.FaceReg(cardInfo, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        fp.FaceSetNoAction();
        fp.PreviewCease();
        fp.FacePresenterSetView(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    Bitmap mbitmap;
    @Override
    public void onBitmap(FacePresenter.FaceResultType resultType, Bitmap bitmap) {
        mbitmap = bitmap;
    }

    @Override
    public void onText(FacePresenter.FaceResultType resultType, String text) {
        if (resultType.equals(FacePresenter.FaceResultType.Reg)) {
            if (text.equals("success")) {
                EventBus.getDefault().post(new FaceDetectEvent(mbitmap,re_user.getUserId()));
            } else {
                ToastUtils.showLong("人脸数据获取失败，请重试");
            }
            finish();
        }
    }


    @Override
    public void onUser(FacePresenter.FaceResultType resultType, User user) {
        re_user = user;
    }

    @Override
    public void onBackPressed() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFaceDetectEvent(FaceDetectEvent event) {

    }

}
