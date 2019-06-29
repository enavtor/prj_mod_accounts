package com.droidmare.accounts.views.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droidmare.accounts.R;
import com.droidmare.accounts.services.ConnectionService;
import com.droidmare.accounts.utils.ImageUtils;
import com.droidmare.accounts.utils.ToastUtils;
import com.droidmare.accounts.services.DateCheckerService;
import com.droidmare.accounts.services.UserDataReceiverService;
import com.droidmare.statistics.StatisticAPI;
import com.droidmare.statistics.StatisticService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getCanonicalName();

    private static final String AMLOGIC_DTV = "com.amlogic.DVBPlayer";
    private static final String DTV_PACKAGE = AMLOGIC_DTV;

    //Layouts containers:
    private RelativeLayout loginLayoutContainer;
    private RelativeLayout createUserLayoutContainer;

    //Login layout elements:
    private EditText loginNicknameTextBox;
    private EditText loginPasswordTextBox;

    private LinearLayout loginButton;
    private LinearLayout createNewUserButton;

    //Create new user layout elements:
    private String newUserEncodedAvatar;

    private ImageView newUserAvatarPreviewBox;

    private EditText newUserNameTextBox;
    private EditText newUserSurnameTextBox;
    private EditText newUserNicknameTextBox;
    private EditText newUserPasswordTextBox;

    private LinearLayout acceptButton;
    private LinearLayout cancelButton;

    //FilesActivity attributes
    private boolean justCreated;

    private Handler onPauseHandler;

    private Runnable onPauseRunnable;

    private StatisticService statistic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String canonicalName = getClass().getCanonicalName();

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

        if (!DateCheckerService.isInstantiated)
            startService(new Intent(getApplicationContext(), DateCheckerService.class));
        else DateCheckerService.setActivitiesDate();

        initializeViews();

        setButtonsBehaviour();

        setUserInformation();
        setVersionNumber();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!justCreated)
            statistic.sendStatistic(StatisticAPI.StatisticType.APP_TRACK, StatisticService.ON_RESUME, getClass().getCanonicalName());

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

        switch (keyCode) {

            case KeyEvent.KEYCODE_PROG_GREEN:
                if (!loginNicknameTextBox.getText().toString().equals("")) login();
                else
                    ToastUtils.makeDefaultCustomToast(getApplicationContext(), getString(R.string.id_not_entered));
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

    @Override  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            newUserEncodedAvatar = data.getStringExtra("encodedAvatar");
            newUserAvatarPreviewBox.setImageBitmap(ImageUtils.decodeBitmapString(newUserEncodedAvatar));
        }
    }

    private void initializeViews() {

        //Layouts views container:
        loginLayoutContainer = findViewById(R.id.login_layout);
        createUserLayoutContainer = findViewById(R.id.create_user_layout);

        //Login layout views:
        loginNicknameTextBox = findViewById(R.id.nickname_input_box);
        loginPasswordTextBox = findViewById(R.id.password_input_box);

        loginButton = findViewById(R.id.login_affirmative_button);
        createNewUserButton = findViewById(R.id.login_create_user_button);

        //Create user layout views:
        newUserEncodedAvatar = "";

        BitmapDrawable defaultAvatar = (BitmapDrawable) getDrawable(R.drawable.photo);

        if (defaultAvatar != null)
            newUserEncodedAvatar = ImageUtils.encodeBitmapImage(defaultAvatar.getBitmap());

        newUserAvatarPreviewBox = findViewById(R.id.create_user_avatar_preview_box);

        newUserAvatarPreviewBox.setImageBitmap(ImageUtils.decodeBitmapString(newUserEncodedAvatar));

        newUserNameTextBox = findViewById(R.id.create_user_name_input_box);
        newUserSurnameTextBox = findViewById(R.id.create_user_surname_input_box);
        newUserNicknameTextBox = findViewById(R.id.create_user_nickname_input_box);
        newUserPasswordTextBox = findViewById(R.id.create_user_password_input_box);

        acceptButton = findViewById(R.id.create_user_affirmative_button);
        cancelButton = findViewById(R.id.create_user_dismiss_layout_button);
    }

    private void setButtonsBehaviour() {

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        createNewUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginLayoutContainer.setVisibility(View.GONE);
                createUserLayoutContainer.setVisibility(View.VISIBLE);
                createUserLayoutContainer.requestFocus();
            }
        });

        newUserAvatarPreviewBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), FilesActivity.class), 0);
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserLayoutContainer.setVisibility(View.GONE);
                loginLayoutContainer.setVisibility(View.VISIBLE);
                loginLayoutContainer.requestFocus();
            }
        });

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
                if (!loginNicknameTextBox.getText().toString().equals("")) login();
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

    private void createUser() {

        JSONObject userJson = new JSONObject();

        try {
            userJson.put(UserDataReceiverService.USER_NAME_FIELD, newUserNameTextBox.getText().toString());
            userJson.put(UserDataReceiverService.USER_SURNAME_FIELD, newUserSurnameTextBox.getText().toString());
            userJson.put(UserDataReceiverService.USER_AVATAR_FIELD, newUserEncodedAvatar);
            userJson.put(UserDataReceiverService.USER_NICKNAME_FIELD, newUserNicknameTextBox.getText().toString());
            userJson.put(UserDataReceiverService.USER_PASSWORD_FIELD, newUserPasswordTextBox.getText().toString());

        } catch (JSONException jsonException) {
            Log.e(TAG, "createUser. JSONException: " + jsonException.getMessage());
        }

        Intent createIntent = new Intent(getApplicationContext(), ConnectionService.class);

        createIntent.putExtra("requestedOperation", ConnectionService.CREATE);
        createIntent.putExtra("userJsonString", userJson.toString());

        startService(createIntent);
    }

    private void login() {

        findViewById(R.id.layout_loading).setVisibility(View.VISIBLE);

        String userNick = loginNicknameTextBox.getText().toString();
        String userPass = loginPasswordTextBox.getText().toString();

        Intent loginIntent = new Intent(getApplicationContext(), ConnectionService.class);

        loginIntent.putExtra("requestedOperation", ConnectionService.LOGIN);
        loginIntent.putExtra(UserDataReceiverService.USER_NICKNAME_FIELD, userNick);
        loginIntent.putExtra(UserDataReceiverService.USER_PASSWORD_FIELD, userPass);

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

        if (UserDataReceiverService.getUserId() != null) {

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