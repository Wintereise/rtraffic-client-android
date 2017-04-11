package se.winterei.rtraffic.libs.generic;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import se.winterei.rtraffic.RTraffic;
import se.winterei.rtraffic.libs.settings.Preference;
import se.winterei.rtraffic.services.PeriodicRunner;


public class Utility
{
    public final static int polylineMatchTolerance = 50;
    public final static int exclusionRadius = 200;

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

    /**
     * This is the implementation Haversine Distance Algorithm between two places
     *
        R = earth’s radius (mean radius = 6,371km)
        Δlat = lat2 − lat1
        Δlong = long2 − long1
        a = sin²(Δlat/2) + cos(lat1).cos(lat2).sin²(Δlong/2)
        c = 2.atan2(√a, √(1−a))
        d = R.c
     *
     */

    public static double greaterCircleDistance (LatLng one, LatLng two)
    {
        final int R = 6371;
        double latDistance = degreesToRadians(two.latitude - one.latitude);
        double lonDistance = degreesToRadians(two.longitude  - one.longitude);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(degreesToRadians(one.latitude)) * Math.cos(degreesToRadians(two.latitude)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static double degreesToRadians (double value)
    {
        return value * Math.PI / 180;
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

    @SuppressWarnings({"MissingPermission"})
    public static Location getCachedLocationOrRegisterForLocationUpdates (RTraffic context, Preference preference, LocationManager locationManager, LocationListener callback)
    {
        final Location cached = (Location) context.get("lastLocation");
        boolean useCached = true;

        if (cached == null)
            useCached = false;
        else
        {
            Date date = new Date(cached.getTime());
            Date now = new Date();
            if (now.getTime() - date.getTime() >= 1*60*1000) //checking if update is older than 5 minutes
                useCached = false;
        }

        if (useCached)
            return cached;
        else
        {
            String preferredProvider = (String) preference.get("pref_location_provider", LocationManager.NETWORK_PROVIDER, String.class);
            String provider;

            if (preferredProvider.equals("2"))
                provider = LocationManager.NETWORK_PROVIDER;
            else
                provider = LocationManager.GPS_PROVIDER;
            locationManager.requestLocationUpdates(provider, Utility.LOCATION_LOCK_MIN_TIME, Utility.LOCATION_LOCK_MIN_DISTANCE, callback);
            return null;
        }
    }
}
