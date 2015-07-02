package org.ethereum.android.jsonrpc.light.method;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.ethereum.android.jsonrpc.light.JsonRpcServerMethod;
import org.ethereum.facade.Ethereum;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

/*
TODO: done it when shh will be ready in -core
*/
public class shh_post extends JsonRpcServerMethod {

    public shh_post (Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() != 1) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {
            JSONObject obj = (JSONObject)params.get(0);

            byte[] from = null;
            if (obj.containsKey("from"))
                from = jsToAddress((String)obj.get("from"));

            byte[] to = null;
            if (obj.containsKey("to"))
                to = jsToAddress((String)obj.get("to"));

            ArrayList<byte[]> topics = new ArrayList<>();
            for (Object item : (JSONArray)obj.get("topics")) {
                if (item instanceof String) {
                    topics.add(jsToAddress((String)item));
                }
            }

            byte[] payload = jsToAddress((String)obj.get("payload"));

            int priority = jsToInt((String) obj.get("priority"));

            int ttl = jsToInt((String)obj.get("ttl"));

//TODO: implement after Adrian merge with dev

            JSONRPC2Response res = new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            return res;
        }
    }
}