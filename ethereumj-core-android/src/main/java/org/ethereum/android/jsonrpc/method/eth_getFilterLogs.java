package org.ethereum.android.jsonrpc.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import net.minidev.json.JSONObject;
import org.ethereum.android.jsonrpc.JsonRpcServerMethod;
import org.ethereum.android.jsonrpc.filter.FilterBase;
import org.ethereum.android.jsonrpc.filter.FilterLog;
import org.ethereum.android.jsonrpc.filter.FilterManager;
import org.ethereum.facade.Ethereum;
import java.util.List;

public class eth_getFilterLogs extends JsonRpcServerMethod {

    public eth_getFilterLogs (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 1) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            int id = jsToInt((String) params.get(0));
            JSONRPC2Response res = new JSONRPC2Response(FilterManager.getInstance().toJS(id, ethereum), req.getID());
            return res;
        }

    }
}