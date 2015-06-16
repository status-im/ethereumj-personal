package org.ethereum.android.jsonrpc.filter;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.facade.Ethereum;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.spongycastle.util.encoders.Hex;
import java.util.ArrayList;
import java.util.Arrays;

/*
Right now LogInfo not contains information about Transaction and Transaction not have information about block.
TODO: talk to Roman about create links between LogInfo and Transaction and between Transaction and Block.
*/
public class FilterLog extends FilterBase {

    private ArrayList<LogInfo> logs = new ArrayList<LogInfo>();

    long blockFrom;
    long blockTo;
    ArrayList<byte[]> addresses = new ArrayList<>();
    ArrayList<byte[]> topics = new ArrayList<>();

    public FilterLog (Ethereum ethereum, JSONObject data) {
        blockFrom = ethereum.getBlockchain().getBestBlock().getNumber();
        if (data.containsKey("fromBlock") && !((String)data.get("fromBlock")).equals("")) {
            String fromS = (String)data.get("fromBlock");
            if (fromS.equals("latest")) {
            } else if (fromS.equals("pending") || fromS.equals("earliest")) {
                blockFrom = -1;
            } else {
                blockFrom = Long.parseLong(fromS.substring(2), 16);
            }
        }

        blockTo = ethereum.getBlockchain().getBestBlock().getNumber();
        if (data.containsKey("toBlock") && !((String)data.get("toBlock")).equals("")) {
            String fromS = (String)data.get("toBlock");
            if (fromS.equals("latest")) {
            } else if (fromS.equals("pending") || fromS.equals("earliest")) {
                blockTo = -1;
            } else {
                blockTo = Long.parseLong(fromS.substring(2), 16);
            }
        }

        if (data.containsKey("address")) {
            if (data.get("address") instanceof String) {
                addresses.add(Hex.decode(((String) data.get("address")).substring(2)));
            } else if (data.get("address") instanceof JSONArray){
                for (Object item : (JSONArray)data.get("address")) {
                    if (item instanceof String) {
                        addresses.add(Hex.decode(((String) item).substring(2)));
                    }
                }
            }
        }

        if (data.containsKey("topics")) {
            if (data.get("topics") instanceof String) {
                topics.add(Hex.decode(((String) data.get("topics")).substring(2)));
            } else if (data.get("topics") instanceof JSONArray){
                for (Object item : (JSONArray)data.get("topics")) {
                    if (item instanceof String) {
                        topics.add(Hex.decode(((String) item).substring(2)));
                    }
                }
            }
        }
    }

/*
TODO: Right now Bloom from -core can be used only to check total mach of 2 same class objects. Will be nice to have possibility to check contains.
*/
    public void processEvent(Object data) {
        if (data instanceof LogInfo) {
            synchronized (logs) {
                LogInfo li = (LogInfo)data;
                //TODO: check if li inside blockFrom - blockTo

                if (checkLogInfo(li))
                    logs.add(li);
            }
        }
    }

    public JSONArray toJS() {
        updateLastRequest();
        JSONArray res = new JSONArray();
        synchronized (logs) {
            for(LogInfo item : logs) {
                res.add(logInfoToJS(item));
            }
            logs.clear();
        }
        return res;
    }

    public JSONArray toJS(Ethereum ethereum) {
        JSONArray res = new JSONArray();

// Process mined blocks
        if (blockFrom >= 0) {
            long i = blockFrom;
            while (true) {
                Block block = ethereum.getBlockchain().getBlockByNumber(i);
                if (block == null)
                    break;
                for (Transaction tx : block.getTransactionsList()) {
                    TransactionReceipt txr = ethereum.getBlockchain().getTransactionReceiptByHash(tx.getHash());
                    if (txr != null) {
                        for (LogInfo li : txr.getLogInfoList()) {
                            if (checkLogInfo(li))
                                res.add(logInfoToJS(li));
                        }
                    }
                }
                i++;
            }
        }

/*
Process pending transactions. But not sure if BlockChain can return TransactionReceipt for pending transaction.
*/
        if (blockFrom < 0 || blockTo < 0) {
            for (Transaction tx : ethereum.getPendingTransactions()) {
                TransactionReceipt txr = ethereum.getBlockchain().getTransactionReceiptByHash(tx.getHash());
                if (txr != null) {
                    for (LogInfo li :  txr.getLogInfoList()) {
                        if (checkLogInfo(li))
                            res.add(logInfoToJS(li));
                    }
                }
            }
        }

        return res;
    }


    private boolean checkLogInfo(LogInfo li) {
        boolean found = false;
        for (byte[] address : addresses) {
            if (Arrays.equals(address, li.getAddress())) {
                found = true;
                break;
            }
        }
        if (!found)
            return false;

        found = false;
        for (byte[] topic : topics) {
            for (DataWord litopic : li.getTopics()) {
                if (Arrays.equals(topic, litopic.getData())) {
                    found = true;
                    break;
                }
            }
            if (found)
                break;
        }
        if (!found)
            return false;

        return true;
    }

    private JSONObject logInfoToJS(LogInfo li) {
        JSONObject res = new JSONObject();

/*
TODO: check here if log's transaction / block mined or pending.
*/
        res.put("type", "pending");
        res.put("logIndex", null);
        res.put("transactionIndex", null);
        res.put("transactionHash", null);
        res.put("blockHash", null);
        res.put("blockNumber", null);

        res.put("address", Hex.toHexString(li.getAddress()));

        res.put("data", Hex.toHexString(li.getData()));

        JSONArray topics = new JSONArray();
        for (DataWord topic : li.getTopics()) {
            topics.add(Hex.toHexString(topic.getData()));
        }
        res.put("topics", topics);

        return res;
    }
}
