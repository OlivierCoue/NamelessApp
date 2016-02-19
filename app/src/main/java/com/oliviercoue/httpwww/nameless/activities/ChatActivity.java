package com.oliviercoue.httpwww.nameless.activities;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.oliviercoue.httpwww.nameless.chat.ChatAsyncResponse;
import com.oliviercoue.httpwww.nameless.chat.ChatFullSizeImage;
import com.oliviercoue.httpwww.nameless.chat.ChatImageHelper;
import com.oliviercoue.httpwww.nameless.chat.ChatManager;
import com.oliviercoue.httpwww.nameless.models.Message;
import com.oliviercoue.httpwww.nameless.models.MessageImage;
import com.oliviercoue.httpwww.nameless.models.MessageTypes;
import com.oliviercoue.httpwww.nameless.models.States;
import com.oliviercoue.httpwww.nameless.models.User;
import com.oliviercoue.httpwww.nameless.notifications.KillNotificationsService;
import com.oliviercoue.httpwww.nameless.notifications.MyNotificationManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 06/02/2016.
 */
public class ChatActivity extends AppCompatActivity implements ChatAsyncResponse, ChatFullSizeImage {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_SELECT_PHOTO = 2;

    // UI references.
    private ListView messageListView;
    private EditText messageInput;
    private Button sendMessageButton, takePictureButton, selectPictureButton;
    private TextView friendLeaveTextView;
    private LinearLayout overlayImageLayout;
    private LinearLayout friendLeaveLayout;
    private ImageView fullSizeImageView;
    private ActionBar actionBar;

    private boolean changingStateAway = false, changingStateChatting = false, isAway = false, firstRun = true, fullSizeImageOpen = false, haveFriendFoundNotif;
    private MyNotificationManager myNotificationManager;
    private NotificationManager notificationManager;
    private ChatManager chatManager;
    private String mCurrentPhotoPath;;
    private ChatArrayAdapter chatArrayAdapter;
    private User currentUser, friendUser;
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

        ServiceConnection mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder binder) {
                ((KillNotificationsService.KillBinder) binder).service.startService(new Intent(ChatActivity.this, KillNotificationsService.class));
            }
            public void onServiceDisconnected(ComponentName className) {
            }
        };
        bindService(new Intent(ChatActivity.this, KillNotificationsService.class), mConnection, Context.BIND_AUTO_CREATE);

        chatManager = new ChatManager(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Button nextButton   = (Button) findViewById(R.id.next_button);
        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        messageInput        = (EditText) findViewById(R.id.message_input);
        messageListView     = (ListView) findViewById(R.id.message_list_view);
        sendMessageButton   = (Button) findViewById(R.id.message_send_button);
        takePictureButton   = (Button) findViewById(R.id.take_picture_button);
        selectPictureButton = (Button) findViewById(R.id.select_picture_button);
        friendLeaveTextView = (TextView) findViewById(R.id.friend_leave_text);
        overlayImageLayout  = (LinearLayout) findViewById(R.id.overlay_image_layout);
        friendLeaveLayout   = (LinearLayout) findViewById(R.id.friend_leave_layout);
        fullSizeImageView   = (ImageView) findViewById(R.id.full_size_image_view);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setShowHideAnimationEnabled(false);
        }
        haveFriendFoundNotif = getIntent().getExtras().getBoolean("HAVE_NOTIFICATION");
        chatManager.loadUsers(getIntent().getExtras().getInt("CURRENT_USER_ID"), getIntent().getExtras().getInt("FRIEND_USER_ID"));

        ioSocket.on("message_received", onMessageReceived);
        ioSocket.on("friend_quit", onFriendQuit);

        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    sendMessageButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    sendMessageButton.setTextColor(getResources().getColor(R.color.colorSecondary));
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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

        selectPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dispatchSelectPictureIntent();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatManager.next();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatManager.close();
            }
        });
        myNotificationManager = new MyNotificationManager(this);
    }

    private void closeAlert() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.confirm_leave_title));
        alert.setMessage(getResources().getString(R.string.confirm_leave_message));
        alert.setPositiveButton(getResources().getString(R.string.confirm_leave_btn1), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                chatManager.close();
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
        setChatUiMode(true);
        chatArrayAdapter = new ChatArrayAdapter(this, R.layout.message_right, currentUser, friendUser);
        messageListView.setAdapter(chatArrayAdapter);
    }

    private void setChatUiMode(boolean mod){
        messageInput.setEnabled(mod);
        takePictureButton.setEnabled(mod);
        selectPictureButton.setEnabled(mod);
        sendMessageButton.setEnabled(mod);
    }

    @Override
    public void onImageClicked(Bitmap image) {
        if(!fullSizeImageOpen){
            actionBar.hide();
            fullSizeImageOpen = true;
            fullSizeImageView.setImageBitmap(image);
            overlayImageLayout.setVisibility(View.VISIBLE);
            setChatUiMode(false);
        }
    }

    @Override
    public void usersLoaded(User[] users) {
        currentUser = users[0]!= null ? users[0] : null;
        friendUser = users[1]!= null ? users[1] : null;
        if(currentUser!=null && friendUser!=null){
            setupUI();
        }
    }

    private void sendMessage() {
        String messageText =  messageInput.getText().toString();
        if(!messageText.isEmpty()) {
            chatManager.sendMessage(messageText);
            chatArrayAdapter.add(new Message(1, messageText, true, new Date(), currentUser));
            messageInput.setText("");
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void dispatchSelectPictureIntent(){
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_SELECT_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {

            } else if (requestCode == REQUEST_SELECT_PHOTO) {
                Uri selectedImageUri = data.getData();
                String[] projection = { MediaStore.MediaColumns.DATA };
                CursorLoader cursorLoader = new CursorLoader(this,selectedImageUri, projection, null, null, null);
                Cursor cursor = cursorLoader.loadInBackground();
                cursor.moveToFirst();
                mCurrentPhotoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            }
            chatManager.sendImage(mCurrentPhotoPath);
        }
    }

    @Override
    public void onImageHandled(Bitmap image) {
        chatArrayAdapter.add(new MessageImage(1, "", true, new Date(), currentUser, ChatImageHelper.getRoundedCornerBitmap(image, 16), mCurrentPhotoPath));
    }

    private Emitter.Listener onMessageReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject response = (JSONObject) args[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Message receivedMessage = null;
                        switch (response.getInt("type")) {
                            case MessageTypes.TEXT:
                                receivedMessage = Message.fromJson(friendUser, response.getJSONObject("message").getJSONObject("data"));
                                chatArrayAdapter.add(receivedMessage);
                                break;
                            case MessageTypes.IMAGE:
                                receivedMessage = Message.fromJson(friendUser, response.getJSONObject("message").getJSONObject("data"));
                                chatArrayAdapter.add(MessageImage.fromJson(chatArrayAdapter, getApplicationContext(), receivedMessage, response.getJSONObject("message_image").getJSONObject("data")));
                        }
                        if (isAway) {
                            myNotificationManager.displayMessageNotifiaction(receivedMessage);
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
                    try {
                        if (friendUser != null && response.getInt("friend_id") == friendUser.getId()) {
                            closeKeybord();
                            setChatUiMode(false);
                            friendLeaveTextView.setText(friendUser.getUsername() + " " + getResources().getString(R.string.friend_leave_content));
                            friendLeaveLayout.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private void closeKeybord(){
        View view = this.getCurrentFocus();
        if(view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void nextUserFounded(User[] users) {
        messageInput.setText("");
        messageListView.setAdapter(chatArrayAdapter);
        currentUser = users[0];
        friendUser = users[1];
        friendLeaveLayout.setVisibility(View.GONE);
        setupUI();
    }

    @Override
    public void onBackPressed() {
        if(fullSizeImageOpen){
            overlayImageLayout.setVisibility(View.GONE);
            actionBar.show();
            fullSizeImageOpen = false;
            setChatUiMode(true);
        }else{
            closeAlert();
        }
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
                chatManager.next();
                return true;
            case android.R.id.home:
                chatManager.close();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(firstRun && haveFriendFoundNotif)notificationManager.cancel(222);
        firstRun = false;
        notificationManager.cancel(111);
        isAway = false;
        if(!changingStateChatting) {
            changingStateChatting = true;
            HashMap<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("state", States.CHATTING.toString());
            RequestParams params = new RequestParams(paramMap);
            NamelessRestClient.post("users/states", params, new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String s, Throwable t ){
                    changingStateChatting = false;
                }
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    changingStateChatting = false;
                }
            });
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        notificationManager.cancel(123);
        if(!isFinishing()){
            if(!changingStateAway) {
                isAway = true;
                changingStateAway = true;
                HashMap<String, String> paramMap = new HashMap<String, String>();
                paramMap.put("state", States.AWAY.toString());
                RequestParams params = new RequestParams(paramMap);
                NamelessRestClient.post("users/states", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String s, Throwable t ){
                        changingStateAway = false;
                    }
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        changingStateAway = false;
                    }
                });
            }
        }
    }
}
