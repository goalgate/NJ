package com.nj.Function.Func_IDCard.mvp.module;

import android.graphics.Bitmap;

import com.drv.card.CardInfoRk123x;


/**
 * Created by zbsz on 2017/6/4.
 */

public interface IIDCard {
    void onOpen(IIdCardListener mylistener);

    void onReadCard();

    void onStopReadCard();

    void onClose();

    interface IIdCardListener {
        void onSetImg(Bitmap bmp);

        void onSetInfo(CardInfoRk123x cardInfo);
    }


}
