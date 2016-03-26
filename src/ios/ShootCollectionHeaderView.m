//
//  ShootCollectionHeaderView.m
//  JRHealthcare
//
//  Created by 路亮 on 15/10/26.
//  Copyright (c) 2015年 路亮. All rights reserved.
//

#import "ShootCollectionHeaderView.h"

@implementation ShootCollectionHeaderView

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

- (id)initWithFrame:(CGRect)frame{
    self = [super initWithFrame:frame];
    if (self) {
        [self configctureSubviews];
    }
    return self;
}

- (void)configctureSubviews{
    self.backgroundColor = RGB(0xffffff);
    UIImage *iconImg = [UIImage imageNamed:@"leftEyeicon"];
    CGFloat imgViewWidth = iconImg.size.width;
    CGFloat imgViewHeitht = iconImg.size.height;
    CGFloat imgViewOriginX = 20.0f;
    CGFloat imgViewOriginY = (CGRectGetHeight(self.bounds)-imgViewHeitht)/2.0f;
    
    self.iconImgView = [[UIImageView alloc] initWithFrame:CGRectMake(imgViewOriginX, imgViewOriginY, imgViewWidth, imgViewHeitht)];
    [self addSubview:_iconImgView];
    
    CGFloat typeOriginX = CGRectGetMaxX(_iconImgView.frame) + 12.0f/2.0f;
    CGFloat typeOriginY = 0.0f;
    CGFloat typeWidth = CGRectGetWidth(self.bounds)-typeOriginX-20.0f;
    CGFloat typeHeight = CGRectGetHeight(self.bounds);
    self.typeNameLabel = [[UILabel alloc] initWithFrame:CGRectMake(typeOriginX, typeOriginY, typeWidth, typeHeight)];
    self.typeNameLabel.textAlignment = NSTextAlignmentLeft;
    self.typeNameLabel.font = [UIFont systemFontOfSize:16.0f];
    [self addSubview:_typeNameLabel];
    
    self.chooseBtn = [ShootCollectionButton buttonWithType:UIButtonTypeCustom];
    self.chooseBtn.hidden = YES;
    self.chooseBtn.isSelected = NO;
    self.chooseBtn.frame = CGRectMake(CGRectGetWidth(self.frame)-40-5, 5, 40, 30);
    [self.chooseBtn setTitle:@"选择" forState:UIControlStateNormal];
    [self.chooseBtn setTitleColor:RGB(0x4f8aff) forState:UIControlStateNormal];
    self.chooseBtn.titleLabel.font = [UIFont systemFontOfSize:16.0f];
    [self addSubview:_chooseBtn];
}

@end
