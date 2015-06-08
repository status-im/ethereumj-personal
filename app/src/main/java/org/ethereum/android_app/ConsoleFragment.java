package org.ethereum.android_app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ethereum.android.EthereumManager;
import org.ethereum.listener.EthereumListenerAdapter;

public class ConsoleFragment extends Fragment {

    EthereumManager ethereumManager;
    private TextView console;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_console, container, false);
        console = (TextView) view.findViewById(R.id.console);
        console.setMovementMethod(new ScrollingMovementMethod());
        return view;

    }

    private void appendTextAndScroll(String text) {
        if (console != null) {
            console.append(text + "\n");
            final Layout layout = console.getLayout();
            if (layout != null) {
                int scrollDelta = layout.getLineBottom(console.getLineCount() - 1) - console.getScrollY() - console.getHeight();
                if (scrollDelta > 0)
                    console.scrollBy(0, scrollDelta);
            }
        }
    }

    public void setEthereumManager(EthereumManager ethereumManager) {

        this.ethereumManager = ethereumManager;
        ethereumManager.addListener(new EthereumListenerAdapter() {

            @Override
            public void trace(final String output) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        appendTextAndScroll(output);
                    }
                });

            }
        });
    }

}