package se.winterei.rtraffic.libs.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import se.winterei.rtraffic.R;
import se.winterei.rtraffic.libs.generic.Point;

public class MapContainer
{
    private GoogleMap map;
    private List<Marker> markerList;
    private List<Polyline> polylineList;
    private HashMap<Polyline, Integer> polylineStateMap;
    private List<MapChangeListener> mapChangeListenerList;
    private boolean notificationEnabled = true;
    private enum type
    {
        MARKER, POLYLINE
    }

    public MapContainer (GoogleMap map)
    {
        this.map = map;
        markerList = new ArrayList<>();
        polylineList = new ArrayList<>();
        polylineStateMap = new HashMap<>();
        mapChangeListenerList = new ArrayList<>();
    }

    public Marker addMarker (MarkerOptions markerOptions)
    {
        Marker tmp = map.addMarker(markerOptions);
        markerList.add(tmp);
        notifyListeners(type.MARKER, tmp);
        return tmp;
    }

    public Marker addInfoMarker (IconGenerator iconGenerator, CharSequence charSequence, LatLng position)
    {
        MarkerOptions markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(charSequence)))
                .position(position)
                .title(charSequence.toString())
                .anchor(iconGenerator.getAnchorU(), iconGenerator.getAnchorV());

        return addMarker(markerOptions);
    }

    public Polyline addPolyline (PolylineOptions polylineOptions)
    {
        Polyline tmp = map.addPolyline(polylineOptions);
        polylineList.add(tmp);
        notifyListeners(type.POLYLINE, tmp);
        return tmp;
    }

    public Polyline addPolyline (PolylineOptions polylineOptions, int state)
    {
        Polyline tmp = map.addPolyline(polylineOptions);
        polylineList.add(tmp);
        polylineStateMap.put(tmp, state);
        notifyListeners(type.POLYLINE, tmp);
        return tmp;
    }

    public void addPoints (List<Point> pointList)
    {
        for (Point point : pointList)
        {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(point.latitude, point.longitude))
                    .title(point.title)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_gray));

            addMarker(markerOptions);
        }
    }

    public synchronized void addPointsWithoutObserverNotify (List<Point> points)
    {
        disableObservers();
        addPoints(points);
        enableObservers();
    }

    public List<Marker> getMarkerList ()
    {
        return markerList;
    }

    public List<Polyline> getPolylineList ()
    {
        return polylineList;
    }

    public HashMap<Polyline, Integer> getPolylineStateMap()
    {
        return polylineStateMap;
    }

    public GoogleMap getMap ()
    {
        return map;
    }

    public void enableObservers ()
    {
        this.notificationEnabled = true;
    }

    public void disableObservers ()
    {
        this.notificationEnabled = false;
    }

    private void notifyListeners (type type, Object o)
    {
        if(!notificationEnabled)
            return;
        for (MapChangeListener listener : mapChangeListenerList)
        {
            switch (type)
            {
                case MARKER:
                    listener.onMarkerAdded((Marker) o);
                    break;
                case POLYLINE:
                    listener.onPolylineAdded((Polyline) o);
                    break;
            }
        }
    }

    public void registerListener (MapChangeListener m)
    {
        mapChangeListenerList.add(m);
    }

    public void unregisterListener (MapChangeListener m)
    {
        mapChangeListenerList.remove(m);
    }

    public void clear ()
    {
        markerList.clear();
        polylineList.clear();
        map.clear();
    }
}
