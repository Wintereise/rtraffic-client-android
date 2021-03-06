package se.winterei.rtraffic.libs.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
        final HashMap<Marker, MarkerOptions> markerOptionsHashMap = mapContainer.getMarkerOptionsMap();

        for (final Marker marker : markerList)
        {
            final List<String> markerComments = new ArrayList<>();
            final MarkerOptions markerOptions = markerOptionsHashMap.containsKey(marker) ? markerOptionsHashMap.get(marker) : null;
            int matchCounter = 0;
            int congested = 0;
            int sbm = 0;
            int uncongested = 0;
            final int markerType;

            for (final Polyline polyline : polylineList)
            {
                if(PolyUtil.isLocationOnPath(markerPositionMap.get(marker), polylinePointsMap.get(polyline), true, Utility.polylineMatchTolerance))
                {
                    final int state = stateMap.containsKey(polyline) ? stateMap.get(polyline) : -1;

                    final String comment = commentMap.containsKey(polyline) ? commentMap.get(polyline) : null;

                    if (comment != null)
                        markerComments.add(comment);

                    switch (state)
                    {
                        case CONGESTED:
                            congested++;
                            break;
                        case SLOW_BUT_MOVING:
                            sbm++;
                            break;
                        case UNCONGESTED:
                            uncongested++;
                            break;
                        default:
                            Log.d(TAG, "doInBackground: Unrecognized state found, this polyline does not likely have state information associated with it.");
                            continue;
                    }

                    matchCounter++;

                }
            }

            if (matchCounter > 0)
            {
                if (congested > sbm && congested > uncongested)
                    markerType = R.drawable.ic_traffic_black_red;
                else if (sbm > congested && sbm > uncongested)
                    markerType = R.drawable.ic_traffic_black_orange;
                else if (uncongested > congested && uncongested > sbm)
                    markerType = R.drawable.ic_traffic_black_green;
                else
                    markerType = R.drawable.ic_traffic_black_orange;

                if (markerOptions != null)
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(markerType));

                instance.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        marker.setIcon(BitmapDescriptorFactory.fromResource(markerType));
                    }
                });
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
