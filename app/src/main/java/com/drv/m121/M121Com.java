package com.drv.m121;

import android.os.Handler;
import android.os.Message;

import com.log.Lg;

import java.util.Calendar;
import java.util.Timer;


/**
 * Created by Administrator on 2017-08-14.
 */

public class M121Com extends SerialPortCom{
    private byte[] buf_ = new byte[2048];
    private int bufCount = 0;
    private int checkCount_ = 0;
    private String testStr="";
    private byte[]  switchingValue=new byte[8]; //开关量状态
    private Calendar  switchingTime=Calendar.getInstance(); //取开关时状态时间

    private Calendar  temHumTime=Calendar.getInstance(); //取温湿度时间
    private int temperature=0;  //温度
    private int humidity=0;   //湿度



    private byte[] dt_temHum_ ={ (byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0x96,0x69, 0x55, 0x63, 0x7E, 0x6B};  //温湿度命令


    private byte[] dt_outD8off_ ={ (byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0x96,0x69, 0x40, 0x30, 0x58, (byte)0xDD};  //D9断电器关命令
    private byte[] dt_outD8on_ ={ (byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0x96,0x69, 0x41, 0x31, 0x08, 0x1D};  //D9断电器开命令
    private byte[] dt_outD9off_ ={ (byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0x96,0x69, 0x20, 0x10, (byte)0x80, (byte)0xF4};  //D9断电器关命令
    private byte[] dt_outD9on_ ={ (byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0x96,0x69, 0x21, 0x11, (byte)0xD0, 0x34};  //D9断电器开命令


    private ICardState iCardState_;  //事件接口


    //接收数据最后时间
    private long lastRevTime_;

    private Timer terCheck = new Timer(); //检测是否读完

    private final int cmd_switchingValue=1; //开关量状态
    private final int cmd_temHum=2; //温湿度
    private final int cmd_outD8=3; //输出1
    private final int cmd_outD9=4; //输出2

    //命令类型
    private int cmdType = 0;
    //命令值
    private int cmdValue=0;


    public M121Com(String port, ICardState iCardState)
    {
        if(!port.equals(""))
            setDevName(port);

        iCardState_=iCardState;

    }


    //开关量状态：1 为闭合   0为断开  2为不存这通道  ch为通道号 1到6路
    public int getSwitchingValue(int ch)
    {
         if(ch<1||ch>6)
         {
             return 2;
         }else
         {
             if(switchingValue[ch-1]==1)
             {
                 return 1;
             }else
             {
                 return 0;
             }
         }
    }

    //开关量状态时间
    public Calendar getSwitchingTime()
    {
        return switchingTime;
    }

    //取温度
    public int getTemperature()
    {
        return temperature;
    }

    //取温度
    public int getHumidity()
    {
        return humidity;
    }
    //取温湿度时间
    public Calendar getTemHumTime()
    {
        return temHumTime;
    }

    //读取温湿度
    public void readTemHum()
    {
        sendData(cmd_temHum,dt_temHum_);
    }

    //输出控制
    public void outCtrl(boolean isOn)
    {
        if(isOn)
        {
            cmdValue=1;
            sendData(cmd_outD8,dt_outD8on_);

        }else
        {
            cmdValue=0;
            sendData(cmd_outD8,dt_outD8off_);

        }

    }

    //输出控制
    public void outCtrl12V(boolean isOn)
    {
         if(isOn)
         {
             cmdValue=1;
             sendData(cmd_outD9,dt_outD9on_);
         }else
         {
             cmdValue=0;
             sendData(cmd_outD9,dt_outD9off_);

         }
    }

    private void sendData(int ct, byte[] bs)
    {
        try
        {
            cmdType = ct;
            //clear();
            write(bs);
            lastRevTime_ = System.currentTimeMillis();    //记录最后一次串口接收数据的时间
        }
        catch(Exception ex) {
            Lg.e("M121_sendData",ex.toString());
        };

        //terCheck_ = true;

    }



    //接收数据
    public void onRead(int fd,int len,byte[] buf)
    {
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
                    cmdType=cmd_switchingValue;
                    cmdValue=1;
                    readHandler.sendEmptyMessage(0);
                }else if(by[0]==(byte)0xBB&&by[1]==(byte)0xBB&&by[2]==(byte)0xBB)
                {
                    if(by[4]==0x00&&by[7]==(byte)0xC1&&by[8]==(byte)0xEF) {
                        temperature = (int) by[5];
                        humidity = (int) by[3];
                        temHumTime = Calendar.getInstance();
                        cmdType=cmd_temHum;
                        cmdValue=1;
                        readHandler.sendEmptyMessage(0);
                    }else if(by[3]==(byte)0x96&&by[6]==0x1F&&by[7]==0x44&&by[8]==(byte)0xAD) {
                        readHandler.sendEmptyMessage(0);
                    }
                }
            }

        }


        lastRevTime_ = System.currentTimeMillis();    //记录最后一次串口接收数据的时间
        checkCount_ = 0;
    }

    public String getTestStr()
    {
        return testStr;
    }

    public String byteToHex(byte b) {
        String s = "";
        s = Integer.toHexString(0xFF&b).trim();
        if (s.length() < 2) {
            s = "0" + s;
        }

        return s.toUpperCase();
    }

    private Handler readHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what != 0) {
                return;
            }
            try {
                iCardState_.onCardState(cmdType, cmdValue);
            }catch (Exception ex){}
            cmdType=0;
            cmdValue=0;

        }
    };

}
