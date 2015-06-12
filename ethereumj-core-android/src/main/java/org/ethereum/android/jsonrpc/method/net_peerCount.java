package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;
import org.ethereum.net.peerdiscovery.PeerInfo;
import java.util.Set;

public class net_peerCount extends JsonRpcServerMethod {

    public net_peerCount (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        int pc = 0;
        final Set<PeerInfo> peers = ethereum.getPeers();
        synchronized (peers) {
            for (PeerInfo peer : peers) {
                if (peer.isOnline())
                    pc++;
            }
        }
        String tmp = "0x" + Integer.toHexString(pc);
        JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
        return res;

    }
}