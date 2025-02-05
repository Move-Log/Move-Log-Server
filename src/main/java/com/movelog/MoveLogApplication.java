package com.movelog;

import com.movelog.domain.record.application.DataMigrationService;
import com.movelog.global.config.YamlPropertySourceFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Slf4j
@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
@PropertySource(value = { "classpath:oauth2/application-oauth2.yml" }, factory = YamlPropertySourceFactory.class)
@PropertySource(value = { "classpath:database/application-database.yml" }, factory = YamlPropertySourceFactory.class)
@PropertySource(value = { "classpath:swagger/application-springdoc.yml" }, factory = YamlPropertySourceFactory.class)
@PropertySource(value = { "classpath:s3/application-s3.yml" }, factory = YamlPropertySourceFactory.class)
@PropertySource(value = { "classpath:chatgpt/application-chatgpt.yml" }, factory = YamlPropertySourceFactory.class)
@PropertySource(value = { "classpath:webclient/application-webclient.yml" }, factory = YamlPropertySourceFactory.class)
@PropertySource(value = { "classpath:redis/application-redis.yml" }, factory = YamlPropertySourceFactory.class)
public class MoveLogApplication {

    private final DataMigrationService dataMigrationService;

    // 생성자를 통한 의존성 주입
    public MoveLogApplication(DataMigrationService dataMigrationService) {
        this.dataMigrationService = dataMigrationService;
    }

    public static void main(String[] args) {
        SpringApplication.run(MoveLogApplication.class, args);
    }

    @PostConstruct
    public void init() {
        log.info("Redis data migration start!");
        dataMigrationService.migrateDataToRedis();
        log.info("Redis data migration complete!");
    }
}
