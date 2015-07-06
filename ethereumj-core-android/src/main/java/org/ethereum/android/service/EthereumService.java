package org.ethereum.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.ethereum.android.di.components.DaggerEthereumComponent;
import org.ethereum.android.di.modules.EthereumModule;
import org.ethereum.android.jsonrpc.JsonRpcServer;
import org.ethereum.android.service.events.BlockEventData;
import org.ethereum.android.service.events.EventData;
import org.ethereum.android.service.events.EventFlag;
import org.ethereum.android.service.events.MessageEventData;
import org.ethereum.android.service.events.PeerDisconnectEventData;
import org.ethereum.android.service.events.PendingTransactionsEventData;
import org.ethereum.android.service.events.TraceEventData;
import org.ethereum.android.service.events.VMTraceCreatedEventData;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.facade.Ethereum;
import org.ethereum.net.p2p.HelloMessage;

import java.util.List;
import java.util.Set;

public class EthereumService extends Service {

    static boolean isConnected = false;

    static boolean isInitialized = false;

    static Ethereum ethereum = null;

    static JsonRpcServer jsonRpcServer;
    static Thread jsonRpcServerThread;

    public EthereumService() {
    }

    protected void broadcastEvent(EventFlag event, EventData data) {


    }

    @Override
    public void onCreate() {

        super.onCreate();
        initializeEthereum();
    }

    @Override
    public void onDestroy() {
        if (jsonRpcServerThread != null) {
            jsonRpcServerThread.interrupt();
        }
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

            broadcastEvent(EventFlag.EVENT_TRACE, new TraceEventData(output));
        }

        @Override
        public void onBlock(org.ethereum.core.Block block, List<TransactionReceipt> receipts) {

            broadcastEvent(EventFlag.EVENT_BLOCK, new BlockEventData(block, receipts));
        }

        @Override
        public void onRecvMessage(org.ethereum.net.message.Message message) {

            broadcastEvent(EventFlag.EVENT_RECEIVE_MESSAGE, new MessageEventData(message.getClass(), message.getEncoded()));
        }

        @Override
        public void onSendMessage(org.ethereum.net.message.Message message) {

            broadcastEvent(EventFlag.EVENT_SEND_MESSAGE, new MessageEventData(message.getClass(), message.getEncoded()));
        }

        @Override
        public void onPeerDisconnect(String host, long port) {

            broadcastEvent(EventFlag.EVENT_PEER_DISCONNECT, new PeerDisconnectEventData(host, port));
        }

        @Override
        public void onPendingTransactionsReceived(Set<Transaction> transactions) {

            broadcastEvent(EventFlag.EVENT_PENDING_TRANSACTIONS_RECEIVED, new PendingTransactionsEventData(transactions));
        }

        @Override
        public void onSyncDone() {

            broadcastEvent(EventFlag.EVENT_SYNC_DONE, new EventData());
        }

        @Override
        public void onNoConnections() {

            broadcastEvent(EventFlag.EVENT_NO_CONNECTIONS, new EventData());
        }

        @Override
        public void onHandShakePeer(HelloMessage helloMessage) {

            broadcastEvent(EventFlag.EVENT_HANDSHAKE_PEER, new MessageEventData(helloMessage.getClass(), helloMessage.getEncoded()));
        }

        @Override
        public void onVMTraceCreated(String transactionHash, String trace) {

            broadcastEvent(EventFlag.EVENT_VM_TRACE_CREATED, new VMTraceCreatedEventData(transactionHash, trace));
        }
    }
}
