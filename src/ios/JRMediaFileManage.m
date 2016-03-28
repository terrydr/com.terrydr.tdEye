//
//  JRMediaFileManage.m
//  JRCamera
//
//  Created by 路亮亮 on 16/2/24.
//
//

#import "JRMediaFileManage.h"

@implementation JRMediaFileManage

+ (JRMediaFileManage*)shareInstance
{
    static dispatch_once_t onceToken;
    static JRMediaFileManage* interface = nil;
    dispatch_once(&onceToken, ^{
        interface = [[JRMediaFileManage alloc]init];
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
