package com.csrd.pims.tools;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.system.ApplicationHome;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ：shiwei
 * @description：
 * @date ：Created in 2020/4/27 13:27
 **/
@Slf4j
public class Utils {
    /**
     * @param type: 当前日期前，还是日期后。  0：小于当前日期  1：大于当前日期
     * @param days: 需要间隔的天数
     * @description 获取需要计算的日期
     */
    public static String calcDate(int type, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        if (type == 0) {
            c.add(Calendar.DATE, -1 * days);
        }
        if (type == 1) {
            c.add(Calendar.DATE, days);
        }
        Date date = c.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        return simpleDateFormat.format(date);
    }


    // 报警时间转化
    public static String getDateStr(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return simpleDateFormat.format(date);
    }

    // 报警时间转化
    public static String getSmsDateStr(LocalDateTime date) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return df.format(date);
    }

    public synchronized static String dateTimeToStr(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    // byte[] 转字符串
    public static String byteBufferToString(ByteBuffer buffer) {
        CharBuffer charBuffer;
        try {
            Charset charset = StandardCharsets.UTF_8;
            CharsetDecoder decoder = charset.newDecoder();
            charBuffer = decoder.decode(buffer);
            buffer.flip();
            return charBuffer.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String getStringFromInputStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] bytes = new byte[1024];
            int length;
            while ((length = inputStream.read(bytes)) > 0) {
                result.write(bytes, 0, length);
            }
            return result.toString("UTF-8");
        }
    }


    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static String uuidToFileName(String UUID) {
        return getUuidToTxtFileName(UUID);
    }

    public static String getUuidToTxtFileName(String uuId) {

        if (StringUtils.isEmpty(uuId)) {
            return "";
        }
        Pattern pattern = Pattern.compile("([a-zA-Z0-9]*)");
        Matcher matcher = pattern.matcher(uuId);
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        while (matcher.find()) {
            if (StringUtils.isNotEmpty(matcher.group())) {
                stringBuilder.append(matcher.group());
                if (i == 0) {
                    stringBuilder.append("_OBSTACLE");
                }
                i++;
            }
        }
        stringBuilder.append(".txt");
        return stringBuilder.toString();
    }


    // 里程数转K里程碑
    public static String kmToStr(int km) {
        String str = "";
        if (Math.floor(km / 1000) > 0) {
            str += "K" + Double.valueOf(Math.floor(km / 1000)).intValue();
        }
        str += "+" + Math.floorMod(km, 1000);
        return str;
    }


    public static LocalDateTime getDateByString(String dateStr) {
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return LocalDateTime.parse(dateStr, pattern);
    }

    /**
     * 获取当前时间字符串 精确到毫秒
     *
     * @return
     */
    public static String getCurrentDateMisTimeStr() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    /**
     * 获取当前时间字符串 精确到毫秒
     *
     * @return
     */
    public static String getReadableDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
    }


    /**
     * 路径规范化,去点最后的斜杆
     *
     * @param path
     * @return String file_path
     */
    public static String getStandardFilePath(String path) {
        if (!StringUtils.isEmpty(path) && path.endsWith(File.separator)) {
            int len = File.separator.length();
            path = path.substring(0, path.length() - len);
        }
        return path;
    }

    /**
     * 获取当前JAR运行目录
     *
     * @return String jar_path
     */
    public static String getJarPath() {
        try {
            ApplicationHome home = new ApplicationHome(Utils.class);
            File jarFile = home.getSource();
            return jarFile.getParentFile().toString();
        } catch (Exception e) {

        }
        return "";
    }

    /**
     * 将javabean转换为Map<String, String>
     *
     * @param bean java bean
     * @param <T>  需要转换Map<String, Object>使用 hutool BeanUtil
     */
    public static <T> Map<String, String> beanToMap(T bean) {

        Map<String, Object> beanMap = BeanUtil.beanToMap(bean);
        Map<String, String> map = new HashMap<>();
        beanMap.forEach((key, value) -> {
            // 有些key在为-但是java中不能使用-作为变量，所以替换为-
            if (Objects.nonNull(value)) {
                if (key.contains("_")) {
                    map.put(key.replace("_", "-"), String.valueOf(value));
                } else {
                    map.put(key, String.valueOf(value));
                }
            }
        });
        return map;
    }

    //  INT转大端BYTE存储
    public synchronized static byte[] intToBytes(int num) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (num >>> (24 - i * 8));
        }
        return b;
    }

    public static Date currentDateTimeMillSecStr(String dateStr) {
        try {
            if (dateStr.length() > 14) {
                dateStr = dateStr.substring(0, 14);
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            return simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            log.error("dateTime format changes error:{}", e.getMessage());
            return null;
        }
    }


    /**
     * 当前时间增加或者减少指定天数
     *
     * @param simpleDateFormatStr 指定输出时间格式字符串
     * @param index               偏移时间量
     * @return
     */
    public synchronized static String getArithmeticDayStr(String simpleDateFormatStr, int index) {
        //设置时区
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(timeZone);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(simpleDateFormatStr);
        calendar.add(Calendar.DAY_OF_MONTH, index);
        String dateStr = simpleDateFormat.format(calendar.getTime());
        return dateStr;
    }

    /**
     * byte 转2位16进制String
     *
     * @param b
     * @return
     */
    public static String toHexString(byte b) {
        return Integer.toHexString(b & 0xff | 0xffffff00).substring(6);
    }

    /**
     * 删除JVM资源占用未释放导致KVM关闭生成的崩溃日志
     */
    public static void cleanJvmErrLog() {
        File f = new File(getJarPath());
        File[] allFiles = f.listFiles();
        if (allFiles != null && allFiles.length > 0) {
            Arrays.stream(allFiles).forEach(e -> {
                if (e.getName().contains("_err_")) {
                    try {
                        FileUtils.forceDelete(e);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }


}
