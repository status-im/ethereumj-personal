package org.ethereum.android.service.events;

public enum EventFlag {

    /**
     * Listen to all events
     */
    EVENT_ALL,
    
    /**
     * Trace messages event
     */
    EVENT_TRACE,

    /**
     * onBlock event
     */
    EVENT_BLOCK,

    /**
     * Received message event
     */
    EVENT_RECEIVE_MESSAGE,

    /**
     * Send message event
     */
    EVENT_SEND_MESSAGE,

    /**
     * Peer disconnect event
     */
    EVENT_PEER_DISCONNECT,

    /**
     * Pending transactions received event
     */
    EVENT_PENDING_TRANSACTIONS_RECEIVED,

    /**
     * Sync done event
     */
    EVENT_SYNC_DONE,

    /**
     * No connections event
     */
    EVENT_NO_CONNECTIONS,

    /**
     * Peer handshake event
     */
    EVENT_HANDSHAKE_PEER,

    /**
     * VM trace created event
     */
    EVENT_VM_TRACE_CREATED,

    EVENT_ETHEREUM_CREATED

}
