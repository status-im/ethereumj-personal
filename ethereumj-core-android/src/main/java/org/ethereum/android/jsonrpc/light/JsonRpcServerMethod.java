package org.ethereum.android.jsonrpc.light;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.server.*;
import net.minidev.json.JSONObject;
import org.ethereum.core.Account;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Ethereum;
import org.spongycastle.util.encoders.Hex;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class JsonRpcServerMethod implements RequestHandler {

    private String name = "";
    protected Ethereum ethereum;
    private JSONRPC2Session jpSession;
    private String currentUrl;

    public JsonRpcServerMethod(Ethereum ethereum) {
        this.ethereum = ethereum;
        name = this.getClass().getSimpleName();
    }

    public String[] handledRequests() {
        return new String[]{name};
    }

    public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {
        if (req.getMethod().equals(name)) {
            return worker(req, ctx);
        } else {
            return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
        }
    }

    protected abstract JSONRPC2Response worker(JSONRPC2Request req, MessageContext ctx);

    protected long getBlockNumber(String height) {
        long blockNumber = 0;
        switch (height) {
            case "earliest":
                blockNumber = 0;
                break;
            case "latest":
                blockNumber = -1;
                break;
            case "pending":
                blockNumber = -2;
                break;
            default:
                blockNumber = jsToLong(height);
        }
        if (blockNumber >= 0)
            blockNumber = -1;
        return blockNumber;
    }

    protected String clearJSString(String data) {
        if (data.substring(0, 2).equals("0x"))
            return data.substring(2);
        return data;
    }

    protected byte[] jsToAddress(String data) {
        return Hex.decode(clearJSString(data));
    }

    protected int jsToInt(String data) {
        return Integer.parseInt(clearJSString(data), 16);
    }

    protected long jsToLong(String data) {
        return Long.parseLong(clearJSString(data), 16);
    }

    protected BigInteger jsToBigInteger(String data) {
        return new BigInteger(clearJSString(data), 16);
    }

    protected Transaction jsToTransaction(JSONObject obj) throws Exception {
        if ((!obj.containsKey("to") || ((String)obj.get("to")).equals("")) && (!obj.containsKey("data") || ((String)obj.get("data")).equals(""))) {
            throw new Exception("");
        }

        byte[] from = getCoinBase();
        if (obj.containsKey("from") && !((String)obj.get("from")).equals("")) {
            from = jsToAddress((String) obj.get("from"));
        }

        Account acc = null;
        for (Account ac : ethereum.getWallet().getAccountCollection()) {
            if (Arrays.equals(ac.getAddress(), from)) {
                acc = ac;
                break;
            }
        }
        if (acc == null) {
            throw new Exception("");
        }

        byte[] senderPrivKey = acc.getEcKey().getPrivKeyBytes();

        // default - from ethereumj-studio
        byte[] to = null;
        if (obj.containsKey("to") && !((String)obj.get("to")).equals("")) {
            to = jsToAddress((String) obj.get("to"));
        }

        // default - from ethereumj-studio
        BigInteger value = new BigInteger("1000");
        if (obj.containsKey("value") && !((String)obj.get("value")).equals("")) {
            value = jsToBigInteger((String) obj.get("value"));
        }

        // default - from ethereumj-studio
        byte[] data = new byte[]{};
        if (obj.containsKey("data") && !((String)obj.get("data")).equals("")) {
            data = jsToAddress((String) obj.get("data"));
        }

        BigInteger gasPrice = getGasPrice();
        if (obj.containsKey("gasPrice") && !((String)obj.get("gasPrice")).equals("")) {
            gasPrice = jsToBigInteger((String) obj.get("gasPrice"));
        }

        JSONObject tmp = new JSONObject();
        if (to != null)
            tmp.put("to", "0x" + Hex.toHexString(to));
        tmp.put("data", Hex.toHexString(data));
        BigInteger gas = getEstimateGas(tmp);
        if (obj.containsKey("gas") && !((String)obj.get("gas")).equals("")) {
            gas = jsToBigInteger((String) obj.get("gas"));
        }

        // default - from ethereumj-studio
        BigInteger nonce = getTransactionCount(from);
        if (obj.containsKey("nonce") && !((String)obj.get("nonce")).equals("")) {
            nonce = jsToBigInteger((String) obj.get("nonce"));
        }

        Transaction tx = ethereum.createTransaction(nonce, gasPrice, gas, to, value, data);

        tx.sign(senderPrivKey);

        return tx;
    }


    protected JSONRPC2Response getRemoteData(JSONRPC2Request req) {
        URL url = JsonRpcServer.getRemoteServer();
        boolean isChanged = !url.toString().equals(currentUrl);
        if (isChanged) {
            currentUrl = url.toString();
        }
        try {
            if (jpSession == null) {
                jpSession = new JSONRPC2Session(url);
            } else {
                if (isChanged) {
                    currentUrl = url.toString();
                    jpSession = new JSONRPC2Session(url);
                }
            }

            return jpSession.send(req);
        } catch (Exception e) {
            System.out.println("Exception getting remote rpc data: " + e.getMessage());
            ethereum.getListener().trace("Exception getting remote rpc data: " + e.getMessage());
            if (!JsonRpcServer.IsRemoteServerRecuring) {
                return getRemoteData(req);
            } else {
                return null;
            }
        }
    }

    protected byte[] getCoinBase() {
        return ((Account) ethereum.getWallet().getAccountCollection().toArray()[0]).getEcKey().getAddress();
    }

    protected BigInteger getBalance(byte[] account) {
        ArrayList<Object> params = new ArrayList<Object>();
        params.add("0x" + Hex.toHexString(account));
        params.add("latest");
        JSONRPC2Request req = new JSONRPC2Request("eth_getBalance", params, 1000);
        JSONRPC2Response res = getRemoteData(req);
        if (res == null || !res.indicatesSuccess()) {
            return BigInteger.ZERO;
        } else {
            return jsToBigInteger(res.getResult().toString());
        }
    }

    protected BigInteger getLatestBlockGasRemaining() {
        ArrayList<Object> params = new ArrayList<Object>();
        params.add("latest");
        params.add(false);
        JSONRPC2Request req = new JSONRPC2Request("eth_getBlockByNumber", params, 1000);
        JSONRPC2Response res = getRemoteData(req);
        if (res == null || !res.indicatesSuccess()) {
            return BigInteger.ZERO;
        } else {
            JSONObject block = (JSONObject)res.getResult();
            return jsToBigInteger((String)block.get("gasLimit")).add(jsToBigInteger((String) block.get("gasUsed")).negate());
        }
    }

    protected BigInteger getTransactionCount(byte[] account) {
        ArrayList<Object> params = new ArrayList<Object>();
        params.add("0x" + Hex.toHexString(account));
        params.add("latest");
        JSONRPC2Request req = new JSONRPC2Request("eth_getTransactionCount", params, 1000);
        JSONRPC2Response res = getRemoteData(req);
        if (res == null || !res.indicatesSuccess()) {
            return BigInteger.ZERO;
        } else {
            return jsToBigInteger(res.getResult().toString());
        }
    }

    protected BigInteger getGasPrice() {
        ArrayList<Object> params = new ArrayList<Object>();
        JSONRPC2Request req = new JSONRPC2Request("eth_gasPrice", params, 1000);
        JSONRPC2Response res = getRemoteData(req);
        if (res == null || !res.indicatesSuccess()) {
            return BigInteger.ZERO;
        } else {
            return jsToBigInteger(res.getResult().toString());
        }
    }

    protected BigInteger getEstimateGas(JSONObject obj) {
        ArrayList<Object> params = new ArrayList<Object>();
        params.add(obj);
        JSONRPC2Request req = new JSONRPC2Request("eth_estimateGas", params, 1000);
        JSONRPC2Response res = getRemoteData(req);
        if (res == null || !res.indicatesSuccess()) {
            return BigInteger.valueOf(90000);
        } else {
            return jsToBigInteger(res.getResult().toString());
        }
    }

}