package com.droidmare.accounts.services;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.droidmare.accounts.R;
import com.droidmare.accounts.utils.ToastUtils;
import com.droidmare.accounts.views.MainActivity;

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
    public static final String BASE_URL = "http://192.168.1.49:3000/user";

    //Connection properties:
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final String CONTENT_TYPE_PROPERTY_NAME = "Content-Type";
    private static final String CONTENT_TYPE_PROPERTY_VALUE = "application/json";

    //Modules packages names and api url receiver classes:
    private static final String ACCOUNTS_MODULE_PACKAGE = "com.droidmare.accounts";
    private static final String CALENDAR_MODULE_PACKAGE = "com.droidmare.calendar";

    //String array with all the modules packages names:
    private static final String[] modulesPackages = {
            ACCOUNTS_MODULE_PACKAGE,
            CALENDAR_MODULE_PACKAGE
    };

    //User data receiver package:
    private static final String USER_RECEIVER_PACKAGE = ".services.UserDataReceiverService";

    //User nickname and password (used when a login operation is performed)
    private String userNick;
    private String userPass;

    //Json that contains or will contain the user information (depending on the requested operation):
    private String userJsonString;

    //Control variable to know if the requested operation id of type GET:
    private boolean requestedGET;

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

        String requestedOperation = intent.getStringExtra("requestedOperation");

        requestedGET = requestedOperation.equals("GET");

        if (requestedGET) {
            userNick = intent.getStringExtra("userNick");
            userPass = intent.getStringExtra("userPass");
        }

        else {
            userJsonString = intent.getStringExtra("userJsonString");
        }

        connectAndRetrieve(requestedOperation, userNick, userPass);

        if (requestedGET) performLogin();
    }

    //Method that establishes a connection to the API and performs the required operations based on the requestedOperation param:
    private void connectAndRetrieve(String requestedOperation, String userNick, String userPass) {

        String apiURL = BASE_URL;

        if (requestedGET) apiURL = apiURL + userNick + userPass;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(apiURL).openConnection();

            connection.setRequestMethod(requestedOperation);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(CONNECTION_TIMEOUT);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);
            connection.setRequestProperty(CONTENT_TYPE_PROPERTY_NAME, CONTENT_TYPE_PROPERTY_VALUE);

            if (!requestedGET) {
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
            Log.e(TAG, "connectAndRetrieve(). IOException: " + ie.getMessage());
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
                ToastUtils.makeDefaultCustomToast(getApplicationContext(), invalidIdMessage);
                break;

            default:
                hideLoadingScreen();
                String connectionError = getResources().getString(R.string.connection_error);
                ToastUtils.makeDefaultCustomToast(getApplicationContext(), connectionError);
        }
    }

    //Method that sends the user json to all the modules that need it (specifically this method launches the UserDataReceiverService of each module):
    private void sendUserData() {

        try {
            JSONObject userJson = new JSONObject(userJsonString);

            String user = userJson.getString("name") + " " + userJson.getString("surname");

            Intent launcher;

            for (String modulePackage : modulesPackages) {

                launcher = new Intent();

                launcher.setComponent(new ComponentName(modulePackage, modulePackage + USER_RECEIVER_PACKAGE));

                launcher.putExtra("userJsonString", userJsonString);

                startService(launcher);
            }

            //Refreshing the user info view:
            if (isMainActivityInstantiated()) mainActivityReference.get().setUserInformation();

            String welcomeMessageHead = getResources().getString(R.string.welcome_message_head);
            String welcomeMessageTail = getResources().getString(R.string.welcome_message_tail);
            String welcomeMessage = welcomeMessageHead + user + welcomeMessageTail;

            hideLoadingScreen();

            ToastUtils.makeDefaultCustomToast(getApplicationContext(), welcomeMessage);

        } catch (JSONException jsonException) {
            Log.e(TAG, "sendUserData(). JSONException: " + jsonException.getMessage());
        }
    }

    //Method that tells the MainActivity to hide the loading screen:
    private void hideLoadingScreen() {
        if (isMainActivityInstantiated()) mainActivityReference.get().hideLoadingScreen();

        //To avoid toasts overlapping with the loading screen, the thread is paused for 80 milliseconds after hiding the loading screen:
        try {
            Thread.sleep(80);
        } catch (InterruptedException ie) {
            Log.e(TAG, "hideLoadingScreen(). InterruptedException: " + ie.getMessage());
        }
    }

    //Method that checks if the MainActivity reference is initialized:
    private boolean isMainActivityInstantiated() {
        return mainActivityReference != null && mainActivityReference.get() != null;
    }
}
