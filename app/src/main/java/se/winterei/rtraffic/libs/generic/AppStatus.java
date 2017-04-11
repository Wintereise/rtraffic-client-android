package se.winterei.rtraffic.libs.generic;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import se.winterei.rtraffic.RTraffic;


public class AppStatus
{

    private static AppStatus instance = new AppStatus();
    private final static String TAG = AppStatus.class.getSimpleName();
    static Context context;
    ConnectivityManager connectivityManager;
    NetworkInfo wifiInfo, mobileInfo;
    boolean connected = false;

    public static AppStatus getInstance(Context ctx)
    {
        context = ctx.getApplicationContext();
        return instance;
    }

    public boolean isLocationServicesEnabled ()
    {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        boolean gps_enabled = false;
        boolean net_enabled = false;

        try
        {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch (Exception ex)
        {
            Log.d(TAG, "locationServicesEnabled: " + ex.getMessage());
        }

        try
        {
            net_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        catch (Exception ex)
        {
            Log.d(TAG, "locationServicesEnabled: " + ex.getMessage());
        }

        return gps_enabled || net_enabled;
    }

    public boolean isOnline ()
    {
        try
        {
            connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;

        }
        catch (Exception e)
        {
            Log.d(TAG, e.toString());
        }
        return connected;
    }
}