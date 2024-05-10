package ro.linic.cloud.resource.server.starter.components;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.util.StringUtils;

import com.zaxxer.hikari.HikariDataSource;

public class MultitenantDataSource extends AbstractDataSource {
	
	private static HikariDataSource initDataSource(final DataSourceProperties properties) {
		final HikariDataSource dataSource = properties
                .initializeDataSourceBuilder().type(HikariDataSource.class).build();
        if (StringUtils.hasText(properties.getName()))
			dataSource.setPoolName(properties.getName());
        return dataSource;
	}
	
	private DataSourceProperties defaultProperties;
	private DataSource defaultDataSource;
	private Map<Integer, HikariDataSource> tenantDataSources = new HashMap<>();
	
	public MultitenantDataSource(final DataSourceProperties defaultProperties) {
		this.defaultProperties = Objects.requireNonNull(defaultProperties);
		this.defaultDataSource = initDataSource(defaultProperties);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return selectedDataSource().getConnection();
	}

	@Override
	public Connection getConnection(final String username, final String password) throws SQLException {
		return selectedDataSource().getConnection(username, password);
	}
	
	private DataSource selectedDataSource() {
		if (TenantContext.getCurrentTenantId() == null)
			return defaultDataSource;
		return tenantDatasource();
	}
	
	private DataSource tenantDatasource() {
		HikariDataSource tenantDs = tenantDataSources.get(TenantContext.getCurrentTenantId());
		if (tenantDs == null) {
			tenantDs = initDataSource(defaultProperties);
			tenantDs.setJdbcUrl(tenantDs.getJdbcUrl()+"-"+TenantContext.getCurrentTenantId());
			Flyway.configure().dataSource(tenantDs).load().migrate();
			tenantDataSources.put(TenantContext.getCurrentTenantId(), tenantDs);
		}
		return tenantDs;
	}
}