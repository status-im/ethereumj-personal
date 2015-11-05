package org.ethereum.android_app;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.support.multidex.MultiDexApplication;

import org.ethereum.android.service.ConnectorHandler;
import org.ethereum.android.service.EthereumClientMessage;
import org.ethereum.android.service.EthereumConnector;
import org.ethereum.android.service.events.BlockEventData;
import org.ethereum.android.service.events.EventData;
import org.ethereum.android.service.events.EventFlag;
import org.ethereum.android.service.events.MessageEventData;
import org.ethereum.android.service.events.PeerDisconnectEventData;
import org.ethereum.android.service.events.PendingTransactionsEventData;
import org.ethereum.android.service.events.TraceEventData;
import org.ethereum.android.service.events.VMTraceCreatedEventData;
import org.ethereum.config.SystemProperties;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.UUID;

public class EthereumApplication extends MultiDexApplication implements ConnectorHandler {

    public static EthereumConnector ethereumConnector = null;
    public static EthereumApplication instance;

    public static String consoleLog = "";

    private String handlerIdentifier = UUID.randomUUID().toString();

    @SuppressLint("SimpleDateFormat")
    private DateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss:SSS");



    public boolean isEthereumConnected = false;

    public EthereumApplication() {

        instance = this;
    }

    @Override public void onCreate() {

        super.onCreate();
        if (ethereumConnector == null) {
            ethereumConnector = new EthereumConnector(this, EthereumRemoteService.class);
            ethereumConnector.registerHandler(this);
            ethereumConnector.bindService();
        }
    }

    @Override
    public void onTerminate() {

        super.onTerminate();
        System.out.println("Terminating application");
        ethereumConnector.removeHandler(this);
        ethereumConnector.unbindService();
        ethereumConnector = null;
    }

    private ArrayList<String> getDefaultAccounts() {

        ArrayList<String> accounts = new ArrayList<String>();

        return null;
    }

    @Override
    public void onConnectorConnected() {
        System.out.println("Connector connected");
        isEthereumConnected = true;
        ethereumConnector.addListener(handlerIdentifier, EnumSet.allOf(EventFlag.class));
        ethereumConnector.init(getDefaultAccounts());
        //ethereumConnector.startJsonRpc();
        Node node = SystemProperties.CONFIG.peerActive().get(0);
        ethereumConnector.connect(node.getHost(), node.getPort(), node.getHexId());
    }

    @Override
    public void onConnectorDisconnected() {
        System.out.println("Connector Disconnected");
        isEthereumConnected = false;
    }

    @Override
    public String getID() {
        return handlerIdentifier;
    }

    private void addLogEntry(long timestanp, String message) {
        Date date = new Date(timestanp);
        consoleLog += dateFormatter.format(date) + " -> " + message + "\n";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean handleMessage(Message message) {

        boolean isClaimed = true;
        switch (message.what) {
            case EthereumClientMessage.MSG_EVENT:
                Bundle data = message.getData();
                data.setClassLoader(EventFlag.class.getClassLoader());
                EventFlag event = (EventFlag) data.getSerializable("event");
                if (event == null)
                    return false;
                EventData eventData;
                MessageEventData messageEventData;
                switch (event) {
                    case EVENT_BLOCK:
                        BlockEventData blockEventData = data.getParcelable("data");
                        addLogEntry(blockEventData.registeredTime, "Added block with " + blockEventData.receipts.size() + " transaction receipts.");
                        break;
                    case EVENT_HANDSHAKE_PEER:
                        messageEventData = data.getParcelable("data");
                        addLogEntry(messageEventData.registeredTime, "Peer " + new HelloMessage(messageEventData.message).getPeerId() + " said hello");
                        break;
                    case EVENT_NO_CONNECTIONS:
                        eventData = data.getParcelable("data");
                        addLogEntry(eventData.registeredTime, "No connections");
                        break;
                    case EVENT_PEER_DISCONNECT:
                        PeerDisconnectEventData peerDisconnectEventData = data.getParcelable("data");
                        addLogEntry(peerDisconnectEventData.registeredTime, "Peer " + peerDisconnectEventData.host + ":" + peerDisconnectEventData.port + " disconnected.");
                        break;
                    case EVENT_PENDING_TRANSACTIONS_RECEIVED:
                        PendingTransactionsEventData pendingTransactionsEventData = data.getParcelable("data");
                        addLogEntry(pendingTransactionsEventData.registeredTime, "Received " + pendingTransactionsEventData.transactions.size() + " pending transactions");
                        break;
                    case EVENT_RECEIVE_MESSAGE:
                        messageEventData = data.getParcelable("data");
                        addLogEntry(messageEventData.registeredTime, "Received message: " + messageEventData.messageClass.getName());
                        break;
                    case EVENT_SEND_MESSAGE:
                        messageEventData = data.getParcelable("data");
                        addLogEntry(messageEventData.registeredTime, "Sent message: " + messageEventData.messageClass.getName());
                        break;
                    case EVENT_SYNC_DONE:
                        eventData = data.getParcelable("data");
                        addLogEntry(eventData.registeredTime, "Sync done");
                        break;
                    case EVENT_VM_TRACE_CREATED:
                        VMTraceCreatedEventData vmTraceCreatedEventData = data.getParcelable("data");
                        addLogEntry(vmTraceCreatedEventData.registeredTime, "CM trace created: " + vmTraceCreatedEventData.transactionHash + " - " + vmTraceCreatedEventData.trace);
                        break;
                    case EVENT_TRACE:
                        TraceEventData traceEventData = data.getParcelable("data");
                        System.out.println("We got a trace message: " + traceEventData.message);
                        addLogEntry(traceEventData.registeredTime, traceEventData.message);
                        break;
                }
                break;
            default:
                isClaimed = false;
        }
        return isClaimed;
    }
}