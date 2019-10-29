package com.droidmare.accounts.services;

import android.content.ComponentName;
import android.content.Intent;

import com.shtvsolution.common.services.CommonIntentService;
import com.shtvsolution.common.utils.ServiceUtils;

//App's data deleter service declaration
//@author Eduardo on 24/05/2018.
public class DataDeleterService extends CommonIntentService {

    private static final String TAG = DataDeleterService.class.getCanonicalName();

    public DataDeleterService() { super(TAG); }

    @Override
    public void onHandleIntent(Intent intent) {

        COMMON_TAG = TAG;

        super.onHandleIntent(intent);

        intent.setComponent(new ComponentName(ConnectionService.CALENDAR_MODULE_PACKAGE, ConnectionService.CALENDAR_MODULE_PACKAGE + ".services.UserDataService"));

        ServiceUtils.startService(getApplicationContext(), intent);

        UserDataService.infoSet = false;
    }
}