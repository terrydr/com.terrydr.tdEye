package com.terrydr.eyeScope;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.terrydr.eyeScope.MatrixImageView.OnSingleTapListener;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
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
	private ImageView mBackView;
	private TextView mCountView,selected_tv,leftorright_tv;
	private View mHeaderBar,mBottomBar;
	private CheckBox select_cb;
	private int i = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.albumitem);

		mViewPager=(AlbumViewPager)findViewById(R.id.albumviewpager);
		mBackView=(ImageView)findViewById(R.id.header_bar_photo_back);
		mCountView=(TextView)findViewById(R.id.header_bar_photo_count);
		mHeaderBar=findViewById(R.id.album_item_header_bar);
		mBottomBar=findViewById(R.id.album_item_bottom_bar);
		leftorright_tv=(TextView)findViewById(R.id.leftorright_tv);
		
		selected_tv= (TextView)findViewById(R.id.selected_tv);
		select_cb=(CheckBox) findViewById(R.id.albumitem_selected_cb);

		mBackView.setOnClickListener(this);
		mCountView.setOnClickListener(this);

		mSaveRoot=getIntent().getExtras().getString("root");
		if(mSaveRoot.equals("left")){
			leftorright_tv.setText("左眼");		
		}else if(mSaveRoot.equals("right")){
			leftorright_tv.setText("右眼");	
		}
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
		
		select_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 
            @Override 
            public void onCheckedChanged(CompoundButton buttonView, 
                    boolean isChecked) { 
                if(isChecked){ 
                	i++;
                }else{ 
                	i--;
                } 
                selected_tv.setText("已选择" + i + "张");
            } 
        }); 
	}

	/**  
	 *  加载图片
	 *  @param rootPath   图片根路
	 */
	public void loadAlbum(String rootPath,String fileName){
		//获取根目录下缩略图文件夹
		String folder=FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_IMAGE, rootPath);
		String thumbnailFolder=FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_THUMBNAIL, rootPath);
		//获取图片文件大图
		List<File> imageList=FileOperateUtil.listFiles(folder, ".jpg");
		//获取视频文件缩略
		List<File> videoList=FileOperateUtil.listFiles(thumbnailFolder, ".jpg","video");
		List<File> files=new ArrayList<File>();
		//将视频文件缩略图加入图片大图列表
		if(videoList!=null&&videoList.size()>0){
			files.addAll(videoList);
		}
		if(imageList!=null&&imageList.size()>0){
			files.addAll(imageList);
		}
		FileOperateUtil.sortList(files, false);
		if(files.size()>0){
			List<String> paths=new ArrayList<String>();
			int currentItem=0;
			for (File file : files) {
				if(fileName!=null&&file.getName().contains(fileName))
					currentItem=files.indexOf(file);
				paths.add(file.getAbsolutePath());
			}
			mViewPager.setAdapter(mViewPager.new ViewPagerAdapter(paths));
			mViewPager.setCurrentItem(currentItem);
			mCountView.setText((currentItem+1)+"/"+paths.size());
		}
		else {
			mCountView.setText("0/0");
		}
	}


	private OnPageChangeListener pageChangeListener=new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			if(mViewPager.getAdapter()!=null){
				String text=(position+1)+"/"+mViewPager.getAdapter().getCount();
				mCountView.setText(text);
			}else {
				mCountView.setText("0/0");
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
