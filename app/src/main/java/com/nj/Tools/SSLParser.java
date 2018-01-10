/**
 * Project Name: AS-MobEnforcement
 * File Name: SSLParser.java
 * Date: 2017-03-10 11:00
 * Copyright (c) 2017, 陕西中爆安全网科技有限公司 All Rights Reserved.
 */
package com.nj.Tools;

import android.content.Context;


import com.nj.R;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Function : <br/>
 * Date:      2017-03-10 11:00<br>
 *
 * @author ReiChin_
 */
public final class SSLParser {

    public static class SSLParams {
        private SSLSocketFactory sslSocketFactory;
        public X509TrustManager trustManager;

        public SSLSocketFactory getSslSocketFactory() {
            return sslSocketFactory;
        }

        public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
            this.sslSocketFactory = sslSocketFactory;
        }
    }

    /**
     * 解析证书文件获取Https请求所需的SSL数字证书信息
     *
     * @param context
     * @return
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws IOException
     * @throws KeyManagementException
     */
    public static SSLParams getSSLParams(Context context) throws CertificateException,
            NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        SSLParams sslParams = new SSLParams();

        KeyStore ks = readBKSKeyStore(context, R.raw.nj_other);
        TrustManager[] trustManagers = prepareTrustManager(ks);
        X509TrustManager trustManager = chooseTrustManager(trustManagers);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, new SecureRandom());
        sslParams.setSslSocketFactory(sslContext.getSocketFactory());
        sslParams.trustManager = trustManager;

        return sslParams;
    }

    /**
     * 读取证书文件中的Key信息
     *
     * @param context
     * @param cerId
     * @param password
     * @return
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws UnrecoverableKeyException
     */
    private static KeyManager[] prepareKeyManager(Context context, int cerId, String password)
            throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException,
            UnrecoverableKeyException {
        KeyStore clientKeyStore = KeyStore.getInstance("BKS");

        InputStream inputStream = null;
        try {
            inputStream = context.getResources().openRawResource(cerId);
            clientKeyStore.load(inputStream, password.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, password.toCharArray());
            return keyManagerFactory.getKeyManagers();
        } finally {
            if (null != inputStream) {
                inputStream.close();
            }
        }
    }

    /**
     * 读取证书文件中的可信任列表
     *
     * @param ks
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    private static TrustManager[] prepareTrustManager(KeyStore ks) throws
            NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(ks);
        return trustManagerFactory.getTrustManagers();
    }

    /**
     * load证书文件
     *
     * @param context
     * @param cerId
     * @return
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private static KeyStore readBKSKeyStore(Context context, int cerId) throws KeyStoreException,
            CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        InputStream inputStream = null;
        try {
            inputStream = context.getResources().openRawResource(cerId);
            ks.load(inputStream, null);
        } finally {
            if (null != inputStream) {
                inputStream.close();
            }
        }
        return ks;
    }

    /**
     * 筛选出符合X509格式
     *
     * @param trustManagers
     * @return
     */
    private static X509TrustManager chooseTrustManager(TrustManager[] trustManagers) {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        return null;
    }

}
