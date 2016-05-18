package com.terrydr.eyeScope;

import java.lang.reflect.Field;
import android.graphics.BitmapFactory.Options;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import com.terrydr.eyeScope.R;
/**
 * 
 */
public class ImageSizeUtil {
	/**
	 * 根据求的宽和高以及图片实际的宽和高计算SampleSize
	 * 
	 * @param options
	 * @param width
	 * @param height
	 * @return
	 */
	public static int caculateInSampleSize(Options options, int reqWidth,
			int reqHeight) {
		int width = options.outWidth;
		int height = options.outHeight;

		int inSampleSize = 1;

		if (width > reqWidth || height > reqHeight) {
			int widthRadio = Math.round(width * 1.0f / reqWidth);
			int heightRadio = Math.round(height * 1.0f / reqHeight);

			inSampleSize = Math.max(widthRadio, heightRadio);
		}

		return inSampleSize;
	}

	/**
	 * 根据ImageView获当的压缩的宽和
	 * 
	 * @param imageView
	 * @return
	 */
	public static ImageSize getImageViewSize(ImageView imageView) {

		ImageSize imageSize = new ImageSize();
		DisplayMetrics displayMetrics = imageView.getContext().getResources()
				.getDisplayMetrics();

		LayoutParams lp = imageView.getLayoutParams();

		int width = imageView.getWidth();// 获取imageview的实际宽
		if (width <= 0) {
			width = lp.width;// 获取imageview在layout中声明的宽度
		}
		if (width <= 0) {
			// width = imageView.getMaxWidth();// 查最大
			width = getImageViewFieldValue(imageView, "mMaxWidth");
		}
		if (width <= 0) {
			width = displayMetrics.widthPixels;
		}

		int height = imageView.getHeight();// 获取imageview的实际高
		if (height <= 0) {
			height = lp.height;// 获取imageview在layout中声明的宽度
		}
		if (height <= 0) {
			height = getImageViewFieldValue(imageView, "mMaxHeight");// 查最大
		}
		if (height <= 0) {
			height = displayMetrics.heightPixels;
		}
		imageSize.width = width;
		imageSize.height = height;

		return imageSize;
	}

	public static class ImageSize {
		int width;
		int height;
	}

	/**
	 * 通过反射获取imageview的某个属性
	 * 
	 * @param object
	 * @param fieldName
	 * @return
	 */
	private static int getImageViewFieldValue(Object object, String fieldName) {
		int value = 0;
		try {
			Field field = ImageView.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			int fieldValue = field.getInt(object);
			if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
				value = fieldValue;
			}
		} catch (Exception e) {
		}
		return value;

	}

}
