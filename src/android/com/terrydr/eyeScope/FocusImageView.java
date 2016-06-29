package com.terrydr.eyeScope;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/** 
 * @ClassName: FocusImageView 
 * @Description:对焦显示ImagView  
 * @date 20160418
 */
public class FocusImageView extends ImageView {
	public final static String TAG="FocusImageView";
	private static final int NO_ID=-1;
	private int mFocusImg=NO_ID;
	private int mFocusSucceedImg=NO_ID;
	private int mFocusFailedImg=NO_ID;
	private Animation mAnimation;
	private Handler mHandler;
	public FocusImageView(Context context) {
		super(context);
		mAnimation=AnimationUtils.loadAnimation(getContext(), R.anim.focusview_show);
		setVisibility(View.GONE);
		mHandler=new Handler();
	}

	public FocusImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mAnimation=AnimationUtils.loadAnimation(getContext(), R.anim.focusview_show);
		mHandler=new Handler();

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FocusImageView);
		mFocusImg = a.getResourceId(R.styleable.FocusImageView_focus_focusing_id, NO_ID);
		mFocusSucceedImg=a.getResourceId(R.styleable.FocusImageView_focus_success_id, NO_ID);
		mFocusFailedImg=a.getResourceId(R.styleable.FocusImageView_focus_fail_id, NO_ID);
		a.recycle();

		if (mFocusImg==NO_ID||mFocusSucceedImg==NO_ID||mFocusFailedImg==NO_ID) 
			throw new RuntimeException("Animation is null");
	}

	/**  
	 *  开始对焦
	 *  @param x 触摸点轴坐标
	 *  @param y 触摸点y轴坐标
	 */
	public void startFocus(Point point){
		if (mFocusImg==NO_ID||mFocusSucceedImg==NO_ID||mFocusFailedImg==NO_ID) 
			throw new RuntimeException("focus image is null");
		RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams) getLayoutParams();
		WindowManager wm = (WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		int height = wm.getDefaultDisplay().getHeight();
//		int y = height*250/1920;
		params.topMargin= point.y-getHeight()/2;
//		params.topMargin= point.y-y-getHeight()/2;
		params.leftMargin=point.x-getWidth()/2;
		setLayoutParams(params);	
		setVisibility(View.VISIBLE);
		setImageResource(mFocusImg);
		startAnimation(mAnimation);	
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				setVisibility(View.GONE);
			}
		},1500);
	}
	
	/**  
	*   对焦成功显示图像
	*/
	public void onFocusSuccess(){
		setImageResource(mFocusSucceedImg);
		mHandler.removeCallbacks(null, null);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				setVisibility(View.GONE);
			}
		},1000);
		
	}
	
	/**  
	*   对焦失败显示图像
	*/
	public void onFocusFailed(){
		setImageResource(mFocusFailedImg);
		mHandler.removeCallbacks(null, null);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				setVisibility(View.GONE);
			}
		},1000);
	}

	/**  
	 * 焦点图像
	 *  @param focus   
	 */
	public void setFocusImg(int focus) {
		this.mFocusImg = focus;
	}

	/**  
	 *  对焦成功图像
	 *  @param focusSucceed   
	 */
	public void setFocusSucceedImg(int focusSucceed) {
		this.mFocusSucceedImg = focusSucceed;
	}
}
