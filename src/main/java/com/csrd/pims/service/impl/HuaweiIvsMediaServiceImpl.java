package com.csrd.pims.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.csrd.pims.amqp.tk.TKAlarmInfo;
import com.csrd.pims.bean.config.HuaweiConfigParam;
import com.csrd.pims.bean.config.TkConfigParam;
import com.csrd.pims.bean.huawei.HuaweiVideoQueue;
import com.csrd.pims.bean.huawei.param.SnapPicture;
import com.csrd.pims.dao.mapper.HuaweiVideoQueueMapper;
import com.csrd.pims.service.HuaweiIvsMediaService;
import com.csrd.pims.service.HuaweiIvsService;
import com.csrd.pims.tools.GsonUtil;
import com.csrd.pims.tools.Params;
import com.csrd.pims.tools.RealVideoImagesUtil;
import com.csrd.pims.tools.SftpUtils;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class HuaweiIvsMediaServiceImpl implements HuaweiIvsMediaService {

    @Value("${sysconfig.local-media-path}")
    private String localFilePath;

    @Autowired
    private HuaweiIvsService huaweiIvsService;

    @Resource
    private HuaweiConfigParam huaweiConfigParam;

    @Resource
    private HuaweiVideoQueueMapper huaweiVideoQueueMapper;

    @Resource
    private TkConfigParam tkConfigParam;


    @Override
    public String capturePic(String cameraNumber) {


        SnapPicture snapPicture = new SnapPicture();
        snapPicture.setCameraCode(cameraNumber);
        String url = "https://" + huaweiConfigParam.getIvs().getIp() + ":" +
                huaweiConfigParam.getIvs().getPort() + "/snapshot/manualsnapshot";
        try (
                InputStream res = HttpRequest.post(url).header("Cookie", Params.ivsCookie)
                        .body(GsonUtil.toJson(snapPicture)).execute().bodyStream()
        ) {
//            // 文件生成目录
//            String path = ApplicationContextUtil.getProperty("sysconfig.video.image-path") + cameraCode;
//            // 文件保存到数据库或供前端使用目录
//            String savePath = ApplicationContextUtil.getProperty("sysconfig.video.image-save-path") + cameraCode;
            // 获取当前毫秒数作为文件名称
            String dateTime = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN);
            String fileName = dateTime + ".jpg";
            // 判断文件是否存在，不存在则新建
            String path = "D:\\img\\test";
            if (!Files.exists(Paths.get(path))) {
                Files.createDirectories(Paths.get(path));
            }
            // 创建文件
            Path file = Files.createFile(Paths.get(path + "/" + fileName));
            Files.copy(res, file, StandardCopyOption.REPLACE_EXISTING);

            res.close();
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addDownloadAlarmIvsVideoQueue(String cameraNumber, String alarmEventId, String alarmVideoPath, Date alarmTime) {
        HuaweiVideoQueue queue = new HuaweiVideoQueue(alarmEventId, cameraNumber, alarmTime, alarmVideoPath, 0);
        // 保存数据库
        queue.setIsDownload(0);
        huaweiVideoQueueMapper.insert(queue);
    }

    @Override
    public void downloadIvsVideo(String cameraNumber, String alarmEventId, String videoPath, Date alarmTime) {
        String videoRtspUrl = huaweiIvsService.getVideoRtspUrl(cameraNumber, alarmTime, 4);

        if (videoRtspUrl == null) {
            return;
        }

        String s_recordFileSavePath = Params.LOCAL_STORAGE_PATH + "video" + File.separator;   // 录像保存路径
        String targetFileName = s_recordFileSavePath + alarmEventId + ".mp4"; // 默认保存路径
        // 创建文件夹
        if (!Files.exists(Paths.get(s_recordFileSavePath))) {
            try {
                Files.createDirectories(Paths.get(s_recordFileSavePath));
            } catch (IOException e) {
                log.info("=====> 创建视频文件夹失败");
            }
        }

        // 使用java程序保存视频
        RealVideoImagesUtil realVideoImagesUtil = new RealVideoImagesUtil();
        try {
            realVideoImagesUtil.getStreamScreenshot(targetFileName, null, videoRtspUrl);

            SftpUtils sftpUtils = new SftpUtils();
            TkConfigParam.Sftp sftp = tkConfigParam.getSftp();
            boolean b = sftpUtils.login(sftp.getUsername(), sftp.getPassword(), sftp.getHost(), sftp.getPort(), null);
            if (b) {
                // 上传文件到远程
                File targetFile = new File(targetFileName);
                if (!targetFile.isFile()) {
                    log.info("视频{}未下载成功", targetFileName);
                    return;
                }
                InputStream inputStream = Files.newInputStream(targetFile.toPath());
                try {
                    boolean upload = sftpUtils.upload(videoPath, alarmEventId + ".mp4", inputStream);
                    if (upload) {
                        inputStream.close();
                        if (Params.LOCAL_DELETE_FLAG) {
                            boolean delete = targetFile.delete();
                            if (delete) {
                                log.info("本地文件删除成功 [{}] ... ...", targetFileName);
                            }
                        }
                        HuaweiVideoQueue queue = new HuaweiVideoQueue();
                        queue.setAlarmEventId(alarmEventId);
                        queue.setIsDownload(1);
                        huaweiVideoQueueMapper.updateById(queue);
                    }
                } catch (SftpException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public synchronized void downloadIvsVideoByFFmpeg(String cameraNumber, String alarmEventId, String uploadVideoPath, Date alarmTime) {
        String videoRtspUrl = huaweiIvsService.getVideoRtspUrl(cameraNumber, alarmTime, 3);

        if (videoRtspUrl == null) {
            return;
        }
        String s_recordFileSavePath = Params.LOCAL_STORAGE_PATH + "video" + File.separator;   // 录像保存路径
        String targetFileName = s_recordFileSavePath + alarmEventId + ".mp4"; // 默认保存路径
        if (StrUtil.isBlank(videoRtspUrl) || StrUtil.isBlank(targetFileName)) {
            return;
        }
        // 创建文件夹
        if (!Files.exists(Paths.get(s_recordFileSavePath))) {
            try {
                Files.createDirectories(Paths.get(s_recordFileSavePath));
            } catch (IOException e) {
                log.info("=====> 创建视频文件夹失败");
            }
        }
        ProcessBuilder extractBuilder = new ProcessBuilder("ffmpeg", "-timeout", "100000", "-ss", "0", "-y", "-rtsp_transport", " tcp", "-i",
                videoRtspUrl, "-b:v", "1000k", "-vcodec", "copy", "-f", "mp4", targetFileName);
        try {
            Process process = extractBuilder.inheritIO().start();
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                log.error("Process did not finish within the timeout. Forcefully destroying...");
                process.destroy();
                return;
            }
        } catch (Exception e) {
            log.error("=====>  下载视频失败");
            e.printStackTrace();
        }
        SftpUtils sftpUtils = new SftpUtils();
        TkConfigParam.Sftp sftp = tkConfigParam.getSftp();
        boolean b = sftpUtils.login(sftp.getUsername(), sftp.getPassword(), sftp.getHost(), sftp.getPort(), null);
        if (b) {
            // 上传文件到远程
            File targetFile = new File(targetFileName);
            if (!targetFile.isFile()) {
                log.info("视频{}未下载成功", targetFileName);
                return;
            }
            try {
                InputStream inputStream = Files.newInputStream(targetFile.toPath());

                boolean upload = sftpUtils.upload(uploadVideoPath, alarmEventId + ".mp4", inputStream);
                if (upload) {
                    inputStream.close();
                    if (Params.LOCAL_DELETE_FLAG) {
                        boolean delete = targetFile.delete();
                        if (delete) {
                            log.info("本地文件删除成功 [{}] ... ...", targetFileName);
                        }
                    }
                    HuaweiVideoQueue queue = new HuaweiVideoQueue();
                    queue.setAlarmEventId(alarmEventId);
                    queue.setIsDownload(1);
                    huaweiVideoQueueMapper.updateById(queue);
                }
            } catch (Exception e) {
                log.error("======> 上传视频失败");
                e.printStackTrace();
            }
        }

    }


    /**
     * 执行Linux命令方法
     *
     * @param command
     */
    public void runCmd(String command) {
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(command);
            InputStream stderr = proc.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            int exitVal = proc.waitFor();
            System.out.println("Process exitValue: " + exitVal);
        } catch (Throwable t) {
            System.out.println(t);
            t.printStackTrace();
            log.error("ffmpeg transform video fail" + t);
        }

    }

    @Override
    public String uploadImg(TKAlarmInfo alarmInfo, String localFileName) {
        String endImage = "";
        try {
            // 上传文件到远程
            SftpUtils sftpUtils = new SftpUtils();
            TkConfigParam.Sftp sftp = tkConfigParam.getSftp();
            boolean b = sftpUtils.login(sftp.getUsername(), sftp.getPassword(), sftp.getHost(), sftp.getPort(), null);
            if (b) {
                File targetFile = new File(localFileName);
                InputStream inputStream = Files.newInputStream(targetFile.toPath());
                try {
                    String[] split = alarmInfo.getAlarmImage().split("/");
                    String imgPath = tkConfigParam.getSftp().getImagePath() + tkConfigParam.getBase().getCompanyName() + "/" + DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN) + "/";
                    if (split.length <= 1) {
                        split = alarmInfo.getAlarmImage().split("\\\\");
                        imgPath = alarmInfo.getAlarmImage().substring(0, alarmInfo.getAlarmImage().lastIndexOf("\\") + 1);
                    }
                    String imageName = split[split.length - 1].replace(".jpg", "-3.jpg");
                    sftpUtils.upload(imgPath, imageName, inputStream);
                    endImage = (imgPath + imageName).replace("/home", "");
                } catch (SftpException e) {
                    log.info("=====> 图片上传失败", e);
                }
            }

        } catch (Exception e) {
            log.info("=====> 图片下载失败", e);
        }
        return endImage;
    }

    public static void main(String[] args) {
        String rstpUrl = "rtsp://192.168.80.181:554/08120267724140340101?DstCode=01&ServiceType=3&ClientType=0&StreamID=1&SrcTP=2&DstTP=2&SrcPP=0&DstPP=1&MediaTransMode=0&BroadcastType=0&SV=1&TimeSpan=20240115T235857Z-20240115T235902Z&Token=4YSM1rXWnTZMSmCJjwwJe0V64/KZKJqC4rSgnY9vf+I=&";
        String aa = "D:\\files\\video\\aa.mp4";
        if (StrUtil.isBlank(rstpUrl) || StrUtil.isBlank(aa)) {
            return;
        }
        ProcessBuilder extractBuilder = new ProcessBuilder("ffmpeg", "-timeout", "100000", "-ss", "0", "-y", "-rtsp_transport", " tcp", "-i",
                rstpUrl, "-b:v", "1000k", "-vcodec", "copy", "-f", "mp4", aa);
        try {
            Process process = extractBuilder.inheritIO().start();
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                System.err.println("Process did not finish within the timeout. Forcefully destroying...");
                process.destroy();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

}
