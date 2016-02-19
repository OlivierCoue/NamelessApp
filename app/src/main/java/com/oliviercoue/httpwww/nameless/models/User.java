package com.oliviercoue.httpwww.nameless.models;

import android.text.Html;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Olivier on 06/02/2016.
 */
public class User {

    private Integer id;
    private String username;
    private Integer state;
    private String socketId;
    private Date createdDate;

    public User(){
    }

    public User(Integer id, String username, Integer state, String socketId, String createdDate) {
        this.id = id;
        this.username = username;
        this.state = state;
        this.socketId = socketId;
        this.createdDate = getDateFromString(createdDate);
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getSocketId() {
        return socketId;
    }

    public void setSocketId(String socketId) {
        this.socketId = socketId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public static User fromJson(JSONObject jsonObject) {
        User u = new User();
        try {
            u.id = jsonObject.getInt("id");
            u.username = Html.fromHtml(jsonObject.getString("username")).toString();
            u.socketId = jsonObject.getString("socketId");
            u.state = jsonObject.getInt("state");
            u.createdDate = u.getDateFromString(jsonObject.getString("createdDate"));
        }catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return u;
    }
}
