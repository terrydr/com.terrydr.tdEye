//
//  WYVideoCaptureController.m
//  WYAVFoundation
//
//  Created by 王俨 on 15/12/31.
//  Copyright © 2015年 wangyan. All rights reserved.
//

#import "WYVideoCaptureController.h"
#import <AVFoundation/AVFoundation.h>
#import <AssetsLibrary/AssetsLibrary.h>
#import "UIView+Extension.h"
#import "NSTimer+Addtion.h"
#import "ProgressView.h"
#import "UIView+AutoLayoutViews.h"
#import "JRMediaFileManage.h"
#import "PictureScanViewController.h"

typedef void(^PropertyChangeBlock)(AVCaptureDevice *captureDevice);
#define kAnimationDuration 0.2
#define kTimeChangeDuration 0.1
#define kVideoTotalTime 30
#define kVideoLimit 10

@interface WYVideoCaptureController (){
    UIBarButtonItem *_leftItem;
    CGRect _leftBtnFrame;
    CGRect _centerBtnFrame;
    CGRect _rightBtnFrame;
    AVPlayer *_player;
    AVPlayerLayer *_playerLayer;
    BOOL _isLeftEye;
    int _takenPictureCount;
}
@property (nonatomic, strong) UIButton *closeBtn;
@property (nonatomic, strong) UISlider *wbSlider;
@property (nonatomic, strong) UIView *viewContainer;
@property (nonatomic, strong) ProgressView *progressView;
@property (nonatomic, strong) UILabel *dotLabel;
@property (nonatomic, strong) UIButton *leftBtn;
@property (nonatomic, strong) UIButton *centerBtn;
@property (nonatomic, strong) UIButton *rightBtn;
@property (nonatomic, strong) UIButton *cameraBtn;
@property (nonatomic, strong) UIImageView *imageView;
@property (nonatomic, strong) UIButton *imageViewBtn;
@property (nonatomic, strong) UIView *toolView;
@property (nonatomic, strong) UIButton *ISOBtn;
@property (nonatomic, strong) UIButton *whiteBalanceBtn;
@property (nonatomic, strong) UIView *whiteBalanceView;

/// 负责输入和输出设备之间数据传递
@property (nonatomic, strong) AVCaptureSession *captureSession;
@property (nonatomic, strong) AVCaptureDevice *captureDevice;
/// 负责从AVCaptureDevice获取数据
@property (nonatomic, strong) AVCaptureDeviceInput *captureDeviceInput;
/// 照片输出流
@property (nonatomic, strong) AVCaptureStillImageOutput *captureStillImageOutput;
/// 相机拍摄预览层
@property (nonatomic, strong) AVCaptureVideoPreviewLayer *captureVideoPreviewLayer;

@end

@implementation WYVideoCaptureController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self configureNavgationBar];
    [self setupUI];
    [self ChangeToLeft:YES];
    [self setupCaptureView];
    self.view.backgroundColor = RGB(0x16161b);
    
    if (_isScan) {
        [self pushToPictureScan:NO];
    }else{
        [self cleanOlderData];
    }
    
}

- (void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    [self initTakenParameters];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [_captureSession startRunning];
}
- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    [_captureSession stopRunning];
}

- (void)dealloc {
    NSLog(@"我是拍照控制器,我被销毁了");
}

- (void)configureNavgationBar{
    _leftItem = [[UIBarButtonItem alloc] initWithTitle:@"取消"
                                                 style:UIBarButtonItemStylePlain
                                                target:self
                                                action:@selector(leftBarButtonItemAction)];
    self.navigationItem.leftBarButtonItem = _leftItem;
}

- (void)leftBarButtonItemAction{
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)cleanOlderData{
    [[JRMediaFileManage shareInstance] deleteAllFiles];
}

- (void)setupCaptureView {
    // 1.初始化会话
    _captureSession = [[AVCaptureSession alloc] init];
    if ([_captureSession canSetSessionPreset:AVCaptureSessionPreset1280x720]) {
        [_captureSession setSessionPreset:AVCaptureSessionPreset1280x720]; // 设置分辨率
    }
    // 2.获得输入设备
    self.captureDevice = [self getCameraDeviceWithPosition:AVCaptureDevicePositionBack];
    [self wbSliderMethod:_wbSlider];
    if (_captureDevice == nil) {
        NSLog(@"获取输入设备失败");
        return;
    }
    // 4.根据输入设备初始化设备输入对象,用于获得输入数据
    NSError *error = nil;
    _captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:_captureDevice error:&error];
    if (error) {
        NSLog(@"创建设备输入对象失败 -- error = %@", error);
        return;
    }
    // 初始化图片设备输出对象
    _captureStillImageOutput = [[AVCaptureStillImageOutput alloc] init];
    _captureStillImageOutput.outputSettings = @{AVVideoCodecKey: AVVideoCodecJPEG}; // 输出设置
    // 6.将设备添加到会话中
    if ([_captureSession canAddInput:_captureDeviceInput]) {
        [_captureSession addInput:_captureDeviceInput];
    }
    // 7.将设备输出添加到会话中
    if ([_captureSession canAddOutput:_captureStillImageOutput]) {
        [_captureSession addOutput:_captureStillImageOutput];
    }
    // 8.创建视频预览层
    _captureVideoPreviewLayer = [[AVCaptureVideoPreviewLayer alloc] initWithSession:_captureSession];
    CALayer *layer = _viewContainer.layer;
    layer.masksToBounds = YES;
    _captureVideoPreviewLayer.frame = layer.bounds;
    _captureVideoPreviewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
    [layer insertSublayer:_captureVideoPreviewLayer atIndex:0];
    [self addNotificationToCaptureDevice:_captureDevice];
}

#pragma mark - CaptureMethod
- (AVCaptureDevice *)getCameraDeviceWithPosition:(AVCaptureDevicePosition)position {
    NSArray *devices = [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo];
    for (AVCaptureDevice *captureDevice in devices) {
        if (captureDevice.position == position) {
            return captureDevice;
        }
    }
    return nil;
}

/// 改变设备属性的统一方法
///
/// @param propertyChange 属性改变操作
- (void)changeDeviceProperty:(PropertyChangeBlock)propertyChange {
    AVCaptureDevice *captureDevice = _captureDeviceInput.device;
    NSError *error = nil;
    // 注意:在改变属性之前一定要先调用lockForConfiguration;调用完成之后使用unlockForConfiguration方法解锁
    if ([captureDevice lockForConfiguration:&error]) {
        propertyChange(captureDevice);
        [captureDevice unlockForConfiguration];
    } else {
        NSLog(@"更改设备属性错误 -- error = %@", error);
    }
}

#pragma mark - Notification
/// 给输入设备添加通知
- (void)addNotificationToCaptureDevice:(AVCaptureDevice *)captureDevie {
    // 注意添加区域改变捕获通知必须首先设置设备允许捕获
    [self changeDeviceProperty:^(AVCaptureDevice *captureDevice) {
        captureDevice.subjectAreaChangeMonitoringEnabled = YES;
    }];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(areaChanged:) name:AVCaptureDeviceSubjectAreaDidChangeNotification object:captureDevie];
}
/// 移除设备通知
- (void)removeNotificationFromCaptureDevice:(AVCaptureDevice *)captureDevice {
    [[NSNotificationCenter defaultCenter] removeObserver:self name:AVCaptureDeviceSubjectAreaDidChangeNotification object:captureDevice];
}

- (void)areaChanged:(NSNotification *)n {
    
}

#pragma mark - UI设计
- (void)setupUI {
    [self prepareUI];
    
    [self.view addSubview:_closeBtn];
    [self.view addSubview:_viewContainer];
    //[self.view addSubview:_progressView];
    [self.view addSubview:self.whiteBalanceView];
    [self.view addSubview:self.wbSlider];
    [self.view addSubview:self.toolView];
    [self.view addSubview:self.ISOBtn];
    [self.view addSubview:self.whiteBalanceBtn];
    [self.view addSubview:_imageView];
    [self.view addSubview:_imageViewBtn];
    
    [self.view addSubview:_dotLabel];
    [self.view addSubview:_leftBtn];
    [self.view addSubview:_centerBtn];
    [self.view addSubview:_rightBtn];
    [self.view addSubview:_cameraBtn];
    
    _closeBtn.frame = CGRectMake(0, 10, 60, 30);
    _viewContainer.frame = CGRectMake(0, 64, APP_WIDTH, APP_HEIGHT-64);
    _progressView.frame = CGRectMake(0, CGRectGetMaxY(_viewContainer.frame), APP_WIDTH, 5);
    _dotLabel.frame = CGRectMake((APP_WIDTH - 5) * 0.5,APP_HEIGHT - ((324.0f-30.0f)/2.0f) , 5, 5);
    CGFloat btnW = 40;
    CGFloat leftBtnX = (APP_WIDTH - 3 * btnW - 2 * 32) *0.5;
    CGFloat leftBtnY = CGRectGetMaxY(_dotLabel.frame) + 6;
    
    _leftBtnFrame = CGRectMake(leftBtnX, leftBtnY, btnW, btnW);
    _centerBtnFrame = CGRectOffset(_leftBtnFrame, 32 + btnW, 0);
    _rightBtnFrame = CGRectOffset(_centerBtnFrame, 32 + btnW, 0);
    [self restoreBtn];
    _cameraBtn.frame = CGRectMake((APP_WIDTH - 67) * 0.5, APP_HEIGHT-62-26, 62, 62);
    CGFloat imageViewOriginX = CGRectGetWidth(self.view.bounds)-60-20;
    _imageView.frame = CGRectMake(imageViewOriginX, APP_HEIGHT-62-26, 60, 60);
    _imageViewBtn.frame = _imageView.frame;
}
- (void)prepareUI {
    self.title = @"拍照";
    
    _closeBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_closeBtn setImage:[UIImage imageNamed:@"button_camera_close"] forState:UIControlStateNormal];
    [_closeBtn addTarget:self action:@selector(closeBtnClick) forControlEvents:UIControlEventTouchUpInside];
    
    _viewContainer = [[UIView alloc] init];
    //添加滑动手势
    UISwipeGestureRecognizer *leftSwipGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipes:)];
    leftSwipGestureRecognizer.direction = UISwipeGestureRecognizerDirectionLeft;
    [_viewContainer addGestureRecognizer:leftSwipGestureRecognizer];
    
    UISwipeGestureRecognizer *rightSwipGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipes:)];
    rightSwipGestureRecognizer.direction = UISwipeGestureRecognizerDirectionRight;
    [_viewContainer addGestureRecognizer:rightSwipGestureRecognizer];
    
    _imageView = [[UIImageView alloc] init];
    _imageView.contentMode = UIViewContentModeScaleAspectFill;
    _imageView.layer.masksToBounds = YES;
    
    _imageViewBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_imageViewBtn addTarget:self
                      action:@selector(imageViewBtnClick:)
            forControlEvents:UIControlEventTouchUpInside];
    
    _progressView = [[ProgressView alloc] initWithFrame:CGRectMake(0, APP_WIDTH + 44, APP_WIDTH, 5)];
    _progressView.totalTime = kVideoTotalTime;
    
    _dotLabel = [[UILabel alloc] init];  // 5 - 5
    _dotLabel.layer.cornerRadius = 2.5;
    _dotLabel.clipsToBounds = YES;
    _dotLabel.backgroundColor = RGB(0x76c000);
    
    _leftBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_leftBtn setTitle:@"左眼" forState:UIControlStateNormal];
    [_leftBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    _leftBtn.titleLabel.font = [UIFont systemFontOfSize:15.0];
    [_leftBtn addTarget:self action:@selector(leftBtnClick:) forControlEvents:UIControlEventTouchUpInside];
    _centerBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_centerBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [_centerBtn setTitle:@"左眼" forState:UIControlStateNormal];
    _centerBtn.titleLabel.font = [UIFont systemFontOfSize:15.0];
    _rightBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_rightBtn setTitle:@"右眼" forState:UIControlStateNormal];
    [_rightBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    _rightBtn.titleLabel.font = [UIFont systemFontOfSize:15.0];
    [_rightBtn addTarget:self action:@selector(rightBtnClick:) forControlEvents:UIControlEventTouchUpInside];
    
    _cameraBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_cameraBtn setImage:[UIImage imageNamed:@"takePhoto"] forState:UIControlStateNormal];
    [_cameraBtn addTarget:self action:@selector(cameraBtnClick:) forControlEvents:UIControlEventTouchUpInside];
}

#pragma mark - View
- (UIButton *)ISOBtn{
    if (!_ISOBtn) {
        UIImage *ISOImg = [UIImage imageNamed:@"iso-icon"];
        CGFloat ISOWidth = ISOImg.size.width;
        CGFloat ISOHeight = ISOImg.size.height;
        CGFloat ISOOriginX = APP_WIDTH-(30.0f/2.0f)-ISOWidth;
        CGFloat ISOOriginY = 64.0f + (45.0f/2.0f);
        _ISOBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        _ISOBtn.frame = CGRectMake(ISOOriginX, ISOOriginY, ISOWidth, ISOHeight);
        [_ISOBtn setBackgroundImage:ISOImg forState:UIControlStateNormal];
        [_ISOBtn addTarget:self action:@selector(ISOBtnClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _ISOBtn;
}

- (UIButton *)whiteBalanceBtn{
    if (!_whiteBalanceBtn) {
        UIImage *whiteBalanceImg = [UIImage imageNamed:@"white-balance-icon"];
        CGFloat whiteBalanceWidth = whiteBalanceImg.size.width;
        CGFloat whiteBalanceHeight = whiteBalanceImg.size.height;
        CGFloat whiteBalanceOriginX = APP_WIDTH-(30.0f/2.0f)-whiteBalanceWidth;
        CGFloat whiteBalanceOriginY = CGRectGetMaxY(_ISOBtn.frame) + (10.0f/2.0f);
        _whiteBalanceBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        _whiteBalanceBtn.frame = CGRectMake(whiteBalanceOriginX, whiteBalanceOriginY, whiteBalanceWidth, whiteBalanceHeight);
        [_whiteBalanceBtn setBackgroundImage:whiteBalanceImg forState:UIControlStateNormal];
        [_whiteBalanceBtn addTarget:self action:@selector(whiteBalanceBtnClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _whiteBalanceBtn;
}

- (UIView *)toolView{
    if (!_toolView) {
        CGFloat toolWidth = APP_WIDTH;
        CGFloat toolHeight = 324.0f/2.0f;
        CGFloat toolOriginX = 0.0f;
        CGFloat toolOriginY = APP_HEIGHT-toolHeight;
        _toolView = [[UIView alloc] initWithFrame:CGRectMake(toolOriginX, toolOriginY, toolWidth, toolHeight)];
        _toolView.backgroundColor = RGB(0x000000);
        _toolView.alpha = 0.6f;
    }
    return _toolView;
}

- (UIView *)whiteBalanceView{
    if (!_whiteBalanceView) {
        CGFloat width = 553.0f/2.0f;
        CGFloat height = 70.0f/2.0f;
        CGFloat originX = (APP_WIDTH-width)/2.0f;
        CGFloat originY = APP_HEIGHT - (324.0f+62.0f+44.0f)/2.0f;
        _whiteBalanceView = [[UIView alloc] initWithFrame:CGRectMake(originX, originY, width, height)];
        _whiteBalanceView.hidden = YES;
        _whiteBalanceView.backgroundColor = RGB(0x000000);
        _whiteBalanceView.alpha = 0.8f;
        _whiteBalanceView.layer.cornerRadius = 5.0f;
        _whiteBalanceView.layer.masksToBounds = YES;
    }
    return _whiteBalanceView;
}

- (UISlider *)wbSlider{
    if (!_wbSlider) {
        UIImage *leftImg = [UIImage imageNamed:@"whiteBalanceLefticon"];
        UIImage *rightImg = [UIImage imageNamed:@"whiteBalanceRighticon"];
        
        CGFloat sliderWidth = CGRectGetWidth(_whiteBalanceView.bounds) - (22.0f+22.0f)/2.0f;
        CGFloat sliderHeight = 31.0f;
        CGFloat sliderOriginX = CGRectGetMinX(_whiteBalanceView.frame) + 22.0f/2.0f;
        CGFloat sliderOriginY = CGRectGetMinY(_whiteBalanceView.frame) +((CGRectGetHeight(_whiteBalanceView.bounds)-sliderHeight)/2.0f);
        _wbSlider = [[UISlider alloc] initWithFrame:CGRectMake(sliderOriginX, sliderOriginY, sliderWidth, sliderHeight)];
        _wbSlider.hidden = YES;
        _wbSlider.minimumValue = 3000.0f;
        _wbSlider.maximumValue = 12000.0f;
        _wbSlider.value = 6000.0f;
        _wbSlider.minimumValueImage = leftImg;
        _wbSlider.maximumValueImage = rightImg;
        [_wbSlider addTarget:self action:@selector(wbSliderMethod:) forControlEvents:UIControlEventValueChanged];
    }
    return _wbSlider;
}

#pragma mark - ButtonClick
- (void)scanBtnClick:(UIButton *)btn{
    [self pushToPictureScan:YES];
}
-(void)imageViewBtnClick:(UIButton *)btn{
    [self pushToPictureScan:YES];
}
- (void)pushToPictureScan:(BOOL)animated{
    PictureScanViewController *scanVc = [[PictureScanViewController alloc] init];
    [self.navigationController pushViewController:scanVc animated:animated];
}
- (void)closeBtnClick {
    NSDictionary *pathDic = [NSDictionary dictionary];
    [[NSNotificationCenter defaultCenter] postNotificationName:@"TakePhotosFinishedNotification"
                                                        object:nil
                                                      userInfo:pathDic];
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)wbSliderMethod:(id)sender{
    UISlider *slider = (UISlider *)sender;
    AVCaptureWhiteBalanceTemperatureAndTintValues temperatureAndTint = {
        .temperature = slider.value,
        .tint = 0,
    };
    AVCaptureWhiteBalanceGains wbGains = [_captureDevice deviceWhiteBalanceGainsForTemperatureAndTintValues:temperatureAndTint];
    [_captureDevice lockForConfiguration:nil];
    [_captureDevice setWhiteBalanceModeLockedWithDeviceWhiteBalanceGains:wbGains completionHandler:nil];
    [_captureDevice unlockForConfiguration];
}

- (void)ISOBtnClick:(id)sender{
    UIButton *btn = (UIButton *)sender;
    btn.selected = !btn.selected;
    
    [_captureDevice lockForConfiguration:nil];
    if (btn.isSelected) {
        [_captureDevice setExposureModeCustomWithDuration:CMTimeMakeWithSeconds(0.05, 1000) ISO:40.0 completionHandler:nil];
    }else{
        [_captureDevice setExposureModeCustomWithDuration:CMTimeMakeWithSeconds(0.05, 1000) ISO:80.0 completionHandler:nil];
    }
    [_captureDevice unlockForConfiguration];
}

- (void)whiteBalanceBtnClick:(id)sender{
    UIButton *btn = (UIButton *)sender;
    btn.selected = !btn.isSelected;
    _whiteBalanceView.hidden = !_whiteBalanceView.hidden;
    _wbSlider.hidden = !_wbSlider.hidden;
    UIImage *whiteBalanceImg = [UIImage imageNamed:@"white-balance-icon"];
    UIImage *whiteBalanceSelectedImg = [UIImage imageNamed:@"white-balance-click-icon"];
    if (btn.isSelected) {
        [_whiteBalanceBtn setBackgroundImage:whiteBalanceSelectedImg
                                    forState:UIControlStateNormal];
    }else{
        [_whiteBalanceBtn setBackgroundImage:whiteBalanceImg
                                    forState:UIControlStateNormal];
    }
}

- (void)handleSwipes:(UISwipeGestureRecognizer *)sender{
    if (sender.direction == UISwipeGestureRecognizerDirectionLeft){
        if (_isLeftEye) {
            [self rightBtnClick:nil];
        }
    }else if(sender.direction == UISwipeGestureRecognizerDirectionRight){
        if (!_isLeftEye) {
            [self leftBtnClick:nil];
        }
    }
}

- (void)leftBtnClick:(UIButton *)btn {
    _dotLabel.hidden = YES;
    [UIView animateWithDuration:kAnimationDuration animations:^{
        _leftBtn.frame = _centerBtnFrame;
        _centerBtn.frame = _rightBtnFrame;
    } completion:^(BOOL finished) {
        [self ChangeToLeft:YES];
    }];
}
- (void)rightBtnClick:(UIButton *)btn {
    _dotLabel.hidden = YES;
    [UIView animateWithDuration:kAnimationDuration animations:^{
        _rightBtn.frame = _centerBtnFrame;
        _centerBtn.frame = _leftBtnFrame;
    } completion:^(BOOL finished) {
        [self ChangeToLeft:NO];
    }];
}
- (void)cameraBtnClick:(UIButton *)btn {
    _takenPictureCount++;
    if (_takenPictureCount==1) {
        [[JRMediaFileManage shareInstance] deleteFileWithEyeType:_isLeftEye];
    }
    
    __weak WYVideoCaptureController *wself = self;
    // 1.根据设备输出获得链接
    AVCaptureConnection *captureConnection = [_captureStillImageOutput connectionWithMediaType:AVMediaTypeVideo];
    // 2.根据链接取得设备输出的数据
    [_captureStillImageOutput captureStillImageAsynchronouslyFromConnection:captureConnection completionHandler:^(CMSampleBufferRef imageDataSampleBuffer, NSError *error) {
        NSData *imageData = [AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:imageDataSampleBuffer];
        [wself saveTakenPictureData:imageData];
    }];
}

- (void)saveTakenPictureData:(NSData *)imgData{
    UIImage *image = [UIImage imageWithData:imgData];
    UIImage *saveImg = [UIImage imageWithCGImage:[self handleImage:image]];
    _imageView.image = saveImg;
    NSData *saveImgData = UIImageJPEGRepresentation(saveImg, 1.0f);
    
    JRMediaFileManage *fileManage = [JRMediaFileManage shareInstance];
    NSString *filePath = [fileManage getJRMediaPathWithType:_isLeftEye];
    NSString *imageName = [NSString stringWithFormat:@"%02d.png",_takenPictureCount];
    NSString *imgPath = [NSString stringWithFormat:@"%@/%@",filePath,imageName];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    BOOL result = [fileManager createFileAtPath:imgPath
                                       contents:saveImgData
                                     attributes:nil];
    NSLog(@"result:%d",result);
}

#pragma mark - private
- (void)restoreBtn {
    _leftBtn.frame = _leftBtnFrame;
    _centerBtn.frame = _centerBtnFrame;
    _rightBtn.frame = _rightBtnFrame;
    _dotLabel.hidden = NO;
    [_centerBtn setTitleColor:RGB(0x76c000) forState:UIControlStateNormal];
}

- (void)initTakenParameters{
    _takenPictureCount = 0;
}
/// 切换拍照和视频录制
///
/// @param isPhoto YES->拍照  NO->视频录制
- (void)ChangeToLeft:(BOOL)isLeft{
    [self restoreBtn];
    [self initTakenParameters];
    _isLeftEye = isLeft;
    NSString *centerTitle = isLeft ? @"左眼" : @"右眼";
    [_centerBtn setTitle:centerTitle forState:UIControlStateNormal];
    _leftBtn.hidden = isLeft;
    _rightBtn.hidden = !isLeft;
}

- (CGImageRef)handleImage:(UIImage *)image {
    UIGraphicsBeginImageContextWithOptions(self.view.size, NO, 1.0);
    [image drawInRect:CGRectMake(0, 0, self.view.width, self.view.height)];
    CGImageRef imageRef = UIGraphicsGetImageFromCurrentImageContext().CGImage;
    CGImageRef subRef = CGImageCreateWithImageInRect(imageRef, CGRectOffset(_viewContainer.frame, 0, 88));
    return subRef;
}

@end
