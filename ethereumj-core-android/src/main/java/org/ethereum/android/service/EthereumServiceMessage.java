package org.ethereum.android.service;


public class EthereumServiceMessage {

    /**
     * Command to the service to connect to a peer
     */
    public static final int MSG_CONNECT = 1;

    /**
     * Command to the service to load blocks dumpr
     */
    public static final int MSG_LOAD_BLOCKS = 2;

    /**
     * Command to the service to start json rpc server
     */
    public static final int MSG_START_JSON_RPC_SERVER = 3;

    /**
     * Command to the service to find an online peer
     */
    public static final int MSG_FIND_ONLINE_PEER = 4;

    /**
     * Command to the service to list the peers
     */
    public static final int MSG_GET_PEERS = 5;

    /**
     * Command to the service to start peer discovery
     */
    public static final int MSG_START_PEER_DISCOVERY = 6;

    /**
     * Command to the service to stop peer discovery
     */
    public static final int MSG_STOP_PEER_DISCOVERY = 7;

    /**
     * Command to the service to get blockchain status (Loading/Loaded)
     */
    public static final int MSG_GET_BLOCKCHAIN_STATUS = 8;

    /**
     * Command to the service to add a listener
     */
    public static final int MSG_ADD_LISTENER = 9;

    /**
     * Command to the service to remove a listener
     */
    public static final int MSG_REMOVE_LISTENER = 10;

    /**
     * Command to the service to get connection status (Connected/Not Connected)
     */
    public static final int MSG_GET_CONNECTION_STATUS = 11;

    /**
     * Command to the service to close
     */
    public static final int MSG_CLOSE = 12;

    /**
     * Command to the service to submit a transaction
     */
    public static final int MSG_SUBMIT_TRANSACTION = 13;

    /**
     * Command to the service to get the admin info
     */
    public static final int MSG_GET_ADMIN_INFO = 14;

    /**
     * Command to the service to get the pernding transactions
     */
    public static final int MSG_GET_PENDING_TRANSACTIONS = 15;

    /**
     * Command to the service to initialize with specific addresses. If already initialized, restart the service
     */
    public static final int MSG_INIT = 16;
}
