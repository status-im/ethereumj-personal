package org.ethereum.android_app;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.ethereum.android.EthereumManager;
import org.ethereum.android.interop.AdminInfo;
import org.ethereum.android.service.ConnectorHandler;
import org.ethereum.android.service.EthereumClientMessage;
import org.ethereum.android.service.events.EventFlag;
import org.ethereum.config.SystemProperties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.UUID;

import static org.ethereum.config.SystemProperties.CONFIG;

public class TestsFragment extends Fragment implements ConnectorHandler {

    TextView connectionStatus;
    TextView blockchainStatus;
    TextView startupTime;
    TextView isConsensus;
    TextView blockExecTime;

    Button connectButton;
    Button getEthereumStatus;
    Button getBlockchainStatus;

    String identifier = UUID.randomUUID().toString();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tests, container, false);

        connectionStatus = (TextView)view.findViewById(R.id.connectionStatus);
        blockchainStatus = (TextView)view.findViewById(R.id.blockchainStatus);
        startupTime = (TextView)view.findViewById(R.id.startupTime);
        isConsensus = (TextView)view.findViewById(R.id.isConsensus);
        blockExecTime = (TextView)view.findViewById(R.id.blockExecTime);
        connectButton = (Button)view.findViewById(R.id.connectButton);
        getEthereumStatus = (Button)view.findViewById(R.id.getEthereumStatus);
        getBlockchainStatus = (Button)view.findViewById(R.id.getBlockchainStatus);

        connectButton.setOnClickListener(onClickListener);
        getEthereumStatus.setOnClickListener(onClickListener);
        getBlockchainStatus.setOnClickListener(onClickListener);

        EthereumApplication.ethereum.registerHandler(this);

        return view;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            switch(v.getId()){
                case R.id.connectButton:
                    EthereumApplication.ethereum.connect("192.168.122.90", 30303, "aceb348f4fd7b9b5033b1703b724970d93dbc6ee8410bdc20bc0585e668d629e542cd8ec560311fc8f4a0851c914aae8945555adee73878063dfa0078cc03e07");
                    break;
                case R.id.getEthereumStatus:
                    EthereumApplication.ethereum.getConnectionStatus(identifier);
                    EthereumApplication.ethereum.getAdminInfo(identifier);
                    break;
                case R.id.getBlockchainStatus:
                    EthereumApplication.ethereum.getBlockchainStatus(identifier);
                    break;
            }
        }
    };

    protected void updateTextView(final TextView view, final String text) {

        view.post(new Runnable() {
            @Override
            public void run() {
                view.setText(text);
            }
        });
    }

    @Override
    public boolean handleMessage(final Message message) {

        boolean isClaimed = true;
        switch(message.what) {
            case EthereumClientMessage.MSG_CONNECTION_STATUS:
                updateTextView(connectionStatus, message.getData().getString("status"));
                break;
            case EthereumClientMessage.MSG_BLOCKCHAIN_STATUS:
                updateTextView(blockchainStatus, message.getData().getString("status"));
                break;
            case EthereumClientMessage.MSG_ADMIN_INFO:
                Bundle data = message.getData();
                data.setClassLoader(AdminInfo.class.getClassLoader());
                AdminInfo adminInfo = data.getParcelable("adminInfo");
                updateTextView(startupTime, new SimpleDateFormat("yyyy MM dd HH:mm:ss").format(new Date(adminInfo.getStartupTimeStamp())));
                updateTextView(isConsensus, adminInfo.isConsensus() ? "true" : "false");
                updateTextView(blockExecTime, adminInfo.getExecAvg().toString());
                break;
            case EthereumClientMessage.MSG_ONLINE_PEER:
                break;
            case EthereumClientMessage.MSG_PEERS:
                break;
            case EthereumClientMessage.MSG_PENDING_TRANSACTIONS:
                break;
            case EthereumClientMessage.MSG_SUBMIT_TRANSACTION_RESULT:
                break;
            default:
                isClaimed = false;
        }
        return isClaimed;
    }

    @Override
    public String getID() {

        return identifier;
    }

    @Override
    public void onConnectorConnected() {

        EthereumApplication.ethereum.addListener(identifier, EnumSet.allOf(EventFlag.class));
        //EthereumApplication.ethereum.connect(SystemProperties.CONFIG.activePeerIP(), SystemProperties.CONFIG.activePeerPort(), SystemProperties.CONFIG.activePeerNodeid());
    }

    @Override
    public void onConnectorDisconnected() {

    }

}