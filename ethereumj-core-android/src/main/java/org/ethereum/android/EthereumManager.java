package org.ethereum.android;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.ethereum.android.di.modules.EthereumModule;
import org.ethereum.android.di.components.DaggerEthereumComponent;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ethereum.android.jsonrpc.JsonRpcServer;

import static org.ethereum.config.SystemProperties.CONFIG;

public class EthereumManager {

    private static final Logger logger = LoggerFactory.getLogger("manager");

    public static Ethereum ethereum = null;

    private JsonRpcServer jsonRpcServer;


    public EthereumManager(Context context) {
        System.setProperty("sun.arch.data.model", "32");
        System.setProperty("leveldb.mmap", "false");
        ethereum = DaggerEthereumComponent.builder()
                .ethereumModule(new EthereumModule(context))
                .build().ethereum();

        jsonRpcServer = new JsonRpcServer(ethereum);
    }

    public void start() {

    }

    public long connect() {

        long duration = 0;
        if (CONFIG.blocksLoader().equals("")) {
            ethereum.connect(SystemProperties.CONFIG.activePeerIP(),
                    SystemProperties.CONFIG.activePeerPort(),
                    SystemProperties.CONFIG.activePeerNodeid());
        } else {
            ethereum.getBlockLoader().loadBlocks();
        }
        return duration;
    }

    public void startPeerDiscovery() {

        ethereum.startPeerDiscovery();
    }

    public void addListener(EthereumListenerAdapter listener) {

        ethereum.addListener(listener);
    }

    public void startJsonRpc() throws Exception {

        jsonRpcServer.start();
    }

    public void onDestroy() {
        OpenHelperManager.releaseHelper();
    }

}
