package com.csrd.pims.tools;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;

/**
 * 网络工具
 */
public class NetToolUtil {


    /**
     * 根据网段找本地ip
     *
     * @param subNet 网段
     * @return 本地ip
     */
    public static String getIpBySegment(String subNet) {

        if(StringUtils.isBlank(subNet)){
            subNet = ApplicationContextUtil.getProperty("sysconfig.local.ip");
        }

        try {

            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                // 回环口,和未启动的网口排除
                if (!netInterface.isLoopback() && netInterface.isUp()) {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip instanceof Inet4Address) {
                            String hostAddress = ip.getHostAddress();
                            // 查找
                            if (hostAddress.indexOf(subNet) == 0) {
                                return hostAddress;
                            }
                        }
                    }
                }
            }
            return subNet;
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 保存文件
     * @param inputStream 输入流
     * @param fileName 文件名
     */
    public static void copyInputStreamToFile(InputStream inputStream, String fileName) {
        String mediaPath = ApplicationContextUtil.getProperty("sysconfig.local-media-path");
        File file = new File(mediaPath);
        try {
            if(!file.exists()){
                file.mkdirs();
            }
            Files.copy(inputStream, Paths.get(mediaPath+"/"+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
