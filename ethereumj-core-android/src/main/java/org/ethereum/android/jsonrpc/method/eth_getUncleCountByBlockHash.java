package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;
import org.spongycastle.util.encoders.Hex;
import java.util.List;

public class eth_getUncleCountByBlockHash extends JsonRpcServerMethod {

    public eth_getUncleCountByBlockHash (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 1) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            byte[] hash = jsToAddress((String)params.get(0));
            String tmp = "0x" + Integer.toHexString(ethereum.getBlockchain().getBlockByHash(hash).getUncleList().size());
            JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
            return res;
        }

    }
}