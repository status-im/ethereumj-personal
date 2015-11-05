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

    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;

    public JsonRpcServer(Ethereum ethereum) {
        super(ethereum);
        this.ethereum = ethereum;

        this.dispatcher = new Dispatcher();

        // Custom methods to receive Address Transaction History
        this.dispatcher.register(new eth_getTransactionHistory(this.ethereum));

        this.dispatcher.register(new eth_coinbase(this.ethereum));
        this.dispatcher.register(new eth_accounts(this.ethereum));
        this.dispatcher.register(new eth_sign(this.ethereum));
        this.dispatcher.register(new eth_sendTransaction(this.ethereum));

        this.dispatcher.register(new proxy(this.ethereum));

        //addRemoteServer("http://rpc0.syng.io:8545/", true);
    }

    public static void addRemoteServer(String serverUrl) {
        try {
            RemoteServer.add(new URL(serverUrl));
        } catch (Exception e) {
            System.out.println("Exception adding remote server: " + e.getMessage());

        }
    }

    public void addRemoteServer(String serverUrl, boolean clearList) {
        try {
            if (clearList) {
                RemoteServer.clear();
            }
            RemoteServer.add(new URL(serverUrl));
            System.out.println("Changed rpc remote server to: " + serverUrl);
            this.ethereum.getListener().trace("Slaving to <" + serverUrl + ">");
        } catch (Exception e) {
            System.out.println("Exception adding remote server: " + e.getMessage());
            this.ethereum.getListener().trace("Exception adding remote server: " + e.getMessage());
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
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
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

    public void stop() {
        if (bossGroup != null) {
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