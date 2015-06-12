package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;
import org.spongycastle.util.encoders.Hex;
import java.util.List;

/*
Not sure if pending can have code. Also code by itself related to repository not very clear for me.
TODO: ask Roman advice for this method.
*/
public class eth_getCode extends JsonRpcServerMethod {

    public eth_getCode (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 2) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            byte[] address = jsToAddress((String)params.get(0));
            String height = (String)params.get(1);

            long blockNumber = getBlockNumber(height);

            byte[] root = ethereum.getBlockchain().getBestBlock().getStateRoot();

            if (blockNumber >= 0) {
                ethereum.getRepository().syncToRoot(ethereum.getBlockchain().getBlockByNumber(blockNumber).getStateRoot());
            }

            String tmp = "0x" + Hex.toHexString(ethereum.getRepository().getCode(address));

            if (blockNumber >= 0) {
                ethereum.getRepository().syncToRoot(root);
            }

            JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
            return res;
        }

    }
}