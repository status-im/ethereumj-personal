package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;

/*
TODO: right now -core not have "finished" mining architecture so not have hashrate
*/
public class eth_hashrate extends JsonRpcServerMethod {

    public eth_hashrate (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        String tmp = "0x" + Integer.toHexString(0);
        JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
        return res;

    }
}