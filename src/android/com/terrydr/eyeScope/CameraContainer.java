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
import android.hardware.Camera.PictureCallback;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.terrydr.eyeScope.R;

public class CameraContainer extends RelativeLayout implements CameraOperation{

	public final static String TAG = "CameraContainer";
	/** 用以执行定时任务的Handler对象 */
//	private Handler mHandler;

	/** 相机绑定的SurfaceView */
	private CameraView mCameraView;

//	/** 触摸屏幕时显示的聚焦图案 */
//	private FocusImageView mFocusImageView;

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
//		setOnTouchListener(new TouchListener());
		
	}

	/**
	 * 初始化子控件
	 * 
	 * @param context
	 */
	private void initView(final Context context) {
		inflate(context, R.layout.cameracontainer, this);
		mCameraView = (CameraView) findViewById(R.id.cameraView);
//		mFocusImageView = (FocusImageView) findViewById(R.id.focusImageView);
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
//			int options = 99;
//			while (baos.toByteArray().length / 1024 > maxSize) { // 循环判断如果压缩后图片是否大100kb,大于继续压缩
//				options -= 3;// 每次都减
//				// 压缩比小，不再压
//				if (options < 0) {
//					break;
//				}
////				Log.i(TAG, baos.toByteArray().length / 1024 + "");
//				baos.reset();// 重置baos即清空baos
//				bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos
//			}
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
	
	public void setOnFocus(Point point) {		
		mCameraView.onFocus(point, null);
	}

}
