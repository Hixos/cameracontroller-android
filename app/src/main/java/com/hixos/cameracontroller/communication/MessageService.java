package com.hixos.cameracontroller.communication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


public class MessageService extends Service {
    private static final String LOGTAG = "MessageService";

    private final TCPClient mTCPClient = new TCPClient();
    private final MessageHandler mMessageHandler = new MessageHandler();
    private final MessageDecoder mDecoder;

    private MessageBinder mBinder = new MessageBinder();

    public MessageService()
    {
        mDecoder = new MessageDecoder(mMessageHandler);
        mTCPClient.registerConnectionListener(mDecoder);
    }

    @Override
    public void onCreate() {
        Log.i(LOGTAG, "Service onCreate");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i(LOGTAG, "Service onDestroy");
        mTCPClient.cancel(false);
        mTCPClient.disconnect();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOGTAG, "Service onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(LOGTAG, "Service onUnBind");
        return false;
    }

    public boolean isConnected()
    {
        synchronized (mTCPClient) {
            return mTCPClient.isConnected() || mTCPClient.isConnecting();
        }
    }

    public boolean connect()
    {
        return mTCPClient.connect(this);
    }

    public void disconnect()
    {
         mTCPClient.disconnect();
    }

    public void registerOnMessageReceivedListener(MessageHandler.OnMessageReceivedListener listener)
    {
        mMessageHandler.registerOnMessageReceivedListener(listener);
    }

    public void unRegisterOnMessageReceivedListener(MessageHandler.OnMessageReceivedListener listener)
    {
        mMessageHandler.removeOnMessageReceivedListener(listener);
    }

    public void registerOnConnectionListener(ConnectionListener listener)
    {
        mTCPClient.registerConnectionListener(listener);
    }

    public void unRegisterOnConnectionListener(ConnectionListener listener)
    {
        mTCPClient.unregisterConnectionListener(listener);
    }

    public void send(Message m)
    {
        if(isConnected())
        {
            mTCPClient.send(m.encode());
        }
    }

    public class MessageBinder extends Binder
    {
        public MessageService getService()
        {
            return MessageService.this;
        }
    }
}
