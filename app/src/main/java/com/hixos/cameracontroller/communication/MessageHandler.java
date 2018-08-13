package com.hixos.cameracontroller.communication;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MessageHandler {
    private static final String LOGTAG = "MessageHandler";

    public interface OnMessageReceivedListener
    {
        void onLogReceived(String log);
    }

    private List<OnMessageReceivedListener> mListeners = new ArrayList<>();

    public void registerOnMessageReceivedListener(OnMessageReceivedListener listener)
    {
        if(!mListeners.contains(listener))
        {
            mListeners.add(listener);
        }
    }

    public void removeOnMessageReceivedListener(OnMessageReceivedListener listener)
    {
        mListeners.remove(listener);
    }

    public void handleMessage(Message msg)
    {
        switch (msg.type)
        {
            case Message.MSGTYPE_LOG:
                handleLogMessage(msg);
                break;

        }
    }

    public void handleLogMessage(Message msg)
    {
        try{
            String log = new String(msg.data, "UTF-8");
            for(OnMessageReceivedListener l : mListeners)
            {
                l.onLogReceived(log);
            }
        }catch (UnsupportedEncodingException uee)
        {
            Log.w(LOGTAG, uee.getMessage());
        }

    }
}
