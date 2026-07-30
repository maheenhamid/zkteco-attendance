package com.zkteco.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

// Explicit primary-datasource JPA config, required because
// SecondaryDataSourceConfig declares its own @EnableJpaRepositories for the
// "second" datasource - once any custom @EnableJpaRepositories exists anywhere,
// Spring Boot's automatic repository-scanning auto-configuration backs off for
// the WHOLE app, so the primary repositories need this equivalent explicit
// declaration (using Spring Boot's default entityManagerFactory/transactionManager
// beans, which are unaffected and still auto-configured for spring.datasource.*).
@EntityScan("com.zkteco.attendance.entity")
@EnableJpaRepositories(basePackages = "com.zkteco.attendance.repository")
@SpringBootApplication
@EnableScheduling
public class AttendanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceApplication.class, args);
    }
}
