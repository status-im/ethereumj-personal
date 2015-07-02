package org.ethereum.android.jsonrpc.light.whisper;

import net.minidev.json.JSONArray;

import org.ethereum.facade.Ethereum;

import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/*
This class must receive notification from -core about new whisper message. Right now I not see the way to do that.
TODO: ask advice from Roman about how to send notification to this class.
*/
public class FilterManager {

    protected static FilterManager instance = null;

    public static FilterManager getInstance() {
        if (instance == null)
            instance = new FilterManager();
        return instance;
    }

    protected Hashtable<Integer, Filter> filters;
    protected int last_id = 0;

    private FilterManager() {
        filters = new Hashtable<Integer, Filter>();
    }

    public void processEvent(Object data) {
        synchronized (filters) {
            for (Map.Entry<Integer, Filter> item : filters.entrySet()) {
                item.getValue().processEvent(data);
            }
        }
    }

    public int addFilter(Filter filter) {
        filter.setId(++last_id);
        filters.put(filter.getId(), filter);
        return filter.getId();
    }

    public Filter getFilter(int id) {
        if (filters.containsKey(id)) {
            return filters.get(id);
        }
        return null;
    }

    public boolean uninstallFilter(int id) {
        synchronized (filters) {
            if (!filters.containsKey(id))
                return false;
            filters.remove(id);
        }
        return true;
    }

    public JSONArray toJS(int id) {
        synchronized (filters) {
            if (!filters.containsKey(id))
                return null;
            return filters.get(id).toJS();
        }
    }

    public JSONArray toJSAll(int id) {
        synchronized (filters) {
            if (!filters.containsKey(id))
                return null;
            return filters.get(id).toJSAll();
        }
    }

}