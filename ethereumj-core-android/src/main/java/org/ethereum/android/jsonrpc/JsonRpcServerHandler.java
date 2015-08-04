package org.ethereum.android.jsonrpc;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONValue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostStandardRequestDecoder;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_HEADERS;
import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_REQUEST_METHOD;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.VARY;

public class JsonRpcServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private HttpRequest request;
    private final StringBuilder responseContent = new StringBuilder();
    private HttpPostStandardRequestDecoder decoder;
    private String postData;
    private boolean isOptions = false;
    private Dispatcher dispatcher;

    public JsonRpcServerHandler(Dispatcher dispatcher) {
        super();
        this.dispatcher = dispatcher;
    }

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
            if (request.getMethod().equals(HttpMethod.OPTIONS)) {
                isOptions = true;
                return;
            } else if (!req.getUri().equals("/") || !request.getMethod().equals(HttpMethod.POST)) {
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
                    Object tmpA = JSONValue.parse(postData);
                    if (tmpA instanceof JSONArray) {
                        JSONArray t = new JSONArray();
                        for (int i = 0; i < ((JSONArray) tmpA).size(); i++) {
                            JSONRPC2Request req = JSONRPC2Request.parse(((JSONArray) tmpA).get(i).toString());
                            JSONRPC2Response resp = dispatcher.process(req, null);
                            t.add(resp);
                        }
                        responseContent.append(t);
                    } else {
                        JSONRPC2Request req = JSONRPC2Request.parse(postData);
                        JSONRPC2Response resp = dispatcher.process(req, null);
                        responseContent.append(resp);
                    }
                    writeResponse(ctx);
                    request = null;
                    decoder.destroy();
                    decoder = null;
                }
            }
        } else if (isOptions) {
            writeOptionsResponse(ctx);
            isOptions = false;
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
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");

        if (!close) {
            response.headers().set(CONTENT_LENGTH, buf.readableBytes());
        }

        ChannelFuture future = ctx.writeAndFlush(response);
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void writeOptionsResponse(ChannelHandlerContext ctx) {
        boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.headers().get(CONNECTION))
                || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.headers().get(CONNECTION));
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type");
        response.headers().set(ACCESS_CONTROL_REQUEST_METHOD, "POST");
        response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(VARY, "Origin");

        if (!close) {
            response.headers().set(CONTENT_LENGTH, 0);
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
