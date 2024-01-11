package com.csrd.pims.amqp;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csrd.pims.amqp.tk.TKAlarmInfo;
import com.csrd.pims.bean.config.TkConfigParam;
import com.csrd.pims.config.runner.InitRunner;
import com.csrd.pims.dao.mapper.TkAlarmMapper;
import com.csrd.pims.tools.GsonUtil;
import com.csrd.pims.tools.Params;
import com.csrd.pims.tools.SftpUtils;
import com.jcraft.jsch.SftpException;
import com.rabbitmq.client.ShutdownSignalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author ：shiwei
 * @description：rabbitmq 状态监测
 * @date ：Created in 2020/5/27 15:32
 **/
@Slf4j
public class RabbitConnectionListener implements ConnectionListener {

    private final Executor executor = Executors.newSingleThreadExecutor();

    private TkAlarmMapper tkAlarmMapper;
    private TkConfigParam tkConfigParam;

    private AmqpSender amqpSender;

    public RabbitConnectionListener(TkAlarmMapper tkAlarmMapper, TkConfigParam tkConfigParam, AmqpSender amqpSender) {
        this.tkAlarmMapper = tkAlarmMapper;
        this.tkConfigParam = tkConfigParam;
        this.amqpSender = amqpSender;
    }

    @Override
    public void onCreate(Connection connection) {
        log.info("[RABBITMQ STATE]amqp connection onCreate: {}", connection);
        executor.execute(this::sendHistoryAlarm);
//        Params.MQ_STATE = true;
    }

    @Override
    public void onClose(Connection connection) {
        log.error("[RABBITMQ STATE]amqp connection onClose: {}", connection);
//        Params.MQ_STATE = false;
    }

    @Override
    public void onShutDown(ShutdownSignalException signal) {
        log.error("[RABBITMQ STATE]amqp connection onShutDown: {}", signal.getMessage());
//        Params.MQ_STATE = false;
    }


    public void sendHistoryAlarm() {
        LambdaQueryWrapper<TKAlarmInfo> objectQueryWrapper = new LambdaQueryWrapper<>();
        objectQueryWrapper.eq(TKAlarmInfo::getIsPushAlarm, 0);
        List<TKAlarmInfo> alarmInfos = tkAlarmMapper.selectList(objectQueryWrapper);
        if (CollectionUtil.isNotEmpty(alarmInfos)) {
            log.info("=====> 推送历史报警: {}", alarmInfos);
            for (TKAlarmInfo alarmInfo : alarmInfos) {
                alarmInfo.setIsPushAlarm(null);
                String message = GsonUtil.toJson(alarmInfo);
                amqpSender.sendByRouter(tkConfigParam.getAmq().getTestMonitorPlatform(), tkConfigParam.getAmq().getAlarmMergeRoutingKey(), message);
                alarmInfo.setIsPushAlarm(1);
                tkAlarmMapper.updateById(alarmInfo);
                // 发送报警图片
                uploadImg(alarmInfo);
            }
        }
    }

    private void uploadImg(TKAlarmInfo alarmInfo) {
        try {
            String s_recordImgSavePath = Params.LOCAL_STORAGE_PATH + "img" + File.separator;
            // 图片保存路径
            String targetImgName = s_recordImgSavePath + alarmInfo.getAlarmEventId() + ".jpg"; // 默认保存路径
            // 上传文件到远程
            SftpUtils sftpUtils = new SftpUtils();
            TkConfigParam.Sftp sftp = tkConfigParam.getSftp();
            boolean b = sftpUtils.login(sftp.getUsername(), sftp.getPassword(), sftp.getHost(), sftp.getPort(), null);
            if (b) {
                File targetFile = new File(targetImgName);
                InputStream inputStream = Files.newInputStream(targetFile.toPath());
                try {
                    String[] split = alarmInfo.getAlarmImage().split("/");
                    if (split.length <= 1) {
                        split = alarmInfo.getAlarmImage().split("\\\\");
                    }
                    String imgPath = alarmInfo.getAlarmImage().substring(0, alarmInfo.getAlarmImage().indexOf(split.length - 1));
                    String imageName = split[split.length - 1];
                    sftpUtils.upload(imgPath, imageName, inputStream);
                } catch (SftpException e) {
                    log.info("=====> 图片上传失败", e);
                }
            }

        } catch (Exception e) {
            log.info("=====> 图片下载失败", e);
        }
    }
}