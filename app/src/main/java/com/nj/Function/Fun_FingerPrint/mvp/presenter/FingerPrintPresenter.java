package com.nj.Function.Fun_FingerPrint.mvp.presenter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;

import com.nj.Function.Fun_FingerPrint.mvp.module.FingerPrintImpl;
import com.nj.Function.Fun_FingerPrint.mvp.module.IFingerPrint;
import com.nj.Function.Fun_FingerPrint.mvp.view.IFingerPrintView;


/**
 * Created by zbsz on 2017/6/9.
 */

public class FingerPrintPresenter {

    private IFingerPrintView view;

    private static FingerPrintPresenter instance = null;

    private FingerPrintPresenter() {
    }

    public static FingerPrintPresenter getInstance() {
        if (instance == null)
            instance = new FingerPrintPresenter();
        return instance;
    }

    public void FingerPrintPresenterSetView(IFingerPrintView view) {
        this.view = view;
    }


    IFingerPrint fpModule = new FingerPrintImpl();

    public void fpInit() {
        fpModule.onInit(getFpListener());
    }

    public void fpOpen() {

        fpModule.onOpen(getFpListener());
    }

    public void fpClose() {
        fpModule.onClose(getFpListener());

    }

    public void fpCancel(boolean status) {
        fpModule.onCancel(status);
    }

    public void fpEnroll(String id) {
        fpModule.onEnroll(id, getFpListener());
    }

    public void fpVerify(String id) {
        fpModule.onVerify(id, getFpListener());
    }

    public void fpIdentify() {
        fpModule.onIdentify(getFpListener());
    }

    public void fpGetEnrollCount() {
        fpModule.onGetEnrollCount(getFpListener());
    }

    public int fpGetEmptyID() {
        return fpModule.onGetEmptyID(getFpListener());
    }

    public void fpCaptureImg() {
        fpModule.onCaptureImg(getFpListener());
    }

    public void fpRemoveTmpl(String TmplId) {
        fpModule.onRemoveTmpl(TmplId, getFpListener());
    }

    public void fpRemoveAll() {
        fpModule.onRemoveAll(getFpListener());
    }

    public String fpUpTemlate(String id) {
        return fpModule.onUpTemplate(id, getFpListener());
    }

    public void fpDownTemplate(String id,String temp) {
        fpModule.onDownTemplate(id ,temp, getFpListener());
    }


    public void sync(String dataList){
        fpModule.sync(dataList);
    }

    private IFingerPrint.IFPListener getFpListener() {
        return new IFingerPrint.IFPListener() {

            @Override
            public void onSetImg(Bitmap bmp) {
                if (view != null) {
                    view.onSetImg(bmp);
                }
            }


            @Override
            public void onText(String msg) {
                if (view != null) {
                    view.onText(msg);
                }
            }

            @Override
            public void onFpSucc(String msg) {
                if (view != null) {
                    view.onFpSucc(msg);
                }
            }
        };
    }
}
