package com.csrd.pims.socket.huawei.radar;

import com.csrd.pims.bean.huawei.radar.HwMMWTargetData;
import com.csrd.pims.tools.TcpUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class HuaweiRadarDecoder extends ByteToMessageDecoder {

    public static byte[] HEADER = new byte[]{(byte) 0xFA, (byte) 0xFB, (byte) 0x00, (byte) 0x00};

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        byte[] bytes = ByteBufUtil.getBytes(byteBuf);
        //log.debug("=====> 收到雷达发送的数据：{}", TcpUtil.toHexString3(bytes));
        try {

            byte[] requestHeader = new byte[HEADER.length];
            if (byteBuf.isReadable() && byteBuf.readableBytes() > 4) {
                byteBuf.readBytes(requestHeader);
            } else {
                byteBuf.clear();
                return;
            }
            if (Arrays.equals(requestHeader, HEADER)) {
                while (byteBuf.isReadable()) {
                    int dataLength = byteBuf.readIntLE();
                    // 先验证剩余数据不小于雷达数据+check data才读取，否则不读取
                    if (byteBuf.readableBytes() < dataLength) {
                        return;
                    }
                    byteBuf.skipBytes(6);
                    HuaweiRadarMessage message = new HuaweiRadarMessage();
                    message.setFunctionCode(byteBuf.readShortLE());
                    if (message.getFunctionCode() == 0x2006) {
                        byte[] data = new byte[16];
                        byteBuf.readBytes(data);
                        message.setData(data);
                        log.debug("=====> 登陆后接收到雷达的响应数据：{}", TcpUtil.toHexString3(data));
                    } else if (message.getFunctionCode() == 0x2008) {
                        byte[] data = new byte[1];
                        byteBuf.readBytes(data);
                        message.setData(data);
                        log.debug("=====> 登陆认证后接收到雷达的登录认证结果：{}", TcpUtil.toHexString3(data));
                    } else if (message.getFunctionCode() == 0x6020) {
                        //log.debug("=====> 收到雷达数据,长度: {}", dataLength - 10);
                        // 接收数据
                        //byteBuf.skipBytes(dataLength - 10);
                        /*
                        ## 多个目标
                        19 90 序号
                        02 00 目标数2
                        00 00 00 00 无列车经过
                        00 00 73 0F 目标1 targetId(2022.10.20改为4位)
                        00 类型0
                        00 输出报警0（不发摄像头）
                        F7 FF 横坐标
                        64 00 纵坐标
                        00 00 x速度
                        FA FF y速度
                        00 00 信噪比
                        0A 00 分数
                        01 00 00 00 保留字

                        00 00 6F 0F 目标2 targetId
                        00 类型0
                        01 输出报警1（发送给摄像头）
                        F7 FF 横坐标
                        31 01 纵坐标
                        00 00 x速度
                        FF FF y速度
                        00 00 信噪比
                        0A 00 分数
                        01 00 00 00 保留字
                         */
                        // 序号
                        int serialNumber = receiveShort(byteBuf);
                        // 目标数
                        int targetNumber = receiveShort(byteBuf);
                        // 是否列车经过（0：否，其他：是）
                        int trainPass = receiveInt(byteBuf);
                        for (int i = 0; i < targetNumber; i++) {
                            int targetId = receiveInt(byteBuf);
                            byte type = byteBuf.readByte();
                            // 是否发送摄像头 输出报警0（不发摄像头），输出报警1（发送给摄像头）
                            byte sendCamera = byteBuf.readByte();
                            // F7 FF 横坐标
                            float x = (float) (receiveShort(byteBuf) * 0.1);
                            // 31 01 纵坐标
                            float y = (float) (receiveShort(byteBuf) * 0.1);
                            // 00 00 x速度
                            float xSpeed = (float) (receiveShort(byteBuf) * 0.1);
                            // FF FF y速度
                            float ySpeed = (float) (receiveShort(byteBuf) * 0.1);
                            // 00 00 信噪比
                            int SNR = receiveShort(byteBuf);
                            // 0A 00 分数
                            int score = receiveShort(byteBuf);
                            // 01 00 00 00 保留字
                            int reserved = receiveInt(byteBuf);
                            if (sendCamera != 0) {

                                HwMMWTargetData targetData = new HwMMWTargetData(targetId, type, sendCamera, x, y, xSpeed, ySpeed, SNR, score, reserved);
                                long createTime = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
                                targetData.setCreateTime(createTime);

                                // 上次保存雷达数据超过1分钟则清空数据
                                if (HuaweiClientHandler.HW_RADAR_LIST.size() > 0) {
                                    HwMMWTargetData latelyData = HuaweiClientHandler.HW_RADAR_LIST.get(HuaweiClientHandler.HW_RADAR_LIST.size() - 1);
                                    if (createTime - latelyData.getCreateTime() > 60000) {
                                        HuaweiClientHandler.HW_RADAR_LIST.clear();
                                        log.debug("=====> 华为毫米波雷达数据超过1分钟，重置数据集合, 最新数据时间戳: {}, 最近数据时间戳: {}", createTime, latelyData.getCreateTime());
                                    }

                                }

                                log.debug("=====> 华为毫米波雷达监测到目标, 目标数量: {}, targetData: {}, 此目标已发送给摄像头", targetNumber, targetData);
                                if (HuaweiClientHandler.HW_RADAR_LIST.size() >= 1200) {
                                    HuaweiClientHandler.HW_RADAR_LIST.remove(0);
                                }
                                HuaweiClientHandler.HW_RADAR_LIST.add(targetData);
                            }

                        }

                        // 跳过check data
                        byteBuf.skipBytes(2);

                    }
                    list.add(message);
                    // 粘包处理，如果还有HEADER，则跳过HEADER继续处理
                    byte[] repeatHeader = new byte[HEADER.length];
                    byteBuf.getBytes(byteBuf.readerIndex(), repeatHeader);
                    if (Arrays.equals(repeatHeader, HEADER)) {
                        byteBuf.skipBytes(HEADER.length);
                    } else {
                        return;
                    }
                }
            }

        } catch (Exception e) {
            log.error("=====> 华为雷达数据解析出错, 16进制数据为: {}", TcpUtil.toHexString3(bytes));
        } finally {
            byteBuf.clear();
//            byteBuf.release();
        }

    }

    private short receiveShort(ByteBuf byteBuf) {
        byte[] bytes = new byte[2];
        byteBuf.readBytes(bytes);
        return TcpUtil.bytesInvertedToShort(bytes);
    }

    private int receiveInt(ByteBuf byteBuf) {
        byte[] bytes = new byte[4];
        byteBuf.readBytes(bytes);
        return TcpUtil.bytesInvertedToInt(bytes);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 200; i++) {
            HwMMWTargetData targetData = new HwMMWTargetData();
            targetData.setTargetId(i);
            if (HuaweiClientHandler.HW_RADAR_LIST.size() >= 100) {
                HuaweiClientHandler.HW_RADAR_LIST.remove(0);
            }
            HuaweiClientHandler.HW_RADAR_LIST.add(targetData);
        }
        System.out.println(HuaweiClientHandler.HW_RADAR_LIST);


    }

}
