package se.winterei.rtraffic.libs.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

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
    private HashMap<Polyline, Integer> polylineStateMap = new HashMap<>();
    private List<MapChangeListener> mapChangeListenerList = new ArrayList<>();

    public MapContainer (GoogleMap map)
    {
        this.map = map;
        markerList = new ArrayList<>();
        polylineList = new ArrayList<>();
    }

    public Marker addMarker (MarkerOptions markerOptions)
    {
        Marker tmp = map.addMarker(markerOptions);
        markerList.add(tmp);
        notifyListeners("marker", tmp);
        return tmp;
    }

    public Polyline addPolyline (PolylineOptions polylineOptions)
    {
        Polyline tmp = map.addPolyline(polylineOptions);
        polylineList.add(tmp);
        notifyListeners("polyline", tmp);
        return tmp;
    }

    public Polyline addPolyline (PolylineOptions polylineOptions, int state)
    {
        Polyline tmp = map.addPolyline(polylineOptions);
        polylineList.add(tmp);
        polylineStateMap.put(tmp, state);
        notifyListeners("polyline", tmp);
        return tmp;
    }

    public void addPoints (List<Point> pointList)
    {
        int markerType = 0;

        for (Point point : pointList)
        {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(point.latitude, point.longitude))
                    .title(point.title);
            switch (point.condition)
            {
                case CONGESTED:
                    markerType = R.drawable.ic_traffic_black_red;
                    break;
                case SLOW_BUT_MOVING:
                    markerType = R.drawable.ic_traffic_black_yellow;
                    break;
                case UNCONGESTED:
                    markerType = R.drawable.ic_traffic_black_green;
                    break;
                default:
                    markerType = R.drawable.ic_traffic_black_18dp;
            }
            markerOptions.icon(BitmapDescriptorFactory.fromResource(markerType));
            addMarker(markerOptions);
        }
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

    private void notifyListeners (String type, Object o)
    {
        for (MapChangeListener listener : mapChangeListenerList)
        {
            switch (type)
            {
                case "marker":
                    listener.onMarkerAdded((Marker) o);
                    break;
                case "polyline":
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
