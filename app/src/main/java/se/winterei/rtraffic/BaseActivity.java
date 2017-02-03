package se.winterei.rtraffic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;


public abstract class BaseActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener
{
    private int backButtonCount = 0;
    private String userName = "Signed out", userEmail = "test@example.com";

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar myToolbar;

    private RTraffic appContext;


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

    public final void setupNavigationView ()
    {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        TextView navHeader = (TextView) header.findViewById(R.id.headerUsername);
        navHeader.setSingleLine(false);
        navHeader.setText(userName + "\n" + userEmail);
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
        if(!authCheck())
        {
            startActivity(new Intent(this, GSignInActivity.class));
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        appContext = (RTraffic) getApplicationContext();
        redirectOnAuthFailure();
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
        setupSearchBar(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    { //for the ActionBar
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
                tmp = new Intent(this, SettingsActivity.class);
                break;
            case R.id.action_help:
                tmp = new Intent(this, HelpActivity.class);
                break;
            case R.id.action_exclude_regions:
                tmp = new Intent(this, SettingsActivity.class);
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
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }


}
