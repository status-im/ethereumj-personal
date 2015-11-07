package org.ethereum.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Messenger;

import org.ethereum.android.di.components.DaggerEthereumComponent;
import org.ethereum.android.di.components.EthereumComponent;
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
import org.ethereum.core.Genesis;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionExecutionSummary;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.HashUtil;
import org.ethereum.android.Ethereum;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.sync.PeersPool;
import org.spongycastle.util.encoders.Hex;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.ethereum.config.SystemProperties.CONFIG;

public class EthereumService extends Service {

    static boolean isConnected = false;

    static boolean isInitialized = false;

    protected static Ethereum ethereum = null;
    protected static EthereumComponent component = null;

    protected static JsonRpcServer jsonRpcServer;
    protected static Thread jsonRpcServerThread;

    public EthereumService() {
    }

    protected void broadcastEvent(EventFlag event, EventData data) {


    }

    @Override
    public void onCreate() {

        super.onCreate();
        /*
        if (!isInitialized) {
            isInitialized = true;
            new InitializeTask(null).execute();
        } else {
            System.out.println(" Already initialized");
            System.out.println("x " + (ethereum != null));
        }
        */
    }

    @Override
    public void onDestroy() {
        if (jsonRpcServerThread != null) {
            jsonRpcServerThread.interrupt();
            jsonRpcServerThread = null;
        }
        ethereum.close();
    }

    protected class InitializeTask extends AsyncTask<Void, Void, Void> {

        protected List<String> privateKeys = null;
        protected Object reply = null;
        protected Messenger replyTo = null;

        public InitializeTask(List<String> privateKeys, Messenger replyTo, Object reply) {

            this.privateKeys = privateKeys;
            this.replyTo = replyTo;
            this.reply = reply;
        }

        protected Void doInBackground(Void... args) {

            createEthereum();
            return null;
        }

        protected void onPostExecute(Void results) {

            onEthereumCreated(privateKeys, replyTo, reply);
        }
    }

    protected void onEthereumCreated(List<String> privateKeys, Messenger replyTo, Object reply) {

        /*
        if (false && ethereum != null) {
            if (privateKeys == null || privateKeys.size() == 0) {
                privateKeys = new ArrayList<>();
                byte[] cowAddr = HashUtil.sha3("cow".getBytes());
                privateKeys.add(Hex.toHexString(cowAddr));

                String secret = CONFIG.coinbaseSecret();
                byte[] cbAddr = HashUtil.sha3(secret.getBytes());
                privateKeys.add(Hex.toHexString(cbAddr));
            }
            ethereum.init(privateKeys);
            broadcastEvent(EventFlag.EVENT_SYNC_DONE, new EventData());
        }
        */
    }

    protected void createEthereum() {

        /*
        System.setProperty("sun.arch.data.model", "32");
        System.setProperty("leveldb.mmap", "false");

        String databaseFolder = getApplicationInfo().dataDir;
        System.out.println("Database folder: " + databaseFolder);
        CONFIG.setDataBaseDir(databaseFolder);
        System.out.println("Loading genesis");
        String genesisFile = CONFIG.genesisInfo();
        try {
            InputStream is = getApplication().getAssets().open("genesis/" + genesisFile);
            Genesis.androidGetInstance(is);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Genesis loaded");
        component = DaggerEthereumComponent.builder()
                .ethereumModule(new EthereumModule(this))
                .build();
        ethereum = component.ethereum();
        ethereum.addListener(new EthereumListener());
        PeersPool peersPool = component.peersPool();
        peersPool.setEthereum(ethereum);
        ChannelManager channelManager = component.channelManager();
        channelManager.setEthereum(ethereum);
        */
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
        public void onHandShakePeer(Node node, HelloMessage helloMessage) {

            broadcastEvent(EventFlag.EVENT_HANDSHAKE_PEER, new MessageEventData(helloMessage.getClass(), helloMessage.getEncoded()));
        }

        @Override
        public void onVMTraceCreated(String transactionHash, String trace) {

            broadcastEvent(EventFlag.EVENT_VM_TRACE_CREATED, new VMTraceCreatedEventData(transactionHash, trace));
        }

        @Override
        public void onEthStatusUpdated(Node node, StatusMessage status) {

            broadcastEvent(EventFlag.EVENT_TRACE, new TraceEventData("Eth status update: " + status.toString()));
        }

        @Override
        public void onNodeDiscovered(Node node) {

            broadcastEvent(EventFlag.EVENT_TRACE, new TraceEventData("Node discovered: " + node.toString()));
        }

        @Override
        public void onTransactionExecuted(TransactionExecutionSummary summary) {

            broadcastEvent(EventFlag.EVENT_TRACE, new TraceEventData("Transaction executed: " + summary.toString()));
        }
    }
}
