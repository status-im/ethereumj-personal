package org.ethereum.manager;


import java.util.LinkedList;
import java.util.List;

import javax.inject.Singleton;

/**
 * @author Roman Mandeleil
 * @since 11.12.2014
 */
@Singleton
public class AdminInfo {


    private long startupTimeStamp;
    private boolean consensus = true;
    private List<Long> blockExecTime = new LinkedList<>();

    public AdminInfo() {
        this.init();
    }

    public void init() {
        startupTimeStamp = System.currentTimeMillis();
    }

    public long getStartupTimeStamp() {
        return startupTimeStamp;
    }

    public boolean isConsensus() {
        return consensus;
    }

    public void lostConsensus() {
        consensus = false;
    }

    public void addBlockExecTime(long time){
        blockExecTime.add(time);
    }

    public Long getExecAvg(){

        if (blockExecTime.size() == 0) return 0L;

        long sum = 0;
        for (int i = 0; i < blockExecTime.size(); ++i){
            sum += blockExecTime.get(i);
        }

        return sum / blockExecTime.size();
    }

    public List<Long> getBlockExecTime(){
        return blockExecTime;
    }
}
