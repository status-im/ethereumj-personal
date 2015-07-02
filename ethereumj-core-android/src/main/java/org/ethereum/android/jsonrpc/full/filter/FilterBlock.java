package org.ethereum.android.jsonrpc.full.filter;

import net.minidev.json.JSONArray;
import org.ethereum.core.Block;
import org.ethereum.facade.Ethereum;
import org.spongycastle.util.encoders.Hex;
import java.util.ArrayList;


public class FilterBlock extends FilterBase {

    private ArrayList<String> blocks = new ArrayList<String>();

    public void processEvent(Object data) {
        if (data instanceof Block) {
            synchronized (blocks) {
                blocks.add("0x" + Hex.toHexString(((Block) data).getHash()));
            }
        }
    }

    public JSONArray toJS() {
        updateLastRequest();
        JSONArray res = new JSONArray();
        synchronized (blocks) {
            for(String item : blocks) {
                res.add(item);
            }
            blocks.clear();
        }
        return res;
    }

    public JSONArray toJS(Ethereum ethereum) {
        return null;
    }

}
