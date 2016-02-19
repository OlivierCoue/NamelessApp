package com.oliviercoue.httpwww.nameless.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.oliviercoue.httpwww.nameless.R;
import com.oliviercoue.httpwww.nameless.activities.ChatActivity;
import com.oliviercoue.httpwww.nameless.api.NamelessRestClient;
import com.oliviercoue.httpwww.nameless.chat.ChatFullSizeImage;
import com.oliviercoue.httpwww.nameless.models.Message;
import com.oliviercoue.httpwww.nameless.models.MessageImage;
import com.oliviercoue.httpwww.nameless.models.User;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 08/02/2016.
 */
public class ChatArrayAdapter extends ArrayAdapter<Message> {

    private LinearLayout topContainer;
    private TextView topDate;
    private TextView topUsername;
    private TextView chatText;
    private ImageView chatImage;
    private User currentUser, friendUser;
    private List<Message> chatMessageList = new ArrayList<Message>();
    private Context context;
    private ChatFullSizeImage chatFullSizeImage;

    @Override
    public void add(Message object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public ChatArrayAdapter(Context context, int textViewResourceId, User currentUser, User friendUser) {
        super(context, textViewResourceId);
        this.context = context;
        this.currentUser = currentUser;
        this.friendUser = friendUser;
        add(null);
        chatFullSizeImage = (ChatActivity)context;
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public Message getItem(int index) {
        return this.chatMessageList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        Message lastMessage = position > 0 ? getItem(position-1) : null;
        final Message message = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(position == 0){
            row = inflater.inflate(R.layout.message_header, parent, false);
            TextView headerTitle = (TextView) row.findViewById(R.id.message_header_title);
            TextView headerContent = (TextView) row.findViewById(R.id.message_header_content);
            String strTitle = context.getResources().getString(R.string.chat_welcome_title);
            String strContent = context.getResources().getString(R.string.chat_welcome_content);
            headerTitle.setText(String.format(strTitle, friendUser.getUsername()));
            headerContent.setText(String.format(strContent, currentUser.getUsername(), friendUser.getUsername()));
        }else {

            if (message.getFromUs()) {
                if (message instanceof MessageImage) {
                    row = inflater.inflate(R.layout.message_image_right, parent, false);
                    chatImage = (ImageView) row.findViewById(R.id.message_image);
                    chatImage.setImageBitmap(((MessageImage) message).getImageBitmap());
                    chatImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openFullImage(BitmapFactory.decodeFile(((MessageImage) message).getLocalPath()));
                        }
                    });
                } else {
                    row = inflater.inflate(R.layout.message_right, parent, false);
                    chatText = (TextView) row.findViewById(R.id.message_text_view);
                    chatText.setText(message.getMessageText());
                }

                if ((lastMessage != null && !lastMessage.getFromUs()) || position == 1) {
                    setMessageTop(row, message);
                }
            } else {
                if (message instanceof MessageImage) {
                    row = inflater.inflate(R.layout.message_image_left, parent, false);
                    chatImage = (ImageView) row.findViewById(R.id.message_image);
                    chatImage.setImageBitmap(((MessageImage) message).getImageBitmap());
                    chatImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            NamelessRestClient.get(((MessageImage) message).getFullName(), new FileAsyncHttpResponseHandler(context) {
                                @Override
                                public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                                }

                                @Override
                                public void onSuccess(int statusCode, Header[] headers, File file) {
                                    openFullImage(BitmapFactory.decodeFile(file.getAbsolutePath()));
                                }
                            });
                        }
                    });
                } else {
                    row = inflater.inflate(R.layout.message_left, parent, false);
                    chatText = (TextView) row.findViewById(R.id.message_text_view);
                    chatText.setText(message.getMessageText());
                }

                if ((lastMessage != null && lastMessage.getFromUs()) || position == 1) {
                    setMessageTop(row, message);
                }
            }
        }
        return row;
    }

    private void openFullImage(Bitmap image){
        chatFullSizeImage.onImageClicked(image);
    }

    private void setMessageTop(View row, Message message){
        topContainer = (LinearLayout) row.findViewById(R.id.message_top_layout);
        topDate = (TextView) row.findViewById(R.id.message_top_date);
        topUsername = (TextView) row.findViewById(R.id.message_top_username);
        topDate.setText(new SimpleDateFormat("HH:mm").format(message.getCreatedDate()));
        topUsername.setText(message.getAuthor().getUsername());
        topContainer.setVisibility(View.VISIBLE);
    }
}
