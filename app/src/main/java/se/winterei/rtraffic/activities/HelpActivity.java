package se.winterei.rtraffic.activities;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import se.winterei.rtraffic.R;

public class HelpActivity extends BaseActivity
{

    private WebView webView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        setupToolbar(null);
        setupNavigationView();

        webView = (WebView) findViewById(R.id.web_help);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageStarted (WebView view, String url, Bitmap favicon)
            {
                progressDialog = ProgressDialog.show(HelpActivity.this, "", getString(R.string.loading), false);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished (WebView view, String url)
            {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                super.onPageFinished(view, url);
            }
        });

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

    @Override
    public void onDestroy ()
    {
        if (progressDialog != null)
            progressDialog.dismiss();

        super.onDestroy();
    }
}
