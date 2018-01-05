package com.drv.fingerprint;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/11/30.
 */

public class FingerprintInfo implements Parcelable{
    /**
	 * 
	 */
	
	private final int ft_connect=1;  //1为是连接成功  0为连接失败
    private final int ft_getImage=2; //1为取到图片  0为取图失败

    private boolean isOpen_=false; //连接是否成功
    private static DevComm m_usbComm;  //USB通信
    private Activity parentAcitivity_;
    private IFingerprintState fingerprintState_;

    private String m_strPost="";  //运行信息
    private String m_errInfo="";  //出错信息
    private boolean m_bCancel=false; //取消

    byte[]      m_binImage, m_bmpImage; //图片存取信息
    int m_nImgWidth =0;  //图片大小
    int m_nImgHeight=0;
    int m_nImgSize=0;

    private Timer timer = new Timer();  //连续取数据
    private boolean isGetFingerprint=false;

    //取行动位置
    public String getStrPost()
    {
        return m_strPost;
    }

    //取出错信息
    public String getErrInfo()
    {
        return m_errInfo;
    }


    private boolean isGetImage_=false;  //是否在取图


    public boolean isOpen() {
        return isOpen_;
    }

    public void setOpen(boolean open_) {
        isOpen_ = open_;
    }

    //初始化
    public FingerprintInfo(Activity parentActivity, IFingerprintState fingerprintState)
    {
        parentAcitivity_=parentActivity;
        fingerprintState_=fingerprintState;
         if(m_usbComm == null){
            m_usbComm = new DevComm(parentActivity, m_IConnectionHandler);
        }
        m_binImage = new byte[1024*100];
        m_bmpImage = new byte[1024*100];
    }
    

    //打开设备
    public boolean open()
    {
        String[] w_strInfo = new String[1];

        if(m_usbComm != null)
        {
            if (!m_usbComm.IsInit())
            {
                if (m_usbComm.OpenComm())
                {
                    //
                    isOpen_=true;
                }
                else{
                    isOpen_ = false;
                    fingerprintState_.onFingerprintState(ft_connect,0);
                }
            }
            else
            {
                if (m_usbComm.Run_TestConnection() == DevComm.ERR_SUCCESS)
                {
                    if (m_usbComm.Run_GetDeviceInfo(w_strInfo) == DevComm.ERR_SUCCESS)
                    {
                        isOpen_=true;
                        fingerprintState_.onFingerprintState(ft_connect,1);
                    }
                    else {
                        isOpen_ = false;
                        fingerprintState_.onFingerprintState(ft_connect,0);
                    }
                }else {
                isOpen_ = false;
                fingerprintState_.onFingerprintState(ft_connect,0);
            }
            }
        }
        timer=new Timer();
        timer.schedule(task, 0, 500);
        return isOpen_;
    }


    //关闭设备
    public boolean close()
    {

        isOpen_=!m_usbComm.CloseComm();
        if(timer!=null){timer.cancel();timer=null;};
        //if(task!=null){task.cancel();task=null;}
        return !isOpen_;

    }

    private int Capturing(){
        int		w_nRet;
        while(true){
            w_nRet = m_usbComm.Run_GetImage();
            if (w_nRet == DevComm.ERR_CONNECTION)
            {
                m_strPost = "Communication error!";
                return -1;
            }
            else if (w_nRet == DevComm.ERR_SUCCESS)
                break;

            if (m_bCancel){
                m_usbComm.Run_SLEDControl(0);
                return -1;
            }
        }

        return 0;
    }


    private String GetErrorMsg(int nErrorCode)
    {
        String  str = new String("");

        switch(nErrorCode)
        {
            case DevComm.ERR_SUCCESS:
                str = "Succcess";
                break;
            case DevComm.ERR_VERIFY:
                str = "Verify NG";
                break;
            case DevComm.ERR_IDENTIFY:
                str = "Identify NG";
                break;
            case DevComm.ERR_EMPTY_ID_NOEXIST:
                str = "Empty Template no Exist";
                break;
            case DevComm.ERR_BROKEN_ID_NOEXIST:
                str = "Broken Template no Exist";
                break;
            case DevComm.ERR_TMPL_NOT_EMPTY:
                str = "Template of this ID Already Exist";
                break;
            case DevComm.ERR_TMPL_EMPTY:
                str = "This Template is Already Empty";
                break;
            case DevComm.ERR_INVALID_TMPL_NO:
                str = "Invalid Template No";
                break;
            case DevComm.ERR_ALL_TMPL_EMPTY:
                str = "All Templates are Empty";
                break;
            case DevComm.ERR_INVALID_TMPL_DATA:
                str = "Invalid Template Data";
                break;
            case DevComm.ERR_DUPLICATION_ID:
                str = "Duplicated ID : ";
                break;
            case DevComm.ERR_BAD_QUALITY:
                str = "Bad Quality Image";
                break;
            case DevComm.ERR_MERGE_FAIL:
                str = "Merge failed";
                break;
            case DevComm.ERR_NOT_AUTHORIZED:
                str = "Device not authorized.";
                break;
            case DevComm.ERR_MEMORY:
                str = "Memory Error ";
                break;
            case DevComm.ERR_INVALID_PARAM:
                str = "Invalid Parameter";
                break;
            case DevComm.ERR_GEN_COUNT:
                str = "Generation Count is invalid";
                break;
            case DevComm.ERR_INVALID_BUFFER_ID:
                str = "Ram Buffer ID is invalid.";
                break;
            case DevComm.ERR_INVALID_OPERATION_MODE:
                str = "Invalid Operation Mode!";
                break;
            case DevComm.ERR_FP_NOT_DETECTED:
                str = "Finger is not detected.";
                break;
            default:
                str = String.format("Fail, error code=%d", nErrorCode);
                break;
        }

        return str;
    }

    public void cancel()
    {
    	m_usbComm.Run_SLEDControl(0);
        m_bCancel=true;

    }

    //连续取指纹图片
    public void readFingerprint()
    {
        if(isGetFingerprint){return;}
        isGetFingerprint=true;
        isGetImage_=false;
        m_bCancel = false;

    }

    //停止取图
    public void stopreadFingerprint()
    {
        isGetFingerprint=false;
        isGetImage_=false;
        cancel();

    }

    private TimerTask task = new TimerTask() {
        public void run() {
            Message message = new Message();
            message.what = 102;
            handler.sendMessage(message);
        }
    };


    public void getImage(){
        //GetConCaptureState();
        if(isGetImage_){return;}
        isGetImage_=true;
        m_usbComm.Run_SLEDControl(1);
        m_strPost="Input finger!";

        new Thread(new Runnable() {
            int		w_nRet;
            int[] 	width = new int[1];
            int[] 	height = new int[1];

            @Override
            public void run() {

                if (Capturing() < 0) {
                    isGetImage_=false;
                    return;
                }

                try {
                    w_nRet = m_usbComm.Run_UpImage(0, m_binImage, width, height);
                    if (w_nRet != DevComm.ERR_SUCCESS) {
                        m_errInfo = GetErrorMsg(w_nRet);
                        fingerprintState_.onFingerprintState(ft_getImage, 0);
                        isGetImage_ = false;
                        return;
                    }

                    m_strPost = "Get Image OK !";
                    m_nImgWidth = width[0];
                    m_nImgHeight = height[0];
                    int nSize;

                    MakeBMPBuf(m_binImage, m_bmpImage, m_nImgWidth, m_nImgHeight);
                    if ((m_nImgWidth % 4) != 0)
                        nSize = m_nImgWidth + (4 - (m_nImgWidth % 4));
                    else
                        nSize = m_nImgWidth;
                    m_nImgSize = 1078 + nSize * m_nImgHeight;
                    Message message = new Message();
                    message.what = 101;
                    handler.sendMessage(message);
                }catch (Exception ex){ex.printStackTrace();}
                isGetImage_=false;
            }
        }).start();
    }

    public Bitmap getBmp()
    {
        Bitmap bmp=null;
        try {
            bmp = BitmapFactory.decodeByteArray(getBmpImage(), 0, getBmpSize());
        }catch(Exception ex){ex.printStackTrace();}
        return bmp;
    }

    public byte[] getBmpImage()
    {
        return m_bmpImage;
    }

    public int getBmpSize()
    {
        return m_nImgSize;
    }


    private final Handler handler = new Handler(){
        public void handleMessage(Message msg) {
        //	Log.e("MSG.WATH:", msg.what+"");
            switch (msg.what) {
                case 101:
                    fingerprintState_.onFingerprintState(ft_getImage,1);
                    break;
                case 102:
                    if(isGetFingerprint) {
                        getImage();
                    }
                    break;
                default:
                    break;
            }
        }
    };


    //复制图片
    private void   MakeBMPBuf(byte[] Input, byte[] Output, int iImageX, int iImageY)
    {

        byte[] w_bTemp = new byte[4];
        byte[] head = new byte[1078];
        byte[] head2={
                /***************************/
                //file header
                0x42,0x4d,//file type
                //0x36,0x6c,0x01,0x00, //file size***
                0x0,0x0,0x0,0x00, //file size***
                0x00,0x00, //reserved
                0x00,0x00,//reserved
                0x36,0x4,0x00,0x00,//head byte***
                /***************************/
                //infoheader
                0x28,0x00,0x00,0x00,//struct size

                //0x00,0x01,0x00,0x00,//map width***
                0x00,0x00,0x0,0x00,//map width***
                //0x68,0x01,0x00,0x00,//map height***
                0x00,0x00,0x00,0x00,//map height***

                0x01,0x00,//must be 1
                0x08,0x00,//color count***
                0x00,0x00,0x00,0x00, //compression
                //0x00,0x68,0x01,0x00,//data size***
                0x00,0x00,0x00,0x00,//data size***
                0x00,0x00,0x00,0x00, //dpix
                0x00,0x00,0x00,0x00, //dpiy
                0x00,0x00,0x00,0x00,//color used
                0x00,0x00,0x00,0x00,//color important
        };

        int		i,j, num, iImageStep;

        Arrays.fill(w_bTemp, (byte)0);

        System.arraycopy(head2, 0, head, 0, head2.length);

        if ((iImageX % 4) != 0)
            iImageStep = iImageX + (4 - (iImageX % 4));
        else
            iImageStep = iImageX;

        num=iImageX; head[18]= (byte)(num & (byte)0xFF);
        num=num>>8;  head[19]= (byte)(num & (byte)0xFF);
        num=num>>8;  head[20]= (byte)(num & (byte)0xFF);
        num=num>>8;  head[21]= (byte)(num & (byte)0xFF);

        num=iImageY; head[22]= (byte)(num & (byte)0xFF);
        num=num>>8;  head[23]= (byte)(num & (byte)0xFF);
        num=num>>8;  head[24]= (byte)(num & (byte)0xFF);
        num=num>>8;  head[25]= (byte)(num & (byte)0xFF);

        j=0;
        for (i=54;i<1078;i=i+4)
        {
            head[i]=head[i+1]=head[i+2]=(byte)j;
            head[i+3]=0;
            j++;
        }

        System.arraycopy(head, 0, Output, 0, 1078);

        if (iImageStep == iImageX){
            for( i = 0; i < iImageY; i ++){
                System.arraycopy(Input, i*iImageX, Output, 1078+i*iImageX, iImageX);
            }
        }
        else{
            iImageStep = iImageStep - iImageX;

            for( i = 0; i < iImageY; i ++){
                System.arraycopy(Input, i*iImageX, Output, 1078+i*(iImageX+iImageStep), iImageX);
                System.arraycopy(w_bTemp, 0, Output, 1078+i*(iImageX+iImageStep)+iImageX, iImageStep);
            }
        }
    }

    //USB事件
    private final IUsbConnState m_IConnectionHandler = new IUsbConnState() {
        @Override
        public void onUsbConnected() {
            String[] w_strInfo = new String[1];

            if (m_usbComm.Run_TestConnection() == DevComm.ERR_SUCCESS)
            {
                if (m_usbComm.Run_GetDeviceInfo(w_strInfo) == DevComm.ERR_SUCCESS)
                {
                    isOpen_=true;
                    fingerprintState_.onFingerprintState(ft_connect,1);
                }
            }
            else {
                isOpen_ = false;
                fingerprintState_.onFingerprintState(ft_connect,0);
            }


        }

        @Override
        public void onUsbPermissionDenied() {
            //Permission denied!
        }

        @Override
        public void onDeviceNotFound() {
            //Can not find usb device!;
        }
    };

	public IFingerprintState getFingerprintState_() {
		return fingerprintState_;
	}

	public void setFingerprintState_(IFingerprintState fingerprintState_) {
		this.fingerprintState_ = fingerprintState_;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
	}
}
