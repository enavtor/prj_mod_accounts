package com.droidmare.accounts.views;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidmare.accounts.R;
import com.droidmare.accounts.services.ConnectionService;
import com.droidmare.accounts.utils.ToastUtils;
import com.droidmare.accounts.services.DateCheckerService;
import com.droidmare.accounts.services.UserDataReceiverService;
import com.droidmare.statistics.StatisticAPI;
import com.droidmare.statistics.StatisticService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getCanonicalName();

    private static final String AMLOGIC_DTV = "com.amlogic.DVBPlayer";
    private static final String DTV_PACKAGE = AMLOGIC_DTV;

    private EditText nicknameTextBox;
    private EditText passwordTextBox;

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

        statistic = new StatisticService(this);
        statistic.sendStatistic(StatisticAPI.StatisticType.APP_TRACK, StatisticService.ON_CREATE, getClass().getCanonicalName());

        setContentView(R.layout.activity_main);

        DateCheckerService.setMainActivityReference(this);
        ConnectionService.setMainActivityReference(this);

        if (!DateCheckerService.isInstantiated) startService(new Intent(getApplicationContext(), DateCheckerService.class));
        else DateCheckerService.setActivitiesDate();

        nicknameTextBox = findViewById(R.id.cuenta_identificador);
        passwordTextBox = findViewById(R.id.cuenta_identificador);

        setButtonsBehaviour();

        setUserInformation();
        setVersionNumber();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!justCreated) statistic.sendStatistic(StatisticAPI.StatisticType.APP_TRACK, StatisticService.ON_RESUME, getClass().getCanonicalName());

        else justCreated = false;

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode){

            case KeyEvent.KEYCODE_PROG_GREEN:
                if (!nicknameTextBox.getText().toString().equals("")) logIn();
                else ToastUtils.makeDefaultCustomToast(getApplicationContext(), getString(R.string.id_not_entered));
                break;

            case KeyEvent.KEYCODE_PROG_YELLOW:
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.shtvsolution.home.aal");
                if (intent != null) launchStatusActivity();
                break;

            case KeyEvent.KEYCODE_PROG_RED:
                Intent intentTv = getPackageManager().getLaunchIntentForPackage(DTV_PACKAGE);
                if (intentTv != null) startActivity(intentTv);
                break;

            default:
        }

        return super.onKeyUp(keyCode, event);
    }

    private void setButtonsBehaviour() {

        findViewById(R.id.ir_red).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=getPackageManager().getLaunchIntentForPackage(DTV_PACKAGE);
                if(intent != null) startActivity(intent);
            }
        });

        findViewById(R.id.ir_green).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!nicknameTextBox.getText().toString().equals("")) logIn();
                else ToastUtils.makeDefaultCustomToast(getApplicationContext(), getResources().getString(R.string.id_not_entered));
            }
        });

        findViewById(R.id.ir_yellow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.shtvsolution.home.aal");
                if (intent != null) launchStatusActivity();
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

        String userNick = nicknameTextBox.getText().toString();
        String userPass = passwordTextBox.getText().toString();

        Intent loginIntent = new Intent(getApplicationContext(), ConnectionService.class);

        loginIntent.putExtra("requestedOperation", "GET");
        loginIntent.putExtra("userNick", userNick);
        loginIntent.putExtra("userPass", userPass);

        startService(loginIntent);
    }

    public void hideLoadingScreen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.layout_loading).setVisibility(View.GONE);
            }
        });
    }

    //Method for setting the date text displayed on the upper right corner of the application:
    public void setDateText() {

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
    public void setUserInformation() {
        UserDataReceiverService.readSharedPrefs(getApplicationContext());

        final ImageView avatar = findViewById(R.id.user_photo);
        final TextView name = findViewById(R.id.user_name);
        final TextView id = findViewById(R.id.user_id);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                avatar.setImageBitmap(UserDataReceiverService.getDecodedAvatar());
                name.setText(UserDataReceiverService.getUserName());
                id.setText(UserDataReceiverService.getUserNickname());
            }
        });
    }

    private void setVersionNumber() {

        String versionNumber = "";

        try {
            versionNumber = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;

        }catch (PackageManager.NameNotFoundException nfe){
            Log.e(TAG, "setVersionNumber. NameNotFoundException: " + nfe.getMessage());
        }

        ((TextView) findViewById(R.id.version_number)).setText(versionNumber);
    }
}