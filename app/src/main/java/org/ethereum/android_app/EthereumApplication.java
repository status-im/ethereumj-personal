package org.ethereum.android_app;


import android.os.AsyncTask;
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
import org.ethereum.net.p2p.HelloMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.UUID;

public class EthereumApplication extends MultiDexApplication implements ConnectorHandler {

    private final static int CONSOLE_LENGTH = 10000;
    public static EthereumConnector ethereum = null;
    public static String log = "";
    public static String identifier = UUID.randomUUID().toString();
    static DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");

    @Override public void onCreate() {

        super.onCreate();
        if (ethereum == null) {
            ethereum = new EthereumConnector(this, EthereumService.class);
        }
        ethereum.registerHandler(this);
        ethereum.bindService();
    }

    @Override
    public void onTerminate() {

        super.onTerminate();
        ethereum.removeHandler(this);
        ethereum.removeListener(identifier);
        ethereum.unbindService();
        ethereum = null;
    }

    @Override
    public void onConnectorConnected() {

        System.out.println("Connector connected");
        ethereum.addListener(identifier, EnumSet.allOf(EventFlag.class));
    }

    @Override
    public void onConnectorDisconnected() {


    }

    @Override
    public String getID() {
        return identifier;
    }

    protected class MessageProcessTask extends AsyncTask<Void, Void, Void> {

        Message message = null;

        public MessageProcessTask(Message message) {

            this.message = message;
        }

        protected Void doInBackground(Void... args) {

            processMessage(message);
            return null;
        }

        protected void onPostExecute(Void results) {


        }
    }

    protected void processMessage(Message message) {

        Bundle data = message.getData();
        data.setClassLoader(EventFlag.class.getClassLoader());
        EventFlag event = (EventFlag)data.getSerializable("event");
        EventData eventData;
        MessageEventData messageEventData;
        switch(event) {
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
                addLogEntry(traceEventData.registeredTime, traceEventData.message);
                break;
        }
    }

    @Override
    public boolean handleMessage(final Message message) {

        boolean isClaimed = true;
        switch(message.what) {
            case EthereumClientMessage.MSG_EVENT:
                Message messageCopy = new Message();
                messageCopy.copyFrom(message);
                new MessageProcessTask(messageCopy).execute();
                break;
            default:
                isClaimed = false;
        }
        return isClaimed;
    }

    protected void addLogEntry(long timestamp, String message) {

        Date date = new Date(timestamp);

        String logEntry = formatter.format(date) + " -> " + message + "\n\n";
        EthereumApplication.log += logEntry;
        if (EthereumApplication.log.length() > CONSOLE_LENGTH) {
            EthereumApplication.log = EthereumApplication.log.substring(CONSOLE_LENGTH);
        }
    }
}