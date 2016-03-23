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
@property (nonatomic, strong) UIButton *flashBtn;
@property (nonatomic, strong) UIView *viewContainer;
@property (nonatomic, strong) ProgressView *progressView;
@property (nonatomic, strong) UILabel *dotLabel;
@property (nonatomic, strong) UIButton *leftBtn;
@property (nonatomic, strong) UIButton *centerBtn;
@property (nonatomic, strong) UIButton *rightBtn;
@property (nonatomic, strong) UIButton *cameraBtn;
@property (nonatomic, strong) UIImageView *imageView;
@property (nonatomic, strong) UIButton *imageViewBtn;

/// 负责输入和输出设备之间数据传递
@property (nonatomic, strong) AVCaptureSession *captureSession;
@property (nonatomic, strong) AVCaptureDevice *captureDevice;
/// 负责从AVCaptureDevice获取数据
@property (nonatomic, strong) AVCaptureDeviceInput *captureDeviceInput;
/// 照片输出流
@property (nonatomic, strong) AVCaptureStillImageOutput *captureStillImageOutput;
/// 相机拍摄预览层
@property (nonatomic, strong) AVCaptureVideoPreviewLayer *captureVideoPreviewLayer;
/// 是否允许旋转 (注意在旋转过程中禁止屏幕旋转)
@property (nonatomic, assign, getter=isEnableRotation) BOOL enableRotation;
/// 旋转前的屏幕大小
//@property (nonatomic, assign) CGRect lastBounds;
/// 后台任务标识
@property (nonatomic, assign) UIBackgroundTaskIdentifier backgroundTaskIndentifier;

@end

@implementation WYVideoCaptureController

- (void)viewDidLoad {
    [super viewDidLoad];
    
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
/// 隐藏状态栏
- (BOOL)prefersStatusBarHidden {
    return YES;
}

- (void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    self.navigationController.navigationBarHidden = YES;
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

#pragma mark - SuperMethod
- (BOOL)shouldAutorotate {
    return self.isEnableRotation;
}
/// 屏幕旋转时调整视频预览图层的方向
- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    AVCaptureConnection *captureConnection = [_captureVideoPreviewLayer connection];
    captureConnection.videoOrientation = (AVCaptureVideoOrientation)toInterfaceOrientation;
}
/// 旋转后重新设置大小
- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation {
    _captureVideoPreviewLayer.frame = _viewContainer.bounds;
}

#pragma mark - UI设计
- (void)setupUI {
    [self prepareUI];
    
    [self.view addSubview:_closeBtn];
    [self.view addSubview:_viewContainer];
    [self.view addSubview:_wbSlider];
    [self.view addSubview:_flashBtn];
    [self.view addSubview:_imageView];
    [self.view addSubview:_imageViewBtn];
    //[self.view addSubview:_progressView];
    [self.view addSubview:_dotLabel];
    [self.view addSubview:_leftBtn];
    [self.view addSubview:_centerBtn];
    [self.view addSubview:_rightBtn];
    [self.view addSubview:_cameraBtn];
    
    _closeBtn.frame = CGRectMake(0, 10, 60, 30);
    CGFloat sliderOriginX = (CGRectGetWidth(self.view.bounds)-200)/2.0;
    CGFloat sliderOriginY = 44+APP_WIDTH-40;
    _wbSlider.frame = CGRectMake(sliderOriginX, sliderOriginY, 200, 20);
    _flashBtn.frame = CGRectMake(CGRectGetWidth(self.view.bounds)-60, 60, 60, 30);
    _viewContainer.frame = CGRectMake(0, 44, APP_WIDTH, APP_WIDTH);
    _progressView.frame = CGRectMake(0, CGRectGetMaxY(_viewContainer.frame), APP_WIDTH, 5);
    _dotLabel.frame = CGRectMake((APP_WIDTH - 5) * 0.5, APP_WIDTH + 60 , 5, 5);
    CGFloat btnW = 40;
    CGFloat leftBtnX = (APP_WIDTH - 3 * btnW - 2 * 32) *0.5;
    CGFloat leftBtnY = CGRectGetMaxY(_dotLabel.frame) + 6;
    
    _leftBtnFrame = CGRectMake(leftBtnX, leftBtnY, btnW, btnW);
    _centerBtnFrame = CGRectOffset(_leftBtnFrame, 32 + btnW, 0);
    _rightBtnFrame = CGRectOffset(_centerBtnFrame, 32 + btnW, 0);
    [self restoreBtn];
    _cameraBtn.frame = CGRectMake((APP_WIDTH - 67) * 0.5, CGRectGetMaxY(_centerBtnFrame) + 32, 67, 67);
    CGFloat imageViewOriginX = CGRectGetWidth(self.view.bounds)-60-20;
    _imageView.frame = CGRectMake(imageViewOriginX, CGRectGetMaxY(_centerBtnFrame) + 32, 60, 60);
    _imageViewBtn.frame = _imageView.frame;
}
- (void)prepareUI {
    self.title = @"拍照";
    
    _closeBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_closeBtn setImage:[UIImage imageNamed:@"button_camera_close"] forState:UIControlStateNormal];
    [_closeBtn addTarget:self action:@selector(closeBtnClick) forControlEvents:UIControlEventTouchUpInside];
    
    _wbSlider = [[UISlider alloc] init];
    _wbSlider.minimumValue = 3000.0f;
    _wbSlider.maximumValue = 12000.0f;
    _wbSlider.value = 6000.0f;
    [_wbSlider addTarget:self action:@selector(wbSliderMethod:) forControlEvents:UIControlEventValueChanged];
    
    _flashBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_flashBtn setTitle:@"开启" forState:UIControlStateNormal];
    [_flashBtn setTitleColor:[UIColor blueColor] forState:UIControlStateNormal];
    [_flashBtn addTarget:self action:@selector(flashBtnClick:) forControlEvents:UIControlEventTouchUpInside];
    
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
    _dotLabel.backgroundColor = RGB(0xffc437);
    
    _leftBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_leftBtn setTitle:@"左眼" forState:UIControlStateNormal];
    [_leftBtn setTitleColor:RGB(0xfefeff) forState:UIControlStateNormal];
    _leftBtn.titleLabel.font = [UIFont systemFontOfSize:15.0];
    [_leftBtn addTarget:self action:@selector(leftBtnClick:) forControlEvents:UIControlEventTouchUpInside];
    _centerBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_centerBtn setTitleColor:RGB(0xffc437) forState:UIControlStateNormal];
    [_centerBtn setTitle:@"左眼" forState:UIControlStateNormal];
    _centerBtn.titleLabel.font = [UIFont systemFontOfSize:15.0];
    _rightBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_rightBtn setTitle:@"右眼" forState:UIControlStateNormal];
    [_rightBtn setTitleColor:RGB(0xfefeff) forState:UIControlStateNormal];
    _rightBtn.titleLabel.font = [UIFont systemFontOfSize:15.0];
    [_rightBtn addTarget:self action:@selector(rightBtnClick:) forControlEvents:UIControlEventTouchUpInside];
    
    _cameraBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [_cameraBtn setImage:[UIImage imageNamed:@"button_camera_screen"] forState:UIControlStateNormal];
    [_cameraBtn addTarget:self action:@selector(cameraBtnClick:) forControlEvents:UIControlEventTouchUpInside];
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

- (void)flashBtnClick:(id)sender{
    UIButton *btn = (UIButton *)sender;
    btn.selected = !btn.selected;
    
    [_captureDevice lockForConfiguration:nil];
    if (btn.isSelected) {
        [_flashBtn setTitle:@"关闭" forState:UIControlStateNormal];
        [_captureDevice setExposureModeCustomWithDuration:CMTimeMakeWithSeconds(0.05, 1000) ISO:40.0 completionHandler:nil];
    }else{
        [_flashBtn setTitle:@"开启" forState:UIControlStateNormal];
        [_captureDevice setExposureModeCustomWithDuration:CMTimeMakeWithSeconds(0.05, 1000) ISO:80.0 completionHandler:nil];
    }
    [_captureDevice unlockForConfiguration];
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
    [_centerBtn setTitleColor:RGB(0xfefeff) forState:UIControlStateNormal];
    _dotLabel.hidden = YES;
    [UIView animateWithDuration:kAnimationDuration animations:^{
        _leftBtn.frame = _centerBtnFrame;
        _centerBtn.frame = _rightBtnFrame;
    } completion:^(BOOL finished) {
        [self ChangeToLeft:YES];
    }];
}
- (void)rightBtnClick:(UIButton *)btn {
    [_centerBtn setTitleColor:RGB(0xfefeff) forState:UIControlStateNormal];
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
    [_centerBtn setTitleColor:RGB(0xffc437) forState:UIControlStateNormal];
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
