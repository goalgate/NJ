package com.nj;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import com.alibaba.fastjson.serializer.FloatArraySerializer;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.Utils;
import com.log.Lg;
import com.nj.Function.Func_Camera.mvp.presenter.PhotoPresenter;
import com.squareup.leakcanary.LeakCanary;
import com.ys.myapi.MyManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by zbsz on 2017/8/25.
 */

public class AppInit extends Application {
    protected static AppInit instance;

    public static AppInit getInstance() {
        return instance;
    }

    public static Context getContext() {
        return getInstance().getApplicationContext();
    }

    private static final String PREFS_NAME = "config";

    PhotoPresenter pp = PhotoPresenter.getInstance();

    @Override
    public void onCreate() {

        super.onCreate();

        Lg.setIsSave(true);

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        LeakCanary.install(this);


        instance = this;

        Utils.init(getContext());

        pp.initCamera();

        SPUtils SP_Config = SPUtils.getInstance(PREFS_NAME);

        if (SP_Config.getBoolean("firstStart", true)) {
            copyFilesToSdCard();
            SP_Config.put("firstStart", false);
        }
    }

    String SDCardPath = Environment.getExternalStorageDirectory() +"/";
    private void copyFilesToSdCard() {
        copyFileOrDir(""); // copy all files in assets folder in my project
    }

    private void copyFileOrDir(String path) {
        AssetManager assetManager = this.getAssets();
        String assets[] = null;
        try {
            Lg.i("tag", "copyFileOrDir() "+path);
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path);
            } else {
                String fullPath = SDCardPath+ path;
                Lg.i("tag", "path="+fullPath);
                File dir = new File(fullPath);
                if (!dir.exists() && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                    if (!dir.mkdirs())
                        Lg.i("tag", "could not create dir "+fullPath);
                for (int i = 0; i < assets.length; ++i) {
                    String p;
                    if (path.equals(""))
                        p = "";
                    else
                        p = path + "/";

                    if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                        copyFileOrDir( p + assets[i]);
                }
            }
        } catch (IOException ex) {
            Lg.e("tag", "I/O Exception");
        }
    }

    private void copyFile(String filename) {
        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        OutputStream out = null;
        String newFileName = null;
        try {
            Lg.i("tag", "copyFile() "+filename);
            in = assetManager.open(filename);
            if (filename.endsWith(".jpg")) // extension was added to avoid compression on APK file
                newFileName =SDCardPath+filename.substring(0, filename.length()-4);
            else
                newFileName =SDCardPath+filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Lg.e("tag", "Exception in copyFile() of "+newFileName);
            Lg.e("tag", "Exception in copyFile() "+e.toString());
        }

    }

}

