package se.winterei.rtraffic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainActivity extends BaseActivity
        implements OnMapReadyCallback, View.OnClickListener, DirectionCallback, LocationListener
{
    private GoogleMap mMap;
    private RTraffic appContext;
    private MainActivity instance = this;
    private ArrayList<LatLng> markerPoints = new ArrayList<>();
    LatLng src, dst;
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar(null);
        setupNavigationView();
        setupFloatingActionButton();

        appContext = (RTraffic) getApplicationContext();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(checkGPSPermissions())
        {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
        }

    }

    private final void setupFloatingActionButton ()
    {
        final FloatingActionButton action_report_info = (FloatingActionButton) findViewById(R.id.action_report_info);
        final FloatingActionButton action_report_traffic = (FloatingActionButton) findViewById(R.id.action_report_traffic);
        action_report_info.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //proc the appropriate activity via intents here
            }
        });

        action_report_traffic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(instance, TrafficReportActivity.class));
            }
        });
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

        // Ghetto, shameful marker implementation to get by for now.
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.794403, 90.401070)).title("Airport Road (Dhaka-Mymensingh Hwy) and Kemal Ataturk Avenue"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.850420, 90.408418)).title("Airport Road Roundabout"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.746015, 90.394651)).title("Bangla Motor Mor"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.743643, 90.382264)).title("Dhanmondi 6"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.780351, 90.416731)).title("Gulshan Circle 1"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.794847, 90.414213)).title("Gulshan Circle 2").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_18dp)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.873833, 90.400593)).title("Uttara Housebuilding"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.775280, 90.389939)).title("Jahangir Gate"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.737625, 90.405229)).title("Kakrail Circle"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.737570, 90.409018)).title("Kakrail Road"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.738111, 90.395851)).title("Kazi Nazrul Islam Avenue & Shahbagh"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.764426, 90.389003)).title("Kazi Nazrul Islam Avenue & Bijoy Sharani"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.758477, 90.389871)).title("Kazi Nazrul Islam Avenue & Indira Road"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.758442, 90.383746)).title("Khamar Bari Gol Chottor"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.828733, 90.420070)).title("Khilkhet"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.744144, 90.414286)).title("Malibag Mor"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.750099, 90.413043)).title("Malibag Rail Gate"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.760161, 90.372976)).title("Mirpur Road & Asad Avenue"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.738768, 90.383448)).title("Mirpur Road & Elephant Road"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.758307, 90.374220)).title("Mirpur Road & Manik Mia Avenue"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.756349, 90.375102)).title("Mirpur Road & Old Dhanmondi 27/New 16\n"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.778311, 90.397932)).title("Mohakhali Chourasta"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.745760, 90.412240)).title("Mouchak"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.751346, 90.378314)).title("Panthapath & Mirpur Road"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.767700, 90.423000)).title("Rampura Bridge - DIT road <-> Hatirjheel"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.749859, 90.393158)).title("SAARC Fountain (Sonargaon, Bashundhara City Shopping Complex)"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.741595, 90.411856)).title("Shantinagar Mor"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.768240, 90.382861)).title("Zia Udyan"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.738348, 90.372999)).title("Zigatala"));

       if (checkGPSPermissions())
           mMap.setMyLocationEnabled(true);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()
        {
            @Override
            public void onMapLongClick(LatLng point)
            {
                if(markerPoints.size() > 1)
                {
                    markerPoints.clear();
                }
                markerPoints.add(point);
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(point);

                /**
                 * For the start location, the color of marker is GREEN and
                 * for the end location, the color of marker is RED.
                 */
                if (markerPoints.size() == 1)
                {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }
                else if (markerPoints.size() == 2)
                {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                mMap.addMarker(options);
                if (markerPoints.size() == 2)
                {
                    src = markerPoints.get(0);
                    dst = markerPoints.get(1);
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.loading), Snackbar.LENGTH_SHORT).show();
                    GoogleDirection.withServerKey(getString(R.string.google_directions_api))
                            .from(src)
                            .to(dst)
                            .transitMode(TransportMode.DRIVING)
                            .execute(instance);
                }
            }
        });
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
    public void onDirectionSuccess(Direction direction, String rawBody)
    {
        if (direction.isOK())
        {
            //mMap.addMarker(new MarkerOptions().position(src));
            //mMap.addMarker(new MarkerOptions().position(dst));

            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.RED));
        }
    }

    @Override
    public void onDirectionFailure(Throwable t)
    {
        Log.d("Direction API: ", t.getMessage());
    }

    @Override
    public void onLocationChanged(Location location)
    {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
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
