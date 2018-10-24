package com.tcc.core.common.config;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import lombok.Data;

/**
 * description
 * 
 * @author xuyi 2018年10月9日
 */
@Data
public class TccDataSourceProperties {

	/**
	 * Name of the datasource.
	 */
	private String name;

	/**
	 * Fully qualified name of the JDBC driver. Auto-detected based on the URL by
	 * default.
	 */
	private String driverClassName;

	/**
	 * JDBC URL of the database.
	 */
	private String url;

	/**
	 * Login username of the database.
	 */
	private String username;

	/**
	 * Login password of the database.
	 */
	private String password;

	/**
	 * 最大连接池数量.
	 *
	 * https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
	 */
	private int maxActive = 20;

	/**
	 * 最小连接池数量.
	 */
	private int minIdle = 10;

	/**
	 * This property controls the maximum number of milliseconds that a client
	 * (that's you) will wait for a connection from the pool. If this time is
	 * exceeded without a connection becoming available, a SQLException will be
	 * thrown. Lowest acceptable connection timeout is 250 ms. Default: 30000 (30
	 * seconds)
	 */
	private long connectionTimeout = SECONDS.toMillis(30);

	/**
	 * This property controls the maximum amount of time that a connection is
	 * allowed to sit idle in the pool. This setting only applies when minimumIdle
	 * is defined to be less than maximumPoolSize. Idle connections will not be
	 * retired once the pool reaches minimumIdle connections. Whether a connection
	 * is retired as idle or not is subject to a maximum variation of +30 seconds,
	 * and average variation of +15 seconds. A connection will never be retired as
	 * idle before this timeout. A value of 0 means that idle connections are never
	 * removed from the pool. The minimum allowed value is 10000ms (10 seconds).
	 * Default: 600000 (10 minutes)
	 */
	private long idleTimeout = MINUTES.toMillis(10);

	/**
	 * This property controls the maximum lifetime of a connection in the pool. An
	 * in-use connection will never be retired, only when it is closed will it then
	 * be removed. On a connection-by-connection basis, minor negative attenuation
	 * is applied to avoid mass-extinction in the pool. We strongly recommend
	 * setting this value, and it should be several seconds shorter than any
	 * database or infrastructure imposed connection time limit. A value of 0
	 * indicates no maximum lifetime (infinite lifetime), subject of course to the
	 * idleTimeout setting. Default: 1800000 (30 minutes)
	 */
	private long maxLifetime = MINUTES.toMillis(30);

	/**
	 * If your driver supports JDBC4 we strongly recommend not setting this
	 * property. This is for "legacy" drivers that do not support the JDBC4
	 * Connection.isValid() API. This is the query that will be executed just before
	 * a connection is given to you from the pool to validate that the connection to
	 * the database is still alive. Again, try running the pool without this
	 * property, HikariCP will log an error if your driver is not JDBC4 compliant to
	 * let you know. Default: none
	 */
	private String connectionTestQuery;

	/**
	 * Add a property (name/value pair) that will be used to configure the
	 * {@link DataSource}/{@link java.sql.Driver}.
	 */
	private Map<String, Object> dataSourcePropertyMap;

	/**
	 * You can use a existing DataSource or generate a new DataSource based on the
	 * configuration.
	 */
	@Autowired(required = false)
	@Qualifier(value = "tccDataSource")
	private DataSource tccDataSource;
}
