package com.oliviercoue.nameless.components.settings;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.oliviercoue.nameless.R;
import com.oliviercoue.nameless.components.start.StartActivity;

import java.util.Locale;

/**
 * Created by Olivier on 06/02/2016.
 *
 */
public class SettingsActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsManager = new SettingsManager(this);

        actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        ListView languagesListView = (ListView) findViewById(R.id.languages_listview);
        String[] languages = getResources().getStringArray(R.array.languages_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, languages);
        languagesListView.setAdapter(adapter);

        languagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Locale locale;
                switch ((int) id) {
                    case 0:
                        locale = Locale.ENGLISH;
                        actionBar.setTitle("Language");
                        break;
                    case 1:
                        locale = Locale.FRANCE;
                        actionBar.setTitle("Langue");
                        break;
                    case 2:
                        locale = new Locale("ru", "RU");
                        actionBar.setTitle("Язык");
                        break;
                    default:
                        locale = Locale.ENGLISH;
                }

                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getResources().updateConfiguration(config, null);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intentStartAct = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(intentStartAct);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intentStartAct = new Intent(getApplicationContext(), StartActivity.class);
                startActivity(intentStartAct);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        settingsManager.activityPaused(isFinishing());
    }

    @Override
    protected void onResume(){
        super.onResume();
        settingsManager.activityResume();
    }

}
