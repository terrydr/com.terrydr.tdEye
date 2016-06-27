//
//  ZQBaseClassesExtended.h
//  
//
//  Created by zhuqian on 12-12-26.
//  Copyright (c) 2012年 jincheng. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface NSObject(ZQExtended)

- (BOOL)isNull;
- (BOOL)isValid;

@end

@interface NSNull(ZQExtended)

- (BOOL)isNull;
- (BOOL)isValid;

@end

@interface NSMutableDictionary(ZQExtended)

- (void)safeSetObject:(id)anObject forKey:(id)aKey;
- (void)safeSetStringObject:(NSString *)anObject forKey:(id)akey;

@end

@interface NSDictionary(ZQExtended)

- (BOOL)isNull;
- (BOOL)isValid;

// this is a safe method, you can just call this method
// if there's no key existed in dictionary or the value of key isNull, it will return nil
- (NSDictionary*)safeDictionaryForKey:(NSString *)key;
- (NSString*)safeStringForKey:(NSString *)key;
- (int)safeIntForKey:(NSString *)key;
- (BOOL)safeBoolForKey:(NSString *)key;
- (float)safeFloatForKey:(NSString *)key;
- (double)safeDoubleForKey:(NSString *)key;
- (long long)safeLongLongForKey:(NSString *)key;
- (id)getObjectForKey:(NSString*)key;
@end

@interface NSMutableArray(ZQExtended)

- (void)safeAddObject:(id)anObject;
- (void)safeInsertObject:(id)anObject atIndex:(NSUInteger)index;

@end

@interface NSArray(ZQExtended)

- (BOOL)isNull;
- (BOOL)isValid;

// this is a safe method, you can just call this method
// if index is larger than count or the value of index isNull, it will return nil
- (id)getObjectAtIndex:(NSUInteger)index;

@end

@interface NSString(ZQExtended)

- (BOOL)isNull;
- (BOOL)isValid;

@end

@interface NSNumber(ZQExtended)

- (BOOL)isNull;
- (BOOL)isValid;

@end
