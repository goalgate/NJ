package com.nj.Function.Func_IDCard.mvp.module;

import android.graphics.Bitmap;
import android.util.Log;

import com.drv.card.CardInfo;
import com.drv.card.CardInfoRk123x;
import com.drv.card.ICardInfo;
import com.drv.card.ICardState;
import com.log.Lg;
import com.nj.AppInit;

public class IDCardImpl2 implements IIDCard{
    private static final String TAG = "信息提示";
    private int cdevfd = -1;
    private static ICardInfo cardInfo = null;
    IIdCardListener mylistener;

    @Override
    public void onOpen(IIdCardListener listener) {
        mylistener = listener;
        try {
            if (AppInit.getMyManager().getAndroidDisplay().startsWith("rk3368")) {
                //cardInfo =new CardInfo("/dev/ttyAMA2",m_onCardState);
                cardInfo = new CardInfo("/dev/ttyS0", m_onCardState);
            } else if (Integer.parseInt(AppInit.getMyManager().getAndroidDisplay().substring(AppInit.getMyManager().getAndroidDisplay().indexOf(".20") + 1, AppInit.getMyManager().getAndroidDisplay().indexOf(".20") + 9)) >= 20180903
                    && Integer.parseInt(AppInit.getMyManager().getAndroidDisplay().substring(AppInit.getMyManager().getAndroidDisplay().indexOf(".20") + 1, AppInit.getMyManager().getAndroidDisplay().indexOf(".20") + 9)) < 20180918) {
                cardInfo = new CardInfoRk123x("/dev/ttyS0", m_onCardState);
                //ToastUtils.showLong("rk123,ttyS0");
            } else {
                cardInfo = new CardInfoRk123x("/dev/ttyS1", m_onCardState);
                //ToastUtils.showLong("rk123,ttyS1");
            }
            cardInfo.setDevType("rk3368");
            cdevfd = cardInfo.open();
            if (cdevfd >= 0) {
                Log.e(TAG, "打开身份证读卡器成功");
            } else {
                cdevfd = -1;
                Log.e(TAG, "打开身份证读卡器失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onReadCard() {
        cardInfo.readCard();
    }

    @Override
    public void onStopReadCard() {
        cardInfo.stopReadCard();
    }

    @Override
    public void onReadSAM() {
        cardInfo.readSam();
    }

    private ICardState m_onCardState = new ICardState() {
        @Override
        public void onCardState(int itype, int value) {
            if (itype == 4 && value == 1) {
                mylistener.onSetInfo(cardInfo);
                Bitmap bmp = cardInfo.getBmp();
                if (bmp != null) {
                    mylistener.onSetImg(bmp);
                } else {
                    Lg.e("信息提示", "没有照片");
                }
                cardInfo.clearIsReadOk();
            } else if (itype == 20) {
                mylistener.onSetText("SAM:" + cardInfo.getSam());
            }

        }

    };

    @Override
    public void onClose() {
        cardInfo.close();
    }
}
