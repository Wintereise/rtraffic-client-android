package se.winterei.rtraffic.activities;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import se.winterei.rtraffic.R;
import se.winterei.rtraffic.RTraffic;
import se.winterei.rtraffic.libs.generic.Utility;

public class TrafficReportActivity extends BaseActivity
        implements OnMapReadyCallback, View.OnClickListener, DirectionCallback, LocationListener, GoogleMap.OnPolylineClickListener
{
    private GoogleMap mMap, mainMap;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_report);

        showPolyLineHelp = true;

        setupToolbar(null);
        setupNavigationView();

        appContext = (RTraffic) getApplicationContext();
        mainMap = (GoogleMap) appContext.get("GMap");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        fragment.getMapAsync(this);

        if(checkGPSPermissions())
        {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
        }

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
                if(mMap != null)
                    mMap.clear();
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
                        Polyline polyline = mainMap.addPolyline(polylineOptionsList.get(polyLineIndex));
                        switch (congestionChoiceID)
                        {
                            case R.id.fullyCongested:
                                polyline.setColor(Color.RED);
                                break;
                            case R.id.moderatelyCongested:
                                polyline.setColor(Color.YELLOW);
                                break;
                            case R.id.notCongested:
                                polyline.setColor(Color.GREEN);
                                break;
                            default:
                                showSnackbar(fragment.getView(), R.string.something_went_wrong, Snackbar.LENGTH_SHORT);
                        }
                        showToast(R.string.traffic_report_thank_you, Toast.LENGTH_SHORT);
                        finish();
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
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
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
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(point);

                /**
                 * For the start location, the color of marker is GREEN and
                 * for the end location, the color of marker is RED.
                 */
                if (markerPoints.size() == 1)
                {
                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_marker));
                    showToast(R.string.traffic_report_second_instruction, Toast.LENGTH_SHORT);
                }
                else if (markerPoints.size() == 2)
                {
                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_end_marker));
                }
                mMap.addMarker(options);
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
        dismissSnackbar(snackbar);
        mMap.clear();
        polylineList.clear();
        polylineOptionsList.clear();


        if (direction.isOK())
        {
            mMap.addMarker(new MarkerOptions().position(src).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_marker)));
            mMap.addMarker(new MarkerOptions().position(dst).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_end_marker)));

            List<Route> routes = direction.getRouteList();

            showToast(getString(R.string.traffic_report_multipath_chooser, routes.size()), Toast.LENGTH_SHORT);

            for (int i = 0; i < routes.size(); i++)
            {
                Route route = routes.get(i);
                ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
                PolylineOptions polylineOptions = DirectionConverter.createPolyline(this, directionPositionList, 5, Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
                Polyline polyline = mMap.addPolyline(polylineOptions);
                polyline.setClickable(true);
                polylineList.add(polyline);
                polylineOptionsList.add(polylineOptions);
            }
        }
        else
            showSnackbar(fragment.getView(), R.string.something_went_wrong, Snackbar.LENGTH_SHORT);
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
            showToast(R.string.traffic_report_polyline_select, Toast.LENGTH_LONG);
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
}