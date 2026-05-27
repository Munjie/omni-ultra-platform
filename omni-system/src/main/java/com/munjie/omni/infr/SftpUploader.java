package com.munjie.omni.infr;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
public class SftpUploader {

    @Value("${sftp.host}")
    private String host;

    @Value("${sftp.port}")
    private int port;

    @Value("${sftp.username}")
    private String username;

    @Value("${sftp.password}")
    private String password;

    @Value("${sftp.remote-directory}")
    private String remoteDirectory;




        /**
         * 上传文件（你原来的方法，稍作优化）
         */
        public String uploadFile(String fileName, InputStream inputStream) throws JSchException, SftpException {
            ChannelSftp channelSftp = null;
            Session session = null;
            try {
                JSch jsch = new JSch();
                session = jsch.getSession(username, host, port);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                channelSftp = (ChannelSftp) session.openChannel("sftp");
                channelSftp.connect();
                try {
                    channelSftp.cd(remoteDirectory);
                } catch (SftpException e) {
                    channelSftp.mkdir(remoteDirectory);
                    channelSftp.cd(remoteDirectory);
                }

                channelSftp.put(inputStream, fileName, ChannelSftp.OVERWRITE);
                return fileName;
            } finally {
                if (channelSftp != null) {
                    channelSftp.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }
            }
        }

        /**
         * 删除远程服务器上的图片文件
         * @param fileName 上传时保存的文件名（例如：abc123.jpg）
         * @return true 删除成功，false 文件不存在或其他原因失败
         */
        public boolean deleteFile(String fileName) {
            ChannelSftp channelSftp = null;
            Session session = null;
            try {
                JSch jsch = new JSch();
                session = jsch.getSession(username, host, port);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                channelSftp = (ChannelSftp) session.openChannel("sftp");
                channelSftp.connect();
                channelSftp.cd(remoteDirectory);
                channelSftp.rm(fileName);
                return true;
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    System.out.println("文件已不存在: " + fileName);
                    return true;
                }
                log.error("删除文件异常",e);
                return false;
            } catch (Exception e) {
                log.error("删除文件异常",e);
                return false;
            } finally {
                if (channelSftp != null) {
                    channelSftp.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }
            }
        }
    }
