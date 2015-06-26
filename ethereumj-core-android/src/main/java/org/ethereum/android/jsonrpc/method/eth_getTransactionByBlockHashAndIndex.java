package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;

import net.minidev.json.JSONObject;

import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.facade.Ethereum;
import org.spongycastle.util.encoders.Hex;
import java.math.BigInteger;
import java.util.List;

public class eth_getTransactionByBlockHashAndIndex extends JsonRpcServerMethod {

    public eth_getTransactionByBlockHashAndIndex (Ethereum ethereum) {
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

            if (block.getTransactionsList().size() <= index)
                return new JSONRPC2Response(null, req.getID());

            JSONObject tx = transactionToJS(block, block.getTransactionsList().get(index));
            tx.put("transactionIndex", "0x" + Integer.toHexString(index));
            tx.put("blockHash", "0x" + Hex.toHexString(block.getHash()));
            tx.put("blockNumber", "0x" + Long.toHexString(block.getNumber()));

            JSONRPC2Response res = new JSONRPC2Response(tx, req.getID());
            return res;
        }

    }
}