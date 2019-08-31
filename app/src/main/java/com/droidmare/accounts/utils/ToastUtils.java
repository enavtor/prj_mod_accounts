package com.droidmare.accounts.utils;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.droidmare.accounts.R;

//Utils for creating custom Toasts
//@author Eduardo on 09/07/2018.
public class ToastUtils {

    private static Toast toast = null;

    private static CountDownTimer toastCountDown;

    private static final int DEFAULT_TOAST_SIZE = 30;
    private static final int DEFAULT_TOAST_DURATION = 5;

    //Method that creates and shows a Toast with a specific text size and duration (in seconds):
    public static void makeCustomToast(final Context context, final String text) {

        //If a toast is being displayed when a new toast is going to be shown, the first toast must be canceled, as well as its countdown:
        cancelCurrentToast();

        //The toast must always be created on the UI thread to avoid application malfunctions:
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                toast = Toast.makeText(context, text, Toast.LENGTH_LONG);

                toast.setGravity(Gravity.CENTER, 0, 0);

                LinearLayout toastLayout = (LinearLayout) toast.getView();

                toastLayout.setBackground(null);

                TextView toastTextView = (TextView) toastLayout.getChildAt(0);

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) toastTextView.getLayoutParams();

                params.width = ImageUtils.transformDipToPix(context, 600);

                int padding = ImageUtils.transformDipToPix(context, 20);

                toastTextView.setPadding(padding, padding, padding, padding);

                toastTextView.setBackground(context.getDrawable(R.drawable.toast_background));

                toastTextView.setTextSize(DEFAULT_TOAST_SIZE);

                //A countdown to display the toast with the specified duration is set:
                toastCountDown = new CountDownTimer(DEFAULT_TOAST_DURATION * 1000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        toast.show();
                    }

                    public void onFinish() {

                        if (toast != null) {
                            toast.cancel();
                            toast = null;
                        }

                        toastCountDown = null;
                    }
                };

                //Now the toast is shown and the countdown is started:
                toast.show();
                toastCountDown.start();
            }
        });
    }

    public static boolean cancelCurrentToast() {

        boolean toastCanceled;

        if (toastCountDown != null) {
            toastCountDown.cancel();
            toastCountDown = null;
        }

        if (toastCanceled = toast != null) {
            toast.cancel();
            toast = null;
        }

        return toastCanceled;
    }
}