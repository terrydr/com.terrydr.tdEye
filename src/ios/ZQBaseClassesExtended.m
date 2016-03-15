//
//  ZQBaseClassesExtended.m
//  
//
//  Created by zhuqian on 12-12-26.
//  Copyright (c) 2012年 jincheng. All rights reserved.
//

#import "ZQBaseClassesExtended.h"

@implementation NSObject(ZQExtended)

- (BOOL)isNull {
    if (self != nil && ![self isKindOfClass:[NSNull class]]) {
        return NO;
    }
    return YES;
}
- (BOOL)isValid {
    return ![self isNull];
}

@end

@implementation NSNull(ZQExtended)

- (BOOL)isNull {
    return YES;
}
- (BOOL)isValid {
    return NO;
}

@end

@implementation NSMutableDictionary(ZQExtended)

- (void)safeSetObject:(id)anObject forKey:(id)aKey {
    if ([anObject isValid] && aKey && [aKey isKindOfClass:[NSString class]]) {
        [self setObject:anObject forKey:aKey];
    }
}
- (void)safeSetStringObject:(NSString *)anObject forKey:(id)akey
{
    if ([anObject isValid]) {
        [self setObject:anObject forKey:akey];
    }
    else {
        [self setObject:@"" forKey:akey];
    }
}

@end

@implementation NSDictionary(ZQExtended)

- (BOOL)isNull {
    if (self != nil && ![self isKindOfClass:[NSNull class]]) {
        if (self.count > 0) {
            return NO;
        }
    }
    return YES;
}
- (BOOL)isValid {
    return ![self isNull];
}
- (BOOL)isValidString:(id)object{
    if (object && [object isKindOfClass:[NSString class]] && [object length]>0) {
        if (NSOrderedSame != [object caseInsensitiveCompare:@"null"]) {
            return YES;
        }
    }
    return NO;
}
- (BOOL)isValidNumber:(id)object{
    if (object && [object isKindOfClass:[NSNumber class]]){
        return YES;
    }
    return NO;
}
- (BOOL)isValidDigital:(id)object{
    if ([self isValidNumber:object] || [self isValidString:object]) {
        return YES;
    }
    return NO;
}
- (NSDictionary*)safeDictionaryForKey:(NSString *)key{
    if ([self isValidString:key]) {
        id value = [self objectForKey:key];
        if (value!=nil && [value isKindOfClass:[NSDictionary class]]) {
            return value;
        }
    }
    return nil;
}
- (NSString*)safeStringForKey:(NSString *)key{
    if ([self isValidString:key]) {
        id value = [self objectForKey:key];
        if ([self isValidString:value]) {
            return value;
        }
    }
    return @"";
}
- (int)safeIntForKey:(NSString *)key{
    if ([self isValidString:key]) {
        id value = [self objectForKey:key];
        if ([self isValidDigital:value]) {
            return [value intValue];
        }
    }
    return 0;
}
- (BOOL)safeBoolForKey:(NSString *)key{
    if ([self isValidString:key]) {
        id value = [self objectForKey:key];
        if ([self isValidDigital:value]) {
            return [value boolValue];
        }
    }
    return NO;
}
- (float)safeFloatForKey:(NSString *)key{
    if ([self isValidString:key]) {
        id value = [self objectForKey:key];
        if ([self isValidDigital:value]) {
            return [value floatValue];
        }
    }
    return 0.0;
}
- (double)safeDoubleForKey:(NSString *)key
{
    if ([self isValidString:key]) {
        id value = [self objectForKey:key];
        if ([self isValidDigital:value]) {
            return [value doubleValue];
        }
    }
    return 0.0;
}
- (long long)safeLongLongForKey:(NSString *)key{
    if ([self isValidString:key]) {
        id value = [self objectForKey:key];
        if ([self isValidDigital:value]) {
            return [value longLongValue];
        }
    }
    return 0;
}
- (id)getObjectForKey:(NSString *)key {
    if (![key isNull] && ![self isNull]) {
        id value = [self objectForKey:key];
        if (value != nil && ![value isKindOfClass:[NSNull class]]) {
            if (![value isNull]) {
                return value;
            }
        }
    }
    return nil;
}

@end


@implementation NSMutableArray(ZQExtended)

- (void)safeAddObject:(id)anObject{
    if (anObject != nil) {
        [self addObject:anObject];
    }
}
- (void)safeInsertObject:(id)anObject atIndex:(NSUInteger)index{
    if (anObject != nil) {
        [self insertObject:anObject atIndex:index];
    }
}

@end

@implementation NSArray(ZQExtended)

- (BOOL)isNull {
    if (self != nil && ![self isKindOfClass:[NSNull class]]) {
        if (self.count > 0) {
            return NO;
        }
    }
    return YES;
}
- (BOOL)isValid {
    return ![self isNull];
}
- (id)getObjectAtIndex:(NSUInteger)index {
    if (![self isNull]) {
        if (index < self.count) {
            id value = [self objectAtIndex:index];
            if (value != nil && ![value isKindOfClass:[NSNull class]]) {
                if (![value isNull]) {
                    return value;
                }
            }
        }
    }
    return nil;
}

@end

@implementation NSString(ZQExtended)

- (BOOL)isNull {
    if (self != nil && ![self isKindOfClass:[NSNull class]]) {
        if (self.length > 0) {
            return NO;
        }
    }
    return YES;
}
- (BOOL)isValid {
    return ![self isNull];
}

@end

@implementation NSNumber(ZQExtended)

- (BOOL)isNull {
    if (self != nil && ![self isKindOfClass:[NSNull class]]) {
        return NO;
    }
    return YES;
}
- (BOOL)isValid {
    return ![self isNull];
}

@end
