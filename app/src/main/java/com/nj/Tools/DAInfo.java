package com.nj.Tools;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import static android.graphics.Color.BLACK;

/**
 * Created by Administrator on 2017-12-05.
 */

class DAInfoPosition{
    public static final int bmpWidth = 0;
    public static final int bmpHeight = 1;
    public static final int QRCodeSize = 2;
    public static final int textSize = 3;
    public static final int QRCodePositionX = 4;
    public static final int QRCodePositionY = 5;
    public static final int namePositionX = 6;
    public static final int namePositionY = 7;
    public static final int powerPositionX = 8;
    public static final int powerPositionY = 9;
    public static final int modelPositionX = 10;
    public static final int modelPositionY = 11;
    public static final int macidPositionX = 12;
    public static final int macidPositionY = 13;
}


public class DAInfo {
    private String name_="网络数据采集仪";
    private String project_="GDYL";
    private String model_="CBDI-RK3368";
    private String macid_=""; //设备ID
    private String power_="12V 2A";
    private String ver_="V1.0"; //版本号
    private String  date_="";
    private String data_="";
    private String labelType_="DE";
    private String id_="";
    private String softwareVer_="";

    public String getSoftwareVer() {
        return softwareVer_;
    }

    public void setSoftwareVer(String softwareVer) {
        this.softwareVer_ = softwareVer;
    }



    public String getId() {
        return id_;
    }

    public void setId(String id_) {
        this.id_ = id_;
    }

    public String getLabelType() {
        return labelType_;
    }

    public void setLabelType(String labelType) {
        this.labelType_ = labelType;
    }

    public DAInfo()
    {
        NetInfo ni=new NetInfo();
        macid_=ni.getMacId();
    }



    public String getData() {
        return data_;
    }

    public void setData(String data_) {
        this.data_ = data_;
    }

    public String getModel() {
        return model_;
    }

    public String getPower() {
        return power_;
    }

    public String getVer() {
        return ver_;
    }

    public void setVer(String ver_) {
        this.ver_ = ver_;
    }

    public void setPower(String power_) {
        this.power_ = power_;
    }

    public void setModel(String model_) {
        this.model_ = model_;
    }

    public String getProject() {
        return project_;
    }

    public void setProject(String project_) {
        this.project_ = project_;
    }

    public String getMacid() {
        return macid_;
    }

    public void setMacid(String macid_) {
        this.macid_ = macid_;
    }

    public String getName() {
        return name_;
    }

    public void setName(String name_) {
        this.name_ = name_;
    }

    public  Bitmap createQRCode(String str, int widthAndHeight)
            throws WriterException {
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        BitMatrix matrix = new MultiFormatWriter().encode(str,
                BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight);

        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        //画黑点
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = BLACK; //0xff000000
                }else
                {
                    pixels[y * width + x] = Color.WHITE; //0xFFFFFFFF
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }


    public void setDate()
    {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
         date_=formatter.format(currentTime);
    }
    //width,height,QRCodeSize,textSize,QRCodePositionX,QRCodePositionY,namePositionX,namePositionY,powerPositionX,powerPositionY,modelPositionX,modelPositionY,macidPositionX,macidPositionY
    private  int[] infoPosition_ ={560,320,240,32,325,10,40,75,40,135,40,195,40,255};

    public boolean setInfoPosition(int pos,int value)
    {
        if(pos<0||pos>13)
        {
            infoPosition_[pos]=value;
            return true;
        }else
        {
            return false;
        }
    }



    public boolean setInfoPosition(int pos,int x,int y)
    {
        if(pos<0||pos>12)
        {
            infoPosition_[pos]=x;
            infoPosition_[pos+1]=y;
            return true;
        }else
        {
            return false;
        }
    }



    public  Bitmap daInfoBmp()
    {
        int width =infoPosition_[0];
        int height =infoPosition_[1];
        Bitmap qr=null;

        try {
            if(labelType_!=null&&labelType_.equals("PR")) {
                data_ = macid_ + ";" + model_ + ";" + ver_ + ";" + project_ + ";" + power_ + ";" + labelType_ + ";" + date_+";"+id_;
                qr = createQRCode(macid_ + ";" + model_ + ";" + ver_ + ";" + project_ + ";" + power_ + ";" + labelType_ + ";" + date_+";"+id_, infoPosition_[2]);
            }else
            {
                data_ = macid_ + ";" + model_ + ";" + ver_ + ";" + project_ + ";" + power_ + ";" + labelType_+";"+softwareVer_+";"+id_;
                qr = createQRCode(macid_ + ";" + model_ + ";" + ver_ + ";" + project_ + ";" + power_ + ";" + labelType_+";"+softwareVer_+";"+id_, infoPosition_[2]);
            }
        }catch (Exception ex){}
        if(qr==null){return null;}
        //创建一个bitmap
        Bitmap newb = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        //将该图片作为画布
        newb.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(newb);
        //canvas.
        //在画布 0，0坐标上开始绘制原始图片
        canvas.drawBitmap(qr,infoPosition_[4],infoPosition_[5], null);

        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(infoPosition_[3]);

        canvas.drawText("名称: "+name_,infoPosition_[6],infoPosition_[7], paint);

        if(id_!=null&&id_.equals(""))
        {
            canvas.drawText("序号: "+macid_,infoPosition_[12],infoPosition_[13], paint);
        }else
        {
            canvas.drawText("序号: "+id_,infoPosition_[12],infoPosition_[13], paint);
        }

        if(softwareVer_!=null&&(!softwareVer_.equals("")))
        {

            canvas.drawText("版本: "+softwareVer_,infoPosition_[12],infoPosition_[7]+(infoPosition_[13]-infoPosition_[7])/4, paint);
            canvas.drawText("电源: "+power_,infoPosition_[8],infoPosition_[7]+(infoPosition_[13]-infoPosition_[7])*2/4, paint);
            canvas.drawText("型号: "+model_,infoPosition_[10],infoPosition_[7]+(infoPosition_[13]-infoPosition_[7])*3/4, paint);
        }else
        {
            canvas.drawText("电源: "+power_,infoPosition_[8],infoPosition_[9], paint);
            canvas.drawText("型号: "+model_,infoPosition_[10],infoPosition_[11], paint);
        }




        //在画布上绘制水印图片
        // 保存
        canvas.save();
        // 存储
        canvas.restore();
        return newb;
    }



    public  Bitmap daInfoBmp60()
    {
        infoPosition_[DAInfoPosition.bmpWidth]=480;
        infoPosition_[DAInfoPosition.bmpHeight]=320;
        infoPosition_[DAInfoPosition.QRCodeSize]=200;
        infoPosition_[DAInfoPosition.textSize]=26;
        infoPosition_[DAInfoPosition.QRCodePositionX]=260;
        infoPosition_[DAInfoPosition.QRCodePositionY]=20;

        infoPosition_[DAInfoPosition.namePositionX]=30;
        infoPosition_[DAInfoPosition.namePositionY]=135;
        infoPosition_[DAInfoPosition.powerPositionX]=30;
        infoPosition_[DAInfoPosition.powerPositionY]=135;
        infoPosition_[DAInfoPosition.modelPositionX]=30;
        infoPosition_[DAInfoPosition.modelPositionY]=195;
        infoPosition_[DAInfoPosition.macidPositionX]=30;
        infoPosition_[DAInfoPosition.macidPositionY]=255;

        return daInfoBmp();

    }

}
