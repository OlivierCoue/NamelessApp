package com.oliviercoue.nameless.components.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.oliviercoue.nameless.R;
import com.oliviercoue.nameless.components.start.StartActivity;
import com.oliviercoue.nameless.network.NamelessRestClient;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 06/02/2016.
 *
 */
public class SearchActivity extends AppCompatActivity {

    private Button cancelSearchButton;

    private SearchManager searchManager;
    private boolean cancelClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchManager = new SearchManager(this);

        cancelSearchButton = (Button) findViewById(R.id.cancel_search_button);

        cancelSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }


    private void cancel(){
        if(searchManager.isAuthenticated()) {
            if (!cancelClicked) {
                cancelClicked = true;
                cancelSearchButton.setClickable(false);
                NamelessRestClient.post("chat/stop", null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Intent intentMainAct = new Intent(getApplicationContext(), StartActivity.class);
                        startActivity(intentMainAct);
                        finish();
                    }
                });
            }
        }
    }


    @Override
    public void onBackPressed() {
        cancel();
    }

    @Override
    protected void onResume(){
        super.onResume();
        searchManager.activityResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        searchManager.activityPaused(isFinishing());
    }
}
