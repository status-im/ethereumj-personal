package org.ethereum.android.service;


import android.os.Message;

public interface ConnectorHandler {

    boolean handleMessage(Message message);
    void onConnectorConnected();
    void onConnectorDisconnected();
    String getID();
}
