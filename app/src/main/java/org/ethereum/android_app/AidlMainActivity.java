package org.ethereum.android_app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.ethereum.android.interop.IEthereumService;
import org.ethereum.android.interop.IListener;
import org.ethereum.config.SystemProperties;
import org.ethereum.net.rlpx.Node;

import java.io.File;
import java.util.ArrayList;


public class AidlMainActivity extends ActionBarActivity implements ActivityInterface {

    private static final String TAG = "MyActivity";

    private Toolbar toolbar;
    private ViewPager viewPager;
    private SlidingTabLayout tabs;
    private TabsPagerAdapter adapter;
    protected ArrayList<FragmentInterface> fragments = new ArrayList<>();

    protected static String consoleLog = "";

    /** Ethereum Aidl Service. */
    IEthereumService ethereumService = null;

    IListener.Stub ethereumListener = new IListener.Stub() {

        public void trace(String message) throws RemoteException {

            logMessage(message);
        }
    };

    /** Flag indicating whether we have called bind on the service. */
    boolean isBound;

    /**
     * Class for interacting with the main interface of the service.
     */
    protected ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {

            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            ethereumService = IEthereumService.Stub.asInterface(service);
            Toast.makeText(AidlMainActivity.this, "service attached", Toast.LENGTH_SHORT).show();

            try {


                // Try to load blocks dump file from /sdcard/poc-9-492k.dmp
                String blocksDump = null;
                File extStore = Environment.getExternalStorageDirectory();
                if (extStore.exists()) {
                    String sdcardPath = extStore.getAbsolutePath();
                    File dumpFile = new File(extStore, "poc-9-492k.dmp");
                    if (dumpFile.exists()) {
                        blocksDump = dumpFile.getAbsolutePath();
                    }
                }
                // Start json rpc server
                ethereumService.startJsonRpcServer();
                // If blocks dump not found, connect to peer
                if (blocksDump != null) {
                    ethereumService.loadBlocks(blocksDump);
                } else {
                    ethereumService.addListener(ethereumListener);
                    Node node = SystemProperties.CONFIG.peerActive().get(0);
                    ethereumService.connect(node.getHost(), node.getPort(), node.getHexId());
                }
                Toast.makeText(AidlMainActivity.this, "connected to service", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                logMessage("Error adding listener: " + e.getMessage());
            }
        }

        public void onServiceDisconnected(ComponentName className) {

            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            ethereumService = null;
            Toast.makeText(AidlMainActivity.this, "service disconnected", Toast.LENGTH_SHORT).show();
        }
    };

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

        ComponentName myService = startService(new Intent(AidlMainActivity.this, EthereumService.class));
        doBindService();

        //StrictMode.enableDefaults();
    }

    protected void logMessage(String message) {

        AidlMainActivity.consoleLog += message + "\n";
        int consoleLength = AidlMainActivity.consoleLog.length();
        if (consoleLength > 5000) {
            AidlMainActivity.consoleLog = AidlMainActivity.consoleLog.substring(4000);
        }

        broadcastFragments(AidlMainActivity.consoleLog);
    }


    protected void broadcastFragments(String message) {

        for (FragmentInterface fragment : fragments) {
            fragment.onMessage(message);
        }
    }

    void doBindService() {

        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(AidlMainActivity.this, EthereumService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
        Toast.makeText(AidlMainActivity.this, "binding to service", Toast.LENGTH_SHORT).show();
    }

    void doUnbindService() {

        if (isBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (ethereumService != null) {
                try {
                    ethereumService.removeListener(ethereumListener);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(serviceConnection);
            isBound = false;
            Toast.makeText(AidlMainActivity.this, "unbinding from service", Toast.LENGTH_SHORT).show();
        }
    }

    public void registerFragment(FragmentInterface fragment) {

        if (!fragments.contains(fragment)) {
            fragments.add(fragment);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.v(TAG, Integer.valueOf(id).toString());
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        doUnbindService();
    }





}
