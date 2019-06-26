package com.baidu.aip.manager;

import android.content.Context;

import com.baidu.idl.facesdk.FaceDetect;
import com.baidu.idl.facesdk.FaceFeature;
import com.baidu.idl.facesdk.callback.Callback;
import com.baidu.idl.facesdk.model.BDFaceSDKConfig;
import com.baidu.idl.facesdk.model.FaceInfo;

/**
 * Time: 2018/12/4
 * Author: v_chaixiaogang
 * Description: 人脸特征API
 */
public class FacefeaturesImage {

    private FaceDetect faceDetect;
    private FaceFeature faceFeature;

    public void initMdoel(final Context context, String visDetectModel, String alignModel, final String idPhotoModel,
                          final String visFeatureModel, final Callback callback) {
        faceDetect = new FaceDetect();
        BDFaceSDKConfig config = new BDFaceSDKConfig();
        config.detectInterval = config.trackInterval = 0;
        faceDetect.loadConfig(config);
        faceDetect.initModel(context, visDetectModel, "", alignModel, new Callback() {
            @Override
            public void onResponse(int code, String response) {
                if (code == 0) {
                    faceFeature = new FaceFeature();
                    faceFeature.initModel(context, idPhotoModel, visFeatureModel, "", callback);
                } else {
                    callback.onResponse(code, response);
                }
            }
        });
    }

    public float extractFeature(int[] argb, int height, int width, byte[] feature) {
        if (faceDetect == null || faceFeature == null) {
            return -1;
        }

        FaceInfo[] faceInfos = faceDetect.trackMaxFace(argb, height, width);
        faceDetect.clearTrackedFaces();
        if (faceInfos == null || faceInfos.length == 0) {
            return -1;
        }
        FaceInfo faceInfo = faceInfos[0];
        return faceFeature.feature(FaceFeature.FeatureType.FEATURE_VIS,
                argb, height, width, faceInfo.landmarks, feature);
    }


    public float extractVisFeature(int[] argb, int height, int width, byte[] feature, int minFaceSize) {
        float ret = -1;
        if (faceDetect == null || faceFeature == null) {
            return ret;
        }

        faceDetect.setDetectMethodType(FaceDetect.DetectType.DETECT_VIS);

        FaceInfo[] faceInfos = faceDetect.detect(argb, height, width, minFaceSize);
        if (faceInfos == null || faceInfos.length == 0) {
            return ret;
        }
        FaceInfo faceInfo = faceInfos[0];
        faceInfo = faceDetect.align(argb, height, width, faceInfo);
        ret = faceFeature.feature(FaceFeature.FeatureType.FEATURE_VIS,
                argb, height, width, faceInfo.landmarks, feature);
        faceDetect.clearTrackedFaces();
        return ret;
    }

    public float extractIdPhotoFeature(int[] argb, int height, int width, byte[] feature, int minFaceSize) {
        float ret = -1;
        if (faceDetect == null || faceFeature == null) {
            return ret;
        }

        faceDetect.setDetectMethodType(FaceDetect.DetectType.DETECT_VIS);

        FaceInfo[] faceInfos = faceDetect.detect(argb, height, width, minFaceSize);
        if (faceInfos == null || faceInfos.length == 0) {
            return ret;
        }
        FaceInfo faceInfo = faceInfos[0];
        faceInfo = faceDetect.align(argb, height, width, faceInfo);
        ret = faceFeature.feature(FaceFeature.FeatureType.FEATURE_ID_PHOTO,
                argb, height, width, faceInfo.landmarks, feature);
        faceDetect.clearTrackedFaces();
        return ret;
    }

    public float extractIdFeature(int[] argb, int height, int width, byte[] feature) {
        if (faceDetect == null || faceFeature == null) {
            return -1;
        }

        FaceInfo[] faceInfos = faceDetect.trackMaxFace(argb, height, width);
        faceDetect.clearTrackedFaces();
        if (faceInfos == null || faceInfos.length == 0) {
            return -1;
        }
        FaceInfo faceInfo = faceInfos[0];
        return faceFeature.feature(FaceFeature.FeatureType.FEATURE_ID_PHOTO,
                argb, height, width, faceInfo.landmarks, feature);
    }

    public float featureCompare(byte[] feature1, byte[] feature2) {
        if (faceFeature == null) {
            return -1;
        }
        return faceFeature.featureCompare(FaceFeature.FeatureType.FEATURE_VIS, feature1, feature2);
    }

    public float featureIdCompare(byte[] feature1, byte[] feature2) {
        if (faceFeature == null) {
            return -1;
        }
        return faceFeature.featureCompare(FaceFeature.FeatureType.FEATURE_ID_PHOTO, feature1, feature2);
    }
}
