package com.terrydr.eyeScope;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.GridView;
import com.terrydr.eyeScope.R;
/**
 * @ClassName: AlbumView
 * @Description: Photo album View, extends the GridView, encapsulates the Adapter and image loading method 
 * @date 20160415
 * 
 */
public class AlbumGridView extends GridView {
	public final static String TAG = "AlbumGridView";
	/** 图片加载 优化了了缓存 */
	private ImageLoader mImageLoader;
	/** 加载图片配置参数 */
	private DisplayImageOptions mOptions;
	private boolean mEditable;

	public AlbumGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mImageLoader = ImageLoader.getInstance(context);
		// 设置图片加载参数
		DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
		builder = builder
//				.showImageOnLoading(R.drawable.ic_stub)
//				.showImageOnFail(R.drawable.ic_error)
				.cacheInMemory(true)
				.cacheOnDisk(false)
				.displayer(new RoundedBitmapDisplayer(0));  //是否图角
		mOptions = builder.build();
		setBackgroundColor(Color.WHITE);
		// 隐藏垂直滚动
		setVerticalScrollBarEnabled(false);
	}
	/**
	 * 
	 * @param listener
	 *            选择图片后执行的回调函数
	 */
	public void selectAll(AlbumGridView.OnCheckedChangeListener listener) {
		((AlbumViewAdapter) getAdapter()).selectAll(listener);
	}

	/**
	 * 
	 * @param listener
	 *            选择图片后执行的回调函数
	 */
	public void unSelectAll(AlbumGridView.OnCheckedChangeListener listener) {
		((AlbumViewAdapter) getAdapter()).unSelectAll(listener);
	}

	/**
	 * 
	 * @param editable
	 */
	public void setEditable(boolean editable) {
		mEditable = editable;
		((AlbumViewAdapter) getAdapter()).notifyDataSetChanged(null);
	}

	/**
	 * 
	 * @param listener
	 *            选择图片后执行的回调函数
	 */
	public void setEditable(boolean editable,
			AlbumGridView.OnCheckedChangeListener listener) {
		mEditable = editable;
		((AlbumViewAdapter) getAdapter()).notifyDataSetChanged(listener);
	}

	/**
	 * 
	 * @return
	 */
	public boolean getEditable() {
		return mEditable;
	}

	/**
	 * 
	 * @return
	 */
	public Set<String> getSelectedItems() {
		if((AlbumViewAdapter) getAdapter()!=null){
			return ((AlbumViewAdapter) getAdapter()).getSelectedItems();
		}
		return null;
//		return ((AlbumViewAdapter) getAdapter()).getSelectedItems();
	}

	public void notifyDataSetChanged() {
		((AlbumViewAdapter) getAdapter()).notifyDataSetChanged();
	}

	/**
	 * @ClassName: OnCheckedChangeListener
	 * @Description: 图片选中后的监听接口，用以在activity内做回调处理
	 * @date 20160419
	 * 
	 */
	public interface OnCheckedChangeListener {
		public void onCheckedChanged(Set<String> set);
	}

	/**
	 * @ClassName: AlbumViewAdapter
	 * @Description: 相册GridView适配
	 * @date 20160419
	 * 
	 */
	public class AlbumViewAdapter extends BaseAdapter implements
			OnClickListener, CompoundButton.OnCheckedChangeListener {

		/** 加载的文件路径集*/
		List<String> mPaths;

		/** 当前选中的文件的集合 */
		Set<String> itemSelectedSet = new HashSet<String>();
		Set<String> itemSelectedSet1 = new HashSet<String>();

		/** 选中图片后执行的回调函数 */
		AlbumGridView.OnCheckedChangeListener listener = null;

		public AlbumViewAdapter(List<String> paths) {
			super();
			this.mPaths = paths;
		}
		
		public AlbumViewAdapter(List<String> paths,Set<String> _itemSelectedSet) {
			super();
			this.mPaths = paths;
			this.itemSelectedSet1 = _itemSelectedSet;
		}

		/**
		 * 适配器内容改变时，重新绘
		 * 
		 * @param listener
		 */
		public void notifyDataSetChanged(
				AlbumGridView.OnCheckedChangeListener listener) {
			// 重置map
			itemSelectedSet = new HashSet<String>();
			itemSelectedSet1 = new HashSet<String>();
			this.listener = listener;
			super.notifyDataSetChanged();
		}

		/**
		 * 选中
		 * 
		 * @param listener
		 */
		public void selectAll(AlbumGridView.OnCheckedChangeListener listener) {
			for (String path : mPaths) {
				itemSelectedSet.add(path);
			}
			this.listener = listener;
			super.notifyDataSetChanged();
			if (listener != null)
				listener.onCheckedChanged(itemSelectedSet);
		}

		/**
		 * 取消选中
		 * 
		 * @param listener
		 */
		public void unSelectAll(AlbumGridView.OnCheckedChangeListener listener) {
			notifyDataSetChanged(listener);
			if (listener != null)
				listener.onCheckedChanged(itemSelectedSet);
		}

		/**
		 * 获取当前选中文件的集
		 * 
		 * @return
		 */
		public Set<String> getSelectedItems() {
			return itemSelectedSet;
		}

		@Override
		public int getCount() {
			return mPaths.size();
		}

		@Override
		public String getItem(int position) {
			return mPaths.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ThumbnaiImageView albumItemView = (ThumbnaiImageView) convertView;
			if (albumItemView == null)
				albumItemView = new ThumbnaiImageView(getContext(),
						mImageLoader, mOptions);
			albumItemView.setOnCheckedChangeListener(this);
			// 设置点击事件，将ItemClick事件转化为AlbumItemView的Click事件
			albumItemView.setOnClickListener(this);
			String path = getItem(position);
			albumItemView.setTags(path, position, mEditable,
					itemSelectedSet1.contains(path));
			return albumItemView;
		}

		@Override
		public void onClick(View v) {
			if (getOnItemClickListener() != null) {
//				// 这里取了上两层父类，因为真正触onClick的是FilterImageView
				ThumbnaiImageView view = (ThumbnaiImageView) v.getParent().getParent();
				getOnItemClickListener().onItemClick(AlbumGridView.this, view,view.getPosition(), 0L);
			}
		}

		@Override
		public void onCheckedChanged(final CompoundButton buttonView,
				boolean isChecked) {
			if(itemSelectedSet.size()>=2 && isChecked){
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setMessage("单侧眼睛最多选择两张图片")
						.setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								buttonView.setChecked(false);
							}
						});
				builder.create().show();
			}else{
				if (buttonView.getTag() == null)
					return;
				if (isChecked){
					itemSelectedSet.add(buttonView.getTag().toString());
				}
				else{
					itemSelectedSet.remove(buttonView.getTag().toString());
				}
				if (listener != null)
					listener.onCheckedChanged(itemSelectedSet);
			}
		}
	}
}
