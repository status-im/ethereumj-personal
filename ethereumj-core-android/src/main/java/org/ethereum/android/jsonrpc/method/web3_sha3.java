package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.crypto.SHA3Helper;
import org.ethereum.facade.Ethereum;
import org.spongycastle.util.encoders.Hex;

import java.util.List;


public class web3_sha3 extends JsonRpcServerMethod {

    public web3_sha3 (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 1) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            byte[] data = jsToAddress((String)params.get(0));
            String tmp = "0x" + Hex.toHexString(SHA3Helper.sha3(data));
            JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
            return res;
        }

    }
}