package com.tcc.core.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.google.common.collect.Maps;
import com.tcc.core.common.config.TccConfig;
import com.tcc.core.common.config.TccDataSourceProperties;
import com.tcc.core.common.constant.CommonConstant;
import com.tcc.core.domain.entity.TccParticipant;
import com.tcc.core.domain.entity.TccTransaction;
import com.tcc.core.exception.TccRuntimeException;
import com.tcc.core.repository.CoordinatorRepository;
import com.tcc.core.serializer.ObjectSerializer;
import com.tcc.core.service.AppNameService;
import com.tcc.core.utils.SpringApplicationHolder;
import com.zaxxer.hikari.HikariDataSource;

/**
 * description
 * 
 * @author xuyi 2018年10月9日
 */
public class DbCoordinatorRepository implements CoordinatorRepository {
	private static final Logger logger = LoggerFactory.getLogger(DbCoordinatorRepository.class);

	private DataSource tccDataSource;

	@SuppressWarnings("rawtypes")
	private ObjectSerializer serializer;

	@Override
	@SuppressWarnings("unchecked")
	public int create(TccTransaction tccTransaction) {
		String sql = "insert into " + CommonConstant.DB_TABLE
				+ "(trans_id,app_id,target_class,target_method,retried_count,"
				+ "create_time,update_time,version,status,invocation,role,pattern,confirm_method,cancel_method,faillist)"
				+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {

			byte[] serialize = tccTransaction.getParticipants() == null ? null
					: serializer.serialize(tccTransaction.getParticipants());
			byte[] faillist = tccTransaction.getFailList() == null ? null
					: serializer.serialize(tccTransaction.getFailList());
			return executeUpdate(sql, tccTransaction.getTransactionId(), tccTransaction.getAppName(),
					tccTransaction.getTargetClass(), tccTransaction.getTargetMethod(), tccTransaction.getRetriedCount(),
					tccTransaction.getCreateTime(), tccTransaction.getUpdateTime(), tccTransaction.getVersion(),
					tccTransaction.getStatus(), serialize, tccTransaction.getRole(), tccTransaction.getPattern(),
					tccTransaction.getConfirmAction(), tccTransaction.getCancelAction(), faillist);
		} catch (Exception e) {
			logger.error("executeUpdate-> ", e);
			return 0;
		}
	}

	@Override
	public TccTransaction findById(String txId, String appName) {
		String selectSql = "select * from " + CommonConstant.DB_TABLE + " where trans_id=? and app_id=?";
		List<Map<String, Object>> list = QueryById(selectSql, txId, appName);
		if (!StringUtils.isEmpty(list)) {
			List<TccTransaction> tccTxList = list.stream().filter(Objects::nonNull).map(this::buildByResultMap)
					.collect(Collectors.toList());
			if (!CollectionUtils.isEmpty(tccTxList) && tccTxList.size() > 0) {
				return tccTxList.get(0);
			}

		}
		return null;
	}

	@Override
	public int remove(String txId, String appName) {
		String sql = "delete from " + CommonConstant.DB_TABLE + " where trans_id=? and app_id=?";
		return executeUpdate(sql, txId, appName);
	}

	@Override
	public int update(TccTransaction tccTransaction) {
		StringBuilder sb = new StringBuilder();
		sb.append("update").append(CommonConstant.DB_TABLE).append(
				" set update_time = ?,version =version+1,retried_count =?,invocation=?,status=? ,pattern=? where trans_id = ? and app_id=? ");
		try {
			@SuppressWarnings("unchecked")
			final byte[] serialize = serializer.serialize(tccTransaction.getParticipants());
			String sql = sb.toString();
			return executeUpdate(sql, tccTransaction.getUpdateTime(), tccTransaction.getRetriedCount(), serialize,
					tccTransaction.getStatus(), tccTransaction.getPattern(), tccTransaction.getTransactionId(),
					tccTransaction.getAppName());
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public int updateFailList(TccTransaction tccTransaction) {
		try {
			@SuppressWarnings("unchecked")
			byte[] faillist = tccTransaction.getFailList() == null ? null
					: serializer.serialize(tccTransaction.getFailList());
			String sql = "update " + CommonConstant.DB_TABLE
					+ " set faillist=?, update_time=?,version=version+1  where trans_id = ? and app_id=? ";
			return executeUpdate(sql, faillist, new Date(), tccTransaction.getTransactionId(),
					tccTransaction.getAppName());
		} catch (Exception e) {
			logger.debug("add participant error", e);
			return 0;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public int updateParticipant(TccTransaction tccTransaction) {
		try {

			byte[] invocation = tccTransaction.getParticipants() == null ? null
					: serializer.serialize(tccTransaction.getParticipants());
			byte[] faillist = tccTransaction.getFailList() == null ? null
					: serializer.serialize(tccTransaction.getParticipants());
			String sql = "update " + CommonConstant.DB_TABLE
					+ " set invocation=?, faillist=?, update_time=?  where trans_id = ? and app_id=? ";
			return executeUpdate(sql, invocation, faillist, new Date(), tccTransaction.getTransactionId(),
					tccTransaction.getAppName());
		} catch (Exception e) {
			logger.debug("add participant error", e);
			return 0;
		}
	}

	@Override
	public int updateStatus(String txId, String appName, Integer status) {
		String sql = "update " + CommonConstant.DB_TABLE
				+ " set status=?, update_time=? where trans_id = ? and app_id=? ";
		return executeUpdate(sql, status, new Date(), txId, appName);
	}

	private int executeUpdate(final String sql, final Object... params) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = tccDataSource.getConnection();
			ps = connection.prepareStatement(sql);
			if (params != null) {
				for (int i = 0; i < params.length; i++) {
					ps.setObject(i + 1, params[i]);
				}
			}
			return ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("executeUpdate-> ", e);
			return 0;
		} finally {
			close(connection, ps, null);
		}
	}

	@Override
	public void init(TccConfig tccConfig) {
		try {
			final TccDataSourceProperties tccDbConfig = tccConfig.getTccDbConfig();
			if (tccDbConfig.getTccDataSource() != null && StringUtils.isEmpty(tccDbConfig.getUrl())) {
				tccDataSource = tccDbConfig.getTccDataSource();
			} else {
				HikariDataSource hikariDataSource = new HikariDataSource();
				hikariDataSource.setJdbcUrl(tccDbConfig.getUrl());
				hikariDataSource.setDriverClassName(tccDbConfig.getDriverClassName());
				hikariDataSource.setUsername(tccDbConfig.getUsername());
				hikariDataSource.setPassword(tccDbConfig.getPassword());
				hikariDataSource.setMaximumPoolSize(tccDbConfig.getMaxActive());
				hikariDataSource.setMinimumIdle(tccDbConfig.getMinIdle());
				hikariDataSource.setConnectionTimeout(tccDbConfig.getConnectionTimeout());
				hikariDataSource.setIdleTimeout(tccDbConfig.getIdleTimeout());
				hikariDataSource.setMaxLifetime(tccDbConfig.getMaxLifetime());
				hikariDataSource.setConnectionTestQuery(tccDbConfig.getConnectionTestQuery());
				if (tccDbConfig.getDataSourcePropertyMap() != null
						&& !tccDbConfig.getDataSourcePropertyMap().isEmpty()) {
					tccDbConfig.getDataSourcePropertyMap().forEach(hikariDataSource::addDataSourceProperty);
				}
				tccDataSource = hikariDataSource;
			}
		} catch (Exception e) {
			logger.warn("jdbc log init exception please check config:{}", e);
			throw new TccRuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private TccTransaction buildByResultMap(final Map<String, Object> map) {
		TccTransaction tccTransaction = new TccTransaction(
				SpringApplicationHolder.getInstance().getBean(AppNameService.class).getAppName());
		tccTransaction.setTransactionId((String) map.get("trans_id"));
		tccTransaction.setAppName((String) map.get("app_id"));
		tccTransaction.setRetriedCount((Integer) map.get("retried_count"));
		tccTransaction.setCreateTime((Date) map.get("create_time"));
		tccTransaction.setCancelAction((String) map.get("cancel_method"));
		tccTransaction.setUpdateTime((Date) map.get("update_time"));
		tccTransaction.setConfirmAction((String) map.get("confirm_method"));
		tccTransaction.setVersion((Integer) map.get("version"));
		tccTransaction.setTargetMethod((String) map.get("target_method"));
		tccTransaction.setStatus((Integer) map.get("status"));
		tccTransaction.setTargetClass((String) map.get("target_class"));
		tccTransaction.setRole((Integer) map.get("role"));
		tccTransaction.setPattern((Integer) map.get("pattern"));
		byte[] invocation = (byte[]) map.get("invocation");
		byte[] faillist = (byte[]) map.get("faillist");
		try {
			if (invocation != null) {
				List<TccParticipant> participants = (List<TccParticipant>) serializer.deserialize(invocation,
						ArrayList.class);
				tccTransaction.setParticipants(participants);
			}
			if (faillist != null) {
				List<TccParticipant> fails = (List<TccParticipant>) serializer.deserialize(faillist, ArrayList.class);
				tccTransaction.setFailList(fails);
			}
		} catch (TccRuntimeException e) {
			logger.warn("deserialize object exception", e);
		}
		return tccTransaction;
	}

	private List<Map<String, Object>> QueryById(String sql, String transId, String appName) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Map<String, Object>> list = null;
		try {
			connection = tccDataSource.getConnection();
			ps = connection.prepareStatement(sql);
			ps.setObject(1, transId);
			ps.setObject(2, appName);
			rs = ps.executeQuery();
			ResultSetMetaData md = rs.getMetaData();
			int columnCount = md.getColumnCount();
			list = new ArrayList<Map<String, Object>>();
			while (rs.next()) {
				Map<String, Object> rowData = Maps.newHashMap();
				for (int i = 1; i <= columnCount; i++) {
					rowData.put(md.getColumnName(i), rs.getObject(i));
				}
				list.add(rowData);
			}
		} catch (SQLException e) {
			logger.error("executeQuery-> ", e);
		} finally {
			close(connection, ps, rs);
		}
		return list;
	}

	private void close(final Connection connection, final PreparedStatement ps, final ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setSerialize(ObjectSerializer<?> serializer) {
		this.serializer = serializer;
	}
}
