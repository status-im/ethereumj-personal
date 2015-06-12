package org.ethereum.android.jsonrpc;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;

import org.ethereum.android.jsonrpc.JsonRpcServer;
import org.ethereum.core.Account;
import org.ethereum.core.AccountState;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Ethereum;
import org.ethereum.vm.DataWord;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public abstract class JsonRpcServerMethod implements RequestHandler {

    private String name = "";
    protected Ethereum ethereum;

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
}