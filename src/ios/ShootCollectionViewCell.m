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
        
        self.selectedView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, CGRectGetWidth(self.frame), CGRectGetWidth(self.frame))];
        self.selectedView.hidden = YES;
        self.selectedView.backgroundColor = [UIColor blackColor];
        self.selectedView.alpha = 0.6f;
        [self addSubview:self.selectedView];
        
        CGFloat origin = (CGRectGetWidth(self.frame)-30)/2.0f;
        UIImageView *selectedImgView = [[UIImageView alloc] initWithFrame:CGRectMake(origin, origin, 30, 30)];
        selectedImgView.image = [UIImage imageNamed:@"Select_list_btn_solid"];
        [_selectedView addSubview:selectedImgView];
    }
    return self;
}

@end
