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

import static org.ethereum.core.Denomination.SZABO;

public abstract class JsonRpcServerMethod implements RequestHandler {

    private String name = "";
    protected Ethereum ethereum;
    private JSONRPC2Session jpSession;

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

    protected byte[] jsToAddress(String data) {
        return Hex.decode(data.substring(2));
    }

    protected int jsToInt(String data) {
        return Integer.parseInt(data.substring(2), 16);
    }

    protected long jsToLong(String data) {
        return Long.parseLong(data.substring(2), 16);
    }

    protected BigInteger jsToBigInteger(String data) {
        return new BigInteger(data.substring(2), 16);
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
        BigInteger gasPrice = SZABO.value().multiply(BigInteger.TEN);
        if (obj.containsKey("gasPrice") && !((String)obj.get("gasPrice")).equals("")) {
            gasPrice = jsToBigInteger((String) obj.get("gasPrice"));
        }

        // default - from cpp-ethereum
        BigInteger gas = getBalance(from).divide(gasPrice);
        BigInteger gasBBRemaining = getLatestBlockGasRemaining();
        if (gasBBRemaining.compareTo(gas) < 0)
            gas = gasBBRemaining;
        if (obj.containsKey("gas") && !((String)obj.get("gas")).equals("")) {
            gas = jsToBigInteger((String) obj.get("gas"));
        }

        // default - from ethereumj-studio
        BigInteger value = new BigInteger("1000");
        if (obj.containsKey("value") && !((String)obj.get("value")).equals("")) {
            value = jsToBigInteger((String) obj.get("value"));
        }

        // default - from ethereumj-studio
        BigInteger nonce = getTransactionCount(from);
        if (obj.containsKey("nonce") && !((String)obj.get("nonce")).equals("")) {
            nonce = jsToBigInteger((String) obj.get("nonce"));
        }

        // default - from ethereumj-studio
        byte[] data = new byte[]{};
        if (obj.containsKey("data") && !((String)obj.get("data")).equals("")) {
            data = jsToAddress((String) obj.get("data"));
        }

        Transaction tx = ethereum.createTransaction(nonce, gasPrice, gas, to, value, data);

        tx.sign(senderPrivKey);

        return tx;
    }


    protected JSONRPC2Response getRemoteData(JSONRPC2Request req) {
        if (jpSession == null) {
            try {
                jpSession = new JSONRPC2Session(new URL(JsonRpcServer.RemoteServer));
            } catch (Exception e) {
                return null;
            }
        }
        try {
            return jpSession.send(req);
        } catch (Exception e) {
            return null;
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
            return jsToBigInteger((String)block.get("gasLimit")).add(jsToBigInteger((String)block.get("gasUsed")).negate());
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

}