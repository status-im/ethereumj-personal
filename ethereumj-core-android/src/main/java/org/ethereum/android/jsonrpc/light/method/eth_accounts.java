package org.ethereum.android.jsonrpc.light.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.light.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;
import org.ethereum.core.*;
import org.spongycastle.util.encoders.Hex;
import java.util.ArrayList;
import java.util.Collection;

public class eth_accounts extends JsonRpcServerMethod {

    public eth_accounts (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        Collection<Account> accounts = ethereum.getWallet().getAccountCollection();
        ArrayList<String> tmp = new ArrayList<String>();
        for (Account ac : accounts) {
            tmp.add("0x" + Hex.toHexString(ac.getEcKey().getAddress()));
        }
        JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
        return res;

    }
}