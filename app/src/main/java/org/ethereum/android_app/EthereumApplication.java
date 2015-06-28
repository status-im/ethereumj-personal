package org.ethereum.android_app;


import android.support.multidex.MultiDexApplication;

import org.ethereum.android.service.EthereumConnector;

public class EthereumApplication extends MultiDexApplication {

    public EthereumConnector ethereum = null;

    @Override public void onCreate() {

        super.onCreate();
        ethereum = new EthereumConnector(this, EthereumRemoteService.class);
        ethereum.bindService();
    }

    @Override
    public void onTerminate() {

        super.onTerminate();
        ethereum.unbindService();
    }
}