package com.igsl.opsfinder.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Liquibase configuration to ensure database schema migration runs on startup.
 * This explicitly configures Liquibase to work with Spring Boot 4.0.0.
 */
@Configuration
public class LiquibaseConfig {

    /**
     * Configure Liquibase bean to run database migrations.
     *
     * @param dataSource the application datasource
     * @return configured SpringLiquibase instance
     */
    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.yaml");
        liquibase.setDefaultSchema("public");
        liquibase.setDropFirst(false);
        liquibase.setShouldRun(true);
        return liquibase;
    }
}
