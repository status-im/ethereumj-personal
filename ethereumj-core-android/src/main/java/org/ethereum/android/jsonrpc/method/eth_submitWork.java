package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.android.jsonrpc.filter.FilterManager;
import org.ethereum.facade.Ethereum;

import java.util.List;

/*
TODO: right now -core not auto start mining so no way to get information about state
*/
public class eth_submitWork extends JsonRpcServerMethod {

    public eth_submitWork (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 3) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            byte[] nonce = jsToAddress((String) params.get(0));
            byte[] powHash = jsToAddress((String) params.get(1));
            byte[] mixDigest = jsToAddress((String) params.get(2));

            JSONRPC2Response res = new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            return res;
        }

    }
}