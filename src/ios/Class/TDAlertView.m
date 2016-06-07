//
//  TDAlertView.m
//  TDAlertView
//
//  Created by 路亮亮 on 16/6/6.
//  Copyright © 2016年 路亮亮. All rights reserved.
//

#import "TDAlertView.h"

//16进制颜色
#define kUIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]
//6&其他适配
#define QBSIX_RATIO    ([[UIScreen mainScreen] bounds].size.height==667.f?1.f:(([[UIScreen mainScreen] bounds].size.height - 64.f)/603.f))

static selectButton STAblock;

@interface TDAlertView()

@property (nonatomic, strong) UIView *tdAlertView;
@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UILabel *contentLabel;
@property (nonatomic, strong) UIImageView *checkView;
@property (nonatomic, strong) UIButton *cancelBtn;
@property (nonatomic, strong) UIButton *sureBtn;

@end

@implementation TDAlertView

- (instancetype)init {
    self = [super init];
    if (self) {
        self.frame = [UIScreen mainScreen].bounds;
        self.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:.3f];
        [self drawView];
    }
    return self;
}

- (void)drawView{
    [self addSubview:self.tdAlertView];
    [self.tdAlertView addSubview:self.titleLabel];
    [self.tdAlertView addSubview:self.contentLabel];
    [self.tdAlertView addSubview:self.checkView];
    [self.tdAlertView addSubview:self.cancelBtn];
    [self.tdAlertView addSubview:self.sureBtn];
}

- (UIView *)tdAlertView{
    if (!_tdAlertView) {
        CGFloat mainWidth = [UIScreen mainScreen].bounds.size.width;
        CGFloat mainHeight = [UIScreen mainScreen].bounds.size.height;
        
        CGFloat alertWidth = (666.0f/2.0f)*QBSIX_RATIO;
        CGFloat alertHeight = 336.0f/2.0f*QBSIX_RATIO;
        CGFloat alertOriginX = (mainWidth-alertWidth)/2.0f;
        CGFloat alertOriginY = (mainHeight-alertHeight)/2.0f;
        
        _tdAlertView = [[UIView alloc]initWithFrame:CGRectMake(alertOriginX,alertOriginY,alertWidth,alertHeight)];
        _tdAlertView.layer.cornerRadius = 5.f;
        _tdAlertView.layer.masksToBounds = YES;
        _tdAlertView.backgroundColor = [UIColor colorWithWhite:1. alpha:.95];
        
        CGFloat linefWidth = alertWidth;
        CGFloat linefHeight = 0.5f;
        CGFloat linefOriginX = 0.0f;
        CGFloat linefOriginY = (50.0f/2.0f)*QBSIX_RATIO*2+20.0f+(58.0f/2.0f)*QBSIX_RATIO;
        
        UIView *linef = [[UIView alloc] initWithFrame:CGRectMake(linefOriginX, linefOriginY, linefWidth, linefHeight)];
        linef.backgroundColor = kUIColorFromRGB(0xcccccc);
        [_tdAlertView addSubview:linef];
        
        CGFloat linesOriginX = alertWidth/2.0f;
        CGFloat linesOriginY = (50.0f/2.0f)*QBSIX_RATIO*2+20.0f+(58.0f/2.0f)*QBSIX_RATIO;
        CGFloat linesWidth = 0.5f;
        CGFloat linesHeight = alertHeight-linesOriginY;
        
        UIView *lines = [[UIView alloc] initWithFrame:CGRectMake(linesOriginX, linesOriginY, linesWidth, linesHeight)];
        lines.backgroundColor = kUIColorFromRGB(0xcccccc);
        [_tdAlertView addSubview:lines];
    }
    return _tdAlertView;
}

- (UILabel *)titleLabel{
    if (!_titleLabel) {
        CGFloat titleWidth = (666.0f/2.0f)*QBSIX_RATIO;
        CGFloat titleHeight = 25.0f;
        CGFloat titleOriginX = 0.0f;
        CGFloat titleOriginY = (50.0f/2.0f)*QBSIX_RATIO;
        
        _titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(titleOriginX, titleOriginY, titleWidth, titleHeight)];
        _titleLabel.text = @"是否删除照片";
        _titleLabel.textAlignment = NSTextAlignmentCenter;
        _titleLabel.font = [UIFont systemFontOfSize:20.0f];
    }
    return _titleLabel;
}

- (UILabel *)contentLabel{
    if (!_contentLabel) {
        CGFloat contentWidth = (666.0f/2.0f)*QBSIX_RATIO-20.0f;
        CGFloat contentHeight = 25.0f;
        CGFloat contentOriginX = 20.0f;
        CGFloat contentOriginY = (50.0f/2.0f)*QBSIX_RATIO*2+10.0f;
        
        _contentLabel = [[UILabel alloc] initWithFrame:CGRectMake(contentOriginX, contentOriginY, contentWidth, contentHeight)];
        _contentLabel.text = @"下次不再提示";
        _contentLabel.textAlignment = NSTextAlignmentCenter;
        _contentLabel.textColor = [UIColor redColor];
        _contentLabel.font = [UIFont systemFontOfSize:15.0f];
    }
    return _contentLabel;
}

- (UIImageView *)checkView{
    if (!_checkView) {
        CGFloat checkOriginX = (666.0f/2.0f)*QBSIX_RATIO/3.0f-20.0f;
        CGFloat checkOriginY = (50.0f/2.0f)*QBSIX_RATIO*2+12.0f;
        CGFloat checkWidth = 44.0f/2.0f;
        CGFloat checkHeight = 44.0f/2.0f;
        
        _checkView = [[UIImageView alloc] init];
        _checkView.frame = CGRectMake(checkOriginX, checkOriginY, checkWidth, checkHeight);
        _checkView.image = [UIImage imageNamed:@"tdaUnselected"];
        _checkView.userInteractionEnabled = YES;
        
        UIButton *checkBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        checkBtn.frame = CGRectMake(0, 0, checkWidth, checkHeight);
        [checkBtn addTarget:self action:@selector(checkBtnClick:) forControlEvents:UIControlEventTouchUpInside];
        [_checkView addSubview:checkBtn];
    }
    return _checkView;
}

- (UIButton *)cancelBtn{
    if (!_cancelBtn) {
        CGFloat cancelOriginX = 0.0f;
        CGFloat cancelOriginY = (50.0f/2.0f)*QBSIX_RATIO*2+20.0f+(58.0f/2.0f)*QBSIX_RATIO;
        CGFloat cancelWidth = (666.0f/2.0f)*QBSIX_RATIO/2.0f;
        CGFloat cancelHeight = 336.0f/2.0f*QBSIX_RATIO-cancelOriginY;
        
        _cancelBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        _cancelBtn.frame = CGRectMake(cancelOriginX, cancelOriginY, cancelWidth, cancelHeight);
        [_cancelBtn setTitle:@"取消" forState:UIControlStateNormal];
        [_cancelBtn setTitleColor:[UIColor colorWithRed:0.0f/255.0f green:122.0f/255.0f blue:1.0f alpha:1.0f] forState:UIControlStateNormal];
        [_cancelBtn addTarget:self action:@selector(cancelBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _cancelBtn;
}

- (UIButton *)sureBtn{
    if (!_sureBtn) {
        CGFloat sureOriginX = (666.0f/2.0f)*QBSIX_RATIO/2.0f;
        CGFloat sureOriginY = (50.0f/2.0f)*QBSIX_RATIO*2+20.0f+(58.0f/2.0f)*QBSIX_RATIO;
        CGFloat sureWidth = (666.0f/2.0f)*QBSIX_RATIO/2.0f;
        CGFloat sureHeight = 336.0f/2.0f*QBSIX_RATIO-sureOriginY;
        
        _sureBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        _sureBtn.frame = CGRectMake(sureOriginX, sureOriginY, sureWidth, sureHeight);
        [_sureBtn setTitle:@"确定" forState:UIControlStateNormal];
        [_sureBtn setTitleColor:[UIColor colorWithRed:0.0f/255.0f green:122.0f/255.0f blue:1.0f alpha:1.0f] forState:UIControlStateNormal];
        [_sureBtn addTarget:self action:@selector(sureBtnClick) forControlEvents:UIControlEventTouchUpInside];
    }
    return _sureBtn;
}

- (void)tdShowWithSelectedBlock:(selectButton)block{
    STAblock = block;
    
    UIWindow *keyWindow = [UIApplication sharedApplication].keyWindow;
    [keyWindow addSubview:self];
}

- (void)tdHide{
    [self removeFromSuperview];
}

- (void)cancelBtnClick{
    STAblock(TDAlertButtonCancel);
    [self tdHide];
}

- (void)sureBtnClick{
    STAblock(TDAlertButtonOk);
    [self tdHide];
}

- (void)checkBtnClick:(id)sender{
    UIButton *btn = (UIButton *)sender;
    btn.selected = !btn.isSelected;
    if (btn.isSelected) {
       _checkView.image = [UIImage imageNamed:@"tdaSelected"];
    }else{
        _checkView.image = [UIImage imageNamed:@"tdaUnselected"];
    }
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end

@interface UIButton (ZoomButton)

@end

@implementation UIButton (ZoomButton)

- (BOOL)pointInside:(CGPoint)point withEvent:(UIEvent*)event
{
    CGRect bounds = self.bounds;
    //若原热区小于44x44，则放大热区，否则保持原大小不变
    CGFloat widthDelta = MAX(44.0 - bounds.size.width, 0);
    CGFloat heightDelta = MAX(44.0 - bounds.size.height, 0);
    bounds = CGRectInset(bounds, -0.5 * widthDelta, -0.5 * heightDelta);
    return CGRectContainsPoint(bounds, point);
}

@end
