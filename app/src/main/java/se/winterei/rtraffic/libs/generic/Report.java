package se.winterei.rtraffic.libs.generic;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by reise on 2/24/2017.
 */

public class Report
{
    public final boolean anonymous;
    public final String comment;
    public final int severity, id;
    public final List<LatLng> polylineList;

    public Report (@Nullable int id, int severity, String comment, boolean anonymous, List<LatLng> polylineList)
    {
        this.id = id;
        this.severity = severity;
        this.comment = comment;
        this.anonymous = anonymous;
        this.polylineList = polylineList;
    }

}
