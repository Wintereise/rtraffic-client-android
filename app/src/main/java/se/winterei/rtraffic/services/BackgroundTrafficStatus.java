package se.winterei.rtraffic.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import se.winterei.rtraffic.R;
import se.winterei.rtraffic.activities.ExcludedRegionsActivity;
import se.winterei.rtraffic.activities.TrafficReportActivity;
import se.winterei.rtraffic.libs.generic.ExcludedRegion;
import se.winterei.rtraffic.libs.generic.Utility;
import se.winterei.rtraffic.libs.settings.Preference;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */

public class BackgroundTrafficStatus extends IntentService
{
    private static final String TAG = BackgroundTrafficStatus.class.getSimpleName();
    private boolean notificationsEnabled;
    private boolean backgroundServiceEnabled;
    private LocationManager locationManager;
    private String locationProvider;
    private Preference preference;

    public BackgroundTrafficStatus ()
    {
        super(TAG);
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onCreate ()
    {
        preference = new Preference(this);
        notificationsEnabled = (Boolean) preference.get("pref_notifications", true, Boolean.class);
        backgroundServiceEnabled = (Boolean) preference.get("pref_background_service", true, Boolean.class);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        locationProvider = locationManager.getBestProvider(criteria, true);
        super.onCreate();
    }



    @Override
    @SuppressWarnings({"MissingPermission", "unchecked"})
    protected void onHandleIntent (Intent intent)
    {
        if(intent == null || !backgroundServiceEnabled || !notificationsEnabled)
        {
            Log.d(TAG, "onHandleIntent: Services are disabled :(");
            return;
        }

        Location location = locationManager.getLastKnownLocation(locationProvider);

        if (location == null)
        {
            Log.d(TAG, "onHandleIntent: location was null");
            return;
        }

        Date date = new Date(location.getTime());
        Date  now = new Date();

        if (now.getTime() - date.getTime() >= 5*60*1000) //checking if update is older than 5 minutes
        {
            Log.d(TAG, "onHandleIntent: location update is older than 5 minutes, skipping.");
            return;
        }

        String JSONExcludedRegions = (String) preference.get(ExcludedRegionsActivity.class.getSimpleName(), "", String.class);
        if (! JSONExcludedRegions.equals(""))
        {
            Moshi moshi = new Moshi.Builder().build();
            try
            {
                List<ExcludedRegion> excludedRegions = (List<ExcludedRegion>) moshi.adapter(Types.newParameterizedType(List.class, ExcludedRegion.class)).fromJson(JSONExcludedRegions);
                if (excludedRegions != null && excludedRegions.size() > 0)
                {
                    for (ExcludedRegion excludedRegion : excludedRegions)
                    {
                        if (Utility.greaterCircleDistance(new LatLng(location.getLatitude(), location.getLongitude()), excludedRegion.location) <= Utility.exclusionRadius)
                        {
                            Log.d(TAG, "onHandleIntent: current location is excluded via region exclusions");
                            return;
                        }
                    }
                }
            }
            catch (IOException e)
            {
                Log.d(TAG, "onHandleIntent: " + e.getMessage());
            }
        }

        if (location.hasSpeed())
        {
            if (location.getSpeed() >= 1.39) //5 kilometers an hour is 1.39 m/s
            {
                Log.d(TAG, "onHandleIntent: speed was " + location.getSpeed() + " m/s, user is not sedentary.");
                return;
            }
        }
        else
            Log.d(TAG, "onHandleIntent: No speed information available, operating based on guesses.");

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent tIntent = new Intent(this, TrafficReportActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), tIntent, 0);
        android.support.v4.app.NotificationCompat.Action affirmative_action = new NotificationCompat.Action.Builder(R.drawable.ic_done_black_24dp,
                getString(R.string.service_notif_affirmative_answer), pIntent)
                .build();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        final StringBuilder sb = new StringBuilder();
        try
        {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if(addressList != null)
            {
                Address address = addressList.get(0);

                for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
                {
                    sb.append(address.getAddressLine(i));
                    if((i + 1) != address.getMaxAddressLineIndex())
                        sb.append(", ");
                }
            }
            else
                return;
        }
        catch (IOException e)
        {
            Log.d(TAG, "onHandleIntent: " + e.getMessage());
            return;
        }

        String address = sb.toString();
        String[] roadIdentifiers = { "road", "way", "rd.", "wy.", "highway", "avenue", "rd", "wy", "no."};

        if (! Utility.containsAny(address, roadIdentifiers))
        {
            Log.d(TAG, "onHandleIntent: " + address + " does not seem to be a road.");
            return;
        }

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.service_notif_main_header))
                .setContentText(getString(R.string.service_notif_main_body, sb.toString()))
                .setSmallIcon(R.drawable.ic_explore_black_24dp)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .addAction(affirmative_action)
                .build();
        notificationManager.notify(0, notification);
    }
}