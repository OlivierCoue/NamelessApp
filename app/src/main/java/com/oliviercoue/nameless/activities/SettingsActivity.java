package com.oliviercoue.nameless.activities;

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

import com.oliviercoue.httpwww.nameless.R;

import java.util.Locale;

/**
 * Created by Olivier on 06/02/2016.
 */
public class SettingsActivity extends AppCompatActivity {

    // UI references.
    private ListView languagesListView;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        languagesListView = (ListView) findViewById(R.id.languages_listview);
        String[] languages = getResources().getStringArray(R.array.languages_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, languages);
        languagesListView.setAdapter(adapter);

        languagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Locale locale = null;
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

}
