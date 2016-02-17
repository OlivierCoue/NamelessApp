package com.oliviercoue.httpwww.nameless.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oliviercoue.httpwww.nameless.R;
import com.oliviercoue.httpwww.nameless.models.Message;
import com.oliviercoue.httpwww.nameless.models.MessageImage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Olivier on 08/02/2016.
 */
public class ChatArrayAdapter extends ArrayAdapter<Message> {

    private LinearLayout topContainer;
    private TextView topDate;
    private TextView topUsername;
    private TextView chatText;
    private ImageView chatImage;
    private List<Message> chatMessageList = new ArrayList<Message>();
    private Context context;

    @Override
    public void add(Message object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public Message getItem(int index) {
        return this.chatMessageList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        Message lastMessage = position > 0 ? getItem(position-1) : null;
        Message message = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (message.getFromUs()) {
            if(message instanceof MessageImage){
                row = inflater.inflate(R.layout.message_image_right, parent, false);
                chatImage = (ImageView) row.findViewById(R.id.message_image);
                chatImage.setImageBitmap(((MessageImage) message).getImageBitmap());
            }
            else{
                row = inflater.inflate(R.layout.message_right, parent, false);
                chatText = (TextView) row.findViewById(R.id.message_text_view);
                chatText.setText(message.getMessageText());

                if((lastMessage != null && !lastMessage.getFromUs()) || position == 0) {
                    setMessageTop(row, message);
                }
            }
        }else{
            if(message instanceof MessageImage){
                row = inflater.inflate(R.layout.message_image_left, parent, false);
                chatImage = (ImageView) row.findViewById(R.id.message_image);
                chatImage.setImageBitmap(((MessageImage) message).getImageBitmap());
            }else{
                row = inflater.inflate(R.layout.message_left, parent, false);
                chatText = (TextView) row.findViewById(R.id.message_text_view);
                chatText.setText(message.getMessageText());

                if((lastMessage != null && lastMessage.getFromUs()) || position == 0) {
                    setMessageTop(row, message);
                }
            }
        }
        return row;
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
