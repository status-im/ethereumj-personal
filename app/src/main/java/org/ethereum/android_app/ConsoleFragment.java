package org.ethereum.android_app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ethereum.android.EthereumManager;
import org.ethereum.listener.EthereumListenerAdapter;

public class ConsoleFragment extends Fragment {

    private final static int CONSOLE_LENGTH = 10000;
    private final static int CONSOLE_REFRESH_MILLS = 1000 * 5; //5 sec

    private TextView consoleText;

    TextViewUpdater consoleUpdater = new TextViewUpdater();
    private Handler handler = new Handler();

    private class TextViewUpdater implements Runnable {

        private String txt;
        @Override
        public void run() {

            int length = EthereumApplication.consoleLog.length();
            if (length > CONSOLE_LENGTH) {
                EthereumApplication.consoleLog = EthereumApplication.consoleLog.substring(CONSOLE_LENGTH * ((length / CONSOLE_LENGTH) - 1) + length % CONSOLE_LENGTH);
            }
            consoleText.setText(EthereumApplication.consoleLog);

            handler.postDelayed(consoleUpdater, CONSOLE_REFRESH_MILLS);
        }
        public void setText(String txt) {

            this.txt = txt;
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_console, container, false);
        consoleText = (TextView) view.findViewById(R.id.console);
        consoleText.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(consoleUpdater);
    }
}