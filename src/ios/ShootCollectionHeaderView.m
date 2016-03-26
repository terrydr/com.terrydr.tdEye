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
    self.typeNameLabel = [[UILabel alloc] initWithFrame:CGRectMake(20, 20, CGRectGetWidth(self.frame)-80, 20)];
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
