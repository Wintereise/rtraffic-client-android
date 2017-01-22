package se.winterei.rtraffic;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, View.OnClickListener
{
    private GoogleMap mMap;
    private RTraffic appContext;

    DrawerLayout drawerLayout;

    private void setupToolbar ()
    {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_generic);
        setSupportActionBar(myToolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setupSearchBar (Menu menu)
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

    private void setupNavigationView ()
    {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    }

    private void setupFloatingActionButton ()
    {
        FloatingActionButton fab_report = (FloatingActionButton) findViewById(R.id.fab_report);
        if (fab_report != null)
            fab_report.setOnClickListener(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNavigationView();
        setupToolbar();
        setupFloatingActionButton();

        appContext = (RTraffic) getApplicationContext();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_titlebar, menu);
        setupSearchBar(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_search:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_refresh:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            case android.R.id.home:
                if (drawerLayout != null)
                {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
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

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(23.794847, 90.414213), 15));

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
}
