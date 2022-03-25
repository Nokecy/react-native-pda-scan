#import <React/RCTBridgeModule.h>
#import "PdaScan.h"

@implementation PdaScan

RCT_EXPORT_MODULE();

+(BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
