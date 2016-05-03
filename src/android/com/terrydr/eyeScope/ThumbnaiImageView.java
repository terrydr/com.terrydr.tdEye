package com.terrydr.eyeScope;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.terrydr.eyeScope.R;
/** 
 * @ClassName: AlbumItemView 
 * @Description:  相册Item 提取出来主要是为了实现点击ImageView变暗效果
 * @date 20160418
 *  
 */
public class ThumbnaiImageView extends FrameLayout  {
	public static  final String TAG="AlbumItemView";
	private final ViewHolder mViewHolder;
	private final ImageLoader mImageLoader;
	private final DisplayImageOptions mOptions;
	private String mPath;
	private int mPosition;
	private CheckBox checkBox;
	

	public ThumbnaiImageView(Context context,ImageLoader imageLoader,DisplayImageOptions options) {
		super(context);
		inflate(context, R.layout.item_album_grid, this);
		FilterImageView imageView=(FilterImageView) findViewById(R.id.imgThumbnail);
		checkBox=(CheckBox) findViewById(R.id.checkbox);
//		checkBox.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				if(checkBox.isChecked())
//					checkBox.setChecked(true);
//				else
//					checkBox.setChecked(false);
//				
//			}
//		});
		mViewHolder=new ViewHolder(imageView,checkBox,null);
		this.mImageLoader=imageLoader;
		this.mOptions=options;
	}

	/**  
	 *  设置标签
	 *  @param path 设置item指向的文件路 会同时把checkbox的标签设置为该
	 *  @param editable 是否可编辑状
	 *  @param checked  checkbox是否选中
	 */
	public void setTags(String path,int position,boolean editable,boolean checked){
		//可编辑状态，显示checkbox
		if (editable) {
			mViewHolder.checkBox.setVisibility(View.VISIBLE);
			mViewHolder.checkBox.setChecked(checked);
		}else {
			mViewHolder.checkBox.setVisibility(View.GONE);
		}
		//原路径和当前路径不同，更新图
		if (mPath==null||!mPath.equals(path)) {
			mImageLoader.loadImage(path, mViewHolder.imageView, mOptions);
			mPath=path;
			//给checkbox设置tag,用以记录当前选中
			mViewHolder.checkBox.setTag(path);
			setTag(path);
			mPosition=position;
		}
	}

	public int getPosition(){
		return mPosition;
	}
	/**  
	 * 设置checkbox的状态改变事
	 *  @param listener   
	 */
	public void setOnCheckedChangeListener(OnCheckedChangeListener listener){
		mViewHolder.checkBox.setOnCheckedChangeListener(listener);
//		if(mViewHolder.checkBox.getVisibility()==View.GONE){
//			mViewHolder.checkBox.setVisibility(View.VISIBLE);
//		}else{
//			mViewHolder.checkBox.setVisibility(View.GONE);
//		}
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		//重写click事件，将该View的click转到imageview触发
		mViewHolder.imageView.setOnClickListener(l);
//		mViewHolder.checkBox.setOnClickListener(l);
	}

	public class ViewHolder {
		public ViewHolder(ImageView imageView,CheckBox checkBox,ImageView icon){
			this.imageView=imageView;
			this.checkBox=checkBox;
			this.videoIconView=icon;
		}
		ImageView imageView;//缩略
		ImageView videoIconView;//播放视频图标
		CheckBox checkBox;//勾

	}
}
