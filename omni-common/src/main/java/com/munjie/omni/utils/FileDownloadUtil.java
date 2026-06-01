package com.munjie.omni.utils;

import com.munjie.omni.exception.CustomException;
import com.munjie.omni.pojo.dto.FileInfoDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Slf4j
public class FileDownloadUtil {


    public static ResponseEntity<ByteArrayResource> downloadAll(File zipFile, List<FileInfoDTO> fileList, HttpServletRequest request) throws IOException {
        // 创建zip文件
        //下载文件
        String fileName = zipFile.getName();
        byte[] data = Files.readAllBytes(Paths.get(zipFile.getAbsolutePath()));
        ByteArrayResource resource = new ByteArrayResource(data);
        boolean b = Files.deleteIfExists(Path.of(zipFile.getAbsolutePath()));
        fileList.forEach(m -> {
            try {
                Files.deleteIfExists(Path.of(m.getFile()));
            } catch (IOException e) {
                log.error("删除临时文件失败", e);
            }
        });
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                encodeDownloadFilename(fileName, request));
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(resource);

    }
    /**
     * 完美解决所有浏览器下载中文文件名乱码
     */
    public static String encodeDownloadFilename(String filename, HttpServletRequest request) {
        String agent = request.getHeader("User-Agent").toUpperCase();
        try {
            // 1. Chrome, Edge, Firefox 等现代浏览器：用 RFC 5987 标准
            if (agent.contains("CHROME") || agent.contains("EDGE") ||
                    agent.contains("FIREFOX") || agent.contains("SAFARI")) {
                return "attachment; filename*=UTF-8''" +
                        URLEncoder.encode(filename, "UTF-8")
                                .replaceAll("\\+", "%20");  // 关键！+号要换成 %20
            }
            // 2. IE 或很老的浏览器：用普通 URL Encode
            if (agent.contains("MSIE") || agent.contains("TRIDENT")) {
                return "attachment; filename=" + URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
            }

            // 3. 其他情况：都按现代浏览器处理
            return "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");

        } catch (Exception e) {
            return "attachment; filename=download.zip";
        }
    }

    /**
     * 使用给定的文件列表创建ZIP文件并将其发送到HTTP响应。
     *
     * @param fileList 文件列表
     * @param title    ZIP文件的标题
     * @param type     1-base64压缩，2-url-压缩
     */
    public static ResponseEntity<ByteArrayResource> downZipFile(List<FileInfoDTO> fileList, String title, Integer type,HttpServletRequest request) throws IOException {
        // 获取模板路径，并构建ZIP文件的完整路径。
        Path tempDir = getExportTempDir(title);
        // 生成 zip 文件的完整路径
        String zipFileName = title + System.currentTimeMillis() + ".zip";
        Path zipFilePath = tempDir.resolve(zipFileName);
        try {
            if (1 == type) {
                // 基于Base64编码的文件创建ZIP文件。
                zipFiles(fileList, zipFilePath);
            } else {
                zipFilesFromURLs(fileList, zipFilePath);
            }
            File file = new File(zipFilePath.toUri());
            return downloadAll(file, fileList,request);
        } catch (Exception e) {
            throw new CustomException("zip 文件下载异常");
        }
    }

    /**
     * 将Base64编码的文件列表压缩为一个ZIP文件。
     *
     * @param srcFiles 需要压缩的文件列表
     * @param zipFilePath  生成的ZIP文件
     */
    public static void zipFiles(List<FileInfoDTO> srcFiles, Path zipFilePath) throws IOException {
        // 如果ZIP文件不存在，创建它。
      /*  if (!zipFilePath.) {
            Files.createFile(zipFile.toPath());
        }*/

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            for (FileInfoDTO fileInfo : srcFiles) {
                // 解码文件名。
                String fileName = URLDecoder.decode(fileInfo.getFileName(), "UTF-8");

                // 创建ZIP条目并添加到ZIP输出流中。
                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);

                // 将Base64编码的文件内容解码为字节数组。
//                byte[] fileBytes = Base64.getDecoder().decode(fileInfo.getFile());
                byte[] fileBytes = getFileBytes(fileInfo.getFile());
                // 将字节数组写入ZIP文件。
                zos.write(fileBytes);
                zos.closeEntry();
            }
        }
    }

    /**
     * 使用从URL获取的文件列表创建ZIP文件。
     *
     * @param srcFilesURLs 需要压缩的文件URL列表
     * @param zipFilePath      生成的ZIP文件
     */
    public static void zipFilesFromURLs(List<FileInfoDTO> srcFilesURLs, Path zipFilePath) throws IOException {
        // 如果ZIP文件不存在，创建它。
       /* if (!zipFile.exists()) {
            Files.createFile(zipFile.toPath());
        }*/

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            for (FileInfoDTO dto : srcFilesURLs) {
                URL url = new URL(dto.getFile());

                // 获取文件的文件名。
                String fileName = Paths.get(url.getPath()).getFileName().toString();

                // 创建ZIP条目并添加到ZIP输出流中。
                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);

                // 从URL读取文件内容。
                try (InputStream is = url.openStream()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        // 将从URL读取的内容写入ZIP文件。
                        zos.write(buffer, 0, bytesRead);
                    }
                }
                zos.closeEntry();
            }
        }
    }

    /**
     * 获取类路径的绝对地址。
     *
     * @return 类路径的绝对地址
     */
    public static String getTemplatePath() {
        try {
            String realPath = new ClassPathResource("").getURL().getPath();
            return URLDecoder.decode(realPath, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



// ...

    /**
     * 获取一个真正可写的临时目录（自动兼容本地和生产环境）
     * 每天一个文件夹，格式：/tmp/export/2025-04-05_标题/
     */
    public static Path getExportTempDir(String title) throws IOException {
        // 1. 优先使用系统临时目录（Linux: /tmp, Windows: %TEMP%）
        String baseTempDir = System.getProperty("java.io.tmpdir");

        // 2. 在临时目录下创建我们专属的子目录（避免和其他程序冲突）
        Path baseDir = Paths.get(baseTempDir, "student-export");
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);  // 自动创建多级目录
        }

        // 3. 按日期 + 标题 创建当天专属文件夹（避免并发覆盖）
        String dirName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "_" + title;
        Path dailyDir = baseDir.resolve(dirName);
        if (!Files.exists(dailyDir)) {
            Files.createDirectories(dailyDir);  // 关键：真正可写！
        }
        return dailyDir;
    }


    public static byte[] getFileBytes(String filePath) throws IOException {
        File file = new File(filePath);

        // 检查文件是否存在
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }

        // 检查是否为文件
        if (!file.isFile()) {
            throw new IOException("路径不是文件: " + filePath);
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            long fileSize = file.length();

            // 检查文件大小是否超过整数范围
            if (fileSize > Integer.MAX_VALUE) {
                throw new IOException("文件太大，无法一次性读取到内存中");
            }

            byte[] bytes = new byte[(int) fileSize];
            int bytesRead = fis.read(bytes);

            // 确保读取完整
            if (bytesRead != fileSize) {
                throw new IOException("未能完整读取文件");
            }

            return bytes;
        }
    }

}