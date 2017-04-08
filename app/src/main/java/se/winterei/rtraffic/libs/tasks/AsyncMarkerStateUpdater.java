package se.winterei.rtraffic.libs.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import se.winterei.rtraffic.R;
import se.winterei.rtraffic.RTraffic;
import se.winterei.rtraffic.activities.BaseActivity;
import se.winterei.rtraffic.libs.generic.Utility;
import se.winterei.rtraffic.libs.map.MapContainer;
import se.winterei.rtraffic.libs.settings.Preference;

import static se.winterei.rtraffic.libs.generic.Utility.CONGESTED;
import static se.winterei.rtraffic.libs.generic.Utility.SLOW_BUT_MOVING;
import static se.winterei.rtraffic.libs.generic.Utility.UNCONGESTED;

/**
 * Created by reise on 2/25/2017.
 */

public class AsyncMarkerStateUpdater extends AsyncTask<Void, Void, Void>
{
    private final BaseActivity instance;
    private final MapContainer mapContainer;
    private final HashMap<Marker, LatLng> markerPositionMap;
    private final HashMap<Polyline, List<LatLng>> polylinePointsMap;

    private final String TAG = AsyncMarkerStateUpdater.class.getSimpleName();
    private Preference preference;

    public boolean running = false;

    public AsyncMarkerStateUpdater (BaseActivity instance, MapContainer mapContainer)
    {
        this.instance = instance;
        this.mapContainer = mapContainer;
        markerPositionMap = new HashMap<>();
        polylinePointsMap = new HashMap<>();
        preference = new Preference(RTraffic.getAppContext());
    }

    @Override
    protected void onPreExecute ()
    {
        running = true;
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
    protected synchronized Void doInBackground (Void... params)
    {
        final List<Marker> markerList = mapContainer.getMarkerList();
        final List<Polyline> polylineList = mapContainer.getPolylineList();
        final HashMap<Polyline, Integer> stateMap = mapContainer.getPolylineStateMap();
        final HashMap<Polyline, String> commentMap = mapContainer.getPolylineCommentMap();

        for (final Marker marker : markerList)
        {
            final List<String> markerComments = new ArrayList<>();
            int matchCounter = 0;

            for (final Polyline polyline : polylineList)
            {
                if(PolyUtil.isLocationOnPath(markerPositionMap.get(marker), polylinePointsMap.get(polyline), true, Utility.polylineMatchTolerance))
                {
                    final int state = stateMap.containsKey(polyline) ? stateMap.get(polyline) : -1;
                    final int markerType;
                    final String comment = commentMap.containsKey(polyline) ? commentMap.get(polyline) : null;

                    if (comment != null && ! comment.equals(""))
                        markerComments.add(comment);

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
                            Log.d(TAG, "doInBackground: Unrecognized state found, this polyline does not likely have state information associated with it.");
                            continue;
                    }
                    matchCounter++;
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

            //Add comment count to the market title enclosed in []
            final int commentListSize = markerComments.size();
            if (commentListSize > 0)
            {
                instance.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String existingMarkerTitle = marker.getTitle();
                        existingMarkerTitle = existingMarkerTitle.replaceAll("\\s*\\[[^\\]]*\\]\\s*", " ");
                        marker.setTitle("[" + commentListSize + "] " + existingMarkerTitle);
                        marker.setTag(markerComments);
                    }
                });
            }

            //Hide marker if said marker has not had a polyline intersect it.
            if (matchCounter == 0 && (Boolean) preference.get("pref_hide_excess_markers", false, Boolean.class))
            {
                instance.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        marker.setVisible(false);
                    }
                });
            }
        }
        running = false;
        return null;
    }
}
