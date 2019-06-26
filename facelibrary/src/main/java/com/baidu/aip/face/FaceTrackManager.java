package com.baidu.aip.face;

import com.baidu.aip.callback.FaceDetectCallBack;
import com.baidu.aip.entity.LivenessModel;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.idl.facesdk.model.FaceInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Time: 2019/1/25
 * Author: v_chaixiaogang
 * rgb单目检测管理类
 */
public class FaceTrackManager {

    private static volatile FaceTrackManager instance = null;

    private ExecutorService es;
    private Future future;
    private boolean isAliving;
    private boolean isDetect = true;

    private FaceDetectCallBack mFaceDetectCallBack;

    public FaceDetectCallBack getFaceDetectCallBack() {
        return mFaceDetectCallBack;
    }

    public void setFaceDetectCallBack(FaceDetectCallBack faceDetectCallBack) {
        mFaceDetectCallBack = faceDetectCallBack;
    }

    public boolean isAliving() {
        return isAliving;
    }

    public void setAliving(boolean aliving) {
        isAliving = aliving;
    }

    public boolean isDetect() {
        return isDetect;
    }

    public void setDetect(boolean detect) {
        isDetect = detect;
    }

    public FaceTrackManager() {
        es = Executors.newSingleThreadExecutor();
    }

    public static FaceTrackManager getInstance() {
        if (instance == null) {
            synchronized (FaceTrackManager.class) {
                if (instance == null) {
                    instance = new FaceTrackManager();
                }
            }
        }
        return instance;
    }

    public void faceTrack(final int[] argb, final int width, final int height,
                          final FaceDetectCallBack faceDetectCallBack) {

        if (future != null && !future.isDone()) {
            return;
        }
        future = es.submit(new Runnable() {
            @Override
            public void run() {
                faceDataDetect(argb, width, height, faceDetectCallBack);
            }
        });
    }

    /**
     * 人脸检测
     *
     * @param argb
     * @param width
     * @param height
     * @param faceDetectCallBack
     */
    private void faceDataDetect(final int[] argb, int width, int height, FaceDetectCallBack faceDetectCallBack) {
        LivenessModel livenessModel = new LivenessModel();
        long startTime = System.currentTimeMillis();
        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetector().trackMaxFace(argb, width, height);
        livenessModel.setRgbDetectDuration(System.currentTimeMillis() - startTime);
        livenessModel.getImageFrame().setArgb(argb);
        livenessModel.getImageFrame().setWidth(width);
        livenessModel.getImageFrame().setHeight(height);
        if (faceInfos != null && faceInfos.length > 0) {
            livenessModel.setTrackFaceInfo(faceInfos);
            FaceInfo faceInfo = faceInfos[0];
            livenessModel.setFaceInfo(faceInfo);
            livenessModel.setLandmarks(faceInfo.landmarks);
            // 塞选人脸，可以调节距离、角度
//            if (!filter(faceInfo, width, height, faceDetectCallBack)) {
//                faceDetectCallBack.onCallback(null);
//            }
            if (isAliving) {
                startTime = System.currentTimeMillis();
                float rgbScore = FaceSDKManager.getInstance().getFaceLiveness().rgbLiveness(argb,
                        width, height, faceInfo.landmarks);
                livenessModel.setRgbLivenessScore(rgbScore);
                livenessModel.setRgbLivenessDuration(System.currentTimeMillis() - startTime);
            }
            if (faceDetectCallBack != null) {
                faceDetectCallBack.onFaceDetectCallback(livenessModel);
                faceDetectCallBack.onTip(0, "人脸检测中");
            }
        } else {
            if (faceDetectCallBack != null) {
                faceDetectCallBack.onFaceDetectCallback(null);
                faceDetectCallBack.onTip(0, "未检测到人脸");
            }
        }
    }

    private boolean filter(FaceInfo faceInfo, int bitMapWidth, int bitMapHeight,
                           FaceDetectCallBack faceDetectCallBack) {

        if (faceInfo.mConf < 0.6) {
            faceDetectCallBack.onTip(0, "人脸置信度太低");
            // clearInfo();
            return false;
        }

        float[] headPose = faceInfo.headPose;
        // Log.i("wtf", "headpose->" + headPose[0] + " " + headPose[1] + " " + headPose[2]);
        if (Math.abs(headPose[0]) > 15 || Math.abs(headPose[1]) > 15 || Math.abs(headPose[2]) > 15) {
            faceDetectCallBack.onTip(0, "人脸置角度太大，请正对屏幕");
            return false;
        }

        // 判断人脸大小，若人脸超过屏幕二分一，则提示文案“人脸离手机太近，请调整与手机的距离”；
        // 若人脸小于屏幕三分一，则提示“人脸离手机太远，请调整与手机的距离”
        float ratio = (float) faceInfo.mWidth / (float) bitMapHeight;
        // Log.i("liveness_ratio", "ratio=" + ratio);
        if (ratio > 0.6) {
            faceDetectCallBack.onTip(0, "人脸离屏幕太近，请调整与屏幕的距离");
            // clearInfo();
            return false;
        } else if (ratio < 0.2) {
            faceDetectCallBack.onTip(0, "人脸离屏幕太远，请调整与屏幕的距离");
            // clearInfo();
            return false;
        } else if (faceInfo.mCenter_x > bitMapWidth * 3 / 4) {
            faceDetectCallBack.onTip(0, "人脸在屏幕中太靠右");
            // clearInfo();
            return false;
        } else if (faceInfo.mCenter_x < bitMapWidth / 4) {
            faceDetectCallBack.onTip(0, "人脸在屏幕中太靠左");
            // clearInfo();
            return false;
        } else if (faceInfo.mCenter_y > bitMapHeight * 3 / 4) {
            faceDetectCallBack.onTip(0, "人脸在屏幕中太靠下");
            // clearInfo();
            return false;
        } else if (faceInfo.mCenter_x < bitMapHeight / 4) {
            faceDetectCallBack.onTip(0, "人脸在屏幕中太靠上");
            // clearInfo();
            return false;
        }

        return true;
    }
}
