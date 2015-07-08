package org.ethereum.android.jsonrpc.full.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.full.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;
import java.util.ArrayList;

public class eth_getCompilers extends JsonRpcServerMethod {

    public eth_getCompilers (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        ArrayList<String> tmp = new ArrayList<String>();
/*
TODO: add lll and solidity and serpent when we find good libs for them. They not planned to be implemented in -core.
*/
        JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
        return res;

    }
}