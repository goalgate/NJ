package com.nj.Function.Func_Face.mvp.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.TextureView;

import com.baidu.aip.entity.User;
import com.baidu.aip.face.AutoTexturePreviewView;
import com.baidu.aip.manager.FaceSDKManager;
import com.drv.card.ICardInfo;
import com.nj.Function.Func_Face.mvp.Module.FaceImpl2;
import com.nj.Function.Func_Face.mvp.Module.IFace;
import com.nj.Function.Func_Face.mvp.view.IFaceView;


public class FacePresenter {
    private IFaceView view;

    private IFace iFace = new FaceImpl2();

    private static FacePresenter instance = null;

    public enum FaceAction {
        No_ACTION, Reg_ACTION, Identify_ACTION, IMG_MATCH_IMG
    }

    public enum FaceResultType {
        Reg, Identify, IMG_MATCH_IMG_False, IMG_MATCH_IMG_True, IMG_MATCH_IMG_Error,
        AllView, Identify_non
    }

    private FacePresenter() {
    }

    public static FacePresenter getInstance() {
        if (instance == null) {
            instance = new FacePresenter();
        }
        return instance;
    }

//    public void FaceSetContext(Context context){
//        iFace.setContext(context);
//    }

    public void FacePresenterSetView(IFaceView view) {
        this.view = view;
    }

    public void FaceInit(Context context, FaceSDKManager.SdkInitListener listener) {
        iFace.FaceInit(context, listener);
    }

    public void FaceSetNoAction(){
        iFace.FaceSetNoAction();
    }

    public void CameraPreview(Context context, AutoTexturePreviewView previewView, TextureView textureView) {
        iFace.CameraPreview(context, previewView, textureView, new IFace.IFaceListener() {
            @Override
            public void onText(FaceResultType resultType, String text) {
                try {
                    view.onText(resultType, text);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmap(FaceResultType resultType, Bitmap bitmap) {
                try {
                    view.onBitmap(resultType, bitmap);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onUser(FaceResultType resultType, User user) {
                try {
                    view.onUser(resultType, user);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void FaceIdentify() {
        iFace.FaceIdentify();
    }

    public void FaceIdentifyReady() {
        iFace.FaceIdentifyReady();
    }

    public void FaceGetAllView() {
        iFace.Face_allView();
    }


    public void FaceReg(ICardInfo cardInfo, Bitmap bitmap) {
        iFace.FaceReg(cardInfo, bitmap);
    }

    public void Face_to_IMG(Bitmap bitmap) {
        iFace.Face_to_IMG(bitmap);
    }

//    public void FaceOnActivityStart() {
//        iFace.FaceOnActivityStart();
//    }
//
//    public void FaceOnActivityStop() {
//        iFace.FaceOnActivityStop();
//    }
//
//
//    public void FaceOnActivityDestroy() {
//        iFace.FaceOnActivityDestroy();
//    }

    public void setIdentifyStatus(int identifyStatus) {
        iFace.setIdentifyStatus(identifyStatus);
    }

    public void PreviewCease(){
        iFace.PreviewCease();
    }


}
