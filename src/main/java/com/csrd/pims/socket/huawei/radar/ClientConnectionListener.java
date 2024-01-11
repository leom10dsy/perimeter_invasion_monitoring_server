package com.csrd.pims.socket.huawei.radar;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ClientConnectionListener implements ChannelFutureListener {

    protected static volatile AtomicInteger RE_CONNECT_TIME = new AtomicInteger();

    private HuaweiNettyClient centerNettyClient;

    private Integer serverPort;

    private String serverIp;

    public ClientConnectionListener(String serverIp, Integer serverPort) {
        this.serverPort = serverPort;
        this.serverIp = serverIp;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {

        if (!channelFuture.isSuccess()) {
            final EventLoop loop = channelFuture.channel().eventLoop();
            loop.schedule(() -> {
                ClientConnectionListener.RE_CONNECT_TIME.getAndIncrement();
                if(ClientConnectionListener.RE_CONNECT_TIME.get() >= 1000){
                    log.error("ConnectionListener:服务端链接不上，重新连接次数超过5次，不再重新连接..." + ClientConnectionListener.RE_CONNECT_TIME.get());
                    channelFuture.channel().eventLoop().shutdownGracefully();
                    return;
                }
                log.error("ConnectionListener:服务端链接不上，开始重连操作..." + ClientConnectionListener.RE_CONNECT_TIME.get());
                if (centerNettyClient == null) {
                    centerNettyClient = new HuaweiNettyClient();
                }
                centerNettyClient.connectSocket();
                channelFuture.channel().eventLoop().shutdownGracefully();
            }, Math.min(5L * ClientConnectionListener.RE_CONNECT_TIME.get(), 600L), TimeUnit.SECONDS);
        }
    }
}
