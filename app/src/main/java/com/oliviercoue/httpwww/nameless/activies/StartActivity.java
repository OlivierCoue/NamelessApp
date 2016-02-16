package com.oliviercoue.httpwww.nameless.activies;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.oliviercoue.httpwww.nameless.R;
import com.oliviercoue.httpwww.nameless.api.NamelessRestClient;
import com.oliviercoue.httpwww.nameless.api.Url;
import com.oliviercoue.httpwww.nameless.handlers.FriendFoundHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;


/**
 * Created by Olivier on 06/02/2016.
 */
public class StartActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // UI references.
    private EditText    usernameView;
    private Button      startChatButton;
    private SeekBar     rangeSeekBar;
    private TextView    rangeValueView;
    private TextView    closeFiendNbView;

    private Activity activity;
    private static String usernameTxt;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private boolean startClicked = false;
    private int searchRange = 10;
    private static float seekBakProcess = 34;
    private static String socketId;
    private Socket ioSocket;
    {
        try {
            ioSocket = IO.socket(Url.SOCKET_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // init socket connection
        ioSocket.connect();
        ioSocket.on("connect_success", onConnectSuccess);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        usernameView = (EditText) findViewById(R.id.username);
        startChatButton = (Button) findViewById(R.id.start_chat_button);
        rangeSeekBar = (SeekBar) findViewById(R.id.range_seekbar);
        rangeValueView = (TextView) findViewById(R.id.range_value);
        closeFiendNbView = (TextView) findViewById(R.id.close_friend_nb);

        rangeSeekBar.setProgress((int) seekBakProcess);
        rangeValueView.setText("~" + (int) Math.pow(2, seekBakProcess / 10) + " km");

        activity = this;

        searchRange = (int)Math.pow(2,seekBakProcess/10);
        if(mLastLocation != null)
            setCloseFriendNb();

        if(usernameTxt != null && !usernameTxt.isEmpty())
            usernameView.setText(usernameTxt);

        startChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                usernameTxt = usernameView.getText().toString();

                if(!startClicked && mLastLocation != null && usernameTxt != null && !usernameTxt.isEmpty() && socketId != null && !socketId.isEmpty()) {
                    startClicked = true;
                    HashMap<String, String> paramMap = new HashMap<String, String>();
                    paramMap.put("username", usernameTxt);
                    paramMap.put("socketId", socketId);
                    paramMap.put("lat", String.valueOf(mLastLocation.getLatitude()));
                    paramMap.put("long", String.valueOf(mLastLocation.getLongitude()));
                    paramMap.put("range", String.valueOf(searchRange));
                    RequestParams params = new RequestParams(paramMap);
                    NamelessRestClient.post("chat/start", params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                if (response.getBoolean("found")) {
                                    // user to speak with founded
                                    new FriendFoundHandler(activity, response);

                                } else {
                                    // user to speak with not founded
                                    Intent intentSearchAct = new Intent(getApplicationContext(), SearchActivity.class);
                                    startActivity(intentSearchAct);
                                    finish();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }else{
                    Log.d(this.getClass().getName(), "bad params");
                }
            }
        });

        rangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                        seekBakProcess = progresValue;
                        rangeValueView.setText("~" + (int)(Math.pow(2,seekBakProcess/10)) + " km");
                        searchRange = (int)Math.pow(2,seekBakProcess/10);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        setCloseFriendNb();
                    }
        });
    }

    private void setCloseFriendNb() {
        NamelessRestClient.get("chat/count?lat=" + String.valueOf(mLastLocation.getLatitude()) + "&long=" + String.valueOf(mLastLocation.getLongitude()) + "&range=" + searchRange, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            closeFiendNbView.setText(response.getString("friendNb"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private Emitter.Listener onConnectSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                socketId = data.getString("socketId");
            } catch (JSONException e) {
                return;
            }
        }
    };

    protected void onResume(){
        super.onResume();
        if(mLastLocation!=null)
            setCloseFriendNb();
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d(this.getClass().getName(), String.valueOf(mLastLocation.getLatitude()));
            Log.d(this.getClass().getName(), String.valueOf(mLastLocation.getLongitude()));
            setCloseFriendNb();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}

