package org.ethereum.android.service;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import org.ethereum.android.di.components.DaggerEthereumComponent;
import org.ethereum.android.di.modules.EthereumModule;
import org.ethereum.android.jsonrpc.JsonRpcServer;
import org.ethereum.android.manager.BlockLoader;
import org.ethereum.android.service.events.EventData;
import org.ethereum.android.service.events.EventFlag;
import org.ethereum.core.Genesis;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.HashUtil;
import org.ethereum.facade.Ethereum;
import org.ethereum.manager.AdminInfo;
import org.ethereum.net.peerdiscovery.PeerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.ethereum.config.SystemProperties.CONFIG;

public class EthereumRemoteService extends EthereumService {

    private static final Logger logger = LoggerFactory.getLogger("EthereumRemoteService");

    static HashMap<String, Messenger> clientListeners = new HashMap<>();
    static EnumMap<EventFlag, List<String>> listenersByType = new EnumMap<EventFlag, List<String>>(EventFlag.class);

    public boolean isEthereumStarted = false;

    public EthereumRemoteService() {

        super();

    }

    /** Handles incoming messages from clients. */
    static class IncomingHandler extends Handler {

        private final WeakReference<EthereumRemoteService> service;

        IncomingHandler(EthereumRemoteService service) {

            this.service = new WeakReference<EthereumRemoteService>(service);
        }

        @Override
        public void handleMessage(Message message) {

            EthereumRemoteService service = this.service.get();
            if (service != null) {
                if (!service.handleMessage(message)) {
                    super.handleMessage(message);
                }
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.Note
     * that calls to its binder are sequential!
     */
    final Messenger serviceMessenger = new Messenger(new IncomingHandler(this));

    /**
     * When binding to the service, we return an interface to our messenger for
     * sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {

        return serviceMessenger.getBinder();
    }

    @Override
    public void onCreate() {

        super.onCreate();
    }

    protected void broadcastEvent(EventFlag event, EventData data) {

        new BroadcastEventTask().execute(event, data);
    }

    protected class BroadcastEventTask extends AsyncTask<Object, Void, Void> {
        protected Void doInBackground(Object... params) {
            Message message = null;
            EventFlag event = (EventFlag)params[0];
            EventData data = (EventData)params[1];
            List<String> listeners = listenersByType.get(event);
            if (listeners != null) {
                for (String identifier: listeners) {
                    Messenger listener = clientListeners.get(identifier);
                    if (listener != null) {
                        message = createEventMessage(event, data);
                        message.obj = getIdentifierBundle(identifier);
                        try {
                            listener.send(message);
                        } catch (RemoteException e) {
                            logger.error("Exception sending event message to client listener: " + e.getMessage());
                        }
                    }
                }
            }
            return null;
        }

        protected void onPostExecute(Void results) {
            System.out.println("Event sent.");
        }
    }


    protected Bundle getIdentifierBundle(String identifier) {

        Bundle bundle = new Bundle();
        bundle.putString("identifier", identifier);
        return bundle;
    }

    protected Message createEventMessage(EventFlag event, EventData data) {

        Message message = Message.obtain(null, EthereumClientMessage.MSG_EVENT, 0, 0);
        Bundle replyData = new Bundle();
        replyData.putSerializable("event", event);
        replyData.putParcelable("data", data);
        message.setData(replyData);

        return message;
    }


    protected boolean handleMessage(Message message) {

        switch (message.what) {

            case EthereumServiceMessage.MSG_INIT:
                init(message);
                break;

            case EthereumServiceMessage.MSG_CONNECT:
                connect(message);
                break;

            case EthereumServiceMessage.MSG_LOAD_BLOCKS:
                loadBlocks(message);
                break;

            case EthereumServiceMessage.MSG_START_JSON_RPC_SERVER:
                startJsonRpc(message);
                break;

            case EthereumServiceMessage.MSG_FIND_ONLINE_PEER:
                findOnlinePeer(message);
                break;

            case EthereumServiceMessage.MSG_GET_PEERS:
                getPeers(message);
                break;

            case EthereumServiceMessage.MSG_START_PEER_DISCOVERY:
                startPeerDiscovery(message);
                break;

            case EthereumServiceMessage.MSG_STOP_PEER_DISCOVERY:
                stopPeerDiscovery(message);
                break;

            case EthereumServiceMessage.MSG_GET_BLOCKCHAIN_STATUS:
                getBlockchainStatus(message);
                break;

            case EthereumServiceMessage.MSG_ADD_LISTENER:
                addListener(message);
                break;

            case EthereumServiceMessage.MSG_REMOVE_LISTENER:
                removeListener(message);
                break;

            case EthereumServiceMessage.MSG_GET_CONNECTION_STATUS:
                getConnectionStatus(message);
                break;

            case EthereumServiceMessage.MSG_CLOSE:
                closeEthereum(message);
                break;

            case EthereumServiceMessage.MSG_SUBMIT_TRANSACTION:
                submitTransaction(message);
                break;

            case EthereumServiceMessage.MSG_GET_ADMIN_INFO:
                getAdminInfo(message);
                break;

            case EthereumServiceMessage.MSG_GET_PENDING_TRANSACTIONS:
                getPendingTransactions(message);
                break;

            default:
                return false;
        }

        return true;
    }

    @Override
    protected void onEthereumCreated(List<String> privateKeys) {

        if (ethereum != null) {
            ethereum.init(privateKeys);
            startJsonRpc(null);
            broadcastEvent(EventFlag.EVENT_SYNC_DONE, new EventData());
            isEthereumStarted = true;
            isInitialized = true;
        }
    }

    @Override
    protected void createEthereum() {

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
        //component.udpListener();
        ethereum = component.ethereum();
        ethereum.addListener(new EthereumListener());
    }

    protected void init(Message message) {

        if (isEthereumStarted) {
            closeEthereum(null);
            ethereum = null;
            component = null;
            isInitialized = false;
        }
        Bundle data = message.getData();
        List<String> privateKeys = data.getStringArrayList("privateKeys");
        new InitializeTask(privateKeys).execute();
    }

    /**
     * Connect to peer
     *
     * Incoming message parameters ( "key": type [description] ):
     * {
     *     "ip": String  [peer ip address]
     *     "port": int  [peer port]
     *     "remoteId": String  [peer remoteId]
     * }
     * Sends message: none
     */
    protected void connect(Message message) {

        if (!isConnected) {
            //isConnected = true;
            new ConnectTask(message).execute(ethereum);
        }
    }

    protected class ConnectTask extends AsyncTask<Ethereum, Message, Void> {

        String ip;
        int port;
        String remoteId;

        public ConnectTask(Message message) {

            Bundle data = message.getData();
            ip = data.getString("ip");
            port = data.getInt("port");
            remoteId = data.getString("remoteId");
        }

        protected Void doInBackground(Ethereum... args) {

            Ethereum ethereum = args[0];
            try {
                ethereum.connect(ip, port, remoteId);
            } catch(Exception e) {
                System.out.println(e.getMessage());
            }
            logger.info("Ethereum connecting to : " + ip + ":" + port);
            return null;
        }

        protected void onPostExecute(Void results) {


        }
    }

    /**
     * Load blocks from dump file
     *
     * Incoming message parameters ( "key": type [description] ):
     * {
     *     "dumpFile": String  [blocks dump file path]
     * }
     * Sends message: none
     */
    protected void loadBlocks(Message message) {

        if (!isConnected) {
            isConnected = true;
            new LoadBlocksTask(message).execute(ethereum);
        }
    }

    protected class LoadBlocksTask extends AsyncTask<Ethereum, Message, Void> {

        String dumpFile;

        public LoadBlocksTask(Message message) {

            Bundle data = message.getData();
            dumpFile = data.getString("dumpFile");
        }

        protected Void doInBackground(Ethereum... args) {

            Ethereum ethereum = args[0];
            logger.info("Loading blocks from: " + dumpFile);
            BlockLoader blockLoader = (BlockLoader)ethereum.getBlockLoader();
            blockLoader.loadBlocks(dumpFile);
            logger.info("Finished loading blocks from: " + dumpFile);
            return null;
        }

        protected void onPostExecute(Void results) {


        }
    }

    /**
     * Start the json rpc server
     *
     * Incoming message parameters: none
     * Sends message: none
     */
    protected void startJsonRpc(Message message) {

        if (jsonRpcServer == null) {
            //TODO: add here switch between full and light version
            jsonRpcServer = new org.ethereum.android.jsonrpc.light.JsonRpcServer(ethereum);
        }
        if (jsonRpcServerThread == null) {
            jsonRpcServerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        jsonRpcServer.start();
                        logger.info("Started json rpc server!");
                    } catch (Exception e) {
                        logger.error("Exception starting json rpc server: " + e.getMessage());
                    }
                }
            });
            jsonRpcServerThread.start();
        }
    }

    /**
     * Find an online peer
     *
     * Incoming message parameters ( "key": type [description] ):
     * {
     *      "excludePeer": Parcelable(PeerInfo) [peer to exclude from search]
     * }
     * Sends message parameters ( "key": type [description] ):
     * {
     *     "peerInfo": Parcelable(PeerInfo) [found online peer, or null if error / online peer not found]
     * }
     */
    protected void findOnlinePeer(Message message) {

        Bundle data = message.getData();
        PeerInfo foundPeerInfo;
        PeerInfo peerInfo = data.getParcelable("excludePeer");
        if (peerInfo != null) {
            foundPeerInfo = ethereum.findOnlinePeer(peerInfo);
        } else {
            PeerInfo[] excludePeerSet = (PeerInfo[])data.getParcelableArray("excludePeerSet");
            if (excludePeerSet != null) {
                foundPeerInfo = ethereum.findOnlinePeer(new HashSet<>(Arrays.asList(excludePeerSet)));
            } else {
                foundPeerInfo = ethereum.findOnlinePeer();
            }
        }
        // Send reply
        Message replyMessage = Message.obtain(null, EthereumClientMessage.MSG_ONLINE_PEER, 0, 0, message.obj);
        Bundle replyData = new Bundle();
        replyData.putParcelable("peerInfo", new org.ethereum.android.interop.PeerInfo(foundPeerInfo));
        replyMessage.setData(replyData);
        try {
            message.replyTo.send(replyMessage);
            logger.info("Sent online peer to client: " + foundPeerInfo.toString());
        } catch (RemoteException e) {
            logger.error("Exception sending online peer to client: " + e.getMessage());
        }
    }

    /**
     * Get etherum peers
     *
     * Incoming message parameters: none
     * Sends message ( "key": type [description] ):
     * {
     *     "peers": Parcelable[](PeerInfo[]) [ethereum peers]
     * }
     */
    protected void getPeers(Message message) {

        Set<PeerInfo> peers = ethereum.getPeers();
        Message replyMessage = Message.obtain(null, EthereumClientMessage.MSG_PEERS, 0, 0, message.obj);
        Bundle replyData = new Bundle();
        org.ethereum.android.interop.PeerInfo[] convertedPeers = new org.ethereum.android.interop.PeerInfo[peers.size()];
        int index = 0;
        for (PeerInfo peerInfo: peers) {
            convertedPeers[index] = new org.ethereum.android.interop.PeerInfo(peerInfo);
            index++;
        }
        replyData.putParcelableArray("peers", convertedPeers);
        replyMessage.setData(replyData);
        try {
            message.replyTo.send(replyMessage);
            logger.info("Sent peers to client: " + peers.size());
        } catch (RemoteException e) {
            logger.error("Exception sending peers to client: " + e.getMessage());
        }
    }

    /**
     * Starts ethereum peer discovery
     * Incoming message parameters: none
     * Sends message: none
     */
    protected void startPeerDiscovery(Message message) {

        ethereum.startPeerDiscovery();
        logger.info("Started peer discovery.");
    }

    /**
     * Stops ethereum peer discovery
     * Incoming message parameters: none
     * Sends message: none
     */
    protected void stopPeerDiscovery(Message message) {

        ethereum.stopPeerDiscovery();
        logger.info("Stopped peer discovery.");
    }

    //TODO: remove this
    /**
     * Gets the blockchain status
     *
     * Incoming message parameters: none
     * Sends message ( "key": type [description] ):
     * {
     *     "status": String [blockchain status: Loading/Loaded]
     * }
     */
    protected void getBlockchainStatus(Message message) {

        boolean isLoading = false;
        String status = isLoading ? "Loading" : "Loaded";
        Message replyMessage = Message.obtain(null, EthereumClientMessage.MSG_BLOCKCHAIN_STATUS, 0, 0, message.obj);
        Bundle replyData = new Bundle();
        replyData.putString("status", status);
        replyMessage.setData(replyData);
        try {
            message.replyTo.send(replyMessage);
            logger.info("Sent blockchain status: " + status);
        } catch (RemoteException e) {
            logger.error("Exception sending blockchain status to client: " + e.getMessage());
        }
    }

    /**
     * Add ethereum event listener
     *
     * Incoming message parameters ( "key": type [description] ):
     * {
     *      "flags": Serializable(EnumSet<ListenerFlag>) [defines flags to listen to specific events]
     * }
     * Sends message: none
     */
    protected void addListener(Message message) {

        // Register the client's messenger
        String identifier = ((Bundle)message.obj).getString("identifier");
        clientListeners.put(identifier, message.replyTo);
        Bundle data = message.getData();
        data.setClassLoader(EventFlag.class.getClassLoader());
        EnumSet<EventFlag> flags = (EnumSet<EventFlag>)data.getSerializable("flags");
        EnumSet<EventFlag> list = (flags == null || flags.contains(EventFlag.EVENT_ALL)) ? EnumSet.allOf(EventFlag.class) : flags;
        for (EventFlag flag: list) {
            List<String> listeners = listenersByType.get(flag);
            boolean shouldAdd = false;
            if (listeners == null) {
                listeners = new ArrayList<>();
                shouldAdd = true;
            }
            if (shouldAdd || !listeners.contains(identifier)) {
                listeners.add(identifier);
                listenersByType.put(flag, listeners);
            }
        }
        logger.info("Client listener registered!");
    }

    /**
     * Remove etherum event listener
     *
     * Incoming message parameters: none
     * Sends message: none
     */
    protected void removeListener(Message message) {

        String identifier = ((Bundle)message.obj).getString("identifier");
        clientListeners.remove(identifier);
        for (EventFlag flag: EventFlag.values()) {
            List<String> listeners = listenersByType.get(flag);
            if (listeners != null && listeners.contains(identifier)) {
                listeners.remove(identifier);
            }
        }
        logger.info("Client listener unregistered!");
    }

    /**
     * Closes ethereum
     *
     * Incoming message parameters: none
     * Sends message: none
     */
    protected void closeEthereum(Message message) {

        ethereum.close();
        isEthereumStarted = false;
        logger.info("Closed ethereum.");
    }

    /**
     * Get connection status
     *
     * Incoming message parameters: none
     * Sends message ( "key": type [description] ):
     * {
     *     "status": String [ethereum connection status: Connected/Not Connected]
     * }
     */
    protected void getConnectionStatus(Message message) {

        String status = ethereum.isConnected() ? "Connected" : "Not Connected";
        Message replyMessage = Message.obtain(null, EthereumClientMessage.MSG_CONNECTION_STATUS, 0, 0, message.obj);
        Bundle replyData = new Bundle();
        replyData.putString("status", status);
        replyMessage.setData(replyData);
        try {
            message.replyTo.send(replyMessage);
            logger.info("Sent ethereum connection status: " + status);
        } catch (RemoteException e) {
            logger.error("Exception sending ethereum connection status to client: " + e.getMessage());
        }
    }

    /**
     * Submit ethereum transaction
     *
     * Incoming message parameters ( "key": type [description] ):
     * {
     *     "transaction": Parcelable(Transaction) [ethereum transaction to submit]
     * }
     * Sends message ( "key": type [description] ):
     * {
     *     "transaction": Parcelable(Transaction) [submitted transaction]
     * }
     */
    protected void submitTransaction(Message message) {

        if (!isConnected) {
            isConnected = true;
            new SubmitTransactionTask(message).execute(ethereum);
        } else {
            logger.warn("Ethereum not connected.");
        }
    }

    protected class SubmitTransactionTask extends AsyncTask<Ethereum, Void, Transaction> {

        Transaction transaction;
        Message message;

        public SubmitTransactionTask(Message message) {

            this.message = message;
            Bundle data = message.getData();
            transaction = data.getParcelable("transaction");
        }

        protected Transaction doInBackground(Ethereum... args) {

            Transaction submitedTransaction = null;
            try {
                submitedTransaction = ethereum.submitTransaction(transaction).get(CONFIG.transactionApproveTimeout(), TimeUnit.SECONDS);
                logger.info("Submitted transaction.");
            } catch (Exception e) {
                logger.error("Exception submitting transaction: " + e.getMessage());
            }

            return submitedTransaction;
        }

        protected void onPostExecute(Transaction submittedTransaction) {

            Message replyMessage = Message.obtain(null, EthereumClientMessage.MSG_SUBMIT_TRANSACTION_RESULT, 0, 0, message.obj);
            Bundle replyData = new Bundle();
            replyData.putParcelable("transaction", new org.ethereum.android.interop.Transaction(submittedTransaction));
            replyMessage.setData(replyData);
            try {
                message.replyTo.send(replyMessage);
                logger.info("Sent submitted transaction: " + submittedTransaction.toString());
            } catch (RemoteException e) {
                logger.error("Exception sending submitted transaction to client: " + e.getMessage());
            }
        }
    }

    /**
     * Get admin info
     *
     * Incoming message parameters: none
     * Sends message ( "key": type [description] ):
     * {
     *     "adminInfo": Parcelable(AdminInfo) [ethereum admin info]
     * }
     */
    protected void getAdminInfo(Message message) {

        Message replyMessage = Message.obtain(null, EthereumClientMessage.MSG_ADMIN_INFO, 0, 0, message.obj);
        Bundle replyData = new Bundle();
        AdminInfo info = ethereum.getAdminInfo();
        replyData.putParcelable("adminInfo", new org.ethereum.android.interop.AdminInfo(info));
        replyMessage.setData(replyData);
        try {
            message.replyTo.send(replyMessage);
            logger.info("Sent admin info: " + info.toString());
        } catch (RemoteException e) {
            logger.error("Exception sending admin info to client: " + e.getMessage());
        }
    }

    /**
     * Get pending transactions
     *
     * Incoming message parameters: none
     * Sends message ( "key": type [description] ):
     * {
     *     "transactions": ParcelableArray(Transaction[]) [ethereum pending transactions]
     * }
     */
    protected void getPendingTransactions(Message message) {

        Message replyMessage = Message.obtain(null, EthereumClientMessage.MSG_PENDING_TRANSACTIONS, 0, 0, message.obj);
        Bundle replyData = new Bundle();
        Set<Transaction> transactions = ethereum.getPendingTransactions();
        org.ethereum.android.interop.Transaction[] convertedTransactions = new org.ethereum.android.interop.Transaction[transactions.size()];
        int index = 0;
        for (Transaction transaction: transactions) {
            convertedTransactions[index] = new org.ethereum.android.interop.Transaction(transaction);
            index++;
        }
        replyData.putParcelableArray("transactions", convertedTransactions);
        replyMessage.setData(replyData);
        try {
            message.replyTo.send(replyMessage);
            logger.info("Sent pending transactions: " + transactions.size());
        } catch (RemoteException e) {
            logger.error("Exception sending pending transactions to client: " + e.getMessage());
        }
    }
}
