package org.ethereum.android;

import android.content.Context;

import org.ethereum.android.di.components.DaggerEthereumComponent;
import org.ethereum.android.di.modules.EthereumModule;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthereumManager {

    private static final Logger logger = LoggerFactory.getLogger("manager");

    public static Ethereum ethereum = null;


    public EthereumManager(Context context) {
        System.setProperty("sun.arch.data.model", "32");
        System.setProperty("leveldb.mmap", "false");
        ethereum = DaggerEthereumComponent.builder()
                .ethereumModule(new EthereumModule(context))
                .build().ethereum();
    }

    public void start() {

    }

    public void connect() {

        ethereum.connect(SystemProperties.CONFIG.activePeerIP(),
                SystemProperties.CONFIG.activePeerPort(),
                SystemProperties.CONFIG.activePeerNodeid());
        //ethereum.getBlockchain();
    }

    public void startPeerDiscovery() {

        ethereum.startPeerDiscovery();
    }

    public void addListener(EthereumListenerAdapter listener) {

        ethereum.addListener(listener);
    }

}
