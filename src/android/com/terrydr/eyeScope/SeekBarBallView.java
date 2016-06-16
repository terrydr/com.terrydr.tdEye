package com.terrydr.eyeScope;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Scroller;

/**
 * 自定义弧形SeekBar 圆球部分
 */
public class SeekBarBallView extends View {

    private Paint paint;
    private Scroller scroller;
    private OnSmoothScrollListener listener;
    
    private Drawable thumb;  
    private Resources res;  
    private Bitmap bmp;  
	

    public SeekBarBallView(Context context) {
        super(context);
        init(context);
    }

    public SeekBarBallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SeekBarBallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
        scroller = new Scroller(context);
//        res = context.getResources();  
//        bmp = BitmapFactory  
//                .decodeResource(res, R.drawable.wb_thumb);  
//        thumb = new BitmapDrawable(bmp); 
        
        final Resources res = getResources();
		thumb = res.getDrawable(R.drawable.wb_thumb);
    }

    public void setListener(OnSmoothScrollListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            if (listener != null) {
                listener.onSmoothScroll(scroller.getCurrX());
                postInvalidate();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.reset();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
//        Rect rect = thumb.getBounds();  
        
//        zoomDrawable(thumb,bmp.getWidth(),bmp.getHeight());
        
        
		thumb.setBounds(
				(int) (getMeasuredWidth() / 2 - getMeasuredWidth() * 40 / 720),
				(int) (getMeasuredWidth() / 2 - getMeasuredWidth() * 40 / 720),
				(int) (getMeasuredWidth() / 2 + getMeasuredWidth() * 40 / 720),
				(int) (getMeasuredWidth() / 2 + getMeasuredWidth() * 40 / 720));
        
        this.thumb.draw(canvas);
        
//        canvas.drawBitmap(bmp, getMeasuredWidth() / 2,getMeasuredWidth() / 2, paint);
        
//        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredWidth() / 2, getMeasuredWidth() / 2, paint);
        
    }
    
    /**
     * 平滑滑动
     *
     * @param start    起始值
     * @param distance 滑动距离
     */
    public void smoothScrollLevel(int start, int distance) {
        scroller.startScroll(start, 0, distance, 0, 200);
        postInvalidate();
    }

    public interface OnSmoothScrollListener {
        void onSmoothScroll(int currentX);
    }
}

