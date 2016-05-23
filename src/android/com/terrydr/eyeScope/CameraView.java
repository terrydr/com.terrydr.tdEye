package com.terrydr.eyeScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.cordova.LOG;

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
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import com.terrydr.eyeScope.R;

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
	
	public CameraView(Context context) {
		super(context);
		cActivity = (CameraActivity) context;
		// 初始化容
		getHolder().addCallback(callback);
		openCamera();
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		cActivity = (CameraActivity) context;
		// 初始化容
		getHolder().addCallback(callback);
		openCamera();
		mIsFrontCamera = false;
	}

	private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				if (mCamera == null) {
					openCamera();
				}
				setCameraParameters();
				mCamera.setPreviewDisplay(getHolder());
			} catch (Exception e) {
				Toast.makeText(getContext(), "未获得相机权限，请到设置中授权后再尝试。", Toast.LENGTH_SHORT)
						.show();
				
				Log.e(TAG, "未获得相机权限，请到设置中授权后再尝试。"+e.getMessage());
				cActivity.finish();
				return;
			}
			mCamera.startPreview();
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			updateCameraOrientation();
		}
		
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
	 * 设置照相机参
	 * 白平衡设置 
	    WHITE_BALANCE_AUTO              Constant Value: "auto" 				自动
		WHITE_BALANCE_INCANDESCENT      Constant Value: "incandescent"    	白炽光
		WHITE_BALANCE_FLUORESCENT		Constant Value: "fluorescent"  		日光
		WHITE_BALANCE_WARM_FLUORESCENT	Constant Value: "warm-fluorescent"  荧光
		WHITE_BALANCE_DAYLIGHT			Constant Value: "daylight" 			白天
		WHITE_BALANCE_CLOUDY_DAYLIGHT	Constant Value: "cloudy-daylight" 	多云、阴天
		WHITE_BALANCE_TWILIGHT			Constant Value: "twilight" 			黄昏
		WHITE_BALANCE_SHADE				Constant Value: "shade" 			暧荧光灯
	 *
	 */
	private void setCameraParameters() {
		Camera.Parameters parameters = mCamera.getParameters();
//		parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
//		boolean m = parameters.isAutoWhiteBalanceLockSupported();
//		String i = parameters.getWhiteBalance();
//		Log.e( "是否支持自动白平衡：", String.valueOf(m));
//		Log.e( "当前白平衡：", String.valueOf(i));
//		parameters.setAutoWhiteBalanceLock(false);
//		String n = parameters.getWhiteBalance();
//		int m = parameters.getMaxExposureCompensation();
		
		// 选择合的预览尺寸
		List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
	    Size pictureS = CameraSize.getInstance().getPreviewSize(sizeList, 1200);  
	    parameters.setPreviewSize(pictureS.width, pictureS.height);  
//	    Log.e(TAG, "w:" + pictureS.width +"-h:" + pictureS.height);  
		// 设置生成的图片大
	    sizeList1 = parameters.getSupportedPictureSizes();
		pictureS1 = CameraSize.getInstance().getPictureSize(sizeList1, 1200);  
	    parameters.setPictureSize(pictureS1.width, pictureS1.height);  
//	    Log.e(TAG, "w1:" + pictureS1.width +"-h1:" + pictureS1.height);  
		
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
//				  Log.d(TAG, "对焦成功");  
			  }else{
//				  Log.d(TAG, "对焦失败");  
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
			parameters.setRotation(rotation);// 生成的图片转90°
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
	 * 手动聚焦 
	 *  @param point 触屏坐标
	 */
	protected void onFocus(Point point,AutoFocusCallback callback){
		mCamera.cancelAutoFocus();

		Camera.Parameters parameters=mCamera.getParameters();
		//不支持设置自定义聚焦，则使用自动聚焦，返回
		if (parameters.getMaxNumFocusAreas()<=0) {
			mCamera.autoFocus(autoFocusCallback);
			return;
		}
		List<Area> areas=new ArrayList<Camera.Area>();
		int left=point.x-300;
		int top=point.y-300;
		int right=point.x+300;
		int bottom=point.y+300;
		left=left<-1000?-1000:left;
		top=top<-1000?-1000:top;
		right=right>1000?1000:right;
		bottom=bottom>1000?1000:bottom;
		areas.add(new Area(new Rect(left,top,right,bottom), 100));
		parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);

		parameters.setFocusAreas(areas);
		try {
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			Log.e(TAG,"手动聚焦失败", e);
		}
		mCamera.autoFocus(autoFocusCallback);
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
//		int i = parameters.getExposureCompensation();
//		int i1 = parameters.getMinExposureCompensation();
//		int i2 = parameters.getMaxExposureCompensation();
//		float i3 = parameters.getExposureCompensationStep();
//		LOG.e(TAG, "i:" + i);
//		LOG.e(TAG, "i1:" + i1);
//		LOG.e(TAG, "i2:" + i2);
//		LOG.e(TAG, "i3:" + i3);
		setBestExposure(parameters,lightOn);
//		parameters.setExposureCompensation(3);
		mCamera.setParameters(parameters);
	}
	
	private final float MAX_EXPOSURE_COMPENSATION = 1.0f;
	private final float MIN_EXPOSURE_COMPENSATION = 0.0f;
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
	
}
