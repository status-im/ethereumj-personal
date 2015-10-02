package org.ethereum.android.jsonrpc.full.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.full.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.eth.handler.EthHandler;

public class eth_protocolVersion extends JsonRpcServerMethod {

    public eth_protocolVersion (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        String tmp = Byte.toString(EthVersion.LOWER);
        JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
        return res;

    }
}