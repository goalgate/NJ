package com.drv.card;

import android.graphics.Bitmap;

public interface ICardInfo {
    void close();
    void clearIsReadOk();
    Bitmap getBmp();
    void readCard();
    void stopReadCard();
    void setDevType(String sType);
    int open();
    void readSam();
    String getSam();
    String cardId();
    String name();
    String sex();
    String nation();
    String birthday();
    String address();

}
