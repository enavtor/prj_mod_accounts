package com.droidmare.accounts.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droidmare.accounts.R;
import com.droidmare.common.utils.ToastUtils;

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

        if (event.getAction() == KeyEvent.ACTION_DOWN && ToastUtils.cancelCurrentToast()) return true;

        else return super.dispatchKeyEvent(event);
    }

    //Initialization of the activity views and buttons:
    private void initializeViews (){

        TextView title = findViewById(R.id.dialog_title);

        LinearLayout affirmative = findViewById(R.id.dialog_affirmative_button);
        LinearLayout negative = findViewById(R.id.dialog_negative_button);

        title.setText(getIntent().getStringExtra(MainActivity.DIALOG_TEXT_FIELD));

        negative.requestFocus();

        //Behaviour of the affirmative option inside the dialog:
        affirmative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
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
}
