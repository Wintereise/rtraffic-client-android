package se.winterei.rtraffic;

import android.os.Bundle;
import android.view.Menu;

public class ExcludedRegions extends BaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excluded_regions);

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
