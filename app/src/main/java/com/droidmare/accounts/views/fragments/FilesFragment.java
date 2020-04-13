package com.droidmare.accounts.views.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.droidmare.accounts.R;
import com.droidmare.accounts.models.Multimedia;
import com.droidmare.accounts.utils.FileUtils;
import com.droidmare.accounts.views.activities.FilesActivity;
import com.droidmare.accounts.views.adapters.FilesAdapter;

import java.util.ArrayList;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

//Files fragment declaration
//@author Eduardo on 12/06/2019.

public class FilesFragment extends Fragment {

    public static final String NAME = "files_fragment";

    private final static int COLUMN_NUMBER = 5;

    private TextView descriptionText;

    private RecyclerView fileGrid;
    private FilesAdapter filesAdapter;

    private int currentScrolledY;
    private int positionToFocus;

    private FilesActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_files, container, false);

        activity = (FilesActivity) getActivity();

        descriptionText = view.findViewById(R.id.description_text);

        fileGrid = view.findViewById(R.id.file_list_view);

        fileGrid.getRecycledViewPool().setMaxRecycledViews(FileUtils.FOLDER_TYPE, 0);
        fileGrid.getRecycledViewPool().setMaxRecycledViews(FileUtils.IMAGE_TYPE, 0);

        fileGrid.setLayoutManager(new GridLayoutManager(activity, COLUMN_NUMBER));

        ArrayList<Multimedia> multimediaList = activity.getFilesList();

        filesAdapter = new FilesAdapter(multimediaList, activity);

        fileGrid.setAdapter(filesAdapter);

        if (multimediaList.size() == 0) fileGrid.setFocusable(false);
        else fileGrid.requestFocus();

        fileGrid.setOverScrollMode(View.OVER_SCROLL_NEVER);

        positionToFocus = activity.getElementToFocus();

        //A scroll listener is added to the recycler view so if the file that must get the focus is not visible when circularly navigating,
        //it can be focused once the scroll operation has finished (moment in which the file will be visible and therefore focusable):
        fileGrid.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView view, int state) {
                if (state == SCROLL_STATE_IDLE && positionToFocus != -1) {
                    RecyclerView.ViewHolder item = fileGrid.findViewHolderForAdapterPosition(positionToFocus);
                    if (item != null) item.itemView.requestFocus();
                    positionToFocus = -1;
                }
            }

            @Override
            public void onScrolled (@NonNull RecyclerView recyclerView, int dx, int dy) {
                currentScrolledY += dy;
            }
        });

        currentScrolledY = 0;

        //After the file grid has been updated and rendered, it must be scrolled to the position indicated by positionToScroll:
        fileGrid.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            private final int positionToScrollTo = activity.getPositionToScrollTo();

            @Override
            public void onGlobalLayout() {

                //First, the grid must be scrolled to the correct position:
                if (currentScrolledY == 0 && positionToFocus > 9)
                    fileGrid.scrollBy(0, positionToScrollTo);

                //Once the list is on the necessary position, the element that must get the focus can be focused:
                else if (currentScrolledY == positionToScrollTo || positionToFocus <= 9) {

                    if (positionToFocus > 4 && positionToFocus <= 9)
                        fileGrid.scrollBy(0, positionToScrollTo);

                    RecyclerView.ViewHolder item = fileGrid.findViewHolderForAdapterPosition(positionToFocus);
                    if (item != null) item.itemView.requestFocus();
                    positionToFocus = -1;

                    activity.setViewsAfterFragmentLoaded();

                    //This listener can be removed since, at this point, the grid status is the correct one:
                    fileGrid.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        return view;
    }

    //Navigation along the recycler views is set as follows to make it circular:
    public boolean dispatchKeyEvent(KeyEvent event) {

        View focusedView = activity.getCurrentFocus();

        //First of all it is necessary to check that a view has the focus:
        if(focusedView != null && event.getAction()== KeyEvent.ACTION_DOWN) {

            //If a view is focused, the parent is got so it can be checked if it is the the recycler view:
            if (((View)focusedView.getParent()).getId() == fileGrid.getId()) {

                //Depending on the pressed key and the focused element's position, the next focused position will be different:
                int focusedPosition = filesAdapter.getFocusedPosition();
                int numberOfItems = filesAdapter.getItemCount();

                //When the d-pad left key is pressed, the next focused element must be the right edge previous element or the last element if the current focused file is the first one:
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT && focusedPosition % COLUMN_NUMBER == 0) {

                    //Behaviour of the first element of the grid:
                    if (focusedPosition == 0) {
                        //The next focused element when navigating to the first element's left side is the grid's last element:
                        fileGrid.smoothScrollToPosition(numberOfItems - 1);
                        positionToFocus = numberOfItems - 1;
                    }

                    //Behaviour of the left edge elements:
                    else if (focusedPosition % COLUMN_NUMBER == 0) {
                        //The next focused element when navigating to the left edge elements' left side is the right edge previous element:
                        positionToFocus = focusedPosition - 1;

                        checkElementVisibility();
                    }

                    return true;
                }

                //When the d-pad right key is pressed, the next focused element must be the left edge next element or the first element if the current focused file is the last one:
                else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && (focusedPosition % COLUMN_NUMBER == COLUMN_NUMBER - 1 || focusedPosition == numberOfItems -1)) {

                    //Behaviour of the last element of the grid:
                    if (focusedPosition == numberOfItems - 1) {
                        //The next focused element when navigating to the last element's right side is the grid's first element:
                        fileGrid.smoothScrollToPosition(0);
                        positionToFocus = 0;
                    }

                    //Behaviour of the right edge elements:
                    else if (focusedPosition % COLUMN_NUMBER == COLUMN_NUMBER - 1) {
                        //The next focused element when navigating to the right edge elements' right side is the left edge next element:
                        positionToFocus = focusedPosition + 1;

                        checkElementVisibility();
                    }

                    return true;
                }

                else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && fileGrid.canScrollVertically(-1)) {
                    positionToFocus = focusedPosition - 5;
                    fileGrid.smoothScrollToPosition(positionToFocus);
                    return true;
                }

                else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && fileGrid.canScrollVertically(+1)) {
                    positionToFocus = focusedPosition + 5;
                    if (positionToFocus > numberOfItems - 1) positionToFocus = numberOfItems -1;
                    fileGrid.smoothScrollToPosition(positionToFocus);
                    return true;
                }
            }
        }

        return false;
    }

    //Method that checks the visibility of the element that must get the focus after performing a backwards navigation:
    private void checkElementVisibility() {
        //If the element is visible (element != null) it will be focused directly, otherwise a scroll operation will be performed:
        RecyclerView.ViewHolder element = fileGrid.findViewHolderForAdapterPosition(positionToFocus);

        if (element != null) {
            element.itemView.requestFocus();
            positionToFocus = -1;
        }

        else fileGrid.smoothScrollToPosition(positionToFocus);
    }

    //Method that returns the whole file grid view, so the FilesActivity can handle the circular navigation:
    public RecyclerView getFileGridView() { return fileGrid; }

    //Method that returns the position to which the list have been scrolled so he FilesActivity can store it before opening a new folder:
    public int getCurrentScrolledPosition() { return currentScrolledY; }

    //Method that updates the description view text:
    public void setDescriptionText(String text) { descriptionText.setText(text); }
}
