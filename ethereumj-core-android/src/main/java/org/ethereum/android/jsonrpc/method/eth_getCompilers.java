package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;
import java.util.ArrayList;

public class eth_getCompilers extends JsonRpcServerMethod {

    public eth_getCompilers (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        ArrayList<String> tmp = new ArrayList<String>();
        tmp.add("serpent");
        //TODO: add lll and solidity when they will be implemented in -core
        JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
        return res;

    }
}