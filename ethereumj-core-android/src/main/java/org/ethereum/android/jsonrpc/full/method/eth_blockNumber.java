package org.ethereum.android.jsonrpc.full.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.full.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;

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