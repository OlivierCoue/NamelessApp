package com.oliviercoue.nameless.components.start;

import org.json.JSONObject;

/**
 * Created by Olivier on 24/02/2016.
 */
public interface StartManagerImp {

    void onStartChatResult(Boolean friendUserFounded, JSONObject serverResponse);

    void onUsersInRangeNumberResult(int usersInRangeNumber);

}
