package com.csrd.pims.tools.sse;

import com.csrd.pims.service.lambda.AnsMsgHandlerInterface;
import com.csrd.pims.tools.SslUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * sse（Server Sent Event）客户端
 */
@Slf4j
public class SseClientUtil {


    /**
     * 当前连接的nceIp
     */
    public static List<String>  list = new CopyOnWriteArrayList<>();


    /**
     * 获取SSE输入流。
     */
    public static InputStream getSseInputStream(String ip, String port, String cookie, String roaRand, String urlPath) throws Exception {
        if (urlPath.indexOf("/") == 0) urlPath = urlPath.substring(1);
        String address = "https://" + ip + ":" + port;
        urlPath = address + "/" + urlPath;
        log.info("=====> SseClientUtil url: {}", urlPath);
        URL url = new URL(urlPath);
        SslUtils.ignoreSsl();
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        // 这儿根据自己的情况选择get或post
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Connection", "Keep-Alive");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestProperty("Accept-Language", "en-US");
        //查询数据库已有的cookie;
        urlConnection.setRequestProperty("X-Auth-Token", cookie);
        //读取过期时间（很重要，建议加上）
        urlConnection.setReadTimeout(0);
        // text/plain模式
        urlConnection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
        InputStream inputStream = urlConnection.getInputStream();
        return new BufferedInputStream(inputStream);
    }

    /**
     * 读取数据。
     */
    public static void readStream(InputStream is, AnsMsgHandlerInterface ansMsgHandler) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while ((line = reader.readLine()) != null) {
                // 处理数据接口
                ansMsgHandler.actMsg(is, line);
            }
            // 当服务器端主动关闭的时候，客户端无法获取到信号。现在还不清楚原因。所以无法执行的此处。
            reader.close();
        } catch (IOException e) {
            log.error("=====> sse关闭数据流", e);
            throw new IOException("=====> 读取结束，关闭数据流！");
        }
    }


}
