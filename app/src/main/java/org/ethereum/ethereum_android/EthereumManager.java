package org.ethereum.ethereum_android;

import org.ethereum.config.SystemProperties;
import org.ethereum.core.AccountState;
import org.ethereum.facade.Ethereum;
import org.ethereum.EthereumFactory;
import org.ethereum.facade.Repository;
import org.ethereum.listener.EthereumListenerAdapter;


import java.util.Set;

public class EthereumManager {

    public static Ethereum ethereum = null;

    public static AccountsDataAdapter adapter = null;

    public static String log = "";

    public EthereumManager(android.content.Context androidContext) {

        ethereum = EthereumFactory.getEthereum(androidContext);
        this.addListener();
    }

    public void connect() {

        ethereum.connect(SystemProperties.CONFIG.activePeerIP(),
                SystemProperties.CONFIG.activePeerPort(),
                SystemProperties.CONFIG.activePeerNodeid());
    }

    public void loadAccounts() {

        Repository repository = ethereum.getRepository();
        Set<byte[]> keys = repository.getAccountsKeys();
        for (byte[] key : keys) {
            AccountsDataAdapter.DataClass dc = new AccountsDataAdapter.DataClass();
            dc.address = key;
            AccountState state = repository.getAccountState(dc.address);
            dc.accountState = state;

            adapter.addDataPiece(dc);
        }
    }

    public void startPeerDiscovery() {

        ethereum.startPeerDiscovery();
    }

    public void addListener() {

        ethereum.addListener(new EthereumListenerAdapter() {

            @Override
            public void trace(final String output) {

                log += output;
                log += "\n\n";
            }
        });
    }

    public String getLog() {

        String logMessages = EthereumManager.log;
        EthereumManager.log = "";
        return logMessages;
    }

}
