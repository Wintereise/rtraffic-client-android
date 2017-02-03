package se.winterei.rtraffic;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class HelpActivity extends BaseActivity
{

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        setupToolbar(null);
        setupNavigationView();

        webView = (WebView) findViewById(R.id.web_help);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://voile.tomoyo.eu/rhelp/");
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
