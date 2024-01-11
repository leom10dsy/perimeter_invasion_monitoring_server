package com.csrd.pims.tools;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DecimalFormat;

/**
 * 使用tcp连接发送信息
 */
@Slf4j
@Data
public class TcpUtil {

    private String ip;
    private int port;

    public TcpUtil(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void sendMessage(String message) {
        //创建Socket类对象
        Socket socket = null;

        try {
            // 连接到服务器
            socket = new Socket(ip, port);
            // 读取服务器端传过来信息的DataInputStream
            DataInputStream in = new DataInputStream(socket.getInputStream());
            // 向服务器端发送信息的DataOutputStream
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            //将客户端的信息传递给服务器
            out.writeUTF("客户端：" + message);
            // 读取来自服务器的信息
            String accpet = in.readUTF();
            //输出来自服务器的信息
            log.info(accpet);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭Socket监听
            try {
                if(socket != null){
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * byte[]-->hexString
     * 使用位运算
     */
    private static final char[] HEX_CHAR = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public static String toHexString3(byte[] bytes) {
        char[] buf = new char[bytes.length * 2];
        int index = 0;
        // 利用位运算进行转换，可以看作方法二的变型
        for (byte b : bytes) {
            buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
            buf[index++] = HEX_CHAR[b & 0xf];
        }

        return new String(buf);
    }

    /**
     * 4字节单精度浮点型数据解析
     * @param in 温湿度字节
     * @return 解析后10进制数据
     */
    public static Float deviceByteDateToFloat(byte[] in) {
        byte b1 = in[0];
        byte b2 = in[1];
        byte b3 = in[2];
        byte b4 = in[3];
        byte[] bytes = {b3, b4, b1, b2};
        byte mark = (byte) 0xff;
        if (b1 == mark && b2 == mark && b3 == mark && b4 == mark) {
            //log.info("channel is forbidden");
            return null;
        }
        // 精度两位
        DecimalFormat df = new DecimalFormat("###.00");
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
        float f = 0L;
        try {
            f = dis.readFloat();
            return Float.valueOf(df.format(f));
        } catch (IOException e) {
            log.error("convert device bytes to float error:" + e.getMessage());
            return f;
        }finally {
            try {
                dis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 将字符串转化为char类型数组
     */
    public static char[] stringToChar(String str){
        if(str == null){
            return null;
        }
        char[] result = new char[str.length()];
        for (int i = 0; i < str.length(); i++) {
            result[i] = str.charAt(i);
        }
        return result;
    }

    /**
     * float数字转byte数组，用于硬件解析
     */
    public static byte[] floatToByte(float f){
        String s = Integer.toHexString(Float.floatToIntBits(f));
        String s1 = s.substring(4,6);
        String s2 = s.substring(6,8);
        String s3 = s.substring(0,2);
        String s4 = s.substring(2,4);
        return new byte[]{(byte) Integer.parseInt(s1,16),(byte) Integer.parseInt(s2,
                16),(byte) Integer.parseInt(s3,16), (byte) Integer.parseInt(s4,16)};
    }


    /**
     * byte数组倒叙
     */
    public static byte[] bytesInverted(byte[] original) {
        byte[] target = new byte[original.length];
        for (int i = 0; i < target.length; i++) {
            target[i] = original[target.length - i - 1];
        }
        return target;
    }

    /**
     * byte数组倒叙
     */
    public static int bytesInvertedToInt(byte[] original) {
        byte[] bytes = bytesInverted(original);
        return bytes[0] << 24 | bytes[1] << 16 | bytes[2] << 8 | bytes[3] & 0xff;
    }

    /**
     * byte数组倒叙
     */
    public static short bytesInvertedToShort(byte[] original) {
        byte[] bytes = bytesInverted(original);
        return (short) ((short) bytes[0] << 8 | bytes[1] & 0xff);
    }



}
