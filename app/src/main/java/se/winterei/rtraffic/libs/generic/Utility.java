package se.winterei.rtraffic.libs.generic;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import se.winterei.rtraffic.RTraffic;
import se.winterei.rtraffic.libs.settings.Preference;
import se.winterei.rtraffic.services.PeriodicRunner;


public class Utility
{
    public final static int polylineMatchTolerance = 50;
    public final static int CONGESTED = 32001;
    public final static int SLOW_BUT_MOVING = 32002;
    public final static int UNCONGESTED = 32003;
    public final static long LOCATION_LOCK_MIN_TIME = 400;
    public final static float LOCATION_LOCK_MIN_DISTANCE = 1000;
    public final static long BROADCAST_ALARM_FREQ = 5 * 60 * 1000;
    public final static int MAIN_MAP_POLYLINE_WIDTH = 3;

    public final static String RTRAFFIC_API_KEY = "RTRAFFIC_API_KEY";

    private final static String TAG = Utility.class.getSimpleName();

    public static int dpToPx (Context context, int dp)
    {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static boolean containsAny (String haystack, String[] needles)
    {
        boolean bResult = false;

        final List<String> list = Arrays.asList(needles);
        final String tempString = haystack.toLowerCase();

        for (final String word : list)
        {
            String tempWord = word.toLowerCase();
            if (tempString.contains(tempWord))
            {
                bResult = true;
                break;
            }
        }
        return bResult;
    }

    public static void scheduleAlarm (Preference preference)
    {
        Boolean notificationsEnabled = (Boolean) preference.get("pref_notifications", true, Boolean.class);
        Boolean backgroundServiceEnabled = (Boolean) preference.get("pref_background_service", true, Boolean.class);

        if ( !backgroundServiceEnabled || !notificationsEnabled)
        {
            Log.d(TAG, "scheduleAlarm: refraining from scheduling background activity since user settings do not allow for it.");
            return;
        }

        final Context ctx = RTraffic.getAppContext();

        final Intent intent = new Intent(ctx, PeriodicRunner.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, PeriodicRunner.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = (AlarmManager)  ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), Utility.BROADCAST_ALARM_FREQ, pendingIntent);
        Log.d(TAG, "scheduleAlarm: scheduled alarm");
    }

    public static void cancelAlarm ()
    {
        final Context ctx = RTraffic.getAppContext();

        final Intent intent = new Intent(ctx, PeriodicRunner.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, PeriodicRunner.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        if(pendingIntent != null)
            alarmManager.cancel(pendingIntent);
        Log.d(TAG, "cancelAlarm: cancelled alarm.");
    }

}
