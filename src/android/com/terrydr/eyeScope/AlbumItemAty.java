package com.terrydr.eyeScope;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.terrydr.eyeScope.MatrixImageView.OnSingleTapListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.terrydr.eyeScope.R;
/** 
 * @ClassName: AlbumItemAty 
 * @Description:相册图片大图Activity 包含图片编辑功能
 * @date 20160419
 *  
 */
public class AlbumItemAty extends Activity implements OnClickListener,OnSingleTapListener{
	public final static String TAG="AlbumDetailAty";
	private String mSaveRoot;
	private AlbumViewPager mViewPager;//显示大图
	private TextView mBackView,header_bar_photo_commit_bt;
	private TextView mCountView,selected_tv,eye_left_select_count_tv,eye_right_select_count_tv,
					eye_left_tv,eye_right_tv;
	private View mHeaderBar,mBottomBar;
	private ImageView header_bar_photo_back_iv;
	private ViewGroup group;
	private CheckBox albumitem_selected_cb;
	private String[] albumitem_selected_cb_bool;  
	private List<String> paths;
	private List<File> files;
	private ArrayList<String> selectPaths = new ArrayList<String>();// 选中的图片
	private ArrayList<String> selectPathsLeft = new ArrayList<String>();// 选中的图片  左眼图片
	private ArrayList<String> selectPathsRight = new ArrayList<String>();// 选中的图片  右眼图片
	private boolean leftOrRight = true;
	private Bundle bundle;
	
	private boolean isPlugin = false;  //标记是否是plugin传过来的,默认为false:否;ure:是
	/** 
     * 装点点的ImageView数组 
     */  
    private ImageView[] tips;  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.albumitem);

		bundle = getIntent().getExtras();
		if (bundle != null) {
			isPlugin = bundle.getBoolean("isPlugin");
		}
		group = (ViewGroup)findViewById(R.id.imagegroup_ll); 
		mViewPager=(AlbumViewPager)findViewById(R.id.albumviewpager);
		mBackView=(TextView)findViewById(R.id.header_bar_photo_back);
		header_bar_photo_back_iv=(ImageView)findViewById(R.id.header_bar_photo_back_iv);
		mCountView=(TextView)findViewById(R.id.header_bar_photo_count);
		mHeaderBar=findViewById(R.id.album_item_header_bar);
		mBottomBar=findViewById(R.id.album_item_bottom_bar);
		albumitem_selected_cb = (CheckBox)findViewById(R.id.albumitem_selected_cb);
		header_bar_photo_commit_bt = (TextView) findViewById(R.id.header_bar_photo_commit_bt);
		selected_tv= (TextView)findViewById(R.id.selected_tv);
		eye_left_tv = (TextView)findViewById(R.id.eye_left_tv);
		eye_right_tv = (TextView)findViewById(R.id.eye_right_tv);
		eye_left_select_count_tv = (TextView)findViewById(R.id.eye_left_select_count_tv);
		eye_right_select_count_tv = (TextView)findViewById(R.id.eye_right_select_count_tv);
		eye_left_select_count_tv.setText("0/2");
		eye_right_select_count_tv.setText("0/2");
		
		TextPaint tp = mCountView.getPaint();  //字体加粗
	    tp.setFakeBoldText(true);

	    header_bar_photo_commit_bt.setOnClickListener(this);
	    albumitem_selected_cb.setOnClickListener(this);
		mBackView.setOnClickListener(this);
		mCountView.setOnClickListener(this);
		header_bar_photo_back_iv.setOnClickListener(this);

		mSaveRoot=getIntent().getExtras().getString("root");
		mViewPager.setOnPageChangeListener(pageChangeListener);
		String currentFileName=null;
		if(getIntent().getExtras()!=null)
			currentFileName=getIntent().getExtras().getString("path");
		if(currentFileName!=null){
			File file=new File(currentFileName);
			currentFileName=file.getName();
			if(currentFileName.indexOf(".")>0)
				currentFileName=currentFileName.substring(0, currentFileName.lastIndexOf("."));
		}
		loadAlbum(mSaveRoot,currentFileName);
		
//		mViewPager.setOnSlideUpListener(AlbumItemAty.this);
	}
	
	public void reloadAlbum(List<String> paths, String deletePath,String selectPath,int deleteCurretItem,int getCurrentItem) {
		if(paths.isEmpty()){
			backPrevious();
			return;
		}
		
		// 获取根目录下缩略图文件夹
		String folder = FileOperateUtil.getFolderPath(this,FileOperateUtil.TYPE_IMAGE, "left");
		// 获取图片文件大图
		List<File> imageList = FileOperateUtil.listFiles(folder, ".jpg");

		String folderRight = FileOperateUtil.getFolderPath(this,FileOperateUtil.TYPE_IMAGE, "right");
		List<File> imageListRight = FileOperateUtil.listFiles(folderRight,".jpg");

		files = new ArrayList<File>();
		if (imageList != null && imageList.size() > 0) {
			files.addAll(imageList);
			eye_left_tv.setVisibility(View.VISIBLE);
			eye_left_select_count_tv.setVisibility(View.VISIBLE);
		} else {
			eye_left_tv.setVisibility(View.GONE);
			eye_left_select_count_tv.setVisibility(View.GONE);
		}
		FileOperateUtil.sortList(files, true);

		List<File> filesRight = new ArrayList<File>();
		if (imageListRight != null && imageListRight.size() > 0) {
			filesRight.addAll(imageListRight);
			eye_right_tv.setVisibility(View.VISIBLE);
			eye_right_select_count_tv.setVisibility(View.VISIBLE);
		} else {
			eye_right_tv.setVisibility(View.GONE);
			eye_right_select_count_tv.setVisibility(View.GONE);
		}
		FileOperateUtil.sortList(filesRight, true);

		files.addAll(filesRight); // 把右眼图片添加到左眼
		// 将点点加入到ViewGroup中
		tips = new ImageView[files.size()];

		List<String> new_albumitem_selected_cb_bool = new ArrayList<String>();

		for (int i = 0; i < albumitem_selected_cb_bool.length; i++) {
			if (i != deleteCurretItem) {
				new_albumitem_selected_cb_bool.add(albumitem_selected_cb_bool[i]);
			}
		}
		albumitem_selected_cb_bool = new String[new_albumitem_selected_cb_bool.size()];
		if (files.size() > 0) {
			group.removeAllViews();
			paths = new ArrayList<String>();
			int i = 0;
			for (File file : files) {
				if (selectPath != null && file.getName().contains(selectPath))
				paths.add(file.getAbsolutePath());

				ImageView imageView = new ImageView(this);
				imageView.setLayoutParams(new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				tips[i] = imageView;
				albumitem_selected_cb_bool[i] = String.valueOf(new_albumitem_selected_cb_bool.toArray()[i]);
				if(albumitem_selected_cb_bool[i].equals("true")){
					tips[i].setBackgroundResource(R.drawable.albumitem_selected_status);
				}else
					tips[i].setBackgroundResource(R.drawable.albumitem_unselect_status);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT));
				layoutParams.leftMargin = 10;
				layoutParams.rightMargin = 10;
				group.addView(imageView, layoutParams);
				i++;
			}
			tips[getCurrentItem].setBackgroundResource(R.drawable.albumitem_selected_current);
			setCountView();
			
			setImageBackground(getCurrentItem);
			setCheckBoxSelected(getCurrentItem);
			if (deletePath.contains("left")) {
				selectPathsLeft.remove(deletePath);
			} else if (deletePath.contains("right")) {
				selectPathsRight.remove(deletePath);
			}

			eye_left_select_count_tv.setText(selectPathsLeft.size()+ "/2");
			eye_right_select_count_tv.setText(selectPathsRight.size()+ "/2");
//			if(selectPathsLeft.size()>0){
//				eye_left_tv.setVisibility(View.VISIBLE);
//				eye_left_select_count_tv.setVisibility(View.VISIBLE);
//			}else{
//				eye_left_tv.setVisibility(View.GONE);
//				eye_left_select_count_tv.setVisibility(View.GONE);
//			}
//			if(selectPathsRight.size()>0){
//				eye_right_tv.setVisibility(View.VISIBLE);
//				eye_right_select_count_tv.setVisibility(View.VISIBLE);
//			}else{
//				eye_right_tv.setVisibility(View.GONE);
//				eye_right_select_count_tv.setVisibility(View.GONE);
//			}
		}
	}
	
	/**  
	 *  加载图片
	 *  @param rootPath   图片根路
	 */
	public void loadAlbum(String rootPath,String fileName){
		//获取根目录下缩略图文件夹
		String folder=FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_IMAGE, "left");
		//获取图片文件大图
		List<File> imageList=FileOperateUtil.listFiles(folder, ".jpg");
		
		String folderRight=FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_IMAGE, "right");
		List<File> imageListRight=FileOperateUtil.listFiles(folderRight, ".jpg");
		
		files=new ArrayList<File>();
		if(imageList!=null&&imageList.size()>0){
			files.addAll(imageList);
			eye_left_tv.setVisibility(View.VISIBLE);
			eye_left_select_count_tv.setVisibility(View.VISIBLE);
		} else {
			eye_left_tv.setVisibility(View.GONE);
			eye_left_select_count_tv.setVisibility(View.GONE);
		}
		FileOperateUtil.sortList(files, true);
		
		List<File> filesRight=new ArrayList<File>();
		if(imageListRight!=null&&imageListRight.size()>0){
			filesRight.addAll(imageListRight);
			eye_right_tv.setVisibility(View.VISIBLE);
			eye_right_select_count_tv.setVisibility(View.VISIBLE);
		} else {
			eye_right_tv.setVisibility(View.GONE);
			eye_right_select_count_tv.setVisibility(View.GONE);
		}
		FileOperateUtil.sortList(filesRight, true);
		
		files.addAll(filesRight);  //把右眼图片添加到左眼
        //将点点加入到ViewGroup中  
        tips = new ImageView[files.size()];  
        albumitem_selected_cb_bool = new String[files.size()];
		
		if(files.size()>0){
			paths=new ArrayList<String>();
			int currentItem=0;
			int i = 0;
			for (File file : files) {
				if(fileName!=null && file.getName().contains(fileName))
					currentItem=files.indexOf(file);
				paths.add(file.getAbsolutePath());

				ImageView imageView = new ImageView(this);
				imageView.setLayoutParams(new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				tips[i] = imageView;
				albumitem_selected_cb_bool[i] = "false";
				tips[i].setBackgroundResource(R.drawable.albumitem_unselect_status);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				layoutParams.leftMargin = 10;
				layoutParams.rightMargin = 10;
				group.addView(imageView, layoutParams);
				i++;
			}
			mViewPager.setAdapter(mViewPager.new ViewPagerAdapter(this,paths));
//			mViewPager.setCurrentItem(currentItem);
//			tips[currentItem].setBackgroundResource(R.drawable.albumitem_selected_current);
			mViewPager.setCurrentItem(0);
			tips[0].setBackgroundResource(R.drawable.albumitem_selected_current);
			setCountView();
		}
	}

	/**
	 * 设置图片标题文字 
	 */
	private void setCountView(){
		if(mViewPager.getAdapter()!=null){
			String filePath = mViewPager.getSelectPath();
			if(filePath.contains("left")){
				leftOrRight = true;
				mCountView.setText("左眼");
			}else if(filePath.contains("right")){
				leftOrRight = false;
				mCountView.setText("右眼");
			}
		}
	}
	/** 
     * 设置标题imageview的状态 
     * @param selectItems 
     */  
	private void setImageBackground(int selectItems) {
		for (int i = 0; i < tips.length; i++) {
			if (i == selectItems) {
				tips[i].setBackgroundResource(R.drawable.albumitem_selected_current);
			} else {
				if (albumitem_selected_cb_bool[i].equals("true")) {
					tips[i].setBackgroundResource(R.drawable.albumitem_selected_status);
				} else {
					tips[i].setBackgroundResource(R.drawable.albumitem_unselect_status);
				}

			}
		}
	}
    
	/** 
     * 设置选中的tip的背景 
     * @param selectItems 
     */  
    private void setCheckBoxSelected(int selectItems){  
        for(int i=0; i<albumitem_selected_cb_bool.length; i++){  
            if(i == selectItems && albumitem_selected_cb_bool[i].equals("true")){  
            	albumitem_selected_cb.setChecked(true);
            	break;
            }else{  
            	albumitem_selected_cb.setChecked(false); 
            }  
        }  
    } 
	
    /**
     * 图片左右滑动改变事件
     */
	private OnPageChangeListener pageChangeListener=new OnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			setCountView();
			if(tips[position]!=null){
				setImageBackground(position);  //设置选中状态
				setCheckBoxSelected(position);  // 设置checkbox是否选中 
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	};

	@Override
	public void onSingleTap() {
		if(mHeaderBar.getVisibility()==View.VISIBLE){
			AlphaAnimation animation=new AlphaAnimation(1, 0);
			animation.setDuration(300);
			mHeaderBar.startAnimation(animation);
			mBottomBar.startAnimation(animation);
			mHeaderBar.setVisibility(View.GONE);
			mBottomBar.setVisibility(View.GONE);
		}else {
			AlphaAnimation animation=new AlphaAnimation(0, 1);
			animation.setDuration(300);
			mHeaderBar.startAnimation(animation);
			mBottomBar.startAnimation(animation);
			mHeaderBar.setVisibility(View.VISIBLE);
			mBottomBar.setVisibility(View.VISIBLE);
		}	
	}

	/**
	 * 返回上一个activity
	 */
	private void backPrevious(){
		Intent intent = null;
//		Bundle bundle = new Bundle();
//		bundle.putStringArrayList("selectPaths", selectPaths);
//		bundle.putStringArrayList("selectPathsLeft", selectPathsLeft);
//		bundle.putStringArrayList("selectPathsRight", selectPathsRight);
//		bundle.putInt("selectPathsLeftSize", selectPathsLeft.size());
//		bundle.putInt("selectPathsRightSize", selectPathsRight.size());
//		intent.putExtras(bundle);
		
//		if (bundle != null) {
//			intent = new Intent();
//			bundle.putBoolean("deleteFile", false);
//			intent.putExtras(bundle);
//		}
//		this.setResult(0, intent);
//		this.finish();
		
		if (!isPlugin) {
			intent = new Intent(AlbumItemAty.this, CameraActivity.class);
			if (bundle != null) {
				bundle.putBoolean("deleteFile", false);
				intent.putExtras(bundle);

			}
			// 设置返回数据
			this.setResult(0, intent);
			this.finish();
		} else {
			intent = new Intent(AlbumItemAty.this, CameraActivity.class);
			if (bundle != null) {
				bundle.putBoolean("deleteFile", false);
				intent.putExtras(bundle);
			}
			// 设置返回数据
			this.setResult(6, intent);
			this.finish();
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.header_bar_photo_back:
			backPrevious();
			break;
		case R.id.header_bar_photo_back_iv:
			backPrevious();
			break; 
		case R.id.albumitem_selected_cb:
			if (selectPathsLeft.size() >= 2 &&leftOrRight && albumitem_selected_cb.isChecked() || selectPathsRight.size() >= 2 && !leftOrRight&&albumitem_selected_cb.isChecked()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("单侧眼睛最多选择两张图片").setPositiveButton("确定",
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								albumitem_selected_cb.setChecked(false);
							}
						});
				builder.create().show();
			} else {
				if (mViewPager.getAdapter() != null) {
					String filePath = mViewPager.getSelectPath();
//					Log.e(TAG, "filePath:" + filePath);
					int index = 0;
					for (File file : files) {
//						Log.e(TAG, "file:" + file.getName());
						if (filePath != null && filePath.contains(file.getName())) {
							index = files.indexOf(file);
							if (albumitem_selected_cb.isChecked()) {
								albumitem_selected_cb_bool[index] = "true";
								selectPaths.add(filePath);
								if (filePath.contains("left")) {
									selectPathsLeft.add(filePath);
								} else if (filePath.contains("right")) {
									selectPathsRight.add(filePath);
								}
							} else {
								albumitem_selected_cb_bool[index] = "false";
								selectPaths.remove(filePath);
								if (filePath.contains("left")) {
									selectPathsLeft.remove(filePath);
								} else if (filePath.contains("right")) {
									selectPathsRight.remove(filePath);
								}
							}
						}
					}
					eye_left_select_count_tv.setText(selectPathsLeft.size()+ "/2");
					eye_right_select_count_tv.setText(selectPathsRight.size()+ "/2");
//					if(selectPathsLeft.size()>0){
//						eye_left_tv.setVisibility(View.VISIBLE);
//						eye_left_select_count_tv.setVisibility(View.VISIBLE);
//					}else{
//						eye_left_tv.setVisibility(View.GONE);
//						eye_left_select_count_tv.setVisibility(View.GONE);
//					}
//					if(selectPathsRight.size()>0){
//						eye_right_tv.setVisibility(View.VISIBLE);
//						eye_right_select_count_tv.setVisibility(View.VISIBLE);
//					}else{
//						eye_right_tv.setVisibility(View.GONE);
//						eye_right_select_count_tv.setVisibility(View.GONE);
//					}
				}
			}
			break;
		case R.id.header_bar_photo_commit_bt:
			commitOnClick();
			break;
		default:
			break;
		}
	}
	
	/**
	 * 提交事件
	 */
	private void commitOnClick(){
		JSONObject result_Json = new JSONObject();
		if (selectPathsLeft != null) {
			if (!selectPathsLeft.isEmpty()) {
				JSONArray path = new JSONArray();
				for (String p : selectPathsLeft) {
					path.put(p);
				}
				try {
					result_Json.put("leftEye", path);
				} catch (JSONException e) {
					Log.e(TAG, e.toString());
				}
			}
		}
		if (selectPathsRight != null) {
			if (!selectPathsRight.isEmpty()) {
				JSONArray path = new JSONArray();
				for (String p : selectPathsRight) {
					path.put(p);
				}
				try {
					result_Json.put("rightEye", path);
				} catch (JSONException e) {
					Log.e(TAG, e.toString());
				}
			}
		}
		if(result_Json.length()==0){
			Toast.makeText(getApplicationContext(), "请选择图片再提交!",Toast.LENGTH_SHORT).show();
			return;
		}
//		Log.e(TAG, "5");
		Log.e(TAG, "result_Json.toString():" + result_Json.toString());
		Intent intent1 = new Intent();
		Bundle bundle1 = new Bundle();
		bundle1.putString("result_Json", result_Json.toString());
		intent1.putExtras(bundle1);
		this.setResult(5, intent1);
		this.finish();
		
//		Intent intents = new Intent(AlbumItemAty.this,AlbumItemAtyForJs.class);
//		startActivity(intents);
		
		
	}
	@Override
	public void onBackPressed() {
		backPrevious();
		super.onBackPressed();
	}
	@Override
	protected void onStop() {
		super.onStop();
	}

	public void onChangeTesChanged(String _text) {
		selected_tv.setText(_text);
	}
	
}
