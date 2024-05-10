package ro.linic.cloud.resource.server.starter.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;

import ro.linic.cloud.resource.server.starter.components.MultitenantDataSource;

@AutoConfiguration(before = DataSourceAutoConfiguration.class)
public class MultitenantConfiguration {
    @Bean
    public DataSource multitenantDataSource(final DataSourceProperties properties) {
        return new MultitenantDataSource(properties);
    }
}