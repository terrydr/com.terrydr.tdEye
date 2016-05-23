package com.terrydr.eyeScope;

import java.io.File;
import java.util.List;
import org.apache.cordova.LOG;
import com.terrydr.eyeScope.CameraContainer.TakePictureListener;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.terrydr.eyeScope.R;

public class CameraActivity extends Activity implements View.OnClickListener,
		TakePictureListener, OnGestureListener, OnTouchListener {

	public final static String TAG = "CameraActivity";
	private CameraContainer mContainer;
	private ImageView photos_iv;
	private String mSaveRoot_left, mSaveRoot_right;
	private ImageView iso_iv, whitebalance_iv;
	private int mExposureNum = 0;
	private boolean lightOn = true;
	private RelativeLayout whitebalance_rl;
	private TextView eyeleft_tv, eyeleft_tv1,return_index_bt,camera_camera_tv;
	private LinearLayout linearlayou_left, linearlayou_right,seekBar_llayout;
	private GestureDetector detector;
	public boolean leftOrRight = true; // 默认为左 true:左眼，false:右眼
	public int i_left = 0; // 记录单侧拍摄图像个数
	public int i_right = 0; // 记录单侧拍摄图像个数
	private boolean deleteFile = true;
	private boolean isLong = false;
	private ImageView wb_iv, wb_auto_iv,wb_incandescent_iv,wb_warm_fluorescent_iv,wb_daylight_iv,wb_cloudy_daylight_iv;
	private TextView wb_tv;
	private ArcSeekBarParent arcSeekBar;
	private int wb_level;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_camera);
		
		checkWriteExternalPermission();  //判断如果用户阻止了权限给提示窗，目前紧对android6.0以上版本有效
		
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
		seekBar_llayout  = (LinearLayout) findViewById(R.id.seekBar_llayout);
		camera_camera_tv = (TextView) findViewById(R.id.camera_camera_tv);
		TextPaint tp = camera_camera_tv.getPaint();  //安体加粗
	    tp.setFakeBoldText(true);
	    
	    wb_iv = (ImageView) findViewById(R.id.wb_iv);
	    
	    wb_incandescent_iv = (ImageView) findViewById(R.id.wb_incandescent_iv);
	    wb_warm_fluorescent_iv = (ImageView) findViewById(R.id.wb_warm_fluorescent_iv);
	    wb_daylight_iv = (ImageView) findViewById(R.id.wb_daylight_iv);
	    wb_cloudy_daylight_iv = (ImageView) findViewById(R.id.wb_cloudy_daylight_iv);
	    wb_tv = (TextView) findViewById(R.id.wb_tv);
	    wb_auto_iv = (ImageView) findViewById(R.id.wb_auto_iv);

//	    wb_auto_iv.setOnClickListener(this);
//	    wb_incandescent_iv.setOnClickListener(this);
//	    wb_warm_fluorescent_iv.setOnClickListener(this);
//	    wb_daylight_iv.setOnClickListener(this);
//	    wb_cloudy_daylight_iv.setOnClickListener(this);
	    
	    arcSeekBar = (ArcSeekBarParent) findViewById(R.id.seek_bar);
	    arcSeekBar.setListener(onChang);
	    
		
		photos_iv.setOnClickListener(this);
		iso_iv.setOnClickListener(this);
		whitebalance_iv.setOnClickListener(this);
		eyeleft_tv.setOnClickListener(this);
		eyeleft_tv1.setOnClickListener(this);
		return_index_bt.setOnClickListener(this);
		photos_iv.setOnLongClickListener(onLongClickListener);
		photos_iv.setOnTouchListener(this);
		mContainer.setOnTouchListener(this);
		
		detector = new GestureDetector(this);
		
		mSaveRoot_left = "left";
		mSaveRoot_right = "right";
		setPath(leftOrRight);

		int i = dip2px(30);
		int m = px2dip(38);
		Log.e(TAG, "i:" + i);
		Log.e(TAG, "m:" + m);
		// mHeaderBar.getBackground().setAlpha(204);//透明0~255透明度 ，越小越透明
	}
	
	/**
	 * 弧形滑动改变事件
	 */
	ArcSeekBarParent.OnProgressChangedListener onChang = new ArcSeekBarParent.OnProgressChangedListener() {

		@Override
		public void OnProgressChanged(int level) {
			if ( level>= 1 && level <2 ){
				setWB(0);
				wb_level = 0;
			}else if( level>= 2 && level <3 ){
				setWB(1);
				wb_level = 1;
			}else if( level>= 3 && level <4 ){
				setWB(2);
				wb_level = 2;
			}else if( level>= 4 && level <5 ){
				setWB(3);
				wb_level = 3;
			}else if( level>= 5 && level <6 ){
				setWB(4);
				wb_level = 4;
			}
		}
	};

	final private int REQUEST_CODE_ASK_PERMISSIONS = 123; 
    private void checkWriteExternalPermission() { 
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(CameraActivity.this, 
                Manifest.permission.CAMERA); 
        LOG.e(TAG, "hasWriteContactsPermission:" + hasWriteContactsPermission + "----PackageManager.PERMISSION_GRANTED:" + PackageManager.PERMISSION_GRANTED);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) { 
        	Toast.makeText(CameraActivity.this, "未获得相机权限，请到设置中授权后再尝试。", Toast.LENGTH_SHORT) .show(); 
        	finish();
//            if (!ActivityCompat.shouldShowRequestPermissionRationale(CameraActivity.this, 
//                    Manifest.permission.CAMERA)) { 
//                showMessageOKCancel("你需要允许获取相机权限", 
//                        new DialogInterface.OnClickListener() { 
//                            @Override 
//                            public void onClick(DialogInterface dialog, int which) { 
//                                ActivityCompat.requestPermissions(CameraActivity.this, 
//                                        new String[] {Manifest.permission.CAMERA}, 
//                                        REQUEST_CODE_ASK_PERMISSIONS); 
//                            } 
//                        }); 
//                return; 
//            } 
//            ActivityCompat.requestPermissions(CameraActivity.this, 
//                    new String[] {Manifest.permission.CAMERA}, 
//                    REQUEST_CODE_ASK_PERMISSIONS); 
//            return; 
        } 
    } 
    
    /**
     * android 6.0以上版本阻止权限提示是否开启权限
     */
    @Override 
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) { 
        switch (requestCode) { 
            case REQUEST_CODE_ASK_PERMISSIONS: 
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { 
                    // Permission Granted 
                } else { 
                    // Permission Denied 
                    Toast.makeText(CameraActivity.this, "WRITE_CONTACTS Denied", Toast.LENGTH_SHORT) 
                            .show(); 
                } 
                break; 
            default: 
                super.onRequestPermissionsResult(requestCode, permissions, grantResults); 
        } 
    } 
    
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) { 
        new AlertDialog.Builder(CameraActivity.this) 
                .setMessage(message) 
                .setPositiveButton("OK", okListener) 
                .setNegativeButton("Cancel", null) 
                .create() 
                .show(); 
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
	private int px2dip(float pxValue) {
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
		int currentX = (int) view.getX();
		switch (view.getId()) {
		case R.id.photos_iv:
//			LOG.d(TAG, "点击拍照按钮");
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
								deleteFolder();
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
					deleteMultiSelectFile(mSaveRoot_left);
					i_left++;
				} else {
					deleteMultiSelectFile(mSaveRoot_right);
					i_right++;
				}
//				Log.e(TAG, "拍照.....");
				photos_iv.setEnabled(false);
				mContainer.takePicture(this);
			}
			break;
		case R.id.iso_iv:
			if (mExposureNum == 0) {
				lightOn = false;
				mExposureNum = 5;
				iso_iv.setImageResource(R.drawable.icon_iso_iv_dark);
			} else {
				lightOn = true;
				mExposureNum = 0;
				iso_iv.setImageResource(R.drawable.icon_iso_iv_bright);
			}
			mContainer.setCameraISO_int(mExposureNum,lightOn);
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
		case R.id.wb_auto_iv:
			setWB(0);
			arcSeekBar.onSmoothScroll(currentX);
			break;
		case R.id.wb_incandescent_iv:
			setWB(1);
			arcSeekBar.onSmoothScroll(currentX);
			break;
		case R.id.wb_warm_fluorescent_iv:
			setWB(2);
			arcSeekBar.onSmoothScroll(currentX);
			break;
		case R.id.wb_daylight_iv:
			setWB(3);
			arcSeekBar.onSmoothScroll(currentX);
			break;
		case R.id.wb_cloudy_daylight_iv:
			setWB(4);
			arcSeekBar.onSmoothScroll(currentX);
			break;
		default:
			break;
		}

	}
	
	/**
	    WHITE_BALANCE_AUTO              Constant Value: "auto" 				自动
		WHITE_BALANCE_INCANDESCENT      Constant Value: "incandescent"    	白炽光
		WHITE_BALANCE_FLUORESCENT		Constant Value: "fluorescent"  		日光
		WHITE_BALANCE_WARM_FLUORESCENT	Constant Value: "warm-fluorescent"  荧光
		WHITE_BALANCE_DAYLIGHT			Constant Value: "daylight" 			白天
		WHITE_BALANCE_CLOUDY_DAYLIGHT	Constant Value: "cloudy-daylight" 	多云、阴天
		WHITE_BALANCE_TWILIGHT			Constant Value: "twilight" 			黄昏
		WHITE_BALANCE_SHADE				Constant Value: "shade" 			暧荧光灯
	 */
    
	private void setWB(int value){
		switch (value) {
		case 0:
			mContainer.setWB(Camera.Parameters.WHITE_BALANCE_AUTO);
			wb_iv.setImageResource(R.drawable.wb_auto);
			wb_tv.setText("自动");
			break;
		case 1:
			mContainer.setWB(Camera.Parameters.WHITE_BALANCE_INCANDESCENT);
			wb_iv.setImageResource(R.drawable.wb_incandescent);
			wb_tv.setText("白炽灯");
			break;
		case 2:
			mContainer.setWB(Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT);
			wb_iv.setImageResource(R.drawable.wb_warm_fluorescent);
			wb_tv.setText("荧光灯");
			break;
		case 3:
			mContainer.setWB(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
			wb_iv.setImageResource(R.drawable.wb_daylight);
			wb_tv.setText("白天");
			break;
		case 4:
			mContainer.setWB(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
			wb_iv.setImageResource(R.drawable.wb_cloudy_daylight);
			wb_tv.setText("阴天");
			break;
		default:
			break;
		}
	}

	/**
	 * 提示是否重拍时删除文件夹
	 */
	private void deleteFolder() {
		String thumbFolder = this.getExternalFilesDir(null).getAbsolutePath()
				+ File.separator + this.getString(R.string.Files);
		if (leftOrRight) {
			deleteFolder(thumbFolder + File.separator + mSaveRoot_left);
			camera_camera_tv.setText(Integer.toString(i_left) + "/6");
		} else {
			deleteFolder(thumbFolder + File.separator + mSaveRoot_right);
			camera_camera_tv.setText(Integer.toString(i_right) + "/6");
		}
	}
	/**
	 * 当拍照大于6张时删除多于图片
	 * @leftOrRight 左、右眼图片路径
	 */
	public void deleteMultiSelectFile(String leftOrRight){
		String thumbFolder = FileOperateUtil.getFolderPath(this,
				FileOperateUtil.TYPE_THUMBNAIL, leftOrRight);
		List<File> files = FileOperateUtil.listFiles(thumbFolder,".jpg");
		String imgFolder = FileOperateUtil.getFolderPath(this,FileOperateUtil.TYPE_IMAGE, leftOrRight);
		List<File> imgFiles = FileOperateUtil.listFiles(imgFolder,".jpg");
		if (files != null) {
			if (files.size() >= 6) {
				String deleteFilePath = files.get(5).getAbsolutePath();
				deleteFile(deleteFilePath);
				String deleteImgFilePath = imgFiles.get(5).getAbsolutePath();
				deleteFile(deleteImgFilePath);
			}
		}
	}
	
	/**
	 * 当拍照大于6张时删除多于图片
	 * @leftOrRight 左、右眼图片路径
	 */
	public void deleteMultiSelectFile2(String leftOrRight){
		String thumbFolder = FileOperateUtil.getFolderPath(this,
				FileOperateUtil.TYPE_THUMBNAIL, leftOrRight);
		List<File> files = FileOperateUtil.listFiles(thumbFolder,".jpg");
		String imgFolder = FileOperateUtil.getFolderPath(this,FileOperateUtil.TYPE_IMAGE, leftOrRight);
		List<File> imgFiles = FileOperateUtil.listFiles(imgFolder,".jpg");
		if (files != null) {
			if (files.size() > 6) {
				String deleteFilePath = files.get(6).getAbsolutePath();
				deleteFile(deleteFilePath);
				String deleteImgFilePath = imgFiles.get(6).getAbsolutePath();
				deleteFile(deleteImgFilePath);
			}
		}
	}
	
	
	
	/**
	 * 当拍照大于6张时删除多于图片
	 * @leftOrRight 左、右眼图片路径
	 */
	public void deleteMultiSelectFile1(String leftOrRight){
		String thumbFolder = FileOperateUtil.getFolderPath(this,
				FileOperateUtil.TYPE_THUMBNAIL, leftOrRight);
		List<File> files = FileOperateUtil.listFiles(thumbFolder,".jpg");
		String imgFolder = FileOperateUtil.getFolderPath(this,FileOperateUtil.TYPE_IMAGE, leftOrRight);
		List<File> imgFiles = FileOperateUtil.listFiles(imgFolder,".jpg");
		if (files != null) {
			if (files.size() >= 6) {
				for(int i=0;i<files.size();i++){
					if(i>=6){
						String deleteFilePath = files.get(i).getAbsolutePath();
						deleteFile(deleteFilePath);
						String deleteImgFilePath = imgFiles.get(i).getAbsolutePath();
						deleteFile(deleteImgFilePath);
					}
				}
				
			}
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
		// 判断目录或文件是否存在
		if (!file.exists()) { // 不存在返回false
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
			// 删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // 删除子目录
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
//		Log.e(TAG, "拍照完成跳转");
		startAlbumAty();  //拍照完成跳转
	}
	
	/**
	 * 拍照结束后跳转到AlbumAty
	 */
	public void startAlbumAty(){
		Intent intent = new Intent(this, AlbumAty.class);
		Bundle bundle = new Bundle();
		int mexposureNum = mExposureNum; // 曝光
		bundle.putInt("mexposureNum", mexposureNum);  
		bundle.putInt("wb_level", wb_level);  
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
				wb_level = b.getInt("wb_level");
			}
			mContainer.setCameraISO_int(mExposureNum,lightOn);
			setWB(wb_level);
			photos_iv.setEnabled(true);
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
		if(e1==null || e2 == null || whitebalance_rl.getVisibility()==View.VISIBLE)
			return false;
		if (e1.getX() - e2.getX() > 120) {
			linearlayou_left.setVisibility(View.GONE);
			linearlayou_right.setVisibility(View.VISIBLE);
			leftOrRight = false;
			setPath(leftOrRight);
			setCameraText(leftOrRight);
			return true;
		} else if (e1.getX() - e2.getX() < -120) {
			linearlayou_left.setVisibility(View.VISIBLE);
			linearlayou_right.setVisibility(View.GONE);
			leftOrRight = true;
			setPath(leftOrRight);
			setCameraText(leftOrRight);
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
	}

	@Override
	public void onBackPressed() {
		finish();
		super.onBackPressed();
	}
	
	@Override
	protected void onResume() {		
		setCameraText(leftOrRight);
		Bundle bundle = getIntent().getExtras();
		if(bundle!=null){
			deleteFile = bundle.getBoolean("deleteFile");
		}
		if(deleteFile){
			String rootPath_left = getFolderPath(this, mSaveRoot_left);
			deleteFolder(rootPath_left);
			String rootPath_right = getFolderPath(this, mSaveRoot_right);
			deleteFolder(rootPath_right);
		}
		super.onResume();
	}
	
	/**
	 * 设置拍照界面的照片个数 '0/6'
	 * @param trueOrFalse  
	 */
	private void setCameraText(boolean trueOrFalse) {
		if (trueOrFalse) {
			camera_camera_tv.setText(i_left + "/6");
		} else {
			camera_camera_tv.setText(i_right + "/6");
		}
	}
	
	/**
	 * 触摸对焦
	 * 
	 */
	@Override
	  public boolean onTouch(View view, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			switch (view.getId()) {
			case R.id.container:
//				Log.d(TAG,"开始触摸对焦...");
				mContainer.setOnFocus(new Point((int)event.getX(), (int)event.getY())); 
				break;
			}
			break;
		case MotionEvent.ACTION_UP:
//			Log.d(TAG,"结束连继拍照..");
			if(isLong){
				photos_iv.setEnabled(false);
				isLong = false;
				switch (view.getId()) {
				case R.id.photos_iv:
//					Log.d(TAG,"结束连继拍照");
					if (leftOrRight) {
						mContainer.stop = i_left;
					}else
						mContainer.stop = i_right;
					stop();
					break;
				}
			}
			break;
		default:
			break;
		}
	    return false;
	  }

	/**
	 * 长按连拍事件处理
	 */
	private OnLongClickListener onLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			isLong = true;
			start();
			return true;
		}
	};
	
	/**
	 * 连拍时改变拍照张数
	 */
    public void setCount(){
		if (leftOrRight) {
			camera_camera_tv.setText(Integer.toString(++i_left) + "/6");
		} else {
			camera_camera_tv.setText(Integer.toString(++i_right) + "/6");
		}
		if ((i_left >= 6 && leftOrRight) || (i_right >= 6 && !leftOrRight)){
			stop();
        }
    }
    
    public void delectMultiFile(){
    	if (leftOrRight) {
    		deleteMultiSelectFile(mSaveRoot_left);
    	}else{
    		deleteMultiSelectFile(mSaveRoot_right);
    	}
    }
    public void delectMultiFile2(){
    	if (leftOrRight) {
    		deleteMultiSelectFile2(mSaveRoot_left);
    	}else{
    		deleteMultiSelectFile2(mSaveRoot_right);
    	}
    }
    
    /**
     * 启动连拍
     */
    public void start() {
		if ((i_left >= 6 && leftOrRight) || (i_right >= 6 && !leftOrRight)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("单侧眼睛最多拍摄六张图片,是否重拍?")
					.setPositiveButton("确认", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (leftOrRight){
								i_left = 0;
								mContainer.mNumLeft = 0;
							}else{
								i_right = 0;
								mContainer.mNumright = 0;
							}
							deleteFolder();
						}
					}).setNegativeButton("取消", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							startAlbumAty();
						}
					});
			builder.create().show();
			stop();
		} else {
			if (leftOrRight){
				mContainer.mNumLeft = i_left;
			}else{
				mContainer.mNumright = i_right;
			}
			mContainer.resumeShooting();
		}
	}
    
    /**
     * 停止连拍
     */
    public void stop(){
    	mContainer.stopShooting();

    }
}
