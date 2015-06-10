package org.ethereum.android_app;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Build;

import org.ethereum.android.EthereumManager;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MyActivity";
    private static Integer quit = 0;

    private Toolbar toolbar;
    private ViewPager viewPager;
    private SlidingTabLayout tabs;
    private TabsPagerAdapter adapter;

    public EthereumManager ethereumManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        ethereumManager = new EthereumManager(this);

        adapter = new TabsPagerAdapter(getSupportFragmentManager(), ethereumManager);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(adapter);;

        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true);
        tabs.setViewPager(viewPager);

        //StrictMode.enableDefaults();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new PostTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new PostTask().execute();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new JsonRpcTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new JsonRpcTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
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
        ethereumManager.onDestroy();
    }

    // The definition of our task class
    private class PostTask extends AsyncTask<Context, Integer, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Context... params) {
            Log.v(TAG, "111");

            Log.v(TAG, "222");
            ethereumManager.connect();
            Log.v(TAG, "333");
            while(true) {

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (quit == 1) {
                    Log.v(TAG, "Ending background process.");
                    return "All Done!";
                }

                //publishProgress(1111);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.v(TAG, values[0].toString());
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }


    // The definition of our task class
    private class JsonRpcTask extends AsyncTask<Context, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Context... params) {
            Log.v(TAG, "444");
            try {
                ethereumManager.startJsonRpc();
            } catch (Exception e) {
            }
            Log.v(TAG, "555");
            return "done";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.v(TAG, values[0].toString());
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }
}
