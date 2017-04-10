package se.winterei.rtraffic.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import se.winterei.rtraffic.libs.generic.Utility;
import se.winterei.rtraffic.libs.settings.Preference;

/**
 * Created by reise on 3/18/2017.
 */

public class FirebaseInstanceIDTracker extends FirebaseInstanceIdService
{
    private final static String TAG = FirebaseInstanceIDTracker.class.getSimpleName();

    private final String FIREBASE_UPDATE_NEEDED = "FIREBASE_UPDATE_NEEDED";
    private final String FIREBASE_INSTANCE_ID = "FIREBASE_INSTANCE_ID";

    @Override
    public void onTokenRefresh ()
    {
        String token = FirebaseInstanceId.getInstance().getToken();

        Preference preference = new Preference(getApplicationContext());
        preference.put(FIREBASE_UPDATE_NEEDED, true, Boolean.class);
        preference.put(FIREBASE_INSTANCE_ID, token, String.class);
    }

}
