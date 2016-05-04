package com.terrydr.eyeScope;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
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

	private TextView mEnterView;
	private TextView cance_bt;
	private Button commit_bt;
	private ImageView mBackView, cance_back_iv;
	private Bundle bundle;
	private boolean isPlugin = false;  //标记是否是plugin传过来的,默认为false:否;ure:是
	ThumbnaiImageView view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.album);

		bundle = getIntent().getExtras();
		if (bundle != null) {
			isPlugin = bundle.getBoolean("isPlugin");
		}

		mAlbumView = (AlbumGridView) findViewById(R.id.albumview); // 左眼
		mAlbumView_right = (AlbumGridView) findViewById(R.id.albumview_right); // 右眼
		mEnterView = (TextView) findViewById(R.id.header_bar_enter_selection);
		cance_bt = (TextView) findViewById(R.id.cance_bt);
		commit_bt = (Button) findViewById(R.id.commit_bt);
		mBackView = (ImageView) findViewById(R.id.header_bar_back);
		cance_back_iv = (ImageView) findViewById(R.id.cance_back_iv);

		mEnterView.setOnClickListener(this);
		cance_back_iv.setOnClickListener(this);
		cance_bt.setOnClickListener(this);
		mBackView.setOnClickListener(this);
		commit_bt.setOnClickListener(this);

		mAlbumView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (mAlbumView.getEditable()){
					view = (ThumbnaiImageView) arg1;
					boolean isChecked  = view.checkBox.isChecked();
					if(isChecked){
						view.checkBox.setChecked(false);
					}else{
						view.checkBox.setChecked(true);
					}
					return;
				}
				Intent intent = new Intent(AlbumAty.this, AlbumItemAty.class);
				intent.putExtra("path", arg1.getTag().toString());
				intent.putExtra("root", mSaveRoot_left);
				startActivityForResult(intent, 0);
			}
		});

		mAlbumView_right.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (mAlbumView_right.getEditable()){
					view = (ThumbnaiImageView) arg1;
					boolean isChecked  = view.checkBox.isChecked();
					if(isChecked){
						view.checkBox.setChecked(false);
					}else{
						view.checkBox.setChecked(true);
					}
					return;
				}
				Intent intent = new Intent(AlbumAty.this, AlbumItemAty.class);
				intent.putExtra("path", arg1.getTag().toString());
				intent.putExtra("root", mSaveRoot_right);
				startActivityForResult(intent, 0);
			}
		});
//		 mAlbumView.setOnItemLongClickListener(new OnItemLongClickListener() {
//		 @Override
//		 public boolean onItemLongClick(AdapterView<?> parent, View view,
//		 int position, long id) {
//		
//		 if (mAlbumView.getEditable())
//		 return true;
//		 // enterEdit();
//		 return true;
//		 }
//		 });
		Set<String> itemSelectedSet = new HashSet<String>();
		loadAlbum1(mSaveRoot_left, ".jpg", mAlbumView, itemSelectedSet);
		loadAlbum1(mSaveRoot_right, ".jpg", mAlbumView_right, itemSelectedSet);
	}

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
			mAlbumView.setEditable(true, this);
		}
		if (mAlbumView_right.getChildCount() > 0) {
			mAlbumView_right.setEditable(true, this);
		}
		commit_bt.setVisibility(View.VISIBLE);
		findViewById(R.id.header_bar_navi).setVisibility(View.GONE);
		findViewById(R.id.header_bar_select).setVisibility(View.VISIBLE);
	}

	/**
	 * 取消选择事件
	 */
	private void leaveEdit() {
		if (mAlbumView.getChildCount() > 0) {
			mAlbumView.setEditable(false);
		}
		if (mAlbumView_right.getChildCount() > 0) {
			mAlbumView_right.setEditable(false);
		}
		commit_bt.setVisibility(View.INVISIBLE);
		findViewById(R.id.header_bar_navi).setVisibility(View.VISIBLE);
		findViewById(R.id.header_bar_select).setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.header_bar_enter_selection:
			enterEdit();
			break;
		case R.id.cance_back_iv:
			leaveEdit();
			break;
		case R.id.cance_bt:
			leaveEdit();
			break;
		case R.id.commit_bt:
			String imageNmae = this.getString(R.string.Image);
			String thumbnail = this.getString(R.string.Thumbnail);
			JSONObject result_Json = new JSONObject();
			Set<String> left_mAlbumView = mAlbumView.getSelectedItems();
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
			Set<String> right_mAlbumView = mAlbumView_right.getSelectedItems();
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
			if (b != null) {
				selectPaths = b.getStringArrayList("selectPaths");
			}
			String imageNmae = this.getString(R.string.Image);
			String thumbnail = this.getString(R.string.Thumbnail);
			Set<String> s = new HashSet<String>();
			for (String path : selectPaths) {
				String repImagePath = path.replace(imageNmae, thumbnail);
				s.add(repImagePath);
			}
			if (s.isEmpty()) {
				leaveEdit();
			} else {
				enterEdit();
			}
			loadAlbum1(mSaveRoot_left, ".jpg", mAlbumView, s);
			loadAlbum1(mSaveRoot_right, ".jpg", mAlbumView_right, s);

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
	public void loadAlbum1(String rootPath, String format, AlbumGridView alvumGridView, Set<String> _itemSelectedSet) {
		// 获取根目录下缩略图文件夹
		String thumbFolder = FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_THUMBNAIL, rootPath);
		List<File> files = FileOperateUtil.listFiles(thumbFolder, format);
		if (files != null && files.size() > 0) {
			List<String> paths = new ArrayList<String>();
			for (File file : files) {
				paths.add(file.getAbsolutePath());
			}
			alvumGridView.setAdapter(alvumGridView.new AlbumViewAdapter(paths, _itemSelectedSet));
		}
	}

	@Override
	public void onCheckedChanged(Set<String> set) {
		// Log.e(TAG, "onCheckedChanged" + set.toString());
		// mAlbumView.getSelectedItem();
	}

}
