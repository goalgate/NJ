package com.nj.Function.Func_Face.mvp.view;

import android.graphics.Bitmap;

import com.baidu.aip.entity.User;
import com.nj.Function.Func_Face.mvp.presenter.FacePresenter;


public interface IFaceView {
    void onText(FacePresenter.FaceResultType resultType, String text);

    void onBitmap(FacePresenter.FaceResultType resultType, Bitmap bitmap);

    void onUser(FacePresenter.FaceResultType resultType, User user);

}
