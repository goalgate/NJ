package com.nj.Function.Fun_FingerPrint.mvp.module;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Base64;

import com.drv.fingerprint.DevComm;
import com.drv.fingerprint.IUsbConnState;
import com.nj.AppInit;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;



/**
 * Created by zbsz on 2017/6/2.
 */

public class FingerPrintImpl implements IFingerPrint {
    IFPListener listener;

    private static DevComm m_usbComm;
    final String TEMPLATE_PATH = "sdcard/template.bin";
    int m_nMaxFpCount = 1000;
    int m_nUserID, m_nImgWidth, m_nImgHeight;
    long m_nPassedTime;
    byte[] m_binImage, m_bmpImage;
    String m_strPost;
    boolean m_bCancel;
    String temp;
    int person_Id;


    @Override
    public void onInit( IFPListener fplistener) {
        this.listener = fplistener;
        if (m_usbComm == null) {
            m_usbComm = new DevComm(AppInit.getContext(), m_IConnectionHandler);
        }
        m_binImage = new byte[1024 * 100];
        m_bmpImage = new byte[1024 * 100];
    }

    @Override
    public void onOpen(IFPListener fplistener) {
        this.listener = fplistener;

        String[] w_strInfo = new String[1];

        if (m_usbComm != null) {
            if (!m_usbComm.IsInit()) {
                if (m_usbComm.OpenComm()) {
                    //
                } else {
                    listener.onText("初始化USB失败!");
                }
            } else {
                if (m_usbComm.Run_TestConnection() == DevComm.ERR_SUCCESS) {
                    if (m_usbComm.Run_GetDeviceInfo(w_strInfo) == DevComm.ERR_SUCCESS) {
                        listener.onText("指纹机打开成功");

                    } else
                        listener.onText("无法连接到指纹机");
                } else
                    listener.onText("无法连接到指纹机");
            }
        }
    }

    @Override
    public void onClose(IFPListener listener) {
        m_usbComm.CloseComm();
    }

    @Override
    public void onCancel(boolean status) {
        m_bCancel = status;
    }

    @Override
    public void onEnroll(String id, final IFPListener listener) {
        int w_nRet;
        int[] w_nState = new int[1];

        if (!m_usbComm.IsInit())
            return;

        if (!CheckUserID(id))
            return;

        // Check if fp is exist
        w_nRet = m_usbComm.Run_GetStatus(m_nUserID, w_nState);

        if (w_nRet != DevComm.ERR_SUCCESS) {
            listener.onText(GetErrorMsg(w_nRet));
            return;
        }

        if (w_nState[0] == DevComm.GD_TEMPLATE_NOT_EMPTY) {
            listener.onText("指纹模板已存在");
            return;
        }

        listener.onText("ID : " + person_Id + "  Press finger : " + m_nUserID);

        m_usbComm.Run_SLEDControl(1);
        m_bCancel = false;

        new Thread(new Runnable() {
            int w_nRet, w_nUserID, w_nEnrollStep = 0, w_nGenCount = 3;
            int[] w_nDupID = new int[1];
            int[] w_nWidth = new int[1];
            int[] w_nHeight = new int[1];

            @Override
            public void run() {


                w_nUserID = m_nUserID;

                while (w_nEnrollStep < w_nGenCount) {
                    m_strPost = String.format("指纹ID : " + m_nUserID + "   放置手指 #%d!", w_nEnrollStep + 1);

                    handler.sendMessage(getMsg(0x234));

                    // Capture
                    if (Capturing() < 0)
                        return;

                    m_strPost = "指纹ID : " + m_nUserID + "   松开手指";
                    handler.sendMessage(getMsg(0x234));

                    // Up Cpatured Image
                    w_nRet = m_usbComm.Run_UpImage(0, m_binImage, w_nWidth, w_nHeight);

                    if (w_nRet != DevComm.ERR_SUCCESS) {
                        m_strPost = GetErrorMsg(w_nRet);
                        handler.sendMessage(getMsg(0x123));
                        return;
                    }

                    // Draw image
                    m_nImgWidth = w_nWidth[0];
                    m_nImgHeight = w_nHeight[0];
                    handler.sendMessage(getMsg(0x345));

                    // Create Template
                    w_nRet = m_usbComm.Run_Generate(w_nEnrollStep);

                    if (w_nRet != DevComm.ERR_SUCCESS) {
                        if (w_nRet == DevComm.ERR_BAD_QUALITY) {
                            m_strPost = "指纹质量欠佳，请重试";
                            handler.sendMessage(getMsg(0x234));
                            continue;
                        } else {
                            m_strPost = GetErrorMsg(w_nRet);
                            handler.sendMessage(getMsg(0x123));
                            return;
                        }
                    }

                    /*
                    if(w_nEnrollStep == 0)
            		{
            			if (w_nGenCount == 3)
            				m_strPost = "Two More";
            			else
            				m_strPost = "One More";
            		}
            		else if(w_nEnrollStep == 1)
            			m_strPost = "One More";

                    m_FpImageViewer.post(runShowStatus);
                    */

                    w_nEnrollStep++;
                }

                //m_strPost = "Release Finger";
                //m_FpImageViewer.post(runShowStatus);

                // Merge
                if (w_nGenCount != 1) {
                    //. Merge Template
                    w_nRet = m_usbComm.Run_Merge(0, w_nGenCount);

                    if (w_nRet != DevComm.ERR_SUCCESS) {
                        m_strPost = GetErrorMsg(w_nRet);
                        handler.sendMessage(getMsg(0x123));
                        return;
                    }
                }

                //. Store template
                w_nRet = m_usbComm.Run_StoreChar(w_nUserID, 0, w_nDupID);

                if (w_nRet != DevComm.ERR_SUCCESS) {
                    if (w_nRet == DevComm.ERR_DUPLICATION_ID)
                        m_strPost = String.format("指纹模板已存在，点我重试", w_nDupID[0]);
                    else
                        m_strPost = GetErrorMsg(w_nRet);
                } else
                    m_strPost = String.format("指纹ID  %d  录入成功", m_nUserID);
                handler.sendMessage(getMsg(0x123));
            }
        }).start();
    }


    @Override
    public void onVerify(String id, IFPListener listener) {
        int w_nRet;
        int[] w_nState = new int[1];

        if (!m_usbComm.IsInit())
            return;

        if (!CheckUserID(id))
            return;

        w_nRet = m_usbComm.Run_GetStatus(m_nUserID, w_nState);

        if (w_nRet != DevComm.ERR_SUCCESS) {
            listener.onText(GetErrorMsg(w_nRet));
            return;
        }

        if (w_nState[0] == DevComm.GD_TEMPLATE_EMPTY) {
            listener.onText("Template is empty");
            return;
        }

        listener.onText("Press finger");

        m_usbComm.Run_SLEDControl(1);
        m_bCancel = false;

        new Thread(new Runnable() {
            int w_nRet;
            int[] w_nLearned = new int[1];
            int[] w_nWidth = new int[1];
            int[] w_nHeight = new int[1];

            @Override
            public void run() {

                if (Capturing() < 0)
                    return;

                m_strPost = "Release your finger.";
                handler.sendMessage(getMsg(0x234));


                // Up Cpatured Image
                w_nRet = m_usbComm.Run_UpImage(0, m_binImage, w_nWidth, w_nHeight);

                if (w_nRet != DevComm.ERR_SUCCESS) {
                    m_strPost = GetErrorMsg(w_nRet);
                    handler.sendMessage(getMsg(0x123));
                    return;
                }

                // Draw image
                m_nImgWidth = w_nWidth[0];
                m_nImgHeight = w_nHeight[0];
                handler.sendMessage(getMsg(0x345));

                // Create template
                m_nPassedTime = SystemClock.elapsedRealtime();
                w_nRet = m_usbComm.Run_Generate(0);

                if (w_nRet != DevComm.ERR_SUCCESS) {
                    m_strPost = GetErrorMsg(w_nRet);
                    handler.sendMessage(getMsg(0x123));
                    return;
                }

                // Verify
                w_nRet = m_usbComm.Run_Verify(m_nUserID, 0, w_nLearned);
                m_nPassedTime = SystemClock.elapsedRealtime() - m_nPassedTime;

                if (w_nRet == DevComm.ERR_SUCCESS)
                    m_strPost = String.format("Result : Success\r\nTemplate No : %d, Learn Result : %d\r\nMatch Time : %dms", m_nUserID, w_nLearned[0], m_nPassedTime);
                else
                    m_strPost = GetErrorMsg(w_nRet);

                handler.sendMessage(getMsg(0x123));
            }
        }).start();
    }

    @Override
    public void onIdentify(IFPListener listener) {

        if (!m_usbComm.IsInit())
            return;
        m_usbComm.Run_SLEDControl(1);
        m_bCancel = false;

        m_strPost = "";

        new Thread(new Runnable() {
            int w_nRet;
            int[] w_nID = new int[1];
            int[] w_nLearned = new int[1];
            int[] w_nWidth = new int[1];
            int[] w_nHeight = new int[1];

            @Override
            public void run() {

                while (true) {
                    if (m_strPost.isEmpty())
                        m_strPost = "Input your finger.";
                    else
                        //m_strPost = m_strPost + "\r\nInput your finger.";
                        handler.sendMessage(getMsg(0x234));

                    if (Capturing() < 0)
                        return;

                    m_strPost = "松开手指";
                    handler.sendMessage(getMsg(0x234));

                    // Up Cpatured Image
                    w_nRet = m_usbComm.Run_UpImage(0, m_binImage, w_nWidth, w_nHeight);

                    if (w_nRet != DevComm.ERR_SUCCESS) {
                        m_strPost = GetErrorMsg(w_nRet);
                        handler.sendMessage(getMsg(0x123));
                        return;
                    }

                    // Draw image
                    m_nImgWidth = w_nWidth[0];
                    m_nImgHeight = w_nHeight[0];
                    handler.sendMessage(getMsg(0x345));

                    // Create template
                    m_nPassedTime = SystemClock.elapsedRealtime();
                    w_nRet = m_usbComm.Run_Generate(0);

                    if (w_nRet != DevComm.ERR_SUCCESS) {
                        m_strPost = GetErrorMsg(w_nRet);
                        handler.sendMessage(getMsg(0x234));

                        if (w_nRet == DevComm.ERR_CONNECTION)
                            return;
                        else {
                            SystemClock.sleep(1000);
                            continue;
                        }
                    }

                    // Identify
                    w_nRet = m_usbComm.Run_Search(0, 1, m_nMaxFpCount, w_nID, w_nLearned);
                    m_nPassedTime = SystemClock.elapsedRealtime() - m_nPassedTime;

                    if (w_nRet == DevComm.ERR_SUCCESS) {
                        m_strPost = String.format("TAG%d", w_nID[0]);
                        handler.sendMessage(getMsg(0x456));
                    } else {
                        //m_strPost = String.format("\r\nMatch Time : %dms", m_nPassedTime);
                        //m_strPost = GetErrorMsg(w_nRet) + m_strPost;
                        m_strPost = GetErrorMsg(w_nRet);
                    }
                }
            }
        }).start();

    }

    @Override
    public void onGetEnrollCount(IFPListener listener) {
        int w_nRet;
        int[] w_nEnrollCount = new int[1];

        if (!m_usbComm.IsInit())
            return;

        w_nRet = m_usbComm.Run_GetEnrollCount(1, m_nMaxFpCount, w_nEnrollCount);

        if (w_nRet != DevComm.ERR_SUCCESS) {
            listener.onText(GetErrorMsg(w_nRet));
            return;
        }

        listener.onText(String.format("Result : Success\r\nEnroll Count = %d", w_nEnrollCount[0]));
    }

    @Override
    public int onGetEmptyID(IFPListener listener) {
        int w_nRet;
        int[] w_nEmptyID = new int[1];

        if (!m_usbComm.IsInit())
            return 0;

        w_nRet = m_usbComm.Run_GetEmptyID(1, m_nMaxFpCount, w_nEmptyID);

        if (w_nRet != DevComm.ERR_SUCCESS) {
            listener.onText(GetErrorMsg(w_nRet));
            return 0;
        }
        person_Id = w_nEmptyID[0];
        return w_nEmptyID[0];
    }

    @Override
    public void onCaptureImg(final IFPListener listener) {

        m_usbComm.Run_SLEDControl(1);
        m_bCancel = false;
        listener.onText("Input finger!");
        new Thread(new Runnable() {
            int w_nRet;
            int[] width = new int[1];
            int[] height = new int[1];

            @Override
            public void run() {
                while (true) {
                    if (Capturing() < 0) {
                        return;
                    }
                    if (m_usbComm != null) {
                        w_nRet = m_usbComm.Run_UpImage(0, m_binImage, width, height);
                    }
                    if (w_nRet != DevComm.ERR_SUCCESS) {
                        m_strPost = GetErrorMsg(w_nRet);
                        handler.sendMessage(getMsg(0x123));
                        return;
                    }

                    m_nImgWidth = width[0];
                    m_nImgHeight = height[0];
                    m_strPost = "Get Image OK !";
                    handler.sendMessage(getMsg(0x345));

                }
            }
        }).start();
    }

    @Override
    public void onRemoveTmpl(String TmplId, IFPListener listener) {
        int w_nRet;

        if (!m_usbComm.IsInit())
            return;

        if (!CheckUserID(TmplId))
            return;

        w_nRet = m_usbComm.Run_DelChar(m_nUserID, m_nUserID);

        if (w_nRet != DevComm.ERR_SUCCESS) {
            listener.onText(GetErrorMsg(w_nRet));
            return;
        }

        listener.onText("Delete OK !");
    }

    @Override
    public void onRemoveAll(IFPListener listener) {
        int w_nRet;

        if (!m_usbComm.IsInit())
            return;

        w_nRet = m_usbComm.Run_DelChar(1, m_nMaxFpCount);

        if (w_nRet != DevComm.ERR_SUCCESS) {
            listener.onText(GetErrorMsg(w_nRet));
            return;
        }
        listener.onText("Delete all OK !");
    }


    @Override
    public String onUpTemplate(String id, IFPListener listener) {
        int w_nRet;
        byte[] w_pTemplate = new byte[DevComm.GD_RECORD_SIZE];

        // Check USB Connection
        if (!m_usbComm.IsInit())
            return null;

        // Check User ID
        if (!CheckUserID(id))
            return null;

        do {


            // Load Template to Buffer
            w_nRet = m_usbComm.Run_LoadChar((short) m_nUserID, 0);
            if (w_nRet != DevComm.ERR_SUCCESS) {
                listener.onText(GetErrorMsg(w_nRet));
                break;
            }

            // Up Template
            w_nRet = m_usbComm.Run_UpChar(0, w_pTemplate);
            if (w_nRet != DevComm.ERR_SUCCESS) {
                listener.onText(GetErrorMsg(w_nRet));
                break;
            }
            temp = Base64.encodeToString(w_pTemplate, Base64.DEFAULT);


            ////////////////////////////////////////////////////////////////////
            // Save Template (/FPData/01.fpt)
            // Create Directory
            String w_szSaveDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FPData";
            File w_fpDir = new File(w_szSaveDirPath);
            if (!w_fpDir.exists())
                w_fpDir.mkdirs();

            // Create Template File
            File w_fpTemplate = new File(w_szSaveDirPath + "/" + String.valueOf(m_nUserID) + ".fpt");
            if (!w_fpTemplate.exists()) {
                try {
                    w_fpTemplate.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            // Save Template Data
            FileOutputStream w_foTemplate = null;

            try {
                w_foTemplate = new FileOutputStream(w_fpTemplate);
                w_foTemplate.write(w_pTemplate, 0, DevComm.GD_RECORD_SIZE);
                w_foTemplate.close();
                // Show Status
                //   listener.onText(String.format("Result : Get Template Success.\r\nDir : %s", w_szSaveDirPath + "/" + String.valueOf(m_nUserID) + ".fpt"));
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            ////////////////////////////////////////////////////////////////////

            //listener.onText(String.format("Result : Get Template Success.\r\nSave Template Failed."));
        } while (false);

        return temp;
    }

    @Override
    public void onDownTemplate(String id,String temp, IFPListener listener) {
        int w_nRet;
        int[] w_nDupTmplNo = new int[1];
        byte[] w_pTemplate = new byte[DevComm.GD_RECORD_SIZE];

        // Check USB Connection
        if (!m_usbComm.IsInit())
            return;

        // Check User ID
        if (!CheckUserID(id))
            return;

        ////////////////////////////////////////////////////////////////////
        // Load Template (/FPData/01.fpt)
        // Check Directory
        /*String w_szLoadDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FPData";
        File w_fpDir = new File(w_szLoadDirPath);
        if (!w_fpDir.exists()) {
            listener.onText(String.format("Result : Can't load template data.\r\nDir : %s", w_szLoadDirPath + "/" + String.valueOf(m_nUserID) + ".fpt"));
            return;
        }

        // Check Template File
        File w_fpTemplate = new File(w_szLoadDirPath + "/" + String.valueOf(m_nUserID) + ".fpt");
        if (!w_fpTemplate.exists()) {
            listener.onText(String.format("Result : Can't load template data.\r\nDir : %s", w_szLoadDirPath + "/" + String.valueOf(m_nUserID) + ".fpt"));
            return;
        }*/

        /*// Load Template Data
        FileInputStream w_fiTemplate = null;
        try {
            w_fiTemplate = new FileInputStream(w_fpTemplate);
            w_fiTemplate.read(w_pTemplate, 0, DevComm.GD_RECORD_SIZE);
            w_fiTemplate.close();
        } catch (Exception e) {
            e.printStackTrace();
            listener.onText(String.format("Result : Can't load template data.\r\nDir : %s", w_szLoadDirPath + "/" + String.valueOf(m_nUserID) + ".fpt"));
            return;
        }*/
        ////////////////////////////////////////////////////////////////////
        w_pTemplate = Base64.decode(temp,Base64.DEFAULT);
        do {
            // Download Template to Buffer
            w_nRet = m_usbComm.Run_DownChar(0, w_pTemplate);
            if (w_nRet != DevComm.ERR_SUCCESS) {
                listener.onText(GetErrorMsg(w_nRet));
                break;
            }

            // Store Template
            w_nRet = m_usbComm.Run_StoreChar(m_nUserID, 0, w_nDupTmplNo);
            if (w_nRet != DevComm.ERR_SUCCESS) {
                if (w_nRet == DevComm.ERR_DUPLICATION_ID) {
                    listener.onText(String.format("Result : Fail\r\nDuplication ID = %d", w_nDupTmplNo[0]));
                } else {
                    listener.onText(GetErrorMsg(w_nRet));
                }
                break;
            }

            listener.onText(String.format("Result : Set Template Success.\r\nUserID = %d", m_nUserID));
        } while (false);
    }


    @Override
    public void sync(String dataList){

    }



    private final IUsbConnState m_IConnectionHandler = new IUsbConnState() {
        @Override
        public void onUsbConnected() {
            String[] w_strInfo = new String[1];

            if (m_usbComm.Run_TestConnection() == DevComm.ERR_SUCCESS) {
                if (m_usbComm.Run_GetDeviceInfo(w_strInfo) == DevComm.ERR_SUCCESS) {
                    listener.onText("Open Success!\r\nDevice Info : " + w_strInfo[0]);
                }
            }
        }

        @Override
        public void onUsbPermissionDenied() {
            listener.onText("Permission denied!");
        }

        @Override
        public void onDeviceNotFound() {
            listener.onText("Can not find usb device!");
        }
    };




    private String GetErrorMsg(int nErrorCode) {
        String str = new String("");

        switch (nErrorCode) {
            case DevComm.ERR_SUCCESS:
                str = "Succcess";
                break;
            case DevComm.ERR_VERIFY:
                str = "Verify NG";
                break;
            case DevComm.ERR_IDENTIFY:
                str = "请确认指纹是否已登记";
                break;
            case DevComm.ERR_EMPTY_ID_NOEXIST:
                str = "Empty Template no Exist";
                break;
            case DevComm.ERR_BROKEN_ID_NOEXIST:
                str = "Broken Template no Exist";
                break;
            case DevComm.ERR_TMPL_NOT_EMPTY:
                str = "Template of this ID Already Exist";
                break;
            case DevComm.ERR_TMPL_EMPTY:
                str = "This Template is Already Empty";
                break;
            case DevComm.ERR_INVALID_TMPL_NO:
                str = "Invalid Template No";
                break;
            case DevComm.ERR_ALL_TMPL_EMPTY:
                str = "请确认指纹是否已登记";
                break;
            case DevComm.ERR_INVALID_TMPL_DATA:
                str = "Invalid Template Data";
                break;
            case DevComm.ERR_DUPLICATION_ID:
                str = "已有重复指纹ID : ";
                break;
            case DevComm.ERR_BAD_QUALITY:
                str = "指纹图片质量差";
                break;
            case DevComm.ERR_MERGE_FAIL:
                str = "指纹合成失败，点我重试";
                break;
            case DevComm.ERR_NOT_AUTHORIZED:
                str = "Device not authorized.";
                break;
            case DevComm.ERR_MEMORY:
                str = "Memory Error ";
                break;
            case DevComm.ERR_INVALID_PARAM:
                str = "Invalid Parameter";
                break;
            case DevComm.ERR_GEN_COUNT:
                str = "Generation Count is invalid";
                break;
            case DevComm.ERR_INVALID_BUFFER_ID:
                str = "Ram Buffer ID is invalid.";
                break;
            case DevComm.ERR_INVALID_OPERATION_MODE:
                str = "Invalid Operation Mode!";
                break;
            case DevComm.ERR_FP_NOT_DETECTED:
                str = "Finger is not detected.";
                break;
            default:
                str = String.format("Fail, error code=%d", nErrorCode);
                break;
        }

        return str;
    }

    private boolean CheckUserID(String TmplId) {
        if (TmplId == "") {
            listener.onText("Please input user id");
            return false;
        }

        try {
            m_nUserID = Integer.parseInt(TmplId);
        } catch (NumberFormatException e) {
            listener.onText("Please input correct user id(1~" + m_nMaxFpCount + ")");
            return false;
        }

        if (m_nUserID > (m_nMaxFpCount) || m_nUserID < 1) {
            listener.onText("Please input correct user id(1~" + m_nMaxFpCount + ")");
            return false;
        }

        return true;
    }

    private void GetConCaptureState() {

    }

    private int Capturing() {
        int w_nRet;
        while (true) {

            w_nRet = m_usbComm.Run_GetImage();

            if (w_nRet == DevComm.ERR_CONNECTION) {
                m_strPost = "Communication error!";
                handler.sendMessage(getMsg(0x123));
                return -1;
            } else if (w_nRet == DevComm.ERR_SUCCESS)
                break;

            if (m_bCancel) {
                StopOperation();
                return -1;
            }
        }

        return 0;
    }

    private void StopOperation() {
        m_strPost = "Canceled";
        handler.sendMessage(getMsg(0x123));
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x123) {
                listener.onText(m_strPost);
                m_usbComm.Run_SLEDControl(0);
            } else if (msg.what == 0x234) {
                listener.onText(m_strPost);
            } else if (msg.what == 0x345) {
                listener.onText(m_strPost);
                int nSize;
                MakeBMPBuf(m_binImage, m_bmpImage, m_nImgWidth, m_nImgHeight);
                if ((m_nImgWidth % 4) != 0)
                    nSize = m_nImgWidth + (4 - (m_nImgWidth % 4));
                else
                    nSize = m_nImgWidth;

                nSize = 1078 + nSize * m_nImgHeight;

                //DebugManage.WriteBmp(m_bmpImage, nSize);
                Bitmap image = BitmapFactory.decodeByteArray(m_bmpImage, 0, nSize);
                listener.onSetImg(image);
            } else if (msg.what == 0x456) {
                listener.onFpSucc(m_strPost);
            }
        }
    };

    private void MakeBMPBuf(byte[] Input, byte[] Output, int iImageX, int iImageY) {

        byte[] w_bTemp = new byte[4];
        byte[] head = new byte[1078];
        byte[] head2 = {
                /***************************/
                //file header
                0x42, 0x4d,//file type
                //0x36,0x6c,0x01,0x00, //file size***
                0x0, 0x0, 0x0, 0x00, //file size***
                0x00, 0x00, //reserved
                0x00, 0x00,//reserved
                0x36, 0x4, 0x00, 0x00,//head byte***
                /***************************/
                //infoheader
                0x28, 0x00, 0x00, 0x00,//struct size

                //0x00,0x01,0x00,0x00,//map width***
                0x00, 0x00, 0x0, 0x00,//map width***
                //0x68,0x01,0x00,0x00,//map height***
                0x00, 0x00, 0x00, 0x00,//map height***

                0x01, 0x00,//must be 1
                0x08, 0x00,//color count***
                0x00, 0x00, 0x00, 0x00, //compression
                //0x00,0x68,0x01,0x00,//data size***
                0x00, 0x00, 0x00, 0x00,//data size***
                0x00, 0x00, 0x00, 0x00, //dpix
                0x00, 0x00, 0x00, 0x00, //dpiy
                0x00, 0x00, 0x00, 0x00,//color used
                0x00, 0x00, 0x00, 0x00,//color important
        };

        int i, j, num, iImageStep;

        Arrays.fill(w_bTemp, (byte) 0);

        System.arraycopy(head2, 0, head, 0, head2.length);

        if ((iImageX % 4) != 0)
            iImageStep = iImageX + (4 - (iImageX % 4));
        else
            iImageStep = iImageX;

        num = iImageX;
        head[18] = (byte) (num & (byte) 0xFF);
        num = num >> 8;
        head[19] = (byte) (num & (byte) 0xFF);
        num = num >> 8;
        head[20] = (byte) (num & (byte) 0xFF);
        num = num >> 8;
        head[21] = (byte) (num & (byte) 0xFF);

        num = iImageY;
        head[22] = (byte) (num & (byte) 0xFF);
        num = num >> 8;
        head[23] = (byte) (num & (byte) 0xFF);
        num = num >> 8;
        head[24] = (byte) (num & (byte) 0xFF);
        num = num >> 8;
        head[25] = (byte) (num & (byte) 0xFF);

        j = 0;
        for (i = 54; i < 1078; i = i + 4) {
            head[i] = head[i + 1] = head[i + 2] = (byte) j;
            head[i + 3] = 0;
            j++;
        }

        System.arraycopy(head, 0, Output, 0, 1078);

        if (iImageStep == iImageX) {
            for (i = 0; i < iImageY; i++) {
                System.arraycopy(Input, i * iImageX, Output, 1078 + i * iImageX, iImageX);
            }
        } else {
            iImageStep = iImageStep - iImageX;

            for (i = 0; i < iImageY; i++) {
                System.arraycopy(Input, i * iImageX, Output, 1078 + i * (iImageX + iImageStep), iImageX);
                System.arraycopy(w_bTemp, 0, Output, 1078 + i * (iImageX + iImageStep) + iImageX, iImageStep);
            }
        }
    }

    private Message getMsg(int what) {
        Message msg = new Message();
        msg.what = what;
        return msg;
    }
}
