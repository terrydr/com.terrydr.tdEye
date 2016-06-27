package com.terrydr.eyeScope;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/** 
* @ClassName: MatrixBitmapDisplayer 
* @Description:  Matrix效果的BitmapDisplayer
* @date 20160419
*  
*/
public class MatrixBitmapDisplayer implements BitmapDisplayer {
	
	public MatrixBitmapDisplayer() {
		
	}

	@Override
	public void display(Bitmap bitmap, ImageView imageView) {
		//正常的图片设置为FIT_CENTER
		imageView.setScaleType(ScaleType.FIT_CENTER);
		imageView.setImageBitmap(bitmap);
	}

	@Override
	public void display(int resouceID, ImageView imageView) {
		// 加载前和出错的的图片不自动拉
		imageView.setScaleType(ScaleType.CENTER);
		imageView.setImageResource(resouceID);
	}
}
