package org.ethereum.android.jsonrpc.filter;

import net.minidev.json.JSONArray;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Ethereum;
import org.spongycastle.util.encoders.Hex;
import java.util.ArrayList;


public class FilterTransaction extends FilterBase {

    private ArrayList<String> transactions = new ArrayList<String>();

    public void processEvent(Object data) {
        if (data instanceof Transaction) {
            synchronized (transactions) {
                transactions.add("0x" + Hex.toHexString(((Transaction) data).getHash()));
            }
        }
    }

    public JSONArray toJS() {
        updateLastRequest();
        JSONArray res = new JSONArray();
        synchronized (transactions) {
            for(String item : transactions) {
                res.add(item);
            }
            transactions.clear();
        }
        return res;
    }

    public JSONArray toJS(Ethereum ethereum) {
        return null;
    }

}
