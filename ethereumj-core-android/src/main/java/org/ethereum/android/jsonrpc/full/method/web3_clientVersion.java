package org.ethereum.android.jsonrpc.full.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.full.JsonRpcServerMethod;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.util.Utils;

public class web3_clientVersion extends JsonRpcServerMethod {

    public web3_clientVersion (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        String tmp = "ethereumj/v" + SystemProperties.CONFIG.projectVersion() + "/android/java" + Utils.JAVA_VERSION;
        JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
        return res;

    }
}