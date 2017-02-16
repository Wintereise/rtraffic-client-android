package se.winterei.rtraffic.activities;

import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;

import se.winterei.rtraffic.R;

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
        genericFixToolbar(menu);

        return true;
    }

    @Override
    public void onBackPressed ()
    {
        finish();
    }
}
