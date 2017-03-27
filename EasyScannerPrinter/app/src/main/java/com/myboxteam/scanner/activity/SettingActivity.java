package com.myboxteam.scanner.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

import com.myboxteam.scanner.R;
import com.myboxteam.scanner.application.MBApplication;

/**
 * Created by Admin on 3/22/2017.
 */

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "SettingActivity";
    private MBApplication mApp;
    private Toolbar toolbar;
    private Context mContext;
    private WebView mWebview ;

    private String URL = "http://data.mbackend.info/myboxteam/SuggestApp.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_setting);
        mContext = this;

        mApp = (MBApplication) getApplication();

        // Find the toolbar view inside the activity layout
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mWebview  = (WebView) findViewById(R.id.webView);

        mWebview.getSettings().setJavaScriptEnabled(true); // enable javascript

        mWebview .loadUrl(URL);
    }
}
