package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;
import java.util.List;

public class eth_getUncleCountByBlockNumber extends JsonRpcServerMethod {

    public eth_getUncleCountByBlockNumber (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 1) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            String height = (String)params.get(0);

            long blockNumber = getBlockNumber(height);
            if (blockNumber == -1)
                blockNumber = ethereum.getBlockchain().getBestBlock().getNumber();

            int count = 0;
            if (blockNumber == -2) {
                count = 0;
            } else {
                count = ethereum.getBlockchain().getBlockByNumber(blockNumber).getUncleList().size();
            }

            String tmp = "0x" + Integer.toHexString(count);
            JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
            return res;
        }

    }
}