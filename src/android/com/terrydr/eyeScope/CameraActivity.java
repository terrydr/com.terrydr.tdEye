package com.terrydr.eyeScope;

import java.io.File;
import java.util.List;
import com.terrydr.eyeScope.CameraContainer.TakePictureListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.terrydr.eyeScope.R;

public class CameraActivity extends Activity implements View.OnClickListener,
		TakePictureListener, OnGestureListener, OnTouchListener {

	public final static String TAG = "CameraActivity";
	private CameraContainer mContainer;
	private ImageView photos_iv;
	private String mSaveRoot_left, mSaveRoot_right;
	private ImageView iso_iv, whitebalance_iv;
	private int mExposureNum = 0;
	private RelativeLayout whitebalance_rl;
	private TextView eyeleft_tv, eyeleft_tv1,return_index_bt,camera_camera_tv;
	private LinearLayout linearlayou_left, linearlayou_right;
	private GestureDetector detector;
	private boolean leftOrRight = true; // 默认为左 true:左眼，false:右眼
	private int i_left = 0; // 记录单侧拍摄图像个数
	private int i_right = 0; // 记录单侧拍摄图像个数
	private boolean deleteFile = true;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_camera);
		mContainer = (CameraContainer) findViewById(R.id.container);
		eyeleft_tv = (TextView) findViewById(R.id.eyeleft_tv);
		eyeleft_tv1 = (TextView) findViewById(R.id.eyeleft_tv1);
		return_index_bt = (TextView) findViewById(R.id.return_index_bt);
		photos_iv = (ImageView) findViewById(R.id.photos_iv);
		iso_iv = (ImageView) findViewById(R.id.iso_iv);
		whitebalance_iv = (ImageView) findViewById(R.id.whitebalance_iv);
		whitebalance_rl = (RelativeLayout) findViewById(R.id.whitebalance_rl);
		linearlayou_left = (LinearLayout) findViewById(R.id.linearlayou_left);
		linearlayou_right = (LinearLayout) findViewById(R.id.linearlayou_right);
		camera_camera_tv = (TextView) findViewById(R.id.camera_camera_tv);
		TextPaint tp = camera_camera_tv.getPaint();  //安体加粗
	    tp.setFakeBoldText(true);

		photos_iv.setOnClickListener(this);
		iso_iv.setOnClickListener(this);
		whitebalance_iv.setOnClickListener(this);
		eyeleft_tv.setOnClickListener(this);
		eyeleft_tv1.setOnClickListener(this);
		return_index_bt.setOnClickListener(this);
		
		mContainer.setOnTouchListener(this);
		

		detector = new GestureDetector(this);
		
		mSaveRoot_left = "left";
		mSaveRoot_right = "right";
		setPath(leftOrRight);

		int i = dip2px(10);
		int m = px2dip(20);
//		Log.e(TAG, "i:" + i);
//		Log.e(TAG, "m:" + m);
		// mHeaderBar.getBackground().setAlpha(204);//透明0~255透明度 ，越小越透明
	}

	/**
	 * 保存图片路径
	 * 
	 * @param falt
	 *            true:左眼，false:右眼
	 */
	private void setPath(boolean falt) {
		if (falt)
			mContainer.setRootPath(mSaveRoot_left);
		else
			mContainer.setRootPath(mSaveRoot_right);
	}

	/**
	 * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
	 * 
	 * @param dipValue
	 * @return
	 */
	private int dip2px(float dipValue) {
		final float scale = getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
	
	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public int px2dip(float pxValue) {
		 final float scale = getResources().getDisplayMetrics().density;
		 return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 获取文件夹路
	 * 
	 * @param type
	 *            文件夹类
	 * @param rootPath
	 *            根目录文件夹名字 为业务流水号
	 * @return
	 */
	public static String getFolderPath(Context context, String rootPath) {
		StringBuilder pathBuilder = new StringBuilder();
		// 添加应用存储路径
		pathBuilder.append(context.getExternalFilesDir(null).getAbsolutePath());
		pathBuilder.append(File.separator);
		// 添加文件总目
		pathBuilder.append(context.getString(R.string.Files));
		pathBuilder.append(File.separator);
		// 添加当然文件类别的路
		pathBuilder.append(rootPath);
		return pathBuilder.toString();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.photos_iv:
			if ((i_left >= 6 && leftOrRight) || (i_right >= 6 && !leftOrRight)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("单侧眼睛最多拍摄六张图片,是否重拍?")
						.setPositiveButton("确认", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (leftOrRight)
									i_left = 0;
								else
									i_right = 0;
							}
						}).setNegativeButton("取消", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								startAlbumAty();
							}
						});
				builder.create().show();
			} else {
				if (leftOrRight) {
					String thumbFolder = FileOperateUtil.getFolderPath(this,
							FileOperateUtil.TYPE_THUMBNAIL, mSaveRoot_left);
					List<File> files = FileOperateUtil.listFiles(thumbFolder,
							".jpg");
					String imgFolder = FileOperateUtil.getFolderPath(this,
							FileOperateUtil.TYPE_IMAGE, mSaveRoot_left);
					List<File> imgFiles = FileOperateUtil.listFiles(imgFolder,
							".jpg");
					if (files != null) {
						if (files.size() >= 6) {
							String deleteFilePath = files.get(5)
									.getAbsolutePath();
							deleteFile(deleteFilePath);
							String deleteImgFilePath = imgFiles.get(5)
									.getAbsolutePath();
							deleteFile(deleteImgFilePath);
						}
					}
					i_left++;
				} else {
					String thumbFolder = FileOperateUtil.getFolderPath(this,
							FileOperateUtil.TYPE_THUMBNAIL, mSaveRoot_right);
					List<File> files = FileOperateUtil.listFiles(thumbFolder,
							".jpg");
					String imgFolder = FileOperateUtil.getFolderPath(this,
							FileOperateUtil.TYPE_IMAGE, mSaveRoot_right);
					List<File> imgFiles = FileOperateUtil.listFiles(imgFolder,
							".jpg");
					if (files != null) {
						if (files.size() >= 6) {
							String deleteFilePath = files.get(5)
									.getAbsolutePath();
							deleteFile(deleteFilePath);
							String deleteImgFilePath = imgFiles.get(5)
									.getAbsolutePath();
							deleteFile(deleteImgFilePath);
						}
					}
					i_right++;
				}
				photos_iv.setClickable(false);
				mContainer.takePicture(this);
			}
			break;
		case R.id.iso_iv:
			if (mExposureNum == 0) {
				mExposureNum = 3;
				iso_iv.setImageResource(R.drawable.icon_iso_iv_dark);
			} else {
				mExposureNum = 0;
				iso_iv.setImageResource(R.drawable.icon_iso_iv_bright);
			}
			mContainer.setCameraISO_int(mExposureNum);
			break;
		case R.id.whitebalance_iv:
			if (whitebalance_rl.getVisibility() == View.GONE) {
				whitebalance_rl.setVisibility(View.VISIBLE);
				whitebalance_iv.setImageResource(R.drawable.icon_whitebalance_iv_dark);
			} else {
				whitebalance_rl.setVisibility(View.GONE);
				whitebalance_iv.setImageResource(R.drawable.icon_whitebalance_iv_bright);
			}
			break;
		case R.id.eyeleft_tv:
			leftOrRight = false;
			setPath(leftOrRight);
			linearlayou_left.setVisibility(View.GONE);
			linearlayou_right.setVisibility(View.VISIBLE);
			break;
		case R.id.eyeleft_tv1:
			leftOrRight = true;
			setPath(leftOrRight);
			linearlayou_left.setVisibility(View.VISIBLE);
			linearlayou_right.setVisibility(View.GONE);
			break;
		case R.id.return_index_bt:
			finish();
			break;
		default:
			break;
		}

	}

	/**
	 * 根据路径删除指定的目录或文件，无论存在与
	 * 
	 * @param sPath
	 *            要删除的目录或文
	 * @return 删除成功返回 true，否则返 false
	 */
	public boolean deleteFolder(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 判断目录或文件是否存
		if (!file.exists()) { // 不存在返�? false
			return flag;
		} else {
			// 判断是否为文
			if (file.isFile()) { // 为文件时调用删除文件方法
				return deleteFile(sPath);
			} else { // 为目录时调用删除目录方法
				return deleteDirectory(sPath);
			}
		}
	}

	/**
	 * 删除单个文件
	 * 
	 * @param sPath
	 *            被删除文件的文件
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	/**
	 * 删除目录（文件夹）以及目录下的文
	 * 
	 * @param sPath
	 *            被删除目录的文件路径
	 * @return 目录删除成功返回true，否则返回false
	 */
	public boolean deleteDirectory(String sPath) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		boolean flag = true;
		// 删除文件夹下的所有文(包括子目)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // 删除子目
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// 删除当前目录
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 拍照结束后跳转
	 */
	@Override
	public void onTakePictureEnd(Bitmap bm) {
		photos_iv.setClickable(true);
		startAlbumAty();  //拍照完成跳转
	}
	
	/**
	 * 拍照结束后跳转到AlbumAty
	 */
	private void startAlbumAty(){
		Intent intent = new Intent(this, AlbumAty.class);
		Bundle bundle = new Bundle();
		int mexposureNum = mExposureNum; // 曝光
		bundle.putInt("mexposureNum", mexposureNum);
		intent.putExtras(bundle);
		startActivityForResult(intent, 0);
	}

	@Override
	public void onAnimtionEnd(Bitmap bm, boolean isVideo) {

	}

	/**
	 * 为了得到传回的数据，必须在前面的Activity中（指MainActivity类）重写onActivityResult方法
	 * 
	 * requestCode 请求码，即调用startActivityForResult()传过去的 resultCode
	 * 结果码，结果码用于标识返回数据来自哪个新Activity
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { // resultCode为回传的标记，回传的是RESULT_OK
		case 0:
//			Log.e(TAG,"0");
			Bundle b = data.getExtras();
			if(b!=null){
				mExposureNum = b.getInt("mexposureNum");
				deleteFile = b.getBoolean("deleteFile");
			}
			mContainer.setCameraISO_int(mExposureNum);
			break;
		case 5:
//			Log.e(TAG,"5");
			Intent intent = new Intent();
			Bundle b1 = data.getExtras();
			intent.putExtras(b1);
			this.setResult(5, intent);
			this.finish();
			break;
		case 6:
//			Log.e(TAG,"6");
			Intent intent1 = new Intent();
			Bundle b11 = data.getExtras();
			intent1.putExtras(b11);
			this.setResult(6, intent1);
			this.finish();
			break;
		default:
			break;
		}
	}

	/**
	 * 重新打开activity
	 */
	public void reload() {
	    Intent intent = getIntent();
	    overridePendingTransition(0, 0);
	    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
	    finish();
	    overridePendingTransition(0, 0);
	    startActivity(intent);
	}
	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 * 左右滑动大于或小事件处理
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e1.getX() - e2.getX() > 120) {
			linearlayou_left.setVisibility(View.GONE);
			linearlayou_right.setVisibility(View.VISIBLE);
			leftOrRight = false;
			setPath(leftOrRight);
			return true;
		} else if (e1.getX() - e2.getX() < -120) {
			linearlayou_left.setVisibility(View.VISIBLE);
			linearlayou_right.setVisibility(View.GONE);
			leftOrRight = true;
			setPath(leftOrRight);
			return true;
		}
		return false;
	}

	/**
	 * 添加触摸事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		return this.detector.onTouchEvent(event);
		// return false;
	}

	@Override
	public void onBackPressed() {
		this.finish();
		super.onBackPressed();
	}
	
	@Override
	protected void onResume() {		
		Bundle bundle = getIntent().getExtras();
		if(bundle!=null){
//			mExposureNum = bundle.getInt("mexposureNum");
			deleteFile = bundle.getBoolean("deleteFile");
//			mContainer.setCameraISO_int(mExposureNum);
		}
//		Log.e(TAG, ""+deleteFile);
		if(deleteFile){
			String rootPath_left = getFolderPath(this, mSaveRoot_left);
			deleteFolder(rootPath_left);
			String rootPath_right = getFolderPath(this, mSaveRoot_right);
			deleteFolder(rootPath_right);
		}
		super.onResume();
	}
	
	@Override
	  public boolean onTouch(View arg0, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mContainer.setOnFocus(new Point((int)event.getX(), (int)event.getY())); 
			break;

		default:
			break;
		}
	    return false;
	  }

}
