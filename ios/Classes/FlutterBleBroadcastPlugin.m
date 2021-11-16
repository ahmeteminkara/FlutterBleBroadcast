#import "FlutterBleBroadcastPlugin.h"
#if __has_include(<flutter_ble_broadcast/flutter_ble_broadcast-Swift.h>)
#import <flutter_ble_broadcast/flutter_ble_broadcast-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_ble_broadcast-Swift.h"
#endif

@implementation FlutterBleBroadcastPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterBleBroadcastPlugin registerWithRegistrar:registrar];
}
@end
