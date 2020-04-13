package com.droidmare.accounts.services;

import android.content.Intent;

import com.droidmare.common.services.CommonUserData;

//User data receiver service declaration
//@author Eduardo on 22/05/2019.

public class UserDataService extends CommonUserData {

    private static final String TAG = UserDataService.class.getCanonicalName();

    public UserDataService() {
        super(TAG);
    }

    @Override
    public void onHandleIntent(Intent dataIntent) {

        COMMON_TAG = TAG;

        super.onHandleIntent(dataIntent);
    }

    @Override
    protected void deleteSharedPreferences() {

        super.deleteSharedPreferences();

        setUserData();
    }
}