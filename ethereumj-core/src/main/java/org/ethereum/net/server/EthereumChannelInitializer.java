package org.ethereum.net.server;

import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.ethereum.core.Blockchain;
import org.ethereum.manager.WorldManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Roman Mandeleil
 * @since 01.11.2014
 */
public class EthereumChannelInitializer extends ChannelInitializer<NioSocketChannel> {

    private static final Logger logger = LoggerFactory.getLogger("net");

    Blockchain blockchain;

    ChannelManager channelManager;

    String remoteId;

    Provider<Channel> channelProvider;

    private boolean peerDiscoveryMode = false;

    @Inject
    public EthereumChannelInitializer(Blockchain blockchain, ChannelManager channelManager, Provider<Channel> channelProvider, String remoteId) {
		logger.info("Channel initializer instantiated");
        this.blockchain = blockchain;
        this.channelManager = channelManager;
        this.channelProvider = channelProvider;
        this.remoteId = remoteId;
    }

    @Override
    public void initChannel(NioSocketChannel ch) throws Exception {
        try {
            logger.info("Open connection, channel: {}", ch.toString());

            final Channel channel = channelProvider.get();
            channel.init(ch.pipeline(), remoteId, peerDiscoveryMode);

            if(!peerDiscoveryMode) {
                channelManager.add(channel);
            }

            // limit the size of receiving buffer to 1024
            ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(16_777_216));
            ch.config().setOption(ChannelOption.SO_RCVBUF, 16_777_216);
            ch.config().setOption(ChannelOption.SO_BACKLOG, 1024);

            // be aware of channel closing
            ch.closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!peerDiscoveryMode) {
                        channelManager.notifyDisconnect(channel);
                    }
                }
            });

        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
        }
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public void setPeerDiscoveryMode(boolean peerDiscoveryMode) {
        this.peerDiscoveryMode = peerDiscoveryMode;
    }
}