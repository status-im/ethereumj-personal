package org.ethereum.android.jsonrpc.full.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.full.JsonRpcServerMethod;
import org.ethereum.core.AccountState;
import org.ethereum.facade.Ethereum;
import org.spongycastle.util.encoders.Hex;
import java.math.BigInteger;
import java.util.List;

/*
TODO: -core not have Solidity compiler
*/
public class eth_compileSolidity extends JsonRpcServerMethod {

    public eth_compileSolidity (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 1) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            String code = (String)params.get(0);

            return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
        }

    }
}