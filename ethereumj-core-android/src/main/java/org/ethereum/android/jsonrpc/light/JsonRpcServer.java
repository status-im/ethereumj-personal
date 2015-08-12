package org.ethereum.android.jsonrpc.light;

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
import org.ethereum.android.jsonrpc.light.whisper.FilterManager;
import org.ethereum.facade.Ethereum;
import com.thetransactioncompany.jsonrpc2.server.*;
import org.ethereum.android.jsonrpc.light.method.*;

import java.net.URL;
import java.util.ArrayList;

import org.ethereum.android.jsonrpc.*;


public final class JsonRpcServer extends org.ethereum.android.jsonrpc.JsonRpcServer{

    static public final int PORT = 8545;
    static private ArrayList<URL> RemoteServer = new ArrayList<>();
    static private int currentRemoteServer = 0;
    static public boolean IsRemoteServerRecuring = false;

    private Ethereum ethereum;
    private Dispatcher dispatcher;

    public JsonRpcServer(Ethereum ethereum) {
        super(ethereum);
        this.ethereum = ethereum;

        this.dispatcher = new Dispatcher();

        //Custom method to receive address transaction history
        this.dispatcher.register(new eth_getTransactionHistory(this.ethereum));

        this.dispatcher.register(new eth_coinbase(this.ethereum));
        this.dispatcher.register(new eth_accounts(this.ethereum));
        this.dispatcher.register(new eth_sign(this.ethereum));
        this.dispatcher.register(new eth_sendTransaction(this.ethereum));

        this.dispatcher.register(new shh_version(this.ethereum));
        this.dispatcher.register(new shh_post(this.ethereum));
        this.dispatcher.register(new shh_newIdentity(this.ethereum));
        this.dispatcher.register(new shh_hasIdentity(this.ethereum));
        this.dispatcher.register(new shh_newGroup(this.ethereum));
        this.dispatcher.register(new shh_addToGroup(this.ethereum));
        this.dispatcher.register(new shh_newFilter(this.ethereum));
        this.dispatcher.register(new shh_uninstallFilter(this.ethereum));
        this.dispatcher.register(new shh_getFilterChanges(this.ethereum));
        this.dispatcher.register(new shh_getMessages(this.ethereum));

        this.dispatcher.register(new proxy(this.ethereum));

        FilterManager.getInstance();

        addRemoteServer("http://139.162.13.89:8545/");
    }

    public static void addRemoteServer(String address) {
        try {
            RemoteServer.add(new URL(address));
        } catch (Exception e) {
        }
    }

    public static URL getRemoteServer() {
        if (currentRemoteServer >= RemoteServer.size()){
            currentRemoteServer = 0;
            IsRemoteServerRecuring = true;
        }
        URL res = RemoteServer.get(currentRemoteServer);
        currentRemoteServer++;
        return res;
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
            p.addLast(new JsonRpcServerHandler(dispatcher));
        }
    }
}