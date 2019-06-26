package com.nj.Function.Func_Face.mvp.Module;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.TextureView;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.api.FaceApi;
import com.baidu.aip.callback.CameraDataCallback;
import com.baidu.aip.callback.FaceDetectCallBack;
import com.baidu.aip.db.DBManager;
import com.baidu.aip.entity.ARGBImg;
import com.baidu.aip.entity.Feature;
import com.baidu.aip.entity.IdentifyRet;
import com.baidu.aip.entity.LivenessModel;
import com.baidu.aip.entity.User;
import com.baidu.aip.face.AutoTexturePreviewView;
import com.baidu.aip.face.FaceCropper;
import com.baidu.aip.face.FaceTrackManager;
import com.baidu.aip.face.camera.Camera1PreviewManager;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.FeatureUtils;
import com.baidu.aip.utils.FileUitls;
import com.baidu.aip.utils.ImageUtils;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.model.FaceInfo;
import com.blankj.utilcode.util.ToastUtils;
import com.drv.card.ICardInfo;
import com.nj.Function.Func_Face.mvp.presenter.FacePresenter;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class FaceImpl2 implements IFace {

    private final static double livnessScore = 0.2;

    static FacePresenter.FaceAction action = FacePresenter.FaceAction.No_ACTION;

    private Handler handler = new Handler(Looper.getMainLooper());

    public static final int FEATURE_DATAS_UNREADY = 1;

    public static final int IDENTITY_IDLE = 2;

    public static final int IDENTITYING = 3;

    private volatile int identityStatus = FEATURE_DATAS_UNREADY;

    private static final String TYPE_LIVENSS = "TYPE_LIVENSS";

    private static final int TYPE_RGB_LIVENSS = 2;

    private static final int mWidth = 640;

    private static final int mHeight = 480;

    private AutoTexturePreviewView mPreviewView;

    TextureView textureView;

    IFace.IFaceListener listener;

    private ExecutorService es = Executors.newSingleThreadExecutor();

    @Override
    public void FaceInit(Context context, FaceSDKManager.SdkInitListener listener) {
        DBManager.getInstance().init(context);
        FaceSDKManager.getInstance().init(context, listener);
        livnessTypeTip();
    }

    @Override
    public void CameraPreview(Context context, AutoTexturePreviewView previewView, TextureView textureView, IFaceListener listener) {
        this.listener = listener;
        startCameraPreview(context, previewView, textureView);

    }


    Bitmap InputBitmap;

    @Override
    public void Face_to_IMG(Bitmap bitmap) {
        action = FacePresenter.FaceAction.IMG_MATCH_IMG;
        this.InputBitmap = bitmap;
    }

    Bitmap global_bitmap;

    @Override
    public void Face_allView() {
//        Bitmap bmp = Bitmap.createBitmap(global_IFrame.getArgb(), global_IFrame.getWidth(), global_IFrame.getHeight(), Bitmap.Config.ARGB_8888);
        listener.onBitmap(FacePresenter.FaceResultType.AllView, global_bitmap);
    }

    @Override
    public void FaceSetNoAction() {
        action = FacePresenter.FaceAction.No_ACTION;
    }

    @Override
    public void setIdentifyStatus(int i) {
        identityStatus = i;
    }

    @Override
    public void FaceIdentify() {
        action = FacePresenter.FaceAction.Identify_ACTION;
    }

    ICardInfo InputCardInfo;

    @Override
    public void FaceReg(ICardInfo cardInfo, Bitmap bitmap) {
        action = FacePresenter.FaceAction.Reg_ACTION;
        this.InputBitmap = bitmap;
        this.InputCardInfo = cardInfo;
    }

    @Override
    public void FaceIdentifyReady() {
        if (identityStatus != FEATURE_DATAS_UNREADY) {
            return;
        }
        es.submit(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                Log.e("sdsdsad", "人脸更新线程已启动");
                // android.os.Process.setThreadPriority (-4);
                FaceApi.getInstance().loadFacesFromDB("1");
                int count = FaceApi.getInstance().getGroup2Facesets().get("1").size();
                Log.e("人脸库内人脸数目:", String.valueOf(count));
                identityStatus = IDENTITY_IDLE;
            }
        });
    }

    @Override
    public void PreviewCease() {
        Camera1PreviewManager.getInstance().stopPreview();
        mPreviewView = null;
        textureView = null;
    }

    private void livnessTypeTip() {
        PreferencesUtil.putInt(TYPE_LIVENSS, TYPE_RGB_LIVENSS);
    }

    private void startCameraPreview(Context context, AutoTexturePreviewView previewView, TextureView textureView) {
        // 设置前置摄像头
        // Camera1PreviewManager.getInstance().setCameraFacing(Camera1PreviewManager.CAMERA_FACING_FRONT);
        // 设置后置摄像头
        // Camera1PreviewManager.getInstance().setCameraFacing(Camera1PreviewManager.CAMERA_FACING_BACK);
        // 设置USB摄像头
        this.mPreviewView = previewView;
        this.textureView = textureView;
        this.textureView.setOpaque(false);
        // 不需要屏幕自动变黑。
        this.textureView.setKeepScreenOn(true);
        Camera1PreviewManager.getInstance().setCameraFacing(Camera1PreviewManager.CAMERA_USB);
        Camera1PreviewManager.getInstance().startPreview(context, previewView, mWidth, mHeight, new CameraDataCallback() {
            @Override
            public void onGetCameraData(int[] data, Camera camera, int width, int height) {
                dealCameraData(data, width, height);
            }
        });
    }

    public void dealCameraData(int[] data, int width, int height) {
//        if (selectType == TYPE_PREVIEWIMAGE_OPEN) {
//            showDetectImage(width, height, data); // 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断。实际应用中可注释掉
//        }
        // 摄像头预览数据进行人脸检测
        global_bitmap = Bitmap.createBitmap(data, width, height, Bitmap.Config.ARGB_8888);
        faceDetect(data, width, height);
    }


    private void faceDetect(int[] argb, int width, int height) {
//        if (liveType == LivenessSettingActivity.TYPE_NO_LIVENSS) {
//            FaceTrackManager.getInstance().setAliving(false); // 无活体检测
//        } else if (liveType == LivenessSettingActivity.TYPE_RGB_LIVENSS) {
//            FaceTrackManager.getInstance().setAliving(true); // 活体检测
//        }
        FaceTrackManager.getInstance().setAliving(true); // 活体检测
        FaceTrackManager.getInstance().faceTrack(argb, width, height, new FaceDetectCallBack() {
            @Override
            public void onFaceDetectCallback(LivenessModel livenessModel) {
                showFrame(livenessModel);
                checkResult(livenessModel);
            }

            @Override
            public void onTip(int code, final String msg) {
//                displayTip(msg);
            }
        });
    }

    boolean livenessSuccess = false;

    private void checkResult(LivenessModel model) {
        if (model == null) {
            return;
        }
        livenessSuccess = (model.getRgbLivenessScore() > livnessScore) ? true : false;
        if (livenessSuccess) {
            switch (action) {
                case No_ACTION:
                    break;
                case Reg_ACTION:
                    action = FacePresenter.FaceAction.No_ACTION;
                    register(model.getFaceInfo(), model.getImageFrame(), InputCardInfo);
                    break;
                case IMG_MATCH_IMG:
                    action = FacePresenter.FaceAction.No_ACTION;
                    Img_match_Img(model.getFaceInfo(), model.getImageFrame());
                    break;
                case Identify_ACTION:
                    identity(model.getImageFrame(), model.getFaceInfo());
                    break;
                default:
                    break;
            }
        }
//        }
    }


    private Paint paint = new Paint();

    {
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(30);
        paint.setStrokeWidth(5);
    }

    RectF rectF = new RectF();

    private void showFrame(LivenessModel model) {
        Canvas canvas = textureView.lockCanvas();
        if (canvas == null) {
            textureView.unlockCanvasAndPost(canvas);
            return;
        }
        if (model == null) {
            // 清空canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            textureView.unlockCanvasAndPost(canvas);
            Log.e("canvasClear", "canvas by model is null");

            return;
        }
        FaceInfo[] faceInfos = model.getTrackFaceInfo();
        ImageFrame imageFrame = model.getImageFrame();
        if (faceInfos == null || faceInfos.length == 0) {
            // 清空canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            textureView.unlockCanvasAndPost(canvas);
            Log.e("canvasClear", "canvas by faceInfo is null");
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        FaceInfo faceInfo = faceInfos[0];
        rectF.set(getFaceRectTwo(faceInfo, imageFrame));
        // 检测图片的坐标和显示的坐标不一样，需要转换。
        mapFromOriginalRect(rectF, faceInfo, imageFrame);
        float yaw2 = Math.abs(faceInfo.headPose[0]);
        float patch2 = Math.abs(faceInfo.headPose[1]);
        float roll2 = Math.abs(faceInfo.headPose[2]);
        if (yaw2 > 20 || patch2 > 20 || roll2 > 20) {
            // 不符合要求，绘制黄框
            paint.setColor(Color.YELLOW);
            String text = "请正视屏幕";
            float width = paint.measureText(text) + 50;
            float x = rectF.centerX() - width / 2;
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(text, x + 25, rectF.top - 20, paint);
            paint.setColor(Color.YELLOW);
        } else {
            // 符合检测要求，绘制绿框
            paint.setColor(Color.GREEN);
        }
        paint.setStyle(Paint.Style.STROKE);
        // 绘制框
        canvas.drawRect(rectF, paint);
        textureView.unlockCanvasAndPost(canvas);
    }


    public Rect getFaceRectTwo(FaceInfo faceInfo, ImageFrame frame) {
        Rect rect = new Rect();
        int[] points = new int[8];
        faceInfo.getRectPoints(points);
        int left = points[2];
        int top = points[3];
        int right = points[6];
        int bottom = points[7];

        int width = (right - left);
        int height = (bottom - top);

        left = (int) ((faceInfo.mCenter_x - width / 2));
        top = (int) ((faceInfo.mCenter_y - height / 2));

        rect.top = top < 0 ? 0 : top;
        rect.left = left < 0 ? 0 : left;
        rect.right = (int) ((faceInfo.mCenter_x + width / 2));
        rect.bottom = (int) ((faceInfo.mCenter_y + height / 2));
        return rect;
    }

    public void mapFromOriginalRect(RectF rectF, FaceInfo faceInfo, ImageFrame imageFrame) {
        int selfWidth = mPreviewView.getPreviewWidth();
        int selfHeight = mPreviewView.getPreviewHeight();
        Matrix matrix = new Matrix();
        if (selfWidth * imageFrame.getHeight() > selfHeight * imageFrame.getWidth()) {
            int targetHeight = imageFrame.getHeight() * selfWidth / imageFrame.getWidth();
            int delta = (targetHeight - selfHeight) / 2;
            float ratio = 1.0f * selfWidth / imageFrame.getWidth();
            matrix.postScale(ratio, ratio);
            matrix.postTranslate(0, -delta);
        } else {
            int targetWith = imageFrame.getWidth() * selfHeight / imageFrame.getHeight();
            int delta = (targetWith - selfWidth) / 2;
            float ratio = 1.0f * selfHeight / imageFrame.getHeight();
            matrix.postScale(ratio, ratio);
            matrix.postTranslate(-delta, 0);
        }
        matrix.mapRect(rectF);
        if (false) { // 根据镜像调整
            float left = selfWidth - rectF.right;
            float right = left + rectF.width();
            rectF.left = left;
            rectF.right = right;
        }
    }

    private boolean Img_match_Img(FaceInfo faceInfo, ImageFrame imageFrame) {
        final Disposable outOfTime = Observable.timer(10, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        listener.onText(FacePresenter.FaceResultType.IMG_MATCH_IMG_Error, "人员登记已超出时间，请重试");
                    }
                });
        final byte[] bytes1 = new byte[512];
        final byte[] bytes2 = new byte[512];
        final Bitmap bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());
        float ret1 = FaceApi.getInstance().extractVisFeature(bitmap, bytes1, 50);
        if (ret1 == 128) {
            float ret2 = FaceApi.getInstance().extractVisFeature(InputBitmap, bytes2, 50);
            if (ret2 == 128) {
                if (FaceApi.getInstance().match(bytes1, bytes2) > 50) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onBitmap(FacePresenter.FaceResultType.IMG_MATCH_IMG_True, bitmap);
                            listener.onText(FacePresenter.FaceResultType.IMG_MATCH_IMG_True, "人证比对通过");
                            outOfTime.dispose();
                        }
                    });
                    return true;
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onBitmap(FacePresenter.FaceResultType.IMG_MATCH_IMG_False, bitmap);
                            listener.onText(FacePresenter.FaceResultType.IMG_MATCH_IMG_False, "人证比对失败,请重试");
                            outOfTime.dispose();

                        }
                    });
                }
            }
        } else if (ret1 == -100) {
            toast("未完成人脸比对，可能原因，图片为空");
            listener.onText(FacePresenter.FaceResultType.IMG_MATCH_IMG_False, "人证比对失败,请重试");
            outOfTime.dispose();
        } else if (ret1 == -102) {
            toast("未完成人脸比对，可能原因，图片未检测到人脸");
            listener.onText(FacePresenter.FaceResultType.IMG_MATCH_IMG_False, "人证比对失败,请重试");
            outOfTime.dispose();

        } else {
            toast("未完成人脸比对，可能原因，"
                    + "人脸太小（小于sdk初始化设置的最小检测人脸）"
                    + "人脸不是朝上，sdk不能检测出人脸");
            listener.onText(FacePresenter.FaceResultType.IMG_MATCH_IMG_False, "人证比对失败,请重试");
            outOfTime.dispose();
        }
        return false;
    }

    private void register(final FaceInfo faceInfo, final ImageFrame imageFrame, final ICardInfo cardInfo) {
        /*
         * 用户id（由数字、字母、下划线组成），长度限制128B
         * uid为用户的id,百度对uid不做限制和处理，应该与您的帐号系统中的用户id对应。
         *
         */
        // String uid = 修改为自己用户系统中用户的id;
        final User user = new User();
//        final String uid = UUID.randomUUID().toString();
        user.setUserId(cardInfo.cardId());
        user.setUserInfo(cardInfo.name());
        user.setGroupId("1");
        es.submit(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());
                ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
                byte[] bytes = new byte[512];
                float ret = FaceApi.getInstance().extractVisFeature(argbImg, bytes, 50);
                if (ret == -1) {
                    toast("人脸太小（必须打于最小检测人脸minFaceSize），或者人脸角度太大，人脸不是朝上");
                    action = FacePresenter.FaceAction.Reg_ACTION;
                } else if (ret != -1) {
                    Feature feature = new Feature();
                    feature.setGroupId("1");
                    feature.setUserId(cardInfo.cardId());
                    feature.setFeature(bytes);
                    user.getFeatureList().add(feature);
                    handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onBitmap(FacePresenter.FaceResultType.Reg, bitmap);
                                listener.onUser(FacePresenter.FaceResultType.Reg, user);
                                listener.onText(FacePresenter.FaceResultType.Reg, "success");
                            }
                        });
//                    if (FaceApi.getInstance().userAdd(user)) {
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                listener.onBitmap(FacePresenter.FaceResultType.Reg, bitmap);
//                                listener.onUser(FacePresenter.FaceResultType.Reg, user);
//                                listener.onText(FacePresenter.FaceResultType.Reg, "success");
//                            }
//                        });
//                        Employees employees = new Employees();
//                        employees.setCardID(cardInfo.cardId());
//                        employees.setName(cardInfo.name());
//                        employees.setFaseBytes(bytes);
//                        SingleEmployee.getInstance().setEm(employees);
//                        //saveFace(faceInfo,imageFrame,cardInfo);
//                    } else {
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                listener.onText(FacePresenter.FaceResultType.Reg, "failed");
//                            }
//                        });
//                        action = FacePresenter.FaceAction.Reg_ACTION;
//                    }
                } else {
                    toast("抽取特征失败");
                }
            }
        });
    }

    private void identity(ImageFrame imageFrame, FaceInfo faceInfo) {
        if (identityStatus != IDENTITY_IDLE) {
            return;
        }
//        final Bitmap bmp = Bitmap.createBitmap(global_IFrame.getArgb(), global_IFrame.getWidth(), global_IFrame.getHeight(), Bitmap.Config.ARGB_8888);
        float raw = Math.abs(faceInfo.headPose[0]);
        float patch = Math.abs(faceInfo.headPose[1]);
        float roll = Math.abs(faceInfo.headPose[2]);
        // 人脸的三个角度大于20不进行识别
        if (raw > 20 || patch > 20 || roll > 20) {
            return;
        }
        identityStatus = IDENTITYING;
        int[] argb = imageFrame.getArgb();
        int rows = imageFrame.getHeight();
        int cols = imageFrame.getWidth();
        int[] landmarks = faceInfo.landmarks;
        final IdentifyRet identifyRet = FaceApi.getInstance().identity(argb, rows, cols, landmarks, "1");
        if (identifyRet.getScore() < 80) {
//            identityStatus = IDENTITY_IDLE;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onText(FacePresenter.FaceResultType.Identify_non, "系统没有找到相关人脸信息");
                }
            });
            Observable.timer(2, TimeUnit.SECONDS).observeOn(Schedulers.from(es))
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            identityStatus = IDENTITY_IDLE;
                        }
                    });
            return;
        } else {
            final User user = FaceApi.getInstance().getUserInfo("1", identifyRet.getUserId());
            if (user == null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onText(FacePresenter.FaceResultType.Identify_non, "系统没有找到相关人脸信息");
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onBitmap(FacePresenter.FaceResultType.Identify, global_bitmap);
                        listener.onUser(FacePresenter.FaceResultType.Identify, user);
                    }
                });
            }
            Observable.timer(5, TimeUnit.SECONDS).observeOn(Schedulers.from(es))
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            identityStatus = IDENTITY_IDLE;
                        }
                    });
        }
    }

    private void toast(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showLong(msg);
            }
        });
    }

    private void saveFace(FaceInfo faceInfo, ImageFrame imageFrame, ICardInfo cardInfo) {
        final Bitmap bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());
        File faceDir = FileUitls.getFaceDirectory();
        if (faceDir != null) {
            String imageName = cardInfo.name()+"_"+cardInfo.cardId();
            File file = new File(faceDir, imageName);
            // 压缩人脸图片至300 * 300，减少网络传输时间
            ImageUtils.resize(bitmap, file, 300, 300);
        } else {
            toast("注册人脸目录未找到");
        }

    }

}
