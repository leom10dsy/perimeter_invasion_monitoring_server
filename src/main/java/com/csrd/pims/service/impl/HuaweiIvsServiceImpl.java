package com.csrd.pims.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csrd.pims.amqp.AmqpSender;
import com.csrd.pims.amqp.tk.TKAlarmInfo;
import com.csrd.pims.bean.config.HuaweiConfigParam;
import com.csrd.pims.bean.config.TkConfigParam;
import com.csrd.pims.bean.huawei.bean.HWAlarmInfo;
import com.csrd.pims.bean.huawei.param.*;
import com.csrd.pims.bean.huawei.result.HuaweiPTZPresetInfo;
import com.csrd.pims.bean.huawei.result.RestFulApiResult;
import com.csrd.pims.bean.huawei.result.Result;
import com.csrd.pims.bean.huawei.result.RtspUrlVideoResult;
import com.csrd.pims.bean.web.HmsHeader;
import com.csrd.pims.bean.web.HuaweiIvsDispositionNotificationCallback;
import com.csrd.pims.dao.entity.ivs.HuaweiCamera;
import com.csrd.pims.dao.mapper.TkAlarmMapper;
import com.csrd.pims.enums.AlarmStateEnum;
import com.csrd.pims.enums.UrlEnum;
import com.csrd.pims.handler.HuaweiIvsAlarmHandler;
import com.csrd.pims.service.HuaweiIvsMediaService;
import com.csrd.pims.service.HuaweiIvsService;
import com.csrd.pims.tools.GsonUtil;
import com.csrd.pims.tools.HmsHttpUtil;
import com.csrd.pims.tools.NetToolUtil;
import com.csrd.pims.tools.Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class HuaweiIvsServiceImpl implements HuaweiIvsService {


    @Resource
    private HuaweiConfigParam huaweiConfigParam;
    @Resource
    private HuaweiIvsAlarmHandler ivsAlarmHandler;

    @Resource
    private TkAlarmMapper tkAlarmMapper;
    @Resource
    private AmqpSender amqpSender;
    @Resource
    private TkConfigParam tkConfigParam;
    @Resource
    private HuaweiIvsMediaService mediaService;


    public boolean keepLive(String ivsIp, String ivsPort, String cookie) {
        HmsHeader hmsHeader = new HmsHeader(cookie, null);
        HttpResponse res = HmsHttpUtil.request(UrlEnum.HUAWEI_IVS_KEEP_LIVE, null, hmsHeader, true, ivsIp, ivsPort);
        String response = new String(res.bodyBytes(), StandardCharsets.UTF_8);
        int resultCode = JSONUtil.parseObj(response).getInt("resultCode");
        log.info("=====> ivs session: {}, Huawei IVS KeepLive Code:{}", cookie, resultCode);
        return resultCode == 0;
    }


    public List<HuaweiCamera> getAllCameraByIpAndPort() {
        List<HuaweiCamera> huaweiCameraList = new ArrayList<>();
        HmsHeader hmsHeader = new HmsHeader(Params.ivsCookie, null);
        HttpResponse res = HmsHttpUtil.request(UrlEnum.HUAWEI_IVS_DEVICE_LIST, null, hmsHeader, true,
                huaweiConfigParam.getIvs().getIp(), huaweiConfigParam.getIvs().getPort());
        JSONObject jsonObject = (new JSONObject(res.body())).getJSONObject("cameraBriefInfos");
        JSONArray array = jsonObject.getJSONArray("cameraBriefInfoList");
        if (array.size() >= 1) {
            for (int i = 0; i < array.size(); ++i) {
                JSONObject data = array.getJSONObject(i);
                HuaweiCamera huaweiCamera = new HuaweiCamera();
                huaweiCamera.setCameraIp(data.getStr("deviceIP"));
                huaweiCamera.setCameraName(data.getStr("name"));
                huaweiCamera.setIvsNumber(huaweiConfigParam.getIvs().getNumber());
                huaweiCamera.setNumber(data.getStr("code"));
                huaweiCamera.setScadaNodeId("video");
                huaweiCamera.setCameraType("2");

                //设置状态为正常
                if (data.getInt("status") == 0) {
                    continue;
                }
                huaweiCamera.setState(1);


                huaweiCameraList.add(huaweiCamera);
            }
        } else {
            log.info("当前ivs下面没有摄像头");
        }
        return huaweiCameraList;
    }


    @Override
    public String getRealRtspurl(String cameraNumber) {
        return getVideoRtspUrl(cameraNumber, null, 1);
    }

    public boolean hwPtzPoint(PTZControlModel ptzControlModel, String ip, String port, String cookie) {
        boolean result = false;

        try {
            HmsHeader hmsHeader = new HmsHeader(cookie, null);
            HttpResponse response = HmsHttpUtil.request(UrlEnum.HUAWEI_IVS_DEVICE_PTZ_CONTROL, ptzControlModel, hmsHeader, true, ip, port);
            String res = response.body();
            int resultCode = JSONUtil.parseObj(res).getInt("resultCode");
            if (resultCode == 0) {
                log.info("转动预置位成功：{}", ptzControlModel.toString());
                result = true;
            } else {
                log.error("转到预置点失败，错误代码为:{}", resultCode);
            }
        } catch (Exception e) {
            log.error("转预置点异常", e);
        }

        return result;
    }


    @Override
    public String getVideoRtspUrl(String cameraNumber, Date alarmTime, int type) {
        RtspUrlApiModel getUrl_data = new RtspUrlApiModel();
        getUrl_data.setCameraCode(cameraNumber + "#" + huaweiConfigParam.getIvs().getNumber());
        MediaURL mediaURL = new MediaURL();
        mediaURL.setBroadCastType(0);
        mediaURL.setProtocolType(2);
        mediaURL.setServiceType(type);
        mediaURL.setStreamType(1);
        mediaURL.setTransMode(0);
        mediaURL.setClientType(1);
        mediaURL.setPackProtocolType(1);
        if (alarmTime != null) {
            TimeSpan timeSpan = new TimeSpan();

            Calendar calendarStart = Calendar.getInstance();
            calendarStart.setTime(alarmTime);
            calendarStart.add(Calendar.HOUR, -8);
            calendarStart.add(Calendar.SECOND, -20);

            Calendar calendarEnd = Calendar.getInstance();
            calendarEnd.setTime(alarmTime);
            calendarEnd.add(Calendar.HOUR, -8);
            calendarEnd.add(Calendar.SECOND, -15);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String timeStart = sdf.format(calendarStart.getTime());
            String timeEnd = sdf.format(calendarEnd.getTime());
            timeSpan.setStartTime(timeStart);
            timeSpan.setEndTime(timeEnd);
            mediaURL.setTimeSpan(timeSpan);
        }

        getUrl_data.setMediaURLParam(mediaURL);
        String url = "https://" + huaweiConfigParam.getIvs().getIp() + ":" + huaweiConfigParam.getIvs().getPort()
                + "/video/rtspurl/v1.0";
        String json = JSONUtil.toJsonStr(getUrl_data);
        String res = HttpRequest.post(url).header("Cookie", Params.ivsCookie).body(json).execute().body();
        RtspUrlVideoResult videoUrl = JSONUtil.toBean(res, RtspUrlVideoResult.class);//录像URL
        if (videoUrl.getResultCode() != 0) {
            log.warn("获取流失败，错误码：{}", videoUrl.getResultCode());
            return null;
        }
        return videoUrl.getRtspURL();
    }

    @Override
    public String getStream(String ip, String port, String cameraNumber, int type, Date alarmTime) {

        DateTime dateTime = DateTime.of(alarmTime);
        if (type == 1) {
            return "rtsp://" + ip + ":" + port + "/" + cameraNumber + "?DstCode=01&ServiceType=1&ClientType=1&StreamID=1&SrcTP=2&DstTP=2&SrcPP=1&DstPP=1&MediaTransMode=0&BroadcastType=0&SV=1";
        }

        String timeStart = DateUtil.format(dateTime.offsetNew(DateField.HOUR, -8).offsetNew(DateField.SECOND, -20),
                DatePattern.PURE_DATETIME_PATTERN);
        String timeEnd = DateUtil.format(dateTime.offsetNew(DateField.HOUR, -8).offsetNew(DateField.SECOND, 15),
                DatePattern.PURE_DATETIME_PATTERN);

        return "rtsp://" + ip + ":" + port + "/" + cameraNumber +
                "?DstCode=01&ServiceType=4&ClientType=1&StreamID=1&SrcTP=2&DstTP=2&SrcPP=1&DstPP=1&MediaTransMode=0&BroadcastType=0&SV=1&TimeSpan="
                + timeStart.substring(0, 8) + "T" + timeStart.substring(8) + "Z-" +
                timeEnd.substring(0, 8) + "T" + timeEnd.substring(8) + "Z";
    }

    public String addSubscribeIntelligentAlarm(String ip, String port, String cookie, String cameraNumber) {
        String result = "";
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Cookie", cookie);
        headerMap.put("ContentType", "application/json");
        Map<Object, Object> params = new HashMap<>();
        Map<Object, Object> subscribeListObject = new HashMap<>();
        List<Object> subscribeObject = new ArrayList<>();
        Map<Object, Object> body = new HashMap<>();
        params.put("SubscribeDetail", "86");
        params.put("ResourceURI", cameraNumber);
        String ipByNet = NetToolUtil.getIpBySegment("");
        String callbackUrl = "https://" + ipByNet + ":443/huaweifront/ivs/intelligentAlarmCallback";
        params.put("ReceiveAddr", callbackUrl);
        log.info("=====> callbackUrl:{}", callbackUrl);
        params.put("CodeType", 0);
        params.put("ResultImgType", 3);
        params.put("Token", cookie);
        subscribeObject.add(params);
        subscribeListObject.put("SubscribeObject", subscribeObject);
        body.put("SubscribeListObject", subscribeListObject);
        HttpResponse response = HmsHttpUtil.request(UrlEnum.HUAWEI_IVS_INTELLIGENT_SUBSCRIBE_ALARM_ADD, body, headerMap, true, ip, port);
        String string = new String(response.bodyBytes(), StandardCharsets.UTF_8);
        JSONObject jsonObject = new JSONObject(string);
        if (jsonObject.getInt("resultCode") == 0) {
            JSONArray array = jsonObject.getJSONArray("resultInfoList");
            JSONObject jsonObject1 = (JSONObject) array.get(0);
            result = String.valueOf(jsonObject1.getStr("SubscribeID"));
        } else {
            log.error("{}智能订阅报警报错，错误代码为:{}", cameraNumber, jsonObject.getInt("resultCode"));
        }

        return result;
    }


    public Boolean deleteSubscribeIntelligentAlarm(String ip, String port, String cookie, String subscribeID) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Cookie", cookie);
        headerMap.put("ContentType", "application/json");
        Map<Object, Object> params = new HashMap<>();
        List<Object> SubscribeIDList = new ArrayList<>();
        Map<Object, Object> body = new HashMap<>();
        params.put("SubscribeID", subscribeID);
        SubscribeIDList.add(params);
        body.put("SubscribeIDList", SubscribeIDList);
        HttpResponse response = HmsHttpUtil.request(UrlEnum.HUAWEI_IVS_INTELLIGENT_SUBSCRIBE_ALARM_DELETE, body, headerMap, true, ip, port);
        String string = new String(response.bodyBytes(), StandardCharsets.UTF_8);
        Result result = JSONUtil.toBean(string, Result.class);
        return result.getCode() == 0;
    }

    public void huaweiIvsIntelligentAlarmHandle(String callbackVo) {
        HuaweiIvsDispositionNotificationCallback huaweiIvsDispositionNotificationCallback = JSONUtil.toBean(callbackVo, HuaweiIvsDispositionNotificationCallback.class);

        ivsAlarmHandler.huaweiIvsIntelligentAlarmHandle(huaweiIvsDispositionNotificationCallback);


    }


    public boolean login(String ip, String port, String userName, String password) {
        boolean loginResultRest = false;
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setUserName(userName);
        loginInfo.setPassword(password);
        HttpResponse res = HmsHttpUtil.request(UrlEnum.HUAWEI_IVS_LOGIN, loginInfo, null, true, ip, port);
        String result = res.body();
        RestFulApiResult loginResult = JSONUtil.toBean(result, RestFulApiResult.class);
        if (loginResult.getCode() == 0) {
            String cookie = res.header("Set-Cookie");
            if (cookie != null && cookie.length() > 0) {
                Params.ivsCookie = cookie.split(";")[0];
                loginResultRest = true;
                log.info("=====>localPath:{}, ivs cookie:{}", Params.LOCAL_IP, Params.ivsCookie);
            }
        } else {
            log.info("Login Huawei IVS Failed,Code:{}, body: {}", res.getStatus(), res.body());
        }

        return loginResultRest;
    }

    public boolean logout(String ip, String port, String cookie) {
        HmsHeader hmsHeader = new HmsHeader(cookie, null);
        HttpResponse res = HmsHttpUtil.request(UrlEnum.HUAWEI_IVS_LOGOUT, null, hmsHeader, true, ip, port);
        String response = new String(res.bodyBytes(), StandardCharsets.UTF_8);
        RestFulApiResult loginResult = JSONUtil.toBean(response, RestFulApiResult.class);
        log.info("=====> ivs session: {}, Huawei IVS logout Code:{}", cookie, loginResult.getCode());
        return loginResult.getCode() == 0;
    }

    public List<HuaweiPTZPresetInfo> getHuaweiPTZPresetInfoList(String cameraNumber, String ivsNumber, String ip, String port, String cookie) {
        HmsHeader hmsHeader = new HmsHeader(cookie, null);
        String url = "https://" + ip + ":" + port + UrlEnum.HUAWEI_IVS_PTZPRESET_LIST.getUri() + "/" + cameraNumber + "/" + ivsNumber;
        HttpResponse res = HmsHttpUtil.request(url, null, hmsHeader, "get");
        String string = new String(res.bodyBytes(), StandardCharsets.UTF_8);
        JSONObject jsonObject = new JSONObject(string);
        if (jsonObject.getInt("resultCode") == 0) {
            JSONObject infoList = jsonObject.getJSONObject("ptzPresetInfoList");
            JSONArray ptzPresetInfo = infoList.getJSONArray("ptzPresetInfo");
            log.info("获得预置位列表成功 ， 预置位列表为：{}", ptzPresetInfo);
            return JSONUtil.toList(ptzPresetInfo, HuaweiPTZPresetInfo.class);

        } else {
            return null;
        }
    }

    /**
     * 推送结束报警
     *
     * @param eventPrefix
     * @param currentDate
     */
    @Override
    public void pushCloseAlarm(String eventPrefix, Date currentDate) {
        // 避免主键重复
        Date lastEndTime = DateUtil.offsetMillisecond(currentDate, -10);
        HWAlarmInfo hwAlarmInfo = Params.LATEST_ALARM_TIME.get(eventPrefix);
        try {
            LambdaQueryWrapper<TKAlarmInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TKAlarmInfo::getAlarmEventId, hwAlarmInfo.getEventId());
            TKAlarmInfo tkAlarmInfo = tkAlarmMapper.selectList(wrapper).get(0);
            // 抓拍、上传图片
            HuaweiCamera huaweiCamera = Params.huaweiCameras.get(0);
            // 本地文件名
            String localImgName = mediaService.capturePic(huaweiCamera.getNumber());
            String localImgPath = "D:\\img\\test";
            String localImg = localImgPath + File.separator + localImgName;
            log.info("alarmImg:{},localImg:{}", tkAlarmInfo.getAlarmImage(), localImg);
            String alarmImage = mediaService.uploadImg(tkAlarmInfo, localImg);
            String companyAlarmId = DateUtil.format(lastEndTime, DatePattern.PURE_DATETIME_MS_PATTERN) + "_" + tkConfigParam.getBase().getCompanyCode();
            tkAlarmInfo.setCompanyAlarmId(companyAlarmId)
                    .setAlarmState(AlarmStateEnum.CLOSE.getValue())
                    .setAlarmImage(alarmImage)
                    .setIsPushAlarm(0)
                    .setCompanyAlarmDate(DateUtil.format(lastEndTime, DatePattern.NORM_DATETIME_PATTERN));
            tkAlarmMapper.insert(tkAlarmInfo);
            String message = GsonUtil.toJson(tkAlarmInfo);
            log.info("=====> 推送结束报警，alarmData：{}", message);
            try {
                amqpSender.sendByRouter(tkConfigParam.getAmq().getTestMonitorPlatform(), tkConfigParam.getAmq().getAlarmMergeRoutingKey(), message);
            } catch (Exception e) {
                log.error("=====> 推送结束报警异常！,{}", e.getMessage());
                return;
            }
            tkAlarmInfo.setIsPushAlarm(1);
            tkAlarmMapper.updateById(tkAlarmInfo);
        } catch (Exception e) {
            log.error("=====> 推送结束报警失败！cause：{}", e.getMessage());
        }
    }


}
