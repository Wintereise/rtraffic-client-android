package se.winterei.rtraffic;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by pinku on 2/4/17.
 */

public class MyLocationActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {
    /*
    * after delete,
    * clean build.gradle
    * clean AndroidManifest
    * clean @string resource
    * clean layout
    *
    * */
    private static final String TAG = "MyLocationActivity";

    private TextView textView;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mylocation);
        setupToolbar(null);
        setupNavigationView();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        textView = (TextView) findViewById(R.id.locationOutput);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        genericFixToolbar(menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {

        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);  //update location every 40 second

        /*
        * NOTE:
        * without casting, error
        * after casting, ACCESS_FINE_LOCATION permission check as auto-added
        * */
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, this);
        //LocationServices.FusedLocationApi.requestLocationUpdates(API cient that we are using,
        //             the location-request that we just set up, the class to receive callbacks);


    }

    //include LocationListener in the implement section of the class for the callback to work
    @Override
    public void onLocationChanged(Location location){ //callback for LocationServices,FusedLocationApi...(...,...,this);
        Log.i(TAG, "onLocationChanged: "+location.toString());
        textView.setText("Latitude:"+Double.toString(location.getLatitude())
                +"\nLongitude:"+Double.toString(location.getLongitude())
                +"\nAltitude:"+Double.toString(location.getAltitude())
                +"\nSpeed:"+Double.toString(location.getSpeed()));

        //textView.setText(location.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: start");
        Log.d(TAG, "onConnectionSuspended: stop");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: start");
        Log.d(TAG, "onConnectionFailed: stop");
    }
}
