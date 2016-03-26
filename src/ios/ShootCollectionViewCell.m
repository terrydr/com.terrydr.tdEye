//
//  ShootCollectionViewCell.m
//  JRHealthcare
//
//  Created by 路亮 on 15/10/24.
//  Copyright (c) 2015年 路亮. All rights reserved.
//

#import "ShootCollectionViewCell.h"

@implementation ShootCollectionViewCell

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        self.backgroundColor = [UIColor purpleColor];
        
        self.imgView = [[UIImageView alloc]initWithFrame:CGRectMake(0, 0, CGRectGetWidth(self.frame), CGRectGetWidth(self.frame))];
        self.imgView.backgroundColor = [UIColor groupTableViewBackgroundColor];
        self.imgView.contentMode = UIViewContentModeScaleAspectFill;
        self.imgView.layer.masksToBounds = YES;
        [self addSubview:self.imgView];
        
        UIImage *selectedImg = [UIImage imageNamed:@"albumSelectedicon"];
        CGFloat selectedWidth = selectedImg.size.width;
        CGFloat selectedHeight = selectedImg.size.height;
        CGFloat selectedOriginX = CGRectGetWidth(self.bounds)-selectedWidth-12.0f/2.0f;
        CGFloat selectedOriginY = CGRectGetHeight(self.bounds)-selectedHeight-12.0f/2.0f;
        self.selectedImgView = [[UIImageView alloc] initWithFrame:CGRectMake(selectedOriginX, selectedOriginY, selectedWidth, selectedHeight)];
        self.selectedImgView.hidden = YES;
        self.selectedImgView.image = selectedImg;
        [self addSubview:self.selectedImgView];
    }
    return self;
}

@end
