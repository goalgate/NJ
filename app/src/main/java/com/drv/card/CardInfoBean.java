package com.drv.card;

import android.graphics.Bitmap;

public class CardInfoBean implements ICardInfo {

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public void setName(String name) {
        this.name = name;
    }

    String cardID;
    String name;


    @Override
    public String getSam() {
        return null;
    }

    @Override
    public Bitmap getBmp() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public String cardId() {
        return cardID;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String nation() {
        return null;
    }

    @Override
    public void readCard() {

    }

    @Override
    public void readSam() {

    }

    @Override
    public int open() {
        return 0;
    }

    @Override
    public void clearIsReadOk() {

    }

    @Override
    public String birthday() {
        return null;
    }

    @Override
    public void stopReadCard() {

    }

    @Override
    public String sex() {
        return null;
    }

    @Override
    public void setDevType(String sType) {

    }

    @Override
    public String address() {
        return null;
    }
}
