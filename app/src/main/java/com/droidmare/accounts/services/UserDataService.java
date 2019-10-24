package com.droidmare.accounts.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import com.droidmare.accounts.utils.ImageUtils;
import com.droidmare.accounts.views.activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

//User data receiver service declaration
//@author Eduardo on 22/05/2018.
public class UserDataService extends IntentService {

    private static final String TAG = UserDataService.class.getCanonicalName();

    public static final String USER_JSON_FIELD = "userJsonString";

    private static final String USER_DATA_PREF = "userDataPrefFile";

    private static final String USER_PREF_KEY = "userDataPrefKey";

    public static final String USER_ID_FIELD = "_id";
    public static final String USER_NAME_FIELD = "name";
    public static final String USER_SURNAME_FIELD = "surname";
    public static final String USER_AVATAR_FIELD = "avatar";
    public static final String USER_NICKNAME_FIELD = "nickname";
    public static final String USER_PASSWORD_FIELD = "password";

    private static String userJsonString;

    private static String userId;
    private static String userName;
    private static String userSurname;
    private static String avatarString;
    private static String userNickname;
    private static String userPassword;

    public static boolean infoSet = false;

    public UserDataService() { super(TAG); }

    private static WeakReference<MainActivity> mainActivityReference;
    public static void setMainActivityReference(MainActivity activity) {
        mainActivityReference = new WeakReference<>(activity);
    }

    @Override
    public void onHandleIntent(Intent dataIntent) {

        Log.d(TAG, "onHandleIntent");

        //If the data Intent contains the user json, that information is stored within the shared preferences:
        if (dataIntent.hasExtra(USER_JSON_FIELD)) {

            userJsonString = dataIntent.getStringExtra(USER_JSON_FIELD);

            writeSharedPrefs();
            setUserAttributes();

            infoSet = true;
        }

        //Otherwise, the current user information is cleared:
        else deleteSharedPreferences();
    }

    private void writeSharedPrefs() {

        Log.d(TAG, "writeSharedPrefs");

        SharedPreferences.Editor editor = getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE).edit();

        editor.putString(USER_PREF_KEY, userJsonString);

        editor.apply();
    }

    private void deleteSharedPreferences() {

        Log.d(TAG, "deleteSharedPreferences");

        SharedPreferences.Editor editor = getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE).edit();

        editor.clear();

        editor.apply();

        resetUserData();

        setUserData();
    }

    private static void setUserAttributes() {

        if (!userJsonString.equals("")) try {
            JSONObject userJson = new JSONObject(userJsonString);

            userId = userJson.getString(USER_ID_FIELD);
            userName = userJson.getString(USER_NAME_FIELD);
            userSurname = userJson.getString(USER_SURNAME_FIELD);
            avatarString = userJson.getString(USER_AVATAR_FIELD);
            userNickname = userJson.getString(USER_NICKNAME_FIELD);
            userPassword = userJson.getString(USER_PASSWORD_FIELD);

        } catch (JSONException jsonException) {
            Log.e(TAG, "setUserAttributes(). JSONException: " + jsonException.getMessage());
        }
    }

    public static void readSharedPrefs(Context context) {
        Log.d(TAG, "readSharedPrefs");

        SharedPreferences sharedPref = context.getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);

        userJsonString = sharedPref.getString(USER_PREF_KEY, "");

        setUserAttributes();
    }

    //Method that returns the user id:
    public static String getUserId() { return userId; }

    //Method that returns the user full name:
    public static String getUserFullName() { return userName + " " + userSurname; }

    //Method that returns the user name:
    public static String getUserName() { return userName; }

    //Method that returns the user surname:
    public static String getUserSurname() { return userSurname; }

    //Method that returns the decoded user avatar:
    public static Bitmap getDecodedAvatar() { return ImageUtils.decodeBitmapString(avatarString); }

    //Method that returns the encoded user avatar:
    public static String getEncodedAvatar() { return avatarString; }

    //Method that returns the user nickname:
    public static String getUserNickname() { return userNickname; }

    //Method that returns the user surname:
    public static String getUserPassword() { return userPassword; }

    private void setUserData() {
        if (mainActivityReference != null && mainActivityReference.get() != null) {
            mainActivityReference.get().setUserInformation();
        }
    }

    private void resetUserData() {
        userId = userName = userSurname = avatarString = userNickname = userPassword = null;
    }
}