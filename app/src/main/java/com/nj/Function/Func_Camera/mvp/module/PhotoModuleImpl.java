package com.nj.Function.Func_Camera.mvp.module;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.blankj.utilcode.util.ScreenUtils;
import com.nj.AppInit;


import java.io.IOException;


/**
 * Created by zbsz on 2017/5/19.
 */

/*public class PhotoModuleImpl implements IPhotoModule {
    final static String ApplicationName = "MBKF_";
    static Camera camera;
    public boolean isPreview = false;
    Bitmap bm;

    private int realFaceNum = 0;
    IOnSetListener callback;
    private static final String PREFS_NAME = "UserInfo";

    @Override
    public void setDisplay(SurfaceHolder sHolder) {
        try {
            isPreview = true;
            camera.setPreviewDisplay(sHolder);
            camera.startPreview();
        } catch (IOException e) {
            ToastUtils.showLong("摄像头出错，请退出程序再打开");
        }
    }



    public void setHolderAndDisplay(final SurfaceHolder sHolder, final boolean display) {
        sHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (camera != null) {

                    Camera.Parameters parameters = camera.getParameters();
                    // 设置预览照片时每秒显示多少帧的最小值和最大值
                    parameters.setPreviewFpsRange(4, 10);
                    // 设置图片格式
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    // 设置JPG照片的质量
                    parameters.set("jpeg-quality", 85);
                    // 通过SurfaceView显示取景画面
                    isPreview = true;

                    if (display) {

                        setDisplay(sHolder);

                    }

                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // 如果camera不为null ,释放摄像头
                if (camera != null) {
                    if (isPreview) camera.stopPreview();
                    camera.release();
                    camera = null;
                    Log.e(ApplicationName, "摄像头被释放");
                    isPreview = false;
                }
            }

        });
    }

    @Override
    public void capture(IOnSetListener listener) {
        this.callback = listener;
        if (isPreview) {
            camera.takePicture(new Camera.ShutterCallback() {
                public void onShutter() {
                    // 按下快门瞬间会执行此处代码
                }
            }, new Camera.PictureCallback() {
                public void onPictureTaken(byte[] data, Camera c) {
                    // 此处代码可以决定是否需要保存原始照片信息
                }
            }, myJpegCallback);
        }
    }

    Camera.PictureCallback myJpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, final Camera camera) {
            isPreview = false;
            camera.stopPreview();

            bm = BitmapFactory.decodeByteArray(data, 0, data.length);
        *//*    File file = new File(Environment
                    .getExternalStorageDirectory(), ApplicationName+ new TimeString().getTimeString().toString() + ".jpg");
            FileOutputStream outStream = null;
            try
            {
                // 打开指定文件对应的输出流
                outStream = new FileOutputStream(file);
                // 把位图输出到指定文件中
                bm.compress(Bitmap.CompressFormat.JPEG, 100,
                        outStream);
                outStream.close();
                ToastUtils.showLong("保存成功");
            }
            catch (IOException e)
            {
                ToastUtils.showLong("保存失败");
            }*//*

            if (SPUtils.getInstance(PREFS_NAME).getBoolean("faceState")) {
                bm = bm.copy(Bitmap.Config.RGB_565, true);
                Observable.just(bm).flatMap(new Function<Bitmap, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull Bitmap bitmap) throws Exception {
                        FaceDetector faceDetector = new FaceDetector(bm.getWidth(), bm.getHeight(), 1);
                        FaceDetector.Face[] faces = new FaceDetector.Face[1];
                        realFaceNum = faceDetector.findFaces(bm, faces);
                        Log.e("人脸数", String.valueOf(realFaceNum));
                        if (realFaceNum == 1) {
                            return Observable.just("拍照成功");
                        } else {
                            return Observable.just("没有检测到人脸,重新拍照");
                        }
                    }
                }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(@NonNull String s) throws Exception {
                                callback.onBtnText(s);
                                if (s.equals("拍照成功")) callback.onGetPhoto(bm);
                            }
                        });
            } else {
                callback.onBtnText("拍照成功");
                callback.onGetPhoto(bm);
            }

        }
    };

    @Override
    public void initCamera() {
        if (!isPreview) {
            // 此处默认打开后置摄像头。
            // 通过传入参数可以打开前置摄像头
            safeCameraOpen(0);
        }

    }

    private void safeCameraOpen(int id) {
        try {
            releaseCameraAndPreview();
            camera = Camera.open(id);
        } catch (Exception e) {
            ToastUtils.showLong("开启摄像头失败");
            e.printStackTrace();
        }

    }

    private void releaseCameraAndPreview() {
        if (camera != null) {
            camera.release();
            camera = null;
        }

    }
}*/

public class PhotoModuleImpl implements IPhotoModule {
    final static String ApplicationName = "PhotoModule_";
    static Camera camera;


    /*    public boolean isPreview = false;*/
    Bitmap bm;
    /*    private boolean hasDetected = false;
        private int realFaceNum = 0;*/
    IOnSetListener callback;

    @Override
    public void setDisplay(SurfaceHolder sHolder) {
        try {
/*            isPreview = true;*/
            if(camera!=null){
                camera.setPreviewDisplay(sHolder);
                camera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setParameter(SurfaceHolder sHolder) {
        sHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (camera != null) {
                    Camera.Parameters parameters = camera.getParameters();
                    // 设置预览照片时每秒显示多少帧的最小值和最大值

                    parameters.setPreviewSize(ScreenUtils.getScreenWidth(),ScreenUtils.getScreenHeight());

                    parameters.setPreviewFpsRange(45,50);
                    // 设置图片格式
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    // 设置JPG照片的质量
                    parameters.set("jpeg-quality", 85);
                    // 通过SurfaceView显示取景画面
                    setDisplay(holder);

                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // 如果camera不为null ,释放摄像头
         /*       if (camera != null) {
            *//*        if (isPreview) *//*camera.stopPreview();
                    camera.release();
                    camera = null;
                    Log.e(ApplicationName, "摄像头被释放");
*//*                    isPreview = false;*//*
                }*/
                releaseCameraAndPreview();
            }

        });
    }

    @Override
    public void capture(IOnSetListener listener) {
        this.callback = listener;
/*        hasDetected = false;*/
  /*      if (isPreview) {*/
        camera.takePicture(new Camera.ShutterCallback() {
            public void onShutter() {
                // 按下快门瞬间会执行此处代码
            }
        }, new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera c) {
                // 此处代码可以决定是否需要保存原始照片信息
            }
        }, myJpegCallback);
  /*      }*/
    }

    Camera.PictureCallback myJpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, final Camera camera) {
 /*           isPreview = false;*/
            camera.stopPreview();
            bm = BitmapFactory.decodeByteArray(data, 0, data.length);
        /*    File file = new File(Environment
                    .getExternalStorageDirectory(), ApplicationName+ new TimeString().getTimeString().toString() + ".jpg");
            FileOutputStream outStream = null;
            try
            {
                // 打开指定文件对应的输出流
                outStream = new FileOutputStream(file);
                // 把位图输出到指定文件中
                bm.compress(Bitmap.CompressFormat.JPEG, 100,
                        outStream);
                outStream.close();
                ToastUtils.showLong("保存成功");
            }
            catch (IOException e)
            {
                ToastUtils.showLong("保存失败");
            }*/
            callback.onBtnText("拍照成功");
            callback.onGetPhoto(bm);
          /*  if (SPUtils.getInstance(PREFS_NAME).getBoolean("faceState")) {
                bm = bm.copy(Bitmap.Config.RGB_565, true);
                Observable.just(bm).flatMap(new Function<Bitmap, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull Bitmap bitmap) throws Exception {
                        FaceDetector faceDetector = new FaceDetector(bm.getWidth(), bm.getHeight(), 1);
                        FaceDetector.Face[] faces = new FaceDetector.Face[1];
                        realFaceNum = faceDetector.findFaces(bm, faces);
                        Log.e("人脸数", String.valueOf(realFaceNum));
                        if (realFaceNum == 1) {
                            return Observable.just("拍照成功");
                        } else {
                            return Observable.just("没有检测到人脸,重新拍照");
                        }
                    }
                }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(@NonNull String s) throws Exception {
                                callback.onBtnText(s);
                                if (s.equals("拍照成功")) callback.onGetPhoto(bm);
                            }
                        });
            } else {
                callback.onBtnText("拍照成功");
                callback.onGetPhoto(bm);
            }*/

        }
    };

    @Override
    public void initCamera() {
     /*   if (!isPreview) {*/
        // 此处默认打开后置摄像头。
        // 通过传入参数可以打开前置摄像头
        safeCameraOpen(0);
      /*  }*/

    }

    private void safeCameraOpen(int id) {
        try {
            releaseCameraAndPreview();
            camera = Camera.open();

        } catch (Exception e) {
            Toast.makeText(AppInit.getContext(),"无法获取摄像头权限",Toast.LENGTH_LONG);
            e.printStackTrace();
        }

    }

    @Override
    public void closeCamera() {
        releaseCameraAndPreview();
    }

    private void releaseCameraAndPreview() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
            Log.e(ApplicationName, "摄像头被释放");
        }

    }
}

