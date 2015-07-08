package org.ethereum.android.jsonrpc.light.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import net.minidev.json.JSONObject;
import org.ethereum.android.jsonrpc.light.JsonRpcServerMethod;
import org.ethereum.core.Account;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Ethereum;
import org.spongycastle.util.encoders.Hex;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.ethereum.core.Denomination.SZABO;
import static org.ethereum.config.SystemProperties.CONFIG;


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

            ArrayList<Object> rparams = new ArrayList<Object>();
            rparams.add("0x" + Hex.toHexString(tx.getEncoded()));
            JSONRPC2Request rreq = new JSONRPC2Request("eth_sendRawTransaction", rparams, req.getID());
            JSONRPC2Response rres = getRemoteData(rreq);
            if (rres == null) {
                return new JSONRPC2Response(JSONRPC2Error.INTERNAL_ERROR, req.getID());
            }

            JSONRPC2Response res = new JSONRPC2Response(rres.getResult(), req.getID());
            return res;
        }

    }
}