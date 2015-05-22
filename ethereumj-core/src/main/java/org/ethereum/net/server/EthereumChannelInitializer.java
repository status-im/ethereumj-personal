package org.ethereum.net.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.ethereum.facade.Blockchain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;
import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Roman Mandeleil
 * @since 01.11.2014
 */
//@Component
//@Scope("prototype")
public class EthereumChannelInitializer extends ChannelInitializer<NioSocketChannel> {

    private static final Logger logger = LoggerFactory.getLogger("net");

    Blockchain blockchain;

    ChannelManager channelManager;

    String remoteId;

    @Inject
    Provider<Channel> channelProvider;

    @Inject
    public EthereumChannelInitializer(Blockchain blockchain, ChannelManager channelManager, String remoteId) {
		logger.info("Channel initializer instantiated");
        this.blockchain = blockchain;
        this.channelManager = channelManager;
        this.remoteId = remoteId;
    }

    @Override
    public void initChannel(NioSocketChannel ch) throws Exception {

        logger.info("Open connection, channel: {}", ch.toString());

        Channel channel = channelProvider.get();
        channel.init(remoteId);

        channelManager.addChannel(channel);

        ch.pipeline().addLast("readTimeoutHandler",
                new ReadTimeoutHandler(CONFIG.peerChannelReadTimeout(), TimeUnit.SECONDS));
//        ch.pipeline().addLast("in  encoder", channel.getMessageDecoder());
//        ch.pipeline().addLast("out encoder", channel.getMessageEncoder());
//        ch.pipeline().addLast(Capability.P2P, channel.getP2pHandler());
//        ch.pipeline().addLast(Capability.ETH, channel.getEthHandler());
//        ch.pipeline().addLast(Capability.SHH, channel.getShhHandler());
        ch.pipeline().addLast("initiator", channel.getMessageCodec().getInitiator());
        ch.pipeline().addLast("messageCodec", channel.getMessageCodec());

        // limit the size of receiving buffer to 1024
        ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(16_777_216));
        ch.config().setOption(ChannelOption.SO_RCVBUF, 16_777_216);
        ch.config().setOption(ChannelOption.SO_BACKLOG, 1024);
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

}