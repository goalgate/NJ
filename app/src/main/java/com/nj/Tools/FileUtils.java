/**
 * 
 */
package com.nj.Tools;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.MediaColumns;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zhaoyang 2015-8-19
 */
public class FileUtils {

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	/**
	 * Try to return the absolute file path from the given Uri
	 * 
	 * @param context
	 * @param uri
	 * @return the file path or null
	 */
	public static String getImgRealFilePath(final Context context, final Uri uri) {
		if (null == uri)
			return null;
		final String scheme = uri.getScheme();
		String data = null;
		if (scheme == null)
			data = uri.getPath();
		else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			Cursor cursor = context.getContentResolver().query(uri,
					new String[] { MediaColumns.DATA }, null, null, null);
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(MediaColumns.DATA);
					if (index > -1) {
						data = cursor.getString(index);
					}
				}
				cursor.close();
			}
		}
		return data;
	}

	/**
	 * Try to return the absolute file path from the given Uri IMG
	 * 
	 * @param context
	 * @param uri
	 * @return the file path or null
	 */
	public static String getVideoRealFilePath(final Context context,
			final Uri uri) {
		if (null == uri)
			return null;
		final String scheme = uri.getScheme();
		String data = null;
		if (scheme == null)
			data = uri.getPath();
		else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			Cursor cursor = context.getContentResolver().query(uri,
					new String[] { MediaColumns.DATA }, null, null, null);
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(MediaColumns.DATA);
					if (index > -1) {
						data = cursor.getString(index);
					}
				}
				cursor.close();
			}
		}
		return data;
	}

	/*
	 * public static Uri getImgUri(final Context context){ String type =
	 * Utils.ensureNotNull(intent.getType()); Log.d(TAG, "uri is " + uri); if
	 * (uri.getScheme().equals("file") && (type.contains("image/"))) { String
	 * path = uri.getEncodedPath(); Log.d(TAG, "path1 is " + path); if (path !=
	 * null) { path = Uri.decode(path); Log.d(TAG, "path2 is " + path);
	 * ContentResolver cr = this.getContentResolver(); StringBuffer buff = new
	 * StringBuffer(); buff.append("(") .append(Images.ImageColumns.DATA)
	 * .append("=") .append("'" + path + "'") .append(")"); Cursor cur =
	 * cr.query( Images.Media.EXTERNAL_CONTENT_URI, new String[] {
	 * Images.ImageColumns._ID }, buff.toString(), null, null); int index = 0;
	 * for (cur.moveToFirst(); !cur.isAfterLast(); cur .moveToNext()) { index =
	 * cur.getColumnIndex(Images.ImageColumns._ID); // set _id value index =
	 * cur.getInt(index); } if (index == 0) { //do nothing } else { Uri uri_temp
	 * = Uri .parse("content://media/external/images/media/" + index);
	 * Log.d(TAG, "uri_temp is " + uri_temp); if (uri_temp != null) { uri =
	 * uri_temp; } } } } }
	 */
	/*
	 * N.1.返回Uri
	 */

	/** Create a file Uri for saving an image or video */
	public static Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	@SuppressLint("SimpleDateFormat")
	public static File getOutputMediaFile(int type) {

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"SZ_devices");
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("SZ_devices", "创建目录失败");
				return null;
			}
		}
		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}
		Log.i("SZ_devices", "PATH=" + mediaFile.toString());
		return mediaFile;
	}

	/*
	 * 压缩图片
	 */
	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	/**
	 * bitmap转为base64
	 * 
	 * @param bitmap
	 * @return
	 */
	/*public static String bitmapToBase64(Bitmap bitmap) {

		String result = null;
		ByteArrayOutputStream baos = null;
		try {
			if (bitmap != null) {
				baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

				baos.flush();
				baos.close();

				byte[] bitmapBytes = baos.toByteArray();
				result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}*/
	
	public static String bitmapToBase64(Bitmap bitmap) {

		String result = null;
		ByteArrayOutputStream baos = null;
		try {
			if (bitmap != null) {
				
				int ix=bitmap.getWidth();				
				if(ix>1000)
				{
					ix=(bitmap.getHeight()*1000)/bitmap.getWidth();
					Bitmap bmp=ThumbnailUtils.extractThumbnail(bitmap, 1000, ix);					
					baos = new ByteArrayOutputStream();
					bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);	
					baos.flush();
					
				}else
				{				
						baos = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
		
						baos.flush();
						//baos.close();
				}

				byte[] bitmapBytes = baos.toByteArray();
				result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * base64转为bitmap
	 * 
	 * @param base64Data
	 * @return
	 */
	public static Bitmap base64ToBitmap(String base64Data) {
		byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}

	/**
	 * encodeBase64File:(将文件转成base64 字符串). <br/>
	 * 
	 * @param path
	 *            文件路径
	 * @return
	 * @throws Exception
	 * @since JDK 1.6
	 */
	public static String encodeBase64File(String path) throws Exception {
		File file = new File(path);
		FileInputStream inputFile = new FileInputStream(file);
		byte[] buffer = new byte[(int) file.length()];
		inputFile.read(buffer);
		inputFile.close();
		return Base64.encodeToString(buffer, Base64.DEFAULT);
	}

	/**
	 * decoderBase64File:(将base64字符解码保存文件). <br/>
	 * 
	 * @param base64Code
	 *            编码后的字串
	 * @param savePath
	 *            文件保存路径
	 * @throws Exception
	 * @since JDK 1.6
	 */
	public static void decoderBase64File(String base64Code, String savePath)
			throws Exception {
		// byte[] buffer = new BASE64Decoder().decodeBuffer(base64Code);
		byte[] buffer = Base64.decode(base64Code, Base64.DEFAULT);
		FileOutputStream out = new FileOutputStream(savePath);
		out.write(buffer);
		out.close();

	}

	/*
	 * 从路径中生成Bitmap
	 * 
	 * @param oriPath 原始路径
	 * 
	 * @param degree 旋转度数
	 */
	public static Bitmap getBitmapFromPath(String bitmapPath, int degree) {

		Bitmap bmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(bitmapPath, opts);
		opts.inSampleSize = FileUtils.computeSampleSize(opts, -1, 800 * 600);
		// 这里一定要将其设置回false，因为之前我们将其设置成了true
		opts.inJustDecodeBounds = false;
		try {
			bmp = BitmapFactory.decodeFile(bitmapPath, opts); //
		} catch (OutOfMemoryError err) {
		}
		return getRotationOfBitmap(degree, bmp);
	}

	/*
	 * 旋转Bitmap 270"
	 */
	public static Bitmap getRotationOfBitmap(int degree, Bitmap oriBmp) {
		Matrix matrix = new Matrix();
		matrix.reset();
		matrix.setRotate(degree);
		return Bitmap.createBitmap(oriBmp, 0, 0, oriBmp.getWidth(),
				oriBmp.getHeight(), matrix, true);
	}
	
	
}
