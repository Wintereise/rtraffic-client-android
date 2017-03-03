package se.winterei.rtraffic.activities;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.winterei.rtraffic.R;
import se.winterei.rtraffic.libs.generic.Point;
import se.winterei.rtraffic.libs.generic.PointDataStore;
import se.winterei.rtraffic.libs.generic.Utility;
import se.winterei.rtraffic.libs.map.MapChangeListener;
import se.winterei.rtraffic.libs.map.MapContainer;

public class ExcludedRegionsActivity extends BaseActivity
    implements OnMapReadyCallback, LocationListener
{
    private final static String TAG = ExcludedRegionsActivity.class.getSimpleName();

    private MapContainer mapContainer;
    private SupportMapFragment fragment;
    private LocationManager locationManager;
    private Snackbar snackbar;
    private ListView listView;
    private SimpleAdapter simpleAdapter;
    private ExcludedRegionsActivity instance = this;
    private List<Point> dataset;
    private List<Map<String, Object>> mapArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excluded_regions);

        dataset = new ArrayList<>();
        mapArrayList = new ArrayList<>();

        fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        fragment.getMapAsync(this);

        if(checkGPSPermissions())
        {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Utility.LOCATION_LOCK_MIN_TIME, Utility.LOCATION_LOCK_MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
        }

        listView = (ListView) findViewById(R.id.ExcludedRegionsListView);

        final String[] from = { "title" };
        final int[] to = { R.id.excludedListViewLabelNoToggle, R.id.listViewTextViewButtonNoToggle };


        listView.setEmptyView(findViewById(R.id.empty_textview));

        for (Point point : dataset)
        {
            final Map<String, Object> map = new HashMap<>();
            map.put("title", point.title);
            map.put("id", String.valueOf(point.id));
            mapArrayList.add(map);
        }

        simpleAdapter = new SimpleAdapter(this, mapArrayList, R.layout.points_listview_non_toggle, from, to)
        {
            @Override
            public View getView (int position, View convertView, ViewGroup parent)
            {
                if (convertView == null)
                {
                    LayoutInflater inflater = getLayoutInflater();
                    convertView = inflater.inflate(R.layout.points_listview_non_toggle, parent, false);
                }
                Map<String, Object> stringObjectMap = mapArrayList.get(position);

                TextView textView = (TextView) convertView.findViewById(R.id.excludedListViewLabelNoToggle);
                textView.setText((String) stringObjectMap.get("title"));

                Button button = (Button) convertView.findViewById(R.id.listViewTextViewButtonNoToggle);
                button.setTag(stringObjectMap.get("id"));

                button.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        showToast((String) v.getTag(), Toast.LENGTH_SHORT);
                    }
                });

                return convertView;
            }
        };

        listView.setAdapter(simpleAdapter);


        setupToolbar(null);
        setupNavigationView();
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
                //simpleAdapter.notifyDataSetChanged();
            }
        });

        mapContainer.getMap().setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()
        {
            @Override
            public void onMapLongClick(final LatLng latLng)
            {
                mapContainer.clear();
                final Marker marker = mapContainer.addMarker(new MarkerOptions().position(latLng));
                final MaterialDialog dialog = new MaterialDialog.Builder(instance)
                        .title(R.string.excluded_regions_dialog_title)
                        .inputRange(3, 30)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(getString(R.string.excluded_regions_dialog_lint), "", new MaterialDialog.InputCallback()
                        {
                            @Override
                            public void onInput (@NonNull MaterialDialog materialDialog, CharSequence input)
                            {
                            }
                        })
                        .positiveText(R.string.submit)
                        .negativeText(R.string.cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback()
                        {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
                            {
                                Point point = new Point(latLng.latitude, latLng.longitude, dialog.getInputEditText().getText().toString(), "");
                                final Map<String, Object> map = new HashMap<>();
                                dataset.add(point);
                                marker.setTitle(point.title);
                                map.put("title", point.title);
                                map.put("id", String.valueOf(point.id));
                                mapArrayList.add(map);
                                simpleAdapter.notifyDataSetChanged();
                            }
                        })
                        .show();
            }
        });

    }

    @Override
    public void onLocationChanged(Location location)
    {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
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