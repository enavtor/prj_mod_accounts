package com.droidmare.accounts.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import com.droidmare.accounts.utils.ImageUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//User data receiver service declaration
//@author Eduardo on 22/05/2018.
public class UserDataReceiverService extends IntentService {

    private static final String TAG = UserDataReceiverService.class.getCanonicalName();

    private static final String USER_DATA_PREF = "userDataPrefFile";

    private static final String USER_PREF_KEY = "userDataPrefKey";

    private static String userJsonString;

    private static String userId;
    private static String userName;
    private static String userSurname;
    private static String avatarString;
    private static String userNickname;
    private static String userPassword;

    public UserDataReceiverService() { super(TAG); }

    @Override
    public void onHandleIntent(Intent dataIntent) {

        Log.d(TAG, "onHandleIntent");

        userJsonString = dataIntent.getStringExtra("userJsonString");

        writeSharedPrefs();
        setUserAttributes();
    }

    private void writeSharedPrefs() {

        Log.d(TAG, "writeSharedPrefs");

        SharedPreferences.Editor editor = getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE).edit();

        editor.putString(USER_PREF_KEY, userJsonString);

        editor.apply();
    }

    private static void setUserAttributes() {

        try {
            JSONObject userJson = new JSONObject(userJsonString);

            userId = userJson.getString("_id");
            userName = userJson.getString("name");
            userSurname = userJson.getString("surname");
            avatarString = userJson.getString("avatar");
            userNickname = userJson.getString("nickname");
            userPassword = userJson.getString("password");

        } catch (JSONException jsonException) {
            Log.e(TAG, "setUserAttributes(). JSONException: " + jsonException.getMessage());
        }
    }

    public static void readSharedPrefs(Context context) {
        Log.d(TAG, "readSharedPrefs");

        SharedPreferences sharedPref = context.getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);

        userJsonString = sharedPref.getString(USER_DATA_PREF, "");

        setUserAttributes();
    }

       //Method that returns the user id:
    public static String getUserId() { return userId; }

    //Method that returns the user name:
    public static String getUserName() { return userName + "" + userSurname; }

    //Method that returns the user avatar decoded:
    public static Bitmap getDecodedAvatar() { return ImageUtils.decodeBitmapString(avatarString); }

    //Method that returns the user nickname:
    public static String getUserNickname() { return userNickname; }

    //Method that returns the user surname:
    public static String getUserPassword() { return userPassword; }
}