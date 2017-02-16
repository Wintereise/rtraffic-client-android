package se.winterei.rtraffic.activities;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import se.winterei.rtraffic.R;
import se.winterei.rtraffic.RTraffic;
import se.winterei.rtraffic.libs.generic.Point;
import se.winterei.rtraffic.libs.generic.PointDataStore;


public class MainActivity extends BaseActivity
        implements OnMapReadyCallback, View.OnClickListener, LocationListener
{
    private GoogleMap mMap;
    private RTraffic appContext;
    private MainActivity instance = this;
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private SupportMapFragment fragment;
    private Snackbar snackbar;
    private List<Point> pointList;
    private List<Marker> markerList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar(null);
        setupNavigationView();
        setupFloatingActionButton();

        pointList = new PointDataStore().getPoints();
        markerList = new ArrayList<>();

        appContext = (RTraffic) getApplicationContext();

        fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        fragment.getMapAsync(this);

        if(checkGPSPermissions())
        {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
        }

    }

    private final void setupFloatingActionButton ()
    {
        final FloatingActionButton action_report_info = (FloatingActionButton) findViewById(R.id.action_report_info);
        final FloatingActionButton action_report_traffic = (FloatingActionButton) findViewById(R.id.action_report_traffic);
        final FloatingActionsMenu fab_report = (FloatingActionsMenu) findViewById(R.id.fab_report) ;
        action_report_info.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fab_report.collapse();
                //proc the appropriate activity via intents here
            }
        });

        action_report_traffic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fab_report.collapse();
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
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange (String newText)
                            {
                                //map filtering logic along with Google Maps API goes here
                                return true;
                            }
                        }
                );
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
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        appContext.put("GMap", mMap);
        int markerType = 0;

        // Ghetto, shameful marker implementation to get by for now.
        if (pointList.size() > 0)
        {
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
                markerList.add(mMap.addMarker(markerOptions));

            }
        }

        snackbar = showSnackbar(fragment.getView(), R.string.loading_main, Snackbar.LENGTH_INDEFINITE);

        if (checkGPSPermissions())
           mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onClick (View view)
    {
        switch (view.getId())
        {
            case R.id.fab_report:
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
        dismissSnackbar(snackbar);
        mMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }

}
