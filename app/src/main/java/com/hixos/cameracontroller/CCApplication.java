package com.hixos.cameracontroller;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.hixos.cameracontroller.communication.MessageHandler;
import com.hixos.cameracontroller.communication.MessageService;

public class CCApplication extends Application {
    private static final String LOGTAG = "CController";
    private MessageService mService;
    private boolean mBound = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(LOGTAG, "OnCreate");

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.w(LOGTAG, "OnTerminate1");
        unbindService(mConnection);
        Log.w(LOGTAG, "OnTerminate2");
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

            mService.registerOnMessageReceivedListener(new MessageHandler.OnMessageReceivedListener() {
                @Override
                public void onLogReceived(String log) {
                    Log.i(LOGTAG, log);
                }
            });

            mService.connect();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOGTAG, "Disconnected");
            mBound = false;
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.w(LOGTAG, "DEAD");
        }
    };
}
