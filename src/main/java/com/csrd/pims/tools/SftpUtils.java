package com.csrd.pims.tools;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * @author ShiWei
 * @description sftp  connection tools
 */
@Slf4j
public class SftpUtils {

    private ChannelSftp sftp;
    private Session session;


    /**
     * 连接sftp服务器
     */
    public boolean login(String username, String password, String host, int port, String privateKey) {
        try {
            JSch jsch = new JSch();
            // 如果使用私钥 设置私钥
            if (privateKey != null) {
                jsch.addIdentity(privateKey);
            }

            session = jsch.getSession(username, host, port);
            // 使用密码登陆
            if (password != null) {
                session.setPassword(password);
            }
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");

            session.setConfig(config);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
            return true;
        } catch (JSchException e) {
            log.error("connect to server [{}:{}] failed. use sftp protocol", host, port);
            return false;
        }
    }


    public void logout() {
        if (sftp != null) {
            if (sftp.isConnected()) {
                sftp.disconnect();
            }
        }
        if (session != null) {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }

    /**
     * 上传到sftp作为文件。
     * directory:  basePath/directoryName
     *
     * @param directory    上传到该目录
     * @param sftpFileName sftp端文件名
     */
    public boolean upload(String directory, String sftpFileName, InputStream input) throws SftpException {
        try {
            if (directory != null && !"".equals(directory)) {
                boolean dirIsExist = dirIsExist(directory);
                if (!dirIsExist) {
                    createDirectory(directory);
                }
                sftp.cd(directory);
            }
            log.info("start to upload file [{}] ... ...", sftpFileName);
            //上传文件
            sftp.put(input, sftpFileName, ChannelSftp.RESUME);
            log.info("file [{}] uploaded successfully", sftpFileName);
            return true;
        } catch (SftpException e) {
            e.printStackTrace();
            log.error("update file error:{}", e.toString());
            return false;
        }
    }

    /**
     * directory:  basePath/directoryName
     */
    public void delete(String directory, String deleteFile) throws SftpException {
        if (directory != null && !"".equals(directory)) {
            sftp.cd(directory);
        }
        sftp.rm(deleteFile);
    }


    public Vector<?> listFiles(String directory) throws SftpException {
        return sftp.ls(directory);
    }


    public boolean isExistsFile(String directory, String fileName) {
        List<String> findFilelist = new ArrayList();
        ChannelSftp.LsEntrySelector selector = new ChannelSftp.LsEntrySelector() {
            @Override
            public int select(ChannelSftp.LsEntry lsEntry) {
                if (lsEntry.getFilename().equals(fileName)) {
                    findFilelist.add(fileName);
                }
                return 0;
            }
        };
        try {
            sftp.ls(directory, selector);
        } catch (SftpException e) {
            log.error("list file error:{}", e.toString());
        }
        if (findFilelist.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 创建目录
     *
     * @param directory 目录全路径
     */
    public void createDirectory(String directory) {
        try {
            String[] pathArray = directory.split("/");
            StringBuilder filePath = new StringBuilder("/");
            for (String path : pathArray) {
                if (path.equals("")) {
                    continue;
                }
                filePath.append(path).append("/");
                if (!dirIsExist(filePath.toString())) {
                    // 建立目录
                    sftp.mkdir(filePath.toString());
                }
            }
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }


    /**
     * 判断目录是否存在
     *
     * @param directory 文件夹目录
     * @return ture:存在；false:不存在
     */
    public boolean dirIsExist(String directory) {
        boolean isDirExistFlag = false;
        try {
            SftpATTRS sftpATTRS = sftp.lstat(directory);
            isDirExistFlag = true;
            return sftpATTRS.isDir();
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().equals("no such file")) {
                isDirExistFlag = false;
            }
        }
        return isDirExistFlag;
    }

}
