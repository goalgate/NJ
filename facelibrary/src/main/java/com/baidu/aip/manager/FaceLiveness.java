/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.manager;


import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.baidu.aip.callback.FaceCallback;
import com.baidu.aip.callback.ILivenessCallBack;
import com.baidu.aip.entity.LivenessModel;
import com.baidu.aip.utils.GlobalSet;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.FaceLive;
import com.baidu.idl.facesdk.callback.Callback;
import com.baidu.idl.facesdk.model.FaceInfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

/**
 * @Time: 2019/1/15
 * @Author: v_chaixiaogang
 * @Description: 活体检测API
 */
public class FaceLiveness {

    private static final String TAG = "FaceLiveness";
    public static final int MASK_RGB = 0X0001;
    public static final int MASK_IR = 0X0010;
    public static final int MASK_DEPTH = 0X0100;

    private FaceLive mFaceLive;
    private int cameraType;

    private Bitmap bitmap;
    private ILivenessCallBack livenessCallBack;

    private int[] nirRgbArray;

    private int[] mRgbArray;
    private volatile boolean isVisHavePixls = false;

    private byte[] mIrByte;
    private volatile boolean isIRHavePixls = false;

    private byte[] mDepthArray;
    private volatile boolean isDepthHavePixls;
    private ExecutorService es;
    private Future future;

    private ExecutorService es2;
    private Future future2;
    private boolean isLiveDetect = true;

    private long startLiveCheckTime;
    private long endLiveCheckTime;

//    public static FaceLiveness getInstance() {
//        return FaceLiveness.HolderClass.instance;
//    }
//
//    private static class HolderClass {
//        private static final FaceLiveness instance = new FaceLiveness();
//    }

    public FaceLiveness() {
        es = Executors.newSingleThreadExecutor();
        es2 = Executors.newSingleThreadExecutor();
    }

    public void initModel(Context context, String visModel, String nirModel, String depthModel, final FaceCallback faceCallback) {
        if (mFaceLive == null) {
            mFaceLive = new FaceLive();
            mFaceLive.initModel(context, visModel, nirModel, depthModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        } else {
            mFaceLive.initModel(context, visModel, nirModel, depthModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        }

    }

    public void setLivenessCallBack(ILivenessCallBack callBack) {
        this.livenessCallBack = callBack;
    }

    /**
     * 设置可见光图
     *
     * @param bitmap
     */
    public void setRgbBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            if (mRgbArray == null) {
                mRgbArray = new int[bitmap.getWidth() * bitmap.getHeight()];
            }
            bitmap.getPixels(mRgbArray, 0, bitmap.getWidth(), 0, 0,
                    bitmap.getWidth(), bitmap.getHeight());
            this.bitmap = bitmap;
            isVisHavePixls = true;
        }
    }

    public void setNirRgbInt(int[] nirRgbData) {
        if (nirRgbData == null) {
            return;
        }
        if (nirRgbArray == null) {
            nirRgbArray = new int[nirRgbData.length];
        }
        try {
            System.arraycopy(nirRgbData, 0, nirRgbArray, 0, nirRgbData.length);
            isVisHavePixls = true;
        } catch (NullPointerException e) {
            Log.e(TAG, String.valueOf(e.getStackTrace()));
        }
    }

    public void setRgbInt(int[] argbData) {

        if (argbData == null) {
            return;
        }
        if (mRgbArray == null) {
            mRgbArray = new int[argbData.length];
        }

        try {
            System.arraycopy(argbData, 0, mRgbArray, 0, argbData.length);
            isVisHavePixls = true;
        } catch (NullPointerException e) {
            Log.e(TAG, String.valueOf(e.getStackTrace()));
        }

    }

    private int[] byte2int(byte[] b) {
        // 数组长度对4余数
        int r;
        byte[] copy;
        if ((r = b.length % 4) != 0) {
            copy = new byte[b.length - r + 4];
            System.arraycopy(b, 0, copy, 0, b.length);
        } else {
            copy = b;
        }

        int[] x = new int[copy.length / 4 + 1];
        int pos = 0;
        for (int i = 0; i < x.length - 1; i++) {
            x[i] = (copy[pos] << 24 & 0xff000000) | (copy[pos + 1] << 16 & 0xff0000)
                    | (copy[pos + 2] << 8 & 0xff00) | (copy[pos + 3] & 0xff);
            pos += 4;
        }
        x[x.length - 1] = r;
        return x;
    }

    /**
     * 设置深度图
     *
     * @param irData
     */
    public void setIrData(byte[] irData) {

        if (irData == null) {
            return;
        }
        if (mIrByte == null) {
            mIrByte = new byte[irData.length];
        }

        try {
            System.arraycopy(irData, 0, mIrByte, 0, irData.length);
            isIRHavePixls = true;
        } catch (NullPointerException e) {
            Log.e(TAG, String.valueOf(e.getStackTrace()));
        }

    }


    /**
     * 设置深度图
     *
     * @param depthData
     */
    public void setDepthData(byte[] depthData) {

        if (mDepthArray == null) {
            mDepthArray = new byte[depthData.length];
        }

        try {
            System.arraycopy(depthData, 0, mDepthArray, 0, depthData.length);
            isDepthHavePixls = true;
        } catch (NullPointerException e) {
            Log.e(TAG, String.valueOf(e.getStackTrace()));
        }

    }


    public void clearInfo() {
        try {
            isDepthHavePixls = false;
            isIRHavePixls = false;
            isVisHavePixls = false;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float rgbLiveness(int[] data, int width, int height, int[] landmarks) {
        final float rgbScore = mFaceLive.silentLive(FaceLive.LiveType.LIVEID_VIS, data, height, width, landmarks);
        return rgbScore;
    }

    public float irLiveness(byte[] data, int width, int height, int[] landmarks) {
        final float irScore = mFaceLive.silentLive(FaceLive.LiveType.LIVEID_NIR, data, height, width, landmarks);
        return irScore;
    }

    public float depthLiveness(byte[] data, int width, int height, int[] landmarks) {
        final float depthScore = mFaceLive.silentLive(FaceLive.LiveType.LIVEID_DEPTH, data, height, width, landmarks);
        return depthScore;
    }

    public void livenessCheck(final int width, final int height, final int type) {
        if (future != null && !future.isDone()) {
            return;
        }
        future = es.submit(new Runnable() {
            @Override
            public void run() {
                // Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                onLivenessCheck(width, height, type);
            }
        });
//         onLivenessCheck(width, height, type);
    }

    public void livenessCheck(final int width, final int height, final int type, final int[] rgb,
                              final byte[] ir, final byte[] depth) {
        if (future != null && !future.isDone()) {
            return;
        }

        setDepthData(depth);
        setRgbInt(rgb);
        setIrData(ir);

        future = es.submit(new Runnable() {
            @Override
            public void run() {
                onLivenessCheck(width, height, type);
            }
        });
    }


    // 活体检测
    private boolean onLivenessCheck(int width, int height, int type) {
        boolean isLiveness = false;
        // 判断当前是否有人脸

        // Log.e(TAG, "开始检测");
        long startTime = System.currentTimeMillis();
        FaceInfo[] faceInfos = null;
        if (mRgbArray != null) {
            faceInfos = FaceSDKManager.getInstance().getFaceDetector().trackMaxFace(mRgbArray, width, height);
        }
        LivenessModel livenessModel = new LivenessModel();
        livenessModel.setRgbDetectDuration(System.currentTimeMillis() - startTime);
        livenessModel.getImageFrame().setArgb(mRgbArray);
        livenessModel.getImageFrame().setWidth(width);
        livenessModel.getImageFrame().setHeight(height);
        livenessModel.setLiveType(type);

        if (faceInfos != null && faceInfos.length > 0) {
            livenessModel.setTrackFaceInfo(faceInfos);
            FaceInfo faceInfo = faceInfos[0];
            livenessModel.setFaceInfo(faceInfo);
            livenessModel.setLandmarks(faceInfo.landmarks);
            // 塞选人脸，可以调节距离、角度
//                if (!filter(faceInfo, width, height)) {
//                    livenessCallBack.onCallback(null);
//                    return isLiveness;
//                }
            if (livenessCallBack != null) {
                livenessCallBack.onCanvasRectCallback(livenessModel);
                livingCheck(width, height, type, faceInfos, livenessModel);
            }
        } else {
            if (livenessCallBack != null) {
                livenessCallBack.onCanvasRectCallback(null);
                livenessCallBack.onCallback(null);
                livenessCallBack.onTip(0, "未检测到人脸");
            }
        }
        return isLiveness;
    }

    private void livingCheck(final int width, final int height, final int type, final FaceInfo[] faceInfos,
                             final LivenessModel livenessModel) {
        if (future2 != null && !future2.isDone()) {
            return;
        }
        future2 = es2.submit(new Runnable() {
            @Override
            public void run() {
                long startTime;
                if (livenessCallBack != null) {
                    livenessCallBack.onTip(0, "活体判断中");
                }

                float rgbScore = 0;
                if ((type & MASK_RGB) == MASK_RGB) {
                    startTime = System.currentTimeMillis();
                    rgbScore = rgbLiveness(mRgbArray, width, height, faceInfos[0].landmarks);
                    livenessModel.setRgbLivenessScore(rgbScore);
                    livenessModel.setRgbLivenessDuration(System.currentTimeMillis() - startTime);
                }
                float irScore = 0;
                if ((type & MASK_IR) == MASK_IR) {
                    startTime = System.currentTimeMillis();
                    irScore = irLiveness(mIrByte, width, height, faceInfos[0].landmarks);
                    //  Log.e("gangzi", "mIrByte长度--" + mIrByte.length + "width--" + width + "height--" + height);
                    livenessModel.setIrLivenessDuration(System.currentTimeMillis() - startTime);
                    livenessModel.setIrLivenessScore(irScore);
                }
                float depthScore = 0;
                if ((type & MASK_DEPTH) == MASK_DEPTH) {
                    startTime = System.currentTimeMillis();
                    int cameraType = PreferencesUtil.getInt(GlobalSet.TYPE_CAMERA, GlobalSet.ORBBEC);
                    if (cameraType == GlobalSet.ORBBECPRODABAI || cameraType == GlobalSet.ORBBECPRODEEYEA
                            || cameraType == GlobalSet.ORBBECATLAS) {
                        depthScore = depthLiveness(mDepthArray, 640, 400, faceInfos[0].landmarks);
                    } else {
                        depthScore = depthLiveness(mDepthArray, width, height, faceInfos[0].landmarks);
                    }
                    livenessModel.setDetphtLivenessDuration(System.currentTimeMillis() - startTime);
                    livenessModel.setDepthLivenessScore(depthScore);
                }
                if (livenessCallBack != null) {
                    livenessCallBack.onCallback(livenessModel);
                }
            }
        });
    }

    private boolean filter(FaceInfo faceInfo, int bitMapWidth, int bitMapHeight) {

        if (faceInfo.mConf < 0.6) {
            livenessCallBack.onTip(0, "人脸置信度太低");
            // clearInfo();
            return false;
        }

        float[] headPose = faceInfo.headPose;
        // Log.i("wtf", "headpose->" + headPose[0] + " " + headPose[1] + " " + headPose[2]);
        if (Math.abs(headPose[0]) > 15 || Math.abs(headPose[1]) > 15 || Math.abs(headPose[2]) > 15) {
            livenessCallBack.onTip(0, "人脸置角度太大，请正对屏幕");
            return false;
        }

        // 判断人脸大小，若人脸超过屏幕二分一，则提示文案“人脸离手机太近，请调整与手机的距离”；
        // 若人脸小于屏幕三分一，则提示“人脸离手机太远，请调整与手机的距离”
        float ratio = (float) faceInfo.mWidth / (float) bitMapHeight;
        // Log.i("liveness_ratio", "ratio=" + ratio);
        if (ratio > 0.6) {
            livenessCallBack.onTip(0, "人脸离屏幕太近，请调整与屏幕的距离");
            // clearInfo();
            return false;
        } else if (ratio < 0.2) {
            livenessCallBack.onTip(0, "人脸离屏幕太远，请调整与屏幕的距离");
            // clearInfo();
            return false;
        } else if (faceInfo.mCenter_x > bitMapWidth * 3 / 4) {
            livenessCallBack.onTip(0, "人脸在屏幕中太靠右");
            clearInfo();
            return false;
        } else if (faceInfo.mCenter_x < bitMapWidth / 4) {
            livenessCallBack.onTip(0, "人脸在屏幕中太靠左");
            // clearInfo();
            return false;
        } else if (faceInfo.mCenter_y > bitMapHeight * 3 / 4) {
            livenessCallBack.onTip(0, "人脸在屏幕中太靠下");
            // clearInfo();
            return false;
        } else if (faceInfo.mCenter_x < bitMapHeight / 4) {
            livenessCallBack.onTip(0, "人脸在屏幕中太靠上");
            // clearInfo();
            return false;
        }

        return true;
    }


    private long lasttime;
    private int unDetectedFaceCount = 0;

    public void release() {
        if (future != null) {
            future.cancel(true);
        }

    }

    private boolean saveRgbImage(String prefix, float score, int[] rgb, int width, int height) {
        boolean success = false;
        if (rgb == null) {
            return success;
        }
        if (!Environment.isExternalStorageEmulated()) {
            return success;
        }
        File sdCard = Environment.getExternalStorageDirectory();
        String uuid = UUID.randomUUID().toString();
        File dir = new File(sdCard.getAbsolutePath() + "/rgb_ir_depth/" + uuid);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String rgbFilename = String.format("%s_%s_%s.jpg", prefix, "rgb", score);
        File rgbFile = new File(dir, rgbFilename);
        if (rgbFile.exists()) {
            rgbFile.delete();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(rgbFile);
            final Bitmap bitmap = Bitmap.createBitmap(rgb, width, height, Bitmap.Config.ARGB_8888);
            Log.i(TAG, "strFileName 1= " + rgbFile.getPath());
            if (null != fos) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                success = true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    public boolean saveFile(String prefix, String type, float score, byte[] data) {
        boolean success = false;
        if (data == null) {
            return success;
        }
        if (!Environment.isExternalStorageEmulated()) {
            return success;
        }
        File sdCard = Environment.getExternalStorageDirectory();
        String uuid = UUID.randomUUID().toString();
        File dir = new File(sdCard.getAbsolutePath() + "/rgb_ir_depth/" + uuid);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (data != null) {
            String nirFilename = String.format("%s_%s_%s", prefix, type, score);
            File nirFile = new File(dir, nirFilename);
            if (nirFile.exists()) {
                nirFile.delete();
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(nirFile);
                fos.write(data, 0, data.length);
                fos.flush();
                fos.close();
                success = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return success;
    }

    /**
     * 根据byte数组生成文件
     *
     * @param bytes      生成文件用到的byte数组
     * @param depthScore
     */
    private void saveDepthByte(byte[] bytes, long time, float depthScore) {
        // TODO Auto-generated method stub
        /**
         * 创建File对象，其中包含文件所在的目录以及文件的命名
         */
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdCardDir = null;
            sdCardDir = Environment.getExternalStorageDirectory() + "/TrackImage/";
            String fileName = null;
            fileName = time + "_nirData_" + depthScore;

            File dirFile = new File(sdCardDir);  // 目录转化成文件夹
            if (!dirFile.exists()) {                // 如果不存在，那就建立这个文件夹
                dirFile.mkdirs();
            }
            String groupDir = time + "";
            File groupFile = new File(dirFile, groupDir);
            if (!groupFile.exists()) {                // 如果不存在，那就建立这个文件夹
                groupFile.mkdirs();
            }
            String path = sdCardDir + groupDir;
            // 判断是否可以对SDcard进行操作
            File file = new File(path, fileName);
            // 创建FileOutputStream对象
            FileOutputStream outputStream = null;
            // 创建BufferedOutputStream对象
            BufferedOutputStream bufferedOutputStream = null;
            try {
                // 如果文件存在则删除
                if (file.exists()) {
                    file.delete();
                }
                // 在文件系统中根据路径创建一个新的空文件
                file.createNewFile();
                // 获取FileOutputStream对象
                outputStream = new FileOutputStream(file);
                // 获取BufferedOutputStream对象
                bufferedOutputStream = new BufferedOutputStream(outputStream);
                // 往文件所在的缓冲输出流中写byte数据
                bufferedOutputStream.write(bytes);
                // 刷出缓冲输出流，该步很关键，要是不执行flush()方法，那么文件的内容是空的。
                bufferedOutputStream.flush();
            } catch (Exception e) {
                // 打印异常信息
                e.printStackTrace();
            } finally {
                // 关闭创建的流对象
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bufferedOutputStream != null) {
                    try {
                        bufferedOutputStream.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
    }

    private void saveLandmarksData(int[] landmarks, long time, float score) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) { // 判断是否可以对SDcard进行操作
            // 获取SDCard指定目录下
            String sdCardDir = null;

            sdCardDir = Environment.getExternalStorageDirectory() + "/TrackImage/";

            File dirFile = new File(sdCardDir);  // 目录转化成文件夹
            if (!dirFile.exists()) {                // 如果不存在，那就建立这个文件夹
                dirFile.mkdirs();
            }
            String groupDir = time + "";
            File groupFile = new File(dirFile, groupDir);
            if (!groupFile.exists()) {                // 如果不存在，那就建立这个文件夹
                groupFile.mkdirs();
            }
            String path = sdCardDir + groupDir; // 文件夹有啦，就可以保存图片啦
            File file = null;

            file = new File(path, time + "_nirLandmarks_" + score); // 在SDcard的目录下创建图片文,以当前时间为其命名

            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(file));
                bw.write(aryToString(landmarks));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("写入成功");
        }

    }

    private static String aryToString(int[] ary) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ary.length; i++) {
            sb.append(ary[i]).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
