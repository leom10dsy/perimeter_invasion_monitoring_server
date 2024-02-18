package com.csrd.pims.tools;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 视频图片转存
 */
@Component
@Slf4j
public class RealVideoImagesUtil {

    //public static final ExecutorService executeChannel = Executors.newSingleThreadExecutor();

    //调用原子线程判断
    private AtomicBoolean running = new AtomicBoolean(true);

    public String streamURL;// 流地址

    public String filePath;// 视频文件路径

    public String imagePath;// 图片路径,存放截取视频某一帧的图片

    public void setStreamURL(String streamURL) {
        this.streamURL = streamURL;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * frame 转图片流
     */
    public BufferedImage FrameToBufferedImage(Frame frame) {
        //创建BufferedImage对象
        Java2DFrameConverter converter = new Java2DFrameConverter();
        return converter.getBufferedImage(frame);
    }


    /**
     * 执行视频流抓取
     */
    public Map<String, Object> run(String uuid, boolean screenshot) {
        Map<String, Object> resultMap = new HashMap<>(3);
        Map<String, Object> map = new LinkedHashMap<>(5);
        map.put("screenshot", screenshot);
        log.info(streamURL);
        //对日志进行处理
        avutil.av_log_set_level(avutil.AV_LOG_QUIET);
        // 获取视频源
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(streamURL);
        grabber.setFrameRate(25);
        grabber.setVideoBitrate(10 * 1024 * 1024);
        grabber.setOption("rtsp_transport", "tcp");
        FFmpegFrameRecorder recorder = null;
        try {
            grabber.start();
            Frame frame = grabber.grabFrame();
            if (frame != null) {
                File outFile = new File(filePath);
                if (!outFile.isFile()) {
                    try {
                        outFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
                recorder = new FFmpegFrameRecorder(filePath, 350, 240, 1);
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
//                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                // 降低编码延时
                recorder.setVideoOption("tune", "zerolatency");
                recorder.setMaxDelay(500);
                recorder.setGopSize(10);
                // 提升编码速度
                recorder.setVideoOption("preset", "ultrafast");
                recorder.setFormat("mp4");// 录制的视频格式
                recorder.setFrameRate(25);// 帧数
                recorder.setVideoBitrate(10 * 1024 * 1024);
                recorder.start();
                int flag = 0;
                long start = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
                log.info("=====> start record video!!");
                while (frame != null) {
                    //视频快照
                    if (screenshot && imagePath != null) {
                        if (flag == 100) {
                            frame = grabber.grabImage();
                            //文件储存对象
                            String fileName = imagePath + ".jpg";
                            File outPut = new File(fileName);
                            ImageIO.write(FrameToBufferedImage(frame), "jpg", outPut);
                            map.put("file" + flag, fileName);
                            screenshot = false;
                        }
                    }

                    recorder.record(frame);// 录制
                    frame = grabber.grabFrame();// 获取下一帧

                    flag++;
                    //try {
                    //    AVPacket pkt = grabber.grabPacket();
                    //    if (pkt == null || pkt.size() <= 0 || pkt.data() == null) {
                    //        log.info("pkt------------");
                    //    }
                    //    //else {
                    //        //System.out.println( pkt.size()+"------------------"+pkt.data());
                    //    //}
                    //}catch (Exception e){
                    //    log.error("=================> error", e);
                    //}
                    long end = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
                    if (end - start > 30 * 1000) {
//                        log.info("=====> video recoding finish: " + flag + " start: " + start + " end: " + end);
                        break;
                    }
                }
//                log.info("=====> video flag number: " + flag);
//                log.info("=====> video frame: " + frame);
//                recorder.record(frame);
                // 停止录制
                recorder.stop();
                recorder.release();
                grabber.stop();
                grabber.close();
                log.info("=====> 录制视频结束");
                //视频文件地址
                map.put("file", outFile);
                resultMap.put(uuid, map);
                //转成JSON存储到redis
                //redisUtil.set(uuid,resultMap,60*60);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                grabber.stop();
                grabber.close();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
            if (recorder != null) {
                try {
                    recorder.stop();
                    recorder.release();
                } catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return resultMap;
    }

    /**
     * 获取视频流
     */
    public void getVideoStream(String filePath, String imagePath, String streamURL, String sufferName) {
        getStream(filePath, imagePath, streamURL, false);
    }

    /**
     * 获取视频流快照
     */
    public void getStreamScreenshot(String filePath, String imagePath, String streamURL) {
        getStream(filePath, imagePath, streamURL, true);
    }

    /**
     * 开始执行下拉流截视频流
     */
    public void getStream(String filePath, String imagePath, String streamURL, boolean screenshot) {
        RealVideoImagesUtil videoUtil = new RealVideoImagesUtil();
        videoUtil.setFilePath(filePath);
        videoUtil.setImagePath(imagePath);
        videoUtil.setStreamURL(streamURL);
        log.info("start...");
        videoUtil.run(imagePath, screenshot);
    }

    public static void main(String[] args) {
        //getVideoStream();
        String path = "D://files/video/";
        String imgPath = "D://files/img/";
        String streamUrl =
                "rtsp://10.168.2.199:554/01167548417785100102?DstCode=01&ServiceType=1&ClientType=1&StreamID=1&SrcTP=2&DstTP=2&SrcPP=1&DstPP=1&MediaTransMode=0&BroadcastType=0&SV=1&Token=G9R0XNYsp2hkMKjZrKRaRnoK2qJxaxuQEeUia+li170=&"
                ;
        String streamUrl2 = "rtsp://10.168.2.51:554/onvif/live/2/1";
        final ExecutorService executeChannel1 = Executors.newSingleThreadExecutor();
        //final ExecutorService executeChannel2 = Executors.newSingleThreadExecutor();
        List<Thread> list1 = new LinkedList<>();
        RealVideoImagesUtil realVideoImagesUtil1 = new RealVideoImagesUtil();
        for (int i = 0; i < 1; i++) {
            String uuid = new SnowFlakeGenerateIdUtil(0L, 30L).generateNextId();
            Thread thread1 = new Thread(() -> {
                try {
                    realVideoImagesUtil1.getStreamScreenshot(path + uuid + "_channel1.mp4", null, streamUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            list1.add(thread1);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (Thread thread : list1) {
            executeChannel1.execute(thread);
            System.out.println("开始执行---------------------------");
        }
        executeChannel1.shutdown();

    }


}
