//
//  TDEye.h
//  HelloCordova
//
//  Created by 路亮亮 on 16/3/15.
//
//

#import <Cordova/CDVPlugin.h>

@interface TDEye : CDVPlugin

//跳转到拍照，视频界面
- (void)tdEyeTakePhotos:(CDVInvokedUrlCommand*)command;
//选择图片
- (void)tdEyeSelectPhotos:(CDVInvokedUrlCommand*)command;
//浏览拍摄的图片
- (void)tdEyeScanPhotos:(CDVInvokedUrlCommand*)command;

@end
