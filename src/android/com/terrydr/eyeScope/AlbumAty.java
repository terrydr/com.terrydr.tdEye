package com.terrydr.eyeScope;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.terrydr.eyeScope.R;

/**
 * @ClassName: AlbumAty
 * @Description: 相册Activity
 * @date 20160415
 * 
 */
public class AlbumAty extends Activity implements View.OnClickListener, AlbumGridView.OnCheckedChangeListener {
	public final static String TAG = "AlbumAty";
	/**
	 * 显示相册的View
	 */
	private AlbumGridView mAlbumView, mAlbumView_right;

	private String mSaveRoot_left = "left";
	private String mSaveRoot_right = "right";
	protected TextView left_count_tv,right_count_tv;
	protected boolean isLeft = true;
	private TextView header_bar_enter_selection;
	private TextView cance_bt,mBackView,album_image_browse_tv,album_image_browse_tv1;
	public Button commit_bt;
	private TextView cance_back_iv;
	private Bundle bundle;
	private boolean isPlugin = false;  //标记是否是plugin传过来的,默认为false:否;ure:是
	private ThumbnaiImageView view;
	private LinearLayout linearLayou_left,linearLayou_right;
	private ImageView header_bar_back_iv,header_bar_back_iv1;
	private boolean returnB = false;

	
	Set<String> itemSelectedLeft = new HashSet<String>();
	Set<String> itemSelectedRight = new HashSet<String>();

	/**
	 * 透明状态栏,透明导航栏
	 */
	@TargetApi(19)
	private void initWindow(){
	     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
	         getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
	         getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);   
	     }
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.album);

//		initWindow();

		bundle = getIntent().getExtras();
		if (bundle != null) {
			isPlugin = bundle.getBoolean("isPlugin");
		}
		linearLayou_left = (LinearLayout) findViewById(R.id.linearLayou_left);
		linearLayou_right = (LinearLayout) findViewById(R.id.linearLayou_right);
		mAlbumView = (AlbumGridView) findViewById(R.id.albumview); // 左眼
		mAlbumView_right = (AlbumGridView) findViewById(R.id.albumview_right); // 右眼
		header_bar_enter_selection = (TextView) findViewById(R.id.header_bar_enter_selection);  //选择
		cance_bt = (TextView) findViewById(R.id.cance_bt);
		commit_bt = (Button) findViewById(R.id.commit_bt);
		mBackView = (TextView) findViewById(R.id.header_bar_back);
		cance_back_iv = (TextView) findViewById(R.id.cance_back_iv);
		header_bar_back_iv = (ImageView) findViewById(R.id.header_bar_back_iv);
		header_bar_back_iv1 = (ImageView) findViewById(R.id.header_bar_back_iv1);
		left_count_tv = (TextView) findViewById(R.id.left_count_tv);
		right_count_tv = (TextView) findViewById(R.id.right_count_tv);
		left_count_tv.setText("0/2");
		right_count_tv.setText("0/2");
		left_count_tv.setVisibility(View.GONE);
		right_count_tv.setVisibility(View.GONE);
		
		album_image_browse_tv = (TextView) findViewById(R.id.album_image_browse_tv);
		album_image_browse_tv1 = (TextView) findViewById(R.id.album_image_browse_tv1);
		TextPaint tp = album_image_browse_tv.getPaint();  //安体加粗
	    tp.setFakeBoldText(true);
		TextPaint tp1 = album_image_browse_tv1.getPaint();  //安体加粗
	    tp1.setFakeBoldText(true);

	    header_bar_enter_selection.setOnClickListener(this);
		cance_back_iv.setOnClickListener(this);
		cance_bt.setOnClickListener(this);
		mBackView.setOnClickListener(this);
		commit_bt.setOnClickListener(this);
		header_bar_back_iv.setOnClickListener(this);
		header_bar_back_iv1.setOnClickListener(this);
		
		mAlbumView.setOnItemClickListener(mAlbumView_left_listener);
		mAlbumView_right.setOnItemClickListener(mAlbumView_right_listener);

		Set<String> itemSelectedSet = new HashSet<String>();
		loadAlbumBySelectImage(mSaveRoot_left, ".jpg", mAlbumView, itemSelectedSet);
		loadAlbumBySelectImage(mSaveRoot_right, ".jpg", mAlbumView_right, itemSelectedSet);
	}
	
	/**
	 * 点击左眼图片缩略图事件
	 */
	private OnItemClickListener mAlbumView_left_listener = new OnItemClickListener() {
		@Override  
		public void onItemClick(AdapterView<?> parent, View _view, int position,
				long id) {
			isLeft = true;
			if (mAlbumView.getEditable()){
				view = (ThumbnaiImageView) _view;
				boolean isChecked  = view.checkBox.isChecked();
				if(itemSelectedLeft.size()>=2 && !isChecked){
					AlertDialog.Builder builder = new AlertDialog.Builder(AlbumAty.this);
					builder.setMessage("单侧眼睛最多选择两张图片")
							.setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							});
					builder.create().show();
				}else{
//					Log.e(TAG, "itemSelectedLeft:"+itemSelectedLeft);
					if(isChecked){
						itemSelectedLeft.remove(_view.getTag().toString());
						view.checkBox.setChecked(false);
						view.checkBox.setVisibility(View.GONE);
					}else{
						itemSelectedLeft.add(_view.getTag().toString());
						view.checkBox.setChecked(true);
						view.checkBox.setVisibility(View.VISIBLE);
					}
//					Log.e(TAG, "selectPathsLeft:"+itemSelectedLeft);
					left_count_tv.setText(itemSelectedLeft.size() + "/2");
				}
				return;
			}
			Intent intent = new Intent(AlbumAty.this, AlbumItemAty.class);
			intent.putExtra("path", _view.getTag().toString());
			intent.putExtra("root", mSaveRoot_left);
			startActivityForResult(intent, 0);
		}
	};
	
	/**
	 * 点击右眼图片缩略图事件
	 */
	private OnItemClickListener mAlbumView_right_listener = new OnItemClickListener() {
		@Override  
		public void onItemClick(AdapterView<?> parent, View _view, int position,
				long id) {
			isLeft = false;
			if (mAlbumView_right.getEditable()){
				view = (ThumbnaiImageView) _view;
				boolean isChecked  = view.checkBox.isChecked();
				if(itemSelectedRight.size()>=2 && !isChecked){
					AlertDialog.Builder builder = new AlertDialog.Builder(AlbumAty.this);
					builder.setMessage("单侧眼睛最多选择两张图片")
							.setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							});
					builder.create().show();
				}else{
					if(isChecked){
						itemSelectedRight.remove(_view.getTag().toString());
						view.checkBox.setChecked(false);
						view.checkBox.setVisibility(View.GONE);
					}else{
						itemSelectedRight.add(_view.getTag().toString());
						view.checkBox.setChecked(true);
						view.checkBox.setVisibility(View.VISIBLE);
					}
					right_count_tv.setText(itemSelectedRight.size() + "/2");
				}
				return;
			}
			
			Intent intent = new Intent(AlbumAty.this, AlbumItemAty.class);
			intent.putExtra("path", _view.getTag().toString());
			intent.putExtra("root", mSaveRoot_right);
			startActivityForResult(intent, 0);
		}
	};

	/**
	 * 加载图片
	 * 
	 * @param rootPath
	 *            根目录文件夹
	 * @param format
	 *            要加载的文件格式
	 */
	public void loadAlbum(String rootPath, String format, AlbumGridView alvumGridView) {
		// 获取根目录下缩略图文件夹
		String thumbFolder = FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_THUMBNAIL, rootPath);
		List<File> files = FileOperateUtil.listFiles(thumbFolder, format);
		if (files != null && files.size() > 0) {
			List<String> paths = new ArrayList<String>();
			for (File file : files) {
				paths.add(file.getAbsolutePath());
			}
			alvumGridView.setAdapter(alvumGridView.new AlbumViewAdapter(paths));
		}
	}

	@Override
	protected void onResume() {
		// loadAlbum(mSaveRoot_left, ".jpg", mAlbumView);
		// loadAlbum(mSaveRoot_right, ".jpg", mAlbumView_right);

		super.onResume();
	}

	/**
	 * 选择事件
	 */
	private void enterEdit() {
		if (mAlbumView.getChildCount() > 0) {
			mAlbumView.setEditable(true, this,true);
		}
		if (mAlbumView_right.getChildCount() > 0) {
			mAlbumView_right.setEditable(true, this,true);
		}
		left_count_tv.setVisibility(View.VISIBLE);
		right_count_tv.setVisibility(View.VISIBLE);
		commit_bt.setVisibility(View.VISIBLE);
		findViewById(R.id.header_bar_navi).setVisibility(View.GONE);
		findViewById(R.id.header_bar_select).setVisibility(View.VISIBLE);
	}

	/**
	 * 取消选择事件
	 */
	private void leaveEdit() {
		if (mAlbumView.getChildCount() > 0) {
			mAlbumView.setEditable(false,false);
		}
		if (mAlbumView_right.getChildCount() > 0) {
			mAlbumView_right.setEditable(false,false);
		}
		
//		selectPathsLeft.clear();
//		selectPathsRight.clear();
		itemSelectedLeft = new HashSet<String>();
		itemSelectedRight = new HashSet<String>();
		left_count_tv.setVisibility(View.GONE);
		right_count_tv.setVisibility(View.GONE);
		commit_bt.setVisibility(View.GONE);
		findViewById(R.id.header_bar_navi).setVisibility(View.VISIBLE);
		findViewById(R.id.header_bar_select).setVisibility(View.GONE);
		left_count_tv.setText("0/2");
		right_count_tv.setText("0/2");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.header_bar_enter_selection:
			enterEdit();
			break;
		case R.id.cance_back_iv:
//			leaveEdit();
			backPrevious();
			break;
		case R.id.header_bar_back_iv1:
			backPrevious();
			break;
		case R.id.cance_bt:
			leaveEdit();
			break;
		case R.id.commit_bt:
			String imageNmae = this.getString(R.string.Image);
			String thumbnail = this.getString(R.string.Thumbnail);
			JSONObject result_Json = new JSONObject();
//			Set<String> left_mAlbumView = mAlbumView.getSelectedItems(); 
			Set<String> left_mAlbumView = itemSelectedLeft; 
			if (left_mAlbumView != null) {
				if (!left_mAlbumView.isEmpty()) {
					JSONArray path = new JSONArray();
					for (Object p : left_mAlbumView.toArray()) {
						String imagePath = String.valueOf(p);
						String repImagePath = imagePath.replace(thumbnail, imageNmae);
						path.put(repImagePath);
					}
					try {
						result_Json.put("leftEye", path);
					} catch (JSONException e) {
						Log.e(TAG, e.toString());
					}
				}
			}
//			Set<String> right_mAlbumView = mAlbumView_right.getSelectedItems();
			Set<String> right_mAlbumView = itemSelectedRight;
			if (right_mAlbumView != null) {
				if (!right_mAlbumView.isEmpty()) {
					JSONArray path = new JSONArray();
					for (Object p : right_mAlbumView.toArray()) {
						String imagePath = String.valueOf(p);
						String repImagePath = imagePath.replace(thumbnail, imageNmae);
						path.put(repImagePath);
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
//			Log.e(TAG, "5");
			Intent intent1 = new Intent();
			Bundle bundle1 = new Bundle();
			bundle1.putString("result_Json", result_Json.toString());
			intent1.putExtras(bundle1);
			this.setResult(5, intent1);
			this.finish();
			break;
		case R.id.header_bar_back:
			backPrevious();
			break;
		case R.id.header_bar_back_iv:
			backPrevious();
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		if (mAlbumView.getEditable()) {
			leaveEdit();
			return;
		}
		backPrevious();
		super.onBackPressed();
	}

	/**
	 * 返回上一个activity
	 */
	private void backPrevious() {
		Intent intent = null;
//		Log.e(TAG, "" + isPlugin);
		if (!isPlugin) {
			intent = new Intent(AlbumAty.this, CameraActivity.class);
			if (bundle != null) {
				bundle.putBoolean("deleteFile", false);
				intent.putExtras(bundle);

			}
			// 设置返回数据
			this.setResult(0, intent);
			this.finish();
		} else {
			intent = new Intent(AlbumAty.this, CameraActivity.class);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { // resultCode为回传的标记，回传的是RESULT_OK
		case 0:
			Bundle b = data.getExtras();
			ArrayList<String> selectPaths = new ArrayList<String>();
			ArrayList<String> selectPathsLeft = new ArrayList<String>();
			ArrayList<String> selectPathsRight = new ArrayList<String>();
			int selectPathsLeftSize = 0;
			int selectPathsRightSize = 0;
			
			if (b != null) {
				selectPaths = b.getStringArrayList("selectPaths");
				selectPathsLeft = b.getStringArrayList("selectPathsLeft");
				selectPathsRight = b.getStringArrayList("selectPathsRight");
				selectPathsLeftSize = b.getInt("selectPathsLeftSize");
				selectPathsRightSize = b.getInt("selectPathsRightSize");
			}
			String imageNmae = this.getString(R.string.Image);
			String thumbnail = this.getString(R.string.Thumbnail);
			Set<String> s = new HashSet<String>();
			Set<String> sl = new HashSet<String>();
			Set<String> sr = new HashSet<String>();
			for (String path : selectPaths) {
				String repImagePath = path.replace(imageNmae, thumbnail);
				s.add(repImagePath);
			}
			for (String path : selectPathsLeft) {
				String repImagePath = path.replace(imageNmae, thumbnail);
				sl.add(repImagePath);
				itemSelectedLeft.add(repImagePath);
			}
			for (String path : selectPathsRight) {
				String repImagePath = path.replace(imageNmae, thumbnail);
				sr.add(repImagePath);
				itemSelectedRight.add(repImagePath);
			}
			if (s.isEmpty()) {
				leaveEdit();
			} else {
				enterEdit();
			}
			returnB = true;
			loadAlbumBySelectImage(mSaveRoot_left, ".jpg", mAlbumView, sl);
			loadAlbumBySelectImage(mSaveRoot_right, ".jpg", mAlbumView_right, sr);
			left_count_tv.setText(selectPathsLeftSize + "/2");
			right_count_tv.setText(selectPathsRightSize + "/2");
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
			break;
		default:
			break;
		}
	}

	/**
	 * 加载图片
	 * 
	 * @param rootPath
	 *            根目录文件夹
	 * @param format
	 *            要加载的文件格式
	 */
	public void loadAlbumBySelectImage(String rootPath, String format, AlbumGridView alvumGridView, Set<String> _itemSelectedSet) {
		// 获取根目录下缩略图文件夹
		String thumbFolder = FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_THUMBNAIL, rootPath);
		List<File> files = FileOperateUtil.listFiles(thumbFolder, format);
		if (files != null && files.size() > 0) {
			if(rootPath.equals(mSaveRoot_left)){
				linearLayou_left.setVisibility(View.VISIBLE);
			}else if(rootPath.equals(mSaveRoot_right)){
				linearLayou_right.setVisibility(View.VISIBLE);
			}
			List<String> paths = new ArrayList<String>();
			for (File file : files) {
				paths.add(file.getAbsolutePath());
			}
			alvumGridView.setAdapter(alvumGridView.new AlbumViewAdapter(this,paths, _itemSelectedSet));
		}
	}

	@Override
	public void onCheckedChanged(Set<String> set) {
		// Log.e(TAG, "onCheckedChanged" + set.toString());
		// mAlbumView.getSelectedItem();
	}

}
