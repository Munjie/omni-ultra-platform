package com.munjie.omni.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.stream.Stream;

@Component
@Slf4j
public class TempFileCleanupTask {

    private static final Path BASE_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "student-export");

    @Scheduled(cron = "0 0 3 * * ?")  // 每天凌晨3点清理7天前的文件
    public void cleanupOldFiles() {
        log.info("开始定时清理临时文件失败");
        try (Stream<Path> paths = Files.walk(BASE_DIR)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> {
                     try {
                         return Files.getLastModifiedTime(p)
                                   .toInstant()
                                   .isBefore(Instant.now().minus(7, ChronoUnit.DAYS));
                     } catch (Exception e) {
                         return true;
                     }
                 })
                 .forEach(p -> {
                     try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                 });

            // 可选：删除空目录
            try (Stream<Path> dirStream = Files.walk(BASE_DIR)) {
                dirStream.filter(Files::isDirectory)
                         .sorted(Comparator.reverseOrder())
                         .forEach(p -> {
                             try {
                                 if (Files.list(p).findAny().isEmpty()) {
                                     Files.deleteIfExists(p);
                                 }
                             } catch (Exception ignored) {}
                         });
            }
        } catch (Exception e) {
            log.error("清理临时文件失败", e);
        }
    }
}