package com.terrydr.eyeScope;

import java.util.Timer;
import java.util.TimerTask;

import com.terrydr.eyeScope.HeadSetUtil.OnHeadSetListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

/**
 * MEDIA_BUTTON耳机媒体按键广播接收器
 * 
 * @Date 20160531
 */
public class MediaButtonReceiver extends BroadcastReceiver {

	public final static String TAG = "HeadSetUtil";
	private Timer timer = null;
	private OnHeadSetListener headSetListener = null;
	private static MTask myTimer = null;
	/** 单击次数 **/
	private static int clickCount;

	public MediaButtonReceiver() {
		timer = new Timer(true);
		this.headSetListener = HeadSetUtil.getInstance().getOnHeadSetListener();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive");
		String intentAction = intent.getAction();
		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT); // 获得KeyEvent对象
			if (headSetListener != null) {
				Log.e(TAG, "keyEvent.getKeyCode():"+keyEvent.getKeyCode()); 
				if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
					if (clickCount == 0) {// 单击
						clickCount++;
						myTimer = new MTask();
						timer.schedule(myTimer, 1000);
					} else if (clickCount == 1) {// 双击
						clickCount++;
					} else if (clickCount == 2) {// 三连击
						clickCount = 0;
						myTimer.cancel();
						headSetListener.onThreeClick();
					}
				}
			}
		}
		abortBroadcast();// 终止广播(不让别的程序收到此广播，免受干扰)
		
//	        // 获得Action 
//	        String intentAction = intent.getAction(); 
//	        // 获得KeyEvent对象 
//	        KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT); 
//	 
//	        Log.i(TAG, "Action ---->" + intentAction + "  KeyEvent----->"+ keyEvent.toString()); 
//	 
//	        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) { 
//	            // 获得按键字节码 
//	            int keyCode = keyEvent.getKeyCode(); 
//	            // 按下 / 松开 按钮 
//	            int keyAction = keyEvent.getAction(); 
//	            // 获得事件的时间 
//	            long downtime = keyEvent.getEventTime(); 
//	 
//	            // 获取按键码 keyCode 
//	            StringBuilder sb = new StringBuilder(); 
//	            // 这些都是可能的按键码 ， 打印出来用户按下的键 
//	            if (KeyEvent.KEYCODE_MEDIA_NEXT == keyCode) { 
//	                sb.append("KEYCODE_MEDIA_NEXT"); 
//	            } 
//	            // 说明：当我们按下MEDIA_BUTTON中间按钮时，实际出发的是 KEYCODE_HEADSETHOOK 而不是 
//	            // KEYCODE_MEDIA_PLAY_PAUSE 
//	            if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == keyCode) { 
//	                sb.append("KEYCODE_MEDIA_PLAY_PAUSE"); 
//	            } 
//	            if (KeyEvent.KEYCODE_HEADSETHOOK == keyCode) { 
//	                sb.append("KEYCODE_HEADSETHOOK"); 
//	            } 
//	            if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == keyCode) { 
//	                sb.append("KEYCODE_MEDIA_PREVIOUS"); 
//	            } 
//	            if (KeyEvent.KEYCODE_MEDIA_STOP == keyCode) { 
//	                sb.append("KEYCODE_MEDIA_STOP"); 
//	            } 
//	            // 输出点击的按键码 
//	            Log.e(TAG, sb.toString()); 
//	        } 
	}

	/**
	 * 定时器，用于延迟1秒，判断是否会发生双击和三连击
	 */
	class MTask extends TimerTask {
		@Override
		public void run() {
			try {
				if (clickCount == 1) {
					mhHandler.sendEmptyMessage(1);
				} else if (clickCount == 2) {
					mhHandler.sendEmptyMessage(2);
				}
				clickCount = 0;
			} catch (Exception e) {
				Log.e(TAG, "mtask Exception", e);
			}
		}
	};

	/**
	 * 此handle的目的主要是为了将接口在主线程中触发 ，为了安全起见把接口放到主线程触发
	 */
	Handler mhHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {// 单击
				headSetListener.onClick();
			} else if (msg.what == 2) {// 双击
				headSetListener.onDoubleClick();
			} else if (msg.what == 3) {// 三连击
				headSetListener.onThreeClick();
			}
		}
	};

}