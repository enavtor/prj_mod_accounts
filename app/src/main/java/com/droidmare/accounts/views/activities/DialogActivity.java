package com.droidmare.accounts.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droidmare.accounts.R;
import com.droidmare.accounts.utils.ToastUtils;

//Activity for displaying a dialog before performing an operation
//@author Eduardo on 31/08/2019.
public class DialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dialog);

        initializeViews();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (ToastUtils.cancelCurrentToast()) return true;

        else return super.dispatchKeyEvent(event);
    }


    //Initialization of the activity views and buttons:
    private void initializeViews (){

        TextView title = findViewById(R.id.dialog_title);

        LinearLayout affirmative = findViewById(R.id.dialog_affirmative_button);
        LinearLayout negative = findViewById(R.id.dialog_negative_button);

        /*if (getIntent().hasExtra("deleteSingleEvent")) {
            title.setText(getResources().getString(R.string.delete_single_dialog_title));
        }

        else if (getIntent().hasExtra("deletePrevAlarm")) {
            title.setText(getResources().getString(R.string.delete_alarm_dialog_title));
        }*/

        negative.requestFocus();

        //Behaviour of the affirmative option inside the dialog:
        affirmative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performOperation();
            }
        });

        //Behaviour of the negative option inside the dialog:
        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void performOperation() {
        setResult(RESULT_OK);
        finish();
    }
}