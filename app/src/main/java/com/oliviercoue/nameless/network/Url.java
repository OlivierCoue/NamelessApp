package com.oliviercoue.nameless.network;

/**
 * Created by Olivier on 06/02/2016.
 *
 */
public interface Url {

    // TODO use prod server before create apk
    // PROD SERVER
    String SOCKET_URL           = "http://www.prestapic.com:3000";
    String API_BASE_URL         = "http://www.prestapic.com:8080/api/";
    String DOWNLOAD_BASE_URL    = "http://www.prestapic.com:8080/uploads/";
    String API_AUTH_URL         = "http://www.prestapic.com:8080/auth";

    // LOCAL SERVER
    /*
    String SOCKET_URL           = "http://192.168.1.10:3000";
    String API_BASE_URL         = "http://192.168.1.10:8080/api/";
    String DOWNLOAD_BASE_URL    = "http://192.168.1.10:8080/uploads/";
    String API_AUTH_URL         = "http://192.168.1.10:8080/auth";
    */

    // DEV SERVER
    /*
    String SOCKET_URL           = "http://www.prestapic.com:3333";
    String API_BASE_URL         = "http://www.prestapic.com:8888/api/";
    String DOWNLOAD_BASE_URL    = "http://www.prestapic.com:8888/uploads/";
    String API_AUTH_URL         = "http://www.prestapic.com:8888/auth";
    */
}
