//
//  JRMediaFileManage.h
//  JRCamera
//
//  Created by 路亮亮 on 16/2/24.
//
//

#import <Foundation/Foundation.h>
#import "JREyeTypeModel.h"
#import "JRPictureModel.h"

@interface JRMediaFileManage : NSObject

+ (JRMediaFileManage*)shareInstance;
//获取图片,视频ID
- (NSString *)getPictureSign;
//图片储路径
- (NSString *)getJRMediaPathWithType:(BOOL)isLeft;
//根据路径删除文件
- (BOOL)deleteFileWithEyeType:(BOOL)isLeftEye;
//删除所有文件
- (BOOL)deleteAllFiles;
//获取图片路径
- (NSString *)getImagePathWithPictureName:(NSString *)pictureName isLeftEye:(BOOL)eyeType;

@end
