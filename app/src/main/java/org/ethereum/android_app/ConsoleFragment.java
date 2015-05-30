package org.ethereum.android_app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ethereum.listener.EthereumListenerAdapter;

public class ConsoleFragment extends Fragment {

    EthereumManager ethereumManager;
    private TextView console;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_console, container, false);
        console = (TextView) view.findViewById(R.id.console);
        console.setMovementMethod(new ScrollingMovementMethod());
        console.append("aaaa");
        return view;

    }

    public void setEthereumManager(EthereumManager ethereumManager) {

        this.ethereumManager = ethereumManager;
        ethereumManager.addListener(new EthereumListenerAdapter() {

            @Override
            public void trace(final String output) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        console.append(output);
                    }
                });

            }
        });
    }

}