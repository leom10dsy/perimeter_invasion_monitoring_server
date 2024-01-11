package com.csrd.pims.socket.huawei.radar;

import com.csrd.pims.tools.TcpUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HuaweiRadarEncoder extends MessageToByteEncoder<HuaweiRadarRequest> {

    // fa fb 00 00 0a 00 00 00 fd fd fd fd 00 01 05 20 89 68
    // FA FB 00 00 0A 00 00 00 FD FD FD FD 00 01 05 20 89 68
    // FA FB 00 00 2A 00 00 00 FD FD FD FD 00 01 07 20 9D 0D AF 0E 3B 2E 31 2C 19 6E 29 39 87 C6 EF 8F 17 FA 8F 2E 73 6E 22 1E 61 8A C2 81 2B 4F 0F A9 36 C7
    // FA FB 00 00 2A 00 00 00 FD FD FD FD 00 01 07 20 81 88 06 0F ED 18 DA 3B 57 73 0D 06 B8 AA 64 0D A9 C2 8B B9 6F BF C1 2A 11 72 93 9D 6B 26 C2 C5 37 7D

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, HuaweiRadarRequest request, ByteBuf byteBuf) throws Exception {

        if (request == null) {
            return;
        }
        int dataLength = request.getLength();
        byteBuf.writeBytes(request.getHeader())
                .writeIntLE(dataLength)
                .writeBytes(request.getId())
                .writeBytes(request.getSendUser())
                .writeBytes(request.getHandle())
                .writeBytes(TcpUtil.bytesInverted(request.getCommandType()));

        // type_data在先，data_check在后
        if (request.getTypeData() != null && request.getTypeData().length > 0) {
            byteBuf.writeBytes(request.getTypeData());
        }

        if (request.getDataCheck() != null && request.getDataCheck().length > 0) {
            byteBuf.writeBytes(request.getDataCheck());
        }
        byte[] bytes = ByteBufUtil.getBytes(byteBuf);
        log.debug("=====> 发送的数据为: {}", TcpUtil.toHexString3(bytes));
    }

    /**
     * int转byte数组（大端）
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    /**
     * short转byte数组（大端）
     */
    public static byte[] shortToByteArray(short i) {
        byte[] result = new byte[2];
        result[0] = (byte) ((i >> 8) & 0xFF);
        result[1] = (byte) (i & 0xFF);
        return result;
    }

}
