/**
 * Project Name:MobEnforcement
 * File Name:OKHttpUtils.java
 * Package Name:com.sxzb.mobenforcement.application
 * Date:2017年1月11日下午1:46:55
 * Copyright (c) 2017, 陕西中爆安全网科技有限公司 All Rights Reserved.
 */

package com.nj.Tools;

import android.content.Context;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.OkHttpClient;

public class OKHttpUtils {
    private static OkHttpClient client;

    /**
     * 创建一个OkHttpClient的对象的单例
     * @return
     */
    public synchronized static OkHttpClient getOkHttpClientInstance(Context mContext) {
        if (client == null) {
            SSLParser.SSLParams sslParams = null;
            try {
                sslParams = SSLParser.getSSLParams(mContext);
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    //设置连接超时等属性,不设置可能会报异常
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .sslSocketFactory(sslParams.getSslSocketFactory(), sslParams.trustManager)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });

            client = builder.build();
        }
        return client;
    }
}
  