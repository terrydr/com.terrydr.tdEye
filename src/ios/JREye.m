//
//  JREye.m
//  HelloCordova
//
//  Created by 路亮亮 on 16/3/15.
//
//

#import "JREye.h"
#import "WYVideoCaptureController.h"

@interface JREye (){
    NSString *_callbackId;
}

@end

@implementation JREye

- (void)jrEyeTakePhotos:(CDVInvokedUrlCommand*)command{
    
    _callbackId = command.callbackId;
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(takePhotosFinished:)
                                                 name:@"TakePhotosFinishedNotification"
                                               object:nil];
    
    WYVideoCaptureController *videoVC = [[WYVideoCaptureController alloc] init];
    [self.viewController presentViewController:videoVC animated:YES completion:^{
    }];
}

- (void)takePhotosFinished:(NSNotification *)notify{
    NSDictionary *pathDic = notify.userInfo;
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:pathDic];
    [self.commandDelegate sendPluginResult:result callbackId:_callbackId];
}

@end
