#import "CalendarLauncherPlugin.h"

#import <EventKitUI/EventKitUI.h>
#import <CoreLocation/CoreLocation.h>

@interface CalendarLauncherPlugin () <EKEventEditViewDelegate>

@end

@implementation CalendarLauncherPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"de.parallax3d/calendar_launcher"
            binaryMessenger:[registrar messenger]];
  CalendarLauncherPlugin* instance = [[CalendarLauncherPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"showSystemCalender" isEqualToString:call.method]) {
      [self showSystemCalendar:call result:result];
  } else if ([@"requestCalendarAccess" isEqualToString:call.method]) {
      [self requestCalendarAccess:call result:result];
  } else {
      result(FlutterMethodNotImplemented);
  }
}

- (void)showSystemCalendar:(FlutterMethodCall*)call result:(FlutterResult)result {
    UIViewController *vc = [UIApplication sharedApplication].delegate.window.rootViewController;

    NSDictionary *arguments = call.arguments;
    NSDateFormatter* dateFormatter = [[NSDateFormatter alloc] init];
    dateFormatter.dateFormat = @"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    dateFormatter.timeZone = [NSTimeZone timeZoneWithName:@"UTC"];

    EKEventStore *eventStore = [[EKEventStore alloc] init];
    EKEventEditViewController *evc = [[EKEventEditViewController alloc] init];
    evc.editViewDelegate = self;
    evc.eventStore = eventStore;

    EKEvent *event = [EKEvent eventWithEventStore:eventStore];
    event.title = arguments[@"title"];
    event.startDate = [dateFormatter dateFromString:arguments[@"start"]];
    event.endDate = [dateFormatter dateFromString:arguments[@"end"]];
    event.notes = arguments[@"description"];
    event.location = arguments[@"location"];

    if (@available(iOS 15.0, *)) {
        UINavigationBarAppearance* appearance = [[UINavigationBarAppearance alloc] init];
        [appearance configureWithOpaqueBackground];

        evc.navigationBar.standardAppearance   = appearance;
        evc.navigationBar.scrollEdgeAppearance = evc.navigationBar.standardAppearance;
    }

    if (arguments[@"location"] != nil) {
        [[[CLGeocoder alloc] init] geocodeAddressString:arguments[@"location"]
                                    completionHandler:^(NSArray<CLPlacemark *> * _Nullable placemarks,
                                                        NSError * _Nullable error) {

            if (error != nil) {
                NSLog(@"GeoCoding failed: %@", error);
            } else if (placemarks.count > 0) {
//                NSString *name = arguments[@"locationTitle"];

                CLPlacemark *placeMark = placemarks[0];
                CLLocation *location = placeMark.location;
                
                if (@available(iOS 9.0, *)) {
                    EKStructuredLocation *structuredLocation = [EKStructuredLocation locationWithTitle:arguments[@"location"]];
                    structuredLocation.geoLocation = location;
                    structuredLocation.radius = 100;
                    event.structuredLocation = structuredLocation;
                }
            }


            dispatch_async(dispatch_get_main_queue(), ^{
                evc.event = event;
                [vc presentViewController:evc animated:YES completion:nil];
            });
        }];
    } else {
        dispatch_async(dispatch_get_main_queue(), ^{
            evc.event = event;
            [vc presentViewController:evc animated:YES completion:nil];
        });
    }
}

- (void)requestCalendarAccess:(FlutterMethodCall*)call result:(FlutterResult)result
{
    EKEventStore *eventStore = [[EKEventStore alloc] init];
    [eventStore requestAccessToEntityType:EKEntityTypeEvent completion:^(BOOL granted, NSError * _Nullable error) {
        if (granted && error == nil) {
            result(@{
                     @"granted" : @(granted),
                     @"error" : @""
                     });
        } else {
            result(@{
                     @"granted" : @(granted),
                     @"error" : error ? error.localizedDescription : @"Access not granted!"
                     });
        }
    }];
}

- (void)eventEditViewController:(nonnull EKEventEditViewController *)controller
          didCompleteWithAction:(EKEventEditViewAction)action
{
    id vc = [UIApplication sharedApplication].delegate.window.rootViewController;
    [vc dismissViewControllerAnimated:YES completion:nil];
}

@end
