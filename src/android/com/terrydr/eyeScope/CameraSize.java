package com.terrydr.eyeScope;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

/**
 * 20160506 获取预览图片大小和拍照图片大小
 *
 */
public class CameraSize {

	private static final String TAG = "CameraSize";
	private CameraSizeComparator sizeComparator = new CameraSizeComparator();
	private static CameraSize myCamPara = null;

	private CameraSize() {

	}

	public static CameraSize getInstance() {
		if (myCamPara == null) {
			myCamPara = new CameraSize();
			return myCamPara;
		} else {
			return myCamPara;
		}
	}

	public Size getPreviewSize(List<Camera.Size> list, int th) {
		Collections.sort(list, sizeComparator);
		int i = 0;
		for (Size s : list) {
			if ((s.width > th) && equalRate(s, 1.333f)) {
//				Log.i(tag, "最终设置预览尺寸:w = " + s.width + "h = " + s.height);
				break;
			}
			i++;
		}
		if(i == list.size()){  
            i = 0;//如果没找到，就选最小的size  
        }
		return list.get(i);
	}

	public Size getPictureSize(List<Camera.Size> list, int th) {
		Collections.sort(list, sizeComparator);
		int i = 0;
		for (Size s : list) {
			if ((s.width > th) && equalRate(s, 1.333f)) {
//				Log.i(tag, "最终设置图片尺寸:w = " + s.width + "h = " + s.height);
				break;
			}
			i++;
		}
		if(i == list.size()){  
            i = 0;//如果没找到，就选最小的size  
        }
		return list.get(i);
	}
	
	/**
	 * 默认查找4:3的尺寸,如果不支持4:3的尺寸则查找16:9
	 * @param list
	 * @param th
	 * @param minWidth
	 * @return
	 */
	public  Size getPropPreviewSize(List<Camera.Size> list, float th, int minWidth){  
        Collections.sort(list, sizeComparator);  
        boolean isEmpty = true;
        int i = 0;  
        for(Size s:list){  
            if((s.width >= minWidth) && equalRate1(s, 1.333f)){  
//                Log.i(TAG, "PreviewSize:w = " + s.width + "h = " + s.height);  
            	isEmpty = false;
                break;  
            }  
            i++;  
        }  
        if(isEmpty){  //如果为空则查找16:9的尺寸
        	i = 0;
            for(Size s:list){  
                if((s.width >= minWidth) && equalRate1(s, 1.777f)){  
                	isEmpty = true;
                    break;  
                }  
                i++;  
            } 
        }
        if(i == list.size()){  
            i = 0;//如果没找到，就选最小的size  
        }  
        return list.get(i);  
    }  
	
	/**
	 * 默认查找4:3的尺寸,如果不支持4:3的尺寸则查找16:9
	 * @param list
	 * @param th
	 * @param minWidth
	 * @return
	 */
    public Size getPropPictureSize(List<Camera.Size> list, float th, int minWidth){  
        Collections.sort(list, sizeComparator);  
        boolean isEmpty = true;
        int i = 0;  
        for(Size s:list){  
            if((s.width >= minWidth) && equalRate1(s, 1.333f)){  
//                Log.i(TAG, "PictureSize : w = " + s.width + "h = " + s.height);  
            	isEmpty = false;
                break;  
            }  
            i++;  
        }  
        if(isEmpty){  //如果为空则查找16:9的尺寸
        	i = 0;
            for(Size s:list){  
                if((s.width >= minWidth) && equalRate1(s, 1.777f)){  
                	isEmpty = true;
                    break;  
                }  
                i++;  
            } 
        }
        if(i == list.size()){  
            i = 0;//如果没找到，就选最小的size  
        }  
        return list.get(i);  
    }  
  
    public boolean equalRate1(Size s, float rate){  
        float r = (float)(s.width)/(float)(s.height);  
        if(Math.abs(r - rate) <= 0.03)  
        {  
            return true;  
        }  
        else{  
            return false;  
        }  
    }  

	public boolean equalRate(Size s, float rate) {
		float r = (float) (s.width) / (float) (s.height);
		if (Math.abs(r - rate) <= 0.2) {
			return true;
		} else {
			return false;
		}
	}

	public class CameraSizeComparator implements Comparator<Camera.Size> {
		// 按升序排列
		public int compare(Size lhs, Size rhs) {
			if (lhs.width == rhs.width) {
				return 0;
			} else if (lhs.width > rhs.width) {
				return 1;
			} else {
				return -1;
			}
		}

	}
}
