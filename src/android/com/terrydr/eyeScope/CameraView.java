package com.terrydr.eyeScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.terrydr.eyeScope.CameraContainer.TakePictureListener;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class CameraView extends SurfaceView implements CameraOperation {

	public final static String TAG = "CameraView";
	/** 和该View绑定的Camera对象 */
	/** 当前屏幕旋转角度 */
	public int mOrientation = 0;
	public Camera mCamera;
	/** 是否打开前置相机,true为前,false为后 */
	private boolean mIsFrontCamera;   //这里为后置相
	
	public int mMode = 0;
	public Size pictureS1,sizeSmorl;
	private CameraSizeComparator sizeComparator = new CameraSizeComparator();
	private List<Camera.Size> sizeList1;
	private List<Size> mSupportList = null;

	private CameraActivity cActivity;
	float previewRate = -1f;  
	// 短边比长边
//    private float ratio;
	int w_screen;
	int h_screen;
	
	public CameraView(Context context) {
		super(context);
		cActivity = (CameraActivity) context;
		previewRate = getScreenRate(cActivity); //默认全屏的比例预览  
		
		
		// 初始化容
		getHolder().addCallback(callback);
		openCamera();
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		cActivity = (CameraActivity) context;
		previewRate = getScreenRate(cActivity); //默认全屏的比例预览  
		// 初始化容
		getHolder().addCallback(callback);
		openCamera();
		mIsFrontCamera = false;
	}
	
	/** 
     * 获取屏幕宽度和高度，单位为px 
     * @param context 
     * @return 
     */  
    public Point getScreenMetrics(Context context){  
        DisplayMetrics dm =context.getResources().getDisplayMetrics();  
        w_screen = dm.widthPixels;  
        h_screen = dm.heightPixels;  
        Log.e(TAG, "Screen---Width = " + w_screen + " Height = " + h_screen + " densityDpi = " + dm.densityDpi);  
        return new Point(w_screen, h_screen);  
          
    }  
      
    /** 
     * 获取屏幕长宽比 
     * @param context 
     * @return 
     */  
    public float getScreenRate(Context context){  
        Point P = getScreenMetrics(context);  
        float H = P.y;  
        float W = P.x;  
        return (H/W);  
    }  
	
    /**
     * 获取sdk版本号
     * @return
     */
	private static int getSDKVersionNumber() {  
	    int sdkVersion;  
	    try {  
	        sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK);  
	    } catch (NumberFormatException e) {  
	        sdkVersion = 0;  
	    }  
	    return sdkVersion;  
	} 

	/**
	 * 用于控制SurfaceView  
	 */
	private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {

		/** 
	     * 当SurfaceView创建的时候，调用此函数 
	     */  
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				if (mCamera == null) {
					openCamera();
				}
				setCameraParameters();
				mCamera.setPreviewDisplay(getHolder());
			} catch (Exception e) {
				if(getSDKVersionNumber()>=23){  //android版本大于6.0直接结束返回
					cActivity.finish();
					return;
				}
				//android版本小于6.0 弹窗提醒
				Toast.makeText(getContext(), "未获得相机权限，请到设置中授权后再尝试。", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "未获得相机权限，请到设置中授权后再尝试。"+e.getMessage());
				cActivity.finish();
				return;
			}
			mCamera.startPreview();
		}

		/** 
	     * 当SurfaceView的视图发生改变的时候，调用此函数 
	     */ 
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			updateCameraOrientation();
		}
		
		/** 
	     * 当SurfaceView销毁的时候，调用此函数 
	     */ 
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		}
	};

	/**
	 * 设置相机参数
	 */
	private void setCameraParameters() {
		Camera.Parameters parameters = mCamera.getParameters();
//		Log.e(TAG, "CameraView.getWidth():" + this.getWidth() + "-CameraView.getHeight():" + this.getHeight());
        //预览实际比率
		float ratio = this.getHeight() / (float) this.getWidth();  
        Log.e(TAG, "ratio:" + ratio);
//        int minWidth = this.getHeight()>this.getWidth() ? this.getHeight():this.getWidth();
        int minWidth = 1440;
        Log.e(TAG, "minWidth:" + minWidth);
		
		// 选择合的预览尺寸
		List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
//        for(Size s:sizeList){  
//        	Log.e(TAG, "list : w = " + s.width + "*h = " + s.height);  
//        }  
		Size pictureS = CameraSize.getInstance().getPropPreviewSize(sizeList,previewRate, minWidth,ratio);  
	    parameters.setPreviewSize(pictureS.width, pictureS.height); 
	    Log.e(TAG, "previewWidth:" + pictureS.width +"-previewHeight:" + pictureS.height); 
	     
		// 设置生成的图片大小
	    sizeList1 = parameters.getSupportedPictureSizes();
	    Size pictureS1 = CameraSize.getInstance().getPropPictureSize(sizeList1,previewRate, minWidth,ratio);  
	    parameters.setPictureSize(pictureS1.width, pictureS1.height);  
	    Log.e(TAG, "pictureWidth:" + pictureS1.width +"-pictureHeight:" + pictureS1.height); 
	    
//        setPreviewSize(parameters);
//        setPictureSize(parameters);
        
//	 // 设置pictureSize
//        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
//        Size mBestPictureSize = null;
//        if (mBestPictureSize == null) {
//            mBestPictureSize =CameraSize.getInstance().findBestPictureSize(pictureSizes, parameters.getPictureSize(), ratio);
//        }
//        parameters.setPictureSize(mBestPictureSize.width, mBestPictureSize.height);
//        Log.e(TAG, "mBestPictureSizeWidth:" + mBestPictureSize.width +"-mBestPictureSizeHeight:" + mBestPictureSize.height); 
//     // 设置previewSize
//        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
//        Size mBestPreviewSize = null;
//        if (mBestPreviewSize == null) {
//            mBestPreviewSize = CameraSize.getInstance().findBestPreviewSize(previewSizes, parameters.getPreviewSize(),
//                    mBestPictureSize, ratio);
//        }
//        parameters.setPreviewSize(mBestPreviewSize.width, mBestPreviewSize.height);
//        Log.e(TAG, "mBestPreviewSizeWidth:" + mBestPreviewSize.width +"-mBestPreviewSizeHeight:" + mBestPreviewSize.height); 
        
		
		// 设置图片格式
		parameters.setPictureFormat(ImageFormat.JPEG);
		parameters.setJpegQuality(100);
		parameters.setJpegThumbnailQuality(100);
		parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		mCamera.setParameters(parameters);
		
		// 启屏幕朝向监
		startOrientationChangeListener();
	}
	
	/**
	 * 自动对焦
	 */
	AutoFocusCallback autoFocusCallback = new AutoFocusCallback(){
		  @Override
		  public void onAutoFocus(boolean success, Camera arg1) {
			  if(success){
				  Log.d(TAG, "对焦成功");  
			  }else{
				  Log.d(TAG, "对焦失败");  
			  }
		  }};

	/**
	 * 启动屏幕朝向改变监听函数 用于在屏幕横竖屏切换时改变保存的图片的方
	 */
	private void startOrientationChangeListener() {
		OrientationEventListener mOrEventListener = new OrientationEventListener(
				getContext()) {
			@Override
			public void onOrientationChanged(int rotation) {

				if (((rotation >= 0) && (rotation <= 45)) || (rotation > 315)) {
					rotation = 0;
				} else if ((rotation > 45) && (rotation <= 135)) {
					rotation = 90;
				} else if ((rotation > 135) && (rotation <= 225)) {
					rotation = 180;
				} else if ((rotation > 225) && (rotation <= 315)) {
					rotation = 270;
				} else {
					rotation = 0;
				}
				if (rotation == mOrientation)
					return;
				mOrientation = rotation;
				updateCameraOrientation();
			}
		};
		mOrEventListener.enable();
	}

	/**
	 * 根据当前朝向修改保存图片的旋转角
	 */
	private void updateCameraOrientation() {
		if (mCamera != null) {
			Camera.Parameters parameters = mCamera.getParameters();
			if(parameters == null){
				return ;
			}
			parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
			// rotation参数水平方向为
			int rotation = 90 + mOrientation == 360 ? 0 : 90 + mOrientation;
			// 前置摄像头需要对垂直方向做变换，否则照片是颠倒的
			if (mIsFrontCamera) {
				if (rotation == 90)
					rotation = 270;
				else if (rotation == 270)
					rotation = 90;
			}
//			Log.e(TAG, "rotation-------------------------"+ rotation);
//			parameters.setRotation(rotation);// 生成的图片转90°
			// 预览图片旋转90°
			mCamera.setDisplayOrientation(90);// 预览
			mCamera.autoFocus(autoFocusCallback);  //添加自动对焦
			mCamera.setParameters(parameters);
			
		}
	}

	/**
	 * 根据当前照相机状(前置或后)，打对应相机
	 */
	private boolean openCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}

		if (mIsFrontCamera) {
			Camera.CameraInfo cameraInfo = new CameraInfo();
			for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
				Camera.getCameraInfo(i, cameraInfo);
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					try {
						mCamera = Camera.open(i);
					} catch (Exception e) {
						mCamera = null;
						return false;
					}

				}
			}
		} else {
			try {
				mCamera = Camera.open();
			} catch (Exception e) {
				mCamera = null;
				return false;
			}

		}
		return true;
	}

	/**  
	 * 自动对焦 
	 *  @param point 触屏坐标
	 */
	public void onFocus(Point point){
		mCamera.cancelAutoFocus();
		if(mCamera == null){
			return ;
		}
		Camera.Parameters parameters=mCamera.getParameters();
		if(parameters == null){
			return ;
		}
		//不支持设置自定义聚焦，则使用自动聚焦，返回
		if (parameters.getMaxNumFocusAreas()<=0) {
			mCamera.autoFocus(autoFocusCallback);
			return;
		}
		mCamera.autoFocus(autoFocusCallback);
	}
	
	/**  
	 * 手动聚焦 
	 *  @param point 触屏坐标
	 */
	public void onFocus(MotionEvent event,AutoFocusCallback callback){
		if(mCamera == null){
			return ;
		}
		mCamera.cancelAutoFocus();

		Camera.Parameters parameters=mCamera.getParameters();
		//不支持设置自定义聚焦，则使用自动聚焦，返回
		if (parameters.getMaxNumFocusAreas()<=0) {
			mCamera.autoFocus(callback);
			return;
		}
		Rect focusRect = calculateTapArea(event.getRawX(), event.getRawY(), 1f);  
//        Rect meteringRect = calculateTapArea(event.getRawX(), event.getRawY(), 1.5f);  
  
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);  
          
        if (parameters.getMaxNumFocusAreas() > 0) {  
            List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();  
            focusAreas.add(new Camera.Area(focusRect, 1000));  
            parameters.setFocusAreas(focusAreas);  
        }  
  
//        if (parameters.getMaxNumMeteringAreas() > 0) {  
//            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();  
//            meteringAreas.add(new Camera.Area(meteringRect, 1000));  
//            parameters.setMeteringAreas(meteringAreas);  
//        }  
  
//        mCamera.setParameters(parameters);  
		try {
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			Log.e(TAG, "手动聚焦失败1", e);
		}
		mCamera.autoFocus(callback);
	}
	
	/**
	 * Convert touch position x:y to {@link Camera.Area} position -1000:-1000 to
	 * 1000:1000.
	 */
	private Rect calculateTapArea(float x, float y, float coefficient) {
		float focusAreaSize = 300;
		int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();

		int centerX = (int) (x / getResolution().width * 2000 - 1000);
		int centerY = (int) (y / getResolution().height * 2000 - 1000);

		int left = clamp(centerX - areaSize / 2, -1000, 1000);
		int right = clamp(left + areaSize, -1000, 1000);
		int top = clamp(centerY - areaSize / 2, -1000, 1000);
		int bottom = clamp(top + areaSize, -1000, 1000);

		return new Rect(left, top, right, bottom);
	}

	private int clamp(int x, int min, int max) {
		if (x > max) {
			return max;
		}
		if (x < min) {
			return min;
		}
		return x;
	}

	private Camera.Size getResolution() {
		Camera.Parameters params = mCamera.getParameters();
		Camera.Size s = params.getPreviewSize();
		return s;
	}
	
	/**  
	 * 手动聚焦 
	 *  @param point 触屏坐标
	 */
	public void onFocus(Point point,AutoFocusCallback callback){
		if(mCamera == null){
			return ;
		}
		mCamera.cancelAutoFocus();
		Camera.Parameters parameters=mCamera.getParameters();
		//不支持设置自定义聚焦，则使用自动聚焦，返回
		if (parameters.getMaxNumFocusAreas()<=0) {
			mCamera.autoFocus(callback);
			return;
		}
		List<Area> areas = new ArrayList<Camera.Area>();
		int left = point.x - 100;
		int top = point.y - 100;
		int right = point.x + 100;
		int bottom = point.y + 100;
		left = left < -1000 ? -1000 : left;
		top = top < -1000 ? -1000 : top;
		right = right > 1000 ? 1000 : right;
		bottom = bottom > 1000 ? 1000 : bottom;
		areas.add(new Area(new Rect(left, top, right, bottom), 100));
		parameters.setFocusAreas(areas);
		try {
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			Log.e(TAG, "手动聚焦失败", e);
		}
//		parameters.setFocusMode(Parameters.FOCUS_MODE_MACRO);
		mCamera.autoFocus(callback);
	}
	
	@Override
	public void takePicture(PictureCallback callback,
			TakePictureListener listener) {
		mCamera.takePicture(null, null, callback);
	}
	
	/**
	 * 返回支持保存图片的大小List
	 * @return  List
	 */
	public List<Size> returnSizeList(){
		createSupportList();
		return mSupportList;
	}
	private void createSupportList(){
        if(mCamera == null){
            return;
        }
        Camera.Parameters params = mCamera.getParameters();
        mSupportList = Reflect.getSupportedPreviewSizes(params);
        if (mSupportList != null && mSupportList.size() > 0) {
            Collections.sort(mSupportList, sizeComparator);
        }
    }
	
	/**
	 * 设置白平衡
	 * @param wbValue
	 * 	/**
	 * 设置照相机参
	 * 白平衡设置  
	    WHITE_BALANCE_AUTO              Constant Value: "auto" 				自动                   			 华为、三星支持模式
		WHITE_BALANCE_INCANDESCENT      Constant Value: "incandescent"    	白炽光                 		 华为、三星支持模式
		WHITE_BALANCE_FLUORESCENT		Constant Value: "fluorescent"  		日光 灯、荧光灯                  华为、三星支持模式
		WHITE_BALANCE_WARM_FLUORESCENT	Constant Value: "warm-fluorescent"  荧光
		WHITE_BALANCE_DAYLIGHT			Constant Value: "daylight" 			白天                    			华为、三星支持模式
		WHITE_BALANCE_CLOUDY_DAYLIGHT	Constant Value: "cloudy-daylight" 	多云、阴天     			华为、三星支持模式
		WHITE_BALANCE_TWILIGHT			Constant Value: "twilight" 			黄昏
		WHITE_BALANCE_SHADE				Constant Value: "shade" 			暧荧光灯
	 *
	 */
	public void setWB(String wbValue){
		if (mCamera == null) {
			return;
		}
		Camera.Parameters parameters = mCamera.getParameters();
		if(wbValue==null){
			return;
		}
		parameters.setWhiteBalance(wbValue);
		mCamera.setParameters(parameters);
	}
	
	@Override
	public void setCameraISO(int iso,boolean lightOn) {
		if (mCamera == null) {
			mCamera = Camera.open();
		}
		Camera.Parameters parameters = mCamera.getParameters();
		setBestExposure(parameters,lightOn);
		mCamera.setParameters(parameters);
	}
	
	private final float MAX_EXPOSURE_COMPENSATION = 1.0f;
	private final float MIN_EXPOSURE_COMPENSATION = 0.0f;
	/**
	 * 自适应设置ISO
	 * @param parameters
	 * @param lightOn
	 */
	public void setBestExposure(Camera.Parameters parameters, boolean lightOn) {
		int minExposure = parameters.getMinExposureCompensation();
		int maxExposure = parameters.getMaxExposureCompensation();
		float step = parameters.getExposureCompensationStep();
		if ((minExposure != 0 || maxExposure != 0) && step > 0.0f) {
			// Set low when light is on
			float targetCompensation = lightOn ? MIN_EXPOSURE_COMPENSATION
					: MAX_EXPOSURE_COMPENSATION;
			int compensationSteps = Math.round(targetCompensation / step);
			float actualCompensation = step * compensationSteps;
			// Clamp value:
			compensationSteps = Math.max(Math.min(compensationSteps, maxExposure), minExposure);
			if (parameters.getExposureCompensation() == compensationSteps) {
				Log.d(TAG, "Exposure compensation already set to "+ compensationSteps + " / " + actualCompensation);
			} else {
				Log.d(TAG, "Setting exposure compensation to "+ compensationSteps + " / " + actualCompensation);
				parameters.setExposureCompensation(compensationSteps);
			}
		} else {
			Log.i(TAG, "Camera does not support exposure compensation");
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
	
	/**
	 * 获取最大缩放级别，最大为40
	 * 
	 * @return
	 */
	public int getMaxZoom() {
		if (mCamera == null)
			return -1;
		Camera.Parameters parameters = mCamera.getParameters();
		if (!parameters.isZoomSupported())
			return -1;
//		return parameters.getMaxZoom() > 40 ? 40 : parameters.getMaxZoom();
//		LOG.e(TAG, "parameters.getMaxZoom():" + parameters.getMaxZoom());
		return parameters.getMaxZoom();
	}

	/**
	 * 设置相机缩放级别
	 * 
	 * @param zoom
	 */
	public void setZoom(int zoom) {
		if (mCamera == null)
			return;
		Camera.Parameters parameters;
		// 注意此处为录像模式下的setZoom方式。在Camera.unlock之后，调用getParameters方法会引起android框架底层的异常
		// stackoverflow上看到的解释是由于多线程同时访问Camera导致的冲突，所以在此使用录像前保存的mParameters。
		parameters = mCamera.getParameters();
		if (!parameters.isZoomSupported())
			return;
		parameters.setZoom(zoom);
		mCamera.setParameters(parameters);
		mZoom = zoom;
	}

	public int getZoom() {
		return mZoom;
	}

	private int mZoom = 0;
}
