package com.hixos.cameracontroller.communication;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Message {

    public static final byte MSGTYPE_LOG         = 1;
    public static final byte MSGTYPE_TELECOMMAND = 2;
    public static final byte MSGTYPE_TELEMETRY   = 3;
    public static final byte MSGTYPE_FILE        = 4;

    public static final int MAGIC_WORD_1 = 0x54;
    public static final int MAGIC_WORD_2 = 0xF0;

    public static final int MSG_HEADER_SIZE = 5;
    public static final int MSG_MAX_LENGTH  = 0xFFFF + MSG_HEADER_SIZE;

    public byte type;
    public int size;
    public byte[] data = null;

    public Message()
    {

    }

    public Message(Message msg)
    {
        type = msg.type;
        size = msg.size;
    }

    @Nullable
    public byte[] encode()
    {
        if(size > 0xFFFF)
        {
            return null;
        }
        byte[] e = new byte[size + MSG_HEADER_SIZE];
        e[0] = (byte)MAGIC_WORD_1;
        e[1] = (byte)MAGIC_WORD_2;
        e[2] = type;
        e[3] = (byte)(size & 0xFF);
        e[4] = (byte)((size & 0xFF00) >> 8);
        System.arraycopy(data, 0, e, MSG_HEADER_SIZE, size);

        return e;
    }

    @Override
    public String toString()
    {
        return data.toString();
    }
}
