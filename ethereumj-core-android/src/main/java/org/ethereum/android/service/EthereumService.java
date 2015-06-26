package org.ethereum.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.ethereum.android.di.components.DaggerEthereumComponent;
import org.ethereum.android.di.modules.EthereumModule;
import org.ethereum.android.interop.IListener;
import org.ethereum.android.jsonrpc.JsonRpcServer;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.facade.Ethereum;
import org.ethereum.net.p2p.HelloMessage;

import java.util.List;
import java.util.Set;

public class EthereumService extends Service {

    boolean isConnected = false;

    boolean isInitialized = false;

    protected Ethereum ethereum = null;

    protected JsonRpcServer jsonRpcServer;

    public EthereumService() {
    }

    protected void broadcastMessage(String message) {

    }

    @Override
    public void onCreate() {

        super.onCreate();
        initializeEthereum();
    }

    protected void initializeEthereum() {

        if (!isInitialized) {
            System.setProperty("sun.arch.data.model", "32");
            System.setProperty("leveldb.mmap", "false");

            String databaseFolder = getApplicationInfo().dataDir;
            System.out.println("Database folder: " + databaseFolder);
            SystemProperties.CONFIG.setDataBaseDir(databaseFolder);

            ethereum = DaggerEthereumComponent.builder()
                    .ethereumModule(new EthereumModule(this))
                    .build().ethereum();
            ethereum.addListener(new EthereumListener());
            isInitialized = true;
        } else {
            System.out.println(" Already initialized");
            System.out.println("x " + (ethereum != null));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected class EthereumListener implements org.ethereum.listener.EthereumListener {

        @Override
        public void trace(String output) {

            broadcastMessage(output);
        }

        @Override
        public void onBlock(org.ethereum.core.Block block, List<TransactionReceipt> receipts) {

            broadcastMessage("Added block.");
        }

        @Override
        public void onRecvMessage(org.ethereum.net.message.Message message) {

            broadcastMessage("Received message: " + message.getCommand().name());
        }

        @Override
        public void onSendMessage(org.ethereum.net.message.Message message) {

            broadcastMessage("Sending message: " + message.getCommand().name());
        }

        @Override
        public void onPeerDisconnect(String host, long port) {

            broadcastMessage("Peer disconnected: " + host + ":" + port);
        }

        @Override
        public void onPendingTransactionsReceived(Set<Transaction> transactions) {

            broadcastMessage("Pending transactions received: " + transactions.size());
        }

        @Override
        public void onSyncDone() {

            broadcastMessage("Sync done");
        }

        @Override
        public void onNoConnections() {

            broadcastMessage("No connections");
        }

        @Override
        public void onHandShakePeer(HelloMessage helloMessage) {

            broadcastMessage("Peer handshaked: " + helloMessage.getCode());
        }

        @Override
        public void onVMTraceCreated(String transactionHash, String trace) {

            broadcastMessage("Trace created: " + " - ");
        }
    }
}
