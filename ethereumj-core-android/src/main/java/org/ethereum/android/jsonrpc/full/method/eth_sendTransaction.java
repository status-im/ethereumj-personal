package org.ethereum.android.jsonrpc.full.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import net.minidev.json.JSONObject;
import org.ethereum.android.jsonrpc.full.JsonRpcServerMethod;
import org.ethereum.core.Account;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Ethereum;
import org.spongycastle.util.encoders.Hex;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.ethereum.core.Denomination.SZABO;
import static org.ethereum.config.SystemProperties.CONFIG;


/*
TODO: get more information from Roman, he think about this right now about 20 - 32 result.
*/
public class eth_sendTransaction extends JsonRpcServerMethod {

    public eth_sendTransaction (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 1) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            JSONObject obj = (JSONObject)params.get(0);
            Transaction tx;
            try {
                tx = jsToTransaction(obj);
            } catch (Exception e) {
                return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
            }

            try {
                ethereum.submitTransaction(tx).get(CONFIG.transactionApproveTimeout(), TimeUnit.SECONDS);
            } catch (Exception e) {
                return new JSONRPC2Response(JSONRPC2Error.INTERNAL_ERROR, req.getID());
            }

            String tmp = "0x" + Hex.toHexString(tx.getHash());
            JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
            return res;
        }

    }
}