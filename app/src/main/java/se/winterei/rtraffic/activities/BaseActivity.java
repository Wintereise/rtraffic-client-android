package se.winterei.rtraffic.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import android.net.Uri;

import io.doorbell.android.Doorbell;
import io.doorbell.android.callbacks.OnFeedbackSentCallback;
import se.winterei.rtraffic.R;
import se.winterei.rtraffic.RTraffic;
import se.winterei.rtraffic.libs.api.APIClient;
import se.winterei.rtraffic.libs.api.APIInterface;
import se.winterei.rtraffic.libs.settings.Preference;

public abstract class BaseActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener
{
    private Toast bToast;

    private int backButtonCount = 0;
    public String userName = "Signed out", userEmail = "test@example.com";
    public Uri photoURI;

    public DrawerLayout drawerLayout;
    public NavigationView navigationView;
    public Toolbar myToolbar;
    public Doorbell doorbell;

    public APIInterface api = APIClient.get()
            .create(APIInterface.class);

    public RTraffic appContext;
    public SharedPreferences preferences;

    public Boolean bypassAuthentication = false;

    private Boolean permissionRequested = false;

    private BaseActivity child;

    public Preference preference;

    public void setChild (BaseActivity child)
    {
        this.child = child;
    }

    public final void setupToolbar (@Nullable View view)
    {
        if(view != null)
            myToolbar = (Toolbar) view.findViewById(R.id.toolbar_generic);
        else
            myToolbar = (Toolbar) findViewById(R.id.toolbar_generic);
        setSupportActionBar(myToolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }



    public final void setupNavigationView ()
    {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        TextView navHeader = (TextView) header.findViewById(R.id.headerUsername);
        navHeader.setSingleLine(false);
        navHeader.setText(userName + "\n" + userEmail);

        if (photoURI != null)
        {
            ImageView imageView = (ImageView) header.findViewById(R.id.profile_image_R8);
            Glide.with(this).load(photoURI.toString()).into(imageView);
        }

    }

    private boolean authCheck ()
    {
        GoogleSignInResult googleSignInResult = (GoogleSignInResult) appContext.get("GSignInResult");
        if(googleSignInResult != null && googleSignInResult.isSuccess())
        {
            GoogleSignInAccount acct = googleSignInResult.getSignInAccount();
            if(acct != null)
            {
                userName = acct.getDisplayName();
                userEmail = acct.getEmail();
                photoURI = acct.getPhotoUrl();
                return true;
            }
            else
                return false;
        }
        else
            return false;
    }

    private void redirectOnAuthFailure ()
    {
        if(!authCheck() && !bypassAuthentication)
        {
            startActivity(new Intent(this, SophisticatedSignIn.class));
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        appContext = (RTraffic) getApplicationContext();
        redirectOnAuthFailure();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        preference = new Preference(this);
    }

    @Override
    protected void onResume ()
    {
        super.onResume();
        redirectOnAuthFailure();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_titlebar, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    { //for the ActionBar
        switch (item.getItemId())
        {
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

    @Override
    public boolean onNavigationItemSelected (MenuItem menuItem)
    {
        Intent tmp = null;
        switch (menuItem.getItemId())
        {
            case R.id.action_acct:
                tmp = new Intent(this, GSignInActivity.class);
                tmp.putExtra("se.winterei.rtraffic.GSignInActivityFilter", "ok");
                break;
            case R.id.action_settings:
                tmp = new Intent(this, SettingsActivity.class);
                break;
            case R.id.action_notif_reg:
                tmp = new Intent(this, PointsOfInterestActivity.class);
                break;
            case R.id.action_help:
                tmp = new Intent(this, HelpActivity.class);
                break;
            case R.id.action_exclude_regions:
                tmp = new Intent(this, ExcludedRegionsActivity.class);
                break;
            case R.id.action_feedback:
                showDoorbell();
                break;
        }
        if (tmp != null)
            startActivity(tmp);
        menuItem.setChecked(true);
        drawerLayout.closeDrawers();
        return false;
    }

    @Override
    public void onBackPressed ()
    {
        if(backButtonCount >= 1)
        {
            backButtonCount = 0;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, getString(R.string.exit_on_back), Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }

    public boolean checkGPSPermissions ()
    {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (!permissionRequested)
                bToast = showToast(R.string.permission_location_err, Toast.LENGTH_SHORT);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            permissionRequested = true;
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        if(grantResults.length > 0)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                if (bToast != null)
                    bToast.cancel();
                if (child != null)
                    child.resumeAfterPermissionGranted();
                else
                    showToast(R.string.something_went_wrong, Toast.LENGTH_LONG);
            }

            else
            {
                showToast(R.string.permission_location_fatal, Toast.LENGTH_LONG);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void resumeAfterPermissionGranted () {};

    public void genericFixToolbar (Menu menu)
    {
        MenuItem tmp = menu.findItem(R.id.action_search);
        if(tmp != null)
            tmp.setVisible(false);
        tmp = menu.findItem(R.id.action_refresh);
        if(tmp != null)
            tmp.setVisible(false);
        invalidateOptionsMenu();
    }

    public Snackbar showSnackbar (@Nullable View v, String str, int length)
    {
        View vTemp;
        if(v != null)
            vTemp = v;
        else
            vTemp = getWindow().getDecorView().getRootView();
        Snackbar tmp = Snackbar.make(vTemp, str,length);
        tmp.show();
        return tmp;
    }

    public Snackbar showSnackbar (@Nullable View v, int stringID, int length)
    {
        View vTemp;
        if(v != null)
            vTemp = v;
        else
            vTemp = getWindow().getDecorView().getRootView();
        return showSnackbar(vTemp, getString(stringID), length);
    }

    public void dismissSnackbar (Snackbar s)
    {
        if(s != null && s.isShownOrQueued())
            s.dismiss();
    }

    public Toast showToast (String str, int length)
    {
        Toast tmp = Toast.makeText(this, str,length);
        tmp.show();
        return tmp;
    }

    public Toast showToast (int stringID, int length)
    {
        return showToast(getString(stringID), length);
    }

    public void showDoorbell ()
    {
        doorbell = new Doorbell(this, Integer.parseInt(getString(R.string.doorbell_app_id)), getString(R.string.doorbell_api));
        doorbell.setEmail(userEmail);
        doorbell.setName(userName);
        doorbell.setEmailFieldVisibility(View.GONE);
        doorbell.setPoweredByVisibility(View.GONE);
        doorbell.setOnFeedbackSentCallback(new OnFeedbackSentCallback() {
            @Override
            public void handle(String s)
            {
                showToast(R.string.feedback_thank_you, Toast.LENGTH_SHORT);
            }
        });
        doorbell.show();
    }

    public boolean isApiNotLowerThan (int versionCode)
    {
        return Build.VERSION.SDK_INT >= versionCode;
    }



}
