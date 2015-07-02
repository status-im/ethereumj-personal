package org.ethereum.android.jsonrpc.full.whisper;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.ethereum.facade.Ethereum;
import org.ethereum.net.shh.ShhMessage;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;

/*
TODO: replace ShhMessage with real class when Roman finish Shh.
*/
public class Filter {
    protected int id;
    protected ArrayList<ShhMessage> messages;
    byte[] to = null;
    boolean isWildcard = false;
    ArrayList<ArrayList<byte[]>> or;
    ArrayList<byte[]> and;
    int sendID = 0;

    public Filter (JSONObject data) {
        messages = new ArrayList<ShhMessage>();
        if (data.containsKey("to")) {
            to = Hex.decode(((String)data.get("to")).substring(2));
        }
        JSONArray topics = (JSONArray)data.get("topics");
        for (Object item : topics) {
            if (item == null) {
                isWildcard = true;
            }
            else if (item instanceof JSONArray) {
                ArrayList<byte[]> tmp = new ArrayList<byte[]>();
                for (Object ori : (JSONArray)item) {
                    if (ori instanceof String) {
                        tmp.add(Hex.decode(((String)ori).substring(2)));
                    }
                }
                or.add(tmp);
            } else if (item instanceof String) {
                and.add(Hex.decode(((String)item).substring(2)));
            }
        }
    }

    public int getId() {
        return id;
    }

    public void  setId(int id) {
        this.id = id;
    }

    public void processEvent(Object data) {
//TODO: parse incomming data when we will know what comes.
    }

    public JSONArray toJS() {
        JSONArray res = new JSONArray();
        synchronized (messages) {
            for (int i = sendID; i < messages.size(); i++) {
                res.add(toJS(messages.get(i)));
            }
            sendID = messages.size();
        }
        return res;
    }

    public JSONArray toJSAll() {
        JSONArray res = new JSONArray();
        synchronized (messages) {
            for (int i = 0; i < messages.size(); i++) {
                res.add(toJS(messages.get(i)));
            }
            sendID = messages.size();
        }
        return res;
    }

    private JSONObject toJS (ShhMessage data) {
        JSONObject res = new JSONObject();
        return res;
    }
}
