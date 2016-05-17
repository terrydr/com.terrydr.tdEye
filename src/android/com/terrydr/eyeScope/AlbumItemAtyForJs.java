package com.terrydr.eyeScope;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import com.terrydr.eyeScope.MatrixImageView.OnSingleTapListener;
import com.terrydr.eyeScope.R;

/**
 * @ClassName: AlbumItemAtyForJs
 * @Description:点击JS查看相册大图Activity
 * @date 20160429
 * 
 */
public class AlbumItemAtyForJs extends Activity implements OnClickListener,
		OnSingleTapListener {
	public final static String TAG = "AlbumItemAtyForJs";
	private AlbumViewPager mViewPager;// 显示大图
	private TextView mCountView, mBackView, header_bar_photo_commit_bt;
	private View mHeaderBar, mBottomBar;
	private String data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.albumitem_for_js);

		mViewPager = (AlbumViewPager) findViewById(R.id.albumviewpager);
		mBackView = (TextView) findViewById(R.id.header_bar_photo_back);
		mCountView = (TextView) findViewById(R.id.header_bar_photo_count);
		header_bar_photo_commit_bt = (TextView) findViewById(R.id.header_bar_photo_commit_bt);
		header_bar_photo_commit_bt.setVisibility(View.GONE);
		mHeaderBar = findViewById(R.id.album_item_header_bar);

		mBackView.setOnClickListener(this);
		mCountView.setOnClickListener(this);

		TextPaint tp = mCountView.getPaint(); // 安体加粗
		tp.setFakeBoldText(true);

		data = getIntent().getExtras().getString("data");
		mViewPager.setOnPageChangeListener(pageChangeListener);

		parseJsonMulti(data); // 解析传过来的数据并且加载图片

	}

	/**
	 *  解析数据的Json
	 * @param strResult
	 */
	private void parseJsonMulti(String strResult) {
		try {
			JSONObject jsonObjs = new JSONObject(strResult);
			JSONArray dataArray = jsonObjs.getJSONArray("data");
			int index = jsonObjs.getInt("index");
			List<String> paths = new ArrayList<String>();
			for (int i = 0; i < dataArray.length(); i++) {
				JSONObject jsonObj = (JSONObject) dataArray.get(i);
				String path = jsonObj.getString("origin");
				paths.add(path);
			}
			if (!paths.isEmpty()) {
				mViewPager.setAdapter(mViewPager.new ViewPagerAdapter(paths,
						true));
				mViewPager.setCurrentItem(index);
				mCountView.setText((index + 1) + " / " + paths.size());
			} else {
				mCountView.setText("0 / 0");
			}
		} catch (JSONException e) {
			Log.e(TAG, e.toString());
		}
	}

	private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			if (mViewPager.getAdapter() != null) {
				String text = (position + 1) + " / "
						+ mViewPager.getAdapter().getCount();
				mCountView.setText(text);
			} else {
				mCountView.setText("0 / 0");
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}
	};

	@Override
	public void onSingleTap() {
		if (mHeaderBar.getVisibility() == View.VISIBLE) {
			AlphaAnimation animation = new AlphaAnimation(1, 0);
			animation.setDuration(300);
			mHeaderBar.startAnimation(animation);
			mBottomBar.startAnimation(animation);
			mHeaderBar.setVisibility(View.GONE);
			mBottomBar.setVisibility(View.GONE);
		} else {
			AlphaAnimation animation = new AlphaAnimation(0, 1);
			animation.setDuration(300);
			mHeaderBar.startAnimation(animation);
			mBottomBar.startAnimation(animation);
			mHeaderBar.setVisibility(View.VISIBLE);
			mBottomBar.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.header_bar_photo_back:
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

}
