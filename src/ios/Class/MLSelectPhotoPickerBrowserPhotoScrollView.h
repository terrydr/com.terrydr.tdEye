//  github: https://github.com/MakeZL/MLSelectPhoto
//  author: @email <120886865@qq.com>
//
//  ZLPhotoPickerBrowserPhotoScrollView.h
//  ZLAssetsPickerDemo
//
//  Created by 张磊 on 14-11-14.
//  Copyright (c) 2014年 com.zixue101.www. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "MLSelectPhotoPickerBrowserPhotoImageView.h"
#import "MLSelectPhotoPickerBrowserPhotoView.h"

typedef void(^callBackBlock)(id obj);
@class MLSelectPhotoPickerBrowserPhotoScrollView;

@protocol ZLPhotoPickerPhotoScrollViewDelegate <NSObject>
@optional
// 单击调用
- (void) pickerPhotoScrollViewDidSingleClick:(MLSelectPhotoPickerBrowserPhotoScrollView *)photoScrollView;
// 长按调用
- (void) pickerPhotoScrollViewDidLongPress:(MLSelectPhotoPickerBrowserPhotoScrollView *)scrollView mlPhotoImageView:(MLSelectPhotoPickerBrowserPhotoImageView *)photoImageView;
@end

@interface MLSelectPhotoPickerBrowserPhotoScrollView : UIScrollView <UIScrollViewDelegate, ZLPhotoPickerBrowserPhotoImageViewDelegate,ZLPhotoPickerBrowserPhotoViewDelegate>

@property (nonatomic,strong) UIImage *photo;
@property (nonatomic, weak) id <ZLPhotoPickerPhotoScrollViewDelegate> photoScrollViewDelegate;
// 单击销毁的block
@property (copy,nonatomic) callBackBlock callback;

@end
