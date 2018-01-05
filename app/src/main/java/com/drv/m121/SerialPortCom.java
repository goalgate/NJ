package com.drv.m121;

import android.os.Handler;
import android.os.Message;

import com.log.Lg;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.SerialPort;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

/**
 * android:sharedUserId="android.uid.system"
 * Created by Administrator on 2016/11/14.
 * 串口调用类
 */

public abstract class SerialPortCom {
    private final int MAXLINES = 200;
    private String devType="rk3368";  //rk2268  t2  t3
    private String devName_ = "/dev/ttyS0";
    private int speed_ = 115200;
    private int dataBits_ = 8;
    private int stopBits_ = 1;
    private int devfd = -1;
    private final int BUFSIZE = 2048;
    private byte[] buf = new byte[BUFSIZE];
    private Timer timer = new Timer();
    private long revSize=0;
    protected SerialPort rkSerial_;
    protected OutputStream mOutputStream;
    protected InputStream mInputStream;
    protected ReadThread mReadThread;
    protected byte[] mBuffer;

    public long getRevSize()
    {
        return revSize;
    }
    public abstract void onRead(int fd,int len,byte[] buf);
    
    
  //设置设备类型
    public void setDevType(String sType) {
    	devType = sType;
    }
    
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                int retSize=0;	
				if (devType.equals("rk3368")) {
					 break;
				} else {

				}
				super.handleMessage(msg);                    
            }
        }
    };

    //发送数据
    public int write(byte[] buf)
    {
    	if(buf==null){return 0;}
        int ret=0;
        try {
            if(devfd>=0)
            {
            		if (rkSerial_.getOutputStream() == null) {
						return 0;
					}

						 rkSerial_.getOutputStream().write(buf);
                         //Thread.sleep(100);
						 ret =buf.length;
            }
        }catch (Exception ex){
            ret=0;
        }
        return ret;
    }

    public int getPortState()
    {
        return devfd;
    }

    private TimerTask task = new TimerTask() {
        public void run() {
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    //设置串口名称
    public void setDevName(String name) {
        devName_ = name;
    }
    


    //设置参数
    public void setPar(int speed, int dataBits, int stopBits) {
        speed_ = speed;
        dataBits_ = dataBits;
        stopBits_ = stopBits;
    }


    public void close(){
            timer.cancel();
            if(devfd!=-1)
            {
                   if(rkSerial_!=null)
                   	rkSerial_.close();
            	    devfd = -1;
            }
   }


    public int open(int sp)
    {
        if(sp>9600)
        {
            speed_=sp;
        }

        try {

        		//getSerialPort();
        		rkSerial_ = new SerialPort(new File(devName_), speed_, 0);
                mInputStream=rkSerial_.getInputStream();


                if(mReadThread==null){
                    mReadThread = new ReadThread();
                    mReadThread.start();
                }
                Lg.v("SerialPortCom_open","open m121 SerialPort ok");
        		devfd=1;

        }catch(Exception e){
            Lg.e("SerialPortCom_open",e.toString());
            return -1;
        }
        return devfd;
    }

    //**byte转为准Hex(两位字符)
    public String byteToHex(byte b) {
        String s = "";
        s = Integer.toHexString(0xFF&b).trim();
        if (s.length() < 2) {
            s = "0" + s;
        }

        return s.toUpperCase();
    }


    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    if (mInputStream == null) {
                        return;
                    }
                    if(devfd>0) {
                        byte[] buffer = new byte[1500];
                        size = mInputStream.read(buffer);
                        if (size > 0) {
                            try {
                                //Lg.d("SerialPortCom_ReadThread",size+"");
                                onRead(devfd, size, buffer);
                                // int ret = HardwareControler.write(devfd,buf);
                            } catch (Exception e) {
                            }
                        }
                    }
                } catch (Exception e) {
                    Lg.e("SerialPortCom_ReadThread",e.toString());
                    return;
                }
            }
        }
    }






}
