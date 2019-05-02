package com.droidmare.accounts.views;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.droidmare.R;
import com.droidmare.accounts.services.ConnectionService;
import com.droidmare.accounts.Utils.STBUtils;
import com.droidmare.accounts.Utils.ToastUtils;
import com.droidmare.accounts.services.DateCheckerService;
import com.droidmare.accounts.services.UserDataReceiverService;
import com.droidmare.statistics.StatisticAPI;
import com.droidmare.statistics.StatisticService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.droidmare.accounts.Utils.ToastUtils.DEFAULT_TOAST_DURATION;
import static com.droidmare.accounts.Utils.ToastUtils.DEFAULT_TOAST_SIZE;

public class MainActivity extends AppCompatActivity {

    private EditText ID;

    //Modules api url receiver packages:
    private final String CALENDAR_RECEIVER_PACKAGE = "com.shtvsolution.calendario";
    private final String CALENDAR_API_RECEIVER_CLASS = "com.shtvsolution.calendario.services.ApiReceiverService";

    private final String STATISTICS_RECEIVER_PACKAGE = "com.shtvsolution.estadisticas";
    private final String STATISTICS_API_RECEIVER_CLASS = "com.shtvsolution.estadisticas.services.ApiReceiverService";

    private final String HOME_PACKAGE_AAL = "com.shtvsolution.home.aal";
    private final String HOME_PACKAGE_HMI = "com.shtvsolution.home.hmi";
    private final String HOME_API_RECEIVER_CLASS = "com.shtvsolution.home.services.ApiReceiverService";

    private final String VIDEO_RECEIVER_PACKAGE = "com.shtvsolution.videoconferencia";
    private final String VIDEO_API_RECEIVER_CLASS = "com.shtvsolution.videoconferencia.services.ApiReceiverService";

    private final String LINPHONE_RECEIVER_PACKAGE = "com.shtvsolution.videoconferencia";
    private final String LINPHONE_API_RECEIVER_CLASS = "com.shtvsolution.services.ApiReceiverService";

    private final String VOD_RECEIVER_PACKAGE = "com.shtvsolution.vod";
    private final String VOD_API_RECEIVER_CLASS = "com.shtvsolution.vod.services.ApiReceiverService";

    private static final String AMLOGIC_DTV = "com.amlogic.DVBPlayer";
    private static final String DTV_PACKAGE = AMLOGIC_DTV;

    private Switch backend;

    //String array with all the api url receiver packages names:
    private final String[] apiReceiverPackages = {

            CALENDAR_RECEIVER_PACKAGE,
            CALENDAR_API_RECEIVER_CLASS,

            STATISTICS_RECEIVER_PACKAGE,
            STATISTICS_API_RECEIVER_CLASS,

            HOME_PACKAGE_AAL,
            HOME_API_RECEIVER_CLASS,

            HOME_PACKAGE_HMI,
            HOME_API_RECEIVER_CLASS,

            VIDEO_RECEIVER_PACKAGE,
            VIDEO_API_RECEIVER_CLASS,

            LINPHONE_RECEIVER_PACKAGE,
            LINPHONE_API_RECEIVER_CLASS,

            VOD_RECEIVER_PACKAGE,
            VOD_API_RECEIVER_CLASS
    };

    //Control variable that indicates if the activity was launched when the home module was starting:
    private boolean homeLogin;

    private boolean justCreated;

    private Handler onPauseHandler;

    private Runnable onPauseRunnable;

    private StatisticService statistic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String canonicalName =  getClass().getCanonicalName();

        onPauseHandler = new Handler();
        onPauseRunnable = new Runnable() {
            @Override
            public void run() {
                statistic.sendStatistic(StatisticAPI.StatisticType.APP_TRACK, StatisticService.ON_PAUSE, canonicalName);
            }
        };

        justCreated = true;

        //The soft keyboard is hidden in order to avoid it being displayed when the module is launched:
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        homeLogin = getIntent().getBooleanExtra("homeLogin", false);

        statistic = new StatisticService(this);
        statistic.sendStatistic(StatisticAPI.StatisticType.APP_TRACK, StatisticService.ON_CREATE, getClass().getCanonicalName());

        setContentView(R.layout.activity_main);

        DateCheckerService.setMainActivityReference(this);

        if (!DateCheckerService.isInstantiated) startService(new Intent(getApplicationContext(), DateCheckerService.class));
        else DateCheckerService.setActivitiesDate();

        backend = findViewById(R.id.BtnSwitch);
        ID = findViewById(R.id.cuenta_identificador);
        setUserInformation();
        initButtons();

        //Version in Footer
        setVersionNumber();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!justCreated) statistic.sendStatistic(StatisticAPI.StatisticType.APP_TRACK, StatisticService.ON_RESUME, getClass().getCanonicalName());

        else justCreated = false;

        homeLogin = getIntent().getBooleanExtra("homeLogin", false);

        setUserInformation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPauseHandler.postDelayed(onPauseRunnable, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onPauseHandler.removeCallbacks(onPauseRunnable);
        statistic.sendStatistic(StatisticAPI.StatisticType.APP_TRACK, StatisticService.ON_DESTROY, getClass().getCanonicalName());
    }

    private void initButtons() {

        LinearLayout tv = findViewById(R.id.ir_red);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=getPackageManager().getLaunchIntentForPackage(DTV_PACKAGE);
                if(intent!=null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });

        LinearLayout login = findViewById(R.id.ir_green);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ID.getText().toString().equals("")) logIn();
                else ToastUtils.makeCustomToast(getApplicationContext(), getResources().getString(R.string.id_not_entered), DEFAULT_TOAST_SIZE, DEFAULT_TOAST_DURATION);
            }
        });

        LinearLayout settings = findViewById(R.id.ir_yellow);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=getPackageManager().getLaunchIntentForPackage("com.shtvsolution.home.aal");
                if(intent!=null) launchStatusActivity();
            }
        });
    }

    private void launchStatusActivity() {
        Intent statusIntent = new Intent();

        statusIntent.setComponent(new ComponentName("com.shtvsolution.home.aal", "com.shtvsolution.home.views.DialogPassword"));

        statusIntent.putExtra("password", "1234");
        statusIntent.putExtra("userStatus", true);

        startActivity(statusIntent);
    }

    private void logIn() {

        String userId = ID.getText().toString();

        String url = "patients/" + userId;

        if (backend.isChecked()) {
            String BASE_URL_AAL_ITA = "http://tvassistdem-backend.istc.cnr.it/";
            url = BASE_URL_AAL_ITA + url;
            sendApiUrl(BASE_URL_AAL_ITA);
        } else {
            //APIs' URLs:
            String BASE_URL_AAL_ESP = "http://api.shtvsolution.com/teleconsulta/v2/";
            url = BASE_URL_AAL_ESP + url;
            sendApiUrl(BASE_URL_AAL_ESP);
        }

        connectToApi(url, userId);
    }

    private void connectToApi(String url, String userId) {

        new ConnectionService(this, homeLogin, userId).execute(url);

        findViewById(R.id.layout_loading).setVisibility(View.VISIBLE);
    }

    public void hideLoadingScreen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.layout_loading).setVisibility(View.GONE);
            }
        });
    }

    //Method that sends the selected API url to all the modules that need it:
    private void sendApiUrl (String apiUrl) {
        Intent launcher;

        for (int packageName = 0, className = 1; className < apiReceiverPackages.length; packageName += 2, className += 2) {

            launcher = new Intent();

            launcher.setComponent(new ComponentName(apiReceiverPackages[packageName], apiReceiverPackages[className]));

            launcher.putExtra("apiUrl", apiUrl);

            startService(launcher);
        }
    }

    //Method for setting the date text displayed on the upper right corner of the application:
    public void setDateText () {

        final Calendar calendar = Calendar.getInstance();
        long time =  calendar.getTimeInMillis();
        Locale localeDate = Locale.getDefault();

        SimpleDateFormat simpleDate = new SimpleDateFormat(getString(R.string.date),localeDate);
        String date = simpleDate.format(time);

        final String upperDate = date.substring(0,1).toUpperCase() + date.substring(1);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView dateText = findViewById(R.id.txt_date);
                dateText.setText(upperDate);
            }
        });
    }

    //Method that configures the user information view:
    public void setUserInformation () {
        UserDataReceiverService.readSharedPrefs(getApplicationContext());

        if (UserDataReceiverService.getUserId() != -1) {

            final ImageView avatar = findViewById(R.id.user_photo);
            final TextView name = findViewById(R.id.user_name);
            final TextView id = findViewById(R.id.user_id);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    avatar.setImageBitmap(UserDataReceiverService.getAvatarImage());
                    name.setText(UserDataReceiverService.getUserName());
                    String idViewText = "id: " + UserDataReceiverService.getUserId();
                    id.setText(idViewText);
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_PROG_GREEN:
                if (!ID.getText().toString().equals("")) logIn();
                else ToastUtils.makeCustomToast(getApplicationContext(), getResources().getString(R.string.id_not_entered), DEFAULT_TOAST_SIZE, DEFAULT_TOAST_DURATION);
                break;
            case KeyEvent.KEYCODE_PROG_YELLOW:
                Intent intent=getPackageManager().getLaunchIntentForPackage("com.shtvsolution.home.aal");
                if(intent!=null) launchStatusActivity();
                break;
            case KeyEvent.KEYCODE_PROG_RED:
                Intent intentTv=getPackageManager().getLaunchIntentForPackage(DTV_PACKAGE);
                if(intentTv!=null) {
                    intentTv.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentTv);
                }
                break;
            default:
        }

        return super.onKeyUp(keyCode, event);
    }

    private void setVersionNumber(){
        TextView appVersion=findViewById(R.id.version_number);
        appVersion.append(STBUtils.getAppVersionName(getApplicationContext()));
    }
}