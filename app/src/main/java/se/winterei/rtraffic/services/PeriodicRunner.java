package se.winterei.rtraffic.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PeriodicRunner extends BroadcastReceiver
{

    public static final int REQUEST_CODE = 1;
    public static final String ACTION = PeriodicRunner.class.getCanonicalName();

    @Override
    public void onReceive (Context context, Intent intent)
    {
        context.startService(new Intent(context, BackgroundTrafficStatus.class));
    }
}
