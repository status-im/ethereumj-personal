package org.ethereum.android.service;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import org.ethereum.android.service.events.EventFlag;
import org.ethereum.core.Transaction;
import org.ethereum.net.peerdiscovery.PeerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;


public class EthereumConnector extends ServiceConnector {

    private static final Logger logger = LoggerFactory.getLogger("EthereumConnector");

    protected ArrayList<Message> messages = new ArrayList<>();

    public EthereumConnector(Context context, Class serviceClass) {

        super(context, serviceClass);
    }

    @Override
    protected void onConnected() {

        if (messages.size() > 0) {
            for(Message message: messages) {
               sendMessage(message);
            }
            messages.clear();
        }
    }

    protected void sendMessage(Message message) {

        if (!isBound) {
            messages.add(message);
        } else {
            try {
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                logger.error("Exception sending message(" + message.toString() + ") to service: " + e.getMessage());
            }
        }
    }

    protected Bundle getIdentifierBundle(String identifier) {

        Bundle bundle = new Bundle();
        bundle.putString("identifier", identifier);
        return bundle;
    }

    public void init(List<String> privateKeys) {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_INIT, 0, 0);
        Bundle data = new Bundle();
        data.putStringArrayList("privateKeys", (ArrayList) privateKeys);
        msg.setData(data);
        sendMessage(msg);
    }

    /**
     * Connect ethereum to peer
     * @param ip String Peer ip address
     * @param port int Peer port
     * @param remoteId String Peer remote id
     *
     * Sends message parameters ( "key": type [description] ):
     * {
     *     "ip": String [Peer ip address]
     *     "port": int [Peer port]
     *     "remoteId": String [Peer remote id]
     * }
     */
    public void connect(String ip, int port, String remoteId) {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_CONNECT, 0, 0);
        Bundle data = new Bundle();
        data.putString("ip", ip);
        data.putInt("port", port);
        data.putString("remoteId", remoteId);
        msg.setData(data);
        sendMessage(msg);
    }

    /**
     * Load blocks from dump file
     * @param dumpFile String Blocks dump file path
     *
     * Sends message parameters ( "key": type [description] ):
     * {
     *     "dumpFile": String [Blocks dump file paths]
     * }
     */
    public void loadBlocks(String dumpFile) {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_LOAD_BLOCKS, 0, 0);
        Bundle data = new Bundle();
        data.putString("dumpFile", dumpFile);
        msg.setData(data);
        sendMessage(msg);
    }

    /**
     * Start the json rpc server
     *
     * Sends message parameters: none
     */
    public void startJsonRpc() {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_START_JSON_RPC_SERVER, 0, 0);
        sendMessage(msg);
    }

    /**
     * Find an online peer
     * @param identifier String Caller identifier used to return the response
     *
     * Sends message parameters: none
     */
    public void findOnlinePeer(String identifier) {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_FIND_ONLINE_PEER, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        sendMessage(msg);
    }

    /**
     * Find an online peer
     * @param identifier String Caller identifier used to return the response
     * @param excludePeer PeerInfo Excluded peer from search
     *
     * Sends message parameters ( "key": type [description] ):
     * {
     *     "excludePeer": Parcelable(PeerInfo) [Exclude peer from search]
     * }
     */
    public void findOnlinePeer(String identifier, PeerInfo excludePeer) {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_FIND_ONLINE_PEER, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        Bundle data = new Bundle();
        data.putParcelable("excludePeer", (org.ethereum.android.interop.PeerInfo) excludePeer);
        msg.setData(data);
        sendMessage(msg);
    }

    /**
     * Find an online peer
     * @param identifier String Caller identifier used to return the response
     * @param excludePeerSet PeerInfo[] Excluded peers from search
     *
     * Sends message parameters ( "key": type [description] ):
     * {
     *     "excludePeerSet": ParcelableArray(PeerInfo[]) [Excluded peers from search]
     * }
     */
    public void findOnlinePeer(String identifier, PeerInfo[] excludePeerSet) {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_FIND_ONLINE_PEER, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        Bundle data = new Bundle();
        data.putParcelableArray("excludePeerSet", (org.ethereum.android.interop.PeerInfo[]) excludePeerSet);
        msg.setData(data);
        sendMessage(msg);
    }

    /**
     * Get etherum peers
     * @param identifier String Caller identifier used to return the response
     *
     * Sends message parameters: none
     */
    public void getPeers(String identifier) {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_GET_PEERS, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        sendMessage(msg);
    }

    /**
     * Starts ethereum peer discovery
     *
     * Sends message parameters: none
     */
    public void startPeerDiscovery() {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_START_PEER_DISCOVERY, 0, 0);
        sendMessage(msg);
    }

    /**
     * Stops ethereum peer discovery
     *
     * Sends message parameters: none
     */
    public void stopPeerDiscovery() {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_LOAD_BLOCKS, 0, 0);
        sendMessage(msg);
    }

    /**
     * Gets the blockchain status
     * @param identifier String Caller identifier used to return the response
     *
     * Sends message parameters: none
     */
    public void getBlockchainStatus(String identifier) {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_GET_BLOCKCHAIN_STATUS, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        sendMessage(msg);
    }

    /**
     * Add ethereum event listener
     * @param identifier String Caller identifier used to return the response
     *
     * Sends message parameters ( "key": type [description] ):
     * {
     *      "flags": Serializable(EnumSet<ListenerFlag>) [sets flags to listen to specific events]
     * }
     */
    public void addListener(String identifier, EnumSet<EventFlag> flags) {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_ADD_LISTENER, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        Bundle data = new Bundle();
        data.putSerializable("flags", flags);
        msg.setData(data);
        sendMessage(msg);
    }

    /**
     * Remove ethereum event listener
     * @param identifier String Caller identifier used to return the response
     *
     * Sends message parameters: none
     */
    public void removeListener(String identifier) {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_REMOVE_LISTENER, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        sendMessage(msg);
    }


    /**
     * Closes ethereum
     *
     * Sends message parameters: none
     */
    public void closeEthereum() {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_CLOSE, 0, 0);
        sendMessage(msg);
    }

    /**
     * Get connection status
     * @param identifier String Caller identifier used to return the response
     *
     * Sends message parameters: none
     */
    public void getConnectionStatus(String identifier) {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_GET_CONNECTION_STATUS, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        sendMessage(msg);
    }

    /**
     * Submit ethereum transaction
     * @param identifier String Caller identifier used to return the response
     * @param transaction Transaction Transaction to submit
     *
     * Sends message parameters ( "key": type [description] ):
     * {
     *     "transaction": Parcelable(Transaction) [transaction to submit]
     * }
     */
    public void submitTransaction(String identifier, Transaction transaction) {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_SUBMIT_TRANSACTION, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        Bundle data = new Bundle();
        data.putParcelable("transaction", (org.ethereum.android.interop.Transaction)transaction);
        msg.setData(data);
        sendMessage(msg);
    }

    /**
     * Get admin info
     * @param identifier String Caller identifier used to return the response
     *
     * Sends message parameters: none
     */
    public void getAdminInfo(String identifier) {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_GET_ADMIN_INFO, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        sendMessage(msg);
    }

    /**
     * Get pending transactions
     * @param identifier String Caller identifier used to return the response
     *
     * Sends message parameters: none
     */
    public void getPendingTransactions(String identifier) {

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_GET_PENDING_TRANSACTIONS, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        sendMessage(msg);
    }

}
