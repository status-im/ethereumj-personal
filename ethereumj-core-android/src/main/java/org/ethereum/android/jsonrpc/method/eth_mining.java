package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;

/*
TODO: must be changed in app that implement mining
*/
public class eth_mining extends JsonRpcServerMethod {

    public eth_mining (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        Boolean tmp = false;
        JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
        return res;

    }
}