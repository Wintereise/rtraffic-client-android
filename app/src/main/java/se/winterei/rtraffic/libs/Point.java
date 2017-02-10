package se.winterei.rtraffic.libs;

import android.support.annotation.Nullable;

/**
 * Created by reise on 2/9/2017.
 */

public class Point
{
    public float longitude, latitude;
    public String title, info;
    public enum state
    {
        CONGESTED, SLOW_BUT_MOVING, UNCONGESTED
    }
    public state state;

    public Point (float longitude, float lat, String title, @Nullable String info, @Nullable state s)
    {
        this.longitude = longitude;
        this.latitude = lat;
        this.title = title;
        this.info = info;
        this.state = s;
    }
}
