package com.hixos.cameracontroller.communication;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MessageDecoder implements ConnectionListener{
    private static final String LOGTAG = "MessageDecoder";
    enum DecoderState
    {
        MAGIC,
        TYPE,
        SIZE,
        DATA
    }

    private DecoderState mState = DecoderState.MAGIC;

    private byte mMagicCount = 0;
    private byte mSizeCount = 0;
    private int mDataPointer = 0;

    private Message mBuildingMessage;

    private MessageHandler mMessageHandler;

    public MessageDecoder(MessageHandler handler)
    {
        mMessageHandler = handler;
    }

    @Override
    public void onDataReceived(byte[] data) {
        for(int i = 0; i < data.length; i++) {
            //Get unsigned int value
            int intVal = ((int)data[i]) & 0xFF;
            switch (mState) {
                case MAGIC:
                    if (mMagicCount == 0) {
                        if(intVal == Message.MAGIC_WORD_1)
                        {
                            mMagicCount++;
                        }
                    }else
                    {
                        mMagicCount = 0;
                        if(intVal == Message.MAGIC_WORD_2)
                        {
                            mState = DecoderState.TYPE;
                        }
                    }
                    break;
                case TYPE:
                    mBuildingMessage = new Message();
                    mBuildingMessage.type = data[i];
                    mState = DecoderState.SIZE;
                    break;
                case SIZE:
                    if(mSizeCount == 0) {
                        mBuildingMessage.size = intVal;
                        mSizeCount++;
                    }else{
                        mSizeCount = 0;
                        mBuildingMessage.size += intVal << 8;
                        mState = DecoderState.DATA;
                    }
                    break;
                case DATA: {
                    if (mBuildingMessage.data == null) {
                        mBuildingMessage.data = new byte[mBuildingMessage.size];
                    }
                    int toCopy = Math.min(data.length - i, mBuildingMessage.size);

                    System.arraycopy(data, i, mBuildingMessage.data, mDataPointer, toCopy);

                    i += toCopy-1; //i is incremented by one on the next loop
                    mDataPointer += toCopy;

                    if (mBuildingMessage.data.length == mBuildingMessage.size) {
                        mState = DecoderState.MAGIC;
                        mDataPointer = 0;
                        mMessageHandler.handleMessage(mBuildingMessage);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onConnectionAttempt(int attempt_num) {

    }

    @Override
    public void onConnectionFailed() {

    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onDisconnect() {

    }
}
