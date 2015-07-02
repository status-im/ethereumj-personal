package org.ethereum.android.jsonrpc.light.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.light.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;
import org.ethereum.core.*;
import org.spongycastle.util.encoders.Hex;

public class eth_coinbase extends JsonRpcServerMethod {

    public eth_coinbase (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        String tmp = "0x" + getCoinBase();
        JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
        return res;

    }
}