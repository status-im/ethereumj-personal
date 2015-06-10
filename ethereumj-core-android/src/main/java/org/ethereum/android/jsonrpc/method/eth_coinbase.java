package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.facade.Ethereum;

public class eth_coinbase implements RequestHandler {

    private String name = "";
    private Ethereum ethereum;

    public eth_coinbase(Ethereum ethereum) {
        this.ethereum = ethereum;
        name = this.getClass().getSimpleName();
    }

    public String[] handledRequests() {
        return new String[]{name};
    }

    public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
        if (req.getMethod().equals(name)) {
            //TODO: place business logic here
            return null;
        } else {
            return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
        }
    }
}