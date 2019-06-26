package com.baidu.aip.face.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Time: 2019/1/24
 * Author: v_chaixiaogang
 * Description:
 */
public class Camera2PreviewManager implements TextureView.SurfaceTextureListener {

    private Context mContext;
    TextureView mTextureView;
    // 预览尺寸
    private Size previewSize;
    boolean mPreviewed = false;
    private boolean mSurfaceCreated = false;
    private SurfaceTexture mSurfaceTexture;
    /**
     * 一个信号量以防止应用程序在关闭相机之前退出。
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private int cameraFacingBack = 0;

    private int cameraFacingFront = 1;

    private int cameraUsb = 2;

    /**
     * 当前相机的ID。
     */
    private String mCameraId;
    /**
     * 系统摄像头
     */
    private CameraDevice mCameraDevice;

    private Integer mSensorOrientation;
    private Size mPreviewSize;
    private boolean mFlashSupported;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest mPreviewRequest;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private int previewWidth;
    private int previewHeight;


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    private void setCameraFront(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(mCameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    Log.i("chaixiaogang", "");
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开启预览
     *
     * @param context
     * @param textureView
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startPreview(Context context, TextureView textureView, int width, int height) {
        Log.e("chaixiaogang", "开启预览模式");
        this.mContext = context;
        mTextureView = textureView;
        this.previewWidth = width;
        this.previewHeight = height;
        mSurfaceTexture = mTextureView.getSurfaceTexture();
        mTextureView.setSurfaceTextureListener(this);
        startBackgroundThread();
    }

    /**
     * 设置与相机相关的成员变量。
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setUpCameraOutputs() {
        CameraManager manager = null;
        manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            // 获取可用摄像头列表
            for (String cameraId : manager.getCameraIdList()) {
                // 获取相机的相关参数
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                // 不使用前置摄像头。
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
//                    continue;
                    Log.i("chaixiaogang", "");
                }
                // 管理摄像头支持的所有输出格式和尺寸
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }
                mCameraId = cameraId;
                Log.e("chaixiaogang", " 相机可用 ");
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // 不支持Camera2API
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int i, int i1) {
        mSurfaceTexture = texture;
        mSurfaceCreated = true;
        if (mSurfaceCreated) {
            openCamera();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
        mSurfaceCreated = false;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {

    }


    /**
     * 关闭预览
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopPreview() {
        mCameraOpenCloseLock.release();
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }


    /**
     * 开启摄像头
     */
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera() {
        // 设置相机输出
        setUpCameraOutputs();
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            cameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对相机状态进行监听
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            // 创建CameraPreviewSession
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    /**
     * 为相机预览创建新的CameraCaptureSession
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createCameraPreviewSession() {
        try {
            Surface surface = new Surface(mSurfaceTexture);
            // 设置了一个具有输出Surface的CaptureRequest.Builder。
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            // 创建一个CameraCaptureSession来进行相机预览。
            mCameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // 相机已经关闭
                            if (null == mCameraDevice) {
                                return;
                            }
                            // 会话准备好后，我们开始显示预览
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // 自动对焦应
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // 开启相机预览并添加事件
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                // 发送请求
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        null, mBackgroundHandler);
                                Log.e("chaixiaogang", " 开启相机预览并添加事件");
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.e("chaixiaogang", " onConfigureFailed 开启预览失败");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            Log.e("chaixiaogang", " CameraAccessException 开启预览失败");
            e.printStackTrace();
        }
    }


    /**
     * 启动一个HandlerThread
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
}
