package se.winterei.rtraffic.libs.generic;


import android.content.Context;
import android.util.DisplayMetrics;

import java.util.Arrays;
import java.util.List;


public class Utility
{
    public final static int polylineMatchTolerance = 50;
    public final static int CONGESTED = 32001;
    public final static int SLOW_BUT_MOVING = 32002;
    public final static int UNCONGESTED = 32003;
    public final static long LOCATION_LOCK_MIN_TIME = 400;
    public final static float LOCATION_LOCK_MIN_DISTANCE = 1000;
    public final static long BROADCAST_ALARM_FREQ = 2 * 60 * 1000;
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

}
