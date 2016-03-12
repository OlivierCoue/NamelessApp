package com.oliviercoue.nameless.components.splash;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.oliviercoue.nameless.R;
import com.oliviercoue.nameless.components.start.StartActivity;
import com.oliviercoue.nameless.components.update.UpdateActivity;
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
    public void onAuthenticationSuccess(int minVersion) {
        Intent intent = null;
        try {
            if(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode < minVersion)
                intent = new Intent(SplashActivity.this, UpdateActivity.class);
            else{
                intent = new Intent(SplashActivity.this, StartActivity.class);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();
    }
}
