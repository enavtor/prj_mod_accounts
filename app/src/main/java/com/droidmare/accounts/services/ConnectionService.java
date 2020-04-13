package com.droidmare.accounts.services;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.droidmare.accounts.R;
import com.droidmare.accounts.views.activities.MainActivity;
import com.droidmare.common.services.CommonIntentService;
import com.droidmare.common.utils.ServiceUtils;
import com.droidmare.common.utils.ToastUtils;

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
public class ConnectionService extends CommonIntentService {

    //API base URL:
    public static final String BASE_URL = "http://droidmareapi.ddns.net:5006/user/";

    //Intent fields:
    public static final String REQUESTED_OPERATION_FIELD = "requestedOperation";

    //Response json fields:
    private static final String OPERATION_SUCCESS = "success";

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

    public ConnectionService() { super(ConnectionService.class.getCanonicalName()); }

    @Override
    public void onHandleIntent(Intent intent) {
        COMMON_TAG = getClass().getCanonicalName();
        
        super.onHandleIntent(intent);

        responseCode = -1;

        userNick = userPass = null;

        String requestedOperation = intent.getStringExtra(REQUESTED_OPERATION_FIELD);

        requestedLogin = requestedOperation.equals(LOGIN);

        if (requestedLogin) {
            userNick = intent.getStringExtra(UserDataService.USER_NICKNAME_FIELD);
            userPass = intent.getStringExtra(UserDataService.USER_PASSWORD_FIELD);
        }

        //If the requested operation was not a login one, the user json must be created here, since it will not be returned after the request is sent:
        else {
            userJsonString = intent.getStringExtra(UserDataService.USER_JSON_FIELD);
            try {
                JSONObject userJson = new JSONObject(userJsonString);

                //The avatar is stored within the API as an array, so that when using tools like ARC or PostMan the field can be collapsed, since it is considerably long:
                String[] avatar = {userJson.getString(UserDataService.USER_AVATAR_FIELD)};
                userJson.put(UserDataService.USER_AVATAR_FIELD, avatar);

                if (userJson.has(UserDataService.USER_NICKNAME_FIELD)) {
                    userNick = userJson.getString(UserDataService.USER_NICKNAME_FIELD);
                    userPass = userJson.getString(UserDataService.USER_PASSWORD_FIELD);
                }
            } catch (JSONException jsonException) {
                Log.e(COMMON_TAG, "onHandleIntent. JSONException: " + jsonException.getMessage());
            }
        }

        //Now the request can be sent to the api and the requested operation can be performed:
        sendRequest(requestedOperation, userNick, userPass);

        if (requestedLogin) performLogin();

        else if (responseCode == 200) switch (requestedOperation) {
            case CREATE:
            case EDIT:
                try {
                    if (showMessageAndDeleteData(requestedOperation)) launchLogin();
                } catch (JSONException jsonException) {
                    Log.e(COMMON_TAG, "onHandleIntent. JSONException: " + jsonException.getMessage());
                }
                break;
            case DELETE:
                try {
                    if (showMessageAndDeleteData(requestedOperation) && isMainActivityInstantiated())
                        mainActivityReference.get().logout();
                } catch (JSONException jsonException) {
                    Log.e(COMMON_TAG, "onHandleIntent. JSONException: " + jsonException.getMessage());
                }
        }

        else ToastUtils.makeCustomToast(getApplicationContext(), getString(R.string.operation_error));
    }

    //Method that shows a message informing the user whether or not the operation was successful and that resets the application data (by starting the data deleter service):
    private boolean showMessageAndDeleteData(String requestedOperation) throws JSONException {

        JSONObject userJson = new JSONObject(userJsonString);

        boolean success = userJson.getBoolean(OPERATION_SUCCESS);

        String message = getOperationMessage(requestedOperation, success);

        ToastUtils.makeCustomToast(getApplicationContext(), message);

        if (success) ServiceUtils.startService(getApplicationContext(), new Intent(getApplicationContext(), DataDeleterService.class));

        hideLoadingScreen();

        return success;
    }

    //Method that, based on the requested operation and in whether or not it was successful, retrieves the appropriate message string:
    private String getOperationMessage(String operation, boolean success) {
        switch (operation) {
            case CREATE:
                if (success) return getString(R.string.operation_create_success);
                else return getString(R.string.operation_create_fail);
            case EDIT:
                if (success) return getString(R.string.operation_edit_success);
                else return getString(R.string.operation_edit_fail);
            case DELETE:
                return getString(R.string.operation_delete_success);
            default: return "";
        }
    }

    //Method that launches the login operation in order to update the MainActivity's view:
    private void launchLogin() {
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isMainActivityInstantiated())
                    mainActivityReference.get().login(userNick, userPass);
            }
        }, 3000);
    }

    //Method that establishes a connection to the API and performs the required operations based on the requestedOperation param:
    private void sendRequest(String requestedOperation, String userNick, String userPass) {

        String apiURL = BASE_URL;
        HttpURLConnection connection = null;

        if (requestedLogin) apiURL = apiURL + userNick + "/" + userPass;

        try {
            connection = (HttpURLConnection) new URL(apiURL).openConnection();

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

        } catch (IOException ioe) {
            Log.e(COMMON_TAG, "sendRequest(). IOException: " + ioe.toString());
        } finally {
            if (connection != null) {
                try {
                    responseCode = connection.getResponseCode();
                    connection.disconnect();
                } catch (IOException ioe) {
                    Log.e(COMMON_TAG, "sendRequest(). IOException: " + ioe.toString());
                }
            }
        }
    }

    //Method that performs the login operation, sending the user data to the modules that need it or showing a Toast if an error occurs:
    private void performLogin() {

        switch (responseCode) {

            case 200:
                sendUserData();
                break;

            case 500:
                hideLoadingScreen();
                String invalidIdMessage = getString(R.string.id_not_valid, userNick, userPass);
                ToastUtils.makeCustomToast(getApplicationContext(), invalidIdMessage);
                break;

            default:
                hideLoadingScreen();
                String connectionError = getString(R.string.connection_error);
                ToastUtils.makeCustomToast(getApplicationContext(), connectionError);
        }
    }

    //Method that sends the user json to all the modules that need it (specifically this method launches the UserDataService of each module):
    private void sendUserData() {

        try {
            JSONObject userJson = new JSONObject(userJsonString);

            //The avatar is stored within the API as an array, so that when using tools like ARC or PostMan the field can be collapsed, since it is considerably long:
            String avatar = userJson.getJSONArray(UserDataService.USER_AVATAR_FIELD).getString(0);
            userJson.put(UserDataService.USER_AVATAR_FIELD, avatar);
            userJsonString = userJson.toString();

            String name = userJson.getString(UserDataService.USER_NAME_FIELD);
            String surname = userJson.getString(UserDataService.USER_SURNAME_FIELD);

            String user = name + " " + surname;

            Intent launcher;

            for (String modulePackage : modulesPackages) {

                launcher = new Intent();

                launcher.setComponent(new ComponentName(modulePackage, modulePackage + USER_RECEIVER_PACKAGE));

                launcher.putExtra(UserDataService.USER_JSON_FIELD, userJsonString);

                ServiceUtils.startService(getApplicationContext(), launcher);
            }

            //The execution thread is going to be paused to let the UserDataService
            //set the user info before it is displayed on the main activity user info view:
            while (!UserDataService.infoSet) {
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
            Log.e(COMMON_TAG, "sendUserData(). JSONException: " + jsonException.getMessage());
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
            Log.e(COMMON_TAG, "pauseServiceThread(). InterruptedException: " + ie.getMessage());
        }
    }

    //Method that checks if the MainActivity reference is initialized:
    private boolean isMainActivityInstantiated() {
        return mainActivityReference != null && mainActivityReference.get() != null;
    }
}
