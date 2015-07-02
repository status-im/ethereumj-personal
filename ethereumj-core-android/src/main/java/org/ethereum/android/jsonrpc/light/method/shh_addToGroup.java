package org.ethereum.android.jsonrpc.light.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.light.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;

import java.util.List;

/*
TODO: not present clear specification for this method, also cpp and go version not provide it.
*/
public class shh_addToGroup extends JsonRpcServerMethod {

    public shh_addToGroup(Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 1) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            byte[] identity = jsToAddress((String)params.get(0));

            JSONRPC2Response res = new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            return res;
        }

    }
}