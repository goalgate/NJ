package com.nj.EventBus;

import android.graphics.Bitmap;

public class FaceDetectEvent {

    Bitmap bitmap;

    String userId;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getUserId() {
        return userId;
    }

    public FaceDetectEvent(Bitmap bitmap, String userId) {
        this.bitmap = bitmap;
        this.userId = userId;

    }
}
