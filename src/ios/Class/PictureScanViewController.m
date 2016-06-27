//
//  PictureScanViewController.m
//  EyeDemo
//
//  Created by 路亮亮 on 16/3/8.
//  Copyright © 2016年 路亮亮. All rights reserved.
//

#import "PictureScanViewController.h"
#import "ShootCollectionHeaderView.h"
#import "ShootCollectionViewCell.h"
#import "TDMediaFileManage.h"
#import "TDEyeTypeModel.h"
#import "TDPictureModel.h"
#import "ZQBaseClassesExtended.h"
#import "MLSelectPhotoBrowserViewController.h"

@interface PictureScanViewController ()<UICollectionViewDataSource,UICollectionViewDelegate>{
    UIBarButtonItem *_rightItem;
}

@property(nonatomic,strong) UIButton *commitBtn;
@property(nonatomic)BOOL isCollectionSelected;
@property(nonatomic)BOOL isLeftValid;
@property(nonatomic)BOOL isRightValid;
@property(nonatomic,strong) UICollectionView *collectionView;
@property(nonatomic, strong) NSMutableArray *sectionArr;
@property(nonatomic, strong) NSMutableArray *selectedModelsArr;
@property(nonatomic, strong) NSMutableArray *leftSelectedPictureModelArr;
@property(nonatomic, strong) NSMutableArray *rightSelectedPictureModelArr;

@end

@implementation PictureScanViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self addNotifications];
    [self initSubview];
    [self initShootCollectionDataArray];
}

- (void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    [self configureNavgationBar];
}

- (void)configureNavgationBar{
    self.title = @"图片浏览";
    self.navigationController.navigationBar.translucent = NO;
    self.navigationController.navigationBar.barTintColor = RGB(0x3691e6);
    self.navigationController.navigationBar.tintColor = [UIColor whiteColor];
    [self.navigationController.navigationBar setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys:[UIColor whiteColor], NSForegroundColorAttributeName, [UIFont boldSystemFontOfSize:18.f], NSFontAttributeName, nil]];
    _rightItem = [[UIBarButtonItem alloc] initWithTitle:@"选择"
                                                  style:UIBarButtonItemStylePlain
                                                 target:self
                                                 action:@selector(rightBarButtonItemAction)];
    self.navigationItem.rightBarButtonItem = _rightItem;
}

- (void)addNotifications{
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(changeSelectedStatus)
                                                 name:@"DidSelectedPictures"
                                               object:nil];
}

- (void)initSubview{
    self.view.backgroundColor = RGB(0xf7f7f7);
    [self.view addSubview:self.collectionView];
}

- (void)changeSelectedStatus{
    [_collectionView reloadData];
    [self rightBarButtonItemAction];
    [self calculateSelectedPictureCount];
}

#pragma mark ----commitBtn-----
- (UIButton *)commitBtn{
    if (!_commitBtn) {
        CGFloat commitWidth = CGRectGetWidth(self.view.bounds)-15.0f*2;
        CGFloat commitHeight = 98.0f/2.0f;
        CGFloat commitOriginY = CGRectGetHeight(self.view.bounds)-commitHeight-40;
        CGFloat commitOriginX = 15.0f;
        _commitBtn = [UIButton buttonWithType:UIButtonTypeRoundedRect];
        _commitBtn.frame = CGRectMake(commitOriginX, commitOriginY, commitWidth, commitHeight);
        [_commitBtn setTitle:@"提交" forState:UIControlStateNormal];
        [_commitBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_commitBtn setBackgroundImage:[UIImage imageNamed:@"Submit-icon"]
                              forState:UIControlStateNormal];
        [_commitBtn addTarget:self action:@selector(commitBtnClick:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _commitBtn;
}

#pragma mark ----collectionView-----

- (UICollectionView *)collectionView{
    if (!_collectionView) {
        float AD_height = 87.0f/2.0f;//header高度
        float footer_height = 22.0f/2.0f;//footer高度
        UICollectionViewFlowLayout *flowLayout=[[UICollectionViewFlowLayout alloc] init];
        [flowLayout setScrollDirection:UICollectionViewScrollDirectionVertical];
        flowLayout.headerReferenceSize = CGSizeMake(CGRectGetWidth(self.view.frame), AD_height+10);//头部
        flowLayout.footerReferenceSize = CGSizeMake(CGRectGetWidth(self.view.frame), footer_height);//尾部
        _collectionView = [[UICollectionView alloc] initWithFrame:CGRectMake(0, 0, CGRectGetWidth(self.view.frame), CGRectGetHeight(self.view.frame)-64.0f) collectionViewLayout:flowLayout];
        [_collectionView registerClass:[ShootCollectionViewCell class] forCellWithReuseIdentifier:@"cellID"];
        [self.collectionView registerClass:[UICollectionReusableView class] forSupplementaryViewOfKind:UICollectionElementKindSectionHeader withReuseIdentifier:@"ReusableView"];
        _collectionView.backgroundColor = RGB(0xf7f7f7);
        _collectionView.dataSource = self;
        _collectionView.delegate = self;
    }
    return _collectionView;
}

- (void)rightBarButtonItemAction{
    _isCollectionSelected = !_isCollectionSelected;
    
    if (_isCollectionSelected) {
        [_rightItem setTitle:@"取消"];
    }else{
        [_rightItem setTitle:@"选择"];
        if ([_selectedModelsArr isValid]) {
            for (TDPictureModel *model in _selectedModelsArr) {
                model.isSelected = NO;
            }
            [_selectedModelsArr removeAllObjects];
            if ([_leftSelectedPictureModelArr isValid]) {
                [_leftSelectedPictureModelArr removeAllObjects];
            }
            if ([_rightSelectedPictureModelArr isValid]) {
                [_rightSelectedPictureModelArr removeAllObjects];
            }
            [self.commitBtn removeFromSuperview];
            CGRect rect = _collectionView.frame;
            rect.size.height = CGRectGetHeight(self.view.bounds);
            _collectionView.frame = rect;
            [_collectionView reloadData];
        }
    }
}

- (void)initShootCollectionDataArray{
    self.sectionArr = [[NSMutableArray alloc] initWithCapacity:0];
    NSMutableArray *leftEyeDataArr = [[NSMutableArray alloc] initWithCapacity:0];
    NSMutableArray *rightEyeDataArr = [[NSMutableArray alloc] initWithCapacity:0];
    self.selectedModelsArr = [[NSMutableArray alloc] initWithCapacity:0];
    self.leftSelectedPictureModelArr = [[NSMutableArray alloc] initWithCapacity:0];
    self.rightSelectedPictureModelArr = [[NSMutableArray alloc] initWithCapacity:0];
    
    NSString *leftFilePath = [[TDMediaFileManage shareInstance] getJRMediaPathWithType:YES];
    NSError *le = nil;
    NSArray *leftFileArr = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:leftFilePath error:&le];
    NSLog(@"leftFileArr:%@",leftFileArr);
    if ([leftFileArr isValid]) {
        _isLeftValid = YES;
        for (NSString *fileName in leftFileArr) {
            TDPictureModel *picture = [[TDPictureModel alloc] init];
            picture.pictureName = fileName;
            picture.isSelected = NO;
            [leftEyeDataArr addObject:picture];
        }
        
        TDEyeTypeModel *typeModel = [[TDEyeTypeModel alloc] init];
        typeModel.isLeftEye = YES;
        typeModel.typeName = @"左眼";
        typeModel.pictureArr = leftEyeDataArr;
        [_sectionArr addObject:typeModel];
    }else{
        _isLeftValid = NO;
    }
    
    NSString *rightFilePath = [[TDMediaFileManage shareInstance] getJRMediaPathWithType:NO];
    NSError *re = nil;
    NSArray *rightFileArr = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:rightFilePath error:&re];
    NSLog(@"rightFileArr:%@",rightFileArr);
    if ([rightFileArr isValid]) {
        _isRightValid = YES;
        for (NSString *fileName in rightFileArr) {
            TDPictureModel *picture = [[TDPictureModel alloc] init];
            picture.pictureName = fileName;
            picture.isSelected = NO;
            [rightEyeDataArr addObject:picture];
        }
        
        TDEyeTypeModel *typeModel = [[TDEyeTypeModel alloc] init];
        typeModel.isLeftEye = NO;
        typeModel.typeName = @"右眼";
        typeModel.pictureArr = rightEyeDataArr;
        [_sectionArr addObject:typeModel];
    }else{
        _isRightValid = NO;
    }
}

#pragma mark -- UICollectionViewDataSource
//头部显示的内容
- (UICollectionReusableView *)collectionView:(UICollectionView *)collectionView viewForSupplementaryElementOfKind:(NSString *)kind atIndexPath:(NSIndexPath *)indexPath {
    
    UICollectionReusableView *reuseableView = [collectionView dequeueReusableSupplementaryViewOfKind:
                                            UICollectionElementKindSectionHeader withReuseIdentifier:@"ReusableView" forIndexPath:indexPath];
    
    for (id view in reuseableView.subviews) {
        [view removeFromSuperview];
    }
    if (kind == UICollectionElementKindSectionHeader) {
        ShootCollectionHeaderView *collectionHeaderView = [[ShootCollectionHeaderView alloc] initWithFrame:CGRectMake(0, 0, CGRectGetWidth(self.view.frame), (87.0f/2.0f))];
        TDEyeTypeModel *model = [_sectionArr objectAtIndex:indexPath.section];
        collectionHeaderView.typeNameLabel.text = model.typeName;
        if ([model.typeName isEqualToString:@"左眼"]) {
            collectionHeaderView.iconImgView.image = [UIImage imageNamed:@"leftEyeicon"];
            collectionHeaderView.selectedLabel.text = [NSString stringWithFormat:@"%lu/2",(unsigned long)_leftSelectedPictureModelArr.count];
        }else{
            collectionHeaderView.iconImgView.image = [UIImage imageNamed:@"rightEyeicon"];
            collectionHeaderView.selectedLabel.text = [NSString stringWithFormat:@"%lu/2",(unsigned long)_rightSelectedPictureModelArr.count];
        }
        if (indexPath.section==0) {
            collectionHeaderView.headerLineView.hidden = YES;
        }else{
            collectionHeaderView.headerLineView.hidden = NO;
        }
        [reuseableView addSubview:collectionHeaderView];//头部广告栏
    }
    return reuseableView;
}

#pragma mark --UICollectionViewDelegateFlowLayout
//定义每个UICollectionView 的大小
- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout*)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath
{
    //边距占5*4=20 ，2个
    //图片为正方形，边长：(fDeviceWidth-20)/2-5-5 所以总高(fDeviceWidth-20)/2-5-5 +20+30+5+5 label高20 btn高30 边
    return CGSizeMake(80, 80);
}

//定义每个UICollectionView 的间距
-(UIEdgeInsets)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout insetForSectionAtIndex:(NSInteger)section
{
    return UIEdgeInsetsMake(0, 15, 0, 10);
}
//定义展示的Section的个数
-(NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView
{
    return _sectionArr.count;
}
//定义展示的UICollectionViewCell的个数
-(NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section
{
    TDEyeTypeModel *model = [_sectionArr objectAtIndex:section];
    return model.pictureArr.count;
}
//每个UICollectionView展示的内容
-(UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *identify = @"cellID";
    ShootCollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:identify forIndexPath:indexPath];
    [cell sizeToFit];
    if (!cell) {
        NSLog(@"无法创建CollectionViewCell时打印，自定义的cell就不可能进来了。");
    }
    
    TDEyeTypeModel *typeModel = [_sectionArr objectAtIndex:indexPath.section];
    TDPictureModel *pictureModel = [typeModel.pictureArr objectAtIndex:indexPath.row];
    NSString *imgPath = [[TDMediaFileManage shareInstance] getImagePathWithPictureName:pictureModel.pictureName isLeftEye:typeModel.isLeftEye];
    cell.imgView.image = [UIImage imageWithContentsOfFile:imgPath];
    if (pictureModel.isSelected) {
        cell.selectedImgView.hidden = NO;
    }else{
        cell.selectedImgView.hidden = YES;
    }
    return cell;
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath{
    TDEyeTypeModel *typeModel = [_sectionArr objectAtIndex:indexPath.section];
    TDPictureModel *pictureModel = [typeModel.pictureArr objectAtIndex:indexPath.row];
    NSString *imgPath = [[TDMediaFileManage shareInstance] getImagePathWithPictureName:pictureModel.pictureName isLeftEye:typeModel.isLeftEye];
    if (_isCollectionSelected) {
        pictureModel.isSelected = !pictureModel.isSelected;
        if (pictureModel.isSelected) {
            if ([typeModel.typeName isEqualToString:@"左眼"]) {
                if (_leftSelectedPictureModelArr.count<2) {
                    [_selectedModelsArr addObject:pictureModel];
                    [_leftSelectedPictureModelArr addObject:imgPath];
                }else{
                    pictureModel.isSelected = !pictureModel.isSelected;
                    [self showBeyondLimitSelectedCount];
                }
            }else{
                if (_rightSelectedPictureModelArr.count<2) {
                    [_selectedModelsArr addObject:pictureModel];
                    [_rightSelectedPictureModelArr addObject:imgPath];
                }else{
                    pictureModel.isSelected = !pictureModel.isSelected;
                    [self showBeyondLimitSelectedCount];
                }
            }
        }else{
            [_selectedModelsArr removeObject:pictureModel];
            if ([typeModel.typeName isEqualToString:@"左眼"]) {
                [_leftSelectedPictureModelArr removeObject:imgPath];
            }else{
                [_rightSelectedPictureModelArr removeObject:imgPath];
            }
        }
        [self.collectionView reloadData];
        [self calculateSelectedPictureCount];
    }else{
        MLSelectPhotoBrowserViewController *browserVc = [[MLSelectPhotoBrowserViewController alloc] init];
        browserVc.mlLeftselectedArr = _leftSelectedPictureModelArr;
        browserVc.mlRightselectedArr = _rightSelectedPictureModelArr;
        browserVc.selectedModelArr = _selectedModelsArr;
        [browserVc setValue:@(NO) forKeyPath:@"isTrashing"];
        browserVc.isModelData = YES;
        
        if (indexPath.section==0) {
            browserVc.currentPage = indexPath.row;
        }else{
            TDEyeTypeModel *typeModel = [_sectionArr objectAtIndex:0];
            NSInteger pageIndex = typeModel.pictureArr.count + indexPath.row;
            browserVc.currentPage = pageIndex;
        }
        
        NSMutableArray *tempMutableArr = [[NSMutableArray alloc] initWithCapacity:0];
        if (_isLeftValid) {
            TDEyeTypeModel *typeModel = [_sectionArr objectAtIndex:0];
            [tempMutableArr addObjectsFromArray:typeModel.pictureArr];
            
            browserVc.leftCount = typeModel.pictureArr.count;
            
            if (_isRightValid) {
                TDEyeTypeModel *rTypeModel = [_sectionArr objectAtIndex:1];
                [tempMutableArr addObjectsFromArray:rTypeModel.pictureArr];
                
                browserVc.rightCount = rTypeModel.pictureArr.count;
            }else{
                browserVc.rightCount = 0;
            }
        }else{
            browserVc.leftCount = 0;
            
            if (_isRightValid) {
                TDEyeTypeModel *typeModel = [_sectionArr objectAtIndex:0];
                [tempMutableArr addObjectsFromArray:typeModel.pictureArr];
                
                browserVc.rightCount = typeModel.pictureArr.count;
            }else{
                browserVc.rightCount = 0;
            }
        }
        
        browserVc.photos = [NSArray arrayWithArray:tempMutableArr];
        browserVc.deleteCallBack = ^(NSArray *assets){
        };
        [self.navigationController pushViewController:browserVc animated:YES];
    }
}

- (void)calculateSelectedPictureCount{
    if (_leftSelectedPictureModelArr.count>0 || _rightSelectedPictureModelArr.count>0) {
        [self.view addSubview:self.commitBtn];
        
        CGRect rect = _collectionView.frame;
        rect.size.height = CGRectGetMinY(_commitBtn.frame)-20.0f;
        _collectionView.frame = rect;
    }else{
        [self.commitBtn removeFromSuperview];
        
        CGRect rect = _collectionView.frame;
        rect.size.height = CGRectGetHeight(self.view.bounds);
        _collectionView.frame = rect;
    }
}

- (void)showBeyondLimitSelectedCount{
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"单侧眼睛最多选择两张图片" message:nil preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *sureAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleCancel handler:^(UIAlertAction *action) {
        
    }];
    // Add the actions.
    [alertController addAction:sureAction];
    [self presentViewController:alertController animated:YES completion:nil];
}

- (void)commitBtnClick:(id)sender{
    NSMutableDictionary *temDic = [[NSMutableDictionary alloc] initWithCapacity:0];
    if ([_leftSelectedPictureModelArr isValid]) {
        [temDic setObject:_leftSelectedPictureModelArr forKey:@"leftEye"];
    }
    if ([_rightSelectedPictureModelArr isValid]) {
        [temDic setObject:_rightSelectedPictureModelArr forKey:@"rightEye"];
    }
    NSDictionary *pathDic = [NSDictionary dictionaryWithDictionary:temDic];
    [[NSNotificationCenter defaultCenter] postNotificationName:@"TakePhotosFinishedNotification"
                                                        object:nil
                                                      userInfo:pathDic];
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
