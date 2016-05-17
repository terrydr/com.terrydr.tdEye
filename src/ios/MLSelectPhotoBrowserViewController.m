//  github: https://github.com/MakeZL/MLSelectPhoto
//  author: @email <120886865@qq.com>
//
//  MLSelectPhotoBrowserViewController.m
//  MLSelectPhoto
//
//  Created by 张磊 on 15/4/23.
//  Copyright (c) 2015年 com.zixue101.www. All rights reserved.
//

#import "MLSelectPhotoBrowserViewController.h"
#import <AssetsLibrary/AssetsLibrary.h>
#import "UIView+MLExtension.h"
#import "MLSelectPhotoPickerBrowserPhotoScrollView.h"
#import "MLSelectPhotoCommon.h"
#import "UIImage+MLTint.h"
#import "JRMediaFileManage.h"
#import "ZQBaseClassesExtended.h"

// 分页控制器的高度
static NSInteger ZLPickerColletionViewPadding = 20;
static NSString *_cellIdentifier = @"collectionViewCell";

@interface MLSelectPhotoBrowserViewController () <UIScrollViewDelegate,ZLPhotoPickerPhotoScrollViewDelegate,UICollectionViewDelegateFlowLayout,UICollectionViewDataSource,UICollectionViewDelegate>{
    
    UIBarButtonItem *_leftItem;
    UIBarButtonItem *_rightItem;
    int _leftSelectedCount;
    int _rightSelectedCount;
    UIView *_currentPageView;
}

// 控件
@property (strong,nonatomic)    UIButton          *deleleBtn;
@property (strong,nonatomic)    UIButton          *trashBtn;
@property (weak,nonatomic)      UIButton          *backBtn;
@property (strong,nonatomic)    UIButton          *selectedBtn;
@property (weak,nonatomic)      UICollectionView  *collectionView;

// 标记View
@property (strong,nonatomic)    UIToolbar *toolBar;
@property (weak,nonatomic)      UILabel *makeView;
@property (strong,nonatomic)    UIButton *doneBtn;

@property (strong,nonatomic)    UIView *pageControl;
@property (strong,nonatomic)    UIView *infoView;
@property (strong,nonatomic)    UILabel *eyeTypeLab;

@property (strong,nonatomic)    NSMutableDictionary *deleteAssets;
@property (strong,nonatomic)    NSMutableArray *doneAssets;

// 是否是编辑模式
@property (assign,nonatomic) BOOL isEditing;
// 是否是选完图预览删除模式,显示垃圾桶
@property (assign,nonatomic) BOOL isTrashing;

@property (assign,nonatomic) BOOL isShowShowSheet;
@end

@implementation MLSelectPhotoBrowserViewController

#pragma mark - getter
#pragma mark collectionView
-(NSMutableDictionary *)deleteAssets{
    if (!_deleteAssets) {
        _deleteAssets = [NSMutableDictionary dictionary];
    }
    return _deleteAssets;
}

- (NSMutableArray *)doneAssets{
    if (!_doneAssets) {
        _doneAssets = [NSMutableArray array];
    }
    return _doneAssets;
}

- (UICollectionView *)collectionView{
    if (!_collectionView) {
        UICollectionViewFlowLayout *flowLayout = [[UICollectionViewFlowLayout alloc] init];
        flowLayout.minimumLineSpacing = ZLPickerColletionViewPadding;
        flowLayout.itemSize =  self.view.ml_size;
        flowLayout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
        
        UICollectionView *collectionView = [[UICollectionView alloc] initWithFrame:CGRectMake(0, 0, self.view.ml_width + ZLPickerColletionViewPadding ,self.view.ml_height) collectionViewLayout:flowLayout];
        collectionView.showsHorizontalScrollIndicator = NO;
        collectionView.showsVerticalScrollIndicator = NO;
        collectionView.pagingEnabled = YES;
        collectionView.dataSource = self;
        collectionView.backgroundColor = [UIColor clearColor];
        collectionView.bounces = YES;
        collectionView.delegate = self;
        [collectionView registerClass:[UICollectionViewCell class] forCellWithReuseIdentifier:_cellIdentifier];
        
        [self.view addSubview:collectionView];
        if (_isModelData) {
            [self.view addSubview:self.infoView];
            [self.view addSubview:self.pageControl];
            [self.infoView addSubview:self.selectedBtn];
        }
        self.collectionView = collectionView;
        
        _collectionView.translatesAutoresizingMaskIntoConstraints = NO;
        [self.view addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|-0-[_collectionView]-x-|" options:0 metrics:@{@"x":@(-20)} views:@{@"_collectionView":_collectionView}]];
        [self.view addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|-0-[_collectionView]-0-|" options:0 metrics:nil views:@{@"_collectionView":_collectionView}]];
        
        if (self.isEditing) {
            self.makeView.hidden = !(self.photos.count && self.isEditing);
            // 初始化底部ToorBar
            [self setupToorBar];
        }
    }
    return _collectionView;
}

- (UIView *)pageControl{
    if (!_pageControl) {
        CGFloat mainWidth = CGRectGetWidth(self.view.bounds);
        _pageControl = [[UIView alloc] initWithFrame:CGRectMake(0, 100, mainWidth, 7)];
        CGFloat padding = (mainWidth-((7+10)*_photos.count-10))/2;
        for (int i=0; i<_photos.count; i++) {
            CGFloat dotOriginX = padding+(7+10)*i;
            CGFloat dotOriginY = 0.0f;
            CGFloat dotWidth = 7.0f;
            CGFloat dotHeight = 7.0f;
            
            UIView *dotView = [[UIView alloc] initWithFrame:CGRectMake(dotOriginX, dotOriginY, dotWidth, dotHeight)];
            dotView.layer.cornerRadius = 3.5f;
            if (i==_currentPage) {
                _currentPageView = dotView;
                dotView.backgroundColor = RGB(0x3691e6);
            }else{
                dotView.backgroundColor = [UIColor whiteColor];
            }
            [_pageControl addSubview:dotView];
        }
    }
    return _pageControl;
}

- (UIView *)infoView{
    if (!_infoView) {
        CGFloat infoWidth = CGRectGetWidth(self.view.bounds);
        CGFloat infoHeight = 120.0f/2.0f;
        CGFloat infoOriginX = 0.0f;
        CGFloat infoOriginY = CGRectGetHeight(self.view.bounds)-infoHeight;
        _infoView = [[UIView alloc] initWithFrame:CGRectMake(infoOriginX, infoOriginY, infoWidth, infoHeight)];
        _infoView.backgroundColor = RGB(0x3691e6);
        
        CGFloat typeOriginX = 30.0f/2.0f;
        CGFloat typeOriginY = 0.0f;
        CGFloat typeWidth = 160.0f;
        CGFloat typeHeight = infoHeight;
        self.eyeTypeLab = [[UILabel alloc] initWithFrame:CGRectMake(typeOriginX, typeOriginY, typeWidth, typeHeight)];
        if (_leftCount>0) {
            if (_rightCount>0) {
                _eyeTypeLab.text = @"左眼 0/2   右眼 0/2";
            }else{
                _eyeTypeLab.text = @"左眼 0/2";
            }
        }else{
            _eyeTypeLab.text = @"右眼 0/2";
        }
        _eyeTypeLab.textAlignment = NSTextAlignmentLeft;
        _eyeTypeLab.textColor = [UIColor whiteColor];
        [_infoView addSubview:_eyeTypeLab];
    }
    return _infoView;
}

#pragma mark Get View
#pragma mark makeView 红点标记View
- (UILabel *)makeView{
    if (!_makeView) {
        UILabel *makeView = [[UILabel alloc] init];
        makeView.textColor = [UIColor whiteColor];
        makeView.textAlignment = NSTextAlignmentCenter;
        makeView.font = [UIFont systemFontOfSize:13];
        makeView.frame = CGRectMake(-5, -5, 20, 20);
        makeView.hidden = YES;
        makeView.layer.cornerRadius = makeView.frame.size.height / 2.0;
        makeView.clipsToBounds = YES;
        makeView.backgroundColor = [UIColor redColor];
        [self.view addSubview:makeView];
        self.makeView = makeView;
        
    }
    return _makeView;
}

- (UIButton *)selectedBtn{
    if (!_selectedBtn) {
        UIImage *selectedImg = [UIImage imageNamed:@"unselectedicon"];
        CGFloat selectedWidth = 38.0f;
        CGFloat selectedHeight = 38.0f;
        CGFloat selectedOriginX = CGRectGetWidth(self.view.bounds)-selectedWidth-30.0f/2.0f;
        CGFloat selectedOriginY = (CGRectGetHeight(self.infoView.bounds)-selectedHeight)/2.0f;
        _selectedBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        _selectedBtn.frame = CGRectMake(selectedOriginX, selectedOriginY, selectedWidth, selectedHeight);
        [_selectedBtn setBackgroundImage:selectedImg forState:UIControlStateNormal];
        [_selectedBtn addTarget:self action:@selector(seclectedBtnClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _selectedBtn;
}

- (void)seclectedBtnClick:(id)sender{
    UIButton *btn = (UIButton *)sender;
    btn.selected = !btn.isSelected;
    JRPictureModel *pictureModel = self.photos[_currentPage];
    
    BOOL isLeftEye;
    if (_leftCount>0) {
        if (_currentPage+1<=_leftCount) {
            isLeftEye=YES;
        }else{
            isLeftEye=NO;
        }
    }else{
        isLeftEye=NO;
    }
    
    NSString *imgPath = [[JRMediaFileManage shareInstance] getImagePathWithPictureName:pictureModel.pictureName isLeftEye:isLeftEye];
    UIImage *selectedImg = [UIImage imageNamed:@"selectedicon"];
    UIImage *unselectedImg = [UIImage imageNamed:@"unselectedicon"];
    if (btn.selected) {
        if (_leftCount>0) {
            if (_currentPage+1<=_leftCount) {
                if (_leftSelectedCount == 2) {
                    [self mlShowBeyondLimitSelectedCount];
                }else{
                    _leftSelectedCount++;
                    [_mlLeftselectedArr addObject:imgPath];
                    [_selectedModelArr addObject:pictureModel];
                    pictureModel.isSelected = YES;
                    [_selectedBtn setBackgroundImage:selectedImg forState:UIControlStateNormal];
                }
            }else{
                if (_rightSelectedCount == 2) {
                    [self mlShowBeyondLimitSelectedCount];
                }else{
                    _rightSelectedCount++;
                    [_mlRightselectedArr addObject:imgPath];
                    [_selectedModelArr addObject:pictureModel];
                    pictureModel.isSelected = YES;
                    [_selectedBtn setBackgroundImage:selectedImg forState:UIControlStateNormal];
                }
            }
        }else{
            if (_rightSelectedCount == 2) {
                [self mlShowBeyondLimitSelectedCount];
            }else{
                _rightSelectedCount++;
                [_mlRightselectedArr addObject:imgPath];
                [_selectedModelArr addObject:pictureModel];
                pictureModel.isSelected = YES;
                [_selectedBtn setBackgroundImage:selectedImg forState:UIControlStateNormal];
            }
        }
    }else{
        if (_leftCount>0) {
            if (_currentPage+1<=_leftCount) {
                _leftSelectedCount--;
                [_mlLeftselectedArr removeObject:imgPath];
            }else{
                _rightSelectedCount--;
                [_mlRightselectedArr removeObject:imgPath];
            }
        }else{
            _rightSelectedCount--;
            [_mlRightselectedArr removeObject:imgPath];
        }
        [_selectedModelArr removeObject:pictureModel];
        pictureModel.isSelected = NO;
        [_selectedBtn setBackgroundImage:unselectedImg forState:UIControlStateNormal];
    }
    
    if (_leftCount>0) {
        if (_rightCount>0) {
            _eyeTypeLab.text = [NSString stringWithFormat:@"左眼 %d/2   右眼 %d/2",_leftSelectedCount,_rightSelectedCount];
        }else{
            _eyeTypeLab.text = [NSString stringWithFormat:@"左眼 %d/2",_leftSelectedCount];
        }
    }else{
        _eyeTypeLab.text = [NSString stringWithFormat:@"右眼 %d/2",_rightSelectedCount];
    }
    
    if (_selectedModelArr.count>0) {
        if (_isModelData) {
            self.navigationItem.rightBarButtonItem.enabled = YES;
        }
    }else{
        if (_isModelData) {
            self.navigationItem.rightBarButtonItem.enabled = NO;
        }
    }
}

- (void)mlShowBeyondLimitSelectedCount{
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"单侧眼睛最多选择两张图片" message:nil preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *sureAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleCancel handler:^(UIAlertAction *action) {
        
    }];
    // Add the actions.
    [alertController addAction:sureAction];
    [self presentViewController:alertController animated:YES completion:nil];
}

- (UIButton *)doneBtn{
    if (!_doneBtn) {
        UIButton *rightBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        [rightBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        rightBtn.enabled = YES;
        rightBtn.titleLabel.font = [UIFont systemFontOfSize:17];
        rightBtn.frame = CGRectMake(0, 0, 45, 45);
        [rightBtn setTitle:@"完成" forState:UIControlStateNormal];
        [rightBtn addTarget:self action:@selector(done) forControlEvents:UIControlEventTouchUpInside];
        [rightBtn addSubview:self.makeView];
        self.doneBtn = rightBtn;
    }
    return _doneBtn;
}

#pragma mark deleleBtn
- (UIButton *)deleleBtn{
    if (!_deleleBtn) {
        UIButton *deleleBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        deleleBtn.titleLabel.font = [UIFont systemFontOfSize:15];
        [deleleBtn setImage:[UIImage imageNamed:MLSelectPhotoSrcName(@"AssetsPickerChecked")] forState:UIControlStateNormal];
        deleleBtn.frame = CGRectMake(0, 0, 30, 30);
        [deleleBtn addTarget:self action:@selector(deleteAsset) forControlEvents:UIControlEventTouchUpInside];
        self.deleleBtn = deleleBtn;
    }
    return _deleleBtn;
}

#pragma mark trashBtn
- (UIButton *)trashBtn{
    if (!_trashBtn) {
        UIButton *trashBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        trashBtn.titleLabel.font = [UIFont systemFontOfSize:15];
        //[trashBtn setImage:[UIImage imageNamed:MLSelectPhotoSrcName(@"AssetsPickerChecked")] forState:UIControlStateNormal];
        //trashBtn.backgroundColor = [UIColor whiteColor];
        trashBtn.frame = CGRectMake(0, 0, 30, 30);
        [trashBtn setTitle:@"删除" forState:UIControlStateNormal];
        [trashBtn setTitleColor:[UIColor blueColor] forState:UIControlStateNormal];
        [trashBtn addTarget:self action:@selector(trashClick) forControlEvents:UIControlEventTouchUpInside];
        self.trashBtn = trashBtn;
    }
    return _trashBtn;
}

- (void)trashClick{
    __weak MLSelectPhotoBrowserViewController *weakSelf = self;
    
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    // Create the actions.
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:^(UIAlertAction *action) {
    }];
    
    UIAlertAction *sureAction = [UIAlertAction actionWithTitle:@"删除" style:UIAlertActionStyleDestructive handler:^(UIAlertAction *action) {
        [weakSelf trashAsset];
    }];
    
    // Add the actions.
    [alertController addAction:cancelAction];
    [alertController addAction:sureAction];
    
    [self presentViewController:alertController animated:YES completion:nil];
}

- (void)setPhotos:(NSArray *)photos{
    _photos = photos;
    _doneAssets = [NSMutableArray arrayWithArray:photos];
    
    [self reloadData];
    self.makeView.text = [NSString stringWithFormat:@"%ld",self.photos.count];
}

#pragma mark - Life cycle
- (void)dealloc{
    self.isShowShowSheet = YES;
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.view.backgroundColor = [UIColor blackColor];
    self.extendedLayoutIncludesOpaqueBars = YES;
    _leftSelectedCount = 0;
    _rightSelectedCount = 0;
    [self configureNavgationBar];
}

- (void)configureNavgationBar{
    _leftItem = [[UIBarButtonItem alloc] initWithTitle:@"取消"
                                                 style:UIBarButtonItemStylePlain
                                                target:self
                                                action:@selector(mlLeftBarButtonItemAction)];
    _rightItem = [[UIBarButtonItem alloc] initWithTitle:@"提交"
                                                  style:UIBarButtonItemStylePlain
                                                 target:self
                                                 action:@selector(mlRightBarButtonItemAction)];
    
    if (_isModelData) {
        self.navigationItem.rightBarButtonItem = _rightItem;
        self.navigationItem.rightBarButtonItem.enabled = NO;
    }else{
        self.navigationItem.leftBarButtonItem = _leftItem;
    }
}

- (void)mlLeftBarButtonItemAction{
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)mlRightBarButtonItemAction{
    NSMutableDictionary *temDic = [[NSMutableDictionary alloc] initWithCapacity:0];
    if ([_mlLeftselectedArr isValid]) {
        [temDic setObject:_mlLeftselectedArr forKey:@"leftEye"];
    }
    if ([_mlRightselectedArr isValid]) {
        [temDic setObject:_mlRightselectedArr forKey:@"rightEye"];
    }
    NSDictionary *pathDic = [NSDictionary dictionaryWithDictionary:temDic];
    [[NSNotificationCenter defaultCenter] postNotificationName:@"TakePhotosFinishedNotification"
                                                        object:nil
                                                      userInfo:pathDic];
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark -初始化底部ToorBar
- (void) setupToorBar{
    UIToolbar *toorBar = [[UIToolbar alloc] init];
    toorBar.barTintColor = UIColorFromRGB(0x333333);
    toorBar.translatesAutoresizingMaskIntoConstraints = NO;
    [self.view addSubview:toorBar];
    self.toolBar = toorBar;
    
    NSDictionary *views = NSDictionaryOfVariableBindings(toorBar);
    NSString *widthVfl =  @"H:|-0-[toorBar]-0-|";
    NSString *heightVfl = @"V:[toorBar(44)]-0-|";
    [self.view addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:widthVfl options:0 metrics:0 views:views]];
    [self.view addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:heightVfl options:0 metrics:0 views:views]];
    
    // 左视图 中间距 右视图
    UIBarButtonItem *fiexItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    UIBarButtonItem *rightItem = [[UIBarButtonItem alloc] initWithCustomView:self.doneBtn];
    
    toorBar.items = @[fiexItem,rightItem];
}

- (void)deleteAsset{
    NSString *currentPage = [NSString stringWithFormat:@"%ld",self.currentPage];
    if ([_deleteAssets valueForKeyPath:currentPage] == nil) {
        [self.deleteAssets setObject:@YES forKey:currentPage];
        [self.deleleBtn setImage:[[UIImage imageNamed:MLSelectPhotoSrcName(@"AssetsPickerChecked") ] imageWithTintColor:[UIColor grayColor]] forState:UIControlStateNormal];
        
        if ([self.doneAssets containsObject:[self.photos objectAtIndex:self.currentPage]]) {
            [self.doneAssets removeObject:[self.photos objectAtIndex:self.currentPage]];
        }
    }else{
        if (![self.doneAssets containsObject:[self.photos objectAtIndex:self.currentPage]]) {
            [self.doneAssets addObject:[self.photos objectAtIndex:self.currentPage]];
        }
        [self.deleteAssets removeObjectForKey:currentPage];
        [self.deleleBtn setImage:[UIImage imageNamed:MLSelectPhotoSrcName(@"AssetsPickerChecked") ] forState:UIControlStateNormal];
    }
    
    self.makeView.text = [NSString stringWithFormat:@"%ld",self.doneAssets.count];
}

- (void)trashAsset{
    NSMutableArray *trashAssets = [NSMutableArray arrayWithArray:self.photos];
    if ([trashAssets containsObject:[self.photos objectAtIndex:self.currentPage]]) {
        [trashAssets removeObject:[self.photos objectAtIndex:self.currentPage]];
        self.photos = [NSArray arrayWithArray:trashAssets];
    }
    self.currentPage --;
    if (self.currentPage < 0) {
        self.currentPage = 0;
    }
    if (self.deleteCallBack) {
        self.deleteCallBack(trashAssets);
    }
    if (trashAssets.count == 0) {
        [self.navigationController popViewControllerAnimated:YES];
    }else{
        [self setPageLabelPage:self.currentPage];
        [self.collectionView reloadData];
    }
}

#pragma mark - reloadData
- (void) reloadData{
    
    [self.collectionView reloadData];
    
    if (self.currentPage >= 0) {
        CGFloat attachVal = 0;
        if (self.currentPage == self.photos.count - 1 && self.currentPage > 0) {
            attachVal = ZLPickerColletionViewPadding;
        }
        
        self.collectionView.ml_x = -attachVal;
        self.collectionView.contentOffset = CGPointMake(self.currentPage * self.collectionView.ml_width, 0);
        
        if (self.currentPage == self.photos.count - 1 && self.photos.count > 1) {
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(00.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                self.collectionView.contentOffset = CGPointMake(self.currentPage * self.collectionView.ml_width, self.collectionView.contentOffset.y);
            });
        }
    }
    
    // 添加自定义View
    [self setPageLabelPage:self.currentPage];
}

- (void)setIsEditing:(BOOL)isEditing{
    _isEditing = isEditing;
    
    if (isEditing) {
        self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.deleleBtn];
    }
}

- (void)setIsTrashing:(BOOL)isTrashing{
    _isTrashing = isTrashing;
    
    if (isTrashing) {
        self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:self.trashBtn];
    }
}

#pragma mark - <UICollectionViewDataSource>
- (NSInteger) numberOfSectionsInCollectionView:(UICollectionView *)collectionView{
    return 1;
}

- (NSInteger) collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section{
    return self.photos.count;
}

- (UICollectionViewCell *) collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath{
    
    UICollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:_cellIdentifier forIndexPath:indexPath];
    
    if (self.photos.count) {
        cell.backgroundColor = [UIColor clearColor];
        UIImage *photo; //[self.dataSource photoBrowser:self photoAtIndex:indexPath.item];
        if (_isModelData) {
            BOOL isLeftEye;
            if (_leftCount>0) {
                if (indexPath.row+1<=_leftCount) {
                    isLeftEye=YES;
                }else{
                    isLeftEye=NO;
                }
            }else{
                isLeftEye=NO;
            }
            JRPictureModel *pictureModel = self.photos[indexPath.item];
            NSString *imgPath = [[JRMediaFileManage shareInstance] getImagePathWithPictureName:pictureModel.pictureName isLeftEye:isLeftEye];
            photo = [UIImage imageWithContentsOfFile:imgPath];
        }else{
            NSDictionary *paramDic = self.photos[indexPath.item];
            NSString *imgPath = [paramDic objectForKey:@"origin"];
            photo = [UIImage imageWithContentsOfFile:imgPath];
        }
        
        if([[cell.contentView.subviews lastObject] isKindOfClass:[UIView class]]){
            [[cell.contentView.subviews lastObject] removeFromSuperview];
        }
        
        UIView *scrollBoxView = [[UIView alloc] init];
        scrollBoxView.frame = cell.bounds;
        scrollBoxView.ml_y = cell.ml_y;
        [cell.contentView addSubview:scrollBoxView];
        
        MLSelectPhotoPickerBrowserPhotoScrollView *scrollView =  [[MLSelectPhotoPickerBrowserPhotoScrollView alloc] init];
        scrollView.backgroundColor = [UIColor clearColor];
        // 为了监听单击photoView事件
        scrollView.frame = [UIScreen mainScreen].bounds;
        scrollView.photoScrollViewDelegate = self;
        scrollView.photo = photo;
        
        [scrollBoxView addSubview:scrollView];
        scrollView.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
    }
    return cell;
}
// 单击调用
- (void) pickerPhotoScrollViewDidSingleClick:(MLSelectPhotoPickerBrowserPhotoScrollView *)photoScrollView{
    self.navigationController.navigationBar.hidden = !self.navigationController.navigationBar.isHidden;
    if (_isModelData) {
        self.infoView.hidden = !self.infoView.hidden;
    }
    if (self.isEditing) {
        self.toolBar.hidden = !self.toolBar.isHidden;
    }
}

// 长按调用
//- (void) pickerPhotoScrollViewDidLongPress:(MLSelectPhotoPickerBrowserPhotoScrollView *)scrollView mlPhotoImageView:(MLSelectPhotoPickerBrowserPhotoImageView *)photoImageView{
//    if (!_isShowShowSheet) {
//        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:nil message:nil preferredStyle:UIAlertControllerStyleActionSheet];
//        
//        // Create the actions.
//        UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:^(UIAlertAction *action) {
//        }];
//        
//        UIAlertAction *sureAction = [UIAlertAction actionWithTitle:@"保存到相册" style:UIAlertActionStyleDestructive handler:^(UIAlertAction *action) {
//            if([UIImagePickerController isSourceTypeAvailable: UIImagePickerControllerSourceTypeSavedPhotosAlbum]) {
//                UIImageWriteToSavedPhotosAlbum(photoImageView.image, nil, nil, nil);
//                if (photoImageView.image) {
//                    [scrollView showMessageWithText:@"保存成功"];
//                }
//            }else{
//                if (photoImageView.image) {
//                    [scrollView showMessageWithText:@"没有用户权限,保存失败"];
//                }
//            }
//        }];
//        
//        // Add the actions.
//        [alertController addAction:cancelAction];
//        [alertController addAction:sureAction];
//        
//        [self presentViewController:alertController animated:YES completion:nil];
//    }
//}

- (void)viewWillDisappear:(BOOL)animated{
    [super viewWillDisappear:animated];
    
    self.navigationController.navigationBar.hidden = NO;
    self.toolBar.hidden = NO;
    if (_mlLeftselectedArr.count>0 || _mlRightselectedArr.count>0) {
        [[NSNotificationCenter defaultCenter] postNotificationName:@"DidSelectedPictures"
                                                            object:nil];
    }
}

#pragma mark - <UIScrollViewDelegate>
- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout*)collectionViewLayout referenceSizeForFooterInSection:(NSInteger)section {
    return CGSizeMake(ZLPickerColletionViewPadding, self.view.frame.size.height);
}
- (void)scrollViewDidScroll:(UIScrollView *)scrollView{
    CGRect tempF = self.collectionView.frame;
    NSInteger currentPage = (NSInteger)((scrollView.contentOffset.x / scrollView.ml_width) + 0.5);
    if (tempF.size.width < [UIScreen mainScreen].bounds.size.width){
        tempF.size.width = [UIScreen mainScreen].bounds.size.width;
    }
    
    if([[self.deleteAssets allValues] count] == 0 || [self.deleteAssets valueForKeyPath:[NSString stringWithFormat:@"%ld",(currentPage)]] == nil){
        [self.deleleBtn setImage:[UIImage imageNamed:MLSelectPhotoSrcName(@"AssetsPickerChecked") ] forState:UIControlStateNormal];
    }else{
        [self.deleleBtn setImage:[[UIImage imageNamed:MLSelectPhotoSrcName(@"AssetsPickerChecked") ] imageWithTintColor:[UIColor grayColor]] forState:UIControlStateNormal];
    }
    
    self.collectionView.frame = tempF;
}

- (void)done{
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [[NSNotificationCenter defaultCenter] postNotificationName:PICKER_TAKE_DONE object:nil userInfo:@{@"selectAssets":self.doneAssets}];
    });
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)setPageLabelPage:(NSInteger)page{
    if (_isModelData) {
        if (_leftCount>0) {
            if (page+1<=_leftCount) {
                self.title = @"左眼";
            }else{
                self.title = @"右眼";
            }
        }else{
            self.title = @"右眼";
        }
        
        if ([_selectedModelArr isValid]) {
            NSMutableArray *indexArr = [[NSMutableArray alloc] initWithCapacity:0];
            for (JRPictureModel *model in _selectedModelArr) {
                NSUInteger index = [_photos indexOfObject:model];
                [indexArr addObject:[NSNumber numberWithUnsignedInteger:index]];
            }
            
            for (int i=0; i<_photos.count; i++) {
                UIView *dotView = [_pageControl.subviews objectAtIndex:i];
                if ([indexArr containsObject:[NSNumber numberWithInt:i]]) {
                    dotView.backgroundColor = RGB(0x76c000);
                }else{
                    dotView.backgroundColor = [UIColor whiteColor];
                }
            }
        }else{
            _currentPageView.backgroundColor = [UIColor whiteColor];
        }
        
        UIView *dotView = [_pageControl.subviews objectAtIndex:page];
        dotView.backgroundColor = RGB(0x3691e6);
        _currentPageView = dotView;
        
        JRPictureModel *pictureModel = self.photos[page];
        UIImage *selectedImg = [UIImage imageNamed:@"selectedicon"];
        UIImage *unselectedImg = [UIImage imageNamed:@"unselectedicon"];
        if (pictureModel.isSelected) {
            _selectedBtn.selected = YES;
            [_selectedBtn setBackgroundImage:selectedImg forState:UIControlStateNormal];
        }else{
            _selectedBtn.selected = NO;
            [_selectedBtn setBackgroundImage:unselectedImg forState:UIControlStateNormal];
        }
    }else{
        self.title = [NSString stringWithFormat:@"%ld / %ld",page + 1, self.photos.count];
    }
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView{
    NSInteger currentPage = (NSInteger)((scrollView.contentOffset.x / scrollView.ml_width) + 0.5);
    self.currentPage = currentPage;
    [self setPageLabelPage:currentPage];
}

@end