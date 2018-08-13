package com.hixos.cameracontroller.communication;

import java.util.List;

public interface ConnectionListener {
    void onConnectionAttempt(int attempt_num);
    void onConnectionFailed();
    void onConnect();
    void onDisconnect();
    void onDataReceived(byte[] data);
}
