package com.hixos.cameracontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class SequentiometerActivity extends AppCompatActivity {
    private static final String LOGTAG = "SequentActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sequentiometer);
        Log.w(LOGTAG, "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.w(LOGTAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w(LOGTAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w(LOGTAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w(LOGTAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(LOGTAG, "onResume");
    }
}
