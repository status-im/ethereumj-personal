package org.ethereum.android_app;


import android.content.Intent;

public class EthereumService extends org.ethereum.android.service.EthereumRemoteService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
