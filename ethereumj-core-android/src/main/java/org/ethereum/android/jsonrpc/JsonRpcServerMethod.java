package org.ethereum.android.jsonrpc;

import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.ethereum.android.jsonrpc.JsonRpcServer;
import org.ethereum.core.Account;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.HashUtil;
import org.ethereum.facade.Ethereum;
import org.ethereum.util.RLP;
import org.ethereum.vm.DataWord;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.ethereum.core.Denomination.SZABO;

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

    protected Transaction jsToTransaction(JSONObject obj) throws Exception {
        if ((!obj.containsKey("to") || ((String)obj.get("to")).equals("")) && (!obj.containsKey("data") || ((String)obj.get("data")).equals(""))) {
            throw new Exception("");
        }

        byte[] from = ((Account) ethereum.getWallet().getAccountCollection().toArray()[1]).getEcKey().getAddress();
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
        BigInteger gas = acc.getBalance().divide(gasPrice);
        BigInteger gasBBRemaining = new BigInteger(Long.toString((ethereum.getBlockchain().getBestBlock().getGasLimit() - ethereum.getBlockchain().getBestBlock().getGasUsed()) / 5));
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
        BigInteger nonce = ethereum.getRepository().getNonce(acc.getAddress());
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

    protected JSONObject blockToJS (Block block, Boolean detailed) {
        JSONObject res = new JSONObject();
        if (block == null)
            return null;

        res.put("number", "0x" + Long.toHexString(block.getNumber()));

        res.put("hash", "0x" + Hex.toHexString(block.getHash()));

        res.put("parentHash", "0x" + Hex.toHexString(block.getParentHash()));

        res.put("nonce", "0x" + Hex.toHexString(block.getNonce()));

        res.put("sha3Uncles", "0x" + Hex.toHexString(block.getUnclesHash()));

        res.put("logsBloom", "0x" + Hex.toHexString(block.getLogBloom()));

        res.put("transactionsRoot", "0x" + Hex.toHexString(block.getHeader().getTxTrieRoot()));

        res.put("stateRoot", "0x" + Hex.toHexString(block.getStateRoot()));

        res.put("miner", "0x" + Hex.toHexString(block.getCoinbase()));

        res.put("difficulty", "0x" + block.getDifficultyBI().toString(16));

        res.put("totalDifficulty", "0x" + block.getCumulativeDifficulty().toString(16));

        res.put("extraData", "0x" + Hex.toHexString(block.getExtraData()));

        // No way to get size of block in bytes, so I try calculate it using formula from  getEncoded
        byte[] header = block.getHeader().getEncoded();
        byte[] transactions = RLP.encodeList();
        byte[][] unclesEncoded = new byte[block.getUncleList().size()][];
        int i = 0;
        for (BlockHeader uncle : block.getUncleList()) {
            unclesEncoded[i] = uncle.getEncoded();
            ++i;
        }
        byte[] uncles = RLP.encodeList(unclesEncoded);
        byte[] rlpEncoded = RLP.encodeList(header, transactions, uncles);
        res.put("size", "0x" + Integer.toHexString(rlpEncoded.length));

        res.put("gasLimit", "0x" + Long.toHexString(block.getGasLimit()));

        res.put("gasUsed", "0x" + Long.toHexString(block.getGasUsed()));

        res.put("timestamp", "0x" + Long.toHexString(block.getTimestamp()));

        JSONArray transactionsJA = new JSONArray();
        i = 0;
        for (Transaction transaction : block.getTransactionsList()) {
            if (detailed) {
                JSONObject tx = transactionToJS(block, transaction);
                tx.put("transactionIndex", "0x" + Integer.toHexString(i));
                tx.put("blockHash", "0x" + Hex.toHexString(block.getHash()));
                tx.put("blockNumber", "0x" + Long.toHexString(block.getNumber()));
                transactionsJA.add(tx);
            } else {
                transactionsJA.add("0x" + Hex.toHexString(transaction.getHash()));
            }
            ++i;
        }
        res.put("transactions", transactionsJA);

        JSONArray unclesJA = new JSONArray();
        for (BlockHeader uncle : block.getUncleList()) {
            unclesJA.add("0x" + Hex.toHexString(HashUtil.sha3(uncle.getEncoded())));
        }
        res.put("uncles", unclesJA);

        return res;
    }

    protected JSONObject transactionToJS (Block block, Transaction transaction) {
        JSONObject res = new JSONObject();

        res.put("hash", "0x" + Hex.toHexString(transaction.getHash()));

        res.put("nonce", "0x" + Hex.toHexString(transaction.getNonce()));

        res.put("from", "0x" + Hex.toHexString(transaction.getSender()));

        res.put("to", "0x" + Hex.toHexString(transaction.getReceiveAddress()));

        res.put("value", "0x" + Hex.toHexString(transaction.getValue()));

        res.put("gasPrice", "0x" + Hex.toHexString(transaction.getGasPrice()));

        res.put("gas", "0x" + Hex.toHexString(transaction.getGasLimit()));

        res.put("input", "0x" + Hex.toHexString(transaction.getData()));

        if (block == null) {
            res.put("transactionIndex", null);
            res.put("blockHash", null);
            res.put("blockNumber", null);
        } else {
            long txi = 0;
            for (Transaction tx : block.getTransactionsList()) {
                if (Arrays.equals(tx.getHash(), transaction.getHash()))
                    break;
                txi++;
            }
            res.put("transactionIndex", "0x" + Long.toHexString(txi));
            res.put("blockHash", "0x" + Hex.toHexString(block.getHash()));
            res.put("blockNumber", "0x" + Long.toHexString(block.getNumber()));
        }

        return res;
    }
}