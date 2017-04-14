package se.winterei.rtraffic.activities;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

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
    private SparseArray<Circle> circleMap;
    private Moshi moshi;
    private SparseIntArray excludedRegionIdToDataSetIndexMap;
    private List<PatternItem> patternItemList;
    private Location currentLocation;
    private boolean requestedLocationUpdates = false;

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
        circleMap = new SparseArray<>();
        excludedRegionIdToDataSetIndexMap = new SparseIntArray();

        patternItemList = new ArrayList<>();
        patternItemList.add(new Dot());

        moshi = new Moshi.Builder().build();

        fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        fragment.getMapAsync(this);

        if(checkGPSPermissions())
        {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            currentLocation = Utility.getCachedLocationOrRegisterForLocationUpdates(appContext, preference, locationManager, this);

            if (currentLocation == null)
                requestedLocationUpdates = true;
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
                        final Circle bCircle = circleMap.get(id);

                        if(row != null)
                            mapArrayList.remove(row);

                        if(bMarker != null)
                            bMarker.remove();

                        if (bCircle != null)
                            bCircle.remove();

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

                                Integer removalIndex = excludedRegionIdToDataSetIndexMap.get(id);
                                if (removalIndex != null)
                                {
                                    int value = removalIndex;
                                    dataset.remove(value);
                                }

                                String jsonRegions = moshi.adapter(Types.newParameterizedType(List.class, ExcludedRegion.class)).toJson(dataset);
                                preference.put(TAG, jsonRegions, String.class);

                                showToast(R.string.excluded_regions_successful_deletion, Toast.LENGTH_SHORT);
                                dynamicizeListView(listView, 1);
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

                String jsonRegions = moshi.adapter(List.class).toJson(dataset);
                preference.put(TAG, jsonRegions, String.class);

                for (ExcludedRegion region : regions)
                {
                    final Map<String, Object> map = new HashMap<>();
                    map.put("title", region.title);
                    map.put("id", String.valueOf(region.id));

                    Marker marker = mapContainer.addMarker(new MarkerOptions().position(region.location).title(region.title));
                    Circle circle = mapContainer.addCircle(new CircleOptions().center(region.location).radius(Utility.exclusionRadius).strokeColor(Color.RED).strokePattern(patternItemList));

                    markerMap.append(region.id, marker);
                    circleMap.append(region.id, circle);

                    listViewIndexMap.append(region.id, map);
                    excludedRegionIdToDataSetIndexMap.append(region.id, dataset.indexOf(region));
                    mapArrayList.add(map);
                }
                simpleAdapter.notifyDataSetChanged();
                dynamicizeListView(listView, 1);
            }

            @Override
            public void onFailure(Call<List<ExcludedRegion>> call, Throwable t)
            {
                Log.d(TAG, "onFailure: " + t.toString());
                showToast(R.string.err_inet_could_not_connect, Toast.LENGTH_SHORT);
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

        showToast(R.string.excluded_regions_startup_tooltip, Toast.LENGTH_LONG);

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
                                        if(response.isSuccessful() && response.body() != null && response.body().data != null)
                                        {
                                            final Marker marker = mapContainer.addMarker(new MarkerOptions().position(latLng));
                                            final Circle circle = mapContainer.addCircle(new CircleOptions().center(latLng).radius(Utility.exclusionRadius).strokeColor(Color.RED).strokePattern(patternItemList));

                                            point.id =  response.body().data.id;

                                            markerMap.append(point.id, marker);
                                            circleMap.append(point.id, circle);

                                            final Map<String, Object> map = new HashMap<>();
                                            dataset.add(point);

                                            String jsonRegions = moshi.adapter(List.class).toJson(dataset);
                                            preference.put(TAG, jsonRegions, String.class);

                                            marker.setTitle(point.title);

                                            map.put("title", point.title);
                                            map.put("id", String.valueOf(point.id));

                                            mapArrayList.add(map);
                                            listViewIndexMap.append(point.id, map);
                                            excludedRegionIdToDataSetIndexMap.append(point.id, dataset.indexOf(point));

                                            simpleAdapter.notifyDataSetChanged();
                                            showToast(R.string.entry_submit, Toast.LENGTH_SHORT);
                                            dynamicizeListView(listView, 1);
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
                                        showToast(R.string.err_inet_could_not_connect, Toast.LENGTH_SHORT);
                                    }
                                });
                            }
                        })
                        .show();
            }
        });
        fetchAndUpdateExclusions();

        if (currentLocation != null)
            onLocationChanged(currentLocation);
    }

    @Override
    public void onLocationChanged(Location location)
    {
        appContext.put("lastLocation", location);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        dismissSnackbar(snackbar);

        if (mapContainer != null)
        {
            GoogleMap map = mapContainer.getMap();
            if (map != null)
                map.animateCamera(cameraUpdate);
        }

        if (requestedLocationUpdates)
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

    public void dynamicizeListView (ListView listView, float threshold)
    {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        float val = (float) (listAdapter.getCount() * 0.11);
        if (val > threshold)
            return;

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) listView.getLayoutParams();
        layoutParams.weight = val;

        listView.setLayoutParams(layoutParams);
        listView.requestLayout();
    }

    @Override
    public void onDestroy ()
    {
        if (progressDialog != null)
            progressDialog.dismiss();

        super.onDestroy();
    }
}