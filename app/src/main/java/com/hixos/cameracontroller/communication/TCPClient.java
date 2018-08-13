package com.hixos.cameracontroller.communication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.hixos.cameracontroller.ByteArray;
import com.hixos.cameracontroller.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TCPClient extends AsyncTask<Void, ByteArray, Boolean> implements DataConnectionInterface {
    private static final String LOGTAG = "TCPClient";

    public static final String PREF_KEY_IP_ADDR = "TCPClient.IPADDR";
    public static final String PREF_KEY_PORT = "TCPClient.PORT";

    private static final int WAIT_TIME = 1000;
    private static final int MAX_CONNECTION_ATTEMPTS = 1;

    private ArrayList<ConnectionListener> mListeners;

    private boolean mRunning = false;
    private volatile boolean mConnecting = false;
    private volatile boolean mConnected = false;
    private volatile boolean mConncetionFailedFlag = false;

    private volatile AtomicInteger mConnectionAttempts = new AtomicInteger(1);

    private int mPort;

    private InetAddress mServerAddr;
    private Socket mSocket;

    private BufferedInputStream mRecvStream;
    private OutputStream mSendStream;

    private Thread mThreadSender;
    private SenderRunnable mSenderRunnable;

    public TCPClient()
    {
        mListeners = new ArrayList<>();
        mSenderRunnable = new SenderRunnable();
    }

    public boolean connect(Context context)
    {
        if(!mConnected && !mConnecting)
        {
            SharedPreferences pref = context.getSharedPreferences(
                    context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String serverAddr = pref.getString(PREF_KEY_IP_ADDR, "");
            mPort = pref.getInt(PREF_KEY_PORT, -1);

            if(serverAddr.equals("") || mPort < 0)
            {
                Log.w(LOGTAG, "Serveraddr is empty");
                return false;
            }

            try {
                mServerAddr = InetAddress.getByName(serverAddr);
            }catch (Exception e)
            {
                Log.e(LOGTAG, "Error getting server inetaddr. " + e.getMessage());
                return false;
            }
        }
        mConnecting = true;

        if(!mRunning){
            mRunning = true;
            execute();
            mThreadSender = new Thread(mSenderRunnable);
            mThreadSender.start();

            mSenderRunnable.onConnect();
        }else {
            synchronized (this) {
                notify();
            }

            mSenderRunnable.onConnect();
        }
        return true;
    }

    private boolean doConnect()
    {
        if(!mConnected && mConnecting) {
            try {
                mSocket = new Socket(mServerAddr, mPort);
            }catch (Exception e)
            {
                Log.e(LOGTAG, "Error connecting to server. " + e.getMessage());
                return false;
            }

            try
            {
                mSendStream = mSocket.getOutputStream();
                mRecvStream = new BufferedInputStream(mSocket.getInputStream());
                if(mSendStream == null)
                {
                    Log.e(LOGTAG, "Sendstream null");
                }

                if(mRecvStream == null)
                {
                    Log.e(LOGTAG, "REcvstream null");
                }
            }catch (IOException ioe)
            {
                Log.e(LOGTAG, "Error obtaining streams. " + ioe.getMessage());
                try
                {
                    mSocket.close();
                }catch (IOException ioe2)
                {
                    Log.e(LOGTAG, "Error closing socket. " + ioe2.getMessage());
                }
                return false;
            }
            Log.i(LOGTAG, "TCPClient successfully connected.");
            mConnected = true;
            mConnecting = false;
        }
        return true;
    }

    public boolean isConnecting()
    {
        return mConnecting;
    }

    public boolean isConnected()
    {
        return mConnected;
    }

    private void close()
    {
        if(mConnected) {
            Log.w(LOGTAG, "Closing");
            mConnected = false;
            try {
                mRecvStream.close();
            } catch (IOException ioe) {
                Log.w(LOGTAG, ioe.getMessage());
            }
            try {
                mSendStream.close();
            } catch (IOException ioe) {
                Log.w(LOGTAG, ioe.getMessage());
            }
            try {
                mSocket.close();
            } catch (IOException ioe) {
                Log.w(LOGTAG, ioe.getMessage());
            }
        }
    }

    public void disconnect()
    {
        disconnect(false);
    }

    public void disconnect(boolean shouldCancel)
    {
        if(shouldCancel) {
            cancel(false);
        }

        Log.i(LOGTAG, "Disconnecting & notifying...");
        close();

        mConnected = false;
        mConnecting = false;
        mConncetionFailedFlag = false;

        synchronized (this) {
            notify();
        }

        mSenderRunnable.onDisconnect();
    }

    /**
     * Register a listener for incoming data.
     *
     * @param listener
     */
    public synchronized void registerConnectionListener(ConnectionListener listener)
    {
        if(!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public synchronized void unregisterConnectionListener(ConnectionListener listener)
    {
        mListeners.remove(listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Boolean doInBackground(Void... voids) {
        while (!isCancelled()) {

            boolean alreadyConnected = mConnected;
            //Try connecting
            if(!mConnecting && !mConnected)
            {
                synchronized (this)
                {
                    try {
                        wait();
                    }catch (InterruptedException e){
                        Log.e(LOGTAG, e.getMessage());
                    }
                }
            }else if(!mConnected && !doConnect())
            {
                Log.i(LOGTAG, "Connection attempt " + mConnectionAttempts.get() + " failed");

                if(mConnectionAttempts.incrementAndGet() >= MAX_CONNECTION_ATTEMPTS)
                {
                    mConnecting = false;
                    mConncetionFailedFlag = true;
                    publishProgress();
                }else{
                    publishProgress();


                    //Wait a bit before looping
                    synchronized (this) {
                        try {
                            wait(WAIT_TIME);
                        } catch (InterruptedException ie) {
                            Log.e(LOGTAG, "Error sleeping: " + ie.getMessage());
                        }
                    }
                }
            }else if(mConnected){ //We are connected

                //Notify if we have just connected
                if(!alreadyConnected) {
                    Log.i(LOGTAG, "Connected");
                    publishProgress();
                }

                //Receive and publish data
                try {
                    byte[] data = new byte[1024];
                    int n = mRecvStream.read(data);
                    //Server terminated the connection
                    if (n == -1) {
                        Log.w(LOGTAG, "Connection ended.");

                        disconnect();
                        publishProgress();
                    } else {
                        byte[] truncated = Arrays.copyOf(data, n);
                        publishProgress(new ByteArray(truncated));
                    }
                } catch (IOException ioe) {
                    Log.w(LOGTAG, "Error reading from socket. (canceled: " + isCancelled() + ") " + ioe.getMessage());
                    mConnected = false;
                    mConnecting = false;
                    publishProgress();
                }
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean b) {
        super.onPostExecute(b);
        disconnect();
        if(b)
        {
            Log.i(LOGTAG, "Task terminated by remote server.");
        }else {
            Log.i(LOGTAG, "Task terminated locally.");
        }
    }

    @Override
    protected final void onProgressUpdate(ByteArray... values) {
        super.onProgressUpdate(values);

        synchronized (this) {
            if (mListeners.size() == 0) {
                Log.e(LOGTAG, "No listeners to receive data.");
            }
            if(values.length > 0) {
                Log.d(LOGTAG, "onDataReceived (" + values[0].array.length + ")");
                for (ConnectionListener l : mListeners) {
                    l.onDataReceived(values[0].array);
                }
            }else{
                if(mConncetionFailedFlag){
                    Log.d(LOGTAG, "onConnectionFailed");
                    for (ConnectionListener l : mListeners) {
                        l.onConnectionFailed();
                    }
                    mConncetionFailedFlag = false;
                }else if(mConnected){
                    Log.d(LOGTAG, "onConnect");
                    for (ConnectionListener l : mListeners) {
                        l.onConnect();
                    }
                }else if(mConnecting){
                    Log.d(LOGTAG, "onConnectionAttempt");
                    for (ConnectionListener l : mListeners) {
                        l.onConnectionAttempt(mConnectionAttempts.get());
                    }
                }else{
                    Log.d(LOGTAG, "onDisconnect");
                    for (ConnectionListener l : mListeners) {
                        l.onDisconnect();
                    }
                }
            }
        }
    }

    @Override
    protected void onCancelled(Boolean b) {
        super.onCancelled(b);
        Log.i(LOGTAG, "onCancelled");
    }

    private class SenderRunnable implements Runnable
    {
        private volatile int mSendBufPtr = 0;
        private byte[] mSendBuffer = new byte[512*1024]; //512Kib should be more than enough for every
                                                         // purpose. No need for ring buffer
        private volatile boolean mConnected = false;

        private void onConnect()
        {
            synchronized (this) {
                mConnected = true;
                notify();
            }
        }

        private void onDisconnect()
        {
            synchronized (this){
                mConnected = false;
                notify();
            }
        }

        private synchronized void send(byte[] data) {
            if(mSendBuffer.length - mSendBufPtr > data.length) {
                System.arraycopy(data, 0, mSendBuffer, mSendBufPtr, data.length);
                mSendBufPtr += data.length;
                notify();
            }
        }

        @Override
        public void run() {
            while (!isCancelled())
            {
                if(!mConnected || mSendBufPtr == 0)
                {
                    //Just wait if we are not connected or the buffer is empty
                    synchronized (this)
                    {
                        try{
                            wait();
                        }catch (InterruptedException ie)
                        {
                            Log.w(LOGTAG, ie.getMessage());
                        }
                    }
                }else if(mSendBufPtr > 0){
                    byte[] toSend;

                    synchronized (this) {
                        toSend = Arrays.copyOf(mSendBuffer, mSendBufPtr);
                        mSendBufPtr = 0;
                    }

                    try {
                        mSendStream.write(toSend);
                        mSendStream.flush();
                    } catch (IOException ioe) {
                        Log.w(LOGTAG, "Error sending data. " + ioe.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void send(byte[] data) {
        if(mConnected) {
            mSenderRunnable.send(data);
        }
    }
}
