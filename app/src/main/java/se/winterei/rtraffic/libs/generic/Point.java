package se.winterei.rtraffic.libs.generic;

import android.support.annotation.Nullable;

/**
 * Created by reise on 2/9/2017.
 */

public class Point
{
    public double longitude, latitude;
    public String title, info;
    public int id = -1;

    public Point (double lat, double longitude, String title, @Nullable String info)
    {
        this.longitude = longitude;
        this.latitude = lat;
        this.title = title;
        this.info = info;
    }

    public Point (int id, double lat, double longitude, String title, @Nullable String info)
    {
        this.id = id;
        this.longitude = longitude;
        this.latitude = lat;
        this.title = title;
        this.info = info;
    }
}
