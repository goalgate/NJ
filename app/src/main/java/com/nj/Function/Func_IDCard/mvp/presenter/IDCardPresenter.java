package com.nj.Function.Func_IDCard.mvp.presenter;

import android.graphics.Bitmap;

import com.drv.card.CardInfo;
import com.drv.card.CardInfoRk123x;
import com.drv.card.ICardInfo;
import com.nj.Function.Func_IDCard.mvp.module.IDCardImpl;
import com.nj.Function.Func_IDCard.mvp.module.IDCardImpl2;
import com.nj.Function.Func_IDCard.mvp.module.IIDCard;
import com.nj.Function.Func_IDCard.mvp.view.IIDCardView;


/**
 * Created by zbsz on 2017/6/9.
 */

public class IDCardPresenter {
    private IIDCardView view;

    private static IDCardPresenter instance = null;

    private IDCardPresenter() {
    }

    public static IDCardPresenter getInstance() {
        if (instance == null)
            instance = new IDCardPresenter();
        return instance;
    }

    public void IDCardPresenterSetView(IIDCardView view) {
        this.view = view;
    }

    IIDCard idCardModule = new IDCardImpl2();

    public void idCardOpen() {
        idCardModule.onOpen(new IIDCard.IIdCardListener() {
            @Override
            public void onSetImg(Bitmap bmp) {
                view.onsetCardImg(bmp);
            }

            @Override
            public void onSetInfo(ICardInfo cardInfo) {
                view.onsetCardInfo(cardInfo);
            }

            @Override
            public void onSetText(String Msg) {
                view.onSetText(Msg);
            }
        });
    }

    public void readCard() {
        idCardModule.onReadCard();
    }

    public void stopReadCard() {
        idCardModule.onStopReadCard();
    }

    public void idCardClose(){
        idCardModule.onClose();
    }

    public void readSam(){
        idCardModule.onReadSAM();
    }
}
