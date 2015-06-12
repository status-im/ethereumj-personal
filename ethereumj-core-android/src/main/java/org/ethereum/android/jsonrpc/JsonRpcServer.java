package org.ethereum.android.jsonrpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostStandardRequestDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.HttpObject;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.channel.ChannelFuture;
import org.ethereum.facade.Ethereum;
import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import io.netty.handler.codec.http.HttpHeaders;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;

import org.ethereum.android.jsonrpc.method.*;


public final class JsonRpcServer {

    static final int PORT = 8545;

    private Ethereum ethereum;
    private Dispatcher dispatcher;

    public JsonRpcServer(Ethereum ethereum) {
        this.ethereum = ethereum;

        this.dispatcher = new Dispatcher();
        this.dispatcher.register(new web3_clientVersion(this.ethereum));
        this.dispatcher.register(new web3_sha3(this.ethereum));
        this.dispatcher.register(new net_version(this.ethereum));
        this.dispatcher.register(new net_listening(this.ethereum));
        this.dispatcher.register(new net_peerCount(this.ethereum));
        this.dispatcher.register(new eth_protocolVersion(this.ethereum));
        this.dispatcher.register(new eth_coinbase(this.ethereum));
        this.dispatcher.register(new eth_mining(this.ethereum));
        this.dispatcher.register(new eth_hashrate(this.ethereum));
        this.dispatcher.register(new eth_gasPrice(this.ethereum));
        this.dispatcher.register(new eth_accounts(this.ethereum));
        this.dispatcher.register(new eth_blockNumber(this.ethereum));
        this.dispatcher.register(new eth_getBalance(this.ethereum));
        this.dispatcher.register(new eth_getStorageAt(this.ethereum));
        this.dispatcher.register(new eth_getTransactionCount(this.ethereum));
        this.dispatcher.register(new eth_getBlockTransactionCountByHash(this.ethereum));
        this.dispatcher.register(new eth_getBlockTransactionCountByNumber(this.ethereum));
        this.dispatcher.register(new eth_getUncleCountByBlockHash(this.ethereum));
        this.dispatcher.register(new eth_getUncleCountByBlockNumber(this.ethereum));
        this.dispatcher.register(new eth_getCode(this.ethereum));
        this.dispatcher.register(new eth_sign(this.ethereum));
        this.dispatcher.register(new eth_sendTransaction(this.ethereum));
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
//            b.localAddress(InetAddress.getLocalHost(), PORT);
            b.localAddress(PORT);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new JsonRpcServerInitializer());

            Channel ch = b.bind().sync().channel();

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    class JsonRpcServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline p = ch.pipeline();
            p.addLast(new HttpServerCodec());
            p.addLast(new JsonRpcServerHandler());
        }
    }

    class JsonRpcServerHandler extends SimpleChannelInboundHandler<HttpObject> {

        private HttpRequest request;
        private final StringBuilder responseContent = new StringBuilder();
        private HttpPostStandardRequestDecoder decoder;
        private String postData;

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            if (decoder != null) {
                decoder.destroy();
            }
        }
        @Override
        public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
            if (msg instanceof HttpRequest) {
                HttpRequest req = this.request = (HttpRequest) msg;
                if (!req.getUri().equals("/") || !request.getMethod().equals(HttpMethod.POST)) {
                    responseContent.append("Hi, how are you?!!");
                    return;
                } else {
                    decoder = new HttpPostStandardRequestDecoder(new DefaultHttpDataFactory(false), req);
                    postData = "";
                }
            }

            if (decoder != null) {
                if (msg instanceof HttpContent) {
                    HttpContent chunk = (HttpContent) msg;
                    decoder.offer(chunk);
                    postData += chunk.content().toString(0, chunk.content().capacity(), CharsetUtil.UTF_8);

                    if (chunk instanceof LastHttpContent) {
                        JSONRPC2Request req = JSONRPC2Request.parse(postData);
                        JSONRPC2Response resp = dispatcher.process(req, null);
                        responseContent.append(resp);
                        writeResponse(ctx);
                        request = null;
                        decoder.destroy();
                        decoder = null;
                    }
                }
            } else {
                writeResponse(ctx);
            }
        }

        private void writeResponse(ChannelHandlerContext ctx) {
            ByteBuf buf = Unpooled.copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
            responseContent.setLength(0);

            boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.headers().get(CONNECTION))
                    || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                    && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.headers().get(CONNECTION));
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
            response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

            if (!close) {
                response.headers().set(CONTENT_LENGTH, buf.readableBytes());
            }

            ChannelFuture future = ctx.writeAndFlush(response);
            if (close) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}