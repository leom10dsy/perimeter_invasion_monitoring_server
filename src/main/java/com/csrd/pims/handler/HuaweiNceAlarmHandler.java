package com.csrd.pims.handler;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.csrd.pims.amqp.AmqpSender;
import com.csrd.pims.amqp.tk.TKAlarmInfo;
import com.csrd.pims.bean.config.HuaweiConfigParam;
import com.csrd.pims.bean.config.TkConfigParam;
import com.csrd.pims.bean.huawei.param.HuaweiNCEAlarmInfo;
import com.csrd.pims.bean.huawei.param.PTZControlModel;
import com.csrd.pims.bean.huawei.result.HuaweiPTZPresetInfo;
import com.csrd.pims.dao.entity.HuaweiNceCamera;
import com.csrd.pims.dao.mapper.HuaweiNceCameraMapper;
import com.csrd.pims.dao.mapper.TkAlarmMapper;
import com.csrd.pims.service.HuaweiIvsMediaService;
import com.csrd.pims.service.HuaweiIvsService;
import com.csrd.pims.tools.GsonUtil;
import com.csrd.pims.tools.Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class HuaweiNceAlarmHandler {

    @Autowired
    HuaweiIvsMediaService huaweiIvsMediaService;
    @Resource
    HuaweiIvsService huaweiIvsService;

    @Resource
    private HuaweiConfigParam huaweiConfigParam;

    @Resource
    private TkConfigParam tkConfigParam;

    @Resource
    private AmqpSender amqpSender;

    @Resource
    private TkAlarmMapper tkAlarmMapper;

    @Resource
    private HuaweiNceCameraMapper huaweiNceCameraMapper;

    public HuaweiNCEAlarmInfo analysisNCEAlarm(String alarmData) {
        if (alarmData.trim().indexOf("{") != 0) {
            alarmData = "{" + alarmData + "}";
        }

        try {
            HuaweiNCEAlarmInfo alarmInfo = GsonUtil.fromJson(alarmData, HuaweiNCEAlarmInfo.class);
            if (CollectionUtil.isNotEmpty(alarmInfo.getNotifications())) {
                log.info("=====> alarmInfo: {}", alarmInfo);
                return alarmInfo;
            }
        } catch (Exception e) {
            log.error("=====> NCE alarm analysis to HuaweiNCEAlarmInfo fail");
            e.printStackTrace();
        }

        try {
            HuaweiNCEAlarmInfo.Notifications notifications = GsonUtil.fromJson(alarmData, HuaweiNCEAlarmInfo.Notifications.class);
            if (notifications != null && notifications.getData() != null) {
                HuaweiNCEAlarmInfo alarmInfo = new HuaweiNCEAlarmInfo();
                alarmInfo.setNotifications(Collections.singletonList(notifications));
//                log.info("=====> notifications: {}", notifications);
                return alarmInfo;
            }
        } catch (Exception e) {
            log.error("=====> NCE alarm analysis to notifications fail");
            e.printStackTrace();
        }

        return null;
    }

    public void alarmHappenHandle(HuaweiNCEAlarmInfo alarmInfo, String nceIp) {
        alarmInfo.getNotifications().forEach((data) -> {
            HuaweiNCEAlarmInfo.NotificationsData notificationsData = data.getData();
            if (notificationsData.getEndTime() == null && notificationsData.getFirstReportTime() != null) {

                Float offset = notificationsData.getRiskPointDistance() == null ? notificationsData.getOffset() : notificationsData.getRiskPointDistance();
                Long firstReportTime = notificationsData.getFirstReportTime();
                DateTime alarmTime = DateUtil.date(firstReportTime);
                if (offset <= 0.0F) {
                    return;
                }

                //寻找对应的距离
                HuaweiNceCamera huaweiNceCamera = null;
                List<HuaweiNceCamera> cameraList = huaweiNceCameraMapper.selectList(null);
                for (HuaweiNceCamera e : cameraList) {
                    if (e.getBegin() <= offset && e.getEnd() > offset) {
                        huaweiNceCamera = e;
                    }
                }

                log.info("收到光视nce推送报警 ， 报警ip: {} ， 报警距离 :{} , 目前{}", nceIp, offset, huaweiNceCamera == null ? "是单NCE报警" : "需要转动摄像头");

                if (huaweiNceCamera == null && huaweiConfigParam.getNce().getIsSingle()) {

                    // TODO 是否需要单nce推送报警

                    String companyAlarmDate = DateUtil.format(alarmTime, DatePattern.NORM_DATETIME_PATTERN);
                    String alarmEventId = tkConfigParam.getBase().getCompanyCode() + tkConfigParam.getBase().getNcealgorithmCode() + firstReportTime;

                    TKAlarmInfo tkAlarmInfo = new TKAlarmInfo();
                    tkAlarmInfo.setCompanyCode(tkConfigParam.getBase().getCompanyCode());
                    tkAlarmInfo.setAlgorithmCode(tkConfigParam.getBase().getNcealgorithmCode());
                    tkAlarmInfo.setCompanyAlarmDate(companyAlarmDate);
                    tkAlarmInfo.setCompanyAlarmId(DateUtil.format(alarmTime, DatePattern.PURE_DATETIME_MS_PATTERN));
                    tkAlarmInfo.setAreaCode(tkConfigParam.getBase().getNceAreaCode());
                    tkAlarmInfo.setPosition(tkConfigParam.getBase().getNcePosition());

                    tkAlarmInfo.setAlarmImage(tkConfigParam.getAmq().getImagePath() + tkConfigParam.getBase().getCompanyName() + "/" + DateUtil.format(alarmTime, DatePattern.PURE_DATE_PATTERN)
                            + "/" + tkConfigParam.getBase().getCompanyName() + "_" + tkConfigParam.getBase().getCompanyCode() + "_" + tkConfigParam.getBase().getNceAreaCode() + tkAlarmInfo.getAlarmEventId() + ".jpg");
                    tkAlarmInfo.setAlarmVideo(tkConfigParam.getAmq().getVideoPath() + tkConfigParam.getBase().getCompanyName() + "/" + DateUtil.format(alarmTime, DatePattern.PURE_DATE_PATTERN)
                            + "/" + tkConfigParam.getBase().getCompanyName() + "_" + tkConfigParam.getBase().getCompanyCode() + "_" + tkConfigParam.getBase().getNceAreaCode() + tkAlarmInfo.getAlarmEventId() + ".mp4");

                    tkAlarmInfo.setAlarmEventId(alarmEventId);

                    //解析报警对象
                    tkAlarmMapper.insert(tkAlarmInfo);
                    tkAlarmInfo.setLineInfo(tkConfigParam.getBase().getNceLineInfo())
                            .setCameraChannelId(tkConfigParam.getBase().getNceCameraChannelId())
                            .setCameraDeviceId(tkConfigParam.getBase().getNceCameraDeviceId())
                            .setDeviceId(tkConfigParam.getBase().getNceDeviceId())
                            .setAlarmState(1)
                            .setAlarmLevel(1)
                            .setAlarmType("人员,石块");
                    amqpSender.sendByRouter(tkConfigParam.getAmq().getTestMonitorPlatform(), tkConfigParam.getAmq().getAlarmMergeRoutingKey(), tkAlarmInfo);

                    return;
                }
                //寻找对应摄像头
                List<HuaweiPTZPresetInfo> ptzPresetInfoList = huaweiIvsService.getHuaweiPTZPresetInfoList(huaweiNceCamera.getCameraNumber(), huaweiConfigParam.getIvs().getNumber(), huaweiConfigParam.getIvs().getIp(), huaweiConfigParam.getIvs().getPort(), Params.ivsCookie);

                for (HuaweiPTZPresetInfo huaweiPTZPresetInfo : ptzPresetInfoList) {
                    String[] split = huaweiPTZPresetInfo.getPresetName().split("，");
                    if (offset >= (float) Integer.parseInt(split[0]) && offset < (float) Integer.parseInt(split[1])) {
                        log.info("转动预置位 , 摄像头为 ：{} ， 预置位为：{}", huaweiNceCamera.getCameraNumber(), huaweiPTZPresetInfo.getPresetIndex());

                        PTZControlModel ptzControlModel = new PTZControlModel();
                        ptzControlModel.setCameraCode(huaweiNceCamera.getCameraNumber() + "#" + huaweiConfigParam.getIvs().getNumber());
                        ptzControlModel.setControlCode(11);
                        ptzControlModel.setControlPara1(huaweiPTZPresetInfo.getPresetIndex());
                        ptzControlModel.setControlPara2("");
                        Params.nceDistance = offset;
                        boolean b = huaweiIvsService.hwPtzPoint(ptzControlModel, huaweiConfigParam.getIvs().getIp(), huaweiConfigParam.getIvs().getPort(), Params.ivsCookie);

                        if (b) {
                            try {
                                Thread.sleep(huaweiConfigParam.getNce().getTimeInterval() * 1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    }
                }
            }

        });
    }

}
