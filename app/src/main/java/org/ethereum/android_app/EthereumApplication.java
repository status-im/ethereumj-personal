package org.ethereum.android_app;


import android.os.Message;
import android.support.multidex.MultiDexApplication;

import org.ethereum.android.service.ConnectorHandler;
import org.ethereum.android.service.EthereumConnector;

public class EthereumApplication extends MultiDexApplication implements ConnectorHandler {

    public static EthereumConnector ethereum = null;
    public static String log = "";

    @Override public void onCreate() {

        super.onCreate();
        if (ethereum == null) {
            ethereum = new EthereumConnector(this, EthereumService.class);
            ethereum.registerHandler(this);
            ethereum.bindService();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ethereum.removeHandler(this);
        ethereum.unbindService();
        ethereum = null;
    }

    @Override
    public void onConnectorConnected() {
        System.out.println("Connector connected");
    }

    @Override
    public void onConnectorDisconnected() {

    }

    @Override
    public String getID() {
        return "1";
    }

    @Override
    public boolean handleMessage(Message message) {

        System.out.println(message.toString());
        return true;
    }
}