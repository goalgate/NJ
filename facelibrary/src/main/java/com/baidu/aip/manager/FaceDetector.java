/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.manager;


import android.content.Context;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.callback.FaceCallback;
import com.baidu.idl.facesdk.FaceDetect;
import com.baidu.idl.facesdk.callback.Callback;
import com.baidu.idl.facesdk.model.FaceInfo;
import com.baidu.idl.facesdk.model.FaceVerifyData;

/**
 * @Time: 2019/1/15
 * @Author: v_chaixiaogang
 * @Description: 人脸检测API
 */
public class FaceDetector {
    private Context context;
    private FaceDetect mFaceDetect;

    public void initModel(Context context, String visModel, String nirModel, String alignModel, final FaceCallback faceCallback) {
        if (mFaceDetect == null) {
            mFaceDetect = new FaceDetect();
            mFaceDetect.initModel(context, visModel, nirModel, alignModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        } else {
            mFaceDetect.initModel(context, visModel, nirModel, alignModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        }
    }

    public void initQuality(final Context context, final String blurModel, final String occlurModel, final FaceCallback faceCallback) {
        if (mFaceDetect == null) {
            mFaceDetect = new FaceDetect();
            mFaceDetect.initQuality(context, blurModel, occlurModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        } else {
            mFaceDetect.initQuality(context, blurModel, occlurModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        }
    }

    /**
     * 单独图片质量检测方法（多人脸track 不做质量检测，可以通过该方法质量检测）
     *
     * @param imageData
     * @param height
     * @param width
     * @param landmark
     * @param bluriness
     * @param illum
     * @param occlusion
     * @param nOccluPart
     * @return
     */
    public int imgQuality(int[] imageData, int height, int width, int[] landmark,
                          float[] bluriness, int[] illum, float[] occlusion, int[] nOccluPart) {
        if (mFaceDetect == null) {
            mFaceDetect = new FaceDetect();
            return mFaceDetect.imgQuality(imageData, height, width, landmark, bluriness, illum, occlusion, nOccluPart);
        } else {
            return mFaceDetect.imgQuality(imageData, height, width, landmark, bluriness, illum, occlusion, nOccluPart);
        }
    }

    /**
     * 图片缩放
     *
     * @param srcImageData 原始图片像素点
     * @param srcHeight    原始图片高
     * @param srcWidth     原始图片宽
     * @param dstData      Resize 之后图片像素点，大小dstHeight*dstWidth
     * @param dstHeight    Resize 之后图片高
     * @param dstWidth     Resize 之后图片宽
     * @param imageType    图片类型，目前ARGB
     * @return
     */
    public int imgResize(int[] srcImageData, int srcHeight, int srcWidth,
                         int[] dstData, int dstHeight, int dstWidth, int imageType) {
        if (mFaceDetect == null) {
            mFaceDetect = new FaceDetect();
            return mFaceDetect.imgResize(srcImageData, srcHeight, srcWidth, dstData, dstHeight, dstWidth, imageType);
        } else {
            return mFaceDetect.imgResize(srcImageData, srcHeight, srcWidth, dstData, dstHeight, dstWidth, imageType);
        }
    }

    /**
     * 检测方法类型设置
     *
     * @param detectMethodType
     */
    public void setDetectMethodType(FaceDetect.DetectType detectMethodType) {
        if (mFaceDetect == null) {
            mFaceDetect = new FaceDetect();
            mFaceDetect.setDetectMethodType(detectMethodType);
        } else {
            mFaceDetect.setDetectMethodType(detectMethodType);
        }
    }

    /**
     * 设置配置参数
     *
     * @param faceEnvironment
     */
    public void loadConfig(FaceEnvironment faceEnvironment) {
        if (mFaceDetect == null) {
            mFaceDetect = new FaceDetect();
            mFaceDetect.loadConfig(faceEnvironment.getConfig());
        } else {
            mFaceDetect.loadConfig(faceEnvironment.getConfig());
        }
    }

    /**
     * 人脸图片追踪最大检测的人脸
     *
     * @param argb   人脸argb_8888图片。
     * @param width  图片宽度
     * @param height 图片高度
     * @return 检测结果代码。
     */
    public FaceInfo[] trackMaxFace(int[] argb, int width, int height) {
        int minDetectFace = FaceSDKManager.getInstance().getFaceEnvironmentConfig().getMinFaceSize();
        if (width < minDetectFace || height < minDetectFace) {
            return null;
        }
        if (mFaceDetect == null) {
            mFaceDetect = new FaceDetect();
            return mFaceDetect.trackMaxFace(argb, height, width);
        } else {
            return mFaceDetect.trackMaxFace(argb, height, width);
        }
    }

    /**
     * 人脸图片追踪最大检测的人脸
     *
     * @param imageFrame 人脸图片帧
     * @return 检测结果代码。
     */
    public FaceInfo[] trackMaxFace(ImageFrame imageFrame) {
        return trackMaxFace(imageFrame.getArgb(), imageFrame.getWidth(), imageFrame.getHeight());
    }

    /**
     * 人脸图片landmark 获取
     *
     * @param imageData
     * @param height
     * @param width
     * @param faceInfo
     * @return
     */
    public FaceInfo align(int[] imageData, int height, int width, FaceInfo faceInfo) {
        if (mFaceDetect == null) {
            mFaceDetect = new FaceDetect();
            return mFaceDetect.align(imageData, height, width, faceInfo);
        } else {
            return mFaceDetect.align(imageData, height, width, faceInfo);
        }
    }

    /**
     * 人脸图片检测所有人脸框
     *
     * @param argb        人脸argb_8888图片。
     * @param width       图片宽度
     * @param height      图片高度
     * @param minFaceSize 最小人脸大小
     * @return 检测结果代码。
     */
    public FaceInfo[] detect(int[] argb, int width, int height, int minFaceSize) {
        if (width < minFaceSize || height < minFaceSize) {
            return null;
        }
        if (mFaceDetect == null) {
            mFaceDetect = new FaceDetect();
            return mFaceDetect.detect(argb, height, width, minFaceSize);
        } else {
            return mFaceDetect.detect(argb, height, width, minFaceSize);
        }
    }

    /**
     * 人脸图片检测所有人脸框
     *
     * @param imageFrame  人脸图片帧
     * @param minFaceSize 最小人脸大小
     * @return 检测结果代码。
     */
    public FaceInfo[] detect(ImageFrame imageFrame, int minFaceSize) {
        return detect(imageFrame.getArgb(), imageFrame.getWidth(), imageFrame.getHeight(), minFaceSize);
    }


    /**
     * 人脸图片追踪第一个检测的人脸
     *
     * @param imageData
     * @param height
     * @param width
     * @return
     */
    public FaceInfo[] trackFirstFace(int[] imageData, int height, int width) {
        if (mFaceDetect == null) {
            mFaceDetect = new FaceDetect();
            return mFaceDetect.trackFirstFace(imageData, height, width);
        } else {
            return mFaceDetect.trackFirstFace(imageData, height, width);
        }
    }

    /**
     * 人脸图片追踪第一个检测的人脸
     *
     * @param imageFrame 人脸图片帧
     * @return 检测结果代码。
     */
    public FaceInfo[] trackFirstFace(ImageFrame imageFrame) {
        if (mFaceDetect != null) {
            return trackFirstFace(imageFrame.getArgb(), imageFrame.getHeight(), imageFrame.getWidth());
        }
        return null;
    }

    /**
     * 人脸图片追踪检测所有人脸
     *
     * @param imageData
     * @param height
     * @param width
     * @param num
     * @return
     */
    public FaceInfo[] track(int[] imageData, int height, int width, int num) {
        if (mFaceDetect == null) {
            mFaceDetect = new FaceDetect();
            return mFaceDetect.track(imageData, height, width, num);
        } else {
            return mFaceDetect.track(imageData, height, width, num);
        }
    }

    /**
     * 人脸图片追踪检测所有人脸
     *
     * @param imageFrame 人脸图片帧
     * @param num        人脸个数
     * @return 检测结果代码。
     */
    public FaceInfo[] track(ImageFrame imageFrame, int num) {
        return track(imageFrame.getArgb(), imageFrame.getHeight(), imageFrame.getWidth(), num);
    }

    /**
     * yuv图片转换为相应的argb;
     *
     * @param yuv      yuv_420p图片
     * @param width    图片宽度
     * @param height   图片高度
     * @param argb     接收argb用得 int数组
     * @param rotation yuv图片的旋转角度
     * @param mirror   是否为镜像
     */
    public int yuvToARGB(byte[] yuv, int width, int height, int[] argb, int rotation, int mirror) {
        if (mFaceDetect == null) {
            mFaceDetect = new FaceDetect();
            return mFaceDetect.getDataFromYUVimg(yuv, argb, width, height, rotation, mirror);
        } else {
            return mFaceDetect.getDataFromYUVimg(yuv, argb, width, height, rotation, mirror);
        }
    }

    /**
     * 人脸检测图片结果反馈
     *
     * @param trackID
     * @return
     */
    public FaceVerifyData[] getFaceVerifyData(int trackID) {
        if (mFaceDetect == null) {
            mFaceDetect = new FaceDetect();
            return mFaceDetect.getFaceVerifyData(trackID);
        } else {
            return mFaceDetect.getFaceVerifyData(trackID);
        }
    }


    /**
     * 重置跟踪人脸。下次将重新开始跟踪。
     */
    public void clearTrackedFaces() {
        if (mFaceDetect == null) {
            mFaceDetect = new FaceDetect();
            mFaceDetect.clearTrackedFaces();
        } else {
            mFaceDetect.clearTrackedFaces();
        }
    }
}
