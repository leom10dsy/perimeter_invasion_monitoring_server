package com.csrd.pims.handler;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.csrd.pims.amqp.AmqpSender;
import com.csrd.pims.amqp.tk.TKAlarmInfo;
import com.csrd.pims.bean.config.TkConfigParam;
import com.csrd.pims.bean.huawei.bean.HWAlarmInfo;
import com.csrd.pims.bean.huawei.radar.HwMMWTargetData;
import com.csrd.pims.bean.web.HuaweiIvsDispositionNotificationCallback;
import com.csrd.pims.dao.entity.HuaweiNceCamera;
import com.csrd.pims.dao.entity.ivs.HuaweiCamera;
import com.csrd.pims.dao.entity.nce.HuaweiNceAlarmDistance;
import com.csrd.pims.dao.entity.nce.HuaweiNceDefaultDistance;
import com.csrd.pims.dao.mapper.HuaweiNceAlarmDistanceMapper;
import com.csrd.pims.dao.mapper.HuaweiNceCameraMapper;
import com.csrd.pims.dao.mapper.HuaweiNceDefaultDistanceMapper;
import com.csrd.pims.dao.mapper.TkAlarmMapper;
import com.csrd.pims.enums.AlarmStateEnum;
import com.csrd.pims.service.HuaweiIvsMediaService;
import com.csrd.pims.service.HuaweiIvsService;
import com.csrd.pims.socket.huawei.radar.HuaweiClientHandler;
import com.csrd.pims.tools.GsonUtil;
import com.csrd.pims.tools.Params;
import com.csrd.pims.tools.SftpUtils;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
@Transactional
public class HuaweiIvsAlarmHandler {

    @Resource
    private HuaweiIvsMediaService huaweiIvsMediaService;

    @Resource
    private TkConfigParam tkConfigParam;

    @Resource
    private AmqpSender amqpSender;

    @Resource
    private TkAlarmMapper tkAlarmMapper;

    @Resource
    private HuaweiNceCameraMapper huaweiNceCameraMapper;

    @Resource
    private HuaweiNceAlarmDistanceMapper huaweiNceAlarmDistanceMapper;

    @Resource
    private HuaweiNceDefaultDistanceMapper huaweiNceDefaultDistanceMapper;
    @Resource
    private HuaweiIvsService ivsService;


    public synchronized void huaweiIvsIntelligentAlarmHandle(HuaweiIvsDispositionNotificationCallback callbackVo) {
        String eventPrefix = "";
        HuaweiIvsDispositionNotificationCallback.DispositionNotificationObject notificationObject = callbackVo.getDispositionNotificationListObject().getDispositionNotificationObject().get(0);
        String companyAlarmId = notificationObject.getTriggerTime() + "_" + tkConfigParam.getBase().getCompanyCode();

        String subscribeID = notificationObject.getSubscribeID();

        HuaweiCamera huaweiCamera = null;

        for (HuaweiCamera e : Params.huaweiCameras) {
            if (e.getSubscribeID().equals(subscribeID)) {
                huaweiCamera = e;
            }
        }
        if (huaweiCamera == null) {
            log.warn("收到报警 ， 但是订阅标识{}无法判定 ", subscribeID);
            return;
        }

        Date alarmTime = DateUtil.parse(notificationObject.getTriggerTime(), DatePattern.PURE_DATETIME_MS_PATTERN);
        log.info("告警时间：{}", alarmTime);

        // 2022-04-15 15:49:00
        String companyAlarmDate = DateUtil.format(alarmTime, DatePattern.NORM_DATETIME_PATTERN);
        String alarmEventId;

        TKAlarmInfo tkAlarmInfo = new TKAlarmInfo();
        tkAlarmInfo.setCompanyAlarmId(companyAlarmId);
        tkAlarmInfo.setCompanyCode(tkConfigParam.getBase().getCompanyCode());
        tkAlarmInfo.setCompanyAlarmDate(companyAlarmDate);


        //解析报警对象
        //雷视报警才获取点云轨迹
        //区分雷视报警
        HuaweiNceCamera huaweiNceCamera = null;

        List<HuaweiNceCamera> cameraList = huaweiNceCameraMapper.selectList(null);
        if (cameraList == null) {
            cameraList = new ArrayList<>();
        }
        for (HuaweiNceCamera e : cameraList) {
            if (StrUtil.equals(e.getCameraNumber(), huaweiCamera.getNumber())) {
                huaweiNceCamera = e;
            }
        }
        String imageName = "";
        List<HwMMWTargetData> hwMMWTargetData = null;
        if (huaweiNceCamera != null) {
            // 光视处理
            eventPrefix = tkConfigParam.getBase().getCompanyCode() + tkConfigParam.getBase().getNcealgorithmCode() + tkConfigParam.getBase().getNceAreaCode();
            // 判断是否超时，需要先推送上一条报警的结束状态
            pushLastAlarmIfTimeout(eventPrefix, alarmTime);
            if (!Params.NCE_ALARM_ENABLE.get()) {
                log.info("nce撤防 -- 光视不触发报警");
                return;
            }
            alarmEventId = tkConfigParam.getBase().getCompanyCode() + tkConfigParam.getBase().getNcealgorithmCode() + alarmTime.getTime();
            nceAlarmHandler(eventPrefix, alarmEventId, alarmTime, huaweiNceCamera, tkAlarmInfo, notificationObject);
            imageName = tkConfigParam.getBase().getCompanyName() + "_" + tkConfigParam.getBase().getNcealgorithmCode() + "_" +
                    tkConfigParam.getBase().getNceAreaCode() + "_" + notificationObject.getTriggerTime() + ".jpg";
        } else {
            // 雷视处理
            eventPrefix = tkConfigParam.getBase().getCompanyCode() + tkConfigParam.getBase().getIvsalgorithmCode() + tkConfigParam.getBase().getIvsAreaCode();
            pushLastAlarmIfTimeout(eventPrefix, alarmTime);
            alarmEventId = tkConfigParam.getBase().getCompanyCode() + tkConfigParam.getBase().getIvsalgorithmCode() + alarmTime.getTime();
            if (!Params.IVS_ALARM_ENABLE.get()) {
                log.info("ivs撤防 -- 雷视不触发报警");
                return;
            }
            ivsAlarmHandler(alarmEventId, eventPrefix, alarmTime, tkAlarmInfo, notificationObject);
            //获取雷达数据
            hwMMWTargetData = HuaweiClientHandler.HW_RADAR_LIST.stream().filter(
                    s -> s.getTargetId() == notificationObject.getBehaviorAnalysisObject().getRadarTargetID()).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(hwMMWTargetData)) {
                log.info("雷达轨迹没有找到");
            }
            tkAlarmInfo.setPosition(tkConfigParam.getBase().getIvsPosition() + "+" + (CollectionUtils.isEmpty(hwMMWTargetData) ? 100 : Math.round(hwMMWTargetData.get(hwMMWTargetData.size() - 1).getTargetY())));
            imageName = tkConfigParam.getBase().getCompanyName() + "_" + tkConfigParam.getBase().getIvsalgorithmCode() + "_" +
                    tkConfigParam.getBase().getIvsAreaCode() + "_" + notificationObject.getTriggerTime() + ".jpg";
        }

        //添加视频
        if (Params.LATEST_ALARM_TIME.containsKey(eventPrefix)) {
            HWAlarmInfo hwAlarmInfo = Params.LATEST_ALARM_TIME.get(eventPrefix);
            if (!hwAlarmInfo.isAddDownloadQueue()) {
                // 没有添加下载视频队列

                //String videoPath = tkConfigParam.getSftp().getVideoPath() + tkConfigParam.getBase().getCompanyName() + "/" + DateUtil.format(alarmTime, DatePattern.PURE_DATE_PATTERN);
                String alarmVideo = tkAlarmInfo.getAlarmVideo();
                String videoPath = "/home" + alarmVideo.substring(0, alarmVideo.lastIndexOf("/") + 1);
                String uploadVideoName = alarmVideo.substring(alarmVideo.lastIndexOf("/") + 1);
                huaweiIvsMediaService.addDownloadAlarmIvsVideoQueue(huaweiCamera.getNumber(), hwAlarmInfo.getEventId(), videoPath, alarmTime, uploadVideoName);
                hwAlarmInfo.setAddDownloadQueue(true);
            }
        }

        //下载图片
        byte[] base64 = Base64.decodeBase64(notificationObject.getBehaviorAnalysisObject().getSubImageList().getSubImageInfoObject().get(0).getData());

        String s_recordImgSavePath = Params.LOCAL_STORAGE_PATH + "img" + File.separator;
        // 图片保存路径
        String targetImgName = s_recordImgSavePath + alarmEventId + ".jpg"; // 默认保存路径
        if (!Files.exists(Paths.get(s_recordImgSavePath))) {
            try {
                Files.createDirectories(Paths.get(s_recordImgSavePath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {

            FileOutputStream outputStream = new FileOutputStream(targetImgName);
            outputStream.write(base64);
            log.info("=====> 图片下载成功{}", targetImgName);

            // 上传文件到远程
            SftpUtils sftpUtils = new SftpUtils();
            TkConfigParam.Sftp sftp = tkConfigParam.getSftp();
            boolean b = sftpUtils.login(sftp.getUsername(), sftp.getPassword(), sftp.getHost(), sftp.getPort(), null);
            if (b) {
                File targetFile = new File(targetImgName);
                InputStream inputStream = Files.newInputStream(targetFile.toPath());
                try {
                    String imgPath = tkConfigParam.getSftp().getImagePath() + tkConfigParam.getBase().getCompanyName() + "/" + DateUtil.format(alarmTime, DatePattern.PURE_DATE_PATTERN);
                    log.info("imgPath:{},imgName:{}", imgPath, imageName);
                    sftpUtils.upload(imgPath, imageName, inputStream);
                } catch (SftpException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            log.info("=====> 图片下载失败");
        }

        if (Params.LATEST_ALARM_TIME.containsKey(eventPrefix)) {
            HWAlarmInfo hwAlarmInfo = Params.LATEST_ALARM_TIME.get(eventPrefix);
            if (hwAlarmInfo.getAlarmLevel() == 0) {
                log.info("=====> 插入新的报警");
                tkAlarmMapper.insert(tkAlarmInfo);
                hwAlarmInfo.setAlarmLevel(1);
                hwAlarmInfo.setAlarmState(AlarmStateEnum.START.getValue());
                Params.LATEST_ALARM_TIME.put(eventPrefix, hwAlarmInfo);
            } else if (hwAlarmInfo.getAlarmLevel() == 1) {
                log.info("=====> 插入进行中的报警");
                tkAlarmMapper.insert(tkAlarmInfo);
                hwAlarmInfo.setAlarmLevel(2);
                hwAlarmInfo.setAlarmState(AlarmStateEnum.MIDDLE.getValue());
                Params.LATEST_ALARM_TIME.put(eventPrefix, hwAlarmInfo);
            } else {
                //alarmLevel() == 2
                //长期运行不推送
//                log.info("=====> 长期运行，不推送进行中的报警");
//                return;

                tkAlarmMapper.insert(tkAlarmInfo);
                //模拟测试继续推送
                //log.info("=====> 模拟测试，继续推送进行中的报警");
            }
        }
        /*
         * 2023年11月,铁科新需求增加字段
         * private String lineInfo;
         * private String cameraChannelId;
         * private String cameraDeviceId;
         * private String deviceId;
         * private Integer alarmState;
         * private Integer alarmLevel;
         * private String alarmType;
         */
        if (huaweiNceCamera != null) {
            // 光视
            tkAlarmInfo.setLineInfo(tkConfigParam.getBase().getNceLineInfo())
                    .setCameraChannelId(tkConfigParam.getBase().getNceCameraChannelId())
                    .setCameraDeviceId(tkConfigParam.getBase().getNceCameraDeviceId())
                    .setDeviceId(tkConfigParam.getBase().getNceDeviceId())
                    .setAlarmLevel(1)
                    .setAlarmType("人员,石块");
        } else {
            // 雷视
            tkAlarmInfo.setLineInfo(tkConfigParam.getBase().getIvsLineInfo())
                    .setCameraChannelId(tkConfigParam.getBase().getIvsCameraChannelId())
                    .setCameraDeviceId(tkConfigParam.getBase().getIvsCameraDeviceId())
                    .setDeviceId(tkConfigParam.getBase().getIvsDeviceId())
                    .setAlarmLevel(1)
                    .setAlarmType(CollectionUtils.isEmpty(hwMMWTargetData) ? "石块,其他异物" : "人员");
        }

        if (!Params.LATEST_ALARM_TIME.containsKey(eventPrefix)) {
            log.info("=====> scheduled task has removed key!!!");
            return;
        }
        // 空指针 LATEST_ALARM_TIME.size = 0 定时任务remove了key
        HWAlarmInfo hwAlarmInfo = Params.LATEST_ALARM_TIME.get(eventPrefix);
        int alarmStateCalc = alarmStateCalc(eventPrefix);
        hwAlarmInfo.setAlarmState(alarmStateCalc);
        Params.LATEST_ALARM_TIME.put(eventPrefix, hwAlarmInfo);
        tkAlarmInfo.setAlarmState(alarmStateCalc);
        tkAlarmInfo.setIsPushAlarm(0);
        String message = GsonUtil.toJson(tkAlarmInfo);
        try {
            amqpSender.sendByRouter(tkConfigParam.getAmq().getTestMonitorPlatform(), tkConfigParam.getAmq().getAlarmMergeRoutingKey(), message);
        } catch (Exception e) {
            log.error("=====> 报警推送异常！,{}", e.getMessage());
            return;
        }
        log.info("报警推送成功！alarmData: {}", message);
        tkAlarmInfo.setIsPushAlarm(1);
        tkAlarmMapper.updateById(tkAlarmInfo);
    }

    /**
     * 判断是否需要推送上一条报警的结束状态
     *
     * @param eventPrefix
     * @param alarmTime   新报警的时间
     */
    private void pushLastAlarmIfTimeout(String eventPrefix, Date alarmTime) {
        if (Params.LATEST_ALARM_TIME.containsKey(eventPrefix)) {
            HWAlarmInfo hwAlarmInfo = Params.LATEST_ALARM_TIME.get(eventPrefix);
            long between = DateUtil.between(hwAlarmInfo.getAlarmTime(), alarmTime, DateUnit.SECOND);
            if (between > 10) {
                try {
                    String eventId = hwAlarmInfo.getEventId();
                    log.info("=====> 推送上一条{}报警结束, alarmEventID:{}", hwAlarmInfo.getAlarmType(), eventId);
                    ivsService.pushCloseAlarm(eventId, alarmTime);
                } catch (Exception e) {
                    log.error("=====> 推送结束报警错误！");
                } finally {
                    Params.LATEST_ALARM_TIME.remove(eventPrefix);
                }
            }
        }
    }

    /**
     * 计算报警状态 0报警结束、1报警开始、2报警进行中
     *
     * @return
     */
    private int alarmStateCalc(String latestKey) {
        if (Params.LATEST_ALARM_TIME.get(latestKey).getAlarmLevel() == 1) {
            // 新的报警事件
            log.info("=====> 新的报警");
            return AlarmStateEnum.START.getValue();
        } else {
            log.info("=====> 报警进行中");
            return AlarmStateEnum.MIDDLE.getValue();
        }
    }

    private void nceAlarmHandler(String eventPrefix, String alarmEventId, Date alarmTime, HuaweiNceCamera huaweiNceCamera, TKAlarmInfo tkAlarmInfo, HuaweiIvsDispositionNotificationCallback.DispositionNotificationObject notificationObject) {

        HWAlarmInfo hwAlarmInfo;
        if (!Params.LATEST_ALARM_TIME.containsKey(eventPrefix)) {
            // 设置报警基本信息
            hwAlarmInfo = new HWAlarmInfo();
            hwAlarmInfo.setEventId(alarmEventId);
            hwAlarmInfo.setAlarmType("ivs");
            hwAlarmInfo.setAlarmTime(alarmTime);
            hwAlarmInfo.setAlarmState(AlarmStateEnum.START.getValue());
            hwAlarmInfo.setAddDownloadQueue(false);
            Params.LATEST_ALARM_TIME.put(eventPrefix, hwAlarmInfo);
            // 设置报警参数
            tkAlarmInfo.setAlarmEventId(alarmEventId);
        } else {
            hwAlarmInfo = Params.LATEST_ALARM_TIME.get(eventPrefix);
            hwAlarmInfo.setAlarmTime(alarmTime);
            Params.LATEST_ALARM_TIME.put(eventPrefix, hwAlarmInfo);
        }
        log.info("开始处理ivs -- 光视报警");
        HuaweiNceDefaultDistance huaweiNceDefaultDistance = huaweiNceDefaultDistanceMapper.selectById(huaweiNceCamera.getCameraNumber());
        String position = "";
        if (huaweiNceDefaultDistance == null) {
            position = tkConfigParam.getBase().getNceDistance();
        } else {
            position = huaweiNceDefaultDistance.getDistance();
        }
        tkAlarmInfo.setAlgorithmCode(tkConfigParam.getBase().getNcealgorithmCode());
        tkAlarmInfo.setAlarmEventId(hwAlarmInfo.getEventId());
        tkAlarmInfo.setAreaCode(tkConfigParam.getBase().getNceAreaCode());
        tkAlarmInfo.setIsPushAlarm(0);
        tkAlarmInfo.setAlarmImage(tkConfigParam.getAmq().getImagePath() + tkConfigParam.getBase().getCompanyName() + "/" + DateUtil.format(alarmTime, DatePattern.PURE_DATE_PATTERN)
                + "/" + tkConfigParam.getBase().getCompanyName() + "_" + tkConfigParam.getBase().getNcealgorithmCode() + "_" + tkConfigParam.getBase().getNceAreaCode() + "_" + notificationObject.getTriggerTime() + ".jpg");
        tkAlarmInfo.setAlarmVideo(tkConfigParam.getAmq().getVideoPath() + tkConfigParam.getBase().getCompanyName() + "/" + DateUtil.format(alarmTime, DatePattern.PURE_DATE_PATTERN)
                + "/" + tkConfigParam.getBase().getCompanyName() + "_" + tkConfigParam.getBase().getNcealgorithmCode() + "_" + tkConfigParam.getBase().getNceAreaCode() + "_" + notificationObject.getTriggerTime() + ".mp4");
        if (Params.nceDistance == null) {
            tkAlarmInfo.setPosition(position);
        } else {
            log.info("最新nce报警距离：{}", Params.nceDistance);
            Float distance = 0f;
            LambdaQueryWrapper<HuaweiNceAlarmDistance> wrapper = new LambdaQueryWrapper<>();
            wrapper.le(HuaweiNceAlarmDistance::getBegin, Params.nceDistance);
            wrapper.gt(HuaweiNceAlarmDistance::getEnd, Params.nceDistance);
            HuaweiNceAlarmDistance alarmDistance = huaweiNceAlarmDistanceMapper.selectOne(wrapper);
            if (alarmDistance == null) {
                tkAlarmInfo.setPosition(position);
            } else if (alarmDistance.getCoefficient() == 0) {
                distance = alarmDistance.getBaseDistance();
                tkAlarmInfo.setPosition(tkConfigParam.getBase().getNcePosition() + alarmDistance.getType() + "+" + Math.round(distance));
            } else {
                distance = alarmDistance.getBaseDistance() + alarmDistance.getCoefficient() * (Params.nceDistance - alarmDistance.getBegin());
                tkAlarmInfo.setPosition(tkConfigParam.getBase().getNcePosition() + alarmDistance.getType() + "+" + Math.round(distance));
            }
        }
    }

    /**
     * ivs毫米波报警处理
     */
    private void ivsAlarmHandler(String alarmEventId, String ivsEventPrefix, Date alarmTime, TKAlarmInfo tkAlarmInfo, HuaweiIvsDispositionNotificationCallback.DispositionNotificationObject notificationObject) {

        log.info("开始处理ivs -- 雷视报警");
        tkAlarmInfo.setAlgorithmCode(tkConfigParam.getBase().getIvsalgorithmCode());
        HWAlarmInfo hwAlarmInfo;
        if (!Params.LATEST_ALARM_TIME.containsKey(ivsEventPrefix)) {
            // 设置报警基本信息
            hwAlarmInfo = new HWAlarmInfo();
            hwAlarmInfo.setEventId(alarmEventId);
            hwAlarmInfo.setAlarmType("ivs");
            hwAlarmInfo.setAlarmTime(alarmTime);
            hwAlarmInfo.setAlarmState(AlarmStateEnum.START.getValue());
            hwAlarmInfo.setAddDownloadQueue(false);
            hwAlarmInfo.setAlarmLevel(0);
            Params.LATEST_ALARM_TIME.put(ivsEventPrefix, hwAlarmInfo);
        } else {
            hwAlarmInfo = Params.LATEST_ALARM_TIME.get(ivsEventPrefix);
            hwAlarmInfo.setAlarmTime(alarmTime);
            Params.LATEST_ALARM_TIME.put(ivsEventPrefix, hwAlarmInfo);
        }
        // 设置报警参数
        tkAlarmInfo.setAlarmEventId(hwAlarmInfo.getEventId());
        tkAlarmInfo.setAreaCode(tkConfigParam.getBase().getIvsAreaCode());
        tkAlarmInfo.setIsPushAlarm(0);

        tkAlarmInfo.setAlarmImage(tkConfigParam.getAmq().getImagePath() + tkConfigParam.getBase().getCompanyName() + "/" + DateUtil.format(alarmTime, DatePattern.PURE_DATE_PATTERN)
                + "/" + tkConfigParam.getBase().getCompanyName() + "_" + tkConfigParam.getBase().getIvsalgorithmCode() + "_" + tkConfigParam.getBase().getIvsAreaCode() + "_" + notificationObject.getTriggerTime() + ".jpg");
        tkAlarmInfo.setAlarmVideo(tkConfigParam.getAmq().getVideoPath() + tkConfigParam.getBase().getCompanyName() + "/" + DateUtil.format(alarmTime, DatePattern.PURE_DATE_PATTERN)
                + "/" + tkConfigParam.getBase().getCompanyName() + "_" + tkConfigParam.getBase().getIvsalgorithmCode() + "_" + tkConfigParam.getBase().getIvsAreaCode() + "_" + notificationObject.getTriggerTime() + ".mp4");

    }
}
