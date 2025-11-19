package de.parallax3d.calendar_launcher;

import android.content.ComponentName;
import android.content.Intent;
import android.provider.CalendarContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** CalendarLauncherPlugin */
public class CalendarLauncherPlugin implements MethodCallHandler {

    private static final String CHANNEL = "de.parallax3d/calendar_launcher";

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
            .setMethodCallHandler(
            (call, result) -> {
                if (call.method.equals("showSystemCalender")) {
                    showSystemCalendar(call, result);
                } else if (call.method.equals("requestCalendarAccess")) {
                    requestCalendarAccess(result);
                } else {
                    result.notImplemented();
                }
            }
        );
    }

    private void requestCalendarAccess(Result result) {
        Intent launchIntent = new Intent(Intent.ACTION_INSERT);
        launchIntent.setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, 0)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, 0)
                .putExtra(CalendarContract.Events.TITLE, "")
                .putExtra(CalendarContract.Events.DESCRIPTION, "")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "")
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

        ComponentName componentName =
                launchIntent.resolveActivity(mRegistrar.context().getPackageManager());

        boolean canLaunch =
                componentName != null
                        && !"{com.android.fallback/com.android.fallback.Fallback}"
                        .equals(componentName.toShortString());
        Map<String, Object> answer = new HashMap<>();
        answer.put("granted", canLaunch);
        answer.put("error", canLaunch ? "" : "No calendar activity found");
        result.success(answer);
    }

    private static long fromISO8601UTC(String dateStr) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date date = df.parse(dateStr);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException ignored) {}

        return System.currentTimeMillis();
    }

    private void showSystemCalendar(MethodCall call, Result result) {

        String title = call.argument("title");
        String start = call.argument("start");
        String end = call.argument("end");
        String description = call.argument("description");
        String location = call.argument("location");

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, fromISO8601UTC(start))
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, fromISO8601UTC(end))
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.DESCRIPTION, description)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

        mRegistrar.activity().startActivity(intent);
        result.success(null);
    }
}
