package com.nj;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.log.Lg;
import com.nj.Tools.DESX;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by zbsz on 2017/12/8.
 */

public class StartActivity extends Activity {

    private SPUtils config = SPUtils.getInstance("config");

    @BindView(R.id.devid_input)
    EditText dev_suffix;

    @OnClick(R.id.next)
    void next() {
            JSONObject jsonKey = new JSONObject();
            try {
                jsonKey.put("daid", dev_suffix.getText().toString());
                jsonKey.put("check", DESX.encrypt(dev_suffix.getText().toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            config.put("firstStart", false);
            config.put("daid", dev_suffix.getText().toString());
            config.put("key",DESX.encrypt(jsonKey.toString()));
            config.put("ServerId","https://113.140.1.137:8446/wiscrisrest/deviceDocking/");
            ActivityUtils.startActivity(getPackageName(),getPackageName()+".IndexActivity");
            copyFilesToSdCard();
            StartActivity.this.finish();
            ToastUtils.showLong("设备ID设置成功");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_form);
        ButterKnife.bind(this);
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
