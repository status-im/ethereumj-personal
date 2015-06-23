package org.ethereum.android.jsonrpc.filter;

import net.minidev.json.JSONArray;

import org.ethereum.facade.Ethereum;

import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/*
This class must receive notification from -core about new log, Block, Transaction. Right now I not see the way todo that.
TODO: ask advice from Roman about how to send notification to this class.
*/
public class FilterManager {

    protected static FilterManager instance = null;

    public static FilterManager getInstance() {
        if (instance == null)
            instance = new FilterManager();
        return instance;
    }

    private Timer timer = null;
    private long filterAutoUninstall = TimeUnit.MINUTES.toMillis(5);

    protected Hashtable<Integer, FilterBase> filters;
    protected int last_id = 0;

    private FilterManager() {
        filters = new Hashtable<Integer, FilterBase>();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                synchronized (filters) {
                    for (Map.Entry<Integer, FilterBase> item : filters.entrySet()) {
                        if (now - item.getValue().getLastRequestTime() >= filterAutoUninstall) {
                            filters.remove(item.getKey());
                        }
                    }
                }
            }
        }, TimeUnit.MINUTES.toMillis(1));
    }

    public void processEvent(Object data) {
        synchronized (filters) {
            for (Map.Entry<Integer, FilterBase> item : filters.entrySet()) {
                item.getValue().processEvent(data);
            }
        }
    }

    public int addFilter(FilterBase filter) {
        filter.setId(++last_id);
        filters.put(filter.getId(), filter);
        return filter.getId();
    }

    public FilterBase getFilter(int id) {
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

    public JSONArray toJS(int id, Ethereum ethereum) {
        synchronized (filters) {
            if (!filters.containsKey(id))
                return null;
            return filters.get(id).toJS(ethereum);
        }
    }

}