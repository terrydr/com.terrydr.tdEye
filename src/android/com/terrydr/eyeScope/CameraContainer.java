package com.terrydr.eyeScope;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.terrydr.eyeScope.R;

public class CameraContainer extends RelativeLayout implements CameraOperation{

	public final static String TAG = "CameraContainer";
	/** 用以执行定时任务的Handler对象 */
//	private Handler mHandler;

	/** 相机绑定的SurfaceView */
	private CameraView mCameraView;

	/** 触摸屏幕时显示的聚焦图案 */
	private FocusImageView mFocusImageView;

	/** 拍照监听接口，用以在拍照始和结束后执行相应操*/
	private TakePictureListener mListener;

	/** 存放照片的根目录 */
	private String mSavePath;

	/** 照片字节流处理类 */
	private DataHandler mDataHandler;

	public CameraContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
//		mHandler = new Handler();
//		 setOnTouchListener(new TouchListener());
		
	}

	/**
	 * 初始化子控件
	 * 
	 * @param context
	 */
	private void initView(final Context context) {
		inflate(context, R.layout.cameracontainer, this);
		mCameraView = (CameraView) findViewById(R.id.cameraView);
		mFocusImageView = (FocusImageView) findViewById(R.id.focusImageView);
//		mCameraView.setOnTouchListener(this);
	}

	@Override
	public void takePicture(PictureCallback callback,
			TakePictureListener listener) {
		mCameraView.takePicture(callback, listener);

	}

	/**
	 * @Description: 拍照方法
	 * @param @param listener 拍照监听接口
	 * @return void
	 * @throws
	 */
	public void takePicture(TakePictureListener listener) {
		this.mListener = listener;
		takePicture(pictureCallback, mListener);
//		Point point=new Point(getWidth()/2, getHeight()/2);
//		mCameraView.onFocus(point,autoFocusCallback);
	}
	
	private final AutoFocusCallback autoFocusCallback=new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			//聚焦之后根据结果修改图片
			if (success) {
//				mFocusImageView.onFocusSuccess();
				Log.e("123", "111");
				takePicture(pictureCallback, mListener);

			}else {
				//聚焦失败显示的图片，由于未找到合适的资源，这里仍显示同一张图片
//				mFocusImageView.onFocusFailed();

			}
		}
	};

	private final PictureCallback pictureCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if (mSavePath == null)
				throw new RuntimeException("mSavePath is null");
				mDataHandler = new DataHandler();
			mDataHandler.setMaxSize(200);
			Bitmap bm = mDataHandler.save(data);
			// 重新打开预览图，进行下一次的拍照准备
			camera.startPreview();
			if (mListener != null)
				mListener.onTakePictureEnd(bm);
		}
	};

	/**
	 * @ClassName: TakePictureListener
	 * @Description: 拍照监听接口，用以在拍照始和结束后执行相应操
	 * @date 20160415
	 * 
	 */
	public static interface TakePictureListener {
		/**
		 * 拍照结束执行的动作，该方法会在onPictureTaken函数执行后触
		 * 
		 * @param bm
		 *            拍照生成的图
		 */
		public void onTakePictureEnd(Bitmap bm);

		/**
		 * 临时图片动画结束后触
		 * 
		 * @param bm
		 *            拍照生成的图
		 * @param isVideo
		 *            true：当前为录像缩略false:为拍照缩略图
		 * */
		public void onAnimtionEnd(Bitmap bm, boolean isVideo);
	}

	private final class TouchListener implements OnTouchListener {

		/** 记录是拖拉照片模式还是放大缩小照片模式 */

		private static final int MODE_INIT = 0;
		/** 放大缩小照片模式 */
		private static final int MODE_ZOOM = 1;
		private int mode = MODE_INIT;// 初始状态 

		/** 用于记录拖拉图片移动的坐标位置 */

		private float startDis;


		@Override
		public boolean onTouch(View v, MotionEvent event) {
			/** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
//			Log.e("1111111111", "12333333333333");
			if(event.getAction()==MotionEvent.ACTION_UP){  
				Log.e("1111111111", "123");
				Point point=new Point((int)event.getX(), (int)event.getY());
//				mCameraView.onFocus(point,autoFocusCallback);
//				
				} 
//			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			// 手指压下屏幕
//			case MotionEvent.ACTION_DOWN:
//				mode = MODE_INIT;
//				break;
//			case MotionEvent.ACTION_POINTER_DOWN:
//				//如果mZoomSeekBar为null 表示该设备不支持缩放 直接跳过设置mode Move指令也无法执行
//				if(mZoomSeekBar==null) return true;
//				//移除token对象为mZoomSeekBar的延时任务
//				mHandler.removeCallbacksAndMessages(mZoomSeekBar);
//				mZoomSeekBar.setVisibility(View.VISIBLE);
//
//				mode = MODE_ZOOM;
//				/** 计算两个手指间的距离 */
//				startDis = distance(event);
//				break;
//			case MotionEvent.ACTION_MOVE:
//				if (mode == MODE_ZOOM) {
//					//只有同时触屏两个点的时候才执行
//					if(event.getPointerCount()<2) return true;
//					float endDis = distance(event);// 结束距离
//					//每变化10f zoom变1
//					int scale=(int) ((endDis-startDis)/10f);
//					if(scale>=1||scale<=-1){
//						int zoom=mCameraView.getZoom()+scale;
//						//zoom不能超出范围
//						if(zoom>mCameraView.getMaxZoom()) zoom=mCameraView.getMaxZoom();
//						if(zoom<0) zoom=0;
//						mCameraView.setZoom(zoom);
//						mZoomSeekBar.setProgress(zoom);
//						//将最后一次的距离设为当前距离
//						startDis=endDis;
//					}
//				}
//				break;
				// 手指离开屏幕
//			case MotionEvent.ACTION_UP:
////				if(mode!=MODE_ZOOM){
//					//设置聚焦
//					Point point=new Point((int)event.getX(), (int)event.getY());
//					mCameraView.onFocus(point,autoFocusCallback);
//					Log.e("1111111111", "123");
//					mFocusImageView.startFocus(point);
//				}else {
//					//ZOOM模式下 在结束两秒后隐藏seekbar 设置token为mZoomSeekBar用以在连续点击时移除前一个定时任务
//					mHandler.postAtTime(new Runnable() {
//
//						@Override
//						public void run() {
//							// TODO Auto-generated method stub
//							mZoomSeekBar.setVisibility(View.GONE);
//						}
//					}, mZoomSeekBar,SystemClock.uptimeMillis()+2000);
//				}
//				break;
//			}
			return true;
		}
		/** 计算两个手指间的距离 */
		private float distance(MotionEvent event) {
			float dx = event.getX(1) - event.getX(0);
			float dy = event.getY(1) - event.getY(0);
			/** 使用勾股定理返回两点之间的距离 */
			return (float) Math.sqrt(dx * dx + dy * dy);
		}

	}
	
	/**
	 * 拍照返回的byte数据处理
	 * 
	 */
	private final class DataHandler {
		/** 大图存放路径 */
		private String mThumbnailFolder;
		/** 小图存放路径 */
		private String mImageFolder;
		/** 压缩后的图片单位KB */
		private int maxSize = 200;

		public DataHandler() {
			mImageFolder = FileOperateUtil.getFolderPath(getContext(),
					FileOperateUtil.TYPE_IMAGE, mSavePath);
			mThumbnailFolder = FileOperateUtil.getFolderPath(getContext(),
					FileOperateUtil.TYPE_THUMBNAIL, mSavePath);
			File folder = new File(mImageFolder);
			if (!folder.exists()) {
				folder.mkdirs();
			}
			folder = new File(mThumbnailFolder);
			if (!folder.exists()) {
				folder.mkdirs();
			}
		}

		/**
		 * 保存图片
		 * 
		 * @param 相机返回的文件流
		 * @return 解析流生成的缩略
		 */
		public Bitmap save(byte[] data) {
			if (data != null) {
				// 解析生成相机返回的图
				Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
				// 生成缩略
				Bitmap thumbnail = ThumbnailUtils
						.extractThumbnail(bm, 213, 213);
				// 产生新的文件
				String imgName = FileOperateUtil.createFileNmae(".jpg");
				String imagePath = mImageFolder + File.separator + imgName;
				String thumbPath = mThumbnailFolder + File.separator + imgName;

				File file = new File(imagePath);
				File thumFile = new File(thumbPath);
				try {
					// 存图片大
					FileOutputStream fos = new FileOutputStream(file);
					ByteArrayOutputStream bos = compress(bm);
					fos.write(bos.toByteArray());
					fos.flush();
					fos.close();
					// 存图片小
					BufferedOutputStream bufferos = new BufferedOutputStream(
							new FileOutputStream(thumFile));
					thumbnail
							.compress(Bitmap.CompressFormat.JPEG, 50, bufferos);
					bufferos.flush();
					bufferos.close();
					return bm;
				} catch (Exception e) {
					Log.e(TAG, e.toString());
					Toast.makeText(getContext(), "解析相机返回流失败",
							Toast.LENGTH_SHORT).show();

				}
			} else {
				Toast.makeText(getContext(), "拍照失败，请重试", Toast.LENGTH_SHORT)
						.show();
			}
			return null;
		}


		/**
		 * 图片压缩方法
		 * 
		 * @param bitmap
		 *            图片文件
		 * @param max
		 *            文件大小
		 * @return 压缩后的字节
		 * @throws Exception
		 */
		public ByteArrayOutputStream compress(Bitmap bitmap) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这表示不压缩，把压缩后的数据存放到baos
			int options = 99;
			while (baos.toByteArray().length / 1024 > maxSize) { // 循环判断如果压缩后图片是否大100kb,大于继续压缩
				options -= 3;// 每次都减
				// 压缩比小，不再压
				if (options < 0) {
					break;
				}
				Log.i(TAG, baos.toByteArray().length / 1024 + "");
				baos.reset();// 重置baos即清空baos
				bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos
			}
			return baos;
		}

		public void setMaxSize(int maxSize) {
			this.maxSize = maxSize;
		}
	}
	
	/**
	 * 设置文件保存路径
	 * @param rootPath
	 */
	public void setRootPath(String rootPath){
		this.mSavePath=rootPath;

	}

	@Override
	public void setCameraISO(int iso) {		
		mCameraView.setCameraISO(iso);
	}
	
	/**
	 * @Description: 设置爆光
	 * @return void
	 * @throws
	 */
	public void setCameraISO_int(int iso) {
		setCameraISO(iso);
	}

}
