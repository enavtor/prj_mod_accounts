package com.droidmare.accounts.views.activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droidmare.accounts.R;
import com.droidmare.accounts.models.Multimedia;
import com.droidmare.accounts.utils.FileUtils;
import com.droidmare.accounts.views.fragments.FilesFragment;
import com.droidmare.common.utils.ImageUtils;

import java.util.ArrayList;

//Files activity declaration (browser activity for selecting an avatar)
//@author Eduardo on 12/06/2019.

public class FilesActivity extends Activity {

    public static final String ROOT_PATH = "storage/emulated/0";

    private static final String HISTORY_SEPARATOR = "<===>";

    private static final int HISTORY_NAME_INDEX = 0;
    private static final int HISTORY_PATH_INDEX = 1;
    private static final int HISTORY_FOCUS_INDEX = 2;
    private static final int HISTORY_SCROLL_INDEX = 3;

    private Handler viewsHandler;

    private ArrayList<String> pathsHistory;
    private ArrayList<Multimedia> filesList;

    private int elementToFocus;
    private int positionToScrollTo;

    private RelativeLayout virtualBackButton;

    private FilesFragment filesFragment;

    private TextView mainTitle;
    private String mainTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewsHandler = new Handler(Looper.getMainLooper());

        pathsHistory = new ArrayList<>();

        elementToFocus = 0;
        positionToScrollTo = 0;

        setContentView(R.layout.activity_files);

        initViews();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        //In order to avoid malfunctions while performing the circular navigation, the focusability of the footer buttons must be checked:
        checkButtonsFocusability(event);

        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

                int historyElements = pathsHistory.size();

                if (historyElements <= 1) finish();

                else {
                    pathsHistory.remove(--historyElements);

                    String[] historyElementAttributes = pathsHistory.get(historyElements -1).split(HISTORY_SEPARATOR);

                    elementToFocus = Integer.valueOf(historyElementAttributes[HISTORY_FOCUS_INDEX]);
                    positionToScrollTo = Integer.valueOf(historyElementAttributes[HISTORY_SCROLL_INDEX]);

                    setDisplayedFragment(false, historyElementAttributes[HISTORY_PATH_INDEX]);

                    mainTitleText = historyElementAttributes[HISTORY_NAME_INDEX];
                }

                return true;
            }

            else if (filesFragment.dispatchKeyEvent(event))
                return true;
        }

        return super.dispatchKeyEvent(event);
    }

    //Method tat initializes this activity's views:
    private void initViews() {

        mainTitle = findViewById(R.id.titleMain);

        mainTitleText = ROOT_PATH;

        pathsHistory.add(ROOT_PATH + HISTORY_SEPARATOR + ROOT_PATH);

        virtualBackButton = findViewById(R.id.virtual_back_layout);

        setDisplayedFragment(true, ROOT_PATH);

        //Virtual back Button:
        virtualBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
            }
        });
    }

    //Method that is used to update the files fragment whenever a folder is opened or a back navigation is performed:
    private void setDisplayedFragment(boolean isFirstExecution, String path) {

        filesList = FileUtils.getDirectoryFiles(path);

        FragmentTransaction fragTrans = getFragmentManager().beginTransaction();

        filesFragment = new FilesFragment();

        if (isFirstExecution) fragTrans.add(R.id.fragment_container, filesFragment, FilesFragment.NAME);
        else fragTrans.replace(R.id.fragment_container, filesFragment, FilesFragment.NAME);

        fragTrans.commit();
    }

    //Method that updates the description view when a file gets the focus:
    public void changeFileDescriptionText(final String text) {
        viewsHandler.post(new Runnable() {
            @Override
            public void run() {
                filesFragment.setDescriptionText(text);
            }
        });
    }

    //Method used to navigate to a new folder:
    public void openFolder(String name, String path, int folderPosition) {

        elementToFocus = positionToScrollTo = 0;

        int currentIndex = pathsHistory.size() - 1;

        String currentFolder = pathsHistory.get(currentIndex);

        String[] historyElementAttributes = currentFolder.split(HISTORY_SEPARATOR);

        String folderName = historyElementAttributes[HISTORY_NAME_INDEX];
        String folderPath = historyElementAttributes[HISTORY_PATH_INDEX];

        //Whenever a folder is opened, its attributes are added to the history, so that a backwards navigation can be correctly performed:
        currentFolder = folderName + HISTORY_SEPARATOR + folderPath + HISTORY_SEPARATOR + folderPosition + HISTORY_SEPARATOR + getScrolledPosition();

        pathsHistory.set(currentIndex, currentFolder);

        pathsHistory.add(name + HISTORY_SEPARATOR + path);

        setDisplayedFragment(false, path);

        mainTitleText = name;
    }

    //Method that returns the position to which the list have been scrolled before opening a new folder, so it can be restored when the user navigates to the previous folder:
    private int getScrolledPosition() {
        return filesFragment.getCurrentScrolledPosition();
    }

    //In order to avoid misbehaviours while performing the circular navigation, the footer buttons focusability must be set to false during the aforementioned navigation:
    private void checkButtonsFocusability(KeyEvent event) {
        View focusedView = getCurrentFocus();

        if (focusedView != null && filesFragment.getFileGridView() != null)
            if (((View)focusedView.getParent()).getId() == filesFragment.getFileGridView().getId()) {

                if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT))
                    changeButtonsFocusability(false);

                else changeButtonsFocusability(true);
        }
    }

    //Method that changes the footer button focusability as long as the new value is different from the current one:
    private void changeButtonsFocusability(boolean focusability) {
        if (virtualBackButton.isFocusable() != focusability) {
            virtualBackButton.setFocusable(focusability);
        }
    }

    //Method used to update the title text with the current folder's name:
    public void setViewsAfterFragmentLoaded() {
        mainTitle.setText(mainTitleText);
    }

    //Method that returns the current directory's file list:
    public ArrayList<Multimedia> getFilesList() { return filesList; }

    //Method that returns the element that must receive the focus when performing a backwards navigation:
    public int getElementToFocus() { return elementToFocus; }

    //Method that returns the position to which the list must be scrolled after performing a backwards navigation:
    public int getPositionToScrollTo() { return positionToScrollTo; }

    //Method that encodes the selected avatar image and sends it back to the MainActivity:
    public void sendPickedAvatar(Multimedia avatar) {

        Bitmap avatarBitmap = BitmapFactory.decodeFile(avatar.getPath());
        String encodedAvatar = ImageUtils.encodeBitmapImage(avatarBitmap);

        Intent resultIntent = new Intent();
        resultIntent.putExtra(MainActivity.ENCODED_AVATAR_FIELD, encodedAvatar);

        setResult(RESULT_OK, resultIntent);

        finish();
    }
}
