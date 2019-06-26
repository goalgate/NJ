package com.baidu.aip.face.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import com.baidu.aip.callback.CameraDataCallback;
import com.baidu.aip.face.ArgbPool;
import com.baidu.aip.face.AutoTexturePreviewView;
import com.baidu.aip.manager.FaceSDKManager;

import java.io.IOException;
import java.util.List;

/**
 * Time: 2019/1/24
 * Author: v_chaixiaogang
 * Description:
 */
public class Camera1PreviewManager implements TextureView.SurfaceTextureListener {

    private Context mContext;
    AutoTexturePreviewView mTextureView;
    // 预览尺寸
    private Size previewSize;
    boolean mPreviewed = false;
    private boolean mSurfaceCreated = false;
    private SurfaceTexture mSurfaceTexture;

    public static final int CAMERA_FACING_BACK = 0;

    public static final int CAMERA_FACING_FRONT = 1;

    public static final int CAMERA_USB = 2;

    private ArgbPool argbPool = new ArgbPool();

    /**
     * 垂直方向
     */
    public static final int ORIENTATION_PORTRAIT = 0;
    /**
     * 水平方向
     */
    public static final int ORIENTATION_HORIZONTAL = 1;

    /**
     * 当前相机的ID。
     */
    private int cameraFacing = CAMERA_FACING_FRONT;

    private int previewWidth;
    private int previewHeight;

    private int videoWidth;
    private int videoHeight;

    private int tempWidth;
    private int tempHeight;

    private int textureWidth;
    private int textureHeight;

    private Camera mCamera;
    private int mCameraNum;

    private int displayOrientation = 0;
    private int cameraId = 0;
    private int mirror = 1; // 镜像处理
    private CameraDataCallback mCameraDataCallback;
    private static volatile Camera1PreviewManager instance = null;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
//        ORIENTATIONS.append(Surface.ROTATION_0, 90);
//        ORIENTATIONS.append(Surface.ROTATION_90, 0);
//        ORIENTATIONS.append(Surface.ROTATION_180, 270);
//        ORIENTATIONS.append(Surface.ROTATION_270, 180);

        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    public static Camera1PreviewManager getInstance() {
        if (instance == null) {
            synchronized (Camera1PreviewManager.class) {
                if (instance == null) {
                    instance = new Camera1PreviewManager();
                }
            }
        }
        return instance;
    }

    public int getCameraFacing() {
        return cameraFacing;
    }

    public void setCameraFacing(int cameraFacing) {
        this.cameraFacing = cameraFacing;
    }

    public int getDisplayOrientation() {
        return displayOrientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        this.displayOrientation = displayOrientation;
    }

    /**
     * 开启预览
     *
     * @param context
     * @param textureView
     */
    public void startPreview(Context context, AutoTexturePreviewView textureView, int width,
                             int height, CameraDataCallback cameraDataCallback) {
        Log.e("chaixiaogang", "开启预览模式");
        this.mContext = context;
        this.mCameraDataCallback = cameraDataCallback;
        mTextureView = textureView;
        this.previewWidth = width;
        this.previewHeight = height;
        mSurfaceTexture = mTextureView.getTextureView().getSurfaceTexture();
        mTextureView.getTextureView().setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int i, int i1) {
        Log.e("chaixiaogang", "--surfaceTexture--SurfaceTextureAvailable");
        mSurfaceTexture = texture;
        mSurfaceCreated = true;
        if (mSurfaceCreated) {
            textureWidth = i;
            textureHeight = i1;
            openCamera();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int i, int i1) {
        Log.e("chaixiaogang", "--surfaceTexture--TextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
        Log.e("chaixiaogang", "--surfaceTexture--destroyed");
        mSurfaceCreated = false;
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        // Log.e("chaixiaogang", "--surfaceTexture--Updated");
    }


    /**
     * 关闭预览
     */
    public void stopPreview() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(null);
                mSurfaceCreated = false;
                mTextureView = null;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 开启摄像头
     */

    private void openCamera() {
        try {
            if (mCamera == null) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == cameraFacing) {
                        cameraId = i;
                    }
                }
                mCamera = Camera.open(cameraId);
                Log.e("chaixiaogang", "initCamera---open camera");
            }

            int detectRotation = 0; // 人脸实际检测角度
            int cameraRotation = 0; // 摄像头图像预览角度
            if (cameraFacing == CAMERA_FACING_FRONT) {
                cameraRotation = ORIENTATIONS.get(displayOrientation);
                cameraRotation = getCameraDisplayOrientation(cameraRotation, cameraId, mCamera);
                mCamera.setDisplayOrientation(cameraRotation);
                detectRotation = cameraRotation;
                if (detectRotation == 90 || detectRotation == 270) {
                    detectRotation = (detectRotation + 180) % 360;
                }
            } else if (cameraFacing == CAMERA_FACING_BACK) {
                cameraRotation = ORIENTATIONS.get(displayOrientation);
                cameraRotation = getCameraDisplayOrientation(cameraRotation, cameraId, mCamera);
                mCamera.setDisplayOrientation(cameraRotation);
                detectRotation = cameraRotation;
            } else if (cameraFacing == CAMERA_USB) {
                mCamera.setDisplayOrientation(0);
                detectRotation = 0;
            }
            if (cameraRotation == 90 || cameraRotation == 270) {
                // 旋转90度或者270，需要调整宽高
                mTextureView.setPreviewSize(previewHeight, previewWidth);
            } else {
                mTextureView.setRotationY(180); // TextureView旋转90度
                mTextureView.setPreviewSize(previewWidth, previewHeight);
            }
            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> sizeList = params.getSupportedPreviewSizes(); // 获取所有支持的camera尺寸
            final Camera.Size optionSize = getOptimalPreviewSize(sizeList, previewWidth,
                    previewHeight); // 获取一个最为适配的camera.size
            if (optionSize.width == previewWidth && optionSize.height == previewHeight) {
                videoWidth = previewWidth;
                videoHeight = previewHeight;
            } else {
                videoWidth = optionSize.width;
                videoHeight = optionSize.height;
            }
            tempWidth = videoWidth;
            tempHeight = videoHeight;
            params.setPreviewSize(videoWidth, videoHeight);
            mCamera.setParameters(params);
            try {
                mCamera.setPreviewTexture(mSurfaceTexture);
                mCamera.startPreview();
                final int finalDetectRotation = detectRotation;
                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {
                        int[] argb = argbPool.acquire(videoWidth, videoHeight);
                        if (argb == null || argb.length != videoWidth * videoHeight) {
                            argb = new int[videoWidth * videoHeight];
                        }
                        // 人脸检测的角度旋转了90或270度。高宽需要替换
                        if (finalDetectRotation % 180 == 90) {
                            if (videoWidth != tempHeight && videoHeight != tempWidth) {
                                int temp = videoWidth;
                                videoWidth = videoHeight;
                                videoHeight = temp;
                            }
                            FaceSDKManager.getInstance().getFaceDetector().yuvToARGB(bytes, videoHeight,
                                    videoWidth, argb, finalDetectRotation, 1);
                        } else {
                            FaceSDKManager.getInstance().getFaceDetector().yuvToARGB(bytes, videoWidth,
                                    videoHeight, argb, finalDetectRotation, 1);
                        }
                        if (mCameraDataCallback != null) {
                            mCameraDataCallback.onGetCameraData(argb, camera,
                                    videoWidth, videoHeight);
                        }
                        argbPool.release(argb);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("chaixiaogang", e.getMessage());
            }
        } catch (RuntimeException e) {
            Log.e("chaixiaogang", e.getMessage());
        }
    }


    private int getCameraDisplayOrientation(int degrees, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation + degrees) % 360;
            rotation = (360 - rotation) % 360; // compensate the mirror
        } else { // back-facing
            rotation = (info.orientation - degrees + 360) % 360;
        }
        return rotation;
    }


    /**
     * 解决预览变形问题
     *
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double aspectTolerance = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > aspectTolerance) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}
