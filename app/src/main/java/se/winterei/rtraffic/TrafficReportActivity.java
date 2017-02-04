package se.winterei.rtraffic;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class TrafficReportActivity extends BaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_report);

        setupToolbar(null);
        setupNavigationView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        fixToolbar(menu);

        return true;
    }

    private void fixToolbar (Menu menu)
    {
        MenuItem tmp = menu.findItem(R.id.action_search);
        if(tmp != null)
            tmp.setVisible(false);
        tmp = menu.findItem(R.id.action_refresh);
        if(tmp != null)
            tmp.setVisible(false);
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed ()
    {
        finish();
    }
}
