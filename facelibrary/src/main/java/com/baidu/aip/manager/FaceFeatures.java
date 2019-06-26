/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.manager;

import com.baidu.aip.callback.FaceCallback;
import com.baidu.idl.facesdk.FaceFeature;
import com.baidu.idl.facesdk.callback.Callback;
import com.baidu.idl.facesdk.model.Feature;

import android.content.Context;

import java.util.List;

/**
 * @Time: 2019/1/15
 * @Author: v_chaixiaogang
 * @Description: 人脸特征API
 */
public class FaceFeatures {

    private FaceFeature mFaceFeature;

    public void initModel(Context context, String idPhotoModel, String visModel, String nirModel, final FaceCallback faceCallback) {
        if (mFaceFeature == null) {
            mFaceFeature = new FaceFeature();
            mFaceFeature.initModel(context, idPhotoModel, visModel, nirModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        } else {
            mFaceFeature.initModel(context, idPhotoModel, visModel, nirModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        }

    }

    /**
     * 人脸特征提取
     *
     * @param argb
     * @param landmarks
     * @param height
     * @param width
     * @param feature
     * @return
     */

    public float extractFeature(int[] argb, int height, int width, byte[] feature, int[] landmarks) {
        if (mFaceFeature == null) {
            mFaceFeature = new FaceFeature();
            return mFaceFeature.feature(FaceFeature.FeatureType.FEATURE_VIS, argb, height, width, landmarks, feature);
        } else {
            return mFaceFeature.feature(FaceFeature.FeatureType.FEATURE_VIS, argb, height, width, landmarks, feature);
        }
    }

    public float extractFeatureForIDPhoto(int[] argb, int height, int width, byte[] feature, int[] landmarks) {
        if (mFaceFeature == null) {
            mFaceFeature = new FaceFeature();
            return mFaceFeature.feature(FaceFeature.FeatureType.FEATURE_ID_PHOTO, argb, height,
                    width, landmarks, feature);
        } else {
            return mFaceFeature.feature(FaceFeature.FeatureType.FEATURE_ID_PHOTO, argb, height,
                    width, landmarks, feature);
        }
    }

    /**
     * 人脸特征比对,并且映射到0--100
     *
     * @param feature1
     * @param feature2
     * @return
     */
    public float featureCompare(byte[] feature1, byte[] feature2) {
        if (mFaceFeature == null) {
            mFaceFeature = new FaceFeature();
            return mFaceFeature.featureCompare(FaceFeature.FeatureType.FEATURE_VIS, feature1, feature2);
        } else {
            return mFaceFeature.featureCompare(FaceFeature.FeatureType.FEATURE_VIS, feature1, feature2);
        }
    }

    public float featureIDCompare(byte[] feature1, byte[] feature2) {
        if (mFaceFeature == null) {
            mFaceFeature = new FaceFeature();
            return mFaceFeature.featureCompare(FaceFeature.FeatureType.FEATURE_ID_PHOTO, feature1, feature2);
        } else {
            return mFaceFeature.featureCompare(FaceFeature.FeatureType.FEATURE_ID_PHOTO, feature1, feature2);
        }
    }

    public int setFeature(List<? extends Feature> features) {
        if (mFaceFeature == null) {
            mFaceFeature = new FaceFeature();
            return mFaceFeature.setFeature(features);
        } else {
            return mFaceFeature.setFeature(features);
        }
    }

    public Feature featureCompareCpp(byte[] firstFaceFeature, FaceFeature.FeatureType featureType,
                                     float thresholdValue) {
        if (mFaceFeature == null) {
            mFaceFeature = new FaceFeature();
            return mFaceFeature.featureCompareCpp(firstFaceFeature, featureType, thresholdValue);
        } else {
            return mFaceFeature.featureCompareCpp(firstFaceFeature, featureType, thresholdValue);
        }
    }
}
