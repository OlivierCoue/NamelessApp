package com.oliviercoue.nameless.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.oliviercoue.nameless.components.chat.views.ChatArrayAdapter;
import com.oliviercoue.nameless.network.NamelessRestClient;
import com.oliviercoue.nameless.components.chat.ChatImageHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 08/02/2016.
 */
public class MessageImage extends Message{

    Bitmap imageBitmap;
    String thumbnailUploadDir;
    String thumbnailName;
    String fullUploadDir;
    String fullName;
    String mime;
    String localPath;

    public MessageImage(){

    }

    public MessageImage(Integer id, String messageText, Boolean fromUs, Date createdDate, User author, Bitmap imageBitmap, String localPath) {
        super(id, messageText, fromUs, createdDate, author);
        this.imageBitmap = imageBitmap;
        this.localPath = localPath;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public String getThumbnailUploadDir() {
        return thumbnailUploadDir;
    }

    public void setThumbnailUploadDir(String thumbnailUploadDir) {
        this.thumbnailUploadDir = thumbnailUploadDir;
    }

    public String getThumbnailName() {
        return thumbnailName;
    }

    public void setThumbnailName(String thumbnailName) {
        this.thumbnailName = thumbnailName;
    }

    public String getFullUploadDir() {
        return fullUploadDir;
    }

    public void setFullUploadDir(String fullUploadDir) {
        this.fullUploadDir = fullUploadDir;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public static MessageImage fromJson(final ChatArrayAdapter adapter, Context context, Message m, JSONObject jsonObject) {
        final MessageImage mi = new MessageImage();

        mi.setId(m.getId());
        mi.setFromUs(m.getFromUs());
        mi.setCreatedDate(m.getCreatedDate());
        mi.setMessageText(m.getMessageText());
        mi.setAuthor(m.getAuthor());

        try {
            mi.thumbnailUploadDir = jsonObject.getString("thumbnail_upload_dir");
            mi.thumbnailName = jsonObject.getString("thumbnail_name");
            mi.fullUploadDir = jsonObject.getString("full_upload_dir");
            mi.fullName = jsonObject.getString("full_name");
            mi.mime = jsonObject.getString("mime");
        }catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        NamelessRestClient.get(mi.thumbnailName, new FileAsyncHttpResponseHandler(context) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                mi.imageBitmap = ChatImageHelper.getRoundedCornerBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()), 16);
                adapter.notifyDataSetChanged();
            }
        });

        return mi;
    }

}
