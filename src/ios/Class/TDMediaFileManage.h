//
//  TDMediaFileManage.h
//  TDCamera
//
//  Created by 路亮亮 on 16/2/24.
//
//

#import <Foundation/Foundation.h>
#import "TDEyeTypeModel.h"
#import "TDPictureModel.h"

@interface TDMediaFileManage : NSObject

+ (TDMediaFileManage*)shareInstance;
//获取图片,视频ID
- (NSString *)getPictureSign;
//图片储路径
- (NSString *)getJRMediaPathWithType:(BOOL)isLeft;
//根据路径保存文件
- (BOOL)saveFileWithPath:(NSString *)filePath fileData:(NSData *)data;
//根据路径删除单个文件
- (void)deleteSingleFileWithPictureName:(NSString *)pictureName isLeftEye:(BOOL)eyeType;
//根据路径删除文件
- (BOOL)deleteFileWithEyeType:(BOOL)isLeftEye;
//删除所有文件
- (BOOL)deleteAllFiles;
//获取图片路径
- (NSString *)getImagePathWithPictureName:(NSString *)pictureName isLeftEye:(BOOL)eyeType;

@end
