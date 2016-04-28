package com.terrydr.eyeScope;

import org.apache.cordova.CordovaActivity;

import android.content.Intent;
import android.os.Bundle;

public class ViewActivity extends CordovaActivity {  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        super.init();  
        super.loadUrl("file:///android_asset/www/index.html");  
    }  
  
    @Override  
    protected void onActivityResult(int requestCode, int resultCode,  
            Intent intent) {  
        super.onActivityResult(requestCode, resultCode, intent);  
  
    }  
}  
