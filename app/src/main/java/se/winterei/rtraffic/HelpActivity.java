package se.winterei.rtraffic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class HelpActivity extends AppCompatActivity
{

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        webView = (WebView) findViewById(R.id.web_help);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://voile.tomoyo.eu/rhelp/");
    }
}
