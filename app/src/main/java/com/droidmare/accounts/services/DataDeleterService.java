package com.droidmare.accounts.services;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;

//App's data deleter service declaration
//@author Eduardo on 24/05/2018.

public class DataDeleterService extends IntentService {

    public DataDeleterService() {
        super("DataDeleterService");
    }

    @Override
    public void onHandleIntent(Intent intent) {

        intent.setComponent(new ComponentName(ConnectionService.CALENDAR_MODULE_PACKAGE, ConnectionService.CALENDAR_MODULE_PACKAGE + "services.DataDeleterService"));

        startService(intent);

        UserDataService.infoSet = false;
    }
}