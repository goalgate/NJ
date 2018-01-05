package com.nj.Tools;

import android.os.Environment;
import android.os.Handler;

import com.blankj.utilcode.util.SPUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * Created by zbsz on 2017/12/2.
 */

public class ServerConnectionUtil {
    public static final String APK_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "Download" + File.separator + "app-release.apk";
    private static int TIME_OUT = 20 * 1000;   //超时时间
    private static String CHARSET = "utf-8";
    private BufferedReader in = null;
    private static String BOUNDARY = UUID.randomUUID().toString();  //边界标识   随机生成
    private static String CONTENT_TYPE = "multipart/form-data";
    private static int bufferSize = 2048;

    Handler handler = new Handler();

    public void download(final String baseUrl, final Callback callback) {
        new Thread() {
            @Override
            public void run() {
                final String response =sendDownload(baseUrl);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(response);
                    }
                });
            }
        }.start();
    }




    private String sendDownload(String baseUrl) {
        String result = null;
        OutputStream os= null;
        File file = new File(APK_PATH);
        if (file.exists()) {
            file.delete();
        }
        try {
            URL url = new URL(baseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);  //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false);  //不允许使用缓存
            conn.setRequestMethod("POST");  //请求方式
            conn.setRequestProperty("Charset", CHARSET);  //设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            if (200 == conn.getResponseCode()) {
                byte[] bs = new byte[bufferSize];
                int len = -1;
                os = new FileOutputStream(APK_PATH);
                while ((len = conn.getInputStream().read(bs)) != -1) {
                    os.write(bs, 0, len);
                }
                if(file.length()>100){
                    result = "true";
                }else{
                    result = null;
                }
            }else{
                result = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                if(os!=null){
                    os.flush();
                    os.close();
                }

            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    public interface Callback {
        void onResponse(String response);
    }




}
