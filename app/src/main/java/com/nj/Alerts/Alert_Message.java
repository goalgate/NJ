package com.nj.Alerts;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.nj.AppInit;
import com.nj.R;
import com.nj.State.DoorState.Door;
import com.nj.State.DoorState.State_Open;
import com.nj.State.LockState.Lock;
import com.nj.State.LockState.State_Lockup;
import com.nj.Tools.NetInfo;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class Alert_Message {

    private Context context;

    public Alert_Message(Context context) {
        this.context = context;
    }
    public static final String STATICIP = "StaticIp";
    public static final String DHCP = "DHCP";
    private AlertView messageAlert;
    private TextView msg_daid;
    private TextView msg_ip;
    private TextView msg_mac;
    private TextView msg_software;
    private TextView msg_ipmode;
    private TextView msg_network;
    private TextView msg_iccard;
    private TextView msg_lockState;
    private TextView msg_doorState;

    public void messageInit() {
        ViewGroup messageView = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.message_form, null);
        msg_daid = (TextView) messageView.findViewById(R.id.msg_daid);
        msg_ip = (TextView) messageView.findViewById(R.id.msg_ip);
        msg_mac = (TextView) messageView.findViewById(R.id.msg_mac);
        msg_software = (TextView) messageView.findViewById(R.id.msg_software);
        msg_ipmode = (TextView) messageView.findViewById(R.id.msg_ipmode);
        msg_network = (TextView) messageView.findViewById(R.id.msg_network);
        msg_iccard = (TextView) messageView.findViewById(R.id.msg_iccard);
        msg_lockState = (TextView) messageView.findViewById(R.id.msg_lockState);
        msg_doorState = (TextView) messageView.findViewById(R.id.msg_doorState);
        messageAlert = new AlertView("信息显示", null, null, new String[]{"确定"}, null, this.context, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {

            }
        });
        messageAlert.addExtView(messageView);
    }
    public void showMessage() {
        msg_daid.setText(SPUtils.getInstance("config").getString("daid"));
        msg_ip.setText(NetworkUtils.getIPAddress(true));
        msg_mac.setText(new NetInfo().getMac());
        msg_software.setText(AppUtils.getAppVersionName());
        if ((DHCP.equals(AppInit.getMyManager().getEthMode()))) {
            msg_ipmode.setText("当前以太网为动态IP获取模式");
        } else if(STATICIP .equals(AppInit.getMyManager().getEthMode())){
            msg_ipmode.setText("当前以太网为静态IP获取模式");
        } else {
            msg_ipmode.setText("当前固件版本过低，无法获取详细信息");
        }
        if (NetworkUtils.isConnected()) {
            msg_network.setText("等待外网联通结果");
            ping();
        } else {
            msg_network.setText("连接网络失败，请检查网线连接状态");
        }
        msg_iccard.setText("请放置身份证进行判断");
        if (Door.getInstance().getDoorState().getClass().getName().equals(State_Open.class.getName())) {
            msg_doorState.setText("仓库门处于开启状态");
        } else {
            msg_doorState.setText("仓库门处于关闭状态");
        }
        if (Lock.getInstance().getLockState().getClass().getName().equals(State_Lockup.class.getName())) {
            msg_lockState.setText("仓库处于上锁状态");
        } else {
            msg_lockState.setText("仓库处于解锁状态");
        }
        messageAlert.show();
    }

    public boolean Showing(){
        return  messageAlert.isShowing();
    }

    public void setICCardText(String id){
        msg_iccard.setText(id);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x666:
                    msg_network.setText("设备与外网通信成功");
                    break;
                case 0x777:
                    msg_network.setText("设备网口正常，外网无法通信，请检查网络");
            }
            super.handleMessage(msg);
        }
    };

    private void ping() {
        new Thread(new Runnable() {
            String result = null;
            @Override
            public void run() {
                try {
                    String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
                    Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
                    // 读取ping的内容，可以不加
                    InputStream input = p.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(input));
                    StringBuffer stringBuffer = new StringBuffer();
                    String content = "";
                    while ((content = in.readLine()) != null) {
                        stringBuffer.append(content);
                    }
                    Log.d("------ping-----", "result content : " + stringBuffer.toString());
                    // ping的状态
                    int status = p.waitFor();
                    if (status == 0) {
                        result = "success";
                        handler.sendEmptyMessage(0x666);
                        return;
                    } else {
                        result = "failed";
                    }
                } catch (IOException e) {
                    result = "IOException";
                } catch (InterruptedException e) {
                    result = "InterruptedException";
                } finally {
                    Log.d("----result---", "result = " + result);
                }
                handler.sendEmptyMessage(0x777);
            }
        }).start();
    }

}
