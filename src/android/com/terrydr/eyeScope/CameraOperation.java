package com.terrydr.eyeScope;

import com.terrydr.eyeScope.CameraContainer.TakePictureListener;
import android.hardware.Camera.PictureCallback;
import com.terrydr.eyeScope.R;

public interface CameraOperation {

	/**  
	 *  拍照
	 *  @param callback 拍照回调函数 
	 *  @param listener 拍照动作监听函数  
	 */
	public void takePicture(PictureCallback callback,TakePictureListener listener);
	
	/**  
	 *  设置爆光
	 *  @return  
	 */
	public void setCameraISO(int iso,boolean lightOn);
}
