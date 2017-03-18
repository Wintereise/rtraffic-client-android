package se.winterei.rtraffic.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by reise on 3/18/2017.
 */

public class FirebaseInstanceIDTracker extends FirebaseInstanceIdService
{
    private final static String TAG = FirebaseInstanceIDTracker.class.getSimpleName();

    @Override
    public void onTokenRefresh ()
    {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onTokenRefresh: token was " + token);
        sendTokenToAPI(token);
    }

    private void sendTokenToAPI (String token)
    {

    }
}
