package com.nj.Function.Func_Face.mvp.Module;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.TextureView;

import com.baidu.aip.entity.User;
import com.baidu.aip.face.AutoTexturePreviewView;
import com.baidu.aip.manager.FaceSDKManager;
import com.drv.card.ICardInfo;
import com.nj.Function.Func_Face.mvp.presenter.FacePresenter;


public interface IFace {

    void FaceInit(Context context, FaceSDKManager.SdkInitListener listener);

    void CameraPreview(Context context, AutoTexturePreviewView previewView, TextureView textureView, IFaceListener listener);

    void FaceIdentify();

    void FaceReg(ICardInfo cardInfo, Bitmap bitmap) ;

    void Face_to_IMG(Bitmap bitmap);

    void Face_allView();

    void FaceSetNoAction();

    void setIdentifyStatus(int i);

    void FaceIdentifyReady();

    void PreviewCease();


    interface IFaceListener{
        void onText(FacePresenter.FaceResultType resultType, String text);

        void onBitmap(FacePresenter.FaceResultType resultType, Bitmap bitmap);

        void onUser(FacePresenter.FaceResultType resultType, User user);

    }

}
