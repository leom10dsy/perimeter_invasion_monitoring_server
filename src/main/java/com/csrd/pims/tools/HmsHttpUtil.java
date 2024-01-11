package com.csrd.pims.tools;

import cn.hutool.http.HttpRequest;
import com.csrd.pims.enums.UrlEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 危情监测http工具类
 */
@Slf4j
public class HmsHttpUtil {

    /**
     * 根据请求类型发送请求
     *
     * @param params       请求参数
     * @param header       请求头参数
     * @param urlEnum      请求类型
     * @param isSslRequest 是否使用https请求
     * @return response
     */
    public static cn.hutool.http.HttpResponse request(UrlEnum urlEnum, Object params, Object header, boolean isSslRequest, String ip, String port) {
        return request(UrlEnum.getRequestUrl(urlEnum, isSslRequest, ip, port), params, header, urlEnum.getRequestType());
    }

    /**
     * 根据请求类型使用httpClient发送请求
     *
     * @param params       请求参数
     * @param headerMap    请求头参数
     * @param urlEnum      请求类型
     * @param isSslRequest 是否使用https请求
     * @return response
     */
    public static String httpClientRequest(UrlEnum urlEnum, Object params, Map<String, String> headerMap, boolean isSslRequest, String ip, String port) {
        String json = Objects.nonNull(params) ? GsonUtil.toJson(params) : null;
        return doPost(UrlEnum.getRequestUrl(urlEnum, isSslRequest, ip, port), json, headerMap);
    }

    /**
     * 根据请求url和类型发送请求
     *
     * @param url         请求地址
     * @param params      请求参数
     * @param header      请求头参数
     * @param requestType 请求类型
     * @return response
     */
    public static cn.hutool.http.HttpResponse request(String url, Object params, Object header, String requestType) {
        cn.hutool.http.HttpResponse res = null;
        // 获取请求地址
        log.info("===== request url: {}", url);
        // 将参数封装为json, 如果没用则为null, 目前ContentType只支持json
        String json = Objects.nonNull(params) ? GsonUtil.toJson(params) : null;
        //log.info("=====> param:{}", json);
        Map<String, String> headerMap = Objects.isNull(header) ? new HashMap<>() : Utils.beanToMap(header);
        boolean hasContentType = false;
        for (String key : headerMap.keySet()) {
            if (key.equalsIgnoreCase("Content-Type") || key.equalsIgnoreCase("ContentType")) {
                hasContentType = true;
                break;
            }
        }

        if (!hasContentType) {
            headerMap.put("Content-Type", "application/json;charset=utf-8");
        }
        //log.info("=====> header:{}", headerMap);
        try {

            switch (requestType.toUpperCase()) {
                case "POST":
                    res = HttpRequest.post(url).headerMap(headerMap, false).body(json).execute();
                    break;
                case "PUT":
                    res = HttpRequest.put(url).headerMap(headerMap, false).body(json).execute();
                    break;
                case "GET":
                    res = HttpRequest.get(url).headerMap(headerMap, false).body(json).execute();
                    break;
                case "DELETE":
                    res = HttpRequest.delete(url).headerMap(headerMap, false).body(json).execute();
                    break;
            }
        } catch (Exception e) {
            log.error("=====> 请求失败, url: {}", url, e);
        }
        return res;
    }

    private static CloseableHttpClient httpClient;

    /**
     * 信任SSL证书
     */
    static {
        try {
            SSLContext sslContext = SSLContextBuilder.create().useProtocol(SSLConnectionSocketFactory.SSL).loadTrustMaterial((x, y) -> true).build();
            RequestConfig config = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build();
            httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).setSSLContext(sslContext).setSSLHostnameVerifier((x, y) -> true).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * apacheHttp请求
     */
    public static String doRequest(String url, String contentType, String body, Map<String, String> headerParam) {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        try {
            HttpPost httpPost = new HttpPost(url);
            StringEntity stringEntity = new StringEntity(body, "utf-8");
            stringEntity.setContentType(contentType);
            httpPost.setEntity(stringEntity);

            headerParam.forEach(httpPost::addHeader);
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpPost.abort();
                throw new RuntimeException("HttpClient is error status code :"
                        + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * post请求 发送json格式的报文 StringEntity
     *
     * @param url
     * @param body
     * @return
     */
    public static String doPost(String url, String body, Map<String, String> headerMap) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        try {
            HttpPost httpPost = new HttpPost(url);
            StringEntity stringEntity = new StringEntity(body, "utf-8");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            httpPost.addHeader("Content-Type", " application/json ");
            httpPost.addHeader("Accept", " application/json ");
            httpPost.addHeader("Accept-Language", " en-US");
            if (headerMap != null) {
                headerMap.forEach(httpPost::addHeader);
            }
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpPost.abort();
                throw new RuntimeException("HttpClient is error status code :"
                        + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String doGet(String url, Map<String, String> headerMap) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("Content-Type", " application/json ");
            httpGet.addHeader("Accept", " application/json ");
            httpGet.addHeader("Accept-Language", " en-US");
            if (headerMap != null) {
                headerMap.forEach(httpGet::addHeader);
            }
            CloseableHttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpGet.abort();
                throw new RuntimeException("HttpClient is error status code :"
                        + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加url参数
     */
    public static String addUrlParam(String url, Map<String, Object> param) {
        StringBuilder paramStr = new StringBuilder("?");

        param.forEach((key, value) -> {
            paramStr.append(key).append("=").append(value).append("&");
        });
        return url + paramStr.substring(0, paramStr.length() - 1);
    }

    /**
     * 加密参数
     */
    public static String encodeParam(String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return param;
    }

    public static void main(String[] args) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("filterBy", HmsHttpUtil.encodeParam("{\"fiberUUID\"") + ":" + HmsHttpUtil.encodeParam("\"" + "03a34ffa-7023-4196-8c10-a1e422e44c7d" + "\"}"));
        String url = UrlEnum.getRequestUrl(UrlEnum.HUAWEI_NCE_FIBER, true, "10.168.25.210", "18531");
        String httpUrl = HmsHttpUtil.addUrlParam(url, paramMap);
        System.out.println(httpUrl);
    }
}
