package com.droidmare.accounts.services;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.droidmare.accounts.R;
import com.droidmare.accounts.utils.ToastUtils;
import com.droidmare.accounts.views.activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

//Service in charge of establishing a connection to the API and performing user data operations.
//Created by enavas on 16/06/2019.
public class ConnectionService extends IntentService {

    private static final String TAG = ConnectionService.class.getCanonicalName();

    //API base URL:
    public static final String BASE_URL = "http://192.168.1.49:3000/user/";
    //public static final String BASE_URL = "http://droidmare-api.localtunnel.me:3000/user/";

    //Intent fields:
    public static final String REQUESTED_OPERATION_FIELD = "requestedOperation";

    //API request operations:
    public static final String CREATE = "POST";
    public static final String LOGIN = "GET";
    public static final String EDIT = "PUT";
    public static final String DELETE = "DELETE";

    //Connection properties:
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final String CONTENT_TYPE_PROPERTY_NAME = "Content-Type";
    private static final String CONTENT_TYPE_PROPERTY_VALUE = "application/json";

    //Modules packages names and api url receiver classes:
    private static final String ACCOUNTS_MODULE_PACKAGE = "com.droidmare.accounts";
    public static final String CALENDAR_MODULE_PACKAGE = "com.droidmare.calendar";

    //String array with all the modules packages names:
    private static final String[] modulesPackages = {
            ACCOUNTS_MODULE_PACKAGE,
            CALENDAR_MODULE_PACKAGE
    };

    //User data receiver package:
    private static final String USER_RECEIVER_PACKAGE = ".services.UserDataService";

    //User nickname and password (used when a login operation is performed)
    private String userNick;
    private String userPass;

    //Json that contains or will contain the user information (depending on the requested operation):
    private String userJsonString;

    //Control variable to know if the requested operation id of type GET:
    private boolean requestedLogin;

    //The response code returned by the connection object:
    private int responseCode;

    //A reference to the MainActivity so that the loading widget can be hidden:
    private static WeakReference<MainActivity> mainActivityReference;
    public static void setMainActivityReference(MainActivity activity) {
        mainActivityReference = new WeakReference<>(activity);
    }

    public ConnectionService() { super(TAG); }

    @Override
    protected void onHandleIntent(Intent intent) {

        responseCode = -1;

        userNick = userPass = null;

        String requestedOperation = intent.getStringExtra(REQUESTED_OPERATION_FIELD);

        requestedLogin = requestedOperation.equals(LOGIN);

        if (requestedLogin) {
            userNick = intent.getStringExtra(UserDataService.USER_NICKNAME_FIELD);
            userPass = intent.getStringExtra(UserDataService.USER_PASSWORD_FIELD);
        }

        else {
            userJsonString = intent.getStringExtra(UserDataService.USER_JSON_FIELD);
            try {
                JSONObject userJson = new JSONObject(userJsonString);
                if (userJson.has(UserDataService.USER_NICKNAME_FIELD)) {
                    userNick = userJson.getString(UserDataService.USER_NICKNAME_FIELD);
                    userPass = userJson.getString(UserDataService.USER_PASSWORD_FIELD);
                }
            } catch (JSONException jsonException) {
                Log.e(TAG, "onHandleIntent(103). JSONException: " + jsonException.getMessage());
            }
        }

        connectAndRetrieve(requestedOperation, userNick, userPass);

        if (requestedLogin) performLogin();

        else if (requestedOperation.equals(CREATE)) {
            try {
                String message = new JSONObject(userJsonString).getString("message");
                ToastUtils.makeCustomToast(getApplicationContext(), message);
                startService(new Intent(getApplicationContext(), DataDeleterService.class));
                new Handler(getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isMainActivityInstantiated()) mainActivityReference.get().login(userNick, userPass);
                    }
                }, 5000);
            } catch (JSONException jsonException) {
                Log.e(TAG, "onHandleIntent(122). JSONException: " + jsonException.getMessage());
            }
        }

        else if (requestedOperation.equals(DELETE)) {

            if (responseCode == 200) {
                try {
                    String message = new JSONObject(userJsonString).getString("message");
                    ToastUtils.makeCustomToast(getApplicationContext(), message);
                    startService(new Intent(getApplicationContext(), DataDeleterService.class));
                    if (isMainActivityInstantiated()) mainActivityReference.get().logout();
                } catch (JSONException jsonException) {
                    Log.e(TAG, "onHandleIntent(122). JSONException: " + jsonException.getMessage());
                }
            }

            else {
                ToastUtils.makeCustomToast(getApplicationContext(), "User not deleted due to a connection error");
            }
        }
    }

    //Method that establishes a connection to the API and performs the required operations based on the requestedOperation param:
    private void connectAndRetrieve(String requestedOperation, String userNick, String userPass) {

        String apiURL = BASE_URL;

        if (requestedLogin) apiURL = apiURL + userNick + "/" + userPass;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(apiURL).openConnection();

            connection.setRequestMethod(requestedOperation);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(CONNECTION_TIMEOUT);

            if (!requestedLogin) {
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setChunkedStreamingMode(0);
                connection.setRequestProperty(CONTENT_TYPE_PROPERTY_NAME, CONTENT_TYPE_PROPERTY_VALUE);

                OutputStreamWriter outputStream = new OutputStreamWriter(connection.getOutputStream());
                outputStream.write(userJsonString);
                outputStream.close();
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;

            StringBuilder response = new StringBuilder();

            while ((inputLine = bufferedReader.readLine()) != null) {
                response.append(inputLine);
            }

            bufferedReader.close();

            userJsonString = response.toString();

            responseCode = connection.getResponseCode();
            connection.disconnect();

        } catch (IOException ie) {
            Log.e(TAG, "connectAndRetrieve(). IOException: " + ie.toString());
        }
    }

    //Method that performs the login operation, sending the user data to the modules that need it or showing a Toast if an error occurs:
    private void performLogin() {

        switch (responseCode) {

            case 200:
                sendUserData();
                break;

            case 300:
                hideLoadingScreen();
                String invalidIdMessage = getResources().getString(R.string.id_not_valid) + userNick + " / " + userPass;
                ToastUtils.makeCustomToast(getApplicationContext(), invalidIdMessage);
                break;

            default:
                hideLoadingScreen();
                String connectionError = getResources().getString(R.string.connection_error);
                ToastUtils.makeCustomToast(getApplicationContext(), connectionError);
        }
    }

    //Method that sends the user json to all the modules that need it (specifically this method launches the UserDataService of each module):
    private void sendUserData() {

        try {
            JSONObject userJson = new JSONObject(userJsonString);

            String name = userJson.getString(UserDataService.USER_NAME_FIELD);
            String surname = userJson.getString(UserDataService.USER_SURNAME_FIELD);

            String user = name + " " + surname;

            Intent launcher;

            for (String modulePackage : modulesPackages) {

                launcher = new Intent();

                launcher.setComponent(new ComponentName(modulePackage, modulePackage + USER_RECEIVER_PACKAGE));

                launcher.putExtra(UserDataService.USER_JSON_FIELD, userJsonString);

                startService(launcher);
            }

            //The execution thread is going to be paused to let the UserDataService
            //set the user info before it is displayed on the main activity user info view:
            while (!UserDataService.infoSet) {
                Log.d("PAUSEDTHREAD", "Paused");
                pauseServiceThread(50);
            }

            //Refreshing the user info view:
            if (isMainActivityInstantiated()) mainActivityReference.get().setUserInformation();

            String welcomeMessageHead = getResources().getString(R.string.welcome_message_head);
            String welcomeMessageTail = getResources().getString(R.string.welcome_message_tail);
            String welcomeMessage = welcomeMessageHead + user + welcomeMessageTail;

            hideLoadingScreen();

            ToastUtils.makeCustomToast(getApplicationContext(), welcomeMessage);

        } catch (JSONException jsonException) {
            Log.e(TAG, "sendUserData(). JSONException: " + jsonException.getMessage());
            ToastUtils.makeCustomToast(getApplicationContext(), userJsonString);
            hideLoadingScreen();
        }
    }

    //Method that tells the MainActivity to hide the loading screen:
    private void hideLoadingScreen() {
        if (isMainActivityInstantiated()) mainActivityReference.get().hideLoadingScreen();

        //To avoid toasts overlapping with the loading screen, the thread is paused for 80 milliseconds after hiding the loading screen:
        pauseServiceThread(80);
    }

    //Method that pauses the execution thread for the specified time:
    private void pauseServiceThread(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Log.e(TAG, "pauseServiceThread(). InterruptedException: " + ie.getMessage());
        }
    }

    //Method that checks if the MainActivity reference is initialized:
    private boolean isMainActivityInstantiated() {
        return mainActivityReference != null && mainActivityReference.get() != null;
    }
}
