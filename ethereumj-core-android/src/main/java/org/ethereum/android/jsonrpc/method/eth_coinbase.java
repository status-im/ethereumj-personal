package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;
import org.ethereum.core.*;
import org.spongycastle.util.encoders.Hex;

/*
Present big issue - current ethereumj-core not have coinbase "functionality".
On each app start - it create 2 addresses: "cow", coinbase.secret ("monkey") --- WorldManager.java -> init
Also because not present mining functionality - no wat to identify what address will be coinbase (mining success payment place to)
TODO: change this after fix in ethereumj-core
*/

public class eth_coinbase extends JsonRpcServerMethod {

    public eth_coinbase (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        Wallet w = ethereum.getWallet();
        String tmp = "0x" + Hex.toHexString(((Account) w.getAccountCollection().toArray()[1]).getEcKey().getAddress());
        JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
        return res;

    }
}