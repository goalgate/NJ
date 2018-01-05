package com.nj.Function.Func_IDCard.mvp.view;


import android.graphics.Bitmap;

import com.drv.card.CardInfoRk123x;


/**
 * Created by zbsz on 2017/6/9.
 */

public interface IIDCardView {
    void onsetCardInfo(CardInfoRk123x cardInfo);

    void onsetCardImg(Bitmap bmp);
}
