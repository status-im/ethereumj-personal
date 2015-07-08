package org.ethereum.android.interop;

import org.ethereum.android.interop.IListener;
import org.ethereum.android.interop.IAsyncCallback;

oneway interface IEthereumService {

    void loadBlocks(String dumpFile);
    void connect(String ip, int port, String remoteId);
    void startJsonRpcServer();
    void addListener(IListener listener);
    void removeListener(IListener listener);
    void getLog(IAsyncCallback callback);
}
