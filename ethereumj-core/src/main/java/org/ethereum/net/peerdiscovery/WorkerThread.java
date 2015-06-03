package org.ethereum.net.peerdiscovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author Roman Mandeleil
 * @since 22.05.2014
 */
public class WorkerThread implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger("peerdiscovery");

    private PeerInfo peerInfo;
    private ThreadPoolExecutor poolExecutor;

    Provider<DiscoveryChannel> discoveryChannelProvider;

    @Inject
    public WorkerThread(Provider<DiscoveryChannel> discoveryChannelProvider) {
        this.discoveryChannelProvider = discoveryChannelProvider;
    }

    public void init(PeerInfo peer, ThreadPoolExecutor poolExecutor) {
        this.peerInfo = peer;
        this.poolExecutor = poolExecutor;
    }

    @Override
    public void run() {
        logger.debug("{} start", Thread.currentThread().getName());
        processCommand();
        logger.debug("{} end", Thread.currentThread().getName());

        sleep(1000);
        poolExecutor.execute(this);
    }

    private void processCommand() {

        try {

            DiscoveryChannel discoveryChannel = discoveryChannelProvider.get();
            discoveryChannel.connect(peerInfo.getAddress().getHostAddress(), peerInfo.getPort());
            peerInfo.setOnline(true);

            peerInfo.setHandshakeHelloMessage(discoveryChannel.getHelloHandshake());
            peerInfo.setStatusMessage(discoveryChannel.getStatusHandshake());

            logger.info("Peer is online: [{}] ", peerInfo);


        } catch (Throwable e) {
            if (peerInfo.isOnline())
                logger.info("Peer: [{}] went offline, due to: [{}]", peerInfo
                        .getAddress().getHostAddress(), e);
            peerInfo.setOnline(false);
        } finally {
            logger.info("Peer: " + peerInfo.toString() + " is "
                    + (peerInfo.isOnline() ? "online" : "offline"));
            peerInfo.setLastCheckTime(System.currentTimeMillis());

        }
    }

    private void sleep(long milliseconds) {

        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        return "Worker for: " + this.peerInfo.toString();
    }
}
