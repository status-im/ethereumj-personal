package org.ethereum.android.jsonrpc.light.method;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.ethereum.android.jsonrpc.light.JsonRpcServerMethod;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Ethereum;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class eth_getTransactionHistory extends JsonRpcServerMethod {

    public eth_getTransactionHistory(Ethereum ethereum) {
        super(ethereum);
    }

    protected JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx) {

        List<Object> params = req.getPositionalParams();
        if (params.size() < 1) {
            return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, req.getID());
        } else {

            String urlS = "https://etherchain.org/api/account/"+params.get(0)+"/tx/";
            if (params.size() > 1) {
                urlS += params.get(1);
            }

            String rres = "";
            try {
                URL url = new URL(urlS);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    InputStreamReader isw = new InputStreamReader(in);

                    int data = isw.read();
                    while (data != -1) {
                        rres += (char) data;
                        data = isw.read();
                    }
                }
                finally{
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
            }

            JSONRPC2Response res = new JSONRPC2Response(JSONValue.parse(rres), req.getID());
            return res;
        }

    }
}