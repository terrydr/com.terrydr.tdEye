package com.terrydr.eyeScope;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Plugin_intent extends CordovaPlugin {
	private final static String TAG = "Plugin_intent";
	private String infos;

	public Plugin_intent() {
	}

	CallbackContext callbackContext;

	@Override
	public boolean execute(String action, org.json.JSONArray args,
			CallbackContext callbackContext) throws org.json.JSONException {
		this.callbackContext = callbackContext;
		if (action.equals("jrEyeTakePhotos")) {
			Log.e(TAG, "jrEyeTakePhotos:" + callbackContext);
			this.startCameraActivity();
			return true;
		} else if (action.equals("jrEyeSelectPhotos")) { // 相册缩略图界面
			Log.e(TAG, "jrEyeSelectPhotos:" + callbackContext);
			startAlbumAty();
			return true;
		} else if (action.equals("jrEyeScanPhotos")) { // 大图片预览界面参数{data:[图片路径，图片路径]}
			Log.e(TAG, "jrEyeScanPhotos:" + callbackContext);
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
		Log.i(TAG, "startAlbumAty");
		Intent intent = new Intent(cordova.getActivity(), AlbumAty.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean("isPlugin", true);
		intent.putExtras(bundle);
		cordova.startActivityForResult((CordovaPlugin) this, intent, 6);
	}

	/**
	 * 大图片预览界面 参数{data:[图片路径，图片路径]}
	 */
	private void startAlbumItemAty(String args) {
		// cordova.getActivity() 获取当前activity的this
		Intent intent = new Intent(cordova.getActivity(),
				AlbumItemAtyForJs.class);
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
			Log.e(TAG, "5");
			Bundle b = intent.getExtras();
			String result_Json = b.getString("result_Json");
			org.json.JSONObject result = null;
			try {
				result = new org.json.JSONObject(result_Json);
//				Log.e(TAG, "result:" + result);
			} catch (JSONException e) {
				Log.e(TAG, "String to Json error!");
			}
//			Log.e(TAG, "callbackContext:" + callbackContext);
			callbackContext.success(result);
			break;
		case 6:
//			Log.e(TAG, "6");
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
