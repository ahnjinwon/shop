package com.example.shop.settlement.batch;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Spring Batch 메타 테이블이 없을 경우 자동으로 schema-postgresql.sql을 실행해 초기화한다.
 */
@Configuration
public class BatchSchemaInitializerConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchSchemaInitializerConfig.class);
    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void initializeBatchSchema() throws SQLException {
        if (batchTablesExist()) {
            log.info("Spring Batch tables already exist. Skipping schema initialization.");
            return;
        }
        log.info("Spring Batch tables not found. Executing schema-postgresql.sql");
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("org/springframework/batch/core/schema-postgresql.sql")
        );
        DatabasePopulatorUtils.execute(populator, dataSource);
    }

    private boolean batchTablesExist() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String schema = connection.getSchema();
            if (schema == null || schema.isBlank()) {
                schema = "public";
            }

            DatabaseMetaData metaData = connection.getMetaData();
            String tableName = "batch_job_instance";
            if (metaData.storesUpperCaseIdentifiers()) {
                tableName = tableName.toUpperCase();
            } else if (metaData.storesMixedCaseIdentifiers()) {
                tableName = "Batch_Job_Instance";
            }

            try (ResultSet tables = metaData.getTables(connection.getCatalog(), schema, tableName, new String[]{"TABLE"})) {
                return tables.next();
            }
        }
    }
}