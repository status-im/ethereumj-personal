package org.ethereum.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import org.ethereum.android.di.components.DaggerEthereumComponent;
import org.ethereum.android.di.modules.EthereumModule;
import org.ethereum.android.jsonrpc.JsonRpcServer;
import org.ethereum.android.manager.BlockLoader;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.facade.Ethereum;
import org.ethereum.android.interop.*;
import org.ethereum.net.p2p.HelloMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EthereumAidlService extends Service {

    protected Ethereum ethereum = null;

    protected JsonRpcServer jsonRpcServer;

    protected ArrayList<IListener> clientListeners = new ArrayList<>();

    public static String log = "";

    boolean isConnected = false;

    boolean isInitialized = false;

    public EthereumAidlService() {
    }

    protected void broadcastMessage(String message) {

        for (IListener listener: clientListeners) {
            try {
                listener.trace(message);
            } catch (Exception e) {
                // Remove listener
                System.out.println("ERRORRRR: " + e.getMessage());
                clientListeners.remove(listener);
            }
        }
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

        return mBinder;
    }

    IEthereumService.Stub mBinder = new IEthereumService.Stub() {

        public void loadBlocks(String dumpFile) throws RemoteException {

            BlockLoader blockLoader = (BlockLoader)ethereum.getBlockLoader();
            blockLoader.loadBlocks(dumpFile);
        }

        public void connect(String ip, int port, String remoteId) throws RemoteException {

            if (!isConnected) {
                System.out.println("Connecting to : " + ip);
                ethereum.connect(ip, port, remoteId);
                isConnected = true;
            } else {
                System.out.println("Already connected");
                System.out.println("x " + ethereum.isConnected());
            }
        }

        public void addListener(IListener listener) throws RemoteException {

            clientListeners.clear();
            clientListeners.add(listener);
        }

        public void removeListener(IListener listener) throws RemoteException {

            try {
                clientListeners.remove(listener);
            } catch (Exception e) {
                System.out.println("ERRORRRR: " + e.getMessage());
            }
        }

        public void startJsonRpcServer() throws RemoteException {

            jsonRpcServer = new JsonRpcServer(ethereum);
        }

        public void getLog(IAsyncCallback callback) throws  RemoteException {

            callback.handleResponse(EthereumAidlService.log);
        }
    };

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
