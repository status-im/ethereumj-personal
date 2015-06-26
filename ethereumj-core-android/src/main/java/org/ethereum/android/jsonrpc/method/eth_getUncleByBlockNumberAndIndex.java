package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.core.Block;
import org.ethereum.crypto.HashUtil;
import org.ethereum.facade.Ethereum;
import java.util.List;


public class eth_getUncleByBlockNumberAndIndex extends JsonRpcServerMethod {

    public eth_getUncleByBlockNumberAndIndex (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 2) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            String height = (String)params.get(0);
            long blockNumber = getBlockNumber(height);
            int index = jsToInt((String) params.get(1));

            if (blockNumber == -1) {
                blockNumber = ethereum.getBlockchain().getBestBlock().getNumber();
            }

            if (blockNumber == -2) {
            }

            Block block = ethereum.getBlockchain().getBlockByNumber(blockNumber);

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