package org.ethereum.net.client;

import org.ethereum.listener.EthereumListener;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.net.server.EthereumChannelInitializer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * This class creates the connection to an remote address using the Netty framework
 *
 * @see <a href="http://netty.io">http://netty.io</a>
 */
public class PeerClient {

    private static final Logger logger = LoggerFactory.getLogger("net");

    private boolean peerDiscoveryMode = false;

    EthereumListener listener;

    ChannelManager channelManager;

    Provider<EthereumChannelInitializer> ethereumChannelInitializerProvider;

    private static EventLoopGroup workerGroup = new NioEventLoopGroup(0, new ThreadFactory() {
        AtomicInteger cnt = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "EthJClientWorker-" + cnt.getAndIncrement());
        }
    });

    @Inject
	public PeerClient(EthereumListener listener, ChannelManager channelManager,
                      Provider<EthereumChannelInitializer> ethereumChannelInitializerProvider) {
        logger.info("Peer client instantiated");
        this.listener = listener;
        this.channelManager = channelManager;
        this.ethereumChannelInitializerProvider = ethereumChannelInitializerProvider;
    }

    public void connect(String host, int port, String remoteId) {
        connect(host, port, remoteId, false);
    }

    public void connect(String host, int port, String remoteId, boolean discoveryMode) {
        listener.trace("Connecting to: " + host + ":" + port);

        EthereumChannelInitializer ethereumChannelInitializer = ethereumChannelInitializerProvider.get();
        ethereumChannelInitializer.setRemoteId(remoteId);
        ethereumChannelInitializer.setPeerDiscoveryMode(discoveryMode);

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);

            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONFIG.peerConnectionTimeout());
            b.remoteAddress(host, port);

            b.handler(ethereumChannelInitializer);

            // Start the client.
            ChannelFuture f = b.connect().sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();

            if(!discoveryMode) {
                channelManager.notifyDisconnect(ethereumChannelInitializer.getChannel());
            }
            logger.debug("Connection is closed");
        } catch (Exception e) {
            if (discoveryMode) {
                logger.debug("Exception:", e);
            } else {
                logger.error("Exception:", e);
            }
        }
    }
}
