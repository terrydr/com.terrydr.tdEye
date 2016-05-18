package com.terrydr.eyeScope;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera.Size;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;

public class ImageAsyncTask extends AsyncTask<Bitmap, Void, Bitmap> {
    public static final String TAG = "ImageAsyncTask";

    Context mContext = null;
    CameraView mCameraPreview;
    CameraContainer mCameraContainer;
    byte[] mData;
    String fileNamePath;
    Size mSize;
    String thumbPath;
    

    ImageAsyncTask(CameraContainer mCameraContainer,CameraView camera, byte[] data,Size size,String filePath,String _thumbPath){
        mData = data;
        this.mCameraContainer = mCameraContainer;
        mCameraPreview = camera;
        fileNamePath = filePath;
        thumbPath = _thumbPath;
        mSize = size;
    }
    
    @Override
    protected Bitmap doInBackground(Bitmap... bmp) {
//    	Bitmap retBmp = BitmapFactory.decodeByteArray(mData, 0, mData.length);
        Bitmap retBmp;
        final int width = mSize.width;
        final int height = mSize.height;          
        int[] rgb = new int[(width * height)];
        decodeYUV420SP(rgb, mData, width, height);
        bmp[0].setPixels(rgb, 0, width, 0, 0, width, height);
        rgb = null;
        
        Matrix matrix = new Matrix();
//        int degree = ((ContShooting)mContext).getDegree();
        int degree = mCameraPreview.mOrientation;
        //matrix.postRotate(90.0f);
        matrix.postRotate(degree + 90);
        retBmp = Bitmap.createBitmap(bmp[0], 0, 0, bmp[0].getWidth(), bmp[0].getHeight(), matrix, true);
//        retBmp = Bitmap.createBitmap(bmp[0], 0, 0, width, height, matrix, true);
        bmp[0].recycle();
        bmp[0] = null;
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        retBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);

        savedata(out.toByteArray(),fileNamePath);
        
//        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(retBmp, 213, 213);
        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(retBmp, 320, 219);
     // 存图片小
		BufferedOutputStream bufferos = null;
		try {
			bufferos = new BufferedOutputStream(
					new FileOutputStream(thumbPath));
			thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, bufferos);
			bufferos.flush();
			bufferos.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException in savedata",e);
		} catch (IOException e) {
			Log.e(TAG, "IOException in savedata",e);
		}
		thumbnail.recycle();
        retBmp.recycle();

        return retBmp;
    }

    @Override
    protected void onPostExecute(Bitmap bmp) {
//    	mCameraContainer.count();
    	mCameraContainer.countShoot();
    	
    }
    
    // YUV420 to BMP 
    public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) { 
        final int frameSize = width * height; 

        for (int j = 0, yp = 0; j < height; j++) { 
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0; 
            for (int i = 0; i < width; i++, yp++) { 
                int y = (0xff & ((int) yuv420sp[yp])) - 16; 
                if (y < 0) y = 0; 
                if ((i & 1) == 0) { 
                        v = (0xff & yuv420sp[uvp++]) - 128; 
                        u = (0xff & yuv420sp[uvp++]) - 128; 
                } 

                int y1192 = 1192 * y; 
                int r = (y1192 + 1634 * v); 
                int g = (y1192 - 833 * v - 400 * u); 
                int b = (y1192 + 2066 * u); 

                if (r < 0) r = 0; else if (r > 262143) r = 262143; 
                if (g < 0) g = 0; else if (g > 262143) g = 262143; 
                if (b < 0) b = 0; else if (b > 262143) b = 262143; 

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff); 
            } 
        }
    }
    
    public void savedata(byte[] data,String filePath){
//
        FileOutputStream fos = null;
        File savefile = null;

        try{
            savefile = new File(filePath);
            fos = new FileOutputStream(savefile);
            fos.write(data);
            fos.flush();
            fos.close();
        }catch(IOException e){
            //Log.e(TAG, "IOException in savedata");
            if(fos != null){
                try {
                    fos.close();
                } catch (IOException e1) {
                    //do nothing
                }
            }
            return;
        }
    }
}
