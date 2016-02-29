package com.oliviercoue.nameless.network;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by Olivier on 06/02/2016.
 */
public class NamelessRestClient {

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(Url.API_BASE_URL + url, params, responseHandler);
    }

    public static void getAuthentificationKey(AsyncHttpResponseHandler responseHandler) {
        client.get(Url.API_AUTH_URL, null, responseHandler);
    }

    public static void get(String url, FileAsyncHttpResponseHandler fileAsyncHttpResponseHandler ){
        client.get(Url.DOWNLOAD_BASE_URL + url, fileAsyncHttpResponseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(Url.API_BASE_URL + url, params, responseHandler);
    }

}
