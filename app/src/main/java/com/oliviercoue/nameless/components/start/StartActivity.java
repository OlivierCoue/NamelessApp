package com.oliviercoue.nameless.components.start;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.oliviercoue.nameless.R;
import com.oliviercoue.nameless.components.search.SearchActivity;
import com.oliviercoue.nameless.components.settings.SettingsActivity;
import com.oliviercoue.nameless.handlers.FriendFoundHandler;

import org.json.JSONObject;


/**
 * Created by Olivier on 06/02/2016.
 *
 */
public class StartActivity extends AppCompatActivity implements StartManagerImp, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final int MAX_RANGE = 999999;

    private EditText usernameView;
    private TextView rangeValueView, closeFiendNbView, rangeAboutView, rangeKmView;
    private LinearLayout seekBarGradientLayout, gradientBackgroundLayout;
    private SeekBar rangeSeekBar;
    private Button startChatButton;

    private static float seekBakProcess = 34;
    private static String usernameTxt;

    private boolean startClicked = false;
    private int searchRangeKm = 10;
    private StartManager startManager;
    private GoogleApiClient mGoogleApiClient;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        activity = this;
        startManager = new StartManager(this);

        if(!isGpsAndNetworkEnabled())
            showLocationDisabledAlert();
        else if(haveMissedToMuchFriend())
            showToMuchFriendMissedAlert();

        if (mGoogleApiClient == null)
            instantiateGoogleApi();

        instantiateUIReferences();
        initRangeSeekBar();
        initUsernameTextView();

        ViewTreeObserver observer = gradientBackgroundLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setSeekBarGradientWidth();
                gradientBackgroundLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        startChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameFromInput = usernameView.getText().toString();
                usernameTxt = (usernameFromInput.isEmpty() || usernameFromInput.length() > 30) ? getResources().getString(R.string.app_name) : usernameFromInput;
                if (!startClicked) {
                    if (startManager.startChat(usernameTxt, searchRangeKm)) {
                        startClicked = true;
                        startChatButton.setClickable(false);
                    }
                }
            }
        });

        rangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                seekBakProcess = progresValue;
                if (seekBakProcess % 10 == 0)
                    setCloseFriendNb();
                setSeekBarGradientWidth();
                updateSearchRangeKm();
                updateRangeValueView();
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

    private void showLocationDisabledAlert(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getResources().getString(R.string.local_disabled));
        dialog.setPositiveButton(getResources().getString(R.string.local_open_param), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        });
        dialog.setNegativeButton(getResources().getString(R.string.local_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            }
        });
        dialog.show();
    }

    private boolean isGpsAndNetworkEnabled(){
        boolean gps_enabled = false;
        boolean network_enabled = false;
        LocationManager mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) 600000, (float) 200, mLocationListener);
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

    private boolean haveMissedToMuchFriend(){
        Bundle extras = getIntent().getExtras();
        return extras != null && extras.containsKey("HAVE_MISSED_TO_MUSH_FRIEND");
    }

    private void showToMuchFriendMissedAlert(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getResources().getString(R.string.missed_conversation));
        dialog.setNegativeButton(getResources().getString(R.string.missed_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            }
        });
        dialog.show();
    }

    private void instantiateUIReferences(){
        startChatButton          = (Button) findViewById(R.id.start_chat_button);
        rangeSeekBar             = (SeekBar) findViewById(R.id.sb_range);
        usernameView             = (EditText) findViewById(R.id.username);
        seekBarGradientLayout    = (LinearLayout) findViewById(R.id.sb_gradient_layout);
        rangeValueView           = (TextView) findViewById(R.id.range_value);
        closeFiendNbView         = (TextView) findViewById(R.id.close_friend_nb);
        rangeAboutView           = (TextView) findViewById(R.id.range_about_textview);
        rangeKmView              = (TextView) findViewById(R.id.range_km_textview);
        gradientBackgroundLayout = (LinearLayout) findViewById(R.id.gradient_background_layout);
        editIfRTL();
    }

    private void editIfRTL(){
        if(getResources().getBoolean(R.bool.is_right_to_left)) {
            View gradientView = findViewById(R.id.sb_gradient_view);
            gradientView.setBackground(ContextCompat.getDrawable(this, R.drawable.sb_range_gradient_rtl));
        }
    }

    private void initRangeSeekBar(){
        rangeSeekBar.setProgress((int) seekBakProcess);
        updateSearchRangeKm();
        updateRangeValueView();
    }

    private void updateSearchRangeKm(){
        int range = (int) Math.pow(2, seekBakProcess / 10);
        searchRangeKm =  range >= 900 ? MAX_RANGE : range;
    }

    private void updateRangeValueView(){
        String searchRangeTxt;
        if(searchRangeKm == MAX_RANGE){
            rangeKmView.setVisibility(View.GONE);
            rangeAboutView.setVisibility(View.GONE);
            searchRangeTxt = getResources().getString(R.string.range_worldwide);
        }else{
            rangeKmView.setVisibility(View.VISIBLE);
            rangeAboutView.setVisibility(View.VISIBLE);
            searchRangeTxt = String.valueOf(searchRangeKm);
        }
        rangeValueView.setText(searchRangeTxt);
    }

    public void initUsernameTextView(){
        if (usernameTxt != null && !usernameTxt.isEmpty())
            usernameView.setText(usernameTxt);
    }

    private void setSeekBarGradientWidth(){
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams((int) (seekBakProcess * (gradientBackgroundLayout.getWidth() / 100)), LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        seekBarGradientLayout.setLayoutParams(param);
    }

    private void setCloseFriendNb() {
        startManager.getUsersInRangeNumber(searchRangeKm);
    }

    @Override
    public void onStartChatResult(Boolean friendUserFounded, JSONObject serverResponse) {
        if (friendUserFounded) {
            new FriendFoundHandler(activity, serverResponse, false);
        } else {
            Intent intentSearchAct = new Intent(getApplicationContext(), SearchActivity.class);
            startActivity(intentSearchAct);
            finish();
        }
    }

    @Override
    public void onUsersInRangeNumberResult(int usersInRangeNumber) {
        closeFiendNbView.setText(String.valueOf(usersInRangeNumber));
    }

    @Override
    public void onBackPressed() {
        Intent homePageIntent = new Intent(Intent.ACTION_MAIN);
        homePageIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(homePageIntent);
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

    @Override
    protected void onPause() {
        super.onPause();
        if(startManager!=null)
            startManager.activityPaused(isFinishing());
    }

    @Override
    protected void onResume(){
        super.onResume();
        setCloseFriendNb();
        if(startManager!=null)
            startManager.activityResume();
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
            startManager.setUserLocation(location);
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
        Location userLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (userLocation != null){
            startManager.setUserLocation(userLocation);
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

