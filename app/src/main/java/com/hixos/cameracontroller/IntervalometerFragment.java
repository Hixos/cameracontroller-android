package com.hixos.cameracontroller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.hixos.cameracontroller.commands.Command;
import com.hixos.cameracontroller.communication.MessageService;

public class IntervalometerFragment extends Fragment {
    private static final String LOGTAG = "SequencerFragment";

    private static final String PREF_KEY_DOWNLOAD_AFTER_CAPTURE = "CameraController.DownloadAfterExp";
    private static final String PREF_KEY_NUM_EXPOSURES = "IntervalometerFragment.NumExposures";
    private static final String PREF_KEY_INTERVAL = "IntervalometerFragment.Interval";
    private static final String PREF_KEY_EXP_TIME = "IntervalometerFragment.ExposureTime";

    private MessageService mService;
    private boolean mBound = false;

    private EditText mEditTextNumExp;
    private EditText mEditTextExpTime;
    private EditText mEditTextInterval;
    private CheckBox mCheckBoxDownload;

    private Button mButtonStart;
    private Button mButtonStop;
    private Button mButtonConfigure;
    private Button mButtonTest;

    SharedPreferences.Editor mPrefEditor;

    private Float mExposureTime = 0.0f;
    private Integer mNumExposures = 1;
    private Integer mInterval = 10;
    private Boolean mDownload = false;

    private MyFocusChangeListener mFocusChangeListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_intervalometer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mButtonStart = view.findViewById(R.id.button_start);
        mButtonStop = view.findViewById(R.id.button_stop);

        mButtonConfigure = view.findViewById(R.id.button_configure);
        mButtonTest = view.findViewById(R.id.button_test);

        mEditTextExpTime = view.findViewById(R.id.edittext_exp_time);
        mEditTextNumExp = view.findViewById(R.id.edittext_num_exp);
        mEditTextInterval = view.findViewById(R.id.edittext_interval);
        mCheckBoxDownload = view.findViewById(R.id.checkbox_download);


    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences pref = getContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mPrefEditor = pref.edit();
        mNumExposures = pref.getInt(PREF_KEY_NUM_EXPOSURES, 1);
        mExposureTime = pref.getFloat(PREF_KEY_EXP_TIME, 0.0f);
        mInterval = pref.getInt(PREF_KEY_INTERVAL, 10);
        mDownload = pref.getBoolean(PREF_KEY_DOWNLOAD_AFTER_CAPTURE, false);

        mEditTextExpTime.setOnFocusChangeListener(mFocusChangeListener);
        mEditTextExpTime.setText(mExposureTime.toString());

        mEditTextInterval.setOnFocusChangeListener(mFocusChangeListener);
        mEditTextInterval.setText(mInterval.toString());

        mEditTextNumExp.setOnFocusChangeListener(mFocusChangeListener);
        mEditTextNumExp.setText(mNumExposures.toString());

        mCheckBoxDownload.setChecked(mDownload);

        mCheckBoxDownload.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(mBound)
                {
                    mPrefEditor.putBoolean(PREF_KEY_DOWNLOAD_AFTER_CAPTURE, b);
                    mPrefEditor.apply();

                    mService.send(Command.downloadAfterExposureCommand(b));
                }
            }
        });
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.send(Command.emptyCommand(Command.CMD_ID_FUNCTIONSTART));
            }
        });
        mButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.send(Command.emptyCommand(Command.CMD_ID_FUNCTIONSTOP));
            }
        });

        mButtonConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!saveValues())
                {
                    Toast.makeText(getContext(), "Invalid data!", Toast.LENGTH_LONG).show();
                    return;
                }

                mPrefEditor.putFloat(PREF_KEY_EXP_TIME, mExposureTime);
                mPrefEditor.putInt(PREF_KEY_NUM_EXPOSURES, mNumExposures);
                mPrefEditor.putInt(PREF_KEY_INTERVAL, mInterval);
                mPrefEditor.apply();

                mService.send(Command.setupIntervalometerCommand(mNumExposures, mInterval, mExposureTime, mDownload));
            }
        });

        mButtonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.send(Command.emptyCommand(Command.CMD_ID_FUNCTION_TEST_CAPTURE));
            }
        });

        Intent i = new Intent(getContext(), MessageService.class);
        getActivity().bindService(i, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unbindService(mConnection);
    }

    private boolean saveValues()
    {
        try{
            mNumExposures = Integer.valueOf(mEditTextNumExp.getText().toString());
            mExposureTime = Float.valueOf(mEditTextExpTime.getText().toString());
            mInterval = Integer.valueOf(mEditTextInterval.getText().toString());
        }catch (NumberFormatException nfe)
        {
            Log.w(LOGTAG, nfe.getMessage());
            return false;
        }
        return true;
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MessageService.MessageBinder binder = (MessageService.MessageBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private class MyFocusChangeListener implements View.OnFocusChangeListener {

        public void onFocusChange(View v, boolean hasFocus){

            if(!hasFocus) {

                InputMethodManager imm =  (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }
}
