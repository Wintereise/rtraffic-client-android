package se.winterei.rtraffic.activities;

import android.os.Bundle;
import android.view.Menu;

import se.winterei.rtraffic.R;

public class SettingsActivity extends BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupToolbar(null);
        setupNavigationView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        genericFixToolbar(menu);

        return true;
    }

    @Override
    public void onBackPressed ()
    {
        finish();
    }
}

