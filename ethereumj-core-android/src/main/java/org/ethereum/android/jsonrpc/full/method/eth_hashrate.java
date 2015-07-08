package org.ethereum.android.jsonrpc.full.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.full.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;

/*
TODO: must be changed in app that implement mining
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