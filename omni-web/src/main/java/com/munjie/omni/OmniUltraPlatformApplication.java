package com.munjie.omni;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = { FreeMarkerAutoConfiguration.class })
@Slf4j
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class OmniUltraPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(OmniUltraPlatformApplication.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                log.info("OmniUltraPlatformApplication shutdown hook!");    }
        });
    }
}
