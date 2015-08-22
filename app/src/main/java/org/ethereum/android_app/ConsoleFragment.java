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


public class ConsoleFragment extends Fragment implements FragmentInterface {

    private TextView console;

    private final static int CONSOLE_LENGTH = 10000;
    private final static int CONSOLE_REFRESH_MILLS = 1000 * 5; //5 sec

    private Handler handler = new Handler();


    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            console.setText(EthereumApplication.log);
            handler.postDelayed(mRunnable, CONSOLE_REFRESH_MILLS);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(mRunnable);
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

    @Override
    public void onMessage(String message) {
        System.out.println(message);
    }
}