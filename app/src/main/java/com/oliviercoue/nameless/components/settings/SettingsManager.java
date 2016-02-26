package com.oliviercoue.nameless.components.settings;

import com.oliviercoue.nameless.components.ActivityManager;
import com.oliviercoue.nameless.components.ActivityManagerImp;

/**
 * Created by Olivier on 26/02/2016.
 */
public class SettingsManager extends ActivityManager implements ActivityManagerImp {

    public SettingsManager(SettingsActivity settingsActivity){
        super(settingsActivity);
    }

    @Override
    public void onConnectionLost() {

    }

    @Override
    public void onConnectionBack() {

    }
}
