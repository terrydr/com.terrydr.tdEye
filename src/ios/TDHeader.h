//
//  TDHeader.h
//  EyeDemo
//
//  Created by 路亮亮 on 16/3/26.
//  Copyright © 2016年 路亮亮. All rights reserved.
//

#ifndef TDHeader_h
#define TDHeader_h

#define RGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16)) / 255.0 green:((float)((rgbValue & 0xFF00) >> 8)) / 255.0 blue:((float)(rgbValue & 0xFF)) / 255.0 alpha:1.0]
#define APP_WIDTH [UIScreen mainScreen].bounds.size.width
#define APP_HEIGHT [UIScreen mainScreen].bounds.size.height

#endif /* TDHeader_h */
