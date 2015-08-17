package org.ethereum.android.jsonrpc.full.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.full.JsonRpcServer;
import org.ethereum.android.jsonrpc.full.JsonRpcServerMethod;
import org.ethereum.core.Repository;
import org.ethereum.facade.Ethereum;
import org.ethereum.vm.DataWord;
import org.spongycastle.util.encoders.Hex;
import java.util.List;

public class eth_getStorageAt extends JsonRpcServerMethod {

    public eth_getStorageAt(Ethereum ethereum) { super(ethereum); }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 3) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            byte[] address = jsToAddress((String)params.get(0));
            long key = jsToLong((String) params.get(1));
            String height = (String)params.get(2);

            long blockNumber = getBlockNumber(height);

            byte[] root = ethereum.getBlockchain().getBestBlock().getStateRoot();

            if (blockNumber >= 0) {
                Repository repository = (Repository)ethereum.getRepository();
                repository.syncToRoot(ethereum.getBlockchain().getBlockByNumber(blockNumber).getStateRoot());
            }

            String tmp = "0x" + Hex.toHexString(ethereum.getRepository().getStorageValue(address, new DataWord(key)).getData());

            if (blockNumber >= 0) {
                Repository repository = (Repository)ethereum.getRepository();
                repository.syncToRoot(root);
            }

            JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
            return res;
        }

    }
}