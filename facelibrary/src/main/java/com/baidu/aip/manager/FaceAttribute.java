package com.baidu.aip.manager;

import android.content.Context;

import com.baidu.aip.callback.FaceCallback;
import com.baidu.idl.facesdk.FaceAttributes;
import com.baidu.idl.facesdk.callback.Callback;
import com.baidu.idl.facesdk.model.BDFaceSDKAttribute;
import com.baidu.idl.facesdk.model.BDFaceSDKEmotions;

/**
 * @Time: 2019/1/15
 * @Author: v_chaixiaogang
 * @Description: 属性检测API
 */
public class FaceAttribute {

    private FaceAttributes mFaceAttributes;

    public void initModel(Context context, String atttibuteModel, String emotionModel, final FaceCallback faceCallback) {
        if (mFaceAttributes == null) {
            mFaceAttributes = new FaceAttributes();
            mFaceAttributes.initModel(context, atttibuteModel, emotionModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        } else {
            mFaceAttributes.initModel(context, atttibuteModel, emotionModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        }

    }

    /**
     * 人脸图片属性检测
     *
     * @param imageData
     * @param height
     * @param width
     * @param landmarks
     * @return
     */

    public BDFaceSDKAttribute attribute(int[] imageData, int height, int width, int[] landmarks) {
        return mFaceAttributes.attribute(imageData, height, width, landmarks);
    }

    /**
     * 人脸图片表情检测
     *
     * @param imageData
     * @param height
     * @param width
     * @param landmarks
     * @return
     */
    public BDFaceSDKEmotions emotions(int[] imageData, int height, int width, int[] landmarks) {
        return mFaceAttributes.emotions(imageData, height, width, landmarks);
    }

}
