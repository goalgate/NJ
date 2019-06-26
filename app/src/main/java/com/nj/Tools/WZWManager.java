package com.nj.Tools;

import android.content.Context;
import android.os.Build;


public class WZWManager {

    private static WZWManager wzwManager;

    com.ys.myapi.MyManager manager1;

    com.ys.rkapi.MyManager manager2;


    public void unBindAIDLService(Context context) {
        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") + 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
            manager2.unBindAIDLService(context);
        } else {
            manager1.unBindAIDLService(context);
        }
    }

    public void setTime(int year, int month, int day, int hour, int minute, int second) {
        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") + 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
            manager2.setTime(year, month, day, hour, minute, second);
        } else {
            manager1.setTime(year, month, day, hour, minute);
        }
    }


    public void reboot() {
        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") + 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
            manager2.reboot();
        } else {
            manager1.reboot();
        }
    }

    public void setStaticEthIPAddress(String IPaddr, String gateWay, String mask, String dns1, String dns2) {
        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") + 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
            manager2.setStaticEthIPAddress(IPaddr, gateWay, mask, dns1, dns2);
        } else {
            manager1.setStaticEthIPAddress(IPaddr, gateWay, mask, dns1, dns2);
        }
    }

    public String getAndroidDisplay() {
        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") + 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
            return manager2.getAndroidDisplay();
        } else {
            return manager1.getAndroidDisplay();
        }
    }

    public void setDhcpIpAddress(Context context) {
        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") + 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
            manager2.setDhcpIpAddress(context);
        } else {
            manager1.setDhcpIpAddress(context);
        }
    }

    public String getEthMode() {
        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") + 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
            return manager2.getEthMode();
        } else {
            return manager1.getEthMode();
        }
    }

    public void bindAIDLService(Context context) {
        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") + 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
            manager2.bindAIDLService(context);
        } else {
            manager1.bindAIDLService(context);
        }
    }

    public static WZWManager getInstance(Context context) {
        if (wzwManager == null) {
            wzwManager = new WZWManager(context);
        }
        return wzwManager;
    }

    private WZWManager(Context context) {
        if (Integer.parseInt(Build.VERSION.INCREMENTAL.substring(Build.VERSION.INCREMENTAL.indexOf(".20") + 1, Build.VERSION.INCREMENTAL.indexOf(".20") + 9)) >= 20190606) {
            manager2 = com.ys.rkapi.MyManager.getInstance(context);
        } else {
            manager1 = com.ys.myapi.MyManager.getInstance(context);
        }
    }

}
