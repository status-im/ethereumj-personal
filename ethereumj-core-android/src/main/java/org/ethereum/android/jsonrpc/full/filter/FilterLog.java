package org.ethereum.android.jsonrpc.full.filter;

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

public class FilterLog extends FilterBase {

    private ArrayList<FilterLogData> logs = new ArrayList<FilterLogData>();

    long blockFrom;
    long blockTo;
    ArrayList<byte[]> addresses = new ArrayList<>();
    ArrayList<byte[]> topics = new ArrayList<>();

    private Ethereum ethereum;

    public FilterLog (Ethereum ethereum, JSONObject data) {
        this.ethereum = ethereum;
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

    public void processEvent(Object data) {
        if (data instanceof FilterLogData) {
            synchronized (logs) {
                FilterLogData li = (FilterLogData)data;
                if ((blockFrom >= 0 && li.block.getNumber() < blockFrom) || (blockTo >= 0 && li.block.getNumber() > blockTo))
                    return;

/*
TODO: Roman must implement Bloom contain. When it will be done - we can use just Bloom.
*/
                if (checkLogInfo(li.li))
                    logs.add(li);
            }
        }
    }

    public JSONArray toJS() {
        updateLastRequest();
        JSONArray res = new JSONArray();
        synchronized (logs) {
            for(FilterLogData item : logs) {
                res.add(logInfoToJS(item));
            }
            logs.clear();
        }
        return res;
    }

    public JSONArray toJS(Ethereum ethereum) {
        JSONArray res = new JSONArray();

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
                                res.add(logInfoToJS(new FilterLogData(block, txr, li)));
                        }
                    }
                }
                i++;
            }
        }

        if (blockFrom < 0 || blockTo < 0) {
            for (Transaction tx : ethereum.getPendingTransactions()) {
                TransactionReceipt txr = ethereum.getBlockchain().getTransactionReceiptByHash(tx.getHash());
                if (txr != null) {
                    for (LogInfo li :  txr.getLogInfoList()) {
                        if (checkLogInfo(li))
                            res.add(logInfoToJS(new FilterLogData(null, txr, li)));
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

    private JSONObject logInfoToJS(FilterLogData data) {
        JSONObject res = new JSONObject();

        if (data.block == null) {
            res.put("type", "pending");
            res.put("logIndex", null);
            res.put("transactionIndex", null);
            res.put("transactionHash", null);
            res.put("blockHash", null);
            res.put("blockNumber", null);
        } else {
            res.put("type", "mined");
            long txi = 0;
            long lii = 0;
/*
TODO: for me it's a little strange way.
*/
            for (Transaction tx : data.block.getTransactionsList()) {
                for (LogInfo li : ethereum.getBlockchain().getTransactionReceiptByHash(tx.getHash()).getLogInfoList()) {
                    if (li.getBloom().equals(data.li.getBloom()))
                        break;
                    lii++;
                }
                if (Arrays.equals(tx.getHash(), data.txr.getTransaction().getHash())) {
                    break;
                }
                txi++;
            }
            res.put("logIndex", "0x" + Long.toHexString(lii));
            res.put("transactionIndex", "0x" + Long.toHexString(txi));
            res.put("transactionHash", "0x" + Hex.toHexString(data.txr.getTransaction().getHash()));
            res.put("blockHash", "0x" + Hex.toHexString(data.block.getHash()));
            res.put("blockNumber", "0x" + Long.toHexString(data.block.getNumber()));
        }

        res.put("address", "0x" + Hex.toHexString(data.li.getAddress()));

        res.put("data", "0x" + Hex.toHexString(data.li.getData()));

        JSONArray topics = new JSONArray();
        for (DataWord topic : data.li.getTopics()) {
            topics.add("0x" + Hex.toHexString(topic.getData()));
        }
        res.put("topics", topics);

        return res;
    }

    public static class FilterLogData {
        public Block block;
        public TransactionReceipt txr;
        public LogInfo li;

        public FilterLogData( Block block, TransactionReceipt txr, LogInfo li) {
            this.block = block;
            this.txr = txr;
            this.li = li;
        }
    }
}
