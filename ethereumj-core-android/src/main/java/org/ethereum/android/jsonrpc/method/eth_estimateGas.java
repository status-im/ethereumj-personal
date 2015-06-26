package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import net.minidev.json.JSONObject;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Ethereum;
import org.ethereum.vm.Program;
import org.ethereum.vm.VM;
import org.spongycastle.util.encoders.Hex;
import java.util.List;

public class eth_estimateGas extends JsonRpcServerMethod {

    public eth_estimateGas (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 2) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            JSONObject obj = (JSONObject)params.get(0);
            Transaction tx;
            try {
                tx = jsToTransaction(obj);
            } catch (Exception e) {
                return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
            }

            String height = (String)params.get(1);
            long blockNumber = getBlockNumber(height);
            byte[] root = ethereum.getBlockchain().getBestBlock().getStateRoot();

            if (blockNumber >= 0) {
                ethereum.getRepository().syncToRoot(ethereum.getBlockchain().getBlockByNumber(blockNumber).getStateRoot());
            }

            VM vm = new VM();
            Program program = new Program(tx.getData(), null);
            vm.play(program);
            long result = program.getResult().getGasUsed();

            if (blockNumber >= 0) {
                ethereum.getRepository().syncToRoot(root);
            }

            String tmp = "0x" + Long.toHexString(result);
            JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
            return res;
        }

    }
}