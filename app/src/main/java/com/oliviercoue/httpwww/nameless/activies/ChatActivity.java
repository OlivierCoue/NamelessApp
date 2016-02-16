package com.oliviercoue.httpwww.nameless.activies;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.oliviercoue.httpwww.nameless.R;
import com.oliviercoue.httpwww.nameless.adapters.ChatArrayAdapter;
import com.oliviercoue.httpwww.nameless.api.NamelessRestClient;
import com.oliviercoue.httpwww.nameless.api.Url;
import com.oliviercoue.httpwww.nameless.models.Message;
import com.oliviercoue.httpwww.nameless.models.MessageImage;
import com.oliviercoue.httpwww.nameless.models.MessageTypes;
import com.oliviercoue.httpwww.nameless.models.States;
import com.oliviercoue.httpwww.nameless.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 06/02/2016.
 */
public class ChatActivity extends AppCompatActivity {

    // UI references.
    private ListView messageListView;
    private EditText messageInput;
    private Button sendMessageButton;
    private Button nextButton;
    private Button cancelButton;
    private Button takePictureButton;
    private LinearLayout friendLeaveLayout;
    private ActionBar actionBar;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    private String mCurrentPhotoPath;;
    private ChatArrayAdapter chatArrayAdapter;
    private User currentUser;
    private User friendUser;
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
        setContentView(R.layout.activity_chat);

        messageInput = (EditText) findViewById(R.id.message_input);
        messageListView = (ListView) findViewById(R.id.message_list_view);
        sendMessageButton = (Button) findViewById(R.id.message_send_button);
        nextButton = (Button) findViewById(R.id.next_button);
        cancelButton = (Button) findViewById(R.id.cancel_button);
        takePictureButton = (Button) findViewById(R.id.take_picture_button);
        friendLeaveLayout = (LinearLayout) findViewById(R.id.friend_leave_layout);
        actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        init(getIntent().getExtras().getInt("CURRENT_USER_ID"), getIntent().getExtras().getInt("FRIEND_USER_ID"));

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.message_right);
        messageListView.setAdapter(chatArrayAdapter);

        // ON SOCKET EVENT
        ioSocket.on("message_received", onMessageReceived);
        ioSocket.on("friend_quit", onFriendQuit);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendMessage();
            }
        });

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dispatchTakePictureIntent();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                next();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                close();
            }
        });
    }

    private void init(Integer currentUserId, Integer friendId){

        NamelessRestClient.get("users/"+currentUserId, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    currentUser = User.fromJson(response.getJSONObject("data"));
                    if(friendUser != null)setupUI();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        NamelessRestClient.get("users/"+friendId, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    friendUser = User.fromJson(response.getJSONObject("data"));
                    if(currentUser != null)setupUI();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void next(){
        NamelessRestClient.get("chat/next", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getBoolean("found")) {
                        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.message_right);
                        messageListView.setAdapter(chatArrayAdapter);
                        currentUser = User.fromJson(response.getJSONObject("currentUser").getJSONObject("data"));
                        friendUser = User.fromJson(response.getJSONObject("friend").getJSONObject("data"));
                        setupUI();
                    } else {
                        Intent intentSearchAct = new Intent(getApplicationContext(), SearchActivity.class);
                        startActivity(intentSearchAct);
                        finish();
                    }
                    friendLeaveLayout.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void close(){
        NamelessRestClient.post("chat/stop", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Intent intentMainAct = new Intent(getApplicationContext(), StartActivity.class);
                startActivity(intentMainAct);
                finish();
            }
        });
    }

    private void closeAlert() {

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.confirm_leave_title));
        alert.setMessage(getResources().getString(R.string.confirm_leave_message));
        alert.setPositiveButton(getResources().getString(R.string.confirm_leave_btn1), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                close();
            }
        });
        alert.setNegativeButton(getResources().getString(R.string.confirm_leave_btn2), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    private void setupUI(){
        actionBar.setTitle(friendUser.getUsername());
        messageInput.setHint(getResources().getString(R.string.chat_input_composer) + " " + friendUser.getUsername());
    }



    private boolean sendMessage() {
        String messageText =  messageInput.getText().toString();
        if(messageText != null && !messageText.isEmpty()) {
            HashMap<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("messageText", messageText);
            RequestParams params = new RequestParams(paramMap);
            NamelessRestClient.post("message", params, new JsonHttpResponseHandler() {});

            chatArrayAdapter.add(new Message(1, messageText, true, new Date()));
            messageInput.setText("");
            return true;
        }else{
            return false;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }else{
                Log.d(this.getClass().getName(), "nullll");
            }
        }
    }

    private Bitmap getPic(int width, int heigth) {
        // Get the dimensions of the View
        int targetW = width;
        int targetH = heigth;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File photoFile = new File(mCurrentPhotoPath);
            Log.d(this.getClass().getName(), mCurrentPhotoPath);
            Bitmap thumbnailImageBitmap = getPic(480, 480);
            Bitmap fullImageBitmap =  BitmapFactory.decodeFile(mCurrentPhotoPath);
            chatArrayAdapter.add(new MessageImage(1, "", true, new Date(), thumbnailImageBitmap));

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            thumbnailImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();
            ByteArrayInputStream thumbnailIS = new ByteArrayInputStream(bitmapdata);

            ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
            fullImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos2);
            byte[] bitmapdata2 = bos2.toByteArray();
            ByteArrayInputStream fullIS = new ByteArrayInputStream(bitmapdata2);

            RequestParams params = new RequestParams();
            params.put("thumbnail", thumbnailIS, "thumbnail.jpeg");
            params.put("full", fullIS, "thumbnail.jpeg");

            NamelessRestClient.post("message/image", params, new JsonHttpResponseHandler() {
            });

        }
    }

    private Emitter.Listener onMessageReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject response = (JSONObject) args[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        switch (response.getInt("type")) {
                            case MessageTypes.TEXT:
                                chatArrayAdapter.add(Message.fromJson(response.getJSONObject("message").getJSONObject("data")));
                                break;
                            case MessageTypes.IMAGE:
                                Message tempMessage = Message.fromJson(response.getJSONObject("message").getJSONObject("data"));
                                chatArrayAdapter.add(MessageImage.fromJson(chatArrayAdapter, getApplicationContext(), tempMessage, response.getJSONObject("message_image").getJSONObject("data")));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    };

    private Emitter.Listener onFriendQuit = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject response = (JSONObject) args[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(this.getClass().getName(), "friend leave");
                    friendLeaveLayout.setVisibility(View.VISIBLE);
                }
            });
        }
    };

    @Override
    public void onBackPressed() {
        closeAlert();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next:
                next();
                return true;
            case android.R.id.home:
                close();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        // change user state to CHATTING
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("state", States.CHATTING.toString());
        RequestParams params = new RequestParams(paramMap);
        NamelessRestClient.post("users/states", params, new JsonHttpResponseHandler() {});
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(!isFinishing()){
            // change user state to AWAY
            HashMap<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("state", States.AWAY.toString());
            RequestParams params = new RequestParams(paramMap);
            NamelessRestClient.post("users/states", params, new JsonHttpResponseHandler() {});
        }
    }

}
