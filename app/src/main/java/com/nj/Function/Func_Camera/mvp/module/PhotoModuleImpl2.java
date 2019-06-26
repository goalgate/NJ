package com.nj.Function.Func_Camera.mvp.module;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.nj.AppInit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PhotoModuleImpl2 implements IPhotoModule, Camera.PreviewCallback{

    final static String ApplicationName = "PhotoModule_";
    static Camera camera;
    Bitmap bm;
    IOnSetListener callback;

    @Override
    public void setDisplay(final SurfaceHolder sHolder) {
        try {
            /*            isPreview = true;*/
            if (camera != null) {
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
                    parameters.setPreviewFpsRange(45, 50);
                    // 设置图片格式
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    // 设置JPG照片的质量
                    parameters.set("jpeg-quality", 100);
                    camera.setPreviewCallback(PhotoModuleImpl2.this);
                    camera.setParameters(parameters);
                    // 通过SurfaceView显示取景画面
                    setDisplay(holder);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // 如果camera不为null ,释放摄像头
                if (camera != null) {
                    /*        if (isPreview) */
                    camera.setPreviewCallback(null);
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                    Log.e(ApplicationName, "摄像头被释放");
                    /*                    isPreview = false;*/
                }
            }

        });
    }

    @Override
    public void capture(IOnSetListener listener) {
        this.callback = listener;
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

    Camera.PictureCallback myJpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, final Camera camera) {
            camera.stopPreview();
            bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            callback.onBtnText("拍照成功");
            callback.onGetPhoto(bm);

        }
    };

    @Override
    public void initCamera() {
        safeCameraOpen(0);
    }

    private void safeCameraOpen(int id) {
        try {
            releaseCameraAndPreview();
            camera = Camera.open();
        } catch (Exception e) {
            Toast.makeText(AppInit.getContext(), "无法获取摄像头权限", Toast.LENGTH_LONG);
            e.printStackTrace();
        }

    }

    @Override
    public void closeCamera() {
        releaseCameraAndPreview();
    }


    private void releaseCameraAndPreview() {
        if (camera != null) {
            camera.release();
        }
    }
    @Override
    public void getOneShut(IOnSetListener listener) {
        this.callback = listener;
        mfaceTask = new PhotoModuleImpl2.FaceTask(global_bytes);
        mfaceTask.execute((Void)null);
    }

    public class FaceTask extends AsyncTask<Void, Void, Void> {

        private byte[] mData;
        //构造函数
        FaceTask(byte[] data){
            this.mData = data;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            Camera.Size size = camera.getParameters().getPreviewSize(); //获取预览大小
            final int w = size.width;  //宽度
            final int h = size.height;
            final YuvImage image = new YuvImage(mData, ImageFormat.NV21, w, h, null);
            ByteArrayOutputStream os = new ByteArrayOutputStream(mData.length);
            if(!image.compressToJpeg(new Rect(0, 0, w, h), 100, os)){
                return null;
            }
            byte[] tmp = os.toByteArray();
            Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0,tmp.length);
            bm = bmp;
            handler.sendEmptyMessage(0x12);
            return null;
        }
    }

    PhotoModuleImpl2.FaceTask mfaceTask;
    byte[] global_bytes;
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (null != mfaceTask) {
            switch (mfaceTask.getStatus()) {
                case RUNNING:
                    return;
                case PENDING:
                    mfaceTask.cancel(false);
                    break;
            }
        }
        global_bytes = data;
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x12:
                    callback.onGetPhoto(bm);
                    break;
            }
        }
    };
}
