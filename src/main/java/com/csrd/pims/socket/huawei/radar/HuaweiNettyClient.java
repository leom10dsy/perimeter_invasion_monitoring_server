package com.csrd.pims.socket.huawei.radar;

import com.csrd.pims.bean.config.HuaweiConfigParam;
import com.csrd.pims.tools.ApplicationContextUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class HuaweiNettyClient extends Thread {

    @Override
    public void run() {
        connectSocket();
    }

    public void connectSocket() {
        // 首先，netty通过ServerBootstrap启动服务端
        Bootstrap client = new Bootstrap();

        //第1步 定义线程组，处理读写和链接事件，没有了accept事件
        EventLoopGroup group = new NioEventLoopGroup();
        client.group(group);
        //第2步 绑定客户端通道
        client.channel(NioSocketChannel.class);

        //第3步 给NIoSocketChannel初始化handler， 处理读写事件
        client.handler(new ChannelInitializer<NioSocketChannel>() {  //通道是NioSocketChannel
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                //字符串编码器，一定要加在SimpleClientHandler 的上面
                // FAFB0000 3A 00 00 00 01000000010220601990020000000000730F0000F7FF64000000FAFF00000A00010000006F0F0001F7FF31010000FFFF00000A0001000000
                //ch.config().setRecvByteBufAllocator(new AdaptiveRecvByteBufAllocator(1024 * 1024, 1024 * 1024, 1024 * 1024 * 1024));
                ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(1024 * 1024 * 1024));
                ch.pipeline()
                        //.addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, Integer.MAX_VALUE, 4,4,0,0, true))
                        .addLast("heartBeat", new IdleStateHandler(5, 0, 0))
                        .addLast("encoder", new HuaweiRadarEncoder())
                        .addLast("decoder", new HuaweiRadarDecoder())
                        .addLast(new HuaweiClientHandler());
            }
        });
        HuaweiConfigParam huaweiParam = ApplicationContextUtil.getBean(HuaweiConfigParam.class);
        if (huaweiParam == null) {
            log.error("=====> huawei radar config ip or port is null, connection failed!");
            return;
        }
        String ip = huaweiParam.getRadar().getIp();
        int port = huaweiParam.getRadar().getPort();
        //String ip = "10.168.21.201";
        //String ip = "127.0.0.1";
        //int port = 8899;
        if (StringUtils.isBlank(ip) || port <= 0) {
            log.error("=====> socket ip or port is null, connection failed!");
            return;
        }
        //连接服务器
        ChannelFuture future = client.connect(ip, port);
        future.addListener(new ClientConnectionListener(ip, port));
    }

    public static void main(String[] args) {
        new HuaweiNettyClient().start();

    }

}
