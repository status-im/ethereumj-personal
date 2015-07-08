package org.ethereum.android.jsonrpc.full.filter;

import net.minidev.json.JSONArray;

import org.ethereum.facade.Ethereum;

public abstract class FilterBase {
    protected int id;
    protected long lastRequest = System.currentTimeMillis();

    public int getId() {
        return id;
    }

    public void  setId(int id) {
        this.id = id;
    }

    public long getLastRequestTime() {
        return lastRequest;
    }

    protected void updateLastRequest() {
        lastRequest = System.currentTimeMillis();
    }

    public abstract void processEvent(Object data);
    public abstract JSONArray toJS();
    public abstract JSONArray toJS(Ethereum ethereum);
}
