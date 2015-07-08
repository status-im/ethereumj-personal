package org.ethereum.android.service;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import org.ethereum.android.jsonrpc.JsonRpcServer;
import org.ethereum.android.manager.BlockLoader;
import org.ethereum.android.interop.*;

import java.util.ArrayList;

public class EthereumAidlService extends EthereumService {

    protected ArrayList<IListener> clientListeners = new ArrayList<>();

    public static String log = "";

    IEthereumService.Stub binder = null;

    public EthereumAidlService() {

        initializeBinder();
    }

    protected void initializeBinder() {

        binder = new IEthereumService.Stub() {

            public void loadBlocks(String dumpFile) throws RemoteException {

                BlockLoader blockLoader = (BlockLoader)ethereum.getBlockLoader();
                blockLoader.loadBlocks(dumpFile);
            }

            public void connect(String ip, int port, String remoteId) throws RemoteException {

                if (!isConnected) {
                    System.out.println("Connecting to : " + ip);
                    ethereum.connect(ip, port, remoteId);
                    isConnected = true;
                } else {
                    System.out.println("Already connected");
                    System.out.println("x " + ethereum.isConnected());
                }
            }

            public void addListener(IListener listener) throws RemoteException {

                clientListeners.clear();
                clientListeners.add(listener);
            }

            public void removeListener(IListener listener) throws RemoteException {

                try {
                    clientListeners.remove(listener);
                } catch (Exception e) {
                    System.out.println("ERRORRRR: " + e.getMessage());
                }
            }

            public void startJsonRpcServer() throws RemoteException {

                //TODO: add here switch between full and light version
                jsonRpcServer = new org.ethereum.android.jsonrpc.light.JsonRpcServer(ethereum);
            }

            public void getLog(IAsyncCallback callback) throws  RemoteException {

                callback.handleResponse(EthereumAidlService.log);
            }
        };
    }

    protected void broadcastMessage(String message) {

        for (IListener listener: clientListeners) {
            try {
                listener.trace(message);
            } catch (Exception e) {
                // Remove listener
                System.out.println("ERRORRRR: " + e.getMessage());
                clientListeners.remove(listener);
            }
        }
    }

    @Override
    public void onCreate() {

        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return binder;
    }

}
