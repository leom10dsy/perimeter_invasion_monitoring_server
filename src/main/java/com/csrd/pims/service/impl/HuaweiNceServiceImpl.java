package com.csrd.pims.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.csrd.pims.amqp.AmqpSender;
import com.csrd.pims.bean.config.HuaweiConfigParam;
import com.csrd.pims.bean.huawei.param.*;
import com.csrd.pims.bean.huawei.result.HuaweiNCELoginResponse;
import com.csrd.pims.enums.UrlEnum;
import com.csrd.pims.handler.HuaweiNceAlarmHandler;
import com.csrd.pims.service.HuaweiNceService;
import com.csrd.pims.tools.GsonUtil;
import com.csrd.pims.tools.HmsHttpUtil;
import com.csrd.pims.tools.Params;
import com.csrd.pims.tools.sse.SseClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
@Slf4j
public class HuaweiNceServiceImpl implements HuaweiNceService {

    @Autowired
    private HuaweiNceAlarmHandler huaweiNceAlarmHandler;

    @Autowired
    private HuaweiConfigParam huaweiConfigParam;

    @Autowired
    private AmqpSender amqpSender;


    public boolean login(String ip, String port, String username, String password) {
        HuaweiOFLoginInfo loginInfo = new HuaweiOFLoginInfo();
        loginInfo.setUserName(username);
        loginInfo.setGrantType("password");
        loginInfo.setValue(password);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Accept", "application/json");
        headerMap.put("Accept-Charset", "utf8");
        log.info("=====> loginInfo: {}, headerMap: {}", loginInfo, headerMap);
        HttpResponse res = HmsHttpUtil.request(UrlEnum.HUAWEI_NCE_LOGIN, loginInfo, headerMap, true, ip, port);
        if (res == null) {
            log.error("=====> nce login failed, response is null");
            return false;
        } else {
            log.info("Login Huawei NCE result status:{}", res.getStatus());
            if (res.getStatus() == 200) {
                try {
                    HuaweiNCELoginResponse huaweiNCELoginResponse = GsonUtil.fromJson(res.body(),
                            HuaweiNCELoginResponse.class);

                    Params.nceCookie = huaweiNCELoginResponse.getAccessSession();
                    Params.nceRoaRand = huaweiNCELoginResponse.getRoaRand();

                } catch (Exception e) {
                    log.error("=====> nce登录参数保存失败，res body: {}", res.body());
                }

                return true;
            } else {
                log.info("Login Huawei NCE result body:{}", res.body());
                return false;
            }
        }
    }

    public boolean logout(String ip, String port, String cookie, String roaRand) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Accept", "application/json");
        headerMap.put("Accept-Charset", "utf8");
        headerMap.put("roaRand", roaRand);
        headerMap.put("X-Auth-Token", cookie);
        HttpResponse res = HmsHttpUtil.request(UrlEnum.HUAWEI_NCE_LOGOUT, null, headerMap, true, ip, port);
        log.info("=====> nce logout status: {}, body: {}", res.getStatus(), res.body());
        return true;
    }


    public boolean addSubscription(String nceIp, String ncePort, String cookie, String roaRand) {
        HuaweiNCESubscriptionRequest subscription = new HuaweiNCESubscriptionRequest();
        List<HuaweiNCESubscriptionRequest.HuaweiNCESubscriptionInfo> list = new ArrayList<>();
        HuaweiNCESubscriptionRequest.HuaweiNCESubscriptionInfo subscriptionInfo = new HuaweiNCESubscriptionRequest.HuaweiNCESubscriptionInfo();
        HuaweiNCESubscriptionRequest.HuaweiNCEIncidentConditions incidentConditions = new HuaweiNCESubscriptionRequest.HuaweiNCEIncidentConditions();
        incidentConditions.setIncidentNames(Arrays.asList("EVENT", "FIBERPILE")).setPriorities(Arrays.asList("critical", "high")).setImpacts(Collections.emptyList()).setUrgencies(Collections.emptyList());
        subscriptionInfo.setTopic("das-resources").setIncident_conditions(incidentConditions);
        list.add(subscriptionInfo);
        subscription.setEncoding("encode-json").setProtocol("sse").setSubscription(list);
        HuaweiNCEInput<HuaweiNCESubscriptionRequest> input = new HuaweiNCEInput(subscription);
        input.setInput(subscription);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Accept", "application/json");
        headerMap.put("Accept-Charset", "utf8");
        headerMap.put("X-Auth-Token", cookie);
        log.info("=====> 订阅nce, param: {}, header: {}", input, headerMap);
        String res = HmsHttpUtil.httpClientRequest(UrlEnum.HUAWEI_NCE_SUBSCRIPTION, input, headerMap, true, nceIp, ncePort);
        log.info("=====> nce subscription body: {}", res);

        try {
            Object output = JSONUtil.parseObj(res).get("output");
            Params.nceSubscriptionResponse = GsonUtil.fromJson(output.toString(), HuaweiNCESubscriptionResponse.class);
            createSseConnection();
        } catch (Exception e) {
            log.info("=====> nce subscription log save: {}", res);
            return false;
        }

        return true;
    }


    public boolean deleteSubscription(String ip, String port, String cookie, String roaRand) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Accept", "application/json");
        headerMap.put("Accept-Charset", "utf8");
        headerMap.put("roaRand", roaRand);
        headerMap.put("X-Auth-Token", cookie);

        String identifier = Params.nceSubscriptionResponse.getIdentifier();
        JSONObject input = JSONUtil.createObj();
        JSONObject jsonObject = JSONUtil.createObj();
        jsonObject.putOpt("identifier", identifier);
        input.putOpt("input", jsonObject);
        String res = HmsHttpUtil.httpClientRequest(UrlEnum.HUAWEI_NCE_DELETE_SUBSCRIPTION, input, headerMap, true, ip, port);
        log.info("=====> nce delete subscription res: {}", res);
        Params.nceSubscriptionResponse = null;


        return true;
    }


    public void createSseConnection() {
        try {
            InputStream inputStream = SseClientUtil.getSseInputStream(huaweiConfigParam.getNce().getIp(), huaweiConfigParam.getNce().getPort(),
                    Params.nceCookie, Params.nceRoaRand, Params.nceSubscriptionResponse.getUrl());
            SseClientUtil.readStream(inputStream, (is, line) -> {
                if (StringUtils.isNotBlank(line)) {
                    Params.nceKeepTime = new DateTime();

                    if (line.toLowerCase().contains("heartbeat")) {
                        log.info("=====> sse heartbeat message: {} ,nceIP : {}", line, huaweiConfigParam.getNce().getIp());
                    } else {
                        try {

                            if(!Params.NCE_ALARM_ENABLE.get()) return;

                            HuaweiNCEAlarmInfo alarmInfo = huaweiNceAlarmHandler.analysisNCEAlarm(line);
                            if (alarmInfo != null) {
                                huaweiNceAlarmHandler.alarmHappenHandle(alarmInfo,
                                        huaweiConfigParam.getNce().getIp());
                            }
                        } catch (Exception e) {
                            log.error("=====> sse message analysis error: {}", e.getMessage());
                        }
                    }
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
