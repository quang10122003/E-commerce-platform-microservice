package com.example.notification;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ForeignKeyMappingTemporaryTest {
    @Test
    void generatedForeignKeysMatchDatabaseWithoutChangingSchema() throws Exception {
        DriverManagerDataSource source = new DriverManagerDataSource("jdbc:mysql://localhost:3306/notifiservice_db",
                System.getenv("USERNAME_DATABASE"), System.getenv("PASSWORD_DATABASE"));
        StringWriter ddl = new StringWriter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(source);
        factory.setPackagesToScan("com.example.notification.adapter.entity");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of("jakarta.persistence.schema-generation.database.action", "none",
                "jakarta.persistence.schema-generation.scripts.action", "create",
                "jakarta.persistence.schema-generation.scripts.create-target", ddl));
        factory.afterPropertiesSet();
        try (Connection connection = source.getConnection();
                PreparedStatement query = connection.prepareStatement("""
                    SELECT k.TABLE_NAME,k.COLUMN_NAME,k.REFERENCED_TABLE_NAME,r.DELETE_RULE
                    FROM information_schema.KEY_COLUMN_USAGE k
                    JOIN information_schema.REFERENTIAL_CONSTRAINTS r
                    ON r.CONSTRAINT_SCHEMA=k.CONSTRAINT_SCHEMA AND r.CONSTRAINT_NAME=k.CONSTRAINT_NAME
                      AND r.TABLE_NAME=k.TABLE_NAME
                    WHERE k.TABLE_SCHEMA='notifiservice_db' AND k.REFERENCED_TABLE_NAME IS NOT NULL
                    """);
                ResultSet result = query.executeQuery()) {
            String sql = ddl.toString().toLowerCase(Locale.ROOT).replace("`", "").replaceAll("\\s+", " ");
            int count = 0;
            while (result.next()) {
                String table = result.getString(1).toLowerCase(Locale.ROOT);
                String column = result.getString(2).toLowerCase(Locale.ROOT);
                String referenced = result.getString(3).toLowerCase(Locale.ROOT);
                String action = result.getString(4).toLowerCase(Locale.ROOT);
                assertTrue(Arrays.stream(sql.split(";")).anyMatch(statement ->
                        statement.contains("alter table " + table + " ")
                        && statement.matches(".*foreign key\\s*\\(" + column + "\\).*")
                        && statement.contains("references " + referenced)
                        && statement.contains("on delete " + action)), table + "." + column + ": " + action + "\n" + sql);
                count++;
            }
            assertTrue(count > 0);
        } finally {
            factory.destroy();
        }
    }
}

