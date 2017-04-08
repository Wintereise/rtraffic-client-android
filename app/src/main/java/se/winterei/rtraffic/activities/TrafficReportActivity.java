package se.winterei.rtraffic.activities;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.winterei.rtraffic.R;
import se.winterei.rtraffic.RTraffic;
import se.winterei.rtraffic.libs.generic.Report;
import se.winterei.rtraffic.libs.generic.Utility;
import se.winterei.rtraffic.libs.map.MapContainer;

public class TrafficReportActivity extends BaseActivity
        implements OnMapReadyCallback, DirectionCallback, LocationListener, GoogleMap.OnPolylineClickListener
{
    private final String TAG = TrafficReportActivity.class.getSimpleName();
    private GoogleMap mMap;
    private MapContainer mapContainer, mainMap;
    private RTraffic appContext;
    private TrafficReportActivity instance = this;
    private ArrayList<LatLng> markerPoints = new ArrayList<>();
    LatLng src, dst;
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private SupportMapFragment fragment;
    private Snackbar snackbar;
    private List<Polyline> polylineList = new ArrayList<>();
    private List<PolylineOptions> polylineOptionsList = new ArrayList<>();
    private int polyLineIndex = 0;
    private Random rnd = new Random();
    private static final int MENU_CLEAR = 500;
    private boolean showPolyLineHelp = true;
    private int congestionChoiceID;
    private EditText commentInput;
    private CheckBox checkBoxInput;
    private ProgressDialog progressDialog;
    private IconGenerator iconGenerator;

    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_report);

        showPolyLineHelp = true;

        setupToolbar(null);
        setupNavigationView();

        appContext = (RTraffic) getApplicationContext();

        mainMap = (MapContainer) appContext.get("MainMapContainer");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        fragment.getMapAsync(this);

        if(checkGPSPermissions())
        {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
        }

        iconGenerator = new IconGenerator(instance);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        genericFixToolbar(menu);

        menu.add(0, MENU_CLEAR, Menu.NONE, R.string.clear).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId())
        {
            case MENU_CLEAR:
                if(mapContainer != null)
                    mapContainer.clear();
                dismissSnackbar(snackbar);
                markerPoints.clear();
                polyLineIndex = 0;
                showToast(R.string.traffic_report_toast_clear_markers, Toast.LENGTH_SHORT);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void radioButtonClicked (View view)
    {
        boolean checked = ((RadioButton) view).isChecked();
    }

    public void showReportDialog ()
    {
        dismissSnackbar(snackbar);
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.traffic_report_dialog_compose_report)
                .customView(R.layout.dialog_traffic_report, true)
                .positiveText(R.string.submit)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
                    {
                        int color = -1, state = -1;
                        switch (congestionChoiceID)
                        {
                            case R.id.fullyCongested:
                                color = Color.RED;
                                state = Utility.CONGESTED;
                                break;
                            case R.id.moderatelyCongested:
                                color = Color.YELLOW;
                                state = Utility.SLOW_BUT_MOVING;
                                break;
                            case R.id.notCongested:
                                color = Color.GREEN;
                                state = Utility.UNCONGESTED;
                                break;
                            default:
                                showSnackbar(fragment.getView(), R.string.something_went_wrong, Snackbar.LENGTH_SHORT);
                        }
                        if (color != -1 && state != -1)
                        {
                            PolylineOptions polylineOptions = polylineOptionsList.get(polyLineIndex);
                            polylineOptions.width(Utility.MAIN_MAP_POLYLINE_WIDTH);
                            Polyline polyline = mainMap.addPolyline(polylineOptions, state, commentInput.getText().toString());
                            polyline.setColor(color);
                            progressDialog = ProgressDialog.show(instance, "", getString(R.string.loading), true);

                            Call<Report> call = api.postReport(new Report (-1, state, commentInput.getText().toString(), checkBoxInput.isChecked(), polyline.getPoints()));
                            call.enqueue(new Callback<Report>() {
                                @Override
                                public void onResponse(Call<Report> call, Response<Report> response)
                                {
                                    progressDialog.dismiss();
                                    showToast(R.string.traffic_report_thank_you, Toast.LENGTH_SHORT);
                                    finish();
                                }

                                @Override
                                public void onFailure(Call<Report> call, Throwable t)
                                {
                                    progressDialog.dismiss();
                                    showToast(R.string.err_inet_could_not_connect, Toast.LENGTH_SHORT);
                                }
                            });
                        }


                    }
                }).build();

        commentInput = (EditText)  dialog.getCustomView().findViewById(R.id.comment);
        checkBoxInput = (CheckBox) dialog.getCustomView().findViewById(R.id.anonymous_report);

        final View positiveAction =  dialog.getActionButton(DialogAction.POSITIVE);
        RadioGroup radioGroup  = (RadioGroup) dialog.getCustomView().findViewById(R.id.congestionPicker);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                positiveAction.setEnabled(true);
                RadioButton tmp = (RadioButton) group.findViewById(checkedId);
                if(tmp != null && tmp.isChecked())
                {
                    congestionChoiceID = checkedId;
                }
            }
        });

        positiveAction.setEnabled(false);
        dialog.show();
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
    @SuppressWarnings({"MissingPermission"})
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mapContainer = new MapContainer(googleMap);

        appContext.put("TrafficReportGMap", mMap);

        snackbar = showSnackbar(fragment.getView(), R.string.loading_main, Snackbar.LENGTH_INDEFINITE);

        if (checkGPSPermissions())
            mMap.setMyLocationEnabled(true);

        mMap.setOnPolylineClickListener(this);



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



                /**
                 * For the start location, the color of marker is GREEN and
                 * for the end location, the color of marker is RED.
                 */
                if (markerPoints.size() == 1)
                {
                    iconGenerator.setStyle(IconGenerator.STYLE_GREEN);
                    mapContainer.addInfoMarker(iconGenerator, getString(R.string.start_map), point);
                    showToast(R.string.traffic_report_second_instruction, Toast.LENGTH_SHORT);
                }
                else if (markerPoints.size() == 2)
                {
                    iconGenerator.setStyle(IconGenerator.STYLE_RED);
                    mapContainer.addInfoMarker(iconGenerator, getString(R.string.end_map), point);
                }
                //mMap.addMarker(options);
                if (markerPoints.size() == 2)
                {
                    src = markerPoints.get(0);
                    dst = markerPoints.get(1);
                    snackbar = showSnackbar(fragment.getView(), R.string.loading, Snackbar.LENGTH_INDEFINITE);
                    GoogleDirection.withServerKey(getString(R.string.google_directions_api))
                            .from(src)
                            .to(dst)
                            .transitMode(TransportMode.DRIVING)
                            .alternativeRoute(true)
                            .execute(instance);
                }
            }
        });
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody)
    {
        dismissSnackbar(snackbar);
        mapContainer.clear();
        polylineList.clear();
        polylineOptionsList.clear();


        if (direction.isOK())
        {
            iconGenerator.setStyle(IconGenerator.STYLE_GREEN);
            mapContainer.addInfoMarker(iconGenerator, getString(R.string.start_map), src);

            iconGenerator.setStyle(IconGenerator.STYLE_RED);
            mapContainer.addInfoMarker(iconGenerator, getString(R.string.end_map), dst);

            List<Route> routes = direction.getRouteList();

            int size = routes.size();

            for (int i = 0; i < size; i++)
            {
                Route route = routes.get(i);
                ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
                PolylineOptions polylineOptions = DirectionConverter.createPolyline(this, directionPositionList, 5, Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
                Polyline polyline = mMap.addPolyline(polylineOptions);
                polyline.setClickable(true);
                polylineList.add(polyline);
                polylineOptionsList.add(polylineOptions);
            }

            if (size == 1)
            {
                showToast(R.string.traffic_report_singlepath, Toast.LENGTH_SHORT);
                onPolylineClick(polylineList.get(0));
            }
            else
            {
                showToast(getString(R.string.traffic_report_multipath_chooser, routes.size()), Toast.LENGTH_SHORT);
            }

        }
        else
        {
            Log.d(TAG, "onDirectionSuccess: " + direction.getErrorMessage());
            showSnackbar(fragment.getView(), R.string.something_went_wrong, Snackbar.LENGTH_SHORT);
        }

    }

    @Override
    public void onPolylineClick(Polyline polyline)
    {
        polyLineIndex =  polylineList.indexOf(polyline);
        for (Polyline line : polylineList)
        {
            line.setColor(Color.GRAY);
            line.setWidth(Utility.dpToPx(this, 3));
        }
        polyline.setColor(ContextCompat.getColor(this, R.color.accent_light));
        polyline.setWidth(Utility.dpToPx(this, 5));
        if(showPolyLineHelp)
        {
            showToast(R.string.traffic_report_polyline_select, Toast.LENGTH_SHORT);
            showPolyLineHelp = false;
        }
        snackbar = Snackbar.make(fragment.getView(), R.string.traffic_report_snackbar_confirm_report, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.confirm, new View.OnClickListener() {
            @Override
            public void onClick (View view)
            {
                showReportDialog();
            }
        });
        snackbar.show();
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
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        dismissSnackbar(snackbar);
        mMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);
        showToast(R.string.traffic_report_initial_instructions, Toast.LENGTH_LONG);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }

    @Override
    public void onBackPressed ()
    {
        finish();
    }

    @Override
    public void onDestroy ()
    {
        if (progressDialog != null)
            progressDialog.dismiss();

        super.onDestroy();
    }
}
