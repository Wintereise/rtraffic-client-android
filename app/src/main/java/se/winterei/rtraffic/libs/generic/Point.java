package se.winterei.rtraffic.libs.generic;

import android.support.annotation.Nullable;

/**
 * Created by reise on 2/9/2017.
 */

public class Point
{
    public double longitude, latitude;
    public String title, info;

    public state condition;

    public enum state
    {
        CONGESTED, SLOW_BUT_MOVING, UNCONGESTED
    }

    public Point (double lat, double longitude, String title, @Nullable String info, @Nullable state s)
    {
        this.longitude = longitude;
        this.latitude = lat;
        this.title = title;
        this.info = info;
        this.condition = s;
    }
}
