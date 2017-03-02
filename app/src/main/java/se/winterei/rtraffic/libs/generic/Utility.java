package se.winterei.rtraffic.libs.generic;


import android.content.Context;
import android.util.DisplayMetrics;

public class Utility
{
    public final static int polylineMatchTolerance = 50;
    public final static int CONGESTED = 32001;
    public final static int SLOW_BUT_MOVING = 32002;
    public final static int UNCONGESTED = 32003;
    public final static long LOCATION_LOCK_MIN_TIME = 400;
    public final static float LOCATION_LOCK_MIN_DISTANCE = 1000;

    public static int dpToPx (Context context, int dp)
    {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
