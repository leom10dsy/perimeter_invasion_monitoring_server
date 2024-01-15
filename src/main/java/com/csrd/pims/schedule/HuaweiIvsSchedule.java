package com.csrd.pims.schedule;

import cn.hutool.core.net.NetUtil;
import com.csrd.pims.amqp.AmqpSender;
import com.csrd.pims.amqp.tk.DefenceAreaPojo;
import com.csrd.pims.amqp.tk.DeviceStatePojo;
import com.csrd.pims.bean.config.HuaweiConfigParam;
import com.csrd.pims.bean.config.TkConfigParam;
import com.csrd.pims.bean.huawei.HuaweiVideoQueue;
import com.csrd.pims.dao.entity.ivs.HuaweiCamera;
import com.csrd.pims.handler.HuaweiIvsScheduleHandler;
import com.csrd.pims.service.HuaweiIvsMediaService;
import com.csrd.pims.service.HuaweiIvsService;
import com.csrd.pims.tools.GsonUtil;
import com.csrd.pims.tools.Params;
import com.csrd.pims.tools.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class HuaweiIvsSchedule {

    @Resource
    private HuaweiIvsService huaweiIvsService;

    @Resource
    private HuaweiIvsScheduleHandler huaweiIvsScheduleHandler;


    @Resource
    HuaweiIvsMediaService mediaService;

    @Resource
    private AmqpSender amqpSender;

    private final Object lock = new Object();

    @Resource
    private HuaweiConfigParam huaweiConfigParam;
    @Resource
    private TkConfigParam tkConfigParam;


    @Scheduled(cron = "0 0/1 * * * ?")
    public void huaweiIvsKeepLive() {

        if (huaweiConfigParam.getIvs().isEnable() && Params.ivsCookie != null) {
            try {
                boolean keepLive = huaweiIvsService.keepLive(huaweiConfigParam.getIvs().getIp(), huaweiConfigParam.getIvs().getPort(),
                        Params.ivsCookie);
                if (!keepLive) {
                    //重新登录

                    boolean login = huaweiIvsService.login(huaweiConfigParam.getIvs().getIp(), huaweiConfigParam.getIvs().getPort(),
                            huaweiConfigParam.getIvs().getUsername(), huaweiConfigParam.getIvs().getPassword());
                    if (login) {

                        log.info("=====> ivs login success");

                        List<HuaweiCamera> huaweiCameras = huaweiIvsService.getAllCameraByIpAndPort();

                        for (HuaweiCamera huaweiCamera : huaweiCameras) {
                            //订阅智能报警
                            String subscribeID = huaweiIvsService.addSubscribeIntelligentAlarm(huaweiConfigParam.getIvs().getIp(), huaweiConfigParam.getIvs().getPort(),
                                    Params.ivsCookie, huaweiCamera.getNumber());
                            huaweiCamera.setSubscribeFlag(1);
                            huaweiCamera.setSubscribeID(subscribeID);
                        }

                        Params.huaweiCameras = huaweiCameras;

                    }

                }
            } catch (Exception e) {
                Params.ivsCookie = null;
            }

        } else if (huaweiConfigParam.getIvs().isEnable()) {
            boolean login = huaweiIvsService.login(huaweiConfigParam.getIvs().getIp(), huaweiConfigParam.getIvs().getPort(),
                    huaweiConfigParam.getIvs().getUsername(), huaweiConfigParam.getIvs().getPassword());
            if (login) {

                log.info("=====> ivs login success");

                List<HuaweiCamera> huaweiCameras = huaweiIvsService.getAllCameraByIpAndPort();

                for (HuaweiCamera huaweiCamera : huaweiCameras) {
                    //订阅智能报警
                    String subscribeID = huaweiIvsService.addSubscribeIntelligentAlarm(huaweiConfigParam.getIvs().getIp(), huaweiConfigParam.getIvs().getPort(),
                            Params.ivsCookie, huaweiCamera.getNumber());
                    huaweiCamera.setSubscribeFlag(1);
                    huaweiCamera.setSubscribeID(subscribeID);
                }

                Params.huaweiCameras = huaweiCameras;

            }

        }
    }

    /**
     * 扫描未下载的视频，并上传
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void huaweiIvsAlarmVideoDownload() {
        synchronized (lock) {
            if (!huaweiConfigParam.getIvs().isDownload()) {
                return;
            }
            int size = 20;
            List<HuaweiVideoQueue> queues = huaweiIvsScheduleHandler.downloadVideo(size);
            if (queues.size() > 0) {
                log.info("当前下载数目为：{}", queues.size());
                for (HuaweiVideoQueue queue : queues) {
                    mediaService.downloadIvsVideo(
                            queue.getCameraNumber(),
                            queue.getAlarmEventId(),
                            queue.getAlarmVideo(),
                            queue.getAlarmTime()
                    );
                }
            }
        }
    }

    /**
     * 铁科心跳、设备状态
     */
    @Scheduled(cron = "0 0/1 * * * ?  ")
    public void sendDeviceState() {
        log.info("----> 发送设备状态给平台");
        DeviceStatePojo deviceStatePojo = new DeviceStatePojo();
        deviceStatePojo.setCompanyCode(tkConfigParam.getBase().getCompanyCode())
                .setDeviceId(tkConfigParam.getBase().getIvsDeviceId())
                .setSendTime(Utils.dateTimeToStr(new Date()));
        // 布撤防状态
        DefenceAreaPojo defenceArea = new DefenceAreaPojo();
        defenceArea.setAreaCode(tkConfigParam.getBase().getIvsAreaCode());
        defenceArea.setAreaState(Params.IVS_ALARM_ENABLE.get() ? 1 : 0);
        deviceStatePojo.setArea(Collections.singletonList(defenceArea));
        // 故障原因  0 无异常 1激光雷达故障 2毫米波雷达故障 3摄像头故障 4盒子故障 5 震动光纤多个使用,分割,初始化0
        StringBuilder builder = new StringBuilder();
        // 雷达状态
        boolean pingRadar = NetUtil.ping(huaweiConfigParam.getRadar().getIp(), 3000);
        if (!pingRadar) {
            builder.append("2,");
        }
        // 摄像机状态
        if (!Params.huaweiCameras.isEmpty()) {
            boolean pingCamera = NetUtil.ping(Params.huaweiCameras.get(0).getCameraIp(), 3000);
            if (!pingCamera) {
                builder.append("3,");
            }
        } else {
            builder.append("3,");
        }

        // ivs状态
        boolean pingIvs = NetUtil.ping(huaweiConfigParam.getIvs().getIp(), 3000);
        if (!pingIvs) {
            builder.append("4,");
        }

        String strAppend = builder.toString();
        String failureCause = "0";
        if (strAppend.endsWith(",")) {
            failureCause = strAppend.substring(0, strAppend.length() - 1);
        }
        deviceStatePojo.setFailureCause(failureCause);
        deviceStatePojo.setDeviceState(failureCause.equals("0") ? 1 : 0);
        String message = GsonUtil.toJson(deviceStatePojo);
        log.info("=====> 发送设备状态！deviceStateData:{}", message);
        amqpSender.sendByRouter(tkConfigParam.getAmq().getTestMonitorPlatform(), tkConfigParam.getAmq().getStateMergeRoutingKey(), message);
    }

}
