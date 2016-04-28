package com.terrydr.eyeScope;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class Plugin_intent extends CordovaPlugin{
	private String infos;  
	public Plugin_intent() {
	}
	
	CallbackContext callbackContext;  
	  
    @Override  
    public boolean execute(String action, org.json.JSONArray args,  
            CallbackContext callbackContext) throws org.json.JSONException {  
        this.callbackContext = callbackContext;  
        Log.e("callbackContext","d" + callbackContext);
        if (action.equals("intent")) {  
            infos = args.getString(0);  
            this.startCameraActivity();  
            
            return true;  
        }  
        
        if (action.equals("jrEyeTakePhotos")) {  
            infos = args.getString(0);  
            this.startCameraActivity();  
            callbackContext.success();  
            return true;  
        }  
        //缩略图界面
        if (action.equals("jrEyeSelectPhotos")) {  
            infos = args.getString(0);  
            this.startCameraActivity();  
            return true;  
        }
        //大图片预览界面 参数{data:[图片路径，图片路径]}
        
        if (action.equals("jrEyeScanPhotos")) {  
            infos = args.getString(0);  
            this.startCameraActivity();  
            return true;  
        }  
        return false;  
  
    }  
	
    /**
     * 跳转到拍照界面  返回参数{leftEye:[];right:[]}
     */
	private void startCameraActivity() {
		// cordova.getActivity() 获取当前activity的this
		Intent intent = new Intent(cordova.getActivity(), CameraActivity.class);
		cordova.startActivityForResult((CordovaPlugin) this, intent, 0);

	}
	
	public void jrEyeTakePhotos(String result){
		callbackContext.success(result);
	}
	
	 @Override  
	    public void onActivityResult(int requestCode, int resultCode, Intent intent) {  
	  
//	        super.onActivityResult(requestCode, resultCode, intent);  
			switch (resultCode) { // resultCode为回传的标记，回传的是RESULT_OK
			case 0:
//				Bundle b = data.getExtras();
//				mExposureNum = b.getInt("mexposureNum");
//				mContainer.setCameraISO_int(mExposureNum);
				break;
			case 5:
				Bundle b = intent.getExtras();
				String result_Json = b.getString("result_Json");
				
				Log.e("132", result_Json);
				Log.e("callbackContext","dd" + callbackContext);
				callbackContext.success(result_Json);
				break;
			default:
				break;
			}
	        // 反回给js
//	        callbackContext.success(com.alibaba.fastjson.JSONArray  
//	                .toJSONString(ResponseJSON.getInstance().getJsonObjects()));  
//	        if (ResponseJSON.getInstance().getJsonObjects() != null  
//	                && ResponseJSON.getInstance().getJsonObjects().size() > 0) {  
//	            Toast.makeText(cordova.getActivity(), "完成", 1000).show();  
//	        }  
	    }  
}
