package org.ethereum.android.jsonrpc.full.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.full.JsonRpcServerMethod;
import org.ethereum.android.jsonrpc.full.filter.FilterBlock;
import org.ethereum.android.jsonrpc.full.filter.FilterManager;
import org.ethereum.android.jsonrpc.full.filter.FilterTransaction;
import org.ethereum.facade.Ethereum;

public class eth_newPendingTransactionFilter extends JsonRpcServerMethod {

    public eth_newPendingTransactionFilter (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        int id = FilterManager.getInstance().addFilter(new FilterTransaction());
        String tmp = "0x" + Integer.toHexString(id);
        JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
        return res;

    }
}