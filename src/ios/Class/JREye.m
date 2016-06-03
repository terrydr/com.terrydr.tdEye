//
//  JREye.m
//  HelloCordova
//
//  Created by 路亮亮 on 16/3/15.
//
//

#import "JREye.h"
#import "TDNavgationController.h"
#import "WYVideoCaptureController.h"
#import "MLSelectPhotoBrowserViewController.h"

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
    videoVC.isScan = NO;
    TDNavgationController *nav = [[TDNavgationController alloc] initWithRootViewController:videoVC];
    [self.viewController presentViewController:nav animated:YES completion:^{
    }];
}

- (void)takePhotosFinished:(NSNotification *)notify{
    NSDictionary *pathDic = notify.userInfo;
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:pathDic];
    [self.commandDelegate sendPluginResult:result callbackId:_callbackId];
}

- (void)jrEyeSelectPhotos:(CDVInvokedUrlCommand*)command{
    _callbackId = command.callbackId;
    WYVideoCaptureController *videoVC = [[WYVideoCaptureController alloc] init];
    videoVC.isScan = YES;
    TDNavgationController *nav = [[TDNavgationController alloc] initWithRootViewController:videoVC];
    [self.viewController presentViewController:nav animated:YES completion:^{
    }];
}

- (void)jrEyeScanPhotos:(CDVInvokedUrlCommand*)command{
    _callbackId = command.callbackId;
    NSArray *paramArr = command.arguments;
    NSString *jsonStr = [paramArr objectAtIndex:0];
    NSDictionary *dataDic = [self dictionaryWithJsonString:jsonStr];
    NSInteger currentIndex = [[dataDic objectForKey:@"index"] integerValue];
    MLSelectPhotoBrowserViewController *browserVc = [[MLSelectPhotoBrowserViewController alloc] init];
    [browserVc setValue:@(NO) forKeyPath:@"isTrashing"];
    browserVc.isModelData = NO;
    browserVc.currentPage = currentIndex;
    browserVc.photos = [dataDic objectForKey:@"data"];
    browserVc.deleteCallBack = ^(NSArray *assets){
    };
    TDNavgationController *nav = [[TDNavgationController alloc] initWithRootViewController:browserVc];
    nav.navigationBar.translucent = NO;
    nav.navigationBar.barTintColor = RGB(0x3691e6);
    nav.navigationBar.tintColor = [UIColor whiteColor];
    [nav.navigationBar setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[UIColor whiteColor], NSForegroundColorAttributeName, [UIFont boldSystemFontOfSize:18.f], NSFontAttributeName, nil]];
    [self.viewController presentViewController:nav animated:YES completion:^{
    }];
}

/*!
 * @brief 把格式化的JSON格式的字符串转换成字典
 * @param jsonString JSON格式的字符串
 * @return 返回字典
 */
- (NSDictionary *)dictionaryWithJsonString:(NSString *)jsonString {
    if (jsonString == nil) {
        return nil;
    }
    
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSError *err;
    NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:jsonData
                                                        options:NSJSONReadingMutableContainers
                                                          error:&err];
    if(err) {
        NSLog(@"json解析失败：%@",err);
        return nil;
    }
    return dic;
}

@end
