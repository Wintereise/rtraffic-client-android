package se.winterei.rtraffic.activities;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.Menu;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import se.winterei.rtraffic.R;
import se.winterei.rtraffic.libs.generic.Utility;
import se.winterei.rtraffic.libs.map.MapChangeListener;
import se.winterei.rtraffic.libs.map.MapContainer;

public class ExcludedRegionsActivity extends BaseActivity
    implements OnMapReadyCallback, LocationListener
{

    private MapContainer mapContainer;
    private SupportMapFragment fragment;
    private LocationManager locationManager;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excluded_regions);

        setupToolbar(null);
        setupNavigationView();

        fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        fragment.getMapAsync(this);

        if(checkGPSPermissions())
        {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Utility.LOCATION_LOCK_MIN_TIME, Utility.LOCATION_LOCK_MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mapContainer = new MapContainer(googleMap);

        snackbar = showSnackbar(fragment.getView(), R.string.loading_main, Snackbar.LENGTH_INDEFINITE);

        if (checkGPSPermissions())
            mapContainer.getMap().setMyLocationEnabled(true);

        mapContainer.registerListener(new MapChangeListener()
        {
            @Override
            public void onPolylineAdded(Polyline polyline)
            {

            }

            @Override
            public void onMarkerAdded(Marker marker)
            {

            }
        });

    }

    @Override
    public void onLocationChanged(Location location)
    {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
        dismissSnackbar(snackbar);
        mapContainer.getMap()
                .animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        genericFixToolbar(menu);

        return true;
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }
}