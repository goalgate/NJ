package com.log;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;

/**
 * Created by Administrator on 2017-05-17.
 */

public class Lg {
    private static boolean isSave=false;  //是否保存日志
    private static boolean isOut=true; //是否输出日志
    private static int logLevel=0;  //输出的级别  1VERBOSE，2DEBUG,3INFO, 4WARN，5ERROR
    private static final int levelVerbose=1;
    private static final int levelDebug=2;
    private static final int levelInfo=3;
    private static final int levelWarn=4;
    private static final int levelError=5;
    private static String logName="app.log";
    private static String logPath="";

    private static OutputStream logSaveOutstream=null;
    private static OutputStreamWriter logSaveOut=null;


    public static void setIsSave(boolean yesorno)
    {
        isSave=yesorno;
    }

    public static void setIsOut(boolean yesorno)
    {
        isOut=yesorno;
    }

    public static void setLogLevel(int level)
    {
        logLevel=level;
    }

    public static int getLogLevel()
    {
        return logLevel;
    }

    public static void setLogPath(String path)
    {
        if(path.substring(path.length()-1).equals("/")) {
            logPath = path;
        }else
        {
            logPath = path+"/";
        }
    }

    public static void setLogName(String name)
    {
        logName=name;
    }


    public static void v(String tag, String msg)
    {
        if(levelVerbose<logLevel)
        {
            return;
        }

        if(isOut)
        {
            Log.v(tag,msg);
        }

        if(isSave)
        {
            saveLog("V",tag,msg);
        }
    }

    public static void d(String tag, String msg)
    {
        if(levelDebug<logLevel)
        {
            return;
        }

        if(isOut)
        {
            Log.d(tag,msg);
        }

        if(isSave)
        {
            saveLog("D",tag,msg);
        }
    }

    public static void i(String tag, String msg)
    {
        if(levelInfo<logLevel)
        {
            return;
        }

        if(isOut)
        {
            Log.i(tag,msg);
        }

        if(isSave)
        {
            saveLog("I",tag,msg);
        }
    }

    public static void w(String tag, String msg)
    {
        if(levelWarn<logLevel)
        {
            return;
        }

        if(isOut)
        {
            Log.w(tag,msg);
        }

        if(isSave)
        {
            saveLog("W",tag,msg);
        }
    }

    public static void e(String tag, String msg)
    {
        if(levelError<logLevel)
        {
            return;
        }

        if(isOut)
        {
            Log.e(tag,msg);
        }

        if(isSave)
        {
            saveLog("E",tag,msg);
        }
    }


    public static void saveLog(String mType, String tag, String msg) {
        try {
            if(!isSave){return;}
            if(logPath.equals("")){return;}
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date date=new java.util.Date();
            String s=sdf.format(date)+"  "+mType+"  "+tag+": "+msg+"\r\n";
            if (logSaveOutstream == null || logSaveOut == null) {
                File file = new File(logPath+logName);
                logSaveOutstream = new FileOutputStream(file);
                logSaveOut = new OutputStreamWriter(logSaveOutstream);
            }
            logSaveOut.write(s);
            logSaveOut.flush();
        } catch (java.io.IOException e) {
            close();
            e.printStackTrace();

        }
    }

    public static void close()
    {
        if(isSave)
        {
            try {
                if (logSaveOut != null) {
                    logSaveOut.close();
                    logSaveOut=null;
                }

                if (logSaveOutstream != null) {
                    logSaveOutstream.close();
                    logSaveOutstream=null;
                }
            }catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }




}
