package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;

/*
TODO: right now -core not mark fact of start listening, it do it automatically and only send Listening trace "string" message.
*/
public class net_listening extends JsonRpcServerMethod {

    public net_listening (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        Boolean tmp = true;
        JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
        return res;

    }
}