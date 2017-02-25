package se.winterei.rtraffic.libs.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.PolyUtil;

import java.util.HashMap;
import java.util.List;

import se.winterei.rtraffic.R;
import se.winterei.rtraffic.activities.BaseActivity;
import se.winterei.rtraffic.libs.generic.Utility;
import se.winterei.rtraffic.libs.map.MapContainer;

import static se.winterei.rtraffic.libs.generic.Utility.CONGESTED;
import static se.winterei.rtraffic.libs.generic.Utility.SLOW_BUT_MOVING;
import static se.winterei.rtraffic.libs.generic.Utility.UNCONGESTED;

/**
 * Created by reise on 2/25/2017.
 */

public class AsyncMarkerStateUpdater extends AsyncTask<Void, Void, Void>
{
    private BaseActivity instance;
    private MapContainer mapContainer;
    private HashMap<Marker, LatLng> markerPositionMap;
    private HashMap<Polyline, List<LatLng>> polylinePointsMap;

    private final String TAG = AsyncMarkerStateUpdater.class.getSimpleName();

    public AsyncMarkerStateUpdater (BaseActivity instance, MapContainer mapContainer)
    {
        this.instance = instance;
        this.mapContainer = mapContainer;
        markerPositionMap = new HashMap<>();
        polylinePointsMap = new HashMap<>();
    }

    @Override
    protected void onPreExecute ()
    {
        final List<Marker> markerList = mapContainer.getMarkerList();
        final List<Polyline> polylineList = mapContainer.getPolylineList();
        for (final Marker marker : markerList)
        {
            markerPositionMap.put(marker, marker.getPosition());
        }
        for (final Polyline polyline : polylineList)
        {
            polylinePointsMap.put(polyline, polyline.getPoints());
        }
    }

    @Override
    protected Void doInBackground (Void... params)
    {
        final List<Marker> markerList = mapContainer.getMarkerList();
        final List<Polyline> polylineList = mapContainer.getPolylineList();

        for (final Marker marker : markerList)
        {
            HashMap<Polyline, Integer> stateMap = mapContainer.getPolylineStateMap();
            for (Polyline polyline : polylineList)
            {
                if(PolyUtil.isLocationOnPath(markerPositionMap.get(marker), polylinePointsMap.get(polyline), true, Utility.polylineMatchTolerance))
                {
                    final int state = stateMap.containsKey(polyline) ? stateMap.get(polyline) : -1;
                    final int markerType;

                    switch (state)
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
                            Log.d("Marker Update", "Unrecognized state found, this polyline does not likely have state information associated with it.");
                            continue;
                    }
                    instance.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(markerType));
                        }
                    });

                }
            }
        }
        return null;
    }
}
