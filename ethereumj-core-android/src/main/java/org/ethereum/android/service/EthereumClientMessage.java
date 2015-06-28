package org.ethereum.android.service;

public class EthereumClientMessage {

    /**
     * Send online peer to the client ("peerInfo" => PeerInfo)
     */
    public static final int MSG_ONLINE_PEER = 1;

    /**
     * Send peers to the client ("peers" => PeerInfo[])
     */
    public static final int MSG_PEERS = 2;

    /**
     * Send blockchain status to the client ("status" => "Loaded/Loading")
     */
    public static final int MSG_BLOCKCHAIN_STATUS = 3;

    /**
     * Send ethereum connection status to the client
     */
    public static final int MSG_CONNECTION_STATUS = 4;

    /**
     * Send submitted transaction to client
     */
    public static final int MSG_SUBMIT_TRANSACTION_RESULT = 5;

    /**
     * Send admin info to client
     */
    public static final int MSG_ADMIN_INFO = 6;

    /**
     * Send peers to the client
     */
    public static final int MSG_PENDING_TRANSACTIONS = 7;

    /**
     * Send event to the client
     */
    public static final int MSG_EVENT = 8;
}
