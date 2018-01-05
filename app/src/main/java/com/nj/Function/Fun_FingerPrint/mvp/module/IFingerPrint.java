package com.nj.Function.Fun_FingerPrint.mvp.module;

import android.app.Activity;
import android.graphics.Bitmap;

/**
 * Created by zbsz on 2017/6/2.
 */

public interface IFingerPrint {
    void onInit(IFPListener listener);

    void onOpen(IFPListener listener);

    void onClose(IFPListener listener);

    void onCancel(boolean status);

    void onEnroll(String id, IFPListener listener);

    void onVerify(String id, IFPListener listener);

    void onIdentify(IFPListener listener);

    void onGetEnrollCount(IFPListener listener);

    int onGetEmptyID(IFPListener listener);

    void onCaptureImg(IFPListener listener);

    void onRemoveTmpl(String TmplId, IFPListener listener);

    void onRemoveAll(IFPListener listener);

    String onUpTemplate(String id, IFPListener listener);

    void onDownTemplate(String id,String temp,IFPListener listener);

    void sync(String dataList);

    interface IFPListener {

        void onSetImg(Bitmap bmp);

        void onText(String msg);

        void onFpSucc(String msg);
    }
}
