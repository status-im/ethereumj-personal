package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;

/*
No matter how long I wait on synchronization - all time got best block number = 0
TODO: check this after Adrian finish db implementation.
*/
public class eth_blockNumber extends JsonRpcServerMethod {

    public eth_blockNumber (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        String tmp = "0x" + Long.toHexString(ethereum.getBlockchain().getBestBlock().getNumber());
        JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
        return res;

    }
}