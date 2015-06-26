package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;

import net.minidev.json.JSONObject;

import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Ethereum;
import org.spongycastle.util.encoders.Hex;
import java.math.BigInteger;
import java.util.List;

public class eth_getTransactionByBlockNumberAndIndex extends JsonRpcServerMethod {

    public eth_getTransactionByBlockNumberAndIndex (Ethereum ethereum) {
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

            Block block = null;
            JSONObject tx = null;
            if (blockNumber == -2) {
                if (ethereum.getPendingTransactions().size() <= index)
                    return new JSONRPC2Response(null, req.getID());
                tx = transactionToJS(null, (Transaction)ethereum.getPendingTransactions().toArray()[index]);
            } else {
                block = ethereum.getBlockchain().getBlockByNumber(blockNumber);
                if (block == null)
                    return new JSONRPC2Response(null, req.getID());
                if (block.getTransactionsList().size() <= index)
                    return new JSONRPC2Response(null, req.getID());
                tx = transactionToJS(block, block.getTransactionsList().get(index));
            }

            JSONRPC2Response res = new JSONRPC2Response(tx, req.getID());
            return res;
        }

    }
}