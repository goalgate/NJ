package com.nj.Function.Func_IDCard.mvp.module;

import android.graphics.Bitmap;

import com.drv.card.CardInfo;
import com.drv.card.CardInfoRk123x;
import com.drv.card.ICardInfo;


/**
 * Created by zbsz on 2017/6/4.
 */

public interface IIDCard {
    void onOpen(IIdCardListener mylistener);

    void onReadCard();

    void onReadSAM();

    void onStopReadCard();

    void onClose();

    interface IIdCardListener {
        void onSetImg(Bitmap bmp);

//        void onSetInfo(CardInfoRk123x cardInfo);

        void onSetInfo(ICardInfo cardInfo);

        void onSetText(String Msg);
    }


}
