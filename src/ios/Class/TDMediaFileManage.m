//
//  TDMediaFileManage.m
//  TDCamera
//
//  Created by 路亮亮 on 16/2/24.
//
//

#import "TDMediaFileManage.h"

@implementation TDMediaFileManage

+ (TDMediaFileManage*)shareInstance
{
    static dispatch_once_t onceToken;
    static TDMediaFileManage* interface = nil;
    dispatch_once(&onceToken, ^{
        interface = [[TDMediaFileManage alloc]init];
    });
    return interface;
}

//获取图片,视频ID
- (NSString *)getPictureSign{
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyyMMddhhmmssSSS"];
    return [formatter stringFromDate:[NSDate date]];
}

//图片存储路径
- (NSString *)getJRMediaPathWithType:(BOOL)isLeft{
    NSString *dir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *path = [dir stringByAppendingPathComponent:@"JRMedia"];
    BOOL isDir = NO;
    if(![[NSFileManager defaultManager] fileExistsAtPath:path isDirectory:&isDir]) {
        NSError *e = nil;
        [[NSFileManager defaultManager] createDirectoryAtPath:path withIntermediateDirectories:NO attributes:nil error:&e];
    }
    
    NSString *mediaPath;
    if (isLeft) {
        mediaPath = [NSString stringWithFormat:@"%@/%@",path,@"leftEye"];
    }else{
        mediaPath = [NSString stringWithFormat:@"%@/%@",path,@"rightEye"];
    }
    
    if(![[NSFileManager defaultManager] fileExistsAtPath:mediaPath isDirectory:&isDir]) {
        NSError *e = nil;
        [[NSFileManager defaultManager] createDirectoryAtPath:mediaPath withIntermediateDirectories:NO attributes:nil error:&e];
    }
    return mediaPath;
}

//根据路径保存文件
- (BOOL)saveFileWithPath:(NSString *)filePath fileData:(NSData *)data{
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    BOOL isDir = NO;
    if([fileManager fileExistsAtPath:filePath isDirectory:&isDir]) {
        NSError *e = nil;
        [fileManager removeItemAtPath:filePath error:&e];
    }
    
    BOOL result = [fileManager createFileAtPath:filePath
                                       contents:data
                                     attributes:nil];
    return result;
}

//根据路径删除单个文件
- (void)deleteSingleFileWithPictureName:(NSString *)pictureName isLeftEye:(BOOL)eyeType{
    NSString *filePath = [self getImagePathWithPictureName:pictureName isLeftEye:eyeType];
    BOOL isDir = NO;
    if([[NSFileManager defaultManager] fileExistsAtPath:filePath isDirectory:&isDir]) {
        NSError *e = nil;
        [[NSFileManager defaultManager] removeItemAtPath:filePath error:&e];
    }
}

//根据路径删除文件
- (BOOL)deleteFileWithEyeType:(BOOL)isLeftEye{
    BOOL isDir = NO;
    BOOL result = NO;
    NSString *filePath;
    NSString *dir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *path = [dir stringByAppendingPathComponent:@"JRMedia"];
    if (isLeftEye) {
        filePath = [NSString stringWithFormat:@"%@/%@",path,@"leftEye"];
    }else{
        filePath = [NSString stringWithFormat:@"%@/%@",path,@"rightEye"];
    }
    if([[NSFileManager defaultManager] fileExistsAtPath:filePath isDirectory:&isDir]) {
        NSError *e = nil;
        result = [[NSFileManager defaultManager] removeItemAtPath:filePath error:&e];
    }
    return result;
}

//删除所有文件
- (BOOL)deleteAllFiles{
    NSString *dir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *filePath = [dir stringByAppendingPathComponent:@"JRMedia"];
    BOOL isDir = NO;
    BOOL result = NO;
    if([[NSFileManager defaultManager] fileExistsAtPath:filePath isDirectory:&isDir]) {
        NSError *e = nil;
        result = [[NSFileManager defaultManager] removeItemAtPath:filePath error:&e];
    }
    return result;
}

- (NSString *)getImagePathWithPictureName:(NSString *)pictureName isLeftEye:(BOOL)eyeType{
    NSString *filePath = [self getJRMediaPathWithType:eyeType];
    NSString *picturePath = [NSString stringWithFormat:@"%@/%@",filePath,pictureName];
    return picturePath;
}

@end
