package com.terrydr.eyeScope;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.terrydr.eyeScope.MatrixImageView.OnMovingListener;
import com.terrydr.eyeScope.MatrixImageView.OnSlideUpListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * @ClassName: AlbumViewPager
 * @Description: 自定义viewpager 优化了事件拦
 * @date 20160419
 * 
 */
public class AlbumViewPager extends ViewPager implements OnMovingListener,OnSlideUpListener {
	public final static String TAG = "AlbumViewPager";

	/** 图片加载 优化了了缓存 */
	private ImageLoader mImageLoader;
	/** 加载图片配置参数 */
	private DisplayImageOptions mOptions;

	/** 当前子控件是否处理拖动状 */
	private boolean mChildIsBeingDragged = false;
	private AlbumItemAty albumItemAty;
//	private CustomDialog.Builder ibuilder;
	private SharedPreferences preferences;   //保存数据 勾选下次不再提示
	private boolean isCheckeds;  //记录勾选下次不再提示

	public AlbumViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		preferences = getContext().getSharedPreferences(
				"deleteRemind", Context.MODE_PRIVATE);
		albumItemAty = (AlbumItemAty) context;
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
	
	/**
	 * 获取当前选中的文件路径
	 */
	public String getSelectPath() {
		return ((ViewPagerAdapter) getAdapter()).getSelectPath(getCurrentItem());

	}

	public Set<String> getPathsArray() {
		return ((ViewPagerAdapter) getAdapter()).getPathsArray();
	}
	
	/**
	 * 返回所有图片路径
	 * @return
	 */
	public List<String> getPaths() {
		return ((ViewPagerAdapter) getAdapter()).getPaths();
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

	/**
	 * 删除事件处理,删除先读取配置文件‘deleteRemind.xml’中有没有写入不提示弹窗的保存值‘ischeck’，
	 * 如果有值并为 true 则不提示弹窗
	 */
	@Override
	public void onSlideUpTap() {
		boolean isCheckVules = getSharedPreferences();
		if (isCheckVules) {
			int deleteCurretItem = getCurrentItem();
			String deletePath = deleteCurrentPath();
			String selectPath = null;
			if (!getPaths().isEmpty()) {
				selectPath = getPaths().get(getCurrentItem());
			}
			albumItemAty.reloadAlbum(getPaths(), deletePath, selectPath,deleteCurretItem, getCurrentItem());
		} else {
			// 定义复选框选项
			final String[] multiChoiceItems = { "下次不再提示", };
			// 复选框默认值：false=未选;true=选中 ,各自对应items[i]
			final boolean[] defaultSelectedStatus = { false };
			AlertDialog.Builder builder = new AlertDialog.Builder(albumItemAty);
			builder.setTitle("是否删除照片");
			builder.setMultiChoiceItems(
					multiChoiceItems,
					defaultSelectedStatus,
					new android.content.DialogInterface.OnMultiChoiceClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which,
								boolean isChecked) {
							// 来回重复选择取消，得相应去改变item对应的bool值，点击确定时，根据这个bool[],得到选择的内容
							defaultSelectedStatus[which] = isChecked;
							isCheckeds = isChecked;
						}
					});
			builder.setPositiveButton("确认",
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Editor editor = preferences.edit();
							editor.putBoolean("ischeck", isCheckeds);
							editor.commit();

							int deleteCurretItem = getCurrentItem();
							String deletePath = deleteCurrentPath();
							String selectPath = null;
							if (!getPaths().isEmpty()) {
								selectPath = getPaths().get(getCurrentItem());
							}
							albumItemAty.reloadAlbum(getPaths(),deletePath,selectPath,deleteCurretItem,getCurrentItem());
							dialog.dismiss();
						}
					}).setNegativeButton("取消",
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							isCheckeds = false;
							dialog.dismiss();
						}
					});
			builder.create().show();
		}
	}
	/**
	 * 读取数据
	 * @return
	 */
	private boolean getSharedPreferences() {
		boolean ischeck = preferences.getBoolean("ischeck", false);
		return ischeck;
	}
	
	/**
	 * 大图数据适配器
	 * @author ty
	 *
	 */
	public class ViewPagerAdapter extends PagerAdapter {
		private List<String> paths;// 大图地址
		private Set<String> selectPaths = new HashSet<String>();// 选中的图片
		private String[] bool;

		public ViewPagerAdapter(List<String> paths, boolean _isForJs) {
			this.paths = paths;
		}

		public ViewPagerAdapter(Context c, List<String> paths) {
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
			imageView.setOnMovingListener(AlbumViewPager.this);
			
			imageView.setOnSlideUpListener(AlbumViewPager.this);  //上滑删除事件
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
//				if (paths.size() > 0)
//					return (getCurrentItem() + 1) + "/" + paths.size();
//				else {
//					return "0/0";
//				}
				return path;
			}
			return null;
		}
		
		// 自定义获取当前getSelectPath
		public String getSelectPath(int position) {
			String path = paths.get(position);
			if (path != null) {
				return path;
			}
			return null;
		}

		public Set<String> getPathsArray() {
			return selectPaths;
		}
		
		public List<String> getPaths() {
			return paths;
		}
	}
}
