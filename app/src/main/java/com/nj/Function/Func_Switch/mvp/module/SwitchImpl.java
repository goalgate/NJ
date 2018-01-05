package com.nj.Function.Func_Switch.mvp.module;

import android.os.Handler;
import android.os.Message;

import com.drv.m121.SerialPortCom;
import com.log.Lg;

import java.util.Calendar;
import java.util.Timer;


/**
 * Created by zbsz on 2017/8/23.
 */

public class SwitchImpl extends SerialPortCom implements ISwitching {

    public enum Hex{
        H1,H2,H3,H4,H5,H6,H7,H8,H9,HA,HB,HC,HD,HE,HF
    }

    private byte[] buf_ = new byte[2048];
    private int bufCount = 0;
    private int checkCount_ = 0;
    private String testStr="";
    private byte[]  switchingValue=new byte[8]; //开关量状态
    private Calendar switchingTime=Calendar.getInstance(); //取开关时状态时间

    private Calendar  temHumTime=Calendar.getInstance(); //取温湿度时间
    private int temperature=0;  //温度
    private int humidity=0;   //湿度

    ISwitchingListener listener;


    private byte[] dt_temHum_ ={ (byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0x96,0x69, 0x55, 0x63, 0x7E, 0x6B};  //温湿度命令


    private byte[] dt_outD8off_ ={ (byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0x96,0x69, 0x40, 0x30, 0x58, (byte)0xDD};  //D9断电器关命令
    private byte[] dt_outD8on_ ={ (byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0x96,0x69, 0x41, 0x31, 0x08, 0x1D};  //D9断电器开命令
    private byte[] dt_outD9off_ ={ (byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0x96,0x69, 0x20, 0x10, (byte)0x80, (byte)0xF4};  //D9断电器关命令
    private byte[] dt_outD9on_ ={ (byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0x96,0x69, 0x21, 0x11, (byte)0xD0, 0x34};  //D9断电器开命令
    private byte[] dt_buzz_ ={ (byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0x0B,0x0B, 0x02, 0x33, (byte)0x7B, 0x23};  //D9断电器开命令
    private byte[] dt_buzzOff ={ 0x02,0x02,0x07,0x00,0x10, 0x01,(byte)0xFF,(byte)0xFF, (byte)0xC8,0x00,0x00,(byte)0x99 ,(byte)0x2E};  //D9断电器开命令

    //接收数据最后时间
    private long lastRevTime_;

    private Timer terCheck = new Timer(); //检测是否读完

    @Override
    public void onOpen(ISwitchingListener listener) {
        this.listener = listener;
        setDevName("/dev/ttyS0");
        open(115200);
    }

    @Override
    public void onReadHum() {
        sendData(dt_temHum_);
    }

    @Override
    public void onOutD8(boolean status) {
        if(status){
            sendData(dt_outD8on_);
        }else{
            sendData(dt_outD8off_);
        }
    }

    @Override
    public void onOutD9(boolean status) {
        if(status){
            sendData(dt_outD9on_);
        }else{
            sendData(dt_outD9off_);
        }
    }

    @Override
    public void onBuzz(Hex hex) {

        switch (hex){
            case H1:
                dt_buzz_[5] = 0x01;
                break;
            case H2:
                dt_buzz_[5] = 0x02;
                break;
            case H3:
                dt_buzz_[5] = 0x03;
                break;
            case H4:
                dt_buzz_[5] = 0x04;
                break;
            case H5:
                dt_buzz_[5] = 0x05;
                break;
            case H6:
                dt_buzz_[5] = 0x06;
                break;
            case H7:
                dt_buzz_[5] = 0x07;
                break;
            case H8:
                dt_buzz_[5] = 0x08;
                break;
            case H9:
                dt_buzz_[5] = 0x09;
                break;
            case HA:
                dt_buzz_[5] = 0x0A;
                break;
            case HB:
                dt_buzz_[5] = 0x0B;
                break;
            case HC:
                dt_buzz_[5] = 0x0C;
                break;
            case HD:
                dt_buzz_[5] = 0x0D;
                break;
            case HE:
                dt_buzz_[5] = 0x0E;
                break;
            case HF:
                dt_buzz_[5] = 0x0F;
                break;



            default:
                break;

        }

        sendData(dt_buzz_);
    }

    @Override
    public void onBuzzOff() {
        sendData(dt_buzzOff);
    }

    @Override
    public void onRead(int fd, int len, byte[] buf) {
        if(buf==null){return;}
        if(buf.length<len){return;}

        int btr = len;
        byte[] by = new byte[btr];
        if (btr > 0)
        {
            System.arraycopy(buf,0,by,0,btr);        //依据串口数据长度BytesToRead来接收串口的数据并存放在by数组之中
            testStr="";
            for(int i=0;i<by.length;i++)
            {
                testStr+=byteToHex(by[i]);
            }

            if(btr>=9)
            {

                if(by[0]==(byte)0xAA&&by[1]==(byte)0xAA&&by[2]==(byte)0xAA)
                {
                    for(int i=0;i<6;i++)
                    {
                        switchingValue[i]=by[8-i];
                    }
                    switchingValue[7]=1;
                    switchingTime=Calendar.getInstance();
                    mhandler.sendMessage(getMsg(0x123));
                }else if(by[0]==(byte)0xBB&&by[1]==(byte)0xBB&&by[2]==(byte)0xBB)
                {
                    if(by[4]==0x00&&by[7]==(byte)0xC1&&by[8]==(byte)0xEF) {
                        temperature = (int) by[5];
                        humidity = (int) by[3];
                        temHumTime = Calendar.getInstance();
                        mhandler.sendMessage(getMsg(0x123));
                        mhandler.sendMessage(getMsg(0x234));
                    }else if(by[3]==(byte)0x96&&by[6]==0x1F&&by[7]==0x44&&by[8]==(byte)0xAD) {
                        //mhandler.sendMessage(getMsg(0x123));
                    }
                }
            }
        }
        lastRevTime_ = System.currentTimeMillis();    //记录最后一次串口接收数据的时间
        checkCount_ = 0;
    }


    private void sendData(byte[] bs)
    {
        try{
            write(bs);
            lastRevTime_ = System.currentTimeMillis();    //记录最后一次串口接收数据的时间
        }
        catch(Exception ex) {
            Lg.e("M121_sendData",ex.toString());
        }
    }


    Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0x123){
                listener.onSwitchingText(testStr);
            }else if(msg.what == 0x234){
                listener.onTemHum(temperature,humidity);
            }
        }
    };

    private Message getMsg(int what){
        Message msg = new Message();
        msg.what = what;
        return msg;
    }
}
