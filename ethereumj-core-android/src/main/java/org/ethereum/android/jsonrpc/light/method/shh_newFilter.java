package org.ethereum.android.jsonrpc.light.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;

import net.minidev.json.JSONObject;

import org.ethereum.android.jsonrpc.light.JsonRpcServerMethod;
import org.ethereum.android.jsonrpc.light.whisper.Filter;
import org.ethereum.android.jsonrpc.light.whisper.FilterManager;
import org.ethereum.facade.Ethereum;

import java.util.List;

public class shh_newFilter extends JsonRpcServerMethod {

    public shh_newFilter(Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 1) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            JSONObject obj = (JSONObject)params.get(0);
            int id = FilterManager.getInstance().addFilter(new Filter(obj));
            String tmp = "0x" + Integer.toHexString(id);
            JSONRPC2Response res = new JSONRPC2Response(tmp, req.getID());
            return res;
        }

    }
}