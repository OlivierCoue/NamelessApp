package com.oliviercoue.nameless.components.chat;

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
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.oliviercoue.httpwww.nameless.R;
import com.oliviercoue.nameless.components.chat.views.ChatArrayAdapter;
import com.oliviercoue.nameless.models.Message;
import com.oliviercoue.nameless.models.MessageImage;
import com.oliviercoue.nameless.models.MessageTypes;
import com.oliviercoue.nameless.models.States;
import com.oliviercoue.nameless.models.User;
import com.oliviercoue.nameless.services.CloseAppService;
import com.oliviercoue.nameless.notifications.MyNotificationManager;
import com.oliviercoue.nameless.notifications.NotificationTypes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by Olivier on 06/02/2016.
 *
 */
public class ChatActivity extends AppCompatActivity implements ChatManagerImp, ChatListViewImp {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_SELECT_PHOTO = 2;

    private ListView messageListView;
    private EditText messageInput;
    private ImageButton sendMessageButton, takePictureButton, selectPictureButton;
    private TextView friendLeaveTextView;
    private LinearLayout overlayImageLayout;
    private LinearLayout friendLeaveLayout;
    private ImageView fullSizeImageView;
    private Button nextButton, cancelButton;
    private ActionBar actionBar;

    private boolean changingStateAway = false, changingStateChatting = false, isAway = false, fullSizeImageOpen = false;
    private int lastMessageId = 0;
    private String mCurrentPhotoPath;
    private MyNotificationManager myNotificationManager;
    private NotificationManager notificationManager;
    private ChatManager chatManager;
    private ChatArrayAdapter chatArrayAdapter;
    private User currentUser, friendUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatManager = new ChatManager(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myNotificationManager = new MyNotificationManager(this);

        instantiateUIReferences();

        addBackButtonToActionBar();

        initUsers();

        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    sendMessageButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.icon_send_hover));
                } else {
                    sendMessageButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.icon_send));
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
                allowTakingPicture(false);
                dispatchTakePictureIntent();
            }
        });
        selectPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                allowTakingPicture(false);
                dispatchSelectPictureIntent();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextButton.setClickable(false);
                chatManager.next();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelButton.setClickable(false);
                chatManager.close();
            }
        });
    }

    private void instantiateUIReferences(){
        nextButton          = (Button) findViewById(R.id.next_button);
        cancelButton        = (Button) findViewById(R.id.cancel_button);
        messageInput        = (EditText) findViewById(R.id.message_input);
        messageListView     = (ListView) findViewById(R.id.message_list_view);
        sendMessageButton   = (ImageButton) findViewById(R.id.message_send_button);
        takePictureButton   = (ImageButton) findViewById(R.id.take_picture_button);
        selectPictureButton = (ImageButton) findViewById(R.id.select_picture_button);
        friendLeaveTextView = (TextView) findViewById(R.id.friend_leave_text);
        overlayImageLayout  = (LinearLayout) findViewById(R.id.overlay_image_layout);
        friendLeaveLayout   = (LinearLayout) findViewById(R.id.friend_leave_layout);
        fullSizeImageView   = (ImageView) findViewById(R.id.full_size_image_view);
        actionBar           = getSupportActionBar();
    }

    private void addBackButtonToActionBar(){
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void initUsers(){
        Bundle extras = getIntent().getExtras();
        if(isExtrasValid(extras))
            chatManager.loadUsers(extras.getInt("CURRENT_USER_ID"), extras.getInt("FRIEND_USER_ID"));
        else
            finish();
    }

    private boolean isExtrasValid(Bundle extras){
        return extras != null && extras.containsKey("CURRENT_USER_ID") && extras.containsKey("FRIEND_USER_ID");
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

    private void allowTakingPicture(boolean allow){
        selectPictureButton.setEnabled(allow);
        takePictureButton.setEnabled(allow);
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
    public void onUsersLoaded(User[] users) {
        currentUser = users[0];
        friendUser = users[1];
        if(currentUser!=null && friendUser!=null)
            setupUI();
    }

    private void sendMessage() {
        String messageText =  messageInput.getText().toString();
        if(!messageText.isEmpty()) {
            chatManager.sendMessage(messageText);
            chatArrayAdapter.add(new Message(1, messageText, true, new Date(), currentUser));
            messageInput.setText("");
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = ChatImageHelper.createImageFile();
                mCurrentPhotoPath = photoFile.getAbsolutePath();
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
            switch (requestCode){
                case REQUEST_IMAGE_CAPTURE:
                    break;
                case REQUEST_SELECT_PHOTO:
                    Uri selectedImageUri = data.getData();
                    String[] projection = { MediaStore.MediaColumns.DATA };
                    CursorLoader cursorLoader = new CursorLoader(this,selectedImageUri, projection, null, null, null);
                    Cursor cursor = cursorLoader.loadInBackground();
                    cursor.moveToFirst();
                    mCurrentPhotoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    break;
                default:
                    Log.d(this.getClass().getName(), "Unknown request code");
            }
            chatManager.sendImage(mCurrentPhotoPath);
            allowTakingPicture(true);
        }
    }

    @Override
    public void onImageHandled(Bitmap image) {
        chatArrayAdapter.add(new MessageImage(1, "", true, new Date(), currentUser, ChatImageHelper.getRoundedCornerBitmap(image, 16), mCurrentPhotoPath));
    }

    @Override
    public void onMessageReceived(final JSONObject serverResponse){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Message receivedMessage = null;
                    switch (serverResponse.getInt("type")) {
                        case MessageTypes.TEXT:
                            receivedMessage = Message.fromJson(friendUser, serverResponse.getJSONObject("message").getJSONObject("data"));
                            chatArrayAdapter.add(receivedMessage);
                            break;
                        case MessageTypes.IMAGE:
                            receivedMessage = Message.fromJson(friendUser, serverResponse.getJSONObject("message").getJSONObject("data"));
                            chatArrayAdapter.add(MessageImage.fromJson(chatArrayAdapter, getApplicationContext(), receivedMessage, serverResponse.getJSONObject("message_image").getJSONObject("data")));
                    }
                    if (isAway && receivedMessage != null) {
                        if (lastMessageId != receivedMessage.getId())
                            myNotificationManager.displayMessageNotifiaction(receivedMessage);
                        lastMessageId = receivedMessage.getId();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onFriendQuit(final JSONObject serverResponse){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (friendUser != null && serverResponse.getInt("friend_id") == friendUser.getId()) {
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

    private void closeKeybord(){
        View view = this.getCurrentFocus();
        if(view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onNextUserFounded(User[] users) {
        messageInput.setText("");
        messageListView.setAdapter(chatArrayAdapter);
        currentUser = users[0];
        friendUser = users[1];
        friendLeaveLayout.setVisibility(View.GONE);
        nextButton.setClickable(true);
        setupUI();
    }

    @Override
    public void onBackPressed() {
        if(fullSizeImageOpen){
            overlayImageLayout.setVisibility(View.GONE);
            actionBar.show();
            fullSizeImageOpen = false;
            setChatUiMode(true);
        }else
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
    public void onStateChanged(boolean success, int state) {
        if(state == States.CHATTING)
            changingStateChatting = false;
        else if(state == States.AWAY)
            changingStateAway = false;
    }

    @Override
    protected void onResume(){
        super.onResume();
        chatManager.activityResume();
        notificationManager.cancel(NotificationTypes.FRIEND_FOUNDED);
        notificationManager.cancel(NotificationTypes.MESSAGE_RECEIVED);
        allowTakingPicture(true);
        isAway = false;
        if(!changingStateChatting) {
            changingStateChatting = true;
            chatManager.changeUserState(States.CHATTING);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        chatManager.activityPaused(isFinishing());
        if(!isFinishing()){
            isAway = true;
            if(!changingStateAway) {
                changingStateAway = true;
                chatManager.changeUserState(States.AWAY);
            }
        }
    }
}
