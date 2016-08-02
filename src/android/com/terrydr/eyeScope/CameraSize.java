package com.terrydr.eyeScope;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
	public  Size getPropPreviewSize(List<Camera.Size> list, float th, int minWidth,float ratio){  
//        Collections.sort(list, sizeComparator);  
		sortCameraSize(list,false);
        boolean isEmpty = true;
        int i = 0;  
        for(Size s:list){  
//        	Log.e(TAG, "list : w = " + s.width + "*h = " + s.height);  
//            if((s.width >= minWidth) && equalRate1(s, 1.333f)){  
        	if((s.width >= minWidth) && equalRate1(s, ratio)){  
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
    public Size getPropPictureSize(List<Camera.Size> list, float th, int minWidth,float ratio){  
//        Collections.sort(list, sizeComparator);  
    	sortCameraSize(list,false);
        boolean isEmpty = true;
        int i = 0;  
        for(Size s:list){  
//        	Log.e(TAG, "list : w = " + s.width + "*h = " + s.height);  
//            if((s.width >= minWidth) && equalRate1(s, 1.333f)){  
            	if((s.width >= minWidth) && equalRate1(s, ratio)){  
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
        Log.i(TAG, "i = " + i);  
        return list.get(i);  
    }  
    
    
    /**
     * 找到短边比长边大于于所接受的最小比例的最大尺寸
     *
     * @param sizes       支持的尺寸列表
     * @param defaultSize 默认大小
     * @param minRatio    相机图片短边比长边所接受的最小比例
     * @return 返回计算之后的尺寸
     */
    private Camera.Size findBestPictureSize1(List<Camera.Size> sizes, Camera.Size defaultSize, float minRatio) {
        final int MIN_PIXELS = 320 * 480;

//        sortSizes(sizes);
        Collections.sort(sizes, sizeComparator);  

        Iterator<Camera.Size> it = sizes.iterator();
        while (it.hasNext()) {
            Camera.Size size = it.next();
            //移除不满足比例的尺寸
            if ((float) size.height / size.width <= minRatio) {
                it.remove();
                continue;
            }
            //移除太小的尺寸
            if (size.width * size.height < MIN_PIXELS) {
                it.remove();
            }
        }

        // 返回符合条件中最大尺寸的一个
        if (!sizes.isEmpty()) {
            return sizes.get(0);
        }
        // 没得选，默认吧
        return defaultSize;
    }
  
    public boolean equalRate1(Size s, float rate){  
        float r = (float)(s.width)/(float)(s.height);  
        if(Math.abs(r - rate) <= 0.2)  
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
	
	/**
	 * Camera.size排序
	 * 
	 * @param sizeList
	 *            排序的Camera.size
	 * @param asc
	 *            是否升序排序 true为升、 false为降
	 */
	public void sortCameraSize(List<Camera.Size> sizeList, final boolean asc) {
		// 按大小排序
		Collections.sort(sizeList, new Comparator<Camera.Size>() {
			public int compare(Size size, Size newSize) {
				if (size.width > newSize.width) {
					if (asc) {
						return 1;
					} else {
						return -1;
					}
				} else if (size.width == newSize.width) {
					return 0;
				} else {
					if (asc) {
						return -1;
					} else {
						return 1;
					}
				}
			}
		});
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
	
	 /**
     * @param sizes
     * @param defaultSize
     * @param pictureSize 图片的大小
     * @param minRatio preview短边比长边所接受的最小比例
     * @return
     */
    public Camera.Size findBestPreviewSize(List<Camera.Size> sizes, Camera.Size defaultSize,
                                            Camera.Size pictureSize, float minRatio) {
        final int pictureWidth = pictureSize.width;
        final int pictureHeight = pictureSize.height;
        boolean isBestSize = (pictureHeight / (float)pictureWidth) > minRatio;
        sortCameraSize(sizes,false);

        Iterator<Camera.Size> it = sizes.iterator();
        while (it.hasNext()) {
            Camera.Size size = it.next();
            if ((float) size.height / size.width <= minRatio) {
                it.remove();
                continue;
            }

            // 找到同样的比例，直接返回
            if (isBestSize && size.width * pictureHeight == size.height * pictureWidth) {
                return size;
            }
        }

        // 未找到同样的比例的，返回尺寸最大的
        if (!sizes.isEmpty()) {
            return sizes.get(0);
        }

        // 没得选，默认吧
        return defaultSize;
    }
	
    /**
     * 找到短边比长边大于于所接受的最小比例的最大尺寸
     *
     * @param sizes       支持的尺寸列表
     * @param defaultSize 默认大小
     * @param minRatio    相机图片短边比长边所接受的最小比例
     * @return 返回计算之后的尺寸
     */
    public Camera.Size findBestPictureSize(List<Camera.Size> sizes, Camera.Size defaultSize, float minRatio) {
        final int MIN_PIXELS = 320 * 480;

        sortCameraSize(sizes,false);

        Iterator<Camera.Size> it = sizes.iterator();
        while (it.hasNext()) {
            Camera.Size size = it.next();
            //移除不满足比例的尺寸
            if ((float) size.height / size.width <= minRatio) {
                it.remove();
                continue;
            }
            //移除太小的尺寸
            if (size.width * size.height < MIN_PIXELS) {
                it.remove();
            }
        }

        // 返回符合条件中最大尺寸的一个
        if (!sizes.isEmpty()) {
            return sizes.get(0);
        }
        // 没得选，默认吧
        return defaultSize;
    }
}
