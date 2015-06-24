package org.ethereum.android_app;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ethereum.android.EthereumManager;
import org.ethereum.listener.EthereumListenerAdapter;

public class ConsoleFragment extends Fragment implements FragmentInterface {

    EthereumManager ethereumManager;
    private TextView console;

    TextViewUpdater consoleUpdater = new TextViewUpdater();

    private class TextViewUpdater implements Runnable {

        private String txt;
        @Override
        public void run() {

            console.setText(txt);
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
        ActivityInterface activityInterface = (ActivityInterface) activity;
        activityInterface.registerFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_console, container, false);
        console = (TextView) view.findViewById(R.id.console);
        console.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }

    public void onMessage(String message) {

        consoleUpdater.setText(message);
        ConsoleFragment.this.console.post(consoleUpdater);
    }
}