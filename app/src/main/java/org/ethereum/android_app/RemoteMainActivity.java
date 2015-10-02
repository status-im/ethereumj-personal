package org.ethereum.android_app;

import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.ethereum.android.service.ConnectorHandler;
import org.ethereum.android.service.EthereumClientMessage;
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


public class RemoteMainActivity extends ActionBarActivity implements ActivityInterface, ConnectorHandler {

    private Toolbar toolbar;
    private ViewPager viewPager;
    private SlidingTabLayout tabs;
    private TabsPagerAdapter adapter;
    protected ArrayList<FragmentInterface> fragments = new ArrayList<>();
    protected String handlerIdentifier = UUID.randomUUID().toString();
    static DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        adapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(adapter);;

        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true);
        tabs.setViewPager(viewPager);
        EthereumApplication app = (EthereumApplication)getApplication();
        app.ethereum.registerHandler(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void registerFragment(FragmentInterface fragment) {

        if (!fragments.contains(fragment)) {
            fragments.add(fragment);
        }
    }

    @Override
    public boolean handleMessage(Message message) {

        boolean isClaimed = true;
        switch(message.what) {
            case EthereumClientMessage.MSG_EVENT:
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
                break;
            default:
                isClaimed = false;
        }
        return isClaimed;
    }

    protected void addLogEntry(long timestamp, String message) {

        Date date = new Date(timestamp);

        EthereumApplication app = (EthereumApplication)getApplication();
        app.log += formatter.format(date) + " -> " + message + "\n\n";
        if (app.log.length() > 10000) {
            app.log = app.log.substring(5000);
        }
        for(FragmentInterface fragment: fragments) {
            fragment.onMessage(app.log);
        }
    }

    @Override
    public String getID() {

        return handlerIdentifier;
    }

    @Override
    public void onConnectorConnected() {

        EthereumApplication app = (EthereumApplication)getApplication();
        app.ethereum.addListener(handlerIdentifier, EnumSet.allOf(EventFlag.class));
        //Node node = SystemProperties.CONFIG.peerActive().get(0);
        //app.ethereum.connect(node.getHost(), node.getPort(), node.getHexId());
        app.ethereum.startJsonRpc();
    }

    @Override
    public void onConnectorDisconnected() {

    }
}
