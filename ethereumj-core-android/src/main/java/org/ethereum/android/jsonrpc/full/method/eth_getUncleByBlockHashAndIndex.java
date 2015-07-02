package org.ethereum.android.jsonrpc.full.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.full.JsonRpcServerMethod;
import org.ethereum.core.Block;
import org.ethereum.crypto.HashUtil;
import org.ethereum.facade.Ethereum;
import java.util.List;

public class eth_getUncleByBlockHashAndIndex extends JsonRpcServerMethod {

    public eth_getUncleByBlockHashAndIndex (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 2) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            byte[] address = jsToAddress((String) params.get(0));
            int index = jsToInt((String) params.get(1));

            Block block = ethereum.getBlockchain().getBlockByHash(address);

            if (block == null)
                return new JSONRPC2Response(null, req.getID());

            if (block.getUncleList().size() <= index)
                return new JSONRPC2Response(null, req.getID());

            Block uncle = new Block(block.getUncleList().get(index), null, null);

            JSONRPC2Response res = new JSONRPC2Response(blockToJS(uncle, false), req.getID());
            return res;
        }

    }
}