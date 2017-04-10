package se.winterei.rtraffic.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.akexorcist.googledirection.util.DirectionConverter;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.winterei.rtraffic.R;
import se.winterei.rtraffic.libs.api.GenericAPIResponse;
import se.winterei.rtraffic.libs.generic.CommentData;
import se.winterei.rtraffic.libs.generic.FirebaseToken;
import se.winterei.rtraffic.libs.generic.Point;
import se.winterei.rtraffic.libs.generic.PointDataStore;
import se.winterei.rtraffic.libs.generic.Report;
import se.winterei.rtraffic.libs.generic.Utility;
import se.winterei.rtraffic.libs.map.MapChangeListener;
import se.winterei.rtraffic.libs.map.MapContainer;
import se.winterei.rtraffic.libs.search.SearchFeedResultsAdapter;
import se.winterei.rtraffic.libs.tasks.AsyncMarkerStateUpdater;

public class MainActivity extends BaseActivity
        implements OnMapReadyCallback, LocationListener
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private MapContainer mapContainer;
    private MainActivity instance = this;
    private LocationManager locationManager;
    private SupportMapFragment fragment;
    private Snackbar snackbar;
    private List<Point> pointList;
    private SearchFeedResultsAdapter searchFeedResultsAdapter;
    private final String[] columns = new String[]{"_id", "title", "position"};
    private boolean FCMFailureToastShown = false;


    @SuppressLint("UseSparseArrays")
    private final HashMap<Integer, Marker> searchPositionMap = new HashMap<>();

    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void onCreate(Bundle savedInstanceState)
    {
        super.setChild(instance);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar(null);
        setupNavigationView();
        setupFloatingActionButton();

        fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (savedInstanceState == null)
            fragment.getMapAsync(this);

        if(checkGPSPermissions())
        {
            String preferredProvider = (String) preference.get("pref_location_provider", LocationManager.NETWORK_PROVIDER, String.class);
            String provider;

            if (preferredProvider.equals("2"))
                provider = LocationManager.NETWORK_PROVIDER;
            else
                provider = LocationManager.GPS_PROVIDER;

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(provider, Utility.LOCATION_LOCK_MIN_TIME, Utility.LOCATION_LOCK_MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER

            Utility.scheduleAlarm(preference);
        }

        reconcileFirebaseMessagingState();
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void resumeAfterPermissionGranted ()
    {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Utility.LOCATION_LOCK_MIN_TIME, Utility.LOCATION_LOCK_MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
        mapContainer.getMap().setMyLocationEnabled(true);

        Utility.scheduleAlarm(preference);

        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();

        if (window == null || layoutParams == null)
            return;

        layoutParams.dimAmount =  0.00f;
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(layoutParams);
    }

    private void setupFloatingActionButton ()
    {
        final FloatingActionButton action_report_traffic = (FloatingActionButton) findViewById(R.id.action_report_traffic);
        action_report_traffic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(instance, TrafficReportActivity.class));
            }
        });
    }

    public final void setupSearchBar (Menu menu)
    {
        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();

        searchView.setOnQueryTextListener
        (
            new SearchView.OnQueryTextListener ()
            {
                @Override
                public boolean onQueryTextSubmit (String query)
                {
                    filterMarkers(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange (String query)
                {
                    if (query.length() >= 3)
                    {
                        filterMarkers(query);
                    }
                    return true;
                }
            }
        );

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener()
        {
            @Override
            public boolean onSuggestionSelect(int position)
            {
                focusMarker(searchView, position);
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position)
            {
                focusMarker(searchView, position);
                return true;
            }
        });

        searchFeedResultsAdapter = new SearchFeedResultsAdapter (instance, R.layout.search_suggestions, null, columns, null, -1000);

        searchView.setSuggestionsAdapter(searchFeedResultsAdapter);
    }

    private void focusMarker (SearchView searchView, int position)
    {
        Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
        int index = Integer.parseInt(cursor.getString(2));

        Marker marker = searchPositionMap.get(index);
        if(marker != null)
        {
            mapContainer.getMap()
                    .animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            marker.showInfoWindow();
            searchView.setQuery(marker.getTitle(), false);
        }

        searchView.clearFocus();
    }

    private void filterMarkers (String searchText)
    {
        List<Marker> results;
        searchText = searchText.toLowerCase(Locale.getDefault());

        if (searchText.isEmpty())
        {
            results = mapContainer.getMarkerList();
        }
        else
        {
            results = new ArrayList<>();
            for (Marker marker : mapContainer.getMarkerList())
            {
                if(marker.getTitle().toLowerCase(Locale.getDefault()).contains(searchText))
                {
                    results.add(marker);
                }
            }
        }

        if (results.size() > 0)
        {
            MatrixCursor matrixCursor = new MatrixCursor(columns);
            searchPositionMap.clear();
            int index = 0;
            for (Marker marker : results)
            {
                String[] tmp = new String[]{ Integer.toString(0), marker.getTitle(), Integer.toString(index) };
                searchPositionMap.put(index, marker);
                matrixCursor.addRow(tmp);
                index++;
            }
            searchFeedResultsAdapter.changeCursor(matrixCursor);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_titlebar, menu);
        setupSearchBar(menu);

        return true;
    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    @SuppressWarnings({"MissingPermission", "unchecked"})
    public void onMapReady(GoogleMap googleMap)
    {
        Log.d(TAG, "onMapReady: called");

        mapContainer = new MapContainer(googleMap, new GoogleMap.OnInfoWindowLongClickListener()
        {
            @Override
            public void onInfoWindowLongClick(Marker marker)
            {
                final List<String> comments = (List<String>) marker.getTag();
                if (comments != null && comments.size() > 0)
                {
                    ArrayList<CommentData> commentDataList = new ArrayList<>();

                    for (String comment : comments)
                    {
                        commentDataList.add(new CommentData(comment, ""));
                    }

                    Intent tmpIntent = new Intent(MainActivity.this, CommentOverlay.class);
                    tmpIntent.putExtra("data", commentDataList);
                    startActivity(tmpIntent);
                }
                else
                    showToast(R.string.main_comment_ui_no_comments_available, Toast.LENGTH_SHORT);
            }
        });

        appContext.put("MainMapContainer", mapContainer);

        snackbar = showSnackbar(fragment.getView(), R.string.loading_main, Snackbar.LENGTH_INDEFINITE);

        if (checkGPSPermissions())
           mapContainer.getMap().setMyLocationEnabled(true);

        synchronizeMap();
    }

    //FAR TOO COMPUTATIONALLY HEAVY EVEN WITH ASYNC MODE DUE TO markers and polylines being UI objects which cannot be shoved background
    //TODO: FIX ^
    public void refreshMarkerStates ()
    {
        new AsyncMarkerStateUpdater(instance, mapContainer).execute();
    }


    private void synchronizeMap ()
    {
        mapContainer.clear();

        Call<List<Point>> call = api.getPoints();
        call.enqueue(new Callback<List<Point>>()
        {
            @Override
            public void onResponse(Call<List<Point>> call, Response<List<Point>> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    pointList = response.body();
                    mapContainer.addPointsWithoutObserverNotify(pointList);
                }
                else
                {
                        showToast(R.string.req_failed_outdated_data, Toast.LENGTH_SHORT);
                        pointList = new PointDataStore().getPoints();
                        mapContainer.addPointsWithoutObserverNotify(pointList);
                }
                appContext.put("MainMapPointList", pointList);
                fetchReports();
            }

            @Override
            public void onFailure(Call<List<Point>> call, Throwable t)
            {
                showToast(R.string.req_failed_outdated_data, Toast.LENGTH_SHORT);
                pointList = new PointDataStore().getPoints();
                mapContainer.addPointsWithoutObserverNotify(pointList);
                fetchReports();
            }
        });
    }

    private void fetchReports ()
    {
        String hours = (String) preference.get("pref_report_history", "1", String.class);

        Call<List<Report>> call = api.getReports(hours);
        call.enqueue(new Callback<List<Report>>()
        {
            @Override
            public void onResponse(Call<List<Report>> call, Response<List<Report>> response)
            {
                parseReportsAndUpdateMap(response.body(), false);
                mapContainer.registerListener(new MapChangeListener()
                {
                    @Override
                    public void onPolylineAdded(Polyline polyline)
                    {
                        refreshMarkerStates();
                    }

                    @Override
                    public void onMarkerAdded(Marker marker)
                    {
                        refreshMarkerStates();
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Report>> call, Throwable t)
            {
                Log.d(TAG, "Retrofit onFailure: " + t.toString());
                showToast(R.string.main_reports_something_went_wrong, Toast.LENGTH_SHORT);
            }
        });
    }

    private void parseReportsAndUpdateMap (List<Report> reports, boolean clearPolyLines)
    {
        if(reports == null || reports.size() == 0)
            return;
        mapContainer.disableObservers();

        if (clearPolyLines)
            mapContainer.clearPolylines();

        for (Report report : reports)
        {
            int color;
            switch (report.severity)
            {
                case Utility.CONGESTED:
                    color = Color.RED;
                    break;
                case Utility.SLOW_BUT_MOVING:
                    color = Color.YELLOW;
                    break;
                case Utility.UNCONGESTED:
                    color = Color.GREEN;
                    break;
                default:
                    continue;
            }

            PolylineOptions tmp = DirectionConverter.createPolyline(this, (ArrayList<LatLng>) report.polypoints, Utility.MAIN_MAP_POLYLINE_WIDTH, color);
            String tmpComment = report.comment + "\n\nSent " + report.created_at;

            mapContainer.addPolyline(tmp, report.severity, tmpComment);
        }
        mapContainer.enableObservers();
        refreshMarkerStates();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
        dismissSnackbar(snackbar);
        GoogleMap map = mapContainer.getMap();

        if (map != null)
            map.animateCamera(cameraUpdate);

        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_refresh:
                synchronizeMap();
                showToast(R.string.update_underway, Toast.LENGTH_SHORT);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void reconcileFirebaseMessagingState ()
    {
        if ((Boolean) preference.get("FIREBASE_UPDATE_NEEDED", false, Boolean.class))
        {
            String token = (String) preference.get("FIREBASE_INSTANCE_ID", "", String.class);
            if (token != null && ! token.equals(""))
            {
                api.updateFirebaseToken(new FirebaseToken(token)).enqueue(new Callback<GenericAPIResponse>()
                {
                    @Override
                    public void onResponse(Call<GenericAPIResponse> call, Response<GenericAPIResponse> response)
                    {
                        if (response.isSuccessful())
                        {
                            if ((Boolean) preference.get("FIREBASE_UPDATE_ATTEMPTED", false, Boolean.class))
                            {
                                showToast(R.string.firebase_token_attempt_success, Toast.LENGTH_SHORT);
                            }
                            preference.put("FIREBASE_UPDATE_NEEDED", false, Boolean.class);
                        }
                        else
                        {
                            if (! FCMFailureToastShown)
                            {
                                showToast(R.string.firebase_token_update_failure, Toast.LENGTH_SHORT);
                                FCMFailureToastShown = true;
                                preference.put("FIREBASE_UPDATE_ATTEMPTED", true, Boolean.class);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<GenericAPIResponse> call, Throwable t)
                    {
                        if (! FCMFailureToastShown)
                        {
                            showToast(R.string.firebase_token_update_failure, Toast.LENGTH_SHORT);
                            FCMFailureToastShown = true;
                        }
                    }
                });
            }
        }
    }
}
