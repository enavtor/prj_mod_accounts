package com.droidmare.accounts.services;

import com.shtvsolution.common.services.CommonDateChecker;

//Service that implements the broadcast receiver for date changes and a daemon
//to ensure that the date text is always updated on the activities that have it:
//@author Eduardo on 28/02/2019.
public class DateCheckerService extends CommonDateChecker {

    @Override
    public void onCreate() {

        TAG = getClass().getCanonicalName();

        super.onCreate();
    }
}