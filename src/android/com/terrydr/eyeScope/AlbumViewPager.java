package com.terrydr.eyeScope;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.terrydr.eyeScope.MatrixImageView.OnMovingListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.terrydr.eyeScope.R;

/**
 * @ClassName: AlbumViewPager
 * @Description: 自定义viewpager 优化了事件拦
 * @date 20160419
 * 
 */
public class AlbumViewPager extends ViewPager implements OnMovingListener {
	public final static String TAG = "AlbumViewPager";

	/** 图片加载 优化了了缓存 */
	private ImageLoader mImageLoader;
	/** 加载图片配置参数 */
	private DisplayImageOptions mOptions;

	/** 当前子控件是否处理拖动状 */
	private boolean mChildIsBeingDragged = false;

	public AlbumViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		mImageLoader = ImageLoader.getInstance(context);
		// 设置图片加载参数
		DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
		builder = builder
				// .showImageOnLoading(R.drawable.ic_stub)
				// .showImageOnFail(R.drawable.ic_error)
				.cacheInMemory(true).cacheOnDisk(false).displayer(new MatrixBitmapDisplayer());
		mOptions = builder.build();
	}

	/**
	 * 删除当前
	 * 
	 * @return “当前位总数量
	 */
	public String deleteCurrentPath() {
		return ((ViewPagerAdapter) getAdapter()).deleteCurrentItem(getCurrentItem());

	}

	public Set<String> getPathsArray() {
		return ((ViewPagerAdapter) getAdapter()).getPathsArray();

	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if (mChildIsBeingDragged)
			return false;
		return super.onInterceptTouchEvent(arg0);
	}

	@Override
	public void startDrag() {
		mChildIsBeingDragged = true;
	}

	@Override
	public void stopDrag() {
		mChildIsBeingDragged = false;
	}

	public class ViewPagerAdapter extends PagerAdapter {
		private List<String> paths;// 大图地址
		private Set<String> selectPaths = new HashSet<String>();// 选中的图片
		private int i = 0;
		private AlbumItemAty main;
		private String text;
		private String[] bool;
		private boolean isForJs = false; // 判断是否是js端跳转过来的

		public ViewPagerAdapter(List<String> paths, boolean _isForJs) {
			this.paths = paths;
			isForJs = _isForJs;
		}

		public ViewPagerAdapter(Context c, List<String> paths) {
			main = (AlbumItemAty) c;
			this.paths = paths;
			bool = new String[paths.size()];
			for (int i = 0; i < paths.size(); i++) {
				bool[i] = "false";
			}
		}

		@Override
		public int getCount() {
			return paths.size();
		}

		@Override
		public Object instantiateItem(ViewGroup viewGroup, final int position) {
			// 注意，这里不可以加inflate的时候直接添加到viewGroup下，而需要用addView重新添加
			// 因为直接加到viewGroup下会导致返回的view为viewGroup
			View imageLayout = inflate(getContext(), R.layout.item_album_pager, null);
			viewGroup.addView(imageLayout);
			assert imageLayout != null;
			MatrixImageView imageView = (MatrixImageView) imageLayout.findViewById(R.id.image);
			final CheckBox select_cb = (CheckBox) imageLayout.findViewById(R.id.albumitem_selected_cb);
			if (isForJs) { // 如果是JS端跳转过来的，直接隐藏checkBox
				select_cb.setVisibility(View.GONE);
			}
			if (bool != null) {
				if (bool[position].equals("true")) {
					select_cb.setChecked(true);
				} else {
					select_cb.setChecked(false);
				}
			}
			select_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (i >= 2 && isChecked) {
						AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setMessage("单侧眼睛最多选择两张图片").setPositiveButton("确定",
								new android.content.DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										i++;
										select_cb.setChecked(false);
									}
								});
						builder.create().show();
					} else {
						if (isChecked) {
							i++;
							bool[position] = "true";
							selectPaths.add(paths.get(position));
						} else {
							i--;
							bool[position] = "false";
							selectPaths.remove(paths.get(position));
						}
					}
					text = "已选 " + i + " 张";
					main.onChangeTesChanged(text);
				}
			});
			imageView.setOnMovingListener(AlbumViewPager.this);
			String path = paths.get(position);
			imageLayout.setTag(path);
			mImageLoader.loadImage(path, imageView, mOptions);
			return imageLayout;
		}

		@Override
		public int getItemPosition(Object object) {
			// 在notifyDataSetChanged时返回None，重新绘
			return POSITION_NONE;
		}

		@Override
		public void destroyItem(ViewGroup container, int arg1, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		// 自定义获取当前view方法
		public String deleteCurrentItem(int position) {
			String path = paths.get(position);
			if (path != null) {
				FileOperateUtil.deleteSourceFile(path, getContext());
				paths.remove(path);
				notifyDataSetChanged();
				if (paths.size() > 0)
					return (getCurrentItem() + 1) + "/" + paths.size();
				else {
					return "0/0";
				}
			}
			return null;
		}

		public Set<String> getPathsArray() {
			return selectPaths;

		}
	}

}
