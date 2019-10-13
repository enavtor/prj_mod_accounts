package com.droidmare.accounts.views.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import com.droidmare.accounts.services.DataDeleterService;
import com.droidmare.accounts.services.UserDataService;
import com.droidmare.accounts.utils.ImageUtils;
import com.droidmare.accounts.utils.ToastUtils;
import com.droidmare.accounts.services.DateCheckerService;

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
    private RelativeLayout userLoggedLayoutContainer;
    private RelativeLayout createUserLayoutContainer;

    //Login layout elements:
    private EditText loginNicknameTextBox;
    private EditText loginPasswordTextBox;

    private LinearLayout loginButton;
    private LinearLayout createUserButton;

    //Login layout elements:
    private RelativeLayout loggedUserAvatar;
    private TextView loggedUserName;
    private TextView loggedUserNickname;

    private LinearLayout logoutButton;
    private LinearLayout editUserButton;
    private LinearLayout deleteUserButton;

    //Create new user layout elements:
    private String newUserEncodedAvatar;

    private RelativeLayout newUserAvatarClickableBox;
    private ImageView newUserAvatarPreviewBox;

    private EditText newUserNameTextBox;
    private EditText newUserSurnameTextBox;
    private EditText newUserNicknameTextBox;
    private EditText newUserPasswordTextBox;

    private LinearLayout acceptButton;
    private LinearLayout cancelButton;

    //Control variables:
    private boolean userLogged;
    private boolean editingUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //The soft keyboard is hidden in order to avoid it being displayed when the module is launched:
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        setContentView(R.layout.activity_main);

        DateCheckerService.setMainActivityReference(this);
        ConnectionService.setMainActivityReference(this);
        UserDataService.setMainActivityReference(this);

        if (!DateCheckerService.isInstantiated)
            startService(new Intent(getApplicationContext(), DateCheckerService.class));
        else DateCheckerService.setActivitiesDate();

        initializeViews();

        setButtonsBehaviour();

        setUserInformation();

        setVersionNumber();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {

            case KeyEvent.KEYCODE_PROG_GREEN:
                if (!loginNicknameTextBox.getText().toString().equals("")) login(null, null);
                else
                    ToastUtils.makeCustomToast(getApplicationContext(), getString(R.string.id_not_entered));
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
        userLoggedLayoutContainer = findViewById(R.id.user_logged_layout);
        createUserLayoutContainer = findViewById(R.id.create_user_layout);

        //Login layout views:
        loginNicknameTextBox = findViewById(R.id.nickname_input_box);
        loginPasswordTextBox = findViewById(R.id.password_input_box);

        loginButton = findViewById(R.id.login_affirmative_button);
        createUserButton = findViewById(R.id.login_create_user_button);

        //Logged layout views:
        loggedUserAvatar = findViewById(R.id.user_avatar_box);
        loggedUserName = findViewById(R.id.logged_user_name);
        loggedUserNickname = findViewById(R.id.logged_user_nick);

        logoutButton = findViewById(R.id.logout_button);
        editUserButton = findViewById(R.id.edit_user_button);
        deleteUserButton = findViewById(R.id.delete_user_button);

        //Create user layout views:
        newUserEncodedAvatar = "";

        BitmapDrawable defaultAvatar = (BitmapDrawable) getDrawable(R.drawable.photo);

        if (defaultAvatar != null)
            newUserEncodedAvatar = ImageUtils.encodeBitmapImage(defaultAvatar.getBitmap());

        newUserAvatarPreviewBox = findViewById(R.id.create_user_avatar_preview_box);
        newUserAvatarPreviewBox.setImageBitmap(ImageUtils.decodeBitmapString(newUserEncodedAvatar));

        newUserAvatarClickableBox = findViewById(R.id.create_user_avatar_clickable_box);

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
                if (!loginNicknameTextBox.getText().toString().equals("")) login(null, null);
                else ToastUtils.makeCustomToast(getApplicationContext(), getResources().getString(R.string.id_not_entered));
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        editUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editingUser = true;
                showCreateEditUserLayout();
            }
        });

        deleteUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        });

        createUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editingUser = false;
                showCreateEditUserLayout();
            }
        });

        newUserAvatarClickableBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), FilesActivity.class), 0);
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editingUser) editUser();
                else createUser();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserLayoutContainer.setVisibility(View.GONE);
                if (userLogged) userLoggedLayoutContainer.setVisibility(View.VISIBLE);
                else loginLayoutContainer.setVisibility(View.VISIBLE);
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
                loginButton.performClick();
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

    public void login(String userNick, String userPass) {

        if (createUserLayoutContainer.getVisibility() == View.VISIBLE)
            cancelButton.performClick();

        findViewById(R.id.layout_loading).setVisibility(View.VISIBLE);

        if (userNick == null) {
            userNick = loginNicknameTextBox.getText().toString();
            userPass = loginPasswordTextBox.getText().toString();
        }

        Intent loginIntent = new Intent(getApplicationContext(), ConnectionService.class);

        loginIntent.putExtra(ConnectionService.REQUESTED_OPERATION_FIELD, ConnectionService.LOGIN);
        loginIntent.putExtra(UserDataService.USER_NICKNAME_FIELD, userNick);
        loginIntent.putExtra(UserDataService.USER_PASSWORD_FIELD, userPass);

        startService(loginIntent);

        loginNicknameTextBox.setText("");
        loginPasswordTextBox.setText("");
    }

    public void logout() {

        userLogged = false;

        startService(new Intent(getApplicationContext(), UserDataService.class));

        startService(new Intent(getApplicationContext(), DataDeleterService.class));
    }

    private void showCreateEditUserLayout() {
        if (userLogged) userLoggedLayoutContainer.setVisibility(View.GONE);
        else loginLayoutContainer.setVisibility(View.GONE);

        createUserLayoutContainer.setVisibility(View.VISIBLE);
        createUserLayoutContainer.requestFocus();

        if (editingUser) {
            newUserNameTextBox.setText(UserDataService.getUserName());
            newUserSurnameTextBox.setText(UserDataService.getUserSurname());
            newUserAvatarPreviewBox.setImageDrawable(new BitmapDrawable(getResources(), UserDataService.getDecodedAvatar()));
            newUserNicknameTextBox.setText(UserDataService.getUserNickname());
            newUserPasswordTextBox.setText(UserDataService.getUserPassword());
        }
    }

    public void createUser() {

        Intent createIntent = new Intent(getApplicationContext(), ConnectionService.class);

        createIntent.putExtra(ConnectionService.REQUESTED_OPERATION_FIELD, ConnectionService.CREATE);
        createIntent.putExtra(UserDataService.USER_JSON_FIELD, getUserAttributesJson().toString());

        startService(createIntent);
    }

    public void editUser() {
        Intent createIntent = new Intent(getApplicationContext(), ConnectionService.class);

        JSONObject userAttributesJson = getUserAttributesJson();

        if (checkNewAttributes(userAttributesJson)) {

            createIntent.putExtra(ConnectionService.REQUESTED_OPERATION_FIELD, ConnectionService.EDIT);
            createIntent.putExtra(UserDataService.USER_JSON_FIELD, getUserAttributesJson().toString());

            startService(createIntent);
        }

        else ToastUtils.makeCustomToast(getApplicationContext(), "The user info was not modified!!!");
    }

    private JSONObject getUserAttributesJson() {
        JSONObject userJson = new JSONObject();

        try {
            if (editingUser) userJson.put(UserDataService.USER_ID_FIELD, UserDataService.getUserId());
            userJson.put(UserDataService.USER_NAME_FIELD, newUserNameTextBox.getText().toString());
            userJson.put(UserDataService.USER_SURNAME_FIELD, newUserSurnameTextBox.getText().toString());
            userJson.put(UserDataService.USER_AVATAR_FIELD, newUserEncodedAvatar);
            userJson.put(UserDataService.USER_NICKNAME_FIELD, newUserNicknameTextBox.getText().toString());
            userJson.put(UserDataService.USER_PASSWORD_FIELD, newUserPasswordTextBox.getText().toString());

        } catch (JSONException jsonException) {
            Log.e(TAG, "getUserAttributesJson. JSONException: " + jsonException.getMessage());
        }

        return userJson;
    }

    private boolean checkNewAttributes(JSONObject attributesJson) {
        try {
            if (UserDataService.getUserName().equals(attributesJson.getString(UserDataService.USER_NAME_FIELD)))
                return true;

            if (UserDataService.getUserSurname().equals(attributesJson.getString(UserDataService.USER_SURNAME_FIELD)))
                return true;

            if (UserDataService.getEncodedAvatar().equals(attributesJson.getString(UserDataService.USER_AVATAR_FIELD)))
                return true;

            if (UserDataService.getUserNickname().equals(attributesJson.getString(UserDataService.USER_NICKNAME_FIELD)))
                return true;

            if (UserDataService.getUserPassword().equals(attributesJson.getString(UserDataService.USER_PASSWORD_FIELD)))
                return true;

        } catch (JSONException jsonException) {
            Log.e(TAG, "checkNewAttributes. JSONException: " + jsonException.getMessage());
        }

        return false;
    }

    public void deleteUser() {

        Intent loginIntent = new Intent(getApplicationContext(), ConnectionService.class);

        loginIntent.putExtra(ConnectionService.REQUESTED_OPERATION_FIELD, ConnectionService.DELETE);

        String deleteUserJson = "{ \"_id\": \"" + UserDataService.getUserId() + "\"}";

        loginIntent.putExtra(UserDataService.USER_JSON_FIELD, deleteUserJson);

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

        UserDataService.readSharedPrefs(getApplicationContext());

        userLogged = UserDataService.getUserId() != null;

        final ImageView avatar = findViewById(R.id.user_photo);
        final TextView name = findViewById(R.id.user_name);
        final TextView id = findViewById(R.id.user_id);

        if (userLogged) {

            final Bitmap userAvatar = UserDataService.getDecodedAvatar();
            final String userName = UserDataService.getUserFullName();
            final String userNickname = UserDataService.getUserNickname();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    userLoggedLayoutContainer.setVisibility(View.VISIBLE);
                    loginLayoutContainer.setVisibility(View.INVISIBLE);

                    avatar.setImageBitmap(userAvatar);
                    loggedUserAvatar.setBackground(new BitmapDrawable(getResources(), userAvatar));

                    name.setText(userName);
                    loggedUserName.setText(userName);

                    id.setText(userNickname);
                    loggedUserNickname.setText(userNickname);
                }
            });
        }

        else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    userLoggedLayoutContainer.setVisibility(View.INVISIBLE);
                    loginLayoutContainer.setVisibility(View.VISIBLE);

                    loginNicknameTextBox.requestFocus();

                    avatar.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.photo));
                    name.setText(getString(R.string.no_user));
                    id.setText(getString(R.string.no_id));
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