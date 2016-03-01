package com.oliviercoue.nameless.components.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.oliviercoue.httpwww.nameless.R;
import com.oliviercoue.nameless.components.start.StartActivity;
import com.oliviercoue.nameless.network.security.Security;
import com.oliviercoue.nameless.network.security.SecurityImp;

/**
 * Created by Olivier on 26/02/2016.
 *
 */
public class SplashActivity extends AppCompatActivity implements SecurityImp{

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);

        Security security = new Security(this);
        security.authentication();

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.hide();

    }

    @Override
    public void onAuthenticationSuccess() {
        Intent mainIntent = new Intent(SplashActivity.this, StartActivity.class);
        SplashActivity.this.startActivity(mainIntent);
        SplashActivity.this.finish();
    }
}
