package org.ethereum.android;

import android.content.Context;

import org.ethereum.android.di.modules.EthereumModule;
import org.ethereum.android.di.components.DaggerEthereumComponent;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.android.manager.BlockLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ethereum.android.jsonrpc.JsonRpcServer;

public class EthereumManager {

    private static final Logger logger = LoggerFactory.getLogger("manager");

    public static Ethereum ethereum = null;

    private JsonRpcServer jsonRpcServer;


    public EthereumManager(Context context, String databaseFolder) {

        System.setProperty("sun.arch.data.model", "32");
        System.setProperty("leveldb.mmap", "false");

        if (databaseFolder != null) {
            System.out.println("Database folder: " + databaseFolder);
            SystemProperties.CONFIG.setDataBaseDir(databaseFolder);
        }

        ethereum = DaggerEthereumComponent.builder()
                .ethereumModule(new EthereumModule(context))
                .build().ethereum();

        //TODO: add here switch between full and light version
        jsonRpcServer = new org.ethereum.android.jsonrpc.light.JsonRpcServer(ethereum);
    }

    public void start() {

    }

    public long connect(String dumpFile) {

        long duration = 0;
        if (dumpFile == null) {
            ethereum.connect(SystemProperties.CONFIG.activePeerIP(),
                    SystemProperties.CONFIG.activePeerPort(),
                    SystemProperties.CONFIG.activePeerNodeid());
        } else {
            BlockLoader blockLoader = (BlockLoader)ethereum.getBlockLoader();
            blockLoader.loadBlocks(dumpFile);
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
        close();
    }

    public void close() {

        ethereum.close();
    }

}
