//
//  JREye.m
//  HelloCordova
//
//  Created by 路亮亮 on 16/3/15.
//
//

#import "JREye.h"
#import "WYVideoCaptureController.h"
#import "PictureScanViewController.h"

@interface JREye (){
    NSString *_callbackId;
}

@end

@implementation JREye

- (void)jrEyeTakePhotos:(CDVInvokedUrlCommand*)command{
    
    _callbackId = command.callbackId;
    NSArray *paramArr = command.arguments;
    NSString *takeType = [paramArr objectAtIndex:0];
    NSLog(@"takeType:%@",takeType);
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(takePhotosFinished:)
                                                 name:@"TakePhotosFinishedNotification"
                                               object:nil];
    
    WYVideoCaptureController *videoVC = [[WYVideoCaptureController alloc] init];
    UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:videoVC];
    [self.viewController presentViewController:nav animated:YES completion:^{
    }];
}

- (void)takePhotosFinished:(NSNotification *)notify{
    NSDictionary *pathDic = notify.userInfo;
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:pathDic];
    [self.commandDelegate sendPluginResult:result callbackId:_callbackId];
}

- (void)jrEyeScanPhotos:(CDVInvokedUrlCommand*)command{
    _callbackId = command.callbackId;
    PictureScanViewController *scanVC = [[PictureScanViewController alloc] init];
    UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:scanVC];
    [self.viewController presentViewController:nav animated:YES completion:^{
    }];
}

@end
