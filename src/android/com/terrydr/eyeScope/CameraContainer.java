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
import android.hardware.Camera.Size;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.terrydr.eyeScope.R;

public class CameraContainer extends RelativeLayout implements CameraOperation{

	public final static String TAG = "CameraContainer";

	/** 相机绑定的SurfaceView */
	private CameraView mCameraView;

	/** 拍照监听接口，用以在拍照始和结束后执行相应操*/
	private TakePictureListener mListener;

	/** 存放照片的根目录 */
	private String mSavePath;

	/** 照片字节流处理类 */
	private DataHandler mDataHandler;
	
	private PreviewCallback mPreviewCallback = null;
	private CameraActivity cActivity;
	private boolean contShoot = false;
	public int mMode = 0;     //是否连拍标识
    public int mNumLeft=0;    //连拍左眼的张数
    public int mNumright=0;    //连拍右眼的张数
    public int stop = 0;      //触摸离开屏幕结束拍照的张数
    
	

	public CameraContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		cActivity = (CameraActivity) context;
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
	}
	/**
	 * 开始连拍
	 */
    public void resumeShooting(){
    	contShoot = true;
		mMode = 1;
		if(mPreviewCallback == null){
			mPreviewCallback = new PreviewCallback();
		}
		if(mPreviewCallback != null){
			
    		if(mCameraView.mCamera != null){
    			mCameraView.mCamera.startPreview();
    			mCameraView.mCamera.setPreviewCallback(mPreviewCallback);
    		}
    	}
    }
    /**
     * 结束连拍
     */
    public void stopShooting(){
    	contShoot = false;
		mMode = 0;
    	if(mCameraView.mCamera!=null){
    		mCameraView.mCamera.stopPreview();
    		mCameraView.mCamera.setPreviewCallback(null);
    		mCameraView.mCamera.startPreview();
    	}
    	
    }
    
    /**
     * 每一张连拍结束的回调函数
     */
    public void countShoot(){
//    	LOG.e(TAG, "cActivity.i_left"+cActivity.i_left);
    	cActivity.delectMultiFile2();
        if(mMode == 1){
        	if(mCameraView.mCamera!=null){
        		mCameraView.mCamera.setPreviewCallback(mPreviewCallback);    
        	}
        }
        if(!contShoot){
        	stopShooting();
        }
//        LOG.e(TAG, "mNumLeft:"+mNumLeft + "------" + cActivity.leftOrRight);
        if(cActivity.leftOrRight){
        	mNumLeft++;
            if(stop==mNumLeft){
            	cActivity.startAlbumAty();
            }else
        	if(mNumLeft>=6){
        		stopShooting();
            	cActivity.startAlbumAty();
        	}
        }else{
        	mNumright++;
            if(stop==mNumright){
            	cActivity.startAlbumAty();
            }else
        	if(mNumright>=6){
        		stopShooting();
            	cActivity.startAlbumAty();
        	}
        }
    }
	
    /**
     * 拍照完返回调用函数
     */
	private PictureCallback pictureCallback = new PictureCallback() {

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
	 * 连拍保存图片接口 
	 * @author ty
	 *
	 */
	public class PreviewCallback implements Camera.PreviewCallback {
        PreviewCallback(){}
		@Override
        public void onPreviewFrame(final byte[] data, Camera camera) {
        	camera.setPreviewCallback(null);
                Thread t2 = new Thread(){
                    public void run(){
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                        if(mCameraView.mCamera != null){
                            if(mMode == 1){
                            	mCameraView.mCamera.setPreviewCallback(mPreviewCallback);  
                            }
                        }
                    }
                };
                t2.start();
			if (mSavePath == null)
				throw new RuntimeException("mSavePath is null");
			
			String mImageFolder = FileOperateUtil.getFolderPath(getContext(),
					FileOperateUtil.TYPE_IMAGE, mSavePath);
			String mThumbnailFolder = FileOperateUtil.getFolderPath(getContext(),
					FileOperateUtil.TYPE_THUMBNAIL, mSavePath);
			File folder = new File(mImageFolder);
			if (!folder.exists()) {
				folder.mkdirs();
			}
			folder = new File(mThumbnailFolder);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			// 产生新的文件
			String imgName = FileOperateUtil.createFileNmae(".jpg");
			String imagePath = mImageFolder + File.separator + imgName;
			String thumbPath = mThumbnailFolder + File.separator + imgName;
            
//			Size bmSize = mCameraView.returnPictureSzie();
			Size bmSize = convertPreviewSize(data);
            final int width = bmSize.width;
            final int height = bmSize.height;
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 
            cActivity.setCount();
            ImageAsyncTask taskBmp = new ImageAsyncTask(CameraContainer.this,mCameraView, data, bmSize,imagePath,thumbPath);
            taskBmp.execute(bmp);
       }

	}
	
	/**
	 * 获取连拍保存图片大小
	 * @param data
	 * @return
	 */
    private Size convertPreviewSize(byte[] data){
        double displaysize = data.length / 1.5;
        Size size;
        int x, y;
        for(int i=0; i<mCameraView.returnSizeList().size(); i++){
            size = mCameraView.returnSizeList().get(i);
            x = size.width;
            y = size.height;
            if((x*y) == displaysize){
                return size;
            }
        }
        return null;
    }

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
