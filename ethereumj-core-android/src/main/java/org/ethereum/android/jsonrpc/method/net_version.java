package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;

/*
TODO: request go version how this must be identified. Cpp version just return "".
*/
public class net_version extends JsonRpcServerMethod {

    public net_version (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {
        String tmp = "";
        JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
        return res;
    }

}