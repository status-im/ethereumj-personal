package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import net.minidev.json.JSONObject;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.android.jsonrpc.filter.FilterLog;
import org.ethereum.android.jsonrpc.filter.FilterManager;
import org.ethereum.facade.Ethereum;
import java.util.List;

public class eth_getLogs extends JsonRpcServerMethod {

    public eth_getLogs (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 1) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            JSONObject obj = (JSONObject)params.get(0);
            JSONRPC2Response res = new JSONRPC2Response((new FilterLog(ethereum, obj)).toJS(ethereum), req.getID());
            return res;
        }

    }
}