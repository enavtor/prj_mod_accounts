package com.droidmare.accounts.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.droidmare.R;
import com.droidmare.accounts.Utils.ToastUtils;
import com.droidmare.accounts.views.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.droidmare.accounts.Utils.ToastUtils.DEFAULT_TOAST_DURATION;
import static com.droidmare.accounts.Utils.ToastUtils.DEFAULT_TOAST_SIZE;

public class ConnectionService extends AsyncTask<String, String, Void> {

    private static final String TAG = ConnectionService.class.getCanonicalName();

    private static final String GET_DATA = "/data";

    private static final String GET_VIDEO = "/videoconference";

    private static final String GET_EVENTS = "/events";

    private String dataResponse;

    private String videoResponse;

    private String eventsResponse;

    private boolean homeLogin;

    private String userId;

    private Context context;

    private int responseCode;

    //Home launcher activity packages:
    private final String HOME_PACKAGE_AAL = "com.shtvsolution.home.aal";
    private final String HOME_PACKAGE_HMI = "com.shtvsolution.home.hmi";

    //Modules user data receiver packages:
    private final String ACCOUNTS_RECEIVER_PACKAGE = "com.shtvsolution.cuentas";
    private final String ACCOUNTS_RECEIVER_CLASS = "com.shtvsolution.cuentas.services.UserDataReceiverService";

    private final String CALENDAR_RECEIVER_PACKAGE = "com.shtvsolution.calendario";
    private final String CALENDAR_RECEIVER_CLASS = "com.shtvsolution.calendario.services.UserDataReceiverService";

    private final String COGNITIVE_RECEIVER_PACKAGE = "com.shtvsolution.cognitivos";
    private final String COGNITIVE_RECEIVER_CLASS = "com.shtvsolution.cognitivos.services.UserDataReceiverService";

    private final String STATISTICS_RECEIVER_PACKAGE = "com.shtvsolution.estadisticas";
    private final String STATISTICS_RECEIVER_CLASS = "com.shtvsolution.estadisticas.services.UserDataReceiverService";

    private final String HOME_RECEIVER_CLASS = "com.shtvsolution.home.services.UserDataReceiverService";

    private final String MEASURES_RECEIVER_PACKAGE = "com.shtvsolution.medidas";
    private final String MEASURES_RECEIVER_CLASS = "com.shtvsolution.medidas.services.UserDataReceiverService";

    private final String REMINDERS_RECEIVER_PACKAGE = "com.shtvsolution.recordatorios";
    private final String REMINDERS_RECEIVER_CLASS = "com.shtvsolution.recordatorios.services.UserDataReceiverService";

    private final String REMINISCENCES_RECEIVER_PACKAGE = "com.shtvsolution.reminiscencia";
    private final String REMINISCENCES_RECEIVER_CLASS = "com.shtvsolution.reminiscencia.ServicesLayer.UserDataReceiverService";

    private final String THERAPY_RECEIVER_PACKAGE = "com.shtvsolution.terapia";
    private final String THERAPY_RECEIVER_CLASS = "com.shtvsolution.terapia.services.UserDataReceiverService";

    private final String VIDEOCONFERENCE_RECEIVER_PACKAGE = "com.shtvsolution.videoconferencia";
    private final String VIDEOCONFERENCE_RECEIVER_CLASS = "com.shtvsolution.videoconferencia.services.UserDataReceiverService";

    private final String LINPHONE_RECEIVER_PACKAGE = "com.shtvsolution.videoconferencia";
    private final String LINPHONE_RECEIVER_CLASS = "com.shtvsolution.services.UserDataReceiverService";

    private final String VOD_RECEIVER_PACKAGE = "com.shtvsolution.vod";
    private final String VOD_RECEIVER_CLASS = "com.shtvsolution.vod.services.UserDataReceiverService";

    private final String HELP_RECEIVER_PACKAGE = "com.shtvsolution.fichas.ayuda";
    private final String HELP_RECEIVER_CLASS = "com.shtvsolution.fichas.ayuda.services.UserDataReceiverService";

    private final String INFO_RECEIVER_PACKAGE = "com.shtvsolution.fichas.info";
    private final String INFO_RECEIVER_CLASS = "com.shtvsolution.fichas.info.services.UserDataReceiverService";

    private final String MEMORY_RECEIVER_PACKAGE = "com.shtvsolution.fichas.reminiscencias";
    private final String MEMORY_RECEIVER_CLASS = "com.shtvsolution.fichas.reminiscencias.services.UserDataReceiverService";

    private final String EDUCATION_RECEIVER_PACKAGE = "com.shtvsolution.fichas.educacion";
    private final String EDUCATION_RECEIVER_CLASS = "com.shtvsolution.fichas.educacion.services.UserDataReceiverService";


    //String array with all the user data receiver packages names:
    private final String[] dataReceiverPackages = {
            ACCOUNTS_RECEIVER_PACKAGE,
            ACCOUNTS_RECEIVER_CLASS,

            CALENDAR_RECEIVER_PACKAGE,
            CALENDAR_RECEIVER_CLASS,

            COGNITIVE_RECEIVER_PACKAGE,
            COGNITIVE_RECEIVER_CLASS,

            STATISTICS_RECEIVER_PACKAGE,
            STATISTICS_RECEIVER_CLASS,

            HOME_PACKAGE_AAL,
            HOME_RECEIVER_CLASS,

            HOME_PACKAGE_HMI,
            HOME_RECEIVER_CLASS,

            MEASURES_RECEIVER_PACKAGE,
            MEASURES_RECEIVER_CLASS,

            REMINDERS_RECEIVER_PACKAGE,
            REMINDERS_RECEIVER_CLASS,

            REMINISCENCES_RECEIVER_PACKAGE,
            REMINISCENCES_RECEIVER_CLASS,

            THERAPY_RECEIVER_PACKAGE,
            THERAPY_RECEIVER_CLASS,

            VIDEOCONFERENCE_RECEIVER_PACKAGE,
            VIDEOCONFERENCE_RECEIVER_CLASS,

            LINPHONE_RECEIVER_PACKAGE,
            LINPHONE_RECEIVER_CLASS,

            VOD_RECEIVER_PACKAGE,
            VOD_RECEIVER_CLASS,

            HELP_RECEIVER_PACKAGE,
            HELP_RECEIVER_CLASS,

            INFO_RECEIVER_PACKAGE,
            INFO_RECEIVER_CLASS,

            MEMORY_RECEIVER_PACKAGE,
            MEMORY_RECEIVER_CLASS,

            EDUCATION_RECEIVER_PACKAGE,
            EDUCATION_RECEIVER_CLASS

};

    public ConnectionService(Context context, boolean homeLogin, String userId) {
        this.context = context;
        this.homeLogin = homeLogin;
        this.userId = userId;
        this.responseCode = 0;
    }

    @Override
    protected Void doInBackground(String... params) {

        dataResponse = getData(GET_DATA, params);
        videoResponse = getData(GET_VIDEO, params);
        eventsResponse = getData(GET_EVENTS, params);

        logIn();

        return null;
    }

    private String getData(String getType, String... params) {

        String result = "";
        String url = params[0] + getType;
        URL urlObject;

        try {
            urlObject = new URL(url);
            HttpURLConnection con;
            con = (HttpURLConnection) urlObject.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);

            if (getType.equals(GET_DATA)) responseCode = con.getResponseCode();

            BufferedReader bufferedReader;

            bufferedReader = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = bufferedReader.readLine()) != null) {
                response.append(inputLine);
            }

            result = response.toString();

            bufferedReader.close();

        } catch (IOException ie) {
            Log.e(TAG, "getData(). IOException: " + ie.getMessage());
            return result;
        }

        return result;
    }

    private void logIn() {

        Log.d("CONNECTION", "Response code: " + responseCode);
        Log.d("CONNECTION", "Data response: " + dataResponse);
        Log.d("CONNECTION", "Video response: " + videoResponse);
        Log.d("CONNECTION", "Events response: " + eventsResponse);

        if (responseCode == 200 || responseCode == 400 || responseCode == 500) {

            if (!dataResponse.equals("Response from teleconsulta@2. GET / response: 3022. URL: /patients/" + userId + "/data") && !dataResponse.equals("")) {

                getUserData(dataResponse);

                getVideoProfile(videoResponse);
                getEvents(eventsResponse);
                getData(dataResponse);

                //If the module was started during the home module start, the home app will be relaunched:
                if (homeLogin) {

                    Intent launcher = new Intent();

                    final PackageManager packageManager = context.getPackageManager();

                    String homePackage = HOME_PACKAGE_AAL;

                    Intent intent = packageManager.getLaunchIntentForPackage(homePackage);

                    if (intent == null) {

                        homePackage = HOME_PACKAGE_HMI;

                        intent = packageManager.getLaunchIntentForPackage(homePackage);
                    }

                    if (intent != null) {
                        String HOME_LAUNCHER_CLASS = "com.shtvsolution.home.SplashActivity";
                        launcher.setComponent(new ComponentName(homePackage, HOME_LAUNCHER_CLASS));
                        context.startActivity(launcher);
                        ((MainActivity)context).finish();
                        this.context = null;
                    }

                    else Log.e(TAG, "Start Home launcher activity in logIn(): Home app is not installed");
                }
            }

            else {
                hideLoadingScreen();
                String invalidIdMessage = context.getResources().getString(R.string.id_not_valid) + userId;
                ToastUtils.makeCustomToast(context.getApplicationContext(), invalidIdMessage, DEFAULT_TOAST_SIZE, DEFAULT_TOAST_DURATION);
            }
        }

        else {
            hideLoadingScreen();
            String connectionError = context.getResources().getString(R.string.connection_error);
            ToastUtils.makeCustomToast(context.getApplicationContext(), connectionError, DEFAULT_TOAST_SIZE, DEFAULT_TOAST_DURATION);
        }
    }

    private void getEvents(String response) {

        try {
            JSONObject responseJson = new JSONObject(response);
            JSONArray eventsJsons = responseJson.getJSONArray("events");

            Intent launcher = new Intent();

            String EVENT_RECEIVER_PACKAGE = "com.shtvsolution.calendario";
            String EVENT_RECEIVER_CLASS = "com.shtvsolution.calendario.services.EventReceiverService";

            launcher.setComponent(new ComponentName(EVENT_RECEIVER_PACKAGE, EVENT_RECEIVER_CLASS));

            launcher.putExtra("numberOfEvents", eventsJsons.length());

            //All events are sent in a single Intent:
            for (int i = 0; i < eventsJsons.length(); i++)
                launcher.putExtra("event" + i, eventsJsons.get(i).toString());

            context.startService(launcher);

        } catch (JSONException jse) {
            Log.e(TAG, "getEvents. JSONException: " + jse.getMessage());
        }
    }

    private void getVideoProfile(String response) {

        Intent launcher = new Intent();

        String VOD_PROFILE_RECEIVER_PACKAGE = "com.shtvsolution.videoconferencia";
        String VOD_PROFILE_RECEIVER_CLASS = "com.shtvsolution.videoconferencia.services.UserContactsReceiverService";

        launcher.setComponent(new ComponentName(VOD_PROFILE_RECEIVER_PACKAGE, VOD_PROFILE_RECEIVER_CLASS));
        launcher.putExtra("datoContacto", response);

        context.startService(launcher);

        launcher = new Intent();
        launcher.putExtra("aalLogin", true);

        String LINPH_PROFILE_RECEIVER_PACKAGE = "com.shtvsolution.videoconferencia";
        String LINPH_PROFILE_RECEIVER_CLASS = "com.shtvsolution.services.UserContactsReceiverService";

        launcher.setComponent(new ComponentName(LINPH_PROFILE_RECEIVER_PACKAGE, LINPH_PROFILE_RECEIVER_CLASS));
        launcher.putExtra("datoContacto", response);

        context.startService(launcher);
    }

    private void getData(String response) {

        Intent launcher = new Intent();

        String MEASURES_PROFILE_RECEIVER_PACKAGE = "com.shtvsolution.medidas";
        String MEASURES_PROFILE_RECEIVER_CLASS = "com.shtvsolution.medidas.services.UserMeasuresReceiverService";

        launcher.setComponent(new ComponentName(MEASURES_PROFILE_RECEIVER_PACKAGE, MEASURES_PROFILE_RECEIVER_CLASS));
        launcher.putExtra("datos", response);

        context.startService(launcher);
    }

    //Method that retrieves all the user data and sends it to all the modules:
    private void getUserData(String response) {

        JSONObject dataJson = transformToJson(response);

        int userId = -1;
        String userName = null;
        String avatarUri = null;
        String[] measuresArray = null;
        String freeMeasureName = null;

        try {
            userId = dataJson.getInt("id");
            userName = dataJson.getString("name");
            avatarUri = dataJson.getString("avatar");

            JSONArray measuresJson = dataJson.getJSONArray("measures");

            measuresArray = new String[measuresJson.length()];

            for (int i = 0, j = 0; i < measuresJson.length(); i++) {

                JSONObject measure = (JSONObject) measuresJson.get(i);

                if (measure != null) {
                    String type = measure.getString("type");
                    measuresArray[j++] = type;
                    if (type.equals("MEASURE_XX"))
                        freeMeasureName = measure.getString("name");
                }
            }

        } catch (JSONException jse) {
            Log.e(TAG, "getUserData. JSONException: " + jse.getMessage());
        }

        Intent launcher;

        for (int packageName = 0, className = 1; className < dataReceiverPackages.length; packageName += 2, className += 2) {

            launcher = new Intent();

            launcher.setComponent(new ComponentName(dataReceiverPackages[packageName], dataReceiverPackages[className]));

            launcher.putExtra("userId", userId);
            launcher.putExtra("userName", userName);
            launcher.putExtra("avatarUri", avatarUri);

            if (dataReceiverPackages[packageName].equals(CALENDAR_RECEIVER_PACKAGE)) {
                launcher.putExtra("userMeasures", measuresArray);
                launcher.putExtra("freeMeasureName", freeMeasureName);
            }

            context.startService(launcher);
        }

        //Refreshing the user info view:
        ((MainActivity)context).setUserInformation();

        String welcomeMessageHead = context.getResources().getString(R.string.welcome_message_head);
        String welcomeMessageTail = context.getResources().getString(R.string.welcome_message_tail);
        String welcomeMessage = welcomeMessageHead + userName + welcomeMessageTail;

        hideLoadingScreen();

        ToastUtils.makeCustomToast(context.getApplicationContext(), welcomeMessage, DEFAULT_TOAST_SIZE, DEFAULT_TOAST_DURATION);
    }

    //Method that tells the MainActivity to hide the loading screen:
    private void hideLoadingScreen() {
        ((MainActivity)context).hideLoadingScreen();

        //To avoid toasts overlapping with the loading screen, the thread is paused for 80 milliseconds after hiding the loading screen:
        try {
            Thread.sleep(80);
        } catch (InterruptedException ie) {
            Log.e(TAG, "hideLoadingScreen(). InterruptedException: " + ie.getMessage());
        }
    }

    //Method that transforms a response string into a json, so the data is easier to retrieve:
    private JSONObject transformToJson(String response) {

        JSONObject dataJson = null;

        try {
            JSONObject auxJson = new JSONObject(response);

            if (auxJson.has("data"))
                dataJson = auxJson.getJSONObject("data");

            else dataJson = auxJson;

        } catch (JSONException jse) {
            Log.e(TAG, "transformToJson. JSONException: " + jse.getMessage());
        }

        return dataJson;
    }

    @Override
    protected void onPostExecute(Void v) {
        this.context = null;
    }
}
