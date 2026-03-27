package com.example.eventalarm.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 앱 시작 시 DB 스키마 보정 실행
 * ddl-auto=update는 NOT NULL 제약 제거를 못 하므로 수동으로 처리
 */
@Component
public class DatabaseMigration {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigration.class);
    private final DataSource dataSource;

    public DatabaseMigration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void migrate() {
        alterColumnNullable("event", "event_date_time");
        alterColumnNullable("event", "location");
    }

    private void alterColumnNullable(String table, String column) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE " + table + " ALTER COLUMN " + column + " DROP NOT NULL");
            log.info("[Migration] {}.{} → NOT NULL 제약 제거 완료", table, column);
        } catch (Exception e) {
            // 이미 nullable이거나 컬럼 없으면 무시
            log.debug("[Migration] {}.{} 이미 nullable이거나 처리 불필요: {}", table, column, e.getMessage());
        }
    }
}
