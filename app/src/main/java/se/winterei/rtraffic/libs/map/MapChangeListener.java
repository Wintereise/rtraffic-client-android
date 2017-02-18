package se.winterei.rtraffic.libs.map;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

public interface MapChangeListener
{
    void onPolylineAdded (Polyline polyline);
    void onMarkerAdded (Marker marker);
}
