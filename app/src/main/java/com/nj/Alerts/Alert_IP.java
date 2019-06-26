package com.nj.Alerts;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.nj.AppInit;
import com.nj.Function.Func_Camera.mvp.presenter.PhotoPresenter;
import com.nj.R;


import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class Alert_IP {
    private Context context;

    public Alert_IP(Context context) {
        this.context = context;
    }
    private SPUtils staticIP = SPUtils.getInstance("staticIP");
    private AlertView inputStaticIPView;
    long count = 5;
    EditText et_Static_ip;
    EditText et_Static_mask;
    EditText et_Static_gateway;
    EditText et_Static_dns1;
    EditText et_Static_dns2;
    CheckBox ipCheckBox;
    public void IpviewInit() {
        ViewGroup ipview = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.inputstaticip_form, null);
        ipCheckBox = (CheckBox) ipview.findViewById(R.id.ip_checkBox);
        et_Static_ip = (EditText) ipview.findViewById(R.id.static_ip);
        et_Static_mask = (EditText) ipview.findViewById(R.id.static_mask);
        et_Static_gateway = (EditText) ipview.findViewById(R.id.static_gateway);
        et_Static_dns1 = (EditText) ipview.findViewById(R.id.static_DNS1);
        et_Static_dns2 = (EditText) ipview.findViewById(R.id.static_DNS2);
        ipCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    et_Static_ip.setEnabled(true);
                    et_Static_mask.setEnabled(true);
                    et_Static_gateway.setEnabled(true);
                    et_Static_dns1.setEnabled(true);
                    et_Static_dns2.setEnabled(true);
                } else {
                    et_Static_ip.setEnabled(false);
                    et_Static_mask.setEnabled(false);
                    et_Static_gateway.setEnabled(false);
                    et_Static_dns1.setEnabled(false);
                    et_Static_dns2.setEnabled(false);
                }
            }
        });
        inputStaticIPView = new AlertView("设置静态IP", null, "取消", new String[]{"确定"}, null, this.context, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    if (ipCheckBox.isChecked()) {
                        if (RegexUtils.isIP(et_Static_ip.getText().toString()) ||
                                RegexUtils.isIP(et_Static_mask.getText().toString()) ||
                                RegexUtils.isIP(et_Static_gateway.getText().toString()) ||
                                RegexUtils.isIP(et_Static_dns1.getText().toString()) ||
                                RegexUtils.isIP(et_Static_dns2.getText().toString())) {
                            staticIP.put("Static_ip", et_Static_ip.getText().toString());
                            staticIP.put("Static_mask", et_Static_mask.getText().toString());
                            staticIP.put("Static_gateway", et_Static_gateway.getText().toString());
                            staticIP.put("Static_dns1", et_Static_dns1.getText().toString());
                            staticIP.put("Static_dns2", et_Static_dns2.getText().toString());
                            staticIP.put("state", true);
                            AppInit.getMyManager().setStaticEthIPAddress/*ssetEthIPAddress*/(et_Static_ip.getText().toString(),
                                    et_Static_gateway.getText().toString(), et_Static_mask.getText().toString(),
                                    et_Static_dns1.getText().toString(), et_Static_dns2.getText().toString());
                            ToastUtils.showLong("静态IP已设置");
                            Observable.interval(0, 1, TimeUnit.SECONDS)
                                    .take(count + 1)
                                    .map(new Function<Long, Long>() {
                                        @Override
                                        public Long apply(@NonNull Long aLong) throws Exception {
                                            return count - aLong;
                                        }
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Observer<Long>() {
                                        @Override
                                        public void onSubscribe(@NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onNext(@NonNull Long aLong) {
                                            ToastUtils.showLong(aLong + "秒后重新开机保存设置");
                                        }

                                        @Override
                                        public void onError(@NonNull Throwable e) {

                                        }

                                        @Override
                                        public void onComplete() {
                                            PhotoPresenter.getInstance().close_Camera();
                                            AppInit.getMyManager().reboot();
                                        }
                                    });
                        } else {
                            ToastUtils.showLong("IP地址输入格式有误，请重试");
                        }
                    } else {
                        AppInit.getMyManager().setDhcpIpAddress(AppInit.getContext());
                        ToastUtils.showLong("已设置为动态IP获取模式");
                        staticIP.put("state", false);
                        Observable.interval(0, 1, TimeUnit.SECONDS)
                                .take(count + 1)
                                .map(new Function<Long, Long>() {
                                    @Override
                                    public Long apply(@NonNull Long aLong) throws Exception {
                                        return count - aLong;
                                    }
                                })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Long>() {
                                    @Override
                                    public void onSubscribe(@NonNull Disposable d) {

                                    }

                                    @Override
                                    public void onNext(@NonNull Long aLong) {
                                        ToastUtils.showLong(aLong + "秒后重新开机保存设置");
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {
                                        PhotoPresenter.getInstance().close_Camera();
                                        AppInit.getMyManager().reboot();
                                    }
                                });
                    }
                }
            }
        });
        inputStaticIPView.addExtView(ipview);
    }


    public void show(){
        if ((Alert_Message.DHCP.equals(AppInit.getMyManager().getEthMode()))) {
            ipCheckBox.setChecked(false);
        } else if (Alert_Message.STATICIP.equals(AppInit.getMyManager().getEthMode())) {
            ipCheckBox.setChecked(true);
        } else {
            if (staticIP.getBoolean("state")) {
                ipCheckBox.setChecked(true);
            } else {
                ipCheckBox.setChecked(false);
            }
        }

        if (!TextUtils.isEmpty(staticIP.getString("Static_ip"))) {
            et_Static_ip.setText(staticIP.getString("Static_ip"));
            et_Static_gateway.setText(staticIP.getString("Static_gateway"));
            et_Static_mask.setText(staticIP.getString("Static_mask"));
            et_Static_dns1.setText(staticIP.getString("Static_dns1"));
            et_Static_dns2.setText(staticIP.getString("Static_dns2"));
        }
        if (ipCheckBox.isChecked()) {
            et_Static_ip.setEnabled(true);
            et_Static_mask.setEnabled(true);
            et_Static_gateway.setEnabled(true);
            et_Static_dns1.setEnabled(true);
            et_Static_dns2.setEnabled(true);
        }
        inputStaticIPView.show();
    }
}
