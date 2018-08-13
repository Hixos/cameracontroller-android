package com.hixos.cameracontroller.communication;

import android.content.Context;

import java.util.List;

public interface DataConnectionInterface {
    boolean connect(Context context);
    void disconnect();

    void registerConnectionListener(ConnectionListener listener);
    void unregisterConnectionListener(ConnectionListener listener);

    void send(byte[] data);

;
}
