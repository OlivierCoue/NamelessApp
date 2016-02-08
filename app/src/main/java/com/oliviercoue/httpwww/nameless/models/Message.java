package com.oliviercoue.httpwww.nameless.models;

import android.text.Html;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.Object;

/**
 * Created by Olivier on 08/02/2016.
 */
public class Message {

    private Integer id;
    private String messageText;
    private Boolean fromUs;
    private Date createdDate;

    public Message() {
    }

    public Message(Integer id, String messageText, Boolean fromUs, Date createdDate) {
        this.id = id;
        this.messageText = messageText;
        this.fromUs = fromUs;
        this.createdDate = createdDate;

    }



    public Date getDateFromString(String dateStr){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            return dateFormat.parse(String.valueOf(dateStr));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Boolean getFromUs() {
        return fromUs;
    }

    public void setFromUs(Boolean fromUs) {
        this.fromUs = fromUs;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public static Message fromJson(JSONObject jsonObject) {
        Message m = new Message();
        try {
            m.id = jsonObject.getInt("id");
            m.messageText = Html.fromHtml(jsonObject.getString("messageText")).toString();
            m.fromUs = jsonObject.getBoolean("fromUs");
            m.createdDate = m.getDateFromString(jsonObject.getString("createdDate"));
        }catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return m;
    }

}
