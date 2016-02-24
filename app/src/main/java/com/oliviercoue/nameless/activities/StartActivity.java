package com.oliviercoue.nameless.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.oliviercoue.nameless.api.NamelessRestClient;
import com.oliviercoue.nameless.api.Url;
import com.oliviercoue.nameless.handlers.FriendFoundHandler;
import com.oliviercoue.nameless.security.Security;
import com.oliviercoue.nameless.security.SecurityImp;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;


/**
 * Created by Olivier on 06/02/2016.
 *
 */
public class StartActivity extends AppCompatActivity implements SecurityImp, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // UI references.
    private EditText usernameView;
    private TextView rangeValueView, closeFiendNbView;
    private LinearLayout seekBarGradientLayout, gradientBackgroundLayout;
    private SeekBar rangeSeekBar;
    private Button startChatButton;

    private static boolean isAuthenticated = false;
    private static String usernameTxt;
    private boolean startClicked = false;
    private int searchRange = 10;
    private static float seekBakProcess = 34;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Activity activity;
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

        activity = this;

        if(!isAuthenticated)
            authenticate();

        if(!isGpsAndNetworkEnabled())
            showLocationDisabledAlert();

        if (mGoogleApiClient == null)
            instantiateGoogleApi();

        ioSocket.connect();
        ioSocket.on("connect_success", onConnectSuccess);

        instantiateUIReferences();

        initRangeSeekBar();
        initUsernameTextView();

        setCloseFriendNb();

        ViewTreeObserver observer = gradientBackgroundLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams((int)(seekBakProcess*(gradientBackgroundLayout.getWidth()/100)), LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                seekBarGradientLayout.setLayoutParams(param);
                gradientBackgroundLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        startChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameFromInput = usernameView.getText().toString();
                usernameTxt  = (usernameFromInput.isEmpty() || usernameFromInput.length() > 30) ? "Nameless" : usernameFromInput;
                if (isAuthenticated && !startClicked && mLastLocation != null && socketId != null && !socketId.isEmpty()) {
                    startClicked = true;
                    HashMap<String, String> paramMap = new HashMap<>();
                    paramMap.put("username", usernameTxt);
                    paramMap.put("socketId", socketId);
                    paramMap.put("lat", String.valueOf(mLastLocation.getLatitude()));
                    paramMap.put("long", String.valueOf(mLastLocation.getLongitude()));
                    paramMap.put("range", String.valueOf(searchRange));
                    NamelessRestClient.post("chat/start", new RequestParams(paramMap), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                if (response.getBoolean("found")) {
                                    new FriendFoundHandler(activity, response, false);
                                } else {
                                    Intent intentSearchAct = new Intent(getApplicationContext(), SearchActivity.class);
                                    startActivity(intentSearchAct);
                                    finish();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    Log.d(this.getClass().getName(), "bad params");
                }
            }
        });

        rangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                seekBakProcess = progresValue;
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams((int) (seekBakProcess * (gradientBackgroundLayout.getWidth() / 100)), LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                seekBarGradientLayout.setLayoutParams(param);
                rangeValueView.setText(String.valueOf((int) (Math.pow(2, seekBakProcess / 10))));
                searchRange = (int) Math.pow(2, seekBakProcess / 10);
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

    private void authenticate(){
        Security security = new Security(this);
        security.authentication();
    }

    private boolean isGpsAndNetworkEnabled(){
        boolean gps_enabled = false;
        boolean network_enabled = false;
        LocationManager mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) 500, (float) 200, mLocationListener);
        try {
            gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return gps_enabled && network_enabled;
    }

    private void instantiateGoogleApi(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void instantiateUIReferences(){
        startChatButton          = (Button) findViewById(R.id.start_chat_button);
        rangeSeekBar             = (SeekBar) findViewById(R.id.sb_range);
        usernameView             = (EditText) findViewById(R.id.username);
        seekBarGradientLayout    = (LinearLayout) findViewById(R.id.sb_gradient_layout);
        rangeValueView           = (TextView) findViewById(R.id.range_value);
        closeFiendNbView         = (TextView) findViewById(R.id.close_friend_nb);
        gradientBackgroundLayout = (LinearLayout) findViewById(R.id.gradient_background_layout);
    }

    private void initRangeSeekBar(){
        rangeSeekBar.setProgress((int) seekBakProcess);
        rangeValueView.setText(String.valueOf((int) Math.pow(2, seekBakProcess / 10)));
        searchRange = (int) Math.pow(2, seekBakProcess / 10);
    }

    public void initUsernameTextView(){
        if (usernameTxt != null && !usernameTxt.isEmpty())
            usernameView.setText(usernameTxt);
    }

    private void setCloseFriendNb() {
        if(mLastLocation != null && isAuthenticated) {
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
    }

    private void showLocationDisabledAlert(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("location disabled");
        dialog.setPositiveButton("open location settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        });
        dialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            }
        });
        dialog.show();
    }

    private Emitter.Listener onConnectSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                socketId = data.getString("socketId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onAuthenticationSuccess() {
        isAuthenticated = true;
        setCloseFriendNb();
    }

    @Override
    public void onBackPressed() {

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_start_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_language:
                Intent intentSearchAct = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intentSearchAct);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onResume(){
        super.onResume();
        if(mLastLocation!=null)
            setCloseFriendNb();
    }

    protected void onStart() {
        if(mGoogleApiClient!=null)
            mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        if(mGoogleApiClient!=null)
            mGoogleApiClient.disconnect();
        super.onStop();
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            mLastLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
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

