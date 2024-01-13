package com.csrd.pims.socket.huawei.radar;


import com.csrd.pims.bean.config.HuaweiConfigParam;
import com.csrd.pims.bean.huawei.radar.HwMMWTargetData;
import com.csrd.pims.config.huawei.HuaweiRadarConfig;
import com.csrd.pims.tools.ApplicationContextUtil;
import com.csrd.pims.tools.TcpUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 接收中心服务消息
 */
@Slf4j
public class HuaweiClientHandler extends SimpleChannelInboundHandler<HuaweiRadarMessage> {

    public static ChannelHandlerContext huaweiRadarServer = null;

    public static volatile List<HwMMWTargetData> HW_RADAR_LIST = Collections.synchronizedList(new LinkedList<>());

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HuaweiRadarMessage message) {

        if (message != null) {
            short commandType = message.getFunctionCode();
            if (commandType == 0x2006) {
                // (byte)0x50 ,(byte)0x50 ,(byte)0x44 ,(byte)0x52 ,(byte)0x6f ,(byte)0x59 ,(byte)0x61 ,(byte)0x57 ,(byte)0x3f ,(byte)0x38 ,(byte)0x77 ,(byte)0x63 ,(byte)0x67 ,(byte)0x59 ,(byte)0x78 ,(byte)0x53
                byte[] nonce = message.getData();
                // 认证密码 小端排序
                // (byte)0x9d ,(byte)0x0d ,(byte)0xaf ,(byte)0x0e ,(byte)0x3b ,(byte)0x2e ,(byte)0x31 ,(byte)0x2c ,(byte)0x19 ,(byte)0x6e ,(byte)0x29 ,(byte)0x39 ,(byte)0x87 ,(byte)0xc6 ,(byte)0xef ,(byte)0x8f ,(byte)0x17 ,(byte)0xfa ,(byte)0x8f ,(byte)0x2e ,(byte)0x73 ,(byte)0x6e ,(byte)0x22 ,(byte)0x1e ,(byte)0x61 ,(byte)0x8a ,(byte)0xc2 ,(byte)0x81 ,(byte)0x2b ,(byte)0x4f ,(byte)0x0f ,(byte)0xa9
                HuaweiConfigParam huaweiParam = ApplicationContextUtil.getBean(HuaweiConfigParam.class);
                byte[] bytes = CRC16Util.huaweiLidaSha256(huaweiParam.getRadar().getUsername(), huaweiParam.getRadar().getPassword(), nonce);
                // 发送认证指令
                byte[] head = new byte[]{(byte) 0xFD, (byte) 0xFD, (byte) 0xFD, (byte) 0xFD, (byte) 0x00, (byte) 0x01, (byte) 0x07, (byte) 0x20};
                byte[] data = new byte[head.length + bytes.length];
                System.arraycopy(head, 0, data, 0, head.length);
                System.arraycopy(bytes, 0, data, head.length, bytes.length);
                // 计算check data
                short check = CRC16Util.calcCrc16(data, 0, data.length);
                HuaweiRadarRequest request = new HuaweiRadarRequest();
                // 放入数据时，check小端排序
                request.setDataCheck(TcpUtil.bytesInverted(HuaweiRadarEncoder.shortToByteArray(check)))
                        .setTypeData(bytes)
                        .setCommandType(new byte[]{0x20, 0x07});
                ctx.channel().writeAndFlush(request);
            } else if (commandType == 0x2008) {
                byte[] result = message.getData();
                log.info("======> 雷达登录结果: {}", TcpUtil.toHexString3(result));
                if (result[0] == 0x30) {
                    HuaweiRadarRequest request = new HuaweiRadarRequest();
                    // 计算check data
                    byte[] data = new byte[]{(byte) 0xFD, (byte) 0xFD, (byte) 0xFD, (byte) 0xFD, (byte) 0x00, (byte) 0x02, (byte) 0x2A, (byte) 0x60};
                    short check = CRC16Util.calcCrc16(data, 0, data.length);
                    request.setHandle(new byte[]{0x02})
                            .setDataCheck(TcpUtil.bytesInverted(HuaweiRadarEncoder.shortToByteArray(check)))
                            .setCommandType(new byte[]{(byte) 0x60, (byte) 0x2A});
                    ctx.channel().writeAndFlush(request);
                    ClientConnectionListener.RE_CONNECT_TIME.set(0);
                    log.info("======> 登录成功后发送接收数据指令0x602A");
                    if (HuaweiRadarConfig.FAILURE_CAUSE.containsKey("radar")) {
                        HuaweiRadarConfig.FAILURE_CAUSE.remove("radar");
                    }
                }
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        synchronized (this) {
            if (huaweiRadarServer != null) {
                ctx.channel().eventLoop().shutdownGracefully();
                log.info("=====> 华为雷达已有连接, 取消本次连接...");
                return;
            }
            log.debug("=====> huaweiRadarServer client connection success!");
            huaweiRadarServer = ctx;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //log.info("====> 长时间未接收到服务器心跳");
            // 发送登录指令
            byte[] data = new byte[]{
                    (byte) 0xFD, (byte) 0xFD, (byte) 0xFD, (byte) 0xFD,
                    (byte) 0x00,
                    (byte) 0x01,
                    (byte) 0x05, (byte) 0x20
            };
            short check = CRC16Util.calcCrc16(data, 0, data.length);
            HuaweiRadarRequest request = new HuaweiRadarRequest();
            request.setDataCheck(TcpUtil.bytesInverted(HuaweiRadarEncoder.shortToByteArray(check)));
            request.setCommandType(new byte[]{0x20, 0x05});
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ctx.channel().writeAndFlush(request);
            log.debug("==============login================");
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        synchronized (this) {
            log.error("=====> huaweiRadarServer client connection out!");
            HuaweiRadarConfig.FAILURE_CAUSE.put("radar", "2");
            if (huaweiRadarServer == null) {
                return;
            }
            try {
                huaweiRadarServer = null;
            } finally {
                ctx.channel().eventLoop().shutdownGracefully();
            }
            new HuaweiNettyClient().start();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("=====> 客户端出现错误: ", cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)) {
                // 接收数据超时, 断开连接并重启
                log.error("=====> 华为雷达长时间未收到数据，触发断线重连机制...");
                this.channelInactive(ctx);
            } else if (event.state().equals(IdleState.WRITER_IDLE)) {
                // 发送数据超时
            } else if (event.state().equals(IdleState.ALL_IDLE)) {
//                log.info("ALL");
            }
        }
    }

}
