package org.ethereum.android_app;

import org.ethereum.android.EthereumAidlService;
import org.ethereum.android.interop.IListener;

public class EthereumService extends EthereumAidlService {

    public EthereumService() {

    }

    @Override
    protected void broadcastMessage(String message) {

        updateLog(message);
        for (IListener listener: clientListeners) {
            try {
                listener.trace(message);
            } catch (Exception e) {
                // Remove listener
                clientListeners.remove(listener);
            }
        }
    }

    private void updateLog(String message) {

        EthereumService.log += message;
        int logLength = EthereumService.log.length();
        if (logLength > 5000) {
            EthereumService.log = EthereumService.log.substring(2500);
        }
    }

}
