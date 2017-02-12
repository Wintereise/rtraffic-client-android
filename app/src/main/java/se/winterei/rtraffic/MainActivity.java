package se.winterei.rtraffic;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
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
import com.google.android.gms.maps.model.MarkerOptions;


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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar(null);
        setupNavigationView();
        setupFloatingActionButton();

        appContext = (RTraffic) getApplicationContext();

        fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        fragment.getMapAsync(this);

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

        // Ghetto, shameful marker implementation to get by for now.
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.794403, 90.401070)).title("Airport Road (Dhaka-Mymensingh Hwy) and Kemal Ataturk Avenue").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_green)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.850420, 90.408418)).title("Airport Road Roundabout").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_green)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.746015, 90.394651)).title("Bangla Motor Mor").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_green)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.743643, 90.382264)).title("Dhanmondi 6").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_green)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.780351, 90.416731)).title("Gulshan Circle 1").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_green)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.873833, 90.400593)).title("Uttara Housebuilding").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_green)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.775280, 90.389939)).title("Jahangir Gate").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_red)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.737625, 90.405229)).title("Kakrail Circle").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_red)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.737570, 90.409018)).title("Kakrail Road").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_red)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.738111, 90.395851)).title("Kazi Nazrul Islam Avenue & Shahbagh").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_red)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.764426, 90.389003)).title("Kazi Nazrul Islam Avenue & Bijoy Sharani").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_red)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.758477, 90.389871)).title("Kazi Nazrul Islam Avenue & Indira Road").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_red)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.758442, 90.383746)).title("Khamar Bari Gol Chottor").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_red)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.828733, 90.420070)).title("Khilkhet").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_green)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.744144, 90.414286)).title("Malibag Mor").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_green)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.750099, 90.413043)).title("Malibag Rail Gate").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_green)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.760161, 90.372976)).title("Mirpur Road & Asad Avenue").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_green)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.738768, 90.383448)).title("Mirpur Road & Elephant Road").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_green)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.758307, 90.374220)).title("Mirpur Road & Manik Mia Avenue").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_green)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.756349, 90.375102)).title("Mirpur Road & Old Dhanmondi 27/New 16").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_green)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.778311, 90.397932)).title("Mohakhali Chourasta").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_green)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.745760, 90.412240)).title("Mouchak").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_yellow)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.751346, 90.378314)).title("Panthapath & Mirpur Road").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_yellow)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.767700, 90.423000)).title("Rampura Bridge - DIT road <-> Hatirjheel").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_yellow)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.749859, 90.393158)).title("SAARC Fountain (Sonargaon, Bashundhara City Shopping Complex)").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_yellow)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.741595, 90.411856)).title("Shantinagar Mor").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_yellow)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.768240, 90.382861)).title("Zia Udyan").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_yellow)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.738348, 90.372999)).title("Zigatala").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_yellow)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.794847, 90.414213)).title("Gulshan Circle 2").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_traffic_black_green)));

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
