package org.ethereum.android.service;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;

import org.ethereum.android.service.events.EventFlag;
import org.ethereum.core.Transaction;
import org.ethereum.net.peerdiscovery.PeerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


public class EthereumConnector extends ServiceConnector {

    private static final Logger logger = LoggerFactory.getLogger("EthereumConnector");

    public EthereumConnector(Context context, Class serviceClass) {

        super(context, serviceClass);
    }

    public void init(List<String> privateKeys) {

        if (!isBound) {
            System.out.println(" Not bound ???");
            return;
        }

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_INIT, 0, 0);
        Bundle data = new Bundle();
        data.putStringArrayList("privateKeys", (ArrayList) privateKeys);
        msg.setData(data);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(init) to service: " + e.getMessage());
        }
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

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_CONNECT, 0, 0);
        Bundle data = new Bundle();
        data.putString("ip", ip);
        data.putInt("port", port);
        data.putString("remoteId", remoteId);
        msg.setData(data);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(connect) to service: " + e.getMessage());
        }
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

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_LOAD_BLOCKS, 0, 0);
        Bundle data = new Bundle();
        data.putString("dumpFile", dumpFile);
        msg.setData(data);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(loadBlocks) to service: " + e.getMessage());
        }
    }

    /**
     * Start the json rpc server
     *
     * Sends message parameters: none
     */
    public void startJsonRpc() {

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_START_JSON_RPC_SERVER, 0, 0);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(startJsonRpc) to service: " + e.getMessage());
        }
    }

    /**
     * Change the json rpc server url
     *
     * Sends message parameters: ( "key": type [description] ):
     * {
     *     "rpc_server": String [Rpc server url]
     * }
     */
    public void changeJsonRpc(String serverUrl) {

        if (!isBound) {
            System.out.println("Connector is not bound.");
            return;
        }

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_CHANGE_JSON_RPC_SERVER, 0, 0);
        Bundle data = new Bundle();
        data.putString("rpc_server", serverUrl);
        msg.setData(data);
        try {
            serviceMessenger.send(msg);
            System.out.println("Sent change rpc server message");
        } catch (RemoteException e) {
            logger.error("Exception sending message(changeJsonRpc) to service: " + e.getMessage());
        }
    }

    /**
     * Find an online peer
     * @param identifier String Caller identifier used to return the response
     *
     * Sends message parameters: none
     */
    public void findOnlinePeer(String identifier) {

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_FIND_ONLINE_PEER, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(findOnlinePeer1) to service: " + e.getMessage());
        }
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

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_FIND_ONLINE_PEER, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        Bundle data = new Bundle();
        data.putParcelable("excludePeer", (org.ethereum.android.interop.PeerInfo) excludePeer);
        msg.setData(data);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(findOnlinePeer2) to service: " + e.getMessage());
        }
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

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_FIND_ONLINE_PEER, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        Bundle data = new Bundle();
        data.putParcelableArray("excludePeerSet", (org.ethereum.android.interop.PeerInfo[]) excludePeerSet);
        msg.setData(data);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(findOnlinePeer3) to service: " + e.getMessage());
        }
    }

    /**
     * Get etherum peers
     * @param identifier String Caller identifier used to return the response
     *
     * Sends message parameters: none
     */
    public void getPeers(String identifier) {

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_GET_PEERS, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(getPeers) to service: " + e.getMessage());
        }
    }

    /**
     * Starts ethereum peer discovery
     *
     * Sends message parameters: none
     */
    public void startPeerDiscovery() {

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_START_PEER_DISCOVERY, 0, 0);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(startPeerDiscovery) to service: " + e.getMessage());
        }
    }

    /**
     * Stops ethereum peer discovery
     *
     * Sends message parameters: none
     */
    public void stopPeerDiscovery() {

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_LOAD_BLOCKS, 0, 0);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(stopPeerDiscovery) to service: " + e.getMessage());
        }
    }

    protected Bundle getIdentifierBundle(String identifier) {

        Bundle bundle = new Bundle();
        bundle.putString("identifier", identifier);
        return bundle;
    }


    /**
     * Gets the blockchain status
     * @param identifier String Caller identifier used to return the response
     *
     * Sends message parameters: none
     */
    public void getBlockchainStatus(String identifier) {

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_GET_BLOCKCHAIN_STATUS, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(getBlockchainStatus) to service: " + e.getMessage());
        }
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

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_ADD_LISTENER, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        Bundle data = new Bundle();
        data.putSerializable("flags", flags);
        msg.setData(data);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(addListener) to service: " + e.getMessage());
        }
    }

    /**
     * Remove ethereum event listener
     * @param identifier String Caller identifier used to return the response
     *
     * Sends message parameters: none
     */
    public void removeListener(String identifier) {

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_REMOVE_LISTENER, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(addListener) to service: " + e.getMessage());
        }
    }


    /**
     * Closes ethereum
     *
     * Sends message parameters: none
     */
    public void closeEthereum() {

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_CLOSE, 0, 0);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(closeEthereum) to service: " + e.getMessage());
        }
    }

    /**
     * Get connection status
     * @param identifier String Caller identifier used to return the response
     *
     * Sends message parameters: none
     */
    public void getConnectionStatus(String identifier) {

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_GET_CONNECTION_STATUS, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(getConnectionStatus) to service: " + e.getMessage());
        }
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

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_SUBMIT_TRANSACTION, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        Bundle data = new Bundle();
        data.putParcelable("transaction", (org.ethereum.android.interop.Transaction)transaction);
        msg.setData(data);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(submitTransaction) to service: " + e.getMessage());
        }
    }

    /**
     * Get admin info
     * @param identifier String Caller identifier used to return the response
     *
     * Sends message parameters: none
     */
    public void getAdminInfo(String identifier) {

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_GET_ADMIN_INFO, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(getAdminInfo) to service: " + e.getMessage());
        }
    }

    /**
     * Get pending transactions
     * @param identifier String Caller identifier used to return the response
     *
     * Sends message parameters: none
     */
    public void getPendingTransactions(String identifier) {

        if (!isBound)
            return;

        Message msg = Message.obtain(null, EthereumServiceMessage.MSG_GET_PENDING_TRANSACTIONS, 0, 0);
        msg.replyTo = clientMessenger;
        msg.obj = getIdentifierBundle(identifier);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            logger.error("Exception sending message(getPendingTransactions) to service: " + e.getMessage());
        }
    }

}
