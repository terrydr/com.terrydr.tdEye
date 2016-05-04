package com.terrydr.eyeScope;

import java.util.ArrayList;
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
	private int mOrientation = 0;
	private Camera mCamera;
	/** 是否打开前置相机,true为前,false为后 */
	private boolean mIsFrontCamera;   //这里为后置相

	public CameraView(Context context) {
		super(context);
		// 初始化容
		getHolder().addCallback(callback);
		openCamera();
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
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
				Toast.makeText(getContext(), "打开相机失败", Toast.LENGTH_SHORT)
						.show();
				Log.e(TAG, e.getMessage());
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
		if (sizeList.size() > 0) {
			Size cameraSize = sizeList.get(0);
			// 预览图片大小
			parameters.setPreviewSize(cameraSize.width, cameraSize.height);
		}

		// 设置生成的图片大
		sizeList = parameters.getSupportedPictureSizes();
		if (sizeList.size() > 0) {
			Size cameraSize = sizeList.get(0);
			for (Size size : sizeList) {
				// 小于100W像素
				if (size.width * size.height < 100 * 10000) {
					cameraSize = size;
					break;
				}
			}
			parameters.setPictureSize(cameraSize.width, cameraSize.height);
		}
		// 设置图片格式
		parameters.setPictureFormat(ImageFormat.JPEG);
		parameters.setJpegQuality(100);
		parameters.setJpegThumbnailQuality(100);
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
				  Log.i(TAG, "自动对焦成功");  
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
		Camera.Parameters parameters=mCamera.getParameters();
		//不支持设置自定义聚焦，则使用自动聚焦，返回
		if (parameters.getMaxNumFocusAreas()<=0) {
			mCamera.autoFocus(callback);
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
		parameters.setFocusAreas(areas);
		try {
			
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			Log.e(TAG,"手动聚焦失败", e);
		}
		mCamera.autoFocus(callback);
	}
	
	@Override
	public void takePicture(PictureCallback callback,
			TakePictureListener listener) {
		mCamera.takePicture(null, null, callback);

	}

	@Override
	public void setCameraISO(int iso) {
		if (mCamera == null) {
			mCamera = Camera.open();
		}
		
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setExposureCompensation(iso);
		mCamera.setParameters(parameters);
	}
	
}
