package de.parallax3d.calendar_launcher;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.provider.CalendarContract;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** CalendarLauncherPlugin */
public class CalendarLauncherPlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native
    /// Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine
    /// and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "de.parallax3d/calendar_launcher");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("showSystemCalender")) {
            showSystemCalendar(call, result);
        } else if (call.method.equals("requestCalendarAccess")) {
            requestCalendarAccess(result);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    // @Override
    // public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    // super.configureFlutterEngine(flutterEngine);
    // new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(),
    // CHANNEL)
    // .setMethodCallHandler(
    // (call, result) -> {
    // if (call.method.equals("showSystemCalender")) {
    // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    // showSystemCalendar(call, result);
    // }
    // } else if (call.method.equals("requestCalendarAccess")) {
    // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    // requestCalendarAccess(result);
    // }
    // } else {
    // result.notImplemented();
    // }
    // });
    // }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void requestCalendarAccess(Result result) {
        Intent launchIntent = new Intent(Intent.ACTION_INSERT);
        launchIntent.setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, 0)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, 0)
                .putExtra(CalendarContract.Events.TITLE, "")
                .putExtra(CalendarContract.Events.DESCRIPTION, "")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "")
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

        ComponentName componentName = launchIntent.resolveActivity(mRegistrar.context().getPackageManager());

        boolean canLaunch = componentName != null
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
        } catch (ParseException ignored) {
        }

        return System.currentTimeMillis();
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
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
