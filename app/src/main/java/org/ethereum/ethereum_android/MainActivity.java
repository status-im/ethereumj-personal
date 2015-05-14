package org.ethereum.ethereum_android;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

import org.springframework.context.ApplicationContext;


public class MainActivity extends ActionBarActivity implements OnClickListener, NavigationDrawerCallbacks {

    public static ApplicationContext context = null;
    private static final String TAG = "MyActivity";
    private static Integer quit = 0;
    private TextView text1;
    private Button consoleButton;
    private Button walletButton;

    public EthereumManager ethereumManager = null;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.fragment_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);

        text1 = (TextView) findViewById(R.id.text1);
        text1.setMovementMethod(new ScrollingMovementMethod());

        StrictMode.enableDefaults();

        System.setProperty("sun.arch.data.model", "32");
        System.setProperty("leveldb.mmap", "false");
        new PostTask().execute(getApplicationContext());

        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (ethereumManager != null) {
                                    text1.append(ethereumManager.getLog());
                                }
                            }
                        });
                    }
                    quit = 1;
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            /*
            case  R.id.consoleButton: {
                // do something for button 1 click
                break;
            }
            */

            //.... etc
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Toast.makeText(this, "Menu item selected -> " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.v(TAG, new Integer(id).toString());
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // The definition of our task class
    private class PostTask extends AsyncTask<android.content.Context, Integer, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //displayProgressBar("Downloading...");
        }

        @Override
        protected String doInBackground(android.content.Context... params) {
            android.content.Context context=params[0];
            Log.v(TAG, "111");
            ethereumManager = new EthereumManager(context);
            Log.v(TAG, "222");
            ethereumManager.connect();
            Log.v(TAG, "333");
            while(true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (quit == 1) {
                    return "All Done!";
                }
                //publishProgress(1111);
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
           // updateProgressBar(values[0]);
            Log.v(TAG, values[0].toString());
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //dismissProgressBar();
        }
    }
}
