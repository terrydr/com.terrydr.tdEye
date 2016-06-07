//
//  TDAlertView.h
//  TDAlertView
//
//  Created by 路亮亮 on 16/6/6.
//  Copyright © 2016年 路亮亮. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 *  button index
 */
typedef NS_ENUM(NSInteger, TDAlertButton){
    TDAlertButtonOk,
    TDAlertButtonCancel
};

/**
 *  the block to tell user whitch button is clicked
 *
 *  @param button button
 */
typedef void (^selectButton)(TDAlertButton buttonindex);

@interface TDAlertView : UIView

@property (nonatomic, strong) NSString *title;
@property (nonatomic, strong) NSString *content;

- (void)tdShowWithSelectedBlock:(selectButton)block;

@end
