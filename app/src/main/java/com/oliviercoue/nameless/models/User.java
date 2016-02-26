package com.oliviercoue.nameless.models;

import android.text.Html;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Olivier on 06/02/2016.
 *
 */
public class User {

    private Integer id;
    private String username;
    private Integer state;
    private String socketId;

    public User(){
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

    public static User fromJson(JSONObject jsonObject) {
        User u = new User();
        try {
            u.id = jsonObject.getInt("id");
            u.username = Html.fromHtml(jsonObject.getString("username")).toString();
            u.socketId = jsonObject.getString("socketId");
            u.state = jsonObject.getInt("state");
        }catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return u;
    }
}
