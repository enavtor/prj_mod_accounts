package com.droidmare.accounts.views.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

    public static final String DIALOG_TEXT_FIELD = "dialogText";
    public static final String ENCODED_AVATAR_FIELD = "encodedAvatar";

    private static final int file_activity_request_code = 0;
    private static final int dialog_logout_request_code = 1;
    private static final int dialog_edit_request_code = 2;
    private static final int dialog_delete_request_code = 3;

    //Layouts containers:
    private RelativeLayout loginLayoutContainer;
    private RelativeLayout userLoggedLayoutContainer;
    private RelativeLayout userParamsLayoutContainer;

    //Login layout elements:
    private EditText loginNicknameTextBox;
    private EditText loginPasswordTextBox;

    private LinearLayout loginButton;
    private LinearLayout createUserButton;

    //Login layout elements:
    private ImageView loggedUserAvatar;
    private TextView loggedUserName;
    private TextView loggedUserNickname;

    private LinearLayout logoutButton;
    private LinearLayout editUserButton;
    private LinearLayout deleteUserButton;

    //User params layout elements:
    private BitmapDrawable defaultAvatar;
    private String newUserEncodedAvatar;

    private RelativeLayout userParamsAvatarClickableBox;
    private ImageView userParamsAvatarPreviewBox;

    private EditText userParamsNameTextBox;
    private EditText userParamsSurnameTextBox;
    private EditText userParamsNicknameTextBox;
    private EditText userParamsPasswordTextBox;

    private TextView userParamsAcceptText;

    private LinearLayout userParamsAcceptButton;
    private LinearLayout userParamsCancelButton;

    //Control variables:
    private boolean userLogged;
    private boolean editingUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //The soft keyboard is hidden in order to avoid it being displayed when the module is launched:
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        setContentView(R.layout.activity_main);

        //In order to launch specific methods of this class from a service, that service needs a reference to this activity:
        DateCheckerService.setMainActivityReference(this);
        ConnectionService.setMainActivityReference(this);
        UserDataService.setMainActivityReference(this);

        //Since the DateCheckerService runs on the background, it only should be started if it is not already running(if it is, its method to update the date view is launched):
        if (!DateCheckerService.isInstantiated)
            startService(new Intent(getApplicationContext(), DateCheckerService.class));
        else DateCheckerService.setActivitiesDate();

        defaultAvatar = (BitmapDrawable) getDrawable(R.drawable.photo);

        //Now all the views can be initialized:
        initializeViews();

        setButtonsBehaviour();

        setUserInformation();

        setVersionNumber();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN && ToastUtils.cancelCurrentToast()) return true;

        else if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    if (userParamsLayoutContainer.getVisibility() == View.VISIBLE) {
                        userParamsCancelButton.performClick();
                        return true;
                    }
                    else finish();
                    break;

                case KeyEvent.KEYCODE_PROG_GREEN:
                    loginButton.performClick();
                    return true;

                case KeyEvent.KEYCODE_PROG_RED:
                    Intent intentTv = getPackageManager().getLaunchIntentForPackage(DTV_PACKAGE);
                    if (intentTv != null) startActivity(intentTv);
                    return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    //This method will receive the result returned by any activity launched from this one by calling startActivityForResult():
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == file_activity_request_code && resultCode == RESULT_OK) {
            newUserEncodedAvatar = data.getStringExtra(ENCODED_AVATAR_FIELD);
            userParamsAvatarPreviewBox.setImageBitmap(ImageUtils.decodeBitmapString(newUserEncodedAvatar));
        }

        else if (requestCode == dialog_logout_request_code && resultCode == RESULT_OK)
            logout();

        else if (requestCode == dialog_edit_request_code && resultCode == RESULT_OK)
            editUser();

        else if (requestCode == dialog_delete_request_code && resultCode == RESULT_OK)
            deleteUser();
    }

    //Method that initializes all the attributes that are related to UI elements (views):
    private void initializeViews() {

        //Layouts views container:
        loginLayoutContainer = findViewById(R.id.login_layout);
        userLoggedLayoutContainer = findViewById(R.id.user_logged_layout);
        userParamsLayoutContainer = findViewById(R.id.create_user_layout);

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

        //User params layout views:
        newUserEncodedAvatar = "";

        if (defaultAvatar != null)
            newUserEncodedAvatar = ImageUtils.encodeBitmapImage(defaultAvatar.getBitmap());

        userParamsAvatarPreviewBox = findViewById(R.id.user_params_avatar_preview_box);
        userParamsAvatarPreviewBox.setImageBitmap(ImageUtils.decodeBitmapString(newUserEncodedAvatar));

        userParamsAvatarClickableBox = findViewById(R.id.user_params_avatar_clickable_box);

        userParamsNameTextBox = findViewById(R.id.user_params_name_input_box);
        userParamsSurnameTextBox = findViewById(R.id.user_params_surname_input_box);
        userParamsNicknameTextBox = findViewById(R.id.user_params_nickname_input_box);
        userParamsPasswordTextBox = findViewById(R.id.user_params_password_input_box);

        userParamsAcceptText = findViewById(R.id.user_params_affirmative_text);

        userParamsAcceptButton = findViewById(R.id.user_params_affirmative_button);
        userParamsCancelButton = findViewById(R.id.user_params_dismiss_layout_button);
    }

    //Method that sets the onClickListener for each one of the clickable views inside this activity:
    private void setButtonsBehaviour() {

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!loginNicknameTextBox.getText().toString().equals("") && !loginPasswordTextBox.getText().toString().equals(""))
                    login(null, null);
                else ToastUtils.makeCustomToast(getApplicationContext(), getResources().getString(R.string.id_not_entered));
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayConfirmationDialog(getString(R.string.dialog_perform_logout), dialog_logout_request_code);
            }
        });

        editUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editingUser = true;
                displayUserParamsLayout();
            }
        });

        deleteUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayConfirmationDialog(getString(R.string.dialog_delete_user), dialog_delete_request_code);
            }
        });

        createUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editingUser = false;
                displayUserParamsLayout();
            }
        });

        userParamsAvatarClickableBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), FilesActivity.class), 0);
            }
        });

        userParamsAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editingUser) displayConfirmationDialog(getString(R.string.dialog_edit_user), dialog_edit_request_code);
                else createUser();
            }
        });

        userParamsCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userParamsLayoutContainer.setVisibility(View.GONE);

                if (userLogged) {
                    userLoggedLayoutContainer.setVisibility(View.VISIBLE);
                    logoutButton.requestFocus();
                }
                else {
                    loginLayoutContainer.setVisibility(View.VISIBLE);
                    loginButton.requestFocus();
                }
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
    }

    //Method that starts the ConnectionService to perform a login operation:
    public void login(String userNick, String userPass) {

        //If the login was launched after creating a new user, the layouts' visibility must be reset:
        if (userParamsLayoutContainer.getVisibility() == View.VISIBLE)
            userParamsCancelButton.performClick();

        //The logging distractor is displayed:
        findViewById(R.id.layout_loading).setVisibility(View.VISIBLE);

        //The value of userNick will depend on whether this method was launched from the login layout or the user parameters one:
        if (userNick == null) {
            userNick = loginNicknameTextBox.getText().toString();
            userPass = loginPasswordTextBox.getText().toString();
        }

        //Now the login can be performed:
        Intent loginIntent = new Intent(getApplicationContext(), ConnectionService.class);

        loginIntent.putExtra(ConnectionService.REQUESTED_OPERATION_FIELD, ConnectionService.LOGIN);
        loginIntent.putExtra(UserDataService.USER_NICKNAME_FIELD, userNick);
        loginIntent.putExtra(UserDataService.USER_PASSWORD_FIELD, userPass);

        startService(loginIntent);

        loginNicknameTextBox.setText("");
        loginPasswordTextBox.setText("");
    }

    //Method that clears all the data related to the user form this app and all the data from the other two apps:
    public void logout() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userLogged = false;

                //Whenever a logout operation takes place, the information on the user parameters layout view must be cleared:
                userParamsNameTextBox.setText("");
                userParamsSurnameTextBox.setText("");
                newUserEncodedAvatar = ImageUtils.encodeBitmapImage(defaultAvatar.getBitmap());
                userParamsAvatarPreviewBox.setImageDrawable(defaultAvatar);
                userParamsNicknameTextBox.setText("");
                userParamsPasswordTextBox.setText("");

                //Now the user information is cleared from the rest of elements within this app and the other applications:
                startService(new Intent(getApplicationContext(), UserDataService.class));
                startService(new Intent(getApplicationContext(), DataDeleterService.class));
            }
        });
    }

    private void displayUserParamsLayout() {
        if (userLogged) userLoggedLayoutContainer.setVisibility(View.GONE);
        else loginLayoutContainer.setVisibility(View.GONE);

        userParamsLayoutContainer.setVisibility(View.VISIBLE);
        userParamsLayoutContainer.requestFocus();

        //If the layout is going to be displayed to edit the current user, his/her information will be displayed as well on the layout views:
        if (editingUser) {
            userParamsAcceptText.setText(getString(R.string.accept_edit_user_button));
            userParamsNameTextBox.setText(UserDataService.getUserName());
            userParamsSurnameTextBox.setText(UserDataService.getUserSurname());
            newUserEncodedAvatar = UserDataService.getEncodedAvatar();
            userParamsAvatarPreviewBox.setImageDrawable(new BitmapDrawable(getResources(), UserDataService.getDecodedAvatar()));
            userParamsNicknameTextBox.setText(UserDataService.getUserNickname());
            userParamsPasswordTextBox.setText(UserDataService.getUserPassword());
        }

        else userParamsAcceptText.setText(getString(R.string.accept_create_user_button));
    }

    //Method that starts the ConnectionService in order to create a new user in the API:
    public void createUser() {

        Intent createIntent = new Intent(getApplicationContext(), ConnectionService.class);

        createIntent.putExtra(ConnectionService.REQUESTED_OPERATION_FIELD, ConnectionService.CREATE);
        createIntent.putExtra(UserDataService.USER_JSON_FIELD, getUserAttributesJson(false).toString());

        startService(createIntent);
    }

    //Method that starts the ConnectionService in order to edit an existing user in the API:
    public void editUser() {
        Intent createIntent = new Intent(getApplicationContext(), ConnectionService.class);

        JSONObject userAttributesJson = getUserAttributesJson(true);

        //In order to determine whether or not the user should be updated in the API, changes on his/her current parameters are sought:
        if (checkNewAttributes(userAttributesJson)) {

            createIntent.putExtra(ConnectionService.REQUESTED_OPERATION_FIELD, ConnectionService.EDIT);
            createIntent.putExtra(UserDataService.USER_JSON_FIELD, userAttributesJson.toString());

            startService(createIntent);
        }

        //If no changes were made the user is notified:
        else ToastUtils.makeCustomToast(getApplicationContext(), getString(R.string.no_data_edited));
    }

    //Method that starts the dialog activity whenever an operation for which the user must give explicit consent is going to be performed (for example when the user selects the delete button):
    private void displayConfirmationDialog(String dialogText, int resultCode) {

        Intent dialogIntent = new Intent(getApplicationContext(), DialogActivity.class);

        dialogIntent.putExtra(DIALOG_TEXT_FIELD, dialogText);

        startActivityForResult(dialogIntent, resultCode);
    }

    //Method that encapsulates the functionality to create a json containing the current user's information, avoiding code redundancies and multiple uses of try-catch blocks:
    private JSONObject getUserAttributesJson(boolean includeId) {

        JSONObject userJson = new JSONObject();

        try {
            if (includeId) userJson.put(UserDataService.USER_ID_FIELD, UserDataService.getUserId());
            userJson.put(UserDataService.USER_NAME_FIELD, userParamsNameTextBox.getText().toString());
            userJson.put(UserDataService.USER_SURNAME_FIELD, userParamsSurnameTextBox.getText().toString());
            userJson.put(UserDataService.USER_AVATAR_FIELD, newUserEncodedAvatar);
            userJson.put(UserDataService.USER_NICKNAME_FIELD, userParamsNicknameTextBox.getText().toString());
            userJson.put(UserDataService.USER_PASSWORD_FIELD, userParamsPasswordTextBox.getText().toString());

        } catch (JSONException jsonException) {
            Log.e(TAG, "getUserAttributesJson. JSONException: " + jsonException.getMessage());
        }

        return userJson;
    }

    //Method that checks if, after displaying the user parameters layout, any attribute was modified, case in which a PUT operation will be performed:
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

    //Method that starts the ConnectionService so a delete operation can be performed:
    public void deleteUser() {

        Intent deleteIntent = new Intent(getApplicationContext(), ConnectionService.class);

        deleteIntent.putExtra(ConnectionService.REQUESTED_OPERATION_FIELD, ConnectionService.DELETE);

        deleteIntent.putExtra(UserDataService.USER_JSON_FIELD, getUserAttributesJson(true).toString());

        startService(deleteIntent);
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
                    loggedUserAvatar.setImageBitmap(userAvatar);

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

                    avatar.setImageBitmap(defaultAvatar.getBitmap());
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