package se.winterei.rtraffic.activities;

import android.app.ProgressDialog;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.winterei.rtraffic.R;
import se.winterei.rtraffic.libs.api.GenericAPIResponse;
import se.winterei.rtraffic.libs.generic.ExcludedRegion;
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
    private List<ExcludedRegion> dataset;
    private List<Map<String, Object>> mapArrayList;
    private ProgressDialog progressDialog;
    private SparseArray<Map<String, Object>> listViewIndexMap;
    private SparseArray<Marker> markerMap;


    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excluded_regions);

        dataset = new ArrayList<>();
        mapArrayList = new ArrayList<>();
        listViewIndexMap = new SparseArray<>();
        markerMap = new SparseArray<>();

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

        for (ExcludedRegion point : dataset)
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
                        progressDialog = ProgressDialog.show(instance, "", getString(R.string.loading), true);
                        progressDialog.show();

                        final int id = Integer.parseInt((String) v.getTag());
                        final Map<String, Object> row = listViewIndexMap.get(id);
                        final Marker bMarker = markerMap.get(id);
                        if(row != null)
                        {
                            mapArrayList.remove(row);
                        }
                        if(bMarker != null)
                        {
                            bMarker.remove();
                        }
                        if(id == -1)
                        {
                            progressDialog.hide();
                            simpleAdapter.notifyDataSetChanged();
                            return;
                        }

                        Call<GenericAPIResponse> call = api.deleteExcludedRegion(id);
                        call.enqueue(new Callback<GenericAPIResponse>()
                        {
                            @Override
                            public void onResponse(Call<GenericAPIResponse> call, Response<GenericAPIResponse> response)
                            {
                                progressDialog.hide();
                                simpleAdapter.notifyDataSetChanged();
                                showToast(R.string.excluded_regions_successful_deletion, Toast.LENGTH_SHORT);
                            }

                            @Override
                            public void onFailure(Call<GenericAPIResponse> call, Throwable t)
                            {
                                progressDialog.hide();
                                showToast(R.string.something_went_wrong, Toast.LENGTH_SHORT);
                            }
                        });
                    }
                });
                return convertView;
            }
        };

        listView.setAdapter(simpleAdapter);


        setupToolbar(null);
        setupNavigationView();
    }

    private void fetchAndUpdateExclusions ()
    {
        Call<List<ExcludedRegion>> call = api.getExcludedRegions();
        call.enqueue(new Callback<List<ExcludedRegion>>()
        {
            @Override
            public void onResponse(Call<List<ExcludedRegion>> call, Response<List<ExcludedRegion>> response)
            {
                List<ExcludedRegion> regions = response.body();
                if(regions == null || regions.size() == 0)
                {
                    Log.d(TAG, "onResponse: empty dataset found or list was null.");
                    return;
                }

                mapArrayList.clear();
                mapContainer.clear();
                listViewIndexMap.clear();
                markerMap.clear();

                dataset = regions;
                simpleAdapter.notifyDataSetChanged();
                for (ExcludedRegion region : regions)
                {
                    final Map<String, Object> map = new HashMap<>();
                    map.put("title", region.title);
                    map.put("id", String.valueOf(region.id));
                    Marker marker = mapContainer.addMarker(new MarkerOptions().position(region.location).title(region.title));
                    markerMap.append(region.id, marker);
                    listViewIndexMap.append(region.id, map);
                    mapArrayList.add(map);
                }
                simpleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<ExcludedRegion>> call, Throwable t)
            {
                Log.d(TAG, "onFailure: " + t.toString());
            }
        });
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
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
                            public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which)
                            {


                                progressDialog = ProgressDialog.show(instance, "", getString(R.string.loading), true);
                                progressDialog.show();

                                final ExcludedRegion point = new ExcludedRegion();
                                point.title = dialog.getInputEditText().getText().toString();
                                point.location = latLng;

                                Call<GenericAPIResponse> call = api.postExcludedRegion(point);
                                call.enqueue(new Callback<GenericAPIResponse>() {
                                    @Override
                                    public void onResponse(Call<GenericAPIResponse> call, Response<GenericAPIResponse> response)
                                    {
                                        progressDialog.dismiss();
                                        if(response.body() != null && response.body().status == 200)
                                        {
                                            final Marker marker = mapContainer.addMarker(new MarkerOptions().position(latLng));
                                            point.id =  response.body().data.id;
                                            final Map<String, Object> map = new HashMap<>();
                                            dataset.add(point);

                                            marker.setTitle(point.title);

                                            map.put("title", point.title);
                                            map.put("id", String.valueOf(point.id));

                                            mapArrayList.add(map);
                                            listViewIndexMap.append(point.id, map);
                                            markerMap.append(point.id, marker);

                                            simpleAdapter.notifyDataSetChanged();
                                            showToast(R.string.entry_submit, Toast.LENGTH_SHORT);
                                        }
                                        else
                                        {
                                            showToast(R.string.something_went_wrong, Toast.LENGTH_SHORT);
                                            Log.d(TAG, "onResponse: PARSE_FAILURE: " + response.message());
                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<GenericAPIResponse> call, Throwable t)
                                    {
                                        progressDialog.dismiss();
                                        Log.d(TAG, "onFailure: " + t.getMessage());
                                        showToast(R.string.something_went_wrong, Toast.LENGTH_SHORT);
                                    }
                                });
                            }
                        })
                        .show();
            }
        });
        fetchAndUpdateExclusions();
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