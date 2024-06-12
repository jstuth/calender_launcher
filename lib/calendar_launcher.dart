import 'dart:async';

import 'package:flutter/services.dart';

class CalendarData {
  final String title;
  final DateTime start;
  final DateTime end;
  final String description;
  final String locationTitle;
  final String location;

  CalendarData({
    required this.title,
    required this.start,
    required this.end,
    required this.description,
    required this.locationTitle,
    required this.location,
  });

  Map<String, dynamic> toJson() => {
        'title': title,
        'start': start.toUtc().toIso8601String(),
        'end': end.toUtc().toIso8601String(),
        'description': description,
        'location': location,
        // 'locationTitle': locationTitle,
      };
}

class CalendarLauncher {
  static const _channel =
      const MethodChannel('de.parallax3d/calendar_launcher');

  static Future<bool> get requestCalendarAccess async {
    final version = await _channel
        .invokeMethod<Map<dynamic, dynamic>>('requestCalendarAccess');
    return version?['granted'];
  }

  static Future<void> showCalendar(CalendarData calendarData) async {
    Map data = calendarData.toJson();
    await _channel.invokeMethod('showSystemCalender', data);
  }
}
