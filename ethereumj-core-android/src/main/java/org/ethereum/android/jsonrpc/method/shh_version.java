package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;
import org.ethereum.net.shh.ShhHandler;

public class shh_version extends JsonRpcServerMethod {

    public shh_version (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        String tmp = "" + ShhHandler.VERSION;
        JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
        return res;

    }
}