package com.nj.Tools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * Created by Administrator on 2017-07-13.
 */

public class NetInfo {
    //取有线网卡MAC
    public String getMac() {
        return getMac("eth0");
    }

    //取无线网卡MAC
    public String getWifiMac() {
        return getMac("wlan0");
    }

    public String getMac(String name) {
        String macSerial = "";
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/"+name+"/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str;) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            macSerial="";
            ex.printStackTrace();
        }
        return macSerial;
    }

    //取macID
    public String getMacId()
    {
        return macToId(getMac());
    }


    public  String macToId(String mac)
    {
        String s="";
        if(mac==null){return "";}
        String[] ss=mac.split(":");
        if(ss.length<6){return "";}
        try
        {
            for(int i=0;i<6;i++)
            {
                int b = Integer.parseInt(ss[i].trim(), 16);
                s+=formatStr(String.valueOf( b),3);
                if(i==1||i==3)
                {
                    s+="-";
                }
            }

        }catch(Exception ex){
            s="";
        }
        return s;
    }

    public  String formatStr(String str, int len) {
        String s = "";
        if (str.length() == len) {
            s = str;
        } else if (str.length() < len) {
            for (int i = str.length(); i < len; i++) {
                s = '0' + s;
            }
            s = s + str;
        } else if (str.length() > len) {
            s = str.substring(str.length() - len);

        }

        return s;

    }
}
