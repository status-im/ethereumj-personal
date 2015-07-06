package org.ethereum.android.jsonrpc.full.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.full.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;
import org.ethereum.core.*;
import org.spongycastle.util.encoders.Hex;

/*
TODO: -core not handle mining so coinbase not present in it. Right now returned second address from Wallet. Must be changed in app where implemented mining
*/
public class eth_coinbase extends JsonRpcServerMethod {

    public eth_coinbase (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        String tmp = "0x" + Hex.toHexString(getCoinBase());
        JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
        return res;

    }
}