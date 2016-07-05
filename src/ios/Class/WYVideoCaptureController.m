//
//  WYVideoCaptureController.m
//  WYAVFoundation
//
//  Created by 王俨 on 15/12/31.
//  Copyright © 2015年 wangyan. All rights reserved.
//

#import "WYVideoCaptureController.h"
#import <MediaPlayer/MediaPlayer.h>
#import <AVFoundation/AVFoundation.h>
#import <AssetsLibrary/AssetsLibrary.h>
#import "UIView+Extension.h"
#import "NSTimer+Addtion.h"
#import "ProgressView.h"
#import "UIView+AutoLayoutViews.h"
#import "TDMediaFileManage.h"
#import "TDPictureModel.h"
#import "ZQBaseClassesExtended.h"
#import "MLSelectPhotoBrowserViewController.h"

typedef void(^PropertyChangeBlock)(AVCaptureDevice *captureDevice);
#define kAnimationDuration 0.2
#define kTimeChangeDuration 0.1
#define kVideoTotalTime 30
#define kVideoLimit 10

@interface WYVideoCaptureController ()<UIGestureRecognizerDelegate>{
    UIBarButtonItem *_leftItem;
    CGRect _leftBtnFrame;
    CGRect _centerBtnFrame;
    CGRect _rightBtnFrame;
    AVPlayer *_player;
    AVPlayerLayer *_playerLayer;
    BOOL _isAppear;
    BOOL _isLeftEye;
    BOOL _isLeftTouchDown;
    BOOL _isRightTouchDown;
    BOOL _isLeftTouchUpInside;
    BOOL _isRightToucUpInside;
    int _leftTakenPictureCount;
    int _rightTakenPictureCount;
}
/** 音量View*/
@property (nonatomic, strong) MPVolumeView *volumeView;
/** 设置音量滚动View*/
@property (nonatomic, strong) UISlider *volumeViewSlider;
/** 音频播放器 */
@property (nonatomic, strong) AVPlayer *player;
@property (nonatomic, strong) UISlider *wbSlider;
@property (nonatomic, strong) UISlider *scaleSlider;
@property (nonatomic, strong) UIView *viewContainer;
@property (nonatomic, strong) ProgressView *progressView;
@property (nonatomic, strong) UIButton *leftBtn;
@property (nonatomic, strong) UIButton *centerBtn;
@property (nonatomic, strong) UIButton *rightBtn;
@property (nonatomic, strong) UIButton *cameraBtn;
@property (nonatomic, strong) UIView *toolView;
@property (strong, nonatomic) UIView *screenFlashView;
@property (nonatomic, strong) UIView *pictureScanView;
@property (nonatomic, strong) UIImageView *pictureScanImgView;
@property (nonatomic, strong) UIButton *pictureScanBtn;
@property (nonatomic, strong) UIButton *ISOBtn;
@property (nonatomic, strong) UIButton *whiteBalanceBtn;
@property (nonatomic, strong) UIView *whiteBalanceView;
@property (nonatomic, strong) UIView *scaleView;

@property (nonatomic, strong) NSMutableArray *takenPicturesArr;
@property (nonatomic, strong) NSMutableArray *leftSelectedPathArr;
@property (nonatomic, strong) NSMutableArray *rightSelectedPathArr;

/// 负责输入和输出设备之间数据传递
@property (nonatomic, strong) AVCaptureSession *captureSession;
@property (nonatomic, strong) AVCaptureDevice *captureDevice;
/// 负责从AVCaptureDevice获取数据
@property (nonatomic, strong) AVCaptureDeviceInput *captureDeviceInput;
/// 照片输出流
@property (nonatomic, strong) AVCaptureStillImageOutput *captureStillImageOutput;
/// 相机拍摄预览层
@property (nonatomic, strong) AVCaptureVideoPreviewLayer *captureVideoPreviewLayer;
/// 聚焦光标
@property (nonatomic, strong) UIImageView *focusCursorImgView;
/// 记录开始的缩放比例
@property(nonatomic,assign)CGFloat beginGestureScale;
/// 最后的缩放比例
@property(nonatomic,assign)CGFloat effectiveScale;

@end

@implementation WYVideoCaptureController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.view.backgroundColor = RGB(0x16161b);
    [self setupUI];
    [self initTakenParameters];
    [self ChangeToLeft:YES];
    [self setupCaptureView];
    [self configureVolumeTool];
    
    if (_isScan) {
        [self pushToPictureScan:NO];
    }else{
        [self cleanOlderData];
    }
}

- (void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    _isAppear = YES;
    [_captureSession startRunning];
    [self configureNavgationBar];
    [self initNavTitle];
    [self addNotifications];
    
    //3.监听点击音量键事件
    [self p_addObserver];
    
    //4.监听打开控制中心
    [self p_addObserverControlCenter];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated{
    [super viewWillDisappear:animated];
    _isAppear = NO;
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    [_captureSession stopRunning];
    
    [self p_removeObserver];
    [self p_removeObserverControlCenter];
}

- (void)dealloc {
    NSLog(@"我是拍照控制器,我被销毁了");
}

- (void)initNavTitle{
    if (_isLeftEye) {
        self.title = [NSString stringWithFormat:@"%d/6",_leftTakenPictureCount<0?0:_leftTakenPictureCount];
    }else{
        self.title = [NSString stringWithFormat:@"%d/6",_rightTakenPictureCount<0?0:_rightTakenPictureCount];
    }
}

- (void)configureNavgationBar{
    //将status bar 文本颜色设置为白色
    self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
    self.navigationController.navigationBar.translucent = YES;
    self.navigationController.navigationBar.barTintColor = RGB(0x000000);
    self.navigationController.navigationBar.tintColor = [UIColor whiteColor];
    [self.navigationController.navigationBar setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[UIColor whiteColor], NSForegroundColorAttributeName, [UIFont boldSystemFontOfSize:18.f], NSFontAttributeName, nil]];
    
    _leftItem = [[UIBarButtonItem alloc] initWithTitle:@"取消"
                                                 style:UIBarButtonItemStylePlain
                                                target:self
                                                action:@selector(leftBarButtonItemAction)];
    self.navigationItem.leftBarButtonItem = _leftItem;
}

- (void)leftBarButtonItemAction{
    if (_leftTakenPictureCount==0 && _rightTakenPictureCount==0) {
        [self dismissViewControllerAnimated:YES completion:^{
            
        }];
        
        return;
    }
    
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"是否放弃当前拍摄图片" message:nil preferredStyle:UIAlertControllerStyleAlert];
    
    // Create the actions.
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:^(UIAlertAction *action) {
    }];
    UIAlertAction *sureAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self dismissViewControllerAnimated:YES completion:^{
            
        }];
    }];
    // Add the actions.
    [alertController addAction:cancelAction];
    [alertController addAction:sureAction];
    [self presentViewController:alertController animated:YES completion:nil];
}

- (void)addNotifications{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(deleteLastPictureMethod:)
                                                 name:@"deleteTheLastPicture"
                                               object:nil];
}

- (void)deleteLastPictureMethod:(NSNotification *)notify{
    if ([_takenPicturesArr isValid]) {
        [_takenPicturesArr removeLastObject];
        if ([_takenPicturesArr isValid]) {
            _pictureScanImgView.image = [_takenPicturesArr lastObject];
        }
    }
}

- (void)cleanOlderData{
    [[TDMediaFileManage shareInstance] deleteAllFiles];
}

- (void)setupCaptureView {
    // 1.初始化会话
    _captureSession = [[AVCaptureSession alloc] init];
    if ([_captureSession canSetSessionPreset:AVCaptureSessionPresetPhoto]) {
        [_captureSession setSessionPreset:AVCaptureSessionPresetPhoto]; // 设置分辨率
    }
    // 2.获得输入设备
    self.captureDevice = [self getCameraDeviceWithPosition:AVCaptureDevicePositionBack];
    [self wbSliderValueChanged:_wbSlider];
    if (_captureDevice == nil) {
        NSLog(@"获取输入设备失败");
        return;
    }
    // 4.根据输入设备初始化设备输入对象,用于获得输入数据
    NSError *error = nil;
    _captureDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:_captureDevice error:&error];
    if (error) {
        NSLog(@"创建设备输入对象失败 -- error = %@", error);
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"未获得相机权限，请到设置中授权后再尝试。" message:nil preferredStyle:UIAlertControllerStyleAlert];
        
        // Create the actions.
        UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleCancel handler:^(UIAlertAction *action) {
            [self dismissViewControllerAnimated:YES completion:^{
                
            }];
        }];
        // Add the actions.
        [alertController addAction:cancelAction];
        [self presentViewController:alertController animated:YES completion:nil];
        
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

#pragma mark - configureVolumeTool
- (void)configureVolumeTool{
    //1.获取音量监听视图
    [self p_getVolumeView];
    
    //2.隐藏音量Icon
    [self p_hiddIcon];
    
    //5.获取当前系统音量
    [self p_getSystemVolume];
    
    //6.耳机中间键远程遥控需要播放一段音频激活，否则无法使用
    [self.player play];
    
    //7.开始接收远程遥控事件,耳机中间键
    [[UIApplication sharedApplication] beginReceivingRemoteControlEvents];
}

/**
 *  获取音量监听视图
 */
- (void)p_getVolumeView {
    self.volumeView = [[MPVolumeView alloc] init];
    for (UIView *view in [self.volumeView subviews]){
        if ([view.class.description isEqualToString:@"MPVolumeSlider"]){
            self.volumeViewSlider = (UISlider*)view;
            break;
        }
    }
}

/**
 *  隐藏音量Icon
 */
- (void)p_hiddIcon {
    self.volumeView.frame = CGRectMake(-1000, -100, 100, 100);
    self.volumeView.hidden = NO;
    [self.view  addSubview:self.volumeView];
}

/**
 *  监听点击音量键事件
 */
- (void)p_addObserver {
    NSError *error;
    [[AVAudioSession sharedInstance] setActive:YES error:&error];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(volumeChanged:) name:@"AVSystemController_SystemVolumeDidChangeNotification" object:nil];
}

/**
 *  取消监听点击音量键事件
 */
- (void)p_removeObserver {
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"AVSystemController_SystemVolumeDidChangeNotification" object:nil];
    //结束远程遥控
    [[UIApplication sharedApplication] endReceivingRemoteControlEvents];
}

/**
 *  应用程序失效或者再次进入前台，会走以下两个通知
 */
- (void)p_addObserverControlCenter {
    //应用程序将要进入后台之前
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(willResignActive) name:UIApplicationWillResignActiveNotification object:nil];
    //应用程序切回到前台
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didBecomeActive) name:UIApplicationDidBecomeActiveNotification object:nil];
}

- (void)p_removeObserverControlCenter {
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationWillResignActiveNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationDidBecomeActiveNotification object:nil];
}

/**
 *  APP挂起时，取消对音量键的监听
 */
- (void)willResignActive {
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"AVSystemController_SystemVolumeDidChangeNotification" object:nil];
    
    //结束远程遥控
    [[UIApplication sharedApplication] endReceivingRemoteControlEvents];
}

/**
 *  重新进去前台
 */
- (void)didBecomeActive {
    //重新监听音量改变事件
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(volumeChanged:) name:@"AVSystemController_SystemVolumeDidChangeNotification" object:nil];
    
    //进入前台后，重新设置player状态为播放
    [self.player play];
    
    //开始远程遥控
    [[UIApplication sharedApplication] beginReceivingRemoteControlEvents];
}

/**
 *  获取系统音量
 */
- (void)p_getSystemVolume {
}

#pragma mark - Lazy
/**
 *  加载音频播放文件
 */
- (AVPlayer *)player {
    if (_player == nil) {
        NSString *filePath = [[NSBundle mainBundle] pathForResource:@"silent.m4a" ofType:nil];
        NSURL *url = [NSURL fileURLWithPath:filePath];
        _player = [[AVPlayer alloc] initWithURL:url];
        [_player setVolume:0.0];
    }
    return _player;
}

//受registerQYService方法的影响app启动后第一次进入此界面或退到后台在进入前台会自动拍摄一张图片
- (void)volumeChanged:(NSNotification *)notification {
    NSDictionary *infoDic = notification.userInfo;
    NSString *singValue = [infoDic objectForKey:@"AVSystemController_AudioVolumeChangeReasonNotificationParameter"];
    if ([singValue isEqualToString:@"ExplicitVolumeChange"]) {
        [self cameraBtnTouchUpInside:nil];
    }
}

#pragma mark - UI设计
- (void)setupUI {
    [self prepareUI];
    
    [self.view addSubview:_viewContainer];
    [_viewContainer addSubview:self.focusCursorImgView];
    [self.view addSubview:self.whiteBalanceView];
    [self.view addSubview:self.scaleView];
    [self.view addSubview:self.wbSlider];
    [self.view addSubview:self.scaleSlider];
    [self.view addSubview:self.toolView];
    [self.view addSubview:self.ISOBtn];
    [self.view addSubview:self.whiteBalanceBtn];
    
    [self.view addSubview:_leftBtn];
    [self.view addSubview:_centerBtn];
    [self.view addSubview:_rightBtn];
    [self.view addSubview:_cameraBtn];
    [self.view addSubview:self.pictureScanView];
    [self.view addSubview:self.screenFlashView];
    
    CGFloat viewContainerHeight = APP_HEIGHT-64-CGRectGetHeight(self.toolView.bounds);
    _viewContainer.frame = CGRectMake(0, 64, APP_WIDTH, viewContainerHeight);
    _progressView.frame = CGRectMake(0, CGRectGetMaxY(_viewContainer.frame), APP_WIDTH, 5);
    CGFloat btnW = 40;
    CGFloat leftBtnX = (APP_WIDTH - 3 * btnW - 2 * 20) *0.5;
    CGFloat leftBtnY = APP_HEIGHT-62-btnW-15;
    
    _leftBtnFrame = CGRectMake(leftBtnX, leftBtnY, btnW, btnW);
    _centerBtnFrame = CGRectOffset(_leftBtnFrame, 20 + btnW, 0);
    _rightBtnFrame = CGRectOffset(_centerBtnFrame, 20 + btnW, 0);
    [self restoreBtn];
    _cameraBtn.frame = CGRectMake((APP_WIDTH - 67) * 0.5, APP_HEIGHT-62-15, 62, 62);
}
- (void)prepareUI {
    _viewContainer = [[UIView alloc] init];
    //添加滑动手势
    UISwipeGestureRecognizer *leftSwipGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipes:)];
    leftSwipGestureRecognizer.direction = UISwipeGestureRecognizerDirectionLeft;
    [_viewContainer addGestureRecognizer:leftSwipGestureRecognizer];
    
    UISwipeGestureRecognizer *rightSwipGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipes:)];
    rightSwipGestureRecognizer.direction = UISwipeGestureRecognizerDirectionRight;
    [_viewContainer addGestureRecognizer:rightSwipGestureRecognizer];
    
    //添加缩放手势
    UIPinchGestureRecognizer *pinch = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(handlePinchGesture:)];
    pinch.delegate = self;
    [_viewContainer addGestureRecognizer:pinch];
    
    //添加点按手势，点按时聚焦
    UITapGestureRecognizer *tapGesture=[[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(tapScreenGesture:)];
    [_viewContainer addGestureRecognizer:tapGesture];
    
    _progressView = [[ProgressView alloc] initWithFrame:CGRectMake(0, APP_WIDTH + 44, APP_WIDTH, 5)];
    _progressView.totalTime = kVideoTotalTime;
    
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
    [_cameraBtn addTarget:self action:@selector(cameraBtnTouchUpInside:) forControlEvents:UIControlEventTouchUpInside];
    [_cameraBtn addTarget:self action:@selector(cameraBtnTouchDown:) forControlEvents:UIControlEventTouchDown];
}

#pragma mark - View
- (UIButton *)ISOBtn{
    if (!_ISOBtn) {
        UIImage *ISOImg = [UIImage imageNamed:@"ISOicon"];
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

- (UIView *)screenFlashView{
    if (!_screenFlashView) {
        _screenFlashView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, APP_WIDTH, APP_HEIGHT-100.0f)];
        _screenFlashView.backgroundColor = [UIColor blackColor];
        [_screenFlashView setAlpha:0];
    }
    return _screenFlashView;
}

- (void)begainScreenFlashAnimation{
    [self.screenFlashView setAlpha:1];
    [UIView animateWithDuration:0.3f animations:^{
        [self.screenFlashView setAlpha:0.0f];
    }];
}

- (UIView *)pictureScanView{
    if (!_pictureScanView) {
        CGFloat scanViewWidth = 110.0f/2.0f;
        CGFloat scanViewHeight = 110.0f/2.0f;
        CGFloat scanViewOriginX = 10.0f;
        CGFloat scanViewOriginY = APP_HEIGHT-scanViewHeight-20.0f;
        _pictureScanView = [[UIView alloc] initWithFrame:CGRectMake(scanViewOriginX, scanViewOriginY, scanViewWidth, scanViewHeight)];
        _pictureScanView.hidden = YES;
        [_pictureScanView addSubview:self.pictureScanImgView];
        [_pictureScanView addSubview:self.pictureScanBtn];
    }
    return _pictureScanView;
}

- (UIImageView *)pictureScanImgView{
    if (!_pictureScanImgView) {
        CGFloat scanImgViewWidth = 110.0f/2.0f;
        CGFloat scanImgViewHeight = 110.0f/2.0f;
        CGFloat scanImgViewOriginX = 0.0f;
        CGFloat scanImgViewOriginY = 0.0f;
        _pictureScanImgView = [[UIImageView alloc] initWithFrame:CGRectMake(scanImgViewOriginX, scanImgViewOriginY, scanImgViewWidth, scanImgViewHeight)];
    }
    return _pictureScanImgView;
}

- (UIButton *)pictureScanBtn{
    if (!_pictureScanBtn) {
        CGFloat psWidth = 110.0f/2.0f;
        CGFloat psHeight = 110.0f/2.0f;
        CGFloat psOriginX = 0.0f;
        CGFloat psOriginY = 0.0f;
        _pictureScanBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        _pictureScanBtn.frame = CGRectMake(psOriginX, psOriginY, psWidth, psHeight);
        [_pictureScanBtn addTarget:self
                            action:@selector(pictureScanBtnClick:)
                  forControlEvents:UIControlEventTouchUpInside];
    }
    return _pictureScanBtn;
}
- (void)pictureScanBtnClick:(id)sender{
    [self pushToPictureScan:YES];
}

- (UIImageView *)focusCursorImgView{
    if (!_focusCursorImgView) {
        UIImage *focusImg = [UIImage imageNamed:@"iconfont-fingerprintwithcrosshairfocus"];
        _focusCursorImgView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, focusImg.size.width, focusImg.size.height)];
        _focusCursorImgView.alpha = 0.0f;
        _focusCursorImgView.image = focusImg;
    }
    return _focusCursorImgView;
}

- (UIView *)toolView{
    if (!_toolView) {
        CGFloat toolWidth = APP_WIDTH;
        CGFloat toolHeight = 115.0f;
        CGFloat toolOriginX = 0.0f;
        CGFloat toolOriginY = APP_HEIGHT-toolHeight;
        _toolView = [[UIView alloc] initWithFrame:CGRectMake(toolOriginX, toolOriginY, toolWidth, toolHeight)];
        _toolView.backgroundColor = RGB(0x000000);
    }
    return _toolView;
}

- (UIView *)whiteBalanceView{
    if (!_whiteBalanceView) {
        CGFloat width = 553.0f/2.0f;
        CGFloat height = 70.0f/2.0f;
        CGFloat originX = (APP_WIDTH-width)/2.0f;
        CGFloat originY = APP_HEIGHT - (200.0f+62.0f+44.0f+30.0f)/2.0f;
        _whiteBalanceView = [[UIView alloc] initWithFrame:CGRectMake(originX, originY, width, height)];
        _whiteBalanceView.hidden = YES;
        _whiteBalanceView.backgroundColor = RGB(0x000000);
        _whiteBalanceView.alpha = 0.8f;
        _whiteBalanceView.layer.cornerRadius = 5.0f;
        _whiteBalanceView.layer.masksToBounds = YES;
    }
    return _whiteBalanceView;
}

- (UIView *)scaleView{
    if (!_scaleView) {
        CGFloat width = 553.0f/2.0f;
        CGFloat height = 70.0f/2.0f;
        CGFloat originX = (APP_WIDTH-width)/2.0f;
        CGFloat originY = APP_HEIGHT - (200.0f+62.0f+44.0f+30.0f)/2.0f;
        _scaleView = [[UIView alloc] initWithFrame:CGRectMake(originX, originY, width, height)];
        _scaleView.hidden = YES;
        _scaleView.backgroundColor = RGB(0x000000);
        _scaleView.alpha = 0.8f;
        _scaleView.layer.cornerRadius = 5.0f;
        _scaleView.layer.masksToBounds = YES;
    }
    return _scaleView;
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
        [_wbSlider addTarget:self
                      action:@selector(wbSliderValueChanged:)
            forControlEvents:UIControlEventValueChanged];
        [_wbSlider addTarget:self
                      action:@selector(wbSliderTouchUpInside:)
            forControlEvents:UIControlEventTouchUpInside];
    }
    return _wbSlider;
}

- (UISlider *)scaleSlider{
    if (!_scaleSlider) {
        UIImage *leftImg = [UIImage imageNamed:@"educe"];
        UIImage *rightImg = [UIImage imageNamed:@"plus"];
        
        CGFloat sliderWidth = CGRectGetWidth(_whiteBalanceView.bounds) - (22.0f+22.0f)/2.0f;
        CGFloat sliderHeight = 31.0f;
        CGFloat sliderOriginX = CGRectGetMinX(_whiteBalanceView.frame) + 22.0f/2.0f;
        CGFloat sliderOriginY = CGRectGetMinY(_whiteBalanceView.frame) +((CGRectGetHeight(_whiteBalanceView.bounds)-sliderHeight)/2.0f);
        _scaleSlider = [[UISlider alloc] initWithFrame:CGRectMake(sliderOriginX, sliderOriginY, sliderWidth, sliderHeight)];
        _scaleSlider.hidden = YES;
        _scaleSlider.minimumValue = 1.0f;
        _scaleSlider.maximumValue = 5.0f;
        _scaleSlider.value = 1.0f;
        _scaleSlider.minimumValueImage = leftImg;
        _scaleSlider.maximumValueImage = rightImg;
        [_scaleSlider addTarget:self
                         action:@selector(scaleSliderValueChanged:)
               forControlEvents:UIControlEventValueChanged];
        [_scaleSlider addTarget:self
                         action:@selector(scaleSliderTouchUpInside:)
               forControlEvents:UIControlEventTouchUpInside];
    }
    return _scaleSlider;
}

#pragma mark - ButtonClick
- (void)pushToPictureScan:(BOOL)animated{
    self.title = @"返回";
    
    int leftSelectedCount = 0;
    int rightSelectedCount = 0;
    NSMutableArray *selectedModelArr = [[NSMutableArray alloc] initWithCapacity:0];
    NSMutableArray *leftSelectedArr = [[NSMutableArray alloc] initWithCapacity:0];
    NSMutableArray *rightSelectedArr = [[NSMutableArray alloc] initWithCapacity:0];
    
    MLSelectPhotoBrowserViewController *browserVc = [[MLSelectPhotoBrowserViewController alloc] init];
    [browserVc setValue:@(NO) forKeyPath:@"isTrashing"];
    browserVc.isModelData = YES;
    browserVc.currentPage = 0;
    
    NSMutableArray *leftEyeDataArr = [[NSMutableArray alloc] initWithCapacity:0];
    NSMutableArray *rightEyeDataArr = [[NSMutableArray alloc] initWithCapacity:0];
    NSMutableArray *tempMutableArr = [[NSMutableArray alloc] initWithCapacity:0];
    
    NSString *leftFilePath = [[TDMediaFileManage shareInstance] getJRMediaPathWithType:YES];
    NSError *le = nil;
    NSArray *leftFileArr = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:leftFilePath error:&le];
    NSLog(@"leftFileArr:%@",leftFileArr);
    NSString *rightFilePath = [[TDMediaFileManage shareInstance] getJRMediaPathWithType:NO];
    NSError *re = nil;
    NSArray *rightFileArr = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:rightFilePath error:&re];
    NSLog(@"rightFileArr:%@",rightFileArr);
    
    if ([leftFileArr isValid]) {
        //左眼
        for (NSString *fileName in leftFileArr) {
            TDPictureModel *picture = [[TDPictureModel alloc] init];
            picture.pictureName = fileName;
            if ([_leftSelectedPathArr containsObject:fileName]) {
                picture.isSelected = YES;
                leftSelectedCount++;
                NSString *imgPath = [self getImagePathWithImageName:fileName isLeftEye:YES];
                [leftSelectedArr addObject:imgPath];
                [selectedModelArr addObject:picture];
            }else{
                picture.isSelected = NO;
            }
            [leftEyeDataArr addObject:picture];
        }
        
        [tempMutableArr addObjectsFromArray:leftEyeDataArr];
        browserVc.leftCount = leftFileArr.count;
        
        if ([rightFileArr isValid]) {
            //右眼
            for (NSString *fileName in rightFileArr) {
                TDPictureModel *picture = [[TDPictureModel alloc] init];
                picture.pictureName = fileName;
                if ([_rightSelectedPathArr containsObject:fileName]) {
                    picture.isSelected = YES;
                    rightSelectedCount++;
                    NSString *imgPath = [self getImagePathWithImageName:fileName isLeftEye:NO];
                    [rightSelectedArr addObject:imgPath];
                    [selectedModelArr addObject:picture];
                }else{
                    picture.isSelected = NO;
                }
                [rightEyeDataArr addObject:picture];
            }
            
            [tempMutableArr addObjectsFromArray:rightEyeDataArr];
            browserVc.rightCount = rightFileArr.count;
        }else{
            browserVc.rightCount = 0;
        }
    }else{
        browserVc.leftCount = 0;
        
        if ([rightFileArr isValid]) {
            //右眼
            for (NSString *fileName in rightFileArr) {
                TDPictureModel *picture = [[TDPictureModel alloc] init];
                picture.pictureName = fileName;
                if ([_rightSelectedPathArr containsObject:fileName]) {
                    picture.isSelected = YES;
                    rightSelectedCount++;
                    NSString *imgPath = [self getImagePathWithImageName:fileName isLeftEye:NO];
                    [rightSelectedArr addObject:imgPath];
                    [selectedModelArr addObject:picture];
                }else{
                    picture.isSelected = NO;
                }
                [rightEyeDataArr addObject:picture];
            }
            
            [tempMutableArr addObjectsFromArray:rightEyeDataArr];
            browserVc.rightCount = rightFileArr.count;
        }else{
            browserVc.rightCount = 0;
        }
    }
    
    browserVc.leftSelectedPathArr = self.leftSelectedPathArr;
    browserVc.rightSelectedPathArr = self.rightSelectedPathArr;
    browserVc.selectedModelArr = selectedModelArr;
    browserVc.leftSelectedCount = leftSelectedCount;
    browserVc.rightSelectedCount = rightSelectedCount;
    browserVc.photos = [NSArray arrayWithArray:tempMutableArr];
    browserVc.mlLeftselectedArr = leftSelectedArr;
    browserVc.mlRightselectedArr = rightSelectedArr;
    browserVc.deleteCallBack = ^(NSArray *assets,NSString *eyeType){
        if ([eyeType isEqualToString:@"left"]) {
            _leftTakenPictureCount--;
        }else{
            _rightTakenPictureCount--;
        }
        if (_leftTakenPictureCount==0 && _rightTakenPictureCount==0) {
            _pictureScanView.hidden = YES;
        }
    };
    [self.navigationController pushViewController:browserVc animated:animated];
}

- (NSString *)getImagePathWithImageName:(NSString *)name isLeftEye:(BOOL)isLeft{
    NSString *imgPath = [[TDMediaFileManage shareInstance] getImagePathWithPictureName:name isLeftEye:isLeft];
    return imgPath;
}

- (void)wbSliderValueChanged:(id)sender{
    [NSObject cancelPreviousPerformRequestsWithTarget:self
                                             selector:@selector(hideWhiteBalanceView:)
                                               object:_whiteBalanceBtn];
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

- (void)wbSliderTouchUpInside:(id)sender{
    [self performSelector:@selector(hideWhiteBalanceView:)
               withObject:_whiteBalanceBtn
               afterDelay:5.0f];
}

- (void)scaleSliderValueChanged:(id)sender{
    [NSObject cancelPreviousPerformRequestsWithTarget:self
                                             selector:@selector(hideScaleView)
                                               object:nil];
    UISlider *slider = (UISlider *)sender;
    self.effectiveScale = slider.value;
    
    [CATransaction begin];
    [CATransaction setAnimationDuration:.025];
    [self.captureVideoPreviewLayer setAffineTransform:CGAffineTransformMakeScale(self.effectiveScale, self.effectiveScale)];
    [CATransaction commit];
}

- (void)scaleSliderTouchUpInside:(id)sender{
    [self performSelector:@selector(hideScaleView) withObject:nil afterDelay:5.0f];
}

- (void)ISOBtnClick:(id)sender{
    UIButton *btn = (UIButton *)sender;
    btn.selected = !btn.selected;
    
    UIImage *ISOImg = [UIImage imageNamed:@"ISOicon"];
    UIImage *ISOClickImg = [UIImage imageNamed:@"ISOclickicon"];
    
    [_captureDevice lockForConfiguration:nil];
    if (btn.isSelected) {
        [_ISOBtn setBackgroundImage:ISOClickImg forState:UIControlStateNormal];
        [_captureDevice setExposureModeCustomWithDuration:CMTimeMakeWithSeconds(0.05, 1000) ISO:40.0 completionHandler:nil];
    }else{
        [_ISOBtn setBackgroundImage:ISOImg forState:UIControlStateNormal];
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
        [self performSelector:@selector(hideWhiteBalanceView:)
                   withObject:btn
                   afterDelay:5.0f];
        
        if (!_scaleView.hidden) {
            _scaleView.hidden = YES;
            _scaleSlider.hidden = YES;
            [NSObject cancelPreviousPerformRequestsWithTarget:self
                                                     selector:@selector(hideScaleView)
                                                       object:nil];
        }
    }else{
        [_whiteBalanceBtn setBackgroundImage:whiteBalanceImg
                                    forState:UIControlStateNormal];
        [NSObject cancelPreviousPerformRequestsWithTarget:self
                                                 selector:@selector(hideWhiteBalanceView:)
                                                   object:btn];
    }
}

- (void)hideWhiteBalanceView:(UIButton *)btn{
    btn.selected = !btn.isSelected;
    _whiteBalanceView.hidden = !_whiteBalanceView.hidden;
    _wbSlider.hidden = !_wbSlider.hidden;
    UIImage *whiteBalanceImg = [UIImage imageNamed:@"white-balance-icon"];
    [_whiteBalanceBtn setBackgroundImage:whiteBalanceImg
                                forState:UIControlStateNormal];
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

//缩放手势 用于调整焦距
- (void)handlePinchGesture:(UIPinchGestureRecognizer *)recognizer{
    BOOL allTouchesAreOnThePreviewLayer = YES;
    NSUInteger numTouches = [recognizer numberOfTouches], i;
    for ( i = 0; i < numTouches; ++i ) {
        CGPoint location = [recognizer locationOfTouch:i inView:self.viewContainer];
        CGPoint convertedLocation = [self.captureVideoPreviewLayer convertPoint:location
                                                                      fromLayer:self.captureVideoPreviewLayer.superlayer];
        if (![self.captureVideoPreviewLayer containsPoint:convertedLocation] ) {
            allTouchesAreOnThePreviewLayer = NO;
            break;
        }
    }
    
    if ( allTouchesAreOnThePreviewLayer ) {
        self.effectiveScale = self.beginGestureScale * recognizer.scale;
        if (self.effectiveScale < 1.0){
            self.effectiveScale = 1.0;
        }
        
        CGFloat sysMaxScaleAndCropFactor = [[self.captureStillImageOutput connectionWithMediaType:AVMediaTypeVideo] videoMaxScaleAndCropFactor];
        
        CGFloat maxScaleAndCropFactor = 5.0f<sysMaxScaleAndCropFactor?5.0f:sysMaxScaleAndCropFactor;
        
        if (self.effectiveScale > maxScaleAndCropFactor)
            self.effectiveScale = maxScaleAndCropFactor;
        
        [CATransaction begin];
        [CATransaction setAnimationDuration:.025];
        [self.captureVideoPreviewLayer setAffineTransform:CGAffineTransformMakeScale(self.effectiveScale, self.effectiveScale)];
        [self.captureDevice lockForConfiguration:nil];
        self.captureDevice.videoZoomFactor = self.effectiveScale;
        [self.captureDevice unlockForConfiguration];
        [CATransaction commit];
    }
    
    if (!_whiteBalanceView.hidden) {
        [self whiteBalanceBtnClick:_whiteBalanceBtn];
    }
    
    if (_scaleView.hidden) {
        _scaleView.hidden = NO;
        _scaleSlider.hidden = NO;
    }
    
    _scaleSlider.value = self.effectiveScale;
    
    if (recognizer.state == UIGestureRecognizerStateEnded) {
        [self performSelector:@selector(hideScaleView) withObject:nil afterDelay:5.0f];
    }
}

- (void)hideScaleView{
    _scaleView.hidden = YES;
    _scaleSlider.hidden = YES;
}

#pragma mark gestureRecognizer delegate
- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer{
    if ([gestureRecognizer isKindOfClass:[UIPinchGestureRecognizer class]]) {
        self.beginGestureScale = self.effectiveScale;
    }
    return YES;
}

-(void)tapScreenGesture:(UITapGestureRecognizer *)tapGesture{
    CGPoint point= [tapGesture locationInView:self.viewContainer];
    //将UI坐标转化为摄像头坐标
    CGPoint cameraPoint= [self.captureVideoPreviewLayer captureDevicePointOfInterestForPoint:point];
    [self setFocusCursorWithPoint:point];
    [self focusWithMode:AVCaptureFocusModeContinuousAutoFocus
           exposureMode:AVCaptureExposureModeContinuousAutoExposure
                atPoint:cameraPoint];
    [self focusWithMode:AVCaptureFocusModeContinuousAutoFocus
           exposureMode:AVCaptureExposureModeContinuousAutoExposure
                atPoint:cameraPoint];
}

/**
 *  设置聚焦光标位置
 *
 *  @param point 光标位置
 */
-(void)setFocusCursorWithPoint:(CGPoint)point{
    self.focusCursorImgView.center=point;
    self.focusCursorImgView.transform=CGAffineTransformMakeScale(1.2, 1.2);
    self.focusCursorImgView.alpha=1.0;
    [UIView animateWithDuration:1.0 animations:^{
        self.focusCursorImgView.transform=CGAffineTransformIdentity;
    } completion:^(BOOL finished) {
        self.focusCursorImgView.alpha=0;
        
    }];
}

/**
 *  设置聚焦点
 *
 *  @param point 聚焦点
 */
-(void)focusWithMode:(AVCaptureFocusMode)focusMode exposureMode:(AVCaptureExposureMode)exposureMode atPoint:(CGPoint)point{
    [self changeDeviceProperty:^(AVCaptureDevice *captureDevice) {
        if ([captureDevice isFocusModeSupported:focusMode]) {
            [captureDevice setFocusMode:focusMode];
        }
        if ([captureDevice isFocusPointOfInterestSupported]) {
            [captureDevice setFocusPointOfInterest:point];
        }
        if ([captureDevice isExposureModeSupported:exposureMode]) {
            [captureDevice setExposureMode:exposureMode];
        }
        if ([captureDevice isExposurePointOfInterestSupported]) {
            [captureDevice setExposurePointOfInterest:point];
        }
    }];
}

- (void)leftBtnClick:(UIButton *)btn {
    [UIView animateWithDuration:kAnimationDuration animations:^{
        _leftBtn.frame = _centerBtnFrame;
        _centerBtn.frame = _rightBtnFrame;
    } completion:^(BOOL finished) {
        [self ChangeToLeft:YES];
    }];
}
- (void)rightBtnClick:(UIButton *)btn {
    [UIView animateWithDuration:kAnimationDuration animations:^{
        _rightBtn.frame = _centerBtnFrame;
        _centerBtn.frame = _leftBtnFrame;
    } completion:^(BOOL finished) {
        [self ChangeToLeft:NO];
    }];
}
- (void)cameraBtnTouchUpInside:(UIButton *)btn {
    if (_isLeftEye) {
        if (_isLeftTouchDown) {
            _isLeftTouchDown = NO;
        }else{
            if (_leftTakenPictureCount == 6) {
                [self showBeyondLimitTakenCount];
            }else{
                _isLeftTouchUpInside = YES;
                [self takePictureMethod];
            }
        }
    }else{
        if (_isRightTouchDown) {
            _isRightTouchDown = NO;
        }else{
            if (_rightTakenPictureCount == 6) {
                [self showBeyondLimitTakenCount];
            }else{
                _isRightToucUpInside = YES;
                [self takePictureMethod];
            }
        }
    }
    [NSObject cancelPreviousPerformRequestsWithTarget:self
                                             selector:@selector(cameraBtnTouchDownMethod)
                                               object:nil];
}

- (void)cameraBtnTouchDown:(UIButton *)btn{
    [self performSelector:@selector(cameraBtnTouchDownMethod) withObject:nil afterDelay:0.5f];
}

- (void)cameraBtnTouchDownMethod{
    if (_isLeftEye) {
        if (_leftTakenPictureCount == 6) {
            [self showBeyondLimitTakenCount];
        }else{
            _isLeftTouchDown = YES;
            [self takePictureMethod];
        }
    }else{
        if (_rightTakenPictureCount == 6) {
            [self showBeyondLimitTakenCount];
        }else{
            _isRightTouchDown = YES;
            [self takePictureMethod];
        }
    }
}

- (void)takePictureMethod{
    if([_captureDevice isAdjustingFocus]){
        [_captureDevice addObserver:self forKeyPath:@"adjustingFocus" options:NSKeyValueObservingOptionNew context:nil];
    }else{
        [self takePictureMethodCore];
    }
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary<NSString *,id> *)change context:(void *)context{
    if([keyPath isEqualToString:@"adjustingFocus"]){
        BOOL adjustingFocus = [ [change objectForKey:NSKeyValueChangeNewKey] isEqualToNumber:[NSNumber numberWithInt:1]];
        if(!adjustingFocus){
            [_captureDevice removeObserver:self forKeyPath:@"adjustingFocus"];
            [self takePictureMethodCore];
        }
    }
}

- (void)takePictureMethodCore{
    if (!_isAppear) {
        return;
    }
    if (!_cameraBtn.userInteractionEnabled) {
        return;
    }
    if (_isLeftEye) {
        _leftTakenPictureCount++;
    }else{
        _rightTakenPictureCount++;
    }
    
    _cameraBtn.userInteractionEnabled = NO;
    _pictureScanBtn.userInteractionEnabled = NO;
    
    __weak WYVideoCaptureController *wself = self;
    // 1.根据设备输出获得链接
    AVCaptureConnection *captureConnection = [_captureStillImageOutput connectionWithMediaType:AVMediaTypeVideo];
    [captureConnection setVideoScaleAndCropFactor:self.effectiveScale];
    // 2.根据链接取得设备输出的数据
    [_captureStillImageOutput captureStillImageAsynchronouslyFromConnection:captureConnection completionHandler:^(CMSampleBufferRef imageDataSampleBuffer, NSError *error) {
        NSData *imageData = [AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:imageDataSampleBuffer];
        [wself saveTakenPictureData:imageData];
    }];
}

- (void)showBeyondLimitTakenCount{
    //__weak WYVideoCaptureController *wself = self;
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"单侧眼睛最多拍摄六张图片,是否重拍?" message:nil preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *sureAction = [UIAlertAction actionWithTitle:@"重拍" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        if (_isLeftEye) {
            _leftTakenPictureCount=0;
        }else{
            _rightTakenPictureCount=0;
        }
        self.title = @"0/6";
        if (_leftTakenPictureCount==0 && _rightTakenPictureCount==0) {
            _pictureScanView.hidden = YES;
        }
        [[TDMediaFileManage shareInstance] deleteFileWithEyeType:_isLeftEye];
        //[wself takePictureMethod];
    }];
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:^(UIAlertAction *action) {
        dispatch_async(dispatch_get_main_queue(), ^{
            // 更新界面
            //[wself pushToPictureScan:YES];
        });
    }];
    // Add the actions.
    [alertController addAction:cancelAction];
    [alertController addAction:sureAction];
    [self presentViewController:alertController animated:YES completion:nil];
}

- (void)saveTakenPictureData:(NSData *)imgData{
    [self begainScreenFlashAnimation];
    [self initNavTitle];
    if (_pictureScanView.hidden) {
        _pictureScanView.hidden = NO;
    }
    
    UIImage *image = [UIImage imageWithData:imgData];
    UIImage *saveImg = [self cropImage:image withCropSize:self.viewContainer.size];
    _pictureScanImgView.image = saveImg;
    [_takenPicturesArr addObject:saveImg];
    NSData *saveImgData = UIImageJPEGRepresentation(saveImg, 1.0f);
    
    TDMediaFileManage *fileManage = [TDMediaFileManage shareInstance];
    NSString *filePath = [fileManage getJRMediaPathWithType:_isLeftEye];
    NSString *imageName;
    if (_isLeftEye) {
        imageName = [NSString stringWithFormat:@"%02d.jpg",_leftTakenPictureCount];
    }else{
        imageName = [NSString stringWithFormat:@"%02d.jpg",_rightTakenPictureCount];
    }
    NSString *imgPath = [NSString stringWithFormat:@"%@/%@",filePath,imageName];
    BOOL result = [fileManage saveFileWithPath:imgPath fileData:saveImgData];
    NSLog(@"result:%d",result);
    
    _cameraBtn.userInteractionEnabled = YES;
    _pictureScanBtn.userInteractionEnabled = YES;
    
    if (_isLeftEye) {
        if (_isLeftTouchDown) {
            if (_leftTakenPictureCount==6) {
                //_isLeftTouchDown = NO;
                [self showBeyondLimitTakenCount];
            }else{
                [self performSelector:@selector(takePictureMethod) withObject:nil afterDelay:0.2f];
            }
        }else{
        }
    }else{
        if (_isRightTouchDown) {
            if (_rightTakenPictureCount==6) {
                //_isRightTouchDown = NO;
                [self showBeyondLimitTakenCount];
            }else{
                [self performSelector:@selector(takePictureMethod) withObject:nil afterDelay:0.2f];
            }
        }else{
        }
    }
}

#pragma mark - private
- (void)restoreBtn {
    _leftBtn.frame = _leftBtnFrame;
    _centerBtn.frame = _centerBtnFrame;
    _rightBtn.frame = _rightBtnFrame;
    [_centerBtn setTitleColor:RGB(0x76c000) forState:UIControlStateNormal];
}

- (void)initTakenParameters{
    _isLeftTouchDown = NO;
    _isRightTouchDown = NO;
    _isLeftTouchUpInside = NO;
    _isRightToucUpInside = NO;
    _leftTakenPictureCount = 0;
    _rightTakenPictureCount = 0;
    self.effectiveScale = 1.0f;
    self.beginGestureScale = 1.0f;
    self.takenPicturesArr = [[NSMutableArray alloc] initWithCapacity:0];
    self.leftSelectedPathArr = [[NSMutableArray alloc] initWithCapacity:0];
    self.rightSelectedPathArr = [[NSMutableArray alloc] initWithCapacity:0];
}
/// 切换拍照和视频录制
///
/// @param isPhoto YES->拍照  NO->视频录制
- (void)ChangeToLeft:(BOOL)isLeft{
    [self restoreBtn];
    _isLeftEye = isLeft;
    [self initNavTitle];
    NSString *centerTitle = isLeft ? @"左眼" : @"右眼";
    [_centerBtn setTitle:centerTitle forState:UIControlStateNormal];
    _leftBtn.hidden = isLeft;
    _rightBtn.hidden = !isLeft;
}

#pragma mark 裁剪照片尺寸
- (UIImage *)cropImage:(UIImage *)image withCropSize:(CGSize)cropSize{
    UIImage *newImage = nil;
    
    CGSize imageSize = image.size;
    CGFloat width = imageSize.width;
    CGFloat height = imageSize.height;
    
    CGFloat targetWidth = cropSize.width;
    CGFloat targetHeight = cropSize.height;
    
    CGFloat scaleFactor = 0;
    CGFloat scaledWidth = targetWidth;
    CGFloat scaledHeight = targetHeight;
    
    CGPoint thumbnailPoint = CGPointMake(0, 0);
    
    if (CGSizeEqualToSize(imageSize, cropSize) == NO) {
        CGFloat widthFactor = targetWidth / width;
        CGFloat heightFactor = targetHeight / height;
        
        if (widthFactor > heightFactor) {
            scaleFactor = widthFactor;
        } else {
            scaleFactor = heightFactor;
        }
        
        scaledWidth  = width * scaleFactor;
        scaledHeight = height * scaleFactor;
        
        
        if (widthFactor > heightFactor) {
            thumbnailPoint.y = (targetHeight - scaledHeight) * .5f;
        } else {
            if (widthFactor < heightFactor) {
                thumbnailPoint.x = (targetWidth - scaledWidth) * .5f;
            }
        }
    }
    
    UIGraphicsBeginImageContextWithOptions(cropSize, YES, 0);
    
    CGRect thumbnailRect = CGRectZero;
    thumbnailRect.origin = thumbnailPoint;
    thumbnailRect.size.width  = scaledWidth;
    thumbnailRect.size.height = scaledHeight;
    
    [image drawInRect:thumbnailRect];
    newImage = UIGraphicsGetImageFromCurrentImageContext();
    
    UIGraphicsEndImageContext();
    
    return newImage;
}

@end
