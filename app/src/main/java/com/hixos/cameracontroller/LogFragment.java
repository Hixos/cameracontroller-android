package com.hixos.cameracontroller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hixos.cameracontroller.communication.MessageHandler;
import com.hixos.cameracontroller.communication.MessageService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class LogFragment extends Fragment implements MessageHandler.OnMessageReceivedListener{
    private MessageService mService;
    private boolean mBound = false;

    private TextView mTextViewLog;
    private ScrollView mScrollView;

    private String sLogFilename = "logfile.txt";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent(getContext(), MessageService.class);
        getActivity().bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextViewLog = view.findViewById(R.id.textview_log);
        mScrollView = view.findViewById(R.id.scrollview_log);

    }

    @Override
    public void onStart() {
        super.onStart();

        String line = "";

        try{
            File logfile = new File(getActivity().getFilesDir(), sLogFilename);
            FileInputStream fis = new FileInputStream(logfile);

            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line + "\n");
            }

            bufferedReader.close();
            line = sb.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.w("Log", "Read:" + line);
        mTextViewLog.setText(line);

        mTextViewLog.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
                return true;
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_log, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_log: {
                mTextViewLog.setText("");
                saveLogToFile();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        saveLogToFile();


    }

    private void saveLogToFile()
    {

        FileOutputStream outputStream;

        try {
            File logfile = new File(getActivity().getFilesDir(), sLogFilename);
            outputStream = new FileOutputStream(logfile);
            outputStream.write(mTextViewLog.getText().toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mBound)
        {
            mService.unRegisterOnMessageReceivedListener(this);
        }
        getActivity().unbindService(mConnection);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MessageService.MessageBinder binder = (MessageService.MessageBinder) service;
            mService = binder.getService();
            mService.registerOnMessageReceivedListener(LogFragment.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onLogReceived(String log) {
        mTextViewLog.append(log);
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}
