package org.ethereum.net.shh;

import org.ethereum.crypto.ECKey;
import org.ethereum.facade.Blockchain;
import org.ethereum.listener.EthereumListener;
import org.ethereum.net.MessageQueue;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.ethereum.net.eth.*;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Process the messages between peers with 'shh' capability on the network.
 *
 * Peers with 'shh' capability can send/receive:
 */
public class ShhHandler extends SimpleChannelInboundHandler<ShhMessage> {

    public final static byte VERSION = 2;
    private MessageQueue msgQueue = null;
    private ECKey privKey;

    private Whisper whisper;

    private boolean active = false;

    private final static Logger logger = LoggerFactory.getLogger("net");

    EthereumListener listener;

    @Inject
    public ShhHandler(EthereumListener listener) {
        this.listener = listener;
    }

    public ShhHandler(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }

    public void setPrivKey(ECKey privKey) {
        this.privKey = privKey;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, ShhMessage msg) throws InterruptedException {

        if (!isActive()) return;

        if (ShhMessageCodes.inRange(msg.getCommand().asByte()))
            logger.info("ShhHandler invoke: [{}]", msg.getCommand());

        listener.trace(String.format("ShhHandler invoke: [%s]", msg.getCommand()));

        switch (msg.getCommand()) {
            case STATUS:
                listener.trace("[Recv: " + msg + "]");
                break;
            case MESSAGE:
                whisper.processEnvelope((Envelope) msg);
                break;
            case ADD_FILTER:
                break;
            case REMOVE_FILTER:
                break;
            case PACKET_COUNT:
                break;
            default:
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getCause().toString());
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        active = false;
        logger.debug("handlerRemoved: ... ");
    }

    public void activate() {
        logger.info("SHH protocol activated");
        listener.trace("SHH protocol activated");
        whisper = new Whisper(msgQueue);
        sendStatus();
        this.active = true;
    }

    private void sendStatus() {
        byte protocolVersion = ShhHandler.VERSION;
        StatusMessage msg = new StatusMessage(protocolVersion);
        msgQueue.sendMessage(msg);
    }

    private void processEnvelop(Envelope envelope) {
        if (!envelope.isEmpty()) {
            Message m = envelope.open(privKey);
            logger.info("ShhHandler invoke: [{}]", m);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setMsgQueue(MessageQueue msgQueue) {
        this.msgQueue = msgQueue;
    }
}