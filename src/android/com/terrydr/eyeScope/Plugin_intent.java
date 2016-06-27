package com.terrydr.eyeScope;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;

/**
 * cordovaplugin启动插件类
 * @author ty
 *
 */
public class Plugin_intent extends CordovaPlugin {
	private final static String TAG = "Plugin_intent";
	private String infos;
	private SharedPreferences preferences;   //保存数据 勾选下次不再提示
	private int i = 0;
	private CallbackContext callbackContext;
	
	public Plugin_intent() {
	}

	/**
	 * 读取本地存储数据
	 * @return
	 */
	private boolean getSharedPreferences() {
		boolean ischeck = preferences.getBoolean("isStart", false);
		return ischeck;
	}

	/**
	 * 启动插件的主要方法
	 */
	@Override
	public boolean execute(String action, org.json.JSONArray args,
			CallbackContext callbackContext) throws org.json.JSONException {
		
		if (action.equals("tdEyeTakePhotos")) { //启动拍照
			preferences = cordova.getActivity().getSharedPreferences("isStart", Context.MODE_PRIVATE);
			boolean isStart = getSharedPreferences();
			int ii = preferences.getInt("i", 0);
			Editor editor = preferences.edit();
			if(ii==0){
				if(isStart && i == 0){
					i++;
					editor.putInt("i", i);
					editor.commit();
					return false;
				}
			}
			this.callbackContext = callbackContext;
			Log.e(TAG, "tdEyeTakePhotos:" + callbackContext);
			this.startCameraActivity();
			editor.putBoolean("isStart", true);
			editor.commit();
			return true;
		} else if (action.equals("tdEyeSelectPhotos")) { // 相册缩略图界面
			this.callbackContext = callbackContext;
			Log.e(TAG, "tdEyeSelectPhotos:" + callbackContext);
			startAlbumAty();
			return true;
		} else if (action.equals("tdEyeScanPhotos")) { // 大图片预览界面参数{data:[图片路径，图片路径]}
			this.callbackContext = callbackContext;
			Log.e(TAG, "tdEyeScanPhotos:" + callbackContext);
			infos = args.getString(0);
			this.startAlbumItemAty(infos);
			return true;
		}
		return true;
	}

	/**
	 * 跳转到拍照界面 返回参数{leftEye:[];rightEye:[]}
	 */
	private void startCameraActivity() {
		Intent intent = new Intent(cordova.getActivity(), CameraActivity.class);
		cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
	}

	/**
	 * 跳转到相册缩略图界面
	 */
	private void startAlbumAty() {
		Intent intent = new Intent(cordova.getActivity(), AlbumItemAty.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean("isPlugin", true);
		intent.putExtras(bundle);
		cordova.startActivityForResult((CordovaPlugin) this, intent, 6);
	}

	/**
	 * 大图片预览界面 参数{data:[图片路径，图片路径]}
	 */
	private void startAlbumItemAty(String args) {
		Intent intent = new Intent(cordova.getActivity(),AlbumItemAtyForJs.class);
		Bundle bundle = new Bundle();
		bundle.putString("data", args);
		intent.putExtras(bundle);
		cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		switch (resultCode) { // resultCode为回传的标记，回传的是RESULT_OK
		case 0:
			break;
		case 5:
			Bundle b = intent.getExtras();
			String result_Json = b.getString("result_Json");
			org.json.JSONObject result = null;
			try {
				result = new org.json.JSONObject(result_Json);
			} catch (JSONException e) {
				Log.e(TAG, "String to Json error!",e);
			}
			callbackContext.success(result);
			break;
		case 6:
			Intent intent1 = new Intent(cordova.getActivity(), CameraActivity.class);
			Bundle bundle = new Bundle();
			bundle.putBoolean("deleteFile", false);
			intent1.putExtras(bundle);
			cordova.startActivityForResult((CordovaPlugin) this, intent1, 0);
			break;
		default:
			break;
		}
	}
}
