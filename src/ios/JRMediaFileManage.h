//
//  JRMediaFileManage.h
//  JRCamera
//
//  Created by 路亮亮 on 16/2/24.
//
//

#import <Foundation/Foundation.h>

@interface JRMediaFileManage : NSObject

+ (JRMediaFileManage*)shareInstance;
//获取图片,视频ID
- (NSString *)getPictureSign;
//图片储路径
- (NSString *)getJRMediaPathWithSign:(NSString *)sign Type:(BOOL)isLeft;
//根据路径删除文件
- (BOOL)deleteFileWithEyeType:(BOOL)isLeftEye;
//删除所有文件
- (BOOL)deleteAllFiles;

@end
